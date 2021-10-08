package de.gofabian.jfixture;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.function.Consumer;

public class ExtensionContextUtil {

    public static void registerAfterSessionCallback(ExtensionContext context, Consumer<ExtensionContext> callback) {
        if (isOuterClassContext(context)) {
            var rootContext = context.getRoot();
            getStore(rootContext).getOrComputeIfAbsent("afterSessionCallback", k ->
                    (ExtensionContext.Store.CloseableResource) () -> callback.accept(rootContext)
            );
        }
    }

    public static boolean isOuterClassContext(ExtensionContext context) {
        return !context.getRequiredTestClass().isMemberClass();
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        var namespace = ExtensionContext.Namespace.create(ExtensionContextUtil.class);
        return context.getStore(namespace);
    }

}
