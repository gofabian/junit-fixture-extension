package jfixture;

import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(FixtureExtension.class)
public class FixtureExtensionTest {

    @Nested
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

    @Nested
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

    @Nested
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

}
