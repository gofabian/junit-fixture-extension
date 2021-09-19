package jfixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixtureManager {

    private final List<FixtureDefinition> definitions;
    private final Map<Class<?>, FixtureLifecycle> lifecycles = new LinkedHashMap<>();

    public FixtureManager(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public FixtureLifecycle getFixtureLifecycle(Class<?> type) {
        var definition = findFixtureDefinition(type);
        if (definition == null) {
            throw new IllegalArgumentException("no fixture found for type " + type);
        }
        return lifecycles.computeIfAbsent(definition.getType(), k -> new FixtureLifecycle(definition));
    }

    public List<FixtureLifecycle> getFixtureLifecycles() {
        return new ArrayList<>(lifecycles.values());
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
