package de.gofabian.jfixture;

import de.gofabian.jfixture.api.FixtureId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureDefinitionQueriesTest {

    @Test
    public void filter() {
        var stringDefinition = new FixtureDefinitionImpl(String.class);
        var booleanDefinition = new FixtureDefinitionImpl(boolean.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(stringDefinition, booleanDefinition));

        var filtered = queries.filterBy(d -> d.getId().getType() == String.class);
        assertEquals(Collections.singletonList(stringDefinition), filtered);
    }

    @Test
    public void findBySameType() {
        var stringDefinition = new FixtureDefinitionImpl(String.class);
        var booleanDefinition = new FixtureDefinitionImpl(boolean.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(stringDefinition, booleanDefinition));

        assertSame(booleanDefinition, queries.findById(new FixtureId(boolean.class, null)));
    }

    @Test
    public void findByLastType() {
        var stringDefinition1 = new FixtureDefinitionImpl(String.class);
        var stringDefinition2 = new FixtureDefinitionImpl(String.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(stringDefinition1, stringDefinition2));

        assertSame(stringDefinition2, queries.findById(new FixtureId(String.class, null)));
    }

    @Test
    public void findByUnknownType() {
        var queries = new FixtureDefinitionQueries(Collections.emptyList());

        assertNull(queries.findById(new FixtureId(int.class, null)));
    }

    @Test
    public void findBySuperType() {
        var listDefinition = new FixtureDefinitionImpl(List.class);
        var arrayListDefinition = new FixtureDefinitionImpl(ArrayList.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(listDefinition, arrayListDefinition));

        assertSame(arrayListDefinition, queries.findById(new FixtureId(List.class, null)));
    }


    public static class FixtureDefinitionImpl extends FixtureDefinition {
        protected FixtureDefinitionImpl(Class<?> type) {
            super(Scope.METHOD, new FixtureId(type, null), Collections.emptyList(), false);
        }

        @Override
        public Object setUp(List<Object> dependencies) {
            return null;
        }

        @Override
        public void tearDown(Object object) {
        }
    }

}
