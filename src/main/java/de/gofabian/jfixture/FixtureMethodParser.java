package de.gofabian.jfixture;

import de.gofabian.jfixture.api.Fixture;
import de.gofabian.jfixture.api.FixtureContext;
import de.gofabian.jfixture.api.FixtureId;
import de.gofabian.jfixture.api.LoadFixtures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class FixtureMethodParser {

    private final Map<Object, List<FixtureDefinition>> cache = new HashMap<>();

    public List<FixtureDefinition> parseClass(Class<?> clazz) {
        var instance = createInstance(clazz);
        return parseInstance(instance);
    }

    public List<FixtureDefinition> parseInstance(Object instance) {
        {
            var definitions = cache.get(instance.getClass());
            if (definitions != null) {
                return definitions;
            }
        }

        var definitions = new ArrayList<FixtureDefinition>();

        for (var externalClass : collectExternalClasses(instance.getClass())) {
            var plus = parseClass(externalClass);
            definitions.addAll(plus);
        }

        for (var method : collectMethodsFromClass(instance.getClass())) {
            var plus = createFixtureDefinition(instance, method);
            definitions.add(plus);
        }

        cache.put(instance.getClass(), definitions);
        return definitions;
    }

    private List<Class<?>> collectExternalClasses(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotationsByType(LoadFixtures.class))
                .map(LoadFixtures::value).collect(Collectors.toList());
    }

    private List<Method> collectMethodsFromClass(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getAnnotation(Fixture.class) != null)
                .collect(Collectors.toList());
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("cannot create instance of fixture container " + clazz, e);
        }
    }

    public FixtureDefinition createFixtureDefinition(Object instance, Method method) {
        var fixtureId = new FixtureId(method.getReturnType(), method.getName());
        List<FixtureId> dependencyIds = Arrays.stream(method.getParameters())
                .filter(p -> p.getType() != FixtureContext.class)
                .map(p -> new FixtureId(p.getType(), p.getName()))
                .collect(Collectors.toList());
        var annotation = method.getAnnotation(Fixture.class);
        var scope = annotation.scope();
        var autoUse = annotation.autoUse();
        var parameterTypes = method.getParameterTypes();

        return new FixtureDefinition(scope, fixtureId, dependencyIds, autoUse) {
            private FixtureContext context;

            @Override
            public Object setUp(List<Object> dependencies) {
                context = new FixtureContext();

                var dependencyIterator = dependencies.iterator();
                var args = Arrays.stream(parameterTypes)
                        .map(t -> {
                            if (t == FixtureContext.class) {
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
