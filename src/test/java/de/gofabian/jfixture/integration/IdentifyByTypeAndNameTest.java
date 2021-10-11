package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.api.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FixtureExtension.class)
public class IdentifyByTypeAndNameTest {

    @Fixture
    public String fixture1() {
        return "fixture1";
    }

    @Fixture
    public String fixture2() {
        return "fixture2";
    }

    @Test
    public void fixture_is_used_automatically(String fixture1, String fixture2, String any) {
        assertEquals("fixture1", fixture1);
        assertEquals("fixture2", fixture2);
        assertTrue(any.equals("fixture1") || any.equals("fixture2"));
    }

}
