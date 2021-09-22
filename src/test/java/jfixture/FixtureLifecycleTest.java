package jfixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixtureLifecycleTest {

    private static class MyFixtureDefinition extends FixtureDefinition {
        int i = 0;
        Object objectTearedDown;

        private MyFixtureDefinition() {
            super(Object.class);
        }

        @Override
        public Object setUp(FixtureResolver resolver) {
            i++;
            return "object" + i;
        }

        @Override
        public void tearDown(Object object) {
            objectTearedDown = object;
        }
    }

    private MyFixtureDefinition definition;

    @BeforeEach
    public void setUp() {
        definition = new MyFixtureDefinition();
    }

    @Test
    public void when_set_up_then_definition_will_set_up() {
        var lifecycle = new FixtureLifecycle(definition);
        var object = lifecycle.setUp(null);
        assertEquals("object1", object);
    }

    @Test
    public void when_lifecycle_sets_up_again_then_definition_will_not_be_set_up_again() {
        var lifecycle = new FixtureLifecycle(definition);
        lifecycle.setUp(null);
        var object = lifecycle.setUp(null);
        assertEquals("object1", object);
    }

    @Test
    public void when_tear_down_before_set_up_then_exception_will_be_thrown() {
        var lifecycle = new FixtureLifecycle(definition);
        assertThrows(IllegalStateException.class, lifecycle::tearDown);
    }

    @Test
    public void when_tear_down_after_set_up_then_definition_will_be_teared_down() {
        var lifecycle = new FixtureLifecycle(definition);
        lifecycle.setUp(null);
        lifecycle.tearDown();
        assertEquals("object1", definition.objectTearedDown);
    }

    @Test
    public void when_set_up_after_tear_down_then_definition_will_set_up_again() {
        var lifecycle = new FixtureLifecycle(definition);
        lifecycle.setUp(null);
        lifecycle.tearDown();
        var object = lifecycle.setUp(null);
        assertEquals("object2", object);
    }

}
