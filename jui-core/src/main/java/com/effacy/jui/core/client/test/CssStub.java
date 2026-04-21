package com.effacy.jui.core.client.test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates dynamic stubs for CSS declaration interfaces used in unit tests.
 * <p>
 * String-returning no-argument methods are resolved either from explicit
 * mappings or from a naming convention.
 */
public final class CssStub {

    private CssStub() {
        // Nothing.
    }

    public static <T> T of(Class<T> type, Map<String, String> values) {
        return of(type, new LinkedHashMap<>(values)::get);
    }

    public static <T> T of(Class<T> type) {
        return prefixed(type, null);
    }

    public static <T> T prefixed(Class<T> type, String prefix) {
        return prefixed(type, prefix, Map.of());
    }

    public static <T> T prefixed(Class<T> type, String prefix, Map<String, String> overrides) {
        Map<String, String> values = new LinkedHashMap<>();
        overrides.forEach(values::put);
        return of(type, method -> {
            String value = values.get(method);
            if (value != null)
                return value;
            if ((prefix == null) || prefix.isBlank())
                return method;
            return prefix + "-" + method;
        });
    }

    private static <T> T of(Class<T> type, Function<String, String> resolver) {
        Object proxy = Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] { type },
            (instance, method, args) -> {
                String name = method.getName();

                if (method.getDeclaringClass() == Object.class) {
                    if ("toString".equals(name))
                        return type.getSimpleName() + "[stub]";
                    if ("hashCode".equals(name))
                        return System.identityHashCode(instance);
                    if ("equals".equals(name))
                        return instance == ((args == null || args.length == 0) ? null : args[0]);
                }

                if ("ensureInjected".equals(name))
                    return true;
                if ("getCssText".equals(name))
                    return "";
                if ("getCssDeclarations".equals(name))
                    return null;

                if ((method.getParameterCount() == 0) && String.class.equals(method.getReturnType()))
                    return resolver.apply(name);

                throw new IllegalStateException("No CSS stub behaviour defined for " + type.getName() + "#" + name);
            }
        );
        return type.cast(proxy);
    }
}
