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

package org.piengine.util.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for parsing annotations from objects, classes, or interfaces.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public final class Annotations {

	public static <T extends Annotation> T parseAnnotatedType(Class<T> annotatioNClass, AnnotatedType target) {
		return target.getAnnotation(annotatioNClass);
	}

	/**
	 * Parses an annotation from a class, checking the class itself, its superclass
	 * hierarchy, and implemented interfaces.
	 *
	 * @param <T>             the type of the annotation
	 * @param annotationClass the class of the annotation to search for
	 * @param target          the class to search
	 * @return the annotation if found, or null if not found or target is null
	 */
	public static <T extends Annotation> T parseClass(Class<T> annotationClass, Class<?> target) {
		if (target == null || target == Object.class)
			return null;

		if (target.isAnnotationPresent(annotationClass))
			return target.getAnnotation(annotationClass);

		T result = parseClass(annotationClass, target.getSuperclass());
		if (result != null)
			return result;

		return parseInterfaces(annotationClass, target.getInterfaces());
	}

	/**
	 * Parses an annotation from an array of classes (typically interfaces),
	 * checking each class and its parent interfaces.
	 *
	 * @param <T>             the type of the annotation
	 * @param annotationClass the class of the annotation to search for
	 * @param target          the array of classes to search
	 * @return the annotation if found, or null if not found
	 */
	public static <T extends Annotation> T parseInterfaces(Class<T> annotationClass, Class<?>... target) {
		return parseInterfaces(annotationClass, new HashSet<>(), target);
	}

	/**
	 * Internal method to parse annotations from an array of classes, tracking
	 * visited classes to avoid redundant processing.
	 *
	 * @param <T>             the type of the annotation
	 * @param annotationClass the class of the annotation to search for
	 * @param visited         the set of classes already processed
	 * @param target          the array of classes to search
	 * @return the annotation if found, or null if not found
	 */
	private static <T extends Annotation> T parseInterfaces(Class<T> annotationClass, Set<Class<?>> visited,
			Class<?>... target) {
		for (var i : target) {
			if (i == null || !visited.add(i))
				continue;

			if (i.isAnnotationPresent(annotationClass))
				return i.getAnnotation(annotationClass);

			T result = parseInterfaces(annotationClass, visited, i.getInterfaces());
			if (result != null)
				return result;
		}
		return null;
	}

	public static <T extends Annotation> T parseMethod(Class<T> annotationClass, Class<?> cl, String methodName,
			Class<?>... parameterTypes) {
		Method method;
		try {
			method = cl.getDeclaredMethod(methodName, parameterTypes);

			return method.getAnnotation(annotationClass);

		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * Parses an annotation of the specified type from the target object, class, or
	 * array of classes. Recursively searches the class hierarchy and implemented
	 * interfaces.
	 *
	 * @param <T>             the type of the annotation
	 * @param annotationClass the class of the annotation to search for
	 * @param target          the object, class, or array of classes to search
	 * @return the annotation if found, or null if not found or target is null
	 */
	public static <T extends Annotation> T parseObject(Class<T> annotationClass, Object target) {
		if (target == null)
			return null;

		if (target instanceof Class<?>[] interfaces)
			return parseInterfaces(annotationClass, interfaces);

		if (target instanceof Class<?> cl)
			return parseClass(annotationClass, cl);

		return parseClass(annotationClass, target.getClass());
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Annotations() {
		// Utility class, not instantiable
	}
}