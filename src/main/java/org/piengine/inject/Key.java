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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.inject.Named;

/**
 * Represents a type-safe key for identifying dependencies in the dependency
 * injection framework. A key consists of a type (class or generic type) and an
 * optional annotation to distinguish between different bindings of the same type.
 *
 * @param <T> the type of the dependency
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Key<T> {

    /**
     * Returns the type of the dependency.
     *
     * @return the Class or TypeLiteral representing the dependency type
     */
    Object getType();

    /**
     * Returns the annotation associated with this key, if any.
     *
     * @return the annotation, or null if none is present
     */
    Annotation getAnnotation();

    /**
     * Creates a new Key for the specified type.
     *
     * @param type the class of the dependency
     * @param <T> the type of the dependency
     * @return a new Key instance
     */
    static <T> Key<T> of(Class<T> type) {
        return new KeyImpl<>(type, null);
    }

    /**
     * Creates a new Key for the specified type and annotation.
     *
     * @param type the class of the dependency
     * @param annotation the annotation to distinguish the binding
     * @param <T> the type of the dependency
     * @return a new Key instance
     */
    static <T> Key<T> of(Class<T> type, Annotation annotation) {
        return new KeyImpl<>(type, annotation);
    }

    /**
     * Creates a new Key for the specified type and annotation type with a value.
     *
     * @param type the class of the dependency
     * @param annotationType the class of the annotation
     * @param annotationValue the value of the annotation
     * @param <T> the type of the dependency
     * @return a new Key instance
     */
    static <T> Key<T> of(Class<T> type, Class<? extends Annotation> annotationType, String annotationValue) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(annotationType, "Annotation type cannot be null");
        Objects.requireNonNull(annotationValue, "Annotation value cannot be null");

        if (annotationType == Named.class) {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getName().equals("value")) return annotationValue;
                    if (method.getName().equals("annotationType")) return Named.class;
                    if (method.getName().equals("equals")) {
                        Object other = args[0];
                        if (other instanceof Named) {
                            return annotationValue.equals(((Named) other).value());
                        }
                        return false;
                    }
                    if (method.getName().equals("hashCode")) {
                        return 127 * "value".hashCode() ^ annotationValue.hashCode();
                    }
                    if (method.getName().equals("toString")) {
                        return "@" + Named.class.getName() + "(value=" + annotationValue + ")";
                    }
                    return null;
                }
            };
            Named named = (Named) Proxy.newProxyInstance(
                Named.class.getClassLoader(),
                new Class<?>[]{Named.class},
                handler
            );
            return new KeyImpl<>(type, named);
        }
        throw new IllegalArgumentException("Unsupported annotation type: " + annotationType);
    }
}