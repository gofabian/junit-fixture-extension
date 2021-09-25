package jfixture.parse;

import jfixture.FixtureDefinition;
import jfixture.FixtureDefinitionQueries;
import jfixture.api.Fixture;
import jfixture.api.FixtureContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

class FixtureDefinitionFactory {

    public List<FixtureDefinition> createFixtureDefinitions(List<FixtureMethod> methods) {
        List<FixtureDefinition> topLevelDefinitions = new ArrayList<>();
        Map<FixtureMethod, FixtureDefinition> createdDefinitions = new HashMap<>();
        List<FixtureMethod> remainingMethods = new ArrayList<>(methods);

        for (var method : methods) {
            var definition = createFixtureDefinition(method, createdDefinitions, remainingMethods);
            topLevelDefinitions.add(definition);
        }
        return topLevelDefinitions;
    }

    private FixtureDefinition createFixtureDefinition(FixtureMethod method,
                                                      Map<FixtureMethod, FixtureDefinition> createdDefinitions,
                                                      List<FixtureMethod> remainingMethods) {
        var definition = createdDefinitions.get(method);
        if (definition != null) {
            return definition;
        }

        remainingMethods.remove(method);
        List<FixtureDefinition> dependencies = getDependencies(method, createdDefinitions, remainingMethods);
        definition = createFixtureDefinition(method.getInstance(), method.getMethod(), dependencies);
        createdDefinitions.put(method, definition);
        return definition;
    }

    private List<FixtureDefinition> getDependencies(FixtureMethod method,
                                                    Map<FixtureMethod, FixtureDefinition> createdDefinitions,
                                                    List<FixtureMethod> remainingMethods) {
        List<FixtureDefinition> dependencies = new ArrayList<>(method.getMethod().getParameterCount());
        for (var type : method.getMethod().getParameterTypes()) {
            if (type != FixtureContext.class) {
                try {
                    var definition = getDependency(type, createdDefinitions, remainingMethods);
                    dependencies.add(definition);
                } catch (Exception e) {
                    // todo: reasonable log message
                    throw e;
                }
            }
        }
        return dependencies;
    }

    private FixtureDefinition getDependency(Class<?> type,
                                            Map<FixtureMethod, FixtureDefinition> createdDefinitions,
                                            List<FixtureMethod> remainingMethods) {

        for (var method : remainingMethods) {
            // todo: generalize this / from Bucket? / FixtureId(type)?
            if (type.isAssignableFrom(method.getMethod().getReturnType())) {
                return createFixtureDefinition(method, createdDefinitions, remainingMethods);
            }
        }

        var bucket = new FixtureDefinitionQueries(new ArrayList<>(createdDefinitions.values()));
        var definition = bucket.findByType(type);
        if (definition != null) {
            return definition;
        }

        throw new IllegalArgumentException("cannot resolve dependency " + type);
    }

    public FixtureDefinition createFixtureDefinition(Object instance, Method method, List<FixtureDefinition> dependencies) {
        var type = method.getReturnType();
        var parameterTypes = method.getParameterTypes();
        var autoUse = method.getAnnotation(Fixture.class).autoUse();

        return new FixtureDefinition(type, dependencies, autoUse) {
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
