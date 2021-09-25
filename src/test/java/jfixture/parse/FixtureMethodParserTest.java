package jfixture.parse;

import jfixture.api.Fixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FixtureMethodParserTest {

    public static class Example {
        @Fixture
        public int integer() {
            return 42;
        }

        @Fixture
        public String string() {
            return "text";
        }
    }

    @Test
    public void collect_fixtures_in_test_instance() {
        var parser = new FixtureMethodParser();
        var fixtures = parser.parseFixtureDefinitions(new Example());
        assertEquals(2, fixtures.size());
    }

}
