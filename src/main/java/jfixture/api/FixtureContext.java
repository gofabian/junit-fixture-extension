package jfixture.api;

import java.util.ArrayList;
import java.util.List;

public class FixtureContext {
    private final List<Runnable> tearDowns = new ArrayList<>();

    public void addTearDown(Runnable tearDown) {
        tearDowns.add(tearDown);
    }

    public List<Runnable> getTearDowns() {
        return tearDowns;
    }

}
