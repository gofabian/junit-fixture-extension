package jfixture.api;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixtureMethodParserTest {

    public static class MyTest {
        @Fixture
        public int integer() {
            return 42;
        }

        @Fixture
        public String string() {
            return "text";
        }
    }

    public static class CannotInvokeTest {
        @Fixture
        private int integer() {
            return 1337;
        }
    }

    @Test
    public void collect_fixtures_in_test_instance() {
        var fixtures = new FixtureMethodParser().parseFixtureDefinitions(new MyTest());

        assertEquals(2, fixtures.size());
        fixtures.sort(Comparator.comparing(f -> f.getType().getSimpleName().toLowerCase()));
        assertEquals(int.class, fixtures.get(0).getType());
        assertEquals(String.class, fixtures.get(1).getType());
    }

    @Test
    public void create_fixture_from_method() throws NoSuchMethodException {
        var method = MyTest.class.getMethod("integer");
        var definition = new FixtureMethodParser().createFixtureDefinition(new MyTest(), method);

        assertEquals(int.class, definition.getType());
    }

    @Test
    public void set_up_by_calling_fixture_method() throws NoSuchMethodException {
        var method = MyTest.class.getMethod("integer");
        var definition = new FixtureMethodParser().createFixtureDefinition(new MyTest(), method);

        var object = definition.setUp();
        assertEquals(42, object);
    }

    @Test
    public void when_fixture_method_cannot_be_invoked_then_throw_exception() throws NoSuchMethodException {
        var method = CannotInvokeTest.class.getDeclaredMethod("integer");
        var definition = new FixtureMethodParser().createFixtureDefinition(new CannotInvokeTest(), method);

        assertThrows(RuntimeException.class, definition::setUp);
    }

}
