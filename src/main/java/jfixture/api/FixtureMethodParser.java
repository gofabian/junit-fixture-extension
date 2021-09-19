package jfixture.api;

import jfixture.FixtureDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FixtureMethodParser {

    public List<FixtureDefinition> parseFixtureDefinitions(Object testInstance) {
        var definitions = new ArrayList<FixtureDefinition>();
        for (var method : testInstance.getClass().getMethods()) {
            var annotation = method.getAnnotation(Fixture.class);
            if (annotation != null) {
                var definition = createFixtureDefinition(testInstance, method);
                definitions.add(definition);
            }
        }
        return definitions;
    }

    public FixtureDefinition createFixtureDefinition(Object testInstance, Method method) {
        // todo: validate method
        return new FixtureDefinition(method.getReturnType()) {
            private FixtureContext context;

            @Override
            public Object setUp() {
                context = new FixtureContext();
                try {
                    var args = Arrays.stream(method.getParameterTypes())
                            .map(t -> {
                                return context;
                            }).toArray();

                    return method.invoke(testInstance, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void tearDown(Object object) {
                var tearDowns = context.getTearDowns();
                var it = tearDowns.listIterator(tearDowns.size());
                while (it.hasPrevious()) {
                    it.previous().run();
                }
            }
        };
    }

}
