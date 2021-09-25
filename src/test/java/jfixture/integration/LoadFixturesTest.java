package jfixture.integration;

import jfixture.FixtureExtension;
import jfixture.api.LoadFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FixtureExtension.class)
@LoadFixtures(ExampleFixtures.class)
public class LoadFixturesTest {

    @Test
    public void useFixtureFromExternalContainer(long longInteger, int integer) {
        var container = new ExampleFixtures();
        assertEquals(container.longInteger(), longInteger);
        assertEquals(container.integer(), integer);
    }

}
