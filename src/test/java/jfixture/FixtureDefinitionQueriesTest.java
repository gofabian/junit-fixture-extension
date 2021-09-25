package jfixture;

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

        var filtered = queries.filterBy(d -> d.getType() == String.class);
        assertEquals(Collections.singletonList(stringDefinition), filtered);
    }

    @Test
    public void findBySameType() {
        var stringDefinition = new FixtureDefinitionImpl(String.class);
        var booleanDefinition = new FixtureDefinitionImpl(boolean.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(stringDefinition, booleanDefinition));

        assertSame(booleanDefinition, queries.findByType(boolean.class));
    }

    @Test
    public void findByLastType() {
        var stringDefinition1 = new FixtureDefinitionImpl(String.class);
        var stringDefinition2 = new FixtureDefinitionImpl(String.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(stringDefinition1, stringDefinition2));

        assertSame(stringDefinition2, queries.findByType(String.class));
    }

    @Test
    public void findByUnknownType() {
        var queries = new FixtureDefinitionQueries(Collections.emptyList());

        assertNull(queries.findByType(int.class));
    }

    @Test
    public void findBySuperType() {
        var listDefinition = new FixtureDefinitionImpl(List.class);
        var arrayListDefinition = new FixtureDefinitionImpl(ArrayList.class);
        var queries = new FixtureDefinitionQueries(Arrays.asList(listDefinition, arrayListDefinition));

        assertSame(arrayListDefinition, queries.findByType(List.class));
    }


    public static class FixtureDefinitionImpl extends FixtureDefinition {
        protected FixtureDefinitionImpl(Class<?> type) {
            super(type, Collections.emptyList(), false);
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
