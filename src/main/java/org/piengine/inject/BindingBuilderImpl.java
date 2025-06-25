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
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Implementation of BindingBuilder for fluent binding configuration.
 *
 * @param <T> the type parameter
 */
class BindingBuilderImpl<T> implements BindingBuilder<T> {
    private final BinderImpl binder;
    private Key<T> key;
    private Class<? extends T> implementation;
    private T instance;
    private Class<? extends Annotation> scopeAnnotation;

    BindingBuilderImpl(BinderImpl binder, Key<T> key) {
        this.binder = Objects.requireNonNull(binder, "Binder cannot be null");
        this.key = Objects.requireNonNull(key, "Key cannot be null");
    }

    BindingBuilderImpl(BinderImpl binder, Class<T> type) {
        this(binder, Key.of(type));
    }

    @Override
    public BindingBuilder<T> to(Class<? extends T> implementation) {
        this.implementation = Objects.requireNonNull(implementation, "Implementation cannot be null");
        return this;
    }

    @Override
    public void toInstance(T instance) {
        this.instance = Objects.requireNonNull(instance, "Instance cannot be null");
        createBinding();
    }

    @Override
    public BindingBuilder<T> in(Class<? extends Annotation> scopeAnnotation) {
        this.scopeAnnotation = Objects.requireNonNull(scopeAnnotation, "Scope annotation cannot be null");
        return this;
    }

    @Override
    public BindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType, String value) {
        Objects.requireNonNull(annotationType, "Annotation type cannot be null");
        Objects.requireNonNull(value, "Annotation value cannot be null");
        this.key = Key.of((Class<T>) key.getType(), annotationType, value);
        return this;
    }

    private void createBinding() {
        if (binder.getBindings().containsKey(key)) {
            return; // Skip creating duplicate binding
        }

        Provider<T> provider;
        if (instance != null) {
            provider = new ProviderImpl<>(() -> instance);
        } else {
            Class<? extends T> targetType = implementation != null ? implementation : (Class<? extends T>) key.getType();
            provider = new ProviderImpl<>(() -> {
                try {
                    Constructor<?>[] constructors = targetType.getDeclaredConstructors();
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.isAnnotationPresent(Inject.class)) {
                            constructor.setAccessible(true);
                            Object[] params = binder.getInjector().getParameters(constructor.getParameterTypes(), constructor.getParameterAnnotations());
                            T instance = (T) constructor.newInstance(params);
                            binder.getInjector().injectMembers(instance);
                            return instance;
                        }
                    }
                    Constructor<?> constructor = targetType.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    T instance = (T) constructor.newInstance();
                    binder.getInjector().injectMembers(instance);
                    return instance;
                } catch (Exception e) {
                    throw new InjectionException("Failed to instantiate " + targetType, e);
                }
            });
        }

        if (scopeAnnotation != null) {
            Scope scope = binder.getInjector().getScope(scopeAnnotation);
            provider = scope.scope(provider);
        }

        Binding<T> binding = new BindingImpl<>(key, provider);
        binder.addBinding(binding);
    }

    public void finalizeBinding() {
        createBinding();
    }
}