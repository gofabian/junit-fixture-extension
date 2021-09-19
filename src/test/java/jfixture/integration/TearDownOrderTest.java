package jfixture.integration;

import jfixture.FixtureExtension;
import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(FixtureExtension.class)
public class TearDownOrderTest {

    private final List<String> tearDowns = new ArrayList<>();

    @Fixture
    public int integer(FixtureContext context) {
        context.addTearDown(() -> tearDowns.add("integer"));
        return 1;
    }

    @Fixture
    public String string(FixtureContext context) {
        context.addTearDown(() -> tearDowns.add("string"));
        return "string";
    }

    @Test
    @Tag("reverse_order_1")
    public void tear_downs_happen_in_reverse_order_1(int integer, String string) {
    }

    @Test
    @Tag("reverse_order_2")
    public void tear_downs_happen_in_reverse_order_2(String string, int integer) {
    }

    @AfterEach
    public void check_tear_down_order(TestInfo testInfo) {
        if (testInfo.getTags().contains("reverse_order_1")) {
            assertEquals("string", tearDowns.get(0));
            assertEquals("integer", tearDowns.get(1));
        } else if (testInfo.getTags().contains("reverse_order_2")) {
            assertEquals("integer", tearDowns.get(0));
            assertEquals("string", tearDowns.get(1));
        } else {
            fail();
        }
    }

}
