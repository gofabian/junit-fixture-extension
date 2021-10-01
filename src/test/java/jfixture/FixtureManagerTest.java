package jfixture;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureManagerTest {


    private static class MyFixtureDefinition extends FixtureDefinition {
        public MyFixtureDefinition(Class<?> type) {
            super(type, new ArrayList<>(), false);
        }

        @Override
        public Object setUp(List<Object> dependencies) {
            return Math.random();
        }

        @Override
        public void tearDown(Object object) {
        }
    }

    @Test
    public void lifecycle_is_reused_for_same_definition() {
        var listDefinition = new MyFixtureDefinition(List.class);
        var manager = new FixtureManager(new FixtureSession(), List.of(listDefinition));

        var object1 = manager.resolve(List.class);
        var lifecycle1 = manager.getFixtureLifecycle(listDefinition);
        var object2 = manager.resolve(List.class);
        var lifecycle2 = manager.getFixtureLifecycle(listDefinition);
        assertSame(object1, object2);
        assertSame(lifecycle1, lifecycle2);
        assertTrue(lifecycle1.isSetUp());
    }

    @Test
    public void tear_down_all() {
        var stringDefinition = new MyFixtureDefinition(String.class);
        var booleanDefinition = new MyFixtureDefinition(Boolean.class);
        var manager = new FixtureManager(new FixtureSession(), List.of(stringDefinition, booleanDefinition));
        manager.resolve(String.class);
        manager.resolve(Boolean.class);

        manager.leave(Scope.METHOD);
        assertFalse(manager.getFixtureLifecycle(stringDefinition).isSetUp());
        assertFalse(manager.getFixtureLifecycle(booleanDefinition).isSetUp());
    }

}
