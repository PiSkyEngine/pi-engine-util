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
import java.util.Objects;

import javax.inject.Named;

/**
 * Implementation of Key for identifying dependencies.
 *
 * @param <T> the type parameter
 */
class KeyImpl<T> implements Key<T> {
    private final Class<T> type;
    private final Annotation annotation;

    KeyImpl(Class<T> type, Annotation annotation) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.annotation = annotation;
    }

    @Override
    public Object getType() {
        return type;
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key<?> key = (Key<?>) o;
        if (!type.equals(key.getType())) return false;
        if (annotation == null && key.getAnnotation() == null) return true;
        if (annotation == null || key.getAnnotation() == null) return false;
        if (annotation instanceof Named && key.getAnnotation() instanceof Named) {
            return ((Named) annotation).value().equals(((Named) key.getAnnotation()).value());
        }
        return annotation.equals(key.getAnnotation());
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        if (annotation instanceof Named) {
            result = 31 * result + ((Named) annotation).value().hashCode();
        } else if (annotation != null) {
            result = 31 * result + annotation.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "Key[type=" + type.getName() + ", annotation=" + (annotation != null ? annotation : "null") + "]";
    }
}