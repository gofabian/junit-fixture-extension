package de.gofabian.jfixture;

import de.gofabian.jfixture.api.FixtureId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FixtureDefinitionQueries {

    private final List<FixtureDefinition> definitions;

    public FixtureDefinitionQueries(List<FixtureDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<FixtureDefinition> filterBy(Predicate<FixtureDefinition> predicate) {
        return definitions.stream().filter(predicate).collect(Collectors.toList());
    }

    public FixtureDefinition findById(FixtureId id) {
        FixtureDefinition candidateWithOtherName = null;
        var it = definitions.listIterator(definitions.size());
        while (it.hasPrevious()) {
            var definition = it.previous();
            if (supportsType(definition, id.getType())) {
                var expectedName = definition.getId().getName();
                var givenName = id.getName();
                if (expectedName != null && expectedName.equals(givenName)) {
                    return definition;
                }
                if (candidateWithOtherName == null) {
                    candidateWithOtherName = definition;
                }
            }
        }
        return candidateWithOtherName;
    }

    private static boolean supportsType(FixtureDefinition definition, Class<?> type) {
        return type.isAssignableFrom(definition.getId().getType());
    }

}
