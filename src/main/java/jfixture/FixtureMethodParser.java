package jfixture;

import jfixture.api.Fixture;
import jfixture.api.FixtureContext;
import jfixture.api.LoadFixtures;

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
