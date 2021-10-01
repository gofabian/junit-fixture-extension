package jfixture.parse;

import jfixture.FixtureDefinition;
import jfixture.api.Fixture;
import jfixture.api.FixtureContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class FixtureDefinitionFactory {

    public List<FixtureDefinition> createFixtureDefinitions(List<FixtureMethod> methods) {
        return methods.stream()
                .map(m -> createFixtureDefinition(m.getInstance(), m.getMethod()))
                .collect(Collectors.toList());
    }

    public FixtureDefinition createFixtureDefinition(Object instance, Method method) {
        var type = method.getReturnType();
        var parameterTypes = method.getParameterTypes();
        List<Class<?>> dependencyTypes = Arrays.stream(parameterTypes)
                .filter(t -> t != FixtureContext.class)
                .collect(Collectors.toList());
        var autoUse = method.getAnnotation(Fixture.class).autoUse();

        return new FixtureDefinition(type, dependencyTypes, autoUse) {
            private FixtureContext context;

            @Override
            public Object setUp(List<Object> dependencies) {
                context = new FixtureContext();

                var dependencyIterator = dependencies.iterator();
                var args = Arrays.stream(parameterTypes)
                        .map(type -> {
                            if (type == FixtureContext.class) {
                                return context;
                            }
                            if (dependencyIterator.hasNext()) {
                                return dependencyIterator.next();
                            }
                            throw new IllegalArgumentException("missing parameter!");
                        }).toArray();

                try {
                    return method.invoke(instance, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void tearDown(Object object) {
                for (var tearDown : context.getTearDowns()) {
                    tearDown.run();
                }
            }
        };
    }


}
