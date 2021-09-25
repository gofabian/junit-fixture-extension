package jfixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixtureManager {

    private final List<FixtureDefinition> definitions;
    private final Map<Class<?>, FixtureLifecycle> typeLifecycleMap = new LinkedHashMap<>();
    private final FixtureResolver resolver = new FixtureResolver(this);

    public FixtureManager(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public Object setUp(Class<?> type) {
        var lifecycle = getFixtureLifecycle(type);
        var object = lifecycle.setUp(resolver);
        typeLifecycleMap.put(lifecycle.getDefinition().getType(), lifecycle);
        return object;
    }

    public void tearDown() {
        var lifecycles = new ArrayList<>(typeLifecycleMap.values());
        var it = lifecycles.listIterator(lifecycles.size());
        while (it.hasPrevious()) {
            it.previous().tearDown();
        }
    }

    FixtureLifecycle getFixtureLifecycle(Class<?> type) {
        var definition = findFixtureDefinition(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        var lifecycle = typeLifecycleMap.get(definition.getType());
        if (lifecycle != null) {
            return lifecycle;
        }
        return new FixtureLifecycle(definition);
    }

    public FixtureDefinition findFixtureDefinition(Class<?> type) {
        var it = definitions.listIterator(definitions.size());
        while (it.hasPrevious()) {
            var definition = it.previous();
            if (type.isAssignableFrom(definition.getType())) {
                return definition;
            }
        }
        return null;
    }

}
