package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.api.Fixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FixtureExtension.class)
public class NestedClassTest {

    @Fixture
    public String text(int count) {
        return "count-" + count;
    }

    @Nested
    public class NestedTest {
        @Fixture
        public int count() {
            return 42;
        }

        @Test
        public void test(String text) {
            assertEquals("count-42", text);
        }
    }

}
