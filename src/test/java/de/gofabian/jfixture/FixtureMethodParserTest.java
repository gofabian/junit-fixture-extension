package de.gofabian.jfixture;

import de.gofabian.jfixture.api.Fixture;
import de.gofabian.jfixture.api.FixtureContext;
import de.gofabian.jfixture.api.FixtureId;
import de.gofabian.jfixture.api.LoadFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        assertEquals(String.class, definition.getId().getType());
        assertEquals(Collections.emptyList(), definition.getDependencyIds());
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

        var dependencyTypes = definition.getDependencyIds().stream()
                .map(FixtureId::getType).collect(Collectors.toList());
        assertEquals(List.of(String.class), dependencyTypes);
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
        definitions.sort(Comparator.comparing(d -> d.getId().getType().getSimpleName()));
        assertEquals(String.class, definitions.get(0).getId().getType());
        assertEquals(int.class, definitions.get(1).getId().getType());
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
        assertEquals(String.class, definitions.get(2).getId().getType());
        definitions.remove(definitions.size() - 1);
        definitions.sort(Comparator.comparing(d -> d.getId().getType().getSimpleName()));
        assertEquals(String.class, definitions.get(0).getId().getType());
        assertEquals(int.class, definitions.get(1).getId().getType());
    }

    @Test
    public void parseSameClassTwice() {
        var parser = new FixtureMethodParser();
        var definitions1 = parser.parseClass(ParseMethodsExample.class);
        var definitions2 = parser.parseClass(ParseMethodsExample.class);

        assertSameElements(definitions1, definitions2);
    }

    @Test
    public void parseSameInstanceTwice() {
        var parser = new FixtureMethodParser();
        var definitions1 = parser.parseInstance(new ParseExternalClassesExample());
        var definitions2 = parser.parseClass(ParseExternalClassesExample.class);

        assertSameElements(definitions1, definitions2);
    }

    private void assertSameElements(List<?> list1, List<?> list2) {
        assertEquals(list1.size(), list2.size());
        for (var i = 0; i < list1.size(); i++) {
            assertSame(list1.get(i), list2.get(i));
        }
    }


}
