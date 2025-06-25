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
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class Methods.
 */
public final class Methods {

	/**
	 * Checks if is non static.
	 *
	 * @param target the target
	 * @return true, if is non static
	 */
	public static boolean isNonStatic(Executable target) {
		if (target == null) {
			throw new NullPointerException("target cannot be null");
		}
		return !Modifier.isStatic(target.getModifiers());
	}

	/**
	 * Checks if is static.
	 *
	 * @param target the target
	 * @return true, if is static
	 */
	public static boolean isStatic(Executable target) {
		if (target == null) {
			throw new NullPointerException("target cannot be null");
		}
		return Modifier.isStatic(target.getModifiers());
	}

	/**
	 * New default constructor instance.
	 *
	 * @param cl the cl
	 * @return the object
	 */
	public static <T> T newDefaultConstructorInstance(Class<T> cl) {
		try {
			var c = cl.getConstructor();

			return c.newInstance();
		} catch (NoSuchMethodException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
	}

	/**
	 * Parses the class.
	 *
	 * @param annotationClass the annotation class
	 * @param target          the target
	 * @return the method[]
	 */
	public static Method[] parseClass(Class<? extends Annotation> annotationClass, Class<?> target) {
		if (annotationClass == null) {
			throw new NullPointerException("annotationClass cannot be null");
		}
		if (target == null || target == Object.class) {
			return new Method[0];
		}

		List<Method> methods = new ArrayList<>();
		// Check current class methods
		Method[] classMethods = parseMethods(annotationClass, target.getDeclaredMethods());
		if (classMethods.length > 0) {
			methods.addAll(List.of(classMethods));
		}

		// Check superclass methods
		Method[] superMethods = parseClass(annotationClass, target.getSuperclass());
		if (superMethods.length > 0) {
			methods.addAll(List.of(superMethods));
		}

		// Check interface methods
		Method[] interfaceMethods = parseInterfaces(annotationClass, target.getInterfaces());
		if (interfaceMethods.length > 0) {
			methods.addAll(List.of(interfaceMethods));
		}

		return methods.toArray(new Method[0]);
	}

	/**
	 * Parses the interfaces.
	 *
	 * @param annotationClass the annotation class
	 * @param interfaces      the interfaces
	 * @return the method[]
	 */
	public static Method[] parseInterfaces(Class<? extends Annotation> annotationClass, Class<?>... interfaces) {
		if (annotationClass == null) {
			throw new NullPointerException("annotationClass cannot be null");
		}
		if (interfaces == null) {
			throw new NullPointerException("interfaces cannot be null");
		}
		return parseInterfaces(annotationClass, new HashSet<>(), interfaces);
	}

	/**
	 * Parses the interfaces.
	 *
	 * @param annotationClass the annotation class
	 * @param visited         the visited
	 * @param target          the target
	 * @return the method[]
	 */
	private static Method[] parseInterfaces(Class<? extends Annotation> annotationClass, Set<Class<?>> visited,
			Class<?>... target) {
		if (annotationClass == null) {
			throw new NullPointerException("annotationClass cannot be null");
		}
		if (visited == null) {
			throw new NullPointerException("visited cannot be null");
		}
		if (target == null) {
			throw new NullPointerException("target cannot be null");
		}

		List<Method> methods = new ArrayList<>();
		for (Class<?> i : target) {
			if (i == null || !visited.add(i)) {
				continue;
			}

			// Check current interface methods
			Method[] interfaceMethods = parseMethods(annotationClass, i.getDeclaredMethods());
			if (interfaceMethods.length > 0) {
				methods.addAll(List.of(interfaceMethods));
			}

			// Recurse to parent interfaces
			Method[] parentMethods = parseInterfaces(annotationClass, visited, i.getInterfaces());
			if (parentMethods.length > 0) {
				methods.addAll(List.of(parentMethods));
			}
		}

		return methods.toArray(new Method[0]);
	}

	/**
	 * Parses the method.
	 *
	 * @param cl             the cl
	 * @param name           the name
	 * @param parameterTypes the parameter types
	 * @return the method
	 */
	public static Method parseMethod(Class<?> cl, String name, Class<?>... parameterTypes) {
		if (cl == Object.class)
			return null;

		try {
			Method method = cl.getDeclaredMethod(name, parameterTypes);
			return method;
		} catch (NoSuchMethodException e) {}

		return parseMethod(cl.getSuperclass(), name, parameterTypes);
	}

	/**
	 * Parses the methods.
	 *
	 * @param annotationClass the annotation class
	 * @param methods         the methods
	 * @return the method[]
	 */
	public static Method[] parseMethods(Class<? extends Annotation> annotationClass, Method... methods) {
		if (annotationClass == null) {
			throw new NullPointerException("annotationClass cannot be null");
		}
		if (methods == null) {
			throw new NullPointerException("methods cannot be null");
		}

		List<Method> annotatedMethods = new ArrayList<>(methods.length);
		for (Method method : methods) {
			if (method.isAnnotationPresent(annotationClass)) {
				annotatedMethods.add(method);
			}
		}

		return annotatedMethods.toArray(new Method[0]);
	}

	/**
	 * Parses the object.
	 *
	 * @param annotationClass the annotation class
	 * @param target          the target
	 * @return the method[]
	 */
	public static Method[] parseObject(Class<? extends Annotation> annotationClass, Object target) {
		if (annotationClass == null) {
			throw new NullPointerException("annotationClass cannot be null");
		}
		if (target == null) {
			return new Method[0];
		}

		if (target instanceof Class<?>[] interfaces) {
			return parseInterfaces(annotationClass, interfaces);
		}

		if (target instanceof Class<?> cl) {
			return parseClass(annotationClass, cl);
		}

		return parseClass(annotationClass, target.getClass());
	}

	/**
	 * Instantiates a new methods.
	 */
	private Methods() {
		// Utility class, not instantiable
	}
}