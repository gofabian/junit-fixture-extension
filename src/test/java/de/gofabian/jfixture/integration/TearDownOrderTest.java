package de.gofabian.jfixture.integration;

import de.gofabian.jfixture.FixtureExtension;
import de.gofabian.jfixture.api.Fixture;
import de.gofabian.jfixture.api.FixtureContext;
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

    private static final List<String> tearDowns = new ArrayList<>();

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

    @Fixture
    public boolean database(FixtureContext context) {
        context.addTearDown(() -> tearDowns.add("database"));
        return true;
    }

    @Fixture
    public long table(FixtureContext context, boolean database) {
        context.addTearDown(() -> tearDowns.add("table"));
        return 2L;
    }

    @Test
    @Tag("reverse_order_1")
    public void tear_downs_happen_in_reverse_order_1(int integer, String string) {
    }

    @Test
    @Tag("reverse_order_2")
    public void tear_downs_happen_in_reverse_order_2(String string, int integer) {
    }

    @Test
    @Tag("fixture_dependency")
    public void dependency_is_teared_down_last(long table) {
    }

    @AfterEach
    public void check_tear_down_order(TestInfo testInfo) {
        if (testInfo.getTags().contains("reverse_order_1")) {
            assertEquals("string", tearDowns.get(0));
            assertEquals("integer", tearDowns.get(1));
        } else if (testInfo.getTags().contains("reverse_order_2")) {
            assertEquals("integer", tearDowns.get(0));
            assertEquals("string", tearDowns.get(1));
        } else if (testInfo.getTags().contains("fixture_dependency")) {
            assertEquals("table", tearDowns.get(0));
            assertEquals("database", tearDowns.get(1));
        } else {
            tearDowns.clear();
            fail();
        }

        tearDowns.clear();
    }

}
