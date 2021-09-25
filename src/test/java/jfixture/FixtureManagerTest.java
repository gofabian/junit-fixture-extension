package jfixture;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureManagerTest {

    private static class MyFixtureDefinition extends FixtureDefinition {
        public MyFixtureDefinition(Class<?> type) {
            super(type, false);
        }

        @Override
        public Object setUp(FixtureResolver resolver) {
            return Math.random();
        }

        @Override
        public void tearDown(Object object) {
        }
    }

    @Test
    public void choose_last_definition_by_type() {
        var stringDefinition = new MyFixtureDefinition(String.class);
        var listDefinition = new MyFixtureDefinition(List.class);
        var bucket = new FixtureDefinitionBucket(Arrays.asList(stringDefinition, listDefinition));

        var definition = bucket.findByType(String.class);
        assertSame(stringDefinition, definition);

        definition = bucket.findByType(List.class);
        assertSame(listDefinition, definition);
    }

    @Test
    public void choose_sub_type_if_possible() {
        var listDefinition = new MyFixtureDefinition(List.class);
        var arrayListDefinition = new MyFixtureDefinition(ArrayList.class);
        var bucket = new FixtureDefinitionBucket(Arrays.asList(listDefinition, arrayListDefinition));

        var definition = bucket.findByType(List.class);
        assertSame(arrayListDefinition, definition);
    }

    @Test
    public void when_no_definition_chosen_then_exception_will_be_thrown() {
        var manager = new FixtureManager(new FixtureDefinitionBucket(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> manager.setUp(String.class));
    }

    @Test
    public void lifecycle_is_reused_for_same_requested_type() {
        var listDefinition = new MyFixtureDefinition(List.class);
        var bucket = new FixtureDefinitionBucket(Collections.singletonList(listDefinition));
        var manager = new FixtureManager(bucket);

        var object1 = manager.setUp(List.class);
        var object2 = manager.setUp(List.class);
        assertSame(object1, object2);
    }

    @Test
    public void lifecycle_is_reused_for_same_definition_type() {
        var arrayListDefinition = new MyFixtureDefinition(ArrayList.class);
        var bucket = new FixtureDefinitionBucket(Collections.singletonList(arrayListDefinition));
        var manager = new FixtureManager(bucket);

        var object1 = manager.setUp(ArrayList.class);
        var object2 = manager.setUp(List.class);
        assertSame(object1, object2);
    }

    @Test
    public void set_up_type() {
        var intDefinition = new MyFixtureDefinition(int.class);
        var bucket = new FixtureDefinitionBucket(Collections.singletonList(intDefinition));
        var manager = new FixtureManager(bucket);

        assertFalse(manager.getFixtureLifecycle(int.class).isSetUp());
        manager.setUp(int.class);
        assertTrue(manager.getFixtureLifecycle(int.class).isSetUp());
    }

    @Test
    public void tear_down_all() {
        var stringDefinition = new MyFixtureDefinition(String.class);
        var bucket = new FixtureDefinitionBucket(Collections.singletonList(stringDefinition));
        var manager = new FixtureManager(bucket);

        manager.setUp(String.class);
        assertTrue(manager.getFixtureLifecycle(String.class).isSetUp());
        manager.tearDown();
        assertFalse(manager.getFixtureLifecycle(String.class).isSetUp());
    }

}
