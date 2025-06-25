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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * A utility class providing factory methods to create {@link Matcher} instances
 * for common use cases in dependency injection frameworks. These matchers are used
 * to filter classes, methods, or other elements based on annotations, package names,
 * class hierarchies, or other criteria.
 *
 * <p>This class is not instantiable and provides only static methods.</p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public final class Matchers {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Matchers() {
        // Not instantiable utility class
    }

    /**
     * Returns a matcher that matches any object.
     *
     * @return a matcher that always returns {@code true}
     */
    public static Matcher<Object> any() {
        return _ -> true;
    }

    /**
     * Returns a matcher that matches elements annotated with the specified annotation type.
     *
     * @param annotationType the type of annotation to match
     * @return a matcher that returns {@code true} for elements with the specified annotation
     * @throws NullPointerException if {@code annotationType} is {@code null}
     */
    public static Matcher<AnnotatedElement> annotatedWith(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException("annotationType cannot be null");
        }
        return t -> t.isAnnotationPresent(annotationType);
    }

    /**
     * Returns a matcher that matches elements annotated with the exact specified annotation instance.
     *
     * @param annotation the annotation instance to match
     * @return a matcher that returns {@code true} for elements with the exact annotation instance
     * @throws NullPointerException if {@code annotation} is {@code null}
     */
    public static Matcher<AnnotatedElement> annotatedWith(Annotation annotation) {
        if (annotation == null) {
            throw new NullPointerException("annotation cannot be null");
        }
        return t -> {
            for (var a : t.getAnnotations())
                if (a == annotation)
                    return true;
            return false;
        };
    }

    /**
     * Returns a matcher that matches classes that are subclasses of the specified superclass.
     * This includes the superclass itself.
     *
     * @param superclass the superclass to match against
     * @return a matcher that returns {@code true} for subclasses of the specified superclass
     * @throws NullPointerException if {@code superclass} is {@code null}
     */
    public static Matcher<Class<?>> subclassesOf(Class<?> superclass) {
        if (superclass == null) {
            throw new NullPointerException("superclass cannot be null");
        }
        return t -> {
            Class<?> cl = t;
            while (cl != null && cl != superclass) {
                cl = cl.getSuperclass();
            }
            return cl == superclass;
        };
    }

    /**
     * Returns a matcher that matches objects equal to the specified value using {@code equals()}.
     *
     * @param value the value to match
     * @return a matcher that returns {@code true} for objects equal to the specified value
     */
    public static Matcher<Object> only(Object value) {
        return t -> (value.equals(t));
    }

    /**
     * Returns a matcher that matches objects identical to the specified value using reference equality ({@code ==}).
     *
     * @param value the value to match
     * @return a matcher that returns {@code true} for objects identical to the specified value
     */
    public static Matcher<Object> identicalTo(Object value) {
        return t -> (t == value);
    }

    /**
     * Returns a matcher that matches classes in the exact specified package.
     * This does not include subpackages and is sensitive to the classloader.
     *
     * @param targetPackage the package to match
     * @return a matcher that returns {@code true} for classes in the specified package
     * @throws NullPointerException if {@code targetPackage} is {@code null}
     */
    public static Matcher<Class<?>> inPackage(Package targetPackage) {
        if (targetPackage == null) {
            throw new NullPointerException("targetPackage cannot be null");
        }
        return t -> t.getPackage().equals(targetPackage);
    }

    /**
     * Returns a matcher that matches classes in the specified package and its subpackages.
     * Unlike {@link #inPackage(Package)}, this matcher is classloader-agnostic and matches
     * classes based on package name prefixes.
     *
     * @param targetPackageName the name of the package (e.g., "com.example")
     * @return a matcher that returns {@code true} for classes in the specified package or its subpackages
     * @throws NullPointerException if {@code targetPackageName} is {@code null}
     */
    public static Matcher<Class<?>> inSubpackage(String targetPackageName) {
        if (targetPackageName == null) {
            throw new NullPointerException("targetPackageName cannot be null");
        }

        // Normalize package name to ensure it ends with a dot for prefix matching
        final String packagePrefix = targetPackageName.endsWith(".")
                ? targetPackageName
                : targetPackageName + ".";

        return new Matcher<Class<?>>() {
            @Override
            public boolean matches(Class<?> clazz) {
                if (clazz == null) {
                    return false;
                }
                // Get the package of the class
                Package classPackage = clazz.getPackage();
                if (classPackage == null) {
                    return false;
                }
                // Check if the class's package name starts with the target package prefix
                String packageName = classPackage.getName();
                return packageName.equals(targetPackageName) || packageName.startsWith(packagePrefix);
            }

            @Override
            public String toString() {
                return "inSubpackage(" + targetPackageName + ")";
            }
        };
    }

    /**
     * Returns a matcher that matches methods with a return type assignable to the specified type.
     * This includes methods returning the exact type or its subtypes.
     *
     * @param returnType the return type to match
     * @return a matcher that returns {@code true} for methods with a compatible return type
     * @throws NullPointerException if {@code returnType} is {@code null}
     */
    public static Matcher<Method> returns(Class<?> returnType) {
        if (returnType == null) {
            throw new NullPointerException("returnType cannot be null");
        }
        return t -> t.getReturnType().isAssignableFrom(returnType);
    }
}