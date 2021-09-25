package jfixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FixtureSession {

    private final Map<FixtureDefinition, FixtureLifecycle> definitionLifecycleMap = new HashMap<>();
    private final List<FixtureLifecycle> setUpLifecycles = new ArrayList<>();

    public Object setUp(FixtureDefinition definition) {
        var lifecycle = getFixtureLifecycle(definition);
        if (lifecycle.isSetUp()) {
            return lifecycle.getObject();
        }

        var dependencies = definition.getDependencies().stream()
                .map(this::setUp).collect(Collectors.toList());
        var object = lifecycle.setUp(dependencies);

        setUpLifecycles.add(lifecycle);
        return object;
    }

    public void tearDown() {
        var it = setUpLifecycles.listIterator(setUpLifecycles.size());
        while (it.hasPrevious()) {
            it.previous().tearDown();
            it.remove();
        }
    }

    public FixtureLifecycle getFixtureLifecycle(FixtureDefinition definition) {
        return definitionLifecycleMap.computeIfAbsent(definition, d -> new FixtureLifecycle(definition));
    }

}
