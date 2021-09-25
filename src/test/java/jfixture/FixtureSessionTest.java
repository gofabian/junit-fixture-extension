package jfixture;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureSessionTest {


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
        var session = new FixtureSession();

        var object1 = session.setUp(listDefinition);
        var lifecycle1 = session.getFixtureLifecycle(listDefinition);
        var object2 = session.setUp(listDefinition);
        var lifecycle2 = session.getFixtureLifecycle(listDefinition);
        assertSame(object1, object2);
        assertSame(lifecycle1, lifecycle2);
        assertTrue(lifecycle1.isSetUp());
    }

    @Test
    public void tear_down_all() {
        var stringDefinition = new MyFixtureDefinition(String.class);
        var booleanDefinition = new MyFixtureDefinition(Boolean.class);
        var session = new FixtureSession();
        session.setUp(stringDefinition);
        session.setUp(booleanDefinition);

        session.tearDown();
        assertFalse(session.getFixtureLifecycle(stringDefinition).isSetUp());
        assertFalse(session.getFixtureLifecycle(booleanDefinition).isSetUp());
    }

}
