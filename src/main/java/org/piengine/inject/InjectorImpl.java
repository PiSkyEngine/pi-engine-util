/*
 * MIT License
 *
 * Copyright (c) 2025 Sly Technologies Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.piengine.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Implementation of the Injector interface, managing bindings and injections.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
class InjectorImpl implements Injector {

    private final InjectorImpl parent;
    private final Map<Key<?>, Binding<?>> bindings = new HashMap<>();
    private final Map<Class<? extends Annotation>, Scope> scopes = new HashMap<>();

    InjectorImpl(InjectorImpl parent, Module... modules) {
        this.parent = parent;
        this.scopes.put(Singleton.class, new SingletonScope());
        configureBindings(modules);
    }

    void addBinding(Binding<?> binding) {
        bindings.put(binding.getKey(), binding);
    }

    void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        scopes.put(annotationType, scope);
    }

    private void configureBindings(Module[] modules) {
        BinderImpl binder = new BinderImpl(this);
        for (Module module : modules) {
            module.configure(binder);
        }
        binder.finalizeBindings();
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        return new InjectorImpl(this, modules);
    }

    @Override
    public <A extends Annotation, T> Injector createExternalInjector(
            Class<A> injectAnnotation, Function<Key<T>, T> injector) {
        return new ExternalInjectorImpl<>(this, injectAnnotation, injector);
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {
        return getBinding(Key.of(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(Key<T> key) {
        Binding<T> binding = (Binding<T>) bindings.get(key);
        return binding;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return getInstance(Key.of(type));
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        Binding<T> binding = getBinding(key);
        if (binding == null && parent != null) {
            T instance = parent.getInstance(key);
            return instance;
        }
        if (binding == null) {
            Class<T> type = (Class<T>) key.getType();
            if (type.isInterface()) {
                throw new InjectionException("No binding found for interface key: " + key);
            }
            // Handle primitive types by mapping to wrapper classes
            Class<?> bindingType = type.isPrimitive() ? primitiveToWrapper(type) : type;
            if (bindingType != type) {
                Binding<?> wrapperBinding = bindings.get(Key.of(bindingType));
                if (wrapperBinding != null) {
                    Object value = wrapperBinding.getProvider().get();
                    T instance = convertToPrimitive(type, value);
                    return instance;
                }
            }
            // Do not instantiate types automatically unless explicitly bound
            throw new InjectionException("No binding found for key: " + key);
        }
        T instance = binding.getProvider().get();
        return instance;
    }

    private Class<?> primitiveToWrapper(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == char.class) return Character.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        return type;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToPrimitive(Class<T> type, Object value) {
        if (value == null) return null;
        if (type == int.class && value instanceof Integer) return (T) value;
        if (type == long.class && value instanceof Long) return (T) value;
        if (type == double.class && value instanceof Double) return (T) value;
        if (type == float.class && value instanceof Float) return (T) value;
        if (type == boolean.class && value instanceof Boolean) return (T) value;
        if (type == char.class && value instanceof Character) return (T) value;
        if (type == byte.class && value instanceof Byte) return (T) value;
        if (type == short.class && value instanceof Short) return (T) value;
        throw new InjectionException("Cannot convert value " + value + " to primitive type " + type);
    }

    Object[] getParameters(Class<?>[] paramTypes, Annotation[][] paramAnnotations) {
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == null) continue;
            Annotation namedAnnotation = null;
            boolean optional = false;
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == Named.class) {
                    namedAnnotation = annotation;
                } else if (annotation.annotationType() == Inject.class) {
                    optional = ((Inject) annotation).optional();
                }
            }
            Object param;
            try {
                if (namedAnnotation != null) {
                    String value = ((Named) namedAnnotation).value();
                    param = getInstance(Key.of(paramTypes[i], Named.class, value));
                } else {
                    param = getInstance(paramTypes[i]);
                }
            } catch (InjectionException e) {
                if (!optional) {
                    throw e;
                }
                param = null;
            }
            params[i] = param;
        }
        return params;
    }

    @Override
    public Object[] getParameters(Class<?>... paramTypes) {
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == null) continue;
            params[i] = getInstance(paramTypes[i]);
        }
        return params;
    }

    @Override
    public Object[] getParameters(Method method) {
        boolean methodOptional = method.isAnnotationPresent(Inject.class) && method.getAnnotation(Inject.class).optional();
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == null) continue;
            Annotation namedAnnotation = null;
            boolean optional = methodOptional;
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == Named.class) {
                    namedAnnotation = annotation;
                } else if (annotation.annotationType() == Inject.class) {
                    optional = ((Inject) annotation).optional();
                }
            }
            Object param;
            try {
                if (namedAnnotation != null) {
                    String value = ((Named) namedAnnotation).value();
                    param = getInstance(Key.of(paramTypes[i], Named.class, value));
                } else {
                    param = getInstance(paramTypes[i]);
                }
            } catch (InjectionException e) {
                if (!optional) {
                    throw e;
                }
                param = null;
            }
            params[i] = param;
        }
        return params;
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return getProvider(Key.of(type));
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        Binding<T> binding = getBinding(key);
        if (binding == null && parent != null) {
            return parent.getProvider(key);
        }
        if (binding == null) {
            throw new InjectionException("No provider found for key: " + key);
        }
        return binding.getProvider();
    }

    Scope getScope(Class<? extends Annotation> annotationType) {
        return scopes.getOrDefault(annotationType, new DefaultScope());
    }

    @Override
    public void injectMembers(Object instance) {
        if (instance == null) return;
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Inject inject = field.getAnnotation(Inject.class);
                field.setAccessible(true);
                try {
                    Object value;
                    if (field.isAnnotationPresent(Named.class)) {
                        String valueName = field.getAnnotation(Named.class).value();
                        value = getInstance(Key.of((Class<?>) field.getType(), Named.class, valueName));
                    } else {
                        value = getInstance(field.getType());
                    }
                    if (value != null && (!(value instanceof String) || !((String) value).isEmpty())) {
                        field.set(instance, value);
                    } else if (!inject.optional()) {
                        throw new InjectionException("No binding found for non-optional field: " + field);
                    }
                } catch (IllegalAccessException e) {
                    if (!inject.optional()) {
                        throw new InjectionException("Failed to inject field: " + field, e);
                    }
                } catch (InjectionException e) {
                    if (!inject.optional()) {
                        throw e;
                    }
                }
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class)) {
                Inject inject = method.getAnnotation(Inject.class);
                method.setAccessible(true);
                try {
                    Object[] params = getParameters(method);
                    method.invoke(instance, params);
                } catch (Exception e) {
                    if (!inject.optional()) {
                        throw new InjectionException("Failed to inject method: " + method, e);
                    }
                }
            }
        }
    }

    @Override
    public void injectStaticMembers(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class) && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                Inject inject = field.getAnnotation(Inject.class);
                field.setAccessible(true);
                try {
                    Object value;
                    if (field.isAnnotationPresent(Named.class)) {
                        String valueName = field.getAnnotation(Named.class).value();
                        value = getInstance(Key.of((Class<?>) field.getType(), Named.class, valueName));
                    } else {
                        value = getInstance(field.getType());
                    }
                    if (value != null && (!(value instanceof String) || !((String) value).isEmpty())) {
                        field.set(null, value);
                    } else if (!inject.optional()) {
                        throw new InjectionException("No binding found for non-optional static field: " + field);
                    }
                } catch (IllegalAccessException e) {
                    if (!inject.optional()) {
                        throw new InjectionException("Failed to inject static field: " + field, e);
                    }
                } catch (InjectionException e) {
                    if (!inject.optional()) {
                        throw e;
                    }
                }
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class) && java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                Inject inject = method.getAnnotation(Inject.class);
                method.setAccessible(true);
                try {
                    Object[] params = getParameters(method);
                    method.invoke(null, params);
                } catch (Exception e) {
                    if (!inject.optional()) {
                        throw new InjectionException("Failed to inject static method: " + method, e);
                    }
                }
            }
        }
    }
}