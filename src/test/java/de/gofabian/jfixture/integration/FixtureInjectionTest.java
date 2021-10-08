package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.api.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(FixtureExtension.class)
public class FixtureInjectionTest {

    @Fixture
    public String string() {
        return "string";
    }

    @Test
    public void fixture_is_provided_to_test_method(String string) {
        assertEquals("string", string);
    }
    
}
