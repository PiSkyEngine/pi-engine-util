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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for discovering methods annotated with a specific annotation in
 * the PiEngine framework. This class provides methods to parse annotated
 * methods from objects, classes, or interfaces, traversing class hierarchies
 * and interface hierarchies recursively. It is typically used to find methods
 * marked with framework-specific annotations (e.g., {@code @PluginBackground},
 * {@code @PluginUpdatable}, {@code @PluginSharedTask}) for execution in the
 * plugin system.
 * <p>
 * Methods are discovered via reflection, and the framework should validate
 * method signatures after discovery (e.g., {@code void method()} for
 * {@code @PluginBackground}, {@code void method(float tpf)} for
 * {@code @PluginUpdatable}). The class also provides utilities to check if a
 * method or constructor is static or non-static, useful for determining
 * execution context.
 * <p>
 * Example usage:
 * 
 * <pre>
 * Method[] backgroundMethods = Methods.parseObject(PluginBackground.class, plugin);
 * for (Method m : backgroundMethods) {
 * 	// Schedule m for dedicated thread execution
 * }
 * </pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public final class Methods {

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
	 * Parses methods annotated with the specified annotation from the target
	 * object, class, or array of interfaces. Recursively searches the class
	 * hierarchy and implemented interfaces.
	 *
	 * @param annotationClass the annotation class to search for
	 * @param target          the object, class, or array of interfaces to search
	 * @return an array of annotated methods, or an empty array if none are found
	 * @throws NullPointerException if {@code annotationClass} is null
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
	 * Parses methods annotated with the specified annotation from the target class,
	 * its superclasses, and implemented interfaces.
	 *
	 * @param annotationClass the annotation class to search for
	 * @param target          the class to search
	 * @return an array of annotated methods, or an empty array if none are found
	 * @throws NullPointerException if {@code annotationClass} is null
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
	 * Parses methods annotated with the specified annotation from the provided
	 * method array.
	 *
	 * @param annotationClass the annotation class to search for
	 * @param methods         the methods to search
	 * @return an array of annotated methods, or an empty array if none are found
	 * @throws NullPointerException if {@code annotationClass} or {@code methods} is
	 *                              null
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
	 * Checks if the specified executable (method or constructor) is static.
	 *
	 * @param target the executable to check
	 * @return {@code true} if the executable is static, {@code false} otherwise
	 * @throws NullPointerException if {@code target} is null
	 */
	public static boolean isStatic(Executable target) {
		if (target == null) {
			throw new NullPointerException("target cannot be null");
		}
		return Modifier.isStatic(target.getModifiers());
	}

	/**
	 * Checks if the specified executable (method or constructor) is non-static
	 * (instance method).
	 *
	 * @param target the executable to check
	 * @return {@code true} if the executable is non-static, {@code false} otherwise
	 * @throws NullPointerException if {@code target} is null
	 */
	public static boolean isNonStatic(Executable target) {
		if (target == null) {
			throw new NullPointerException("target cannot be null");
		}
		return !Modifier.isStatic(target.getModifiers());
	}

	/**
	 * Parses methods annotated with the specified annotation from the target
	 * interfaces.
	 *
	 * @param annotationClass the annotation class to search for
	 * @param interfaces      the interfaces to search
	 * @return an array of annotated methods, or an empty array if none are found
	 * @throws NullPointerException if {@code annotationClass} or {@code interfaces}
	 *                              is null
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
	 * Internal method to parse methods annotated with the specified annotation from
	 * the target interfaces, tracking visited interfaces to avoid redundant
	 * processing.
	 *
	 * @param annotationClass the annotation class to search for
	 * @param visited         the set of interfaces already processed
	 * @param target          the interfaces to search
	 * @return an array of annotated methods, or an empty array if none are found
	 * @throws NullPointerException if {@code annotationClass}, {@code visited}, or
	 *                              {@code target} is null
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
	 * Private constructor to prevent instantiation.
	 */
	private Methods() {
		// Utility class, not instantiable
	}
}