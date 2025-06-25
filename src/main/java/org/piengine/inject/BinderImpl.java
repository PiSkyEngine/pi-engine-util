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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of Binder for configuring bindings.
 */
class BinderImpl implements Binder {
    private final InjectorImpl injector;
    private final Map<Key<?>, Binding<?>> bindings = new HashMap<>();
    private final List<BindingBuilderImpl<?>> builders = new ArrayList<>();

    BinderImpl(InjectorImpl injector) {
        this.injector = Objects.requireNonNull(injector, "Injector cannot be null");
    }

    @Override
    public <T> BindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        Key<T> key = Key.of((Class<T>) typeLiteral.getClass());
        BindingBuilderImpl<T> builder = new BindingBuilderImpl<>(this, key);
        builders.add(builder);
        return builder;
    }

    @Override
    public <T> BindingBuilder<T> bind(Key<T> key) {
        BindingBuilderImpl<T> builder = new BindingBuilderImpl<>(this, key);
        builders.add(builder);
        return builder;
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> type) {
        BindingBuilderImpl<T> builder = new BindingBuilderImpl<>(this, type);
        builders.add(builder);
        return builder;
    }

    @Override
    public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        injector.bindScope(annotationType, scope);
    }

    @Override
    public void install(Module module) {
        module.configure(this);
    }

    void addBinding(Binding<?> binding) {
        if (!bindings.containsKey(binding.getKey())) {
            bindings.put(binding.getKey(), binding);
            injector.addBinding(binding);
        }
    }

    InjectorImpl getInjector() {
        return injector;
    }

    Map<Key<?>, Binding<?>> getBindings() {
        return bindings;
    }

    Scope getScope(Class<? extends Annotation> scopeAnnotation) {
        return injector.getScope(scopeAnnotation);
    }

    void finalizeBindings() {
        builders.forEach(BindingBuilderImpl::finalizeBinding);
        builders.clear();
    }
}