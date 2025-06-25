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

package org.piengine.inject.matcher;

/**
 * A type-safe matcher for filtering objects based on specific criteria.
 * Implementations of this interface define conditions to determine whether an
 * object of type {@code T} matches the specified criteria. This is commonly used
 * in dependency injection frameworks to filter classes, methods, or other elements
 * for binding or interception purposes.
 *
 * @param <T> the type of object to match
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Matcher<T> {

    /**
     * Combines this matcher with another matcher using a logical AND operation.
     * The resulting matcher returns {@code true} only if both this matcher and the
     * provided matcher return {@code true} for the same input.
     *
     * @param other the other matcher to combine with this matcher
     * @return a new matcher that represents the logical AND of this matcher and the other
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Matcher<T> and(Matcher<? super T> other) {
        if (other == null) {
            throw new NullPointerException("other matcher cannot be null");
        }
        return t -> matches(t) && other.matches(t);
    }

    /**
     * Combines this matcher with another matcher using a logical OR operation.
     * The resulting matcher returns {@code true} if either this matcher or the
     * provided matcher returns {@code true} for the same input.
     *
     * @param other the other matcher to combine with this matcher
     * @return a new matcher that represents the logical OR of this matcher and the other
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Matcher<T> or(Matcher<? super T> other) {
        if (other == null) {
            throw new NullPointerException("other matcher cannot be null");
        }
        return t -> matches(t) || other.matches(t);
    }

    /**
     * Determines whether the given object matches the criteria defined by this matcher.
     *
     * @param t the object to evaluate
     * @return {@code true} if the object matches the criteria, {@code false} otherwise
     */
    boolean matches(T t);
}