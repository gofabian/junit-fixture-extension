package jfixture.integration;

import jfixture.FixtureExtension;
import jfixture.api.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(FixtureExtension.class)
public class FixtureDependenciesTest {

    @Fixture
    public int one() {
        return 1;
    }

    @Fixture
    public long two() {
        return 2L;
    }

    @Fixture
    public String twelve(int one, long two) {
        return "" + one + two;
    }

    @Test
    public void dependencies_are_provided_to_fixture(String twelve) {
        assertEquals("12", twelve);
    }

}
