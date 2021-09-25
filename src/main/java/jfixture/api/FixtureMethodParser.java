package jfixture.api;

import jfixture.FixtureDefinition;
import jfixture.FixtureResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FixtureMethodParser {

    public List<FixtureDefinition> parseFixtureDefinitions(Object testInstance) {
        var definitions = new ArrayList<FixtureDefinition>();

        parseContainerInstance(testInstance, definitions);

        var annotations = testInstance.getClass().getAnnotationsByType(LoadFixtures.class);
        for (var annotation : annotations) {
            parseContainerClass(annotation.value(), definitions);
        }

        return definitions;
    }

    private void parseContainerClass(Class<?> containerClass, List<FixtureDefinition> definitions) {
        try {
            var containerInstance = containerClass.getDeclaredConstructor().newInstance();
            parseContainerInstance(containerInstance, definitions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("cannot create instance of fixture container", e);
        }
    }

    private void parseContainerInstance(Object testInstance, List<FixtureDefinition> definitions) {
        for (var method : testInstance.getClass().getMethods()) {
            var annotation = method.getAnnotation(Fixture.class);
            if (annotation != null) {
                var definition = createFixtureDefinition(testInstance, method);
                definitions.add(definition);
            }
        }
    }

    public FixtureDefinition createFixtureDefinition(Object testInstance, Method method) {
        // todo: validate method
        return new FixtureDefinition(method.getReturnType()) {
            private FixtureContext context;

            @Override
            public Object setUp(FixtureResolver resolver) {
                context = new FixtureContext();
                try {
                    var args = Arrays.stream(method.getParameterTypes())
                            .map(type -> {
                                if (type == FixtureContext.class) {
                                    return context;
                                }
                                return resolver.resolve(type);
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
