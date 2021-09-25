package jfixture.parse;

import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureDefinitionFactoryTest {

    private final FixtureDefinitionFactory factory = new FixtureDefinitionFactory();

    @Nested
    public class CreateSingleFixtureDefinition {

        public class Example {
            final List<String> tearDowns = new ArrayList<>();

            @Fixture
            public String simple() {
                return "fixture";
            }

            @Fixture
            public long tearDown(FixtureContext context) {
                context.addTearDown(() -> tearDowns.add("tearDown1"));
                context.addTearDown(() -> tearDowns.add("tearDown2"));
                return 42L;
            }

            @Fixture
            public int parameters(String simple, FixtureContext context1, long tearDown, FixtureContext context2) {
                assertNotNull(context1);
                assertSame(context1, context2);
                assertEquals("string", simple);
                assertEquals(123L, tearDown);
                context1.addTearDown(() -> tearDowns.add("tearDown"));
                return 1337;
            }

            @Fixture
            private boolean error() {
                return false;
            }
        }


        @Test
        public void create_fixture_without_parameters() {
            var definition = factory.createFixtureDefinition(new Example(),
                    getMethod(Example.class, "simple"),
                    Collections.emptyList());

            assertSame(String.class, definition.getType());
            assertEquals(Collections.emptyList(), definition.getDependencies());
            assertFalse(definition.isAutoUse());

            var object = definition.setUp(Collections.emptyList());
            assertEquals("fixture", object);
        }

        @Test
        public void create_fixture_with_tear_down() {
            var instance = new Example();
            var definition = factory.createFixtureDefinition(instance,
                    getMethod(Example.class, "tearDown"),
                    Collections.emptyList());

            var object = definition.setUp(Collections.emptyList());
            assertEquals(42L, object);

            assertEquals(Collections.emptyList(), instance.tearDowns);
            definition.tearDown(object);
            assertEquals(Arrays.asList("tearDown1", "tearDown2"), instance.tearDowns);
        }

        @Test
        public void create_fixture_with_parameters() {
            var instance = new Example();
            var definition = factory.createFixtureDefinition(instance,
                    getMethod(Example.class, "parameters"),
                    Collections.emptyList());

            var object = definition.setUp(Arrays.asList("string", 123L));
            assertEquals(1337, object);

            assertEquals(Collections.emptyList(), instance.tearDowns);
            definition.tearDown(object);
            assertEquals(Collections.singletonList("tearDown"), instance.tearDowns);
        }

        @Test
        public void create_fixture_with_missing_parameter() {
            var definition = factory.createFixtureDefinition(new Example(),
                    getMethod(Example.class, "parameters"),
                    Collections.emptyList());

            assertThrows(IllegalArgumentException.class, () ->
                    definition.setUp(Collections.singletonList("string"))
            );
        }

        @Test
        public void create_fixture_with_setup_error() {
            var definition = factory.createFixtureDefinition(new Example(),
                    getMethod(Example.class, "error"),
                    Collections.emptyList());

            assertThrows(IllegalStateException.class, () ->
                    definition.setUp(Collections.emptyList())
            );
        }

    }

    @Nested
    public class CreateMultipleFixtureDefinitions {

        @Test
        public void create_fixtures() {
            class Example {
                @Fixture
                public String string() {
                    return "string";
                }

                @Fixture(autoUse = true)
                public int integer() {
                    return 42;
                }

                public boolean noFixture() {
                    return false;
                }
            }

            var definitions = factory.createFixtureDefinitions(Arrays.asList(
                    new FixtureMethod(new Example(), getMethod(Example.class, "string")),
                    new FixtureMethod(new Example(), getMethod(Example.class, "integer"))
            ));

            assertEquals(2, definitions.size());
            assertEquals(String.class, definitions.get(0).getType());
            assertFalse(definitions.get(0).isAutoUse());
            assertEquals(Collections.emptyList(), definitions.get(0).getDependencies());
            assertEquals(int.class, definitions.get(1).getType());
            assertTrue(definitions.get(1).isAutoUse());
            assertEquals(Collections.emptyList(), definitions.get(1).getDependencies());
        }

        @Test
        public void create_fixtures_with_dependency() {
            class Example {
                @Fixture
                public String string(int integer) {
                    return "string" + integer;
                }

                @Fixture
                public int integer() {
                    return 42;
                }
            }

            {
                var definitions = factory.createFixtureDefinitions(Arrays.asList(
                        new FixtureMethod(new Example(), getMethod(Example.class, "string")),
                        new FixtureMethod(new Example(), getMethod(Example.class, "integer"))
                ));

                assertEquals(2, definitions.size());
                assertEquals(String.class, definitions.get(0).getType());
                assertEquals(1, definitions.get(0).getDependencies().size());
                assertSame(definitions.get(1), definitions.get(0).getDependencies().get(0));
                assertEquals(int.class, definitions.get(1).getType());
                assertEquals(Collections.emptyList(), definitions.get(1).getDependencies());
            }
            {
                var definitions = factory.createFixtureDefinitions(Arrays.asList(
                        new FixtureMethod(new Example(), getMethod(Example.class, "integer")),
                        new FixtureMethod(new Example(), getMethod(Example.class, "string"))
                ));

                assertEquals(2, definitions.size());
                assertEquals(int.class, definitions.get(0).getType());
                assertEquals(Collections.emptyList(), definitions.get(0).getDependencies());
                assertEquals(String.class, definitions.get(1).getType());
                assertEquals(1, definitions.get(1).getDependencies().size());
                assertSame(definitions.get(0), definitions.get(1).getDependencies().get(0));
            }
        }

        @Test
        public void create_fixtures_with_missing_dependency() {
            class Example {
                @Fixture
                public String string(int integer) {
                    return "string" + integer;
                }
            }

            assertThrows(IllegalArgumentException.class, () ->
                    factory.createFixtureDefinitions(Collections.singletonList(
                            new FixtureMethod(new Example(), getMethod(Example.class, "string"))
                    ))
            );
        }

    }

    private Method getMethod(Class<?> type, String methodName) {
        for (var method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("unknown method: " + type + "." + methodName + "()");
    }

}
