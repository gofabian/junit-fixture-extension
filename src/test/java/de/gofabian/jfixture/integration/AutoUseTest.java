package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.api.Fixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FixtureExtension.class)
public class AutoUseTest {

    private static boolean fixtureIsSetUp = false;

    @AfterEach
    public void tearDown() {
        fixtureIsSetUp = false;
    }

    @Fixture(autoUse = true)
    public String usedAutomatically() {
        fixtureIsSetUp = true;
        return "usedAutomatically";
    }

    @Test
    public void fixture_is_used_automatically() {
        assertTrue(fixtureIsSetUp);
    }

    @Test
    public void fixture_is_used_automatically_on_each_test() {
        assertTrue(fixtureIsSetUp);
    }

}
