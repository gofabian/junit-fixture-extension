package jfixture;

import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import jfixture.api.LoadFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureMethodParserTest {

    private final FixtureMethodParser parser = new FixtureMethodParser();

    public static class CreateFixtureDefinitionExample {
        final List<String> tearDowns = new ArrayList<>();

        @Fixture
        public String simple() {
            return "simple";
        }

        @Fixture(autoUse = true)
        public double autoUse() {
            return 1.8;
        }

        @Fixture
        public int param(String simple) {
            return simple.length();
        }

        @Fixture
        public char tearDown(String text, FixtureContext context, int index) {
            context.addTearDown(() -> tearDowns.add("first"));
            context.addTearDown(() -> tearDowns.add("second"));
            return 'x';
        }

        @Fixture
        private String cannotInvoke() {
            return "error";
        }

        public static Method getMethod(String methodName) {
            for (var method : CreateFixtureDefinitionExample.class.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("unknown method: " + methodName + "()");
        }
    }

    @Test
    public void createFixtureDefinition() {
        var definition = parser.createFixtureDefinition(
                new CreateFixtureDefinitionExample(),
                CreateFixtureDefinitionExample.getMethod("simple")
        );

        assertEquals(String.class, definition.getType());
        assertEquals(Collections.emptyList(), definition.getDependencyTypes());
        assertFalse(definition.isAutoUse());
        assertEquals("simple", definition.setUp(Collections.emptyList()));
    }

    @Test
    public void createFixtureDefinitionWithAutoUse() {
        var definition = parser.createFixtureDefinition(
                new CreateFixtureDefinitionExample(),
                CreateFixtureDefinitionExample.getMethod("autoUse")
        );

        assertTrue(definition.isAutoUse());
    }

    @Test
    public void createFixtureDefinitionWithParameter() {
        var definition = parser.createFixtureDefinition(
                new CreateFixtureDefinitionExample(),
                CreateFixtureDefinitionExample.getMethod("param")
        );

        assertEquals(List.of(String.class), definition.getDependencyTypes());
        assertEquals("complex".length(), definition.setUp(List.of("complex")));
    }

    @Test
    public void createFixtureDefinitionWithTearDown() {
        var example = new CreateFixtureDefinitionExample();
        var definition = parser.createFixtureDefinition(
                example,
                CreateFixtureDefinitionExample.getMethod("tearDown")
        );

        assertEquals(Collections.emptyList(), example.tearDowns);
        definition.setUp(List.of("string", 42));
        definition.tearDown("");
        assertEquals(List.of("first", "second"), example.tearDowns);
    }

    @Test
    public void setUpFixtureDefinitionWithMissingParameter() {
        var definition = parser.createFixtureDefinition(
                new CreateFixtureDefinitionExample(),
                CreateFixtureDefinitionExample.getMethod("param")
        );

        assertThrows(IllegalArgumentException.class, () -> definition.setUp(Collections.emptyList()));
    }

    @Test
    public void setUpFixtureDefinitionWithAccessDenied() {
        var definition = parser.createFixtureDefinition(
                new CreateFixtureDefinitionExample(),
                CreateFixtureDefinitionExample.getMethod("cannotInvoke")
        );

        assertThrows(IllegalStateException.class, () -> definition.setUp(Collections.emptyList()));
    }

    public static class ParseMethodsExample {
        @Fixture
        public String text() {
            return "text";
        }

        @Fixture
        public int count(String text) {
            return text.length();
        }
    }

    @Test
    public void parseFixtureMethods() {
        var definitions = parser.parseClass(ParseMethodsExample.class);

        assertEquals(2, definitions.size());
        definitions.sort(Comparator.comparing(d -> d.getType().getSimpleName()));
        assertEquals(String.class, definitions.get(0).getType());
        assertEquals(int.class, definitions.get(1).getType());
    }

    @LoadFixtures(ParseMethodsExample.class)
    public static class ParseExternalClassesExample {
        @Fixture
        public String priority() {
            return "highest";
        }
    }

    @Test
    public void parseExternalClasses() {
        var definitions = parser.parseClass(ParseExternalClassesExample.class);

        assertEquals(3, definitions.size());
        assertEquals(String.class, definitions.get(2).getType());
        definitions.remove(definitions.size() - 1);
        definitions.sort(Comparator.comparing(d -> d.getType().getSimpleName()));
        assertEquals(String.class, definitions.get(0).getType());
        assertEquals(int.class, definitions.get(1).getType());
    }

}
