package jfixture.parse;

import jfixture.api.Fixture;
import jfixture.api.LoadFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureMethodCollectorTest {

    public static class FixtureAnnotationExample {
        @Fixture
        public String string() {
            return "string";
        }

        public boolean noFixture() {
            return false;
        }
    }

    @LoadFixtures(FixtureAnnotationExample.class)
    public static class LoadFixturesAnnotationExample {
        @Fixture
        public int integer() {
            return 1337;
        }
    }

    public static class InvalidConstructorExample {
        public InvalidConstructorExample(String unsupportedParameter) {
        }

        @Fixture
        public String fixture() {
            return "fixture";
        }
    }

    private final FixtureMethodCollector collector = new FixtureMethodCollector();

    @Test
    public void collect_fixture_annotation() {
        var methods = collector.collectMethodsFromClass(FixtureAnnotationExample.class);
        assertEquals(1, methods.size());
        assertSame(String.class, methods.get(0).getMethod().getReturnType());
    }

    @Test
    public void collect_load_fixtures_annotation() {
        var methods = collector.collectMethodsFromClass(LoadFixturesAnnotationExample.class);
        assertEquals(2, methods.size());
        assertSame(String.class, methods.get(0).getMethod().getReturnType());
        assertSame(int.class, methods.get(1).getMethod().getReturnType());
    }

    @Test
    public void invalid_constructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            collector.collectMethodsFromClass(InvalidConstructorExample.class);
        });
    }

}
