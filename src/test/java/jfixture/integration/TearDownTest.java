package jfixture.integration;

import jfixture.FixtureExtension;
import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FixtureExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TearDownTest {

    private int tearDownCount = 0;

    @Fixture
    public int withTearDown(FixtureContext context) {
        tearDownCount = 0;
        context.addTearDown(() -> tearDownCount++);
        return 1;
    }

    @Test
    public void tear_down_fixture_is_provided_to_test_method(int withTearDown) {
        assertEquals(0, tearDownCount);
    }

    @Test
    public void tear_down_is_called_only_once(int withTearDown) {
        assertEquals(0, tearDownCount);
    }

    @AfterEach
    public void fixture_is_teared_down_after_test() {
        assertEquals(1, tearDownCount);
    }

}
