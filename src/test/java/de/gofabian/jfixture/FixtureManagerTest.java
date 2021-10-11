package de.gofabian.jfixture;

import de.gofabian.jfixture.api.FixtureId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureManagerTest {


    private static class MyFixtureDefinition extends FixtureDefinition {
        public MyFixtureDefinition(Class<?> type) {
            super(Scope.METHOD, new FixtureId(type, null), new ArrayList<>(), false);
        }

        public MyFixtureDefinition(Class<?> type, Scope scope, List<Class<?>> dependencyTypes) {
            super(
                    scope,
                    new FixtureId(type, null),
                    dependencyTypes.stream()
                            .map(t -> new FixtureId(t, null))
                            .collect(Collectors.toList()),
                    false
            );
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

        var object1 = manager.resolve(new FixtureId(List.class, null));
        var lifecycle1 = manager.getFixtureLifecycle(listDefinition);
        var object2 = manager.resolve(new FixtureId(List.class, null));
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
        manager.resolve(new FixtureId(String.class, null));
        manager.resolve(new FixtureId(Boolean.class, null));

        manager.leave(Scope.METHOD);
        assertFalse(manager.getFixtureLifecycle(stringDefinition).isSetUp());
        assertFalse(manager.getFixtureLifecycle(booleanDefinition).isSetUp());
    }

    @Test
    public void set_up_scope_dependency_with_lower_order() {
        var childDefinition = new MyFixtureDefinition(String.class, Scope.METHOD, List.of());
        var parentDefinition = new MyFixtureDefinition(int.class, Scope.CLASS, List.of(String.class));
        var manager = new FixtureManager(new FixtureSession(), List.of(childDefinition, parentDefinition));

        assertThrows(IllegalArgumentException.class, () -> manager.resolve(new FixtureId(int.class, null)));
    }

}
