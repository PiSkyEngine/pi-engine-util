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
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * The central interface for dependency injection, responsible for creating
 * instances, resolving bindings, and managing injections.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Injector {

	/**
	 * Creates a new root injector with the specified modules.
	 *
	 * @param modules the modules to configure the injector
	 * @return a new Injector instance
	 */
	static Injector createInjector(Module... modules) {
		return new InjectorImpl(null, modules);
	}

	/**
	 * Creates an external injector for a specific annotation and injector function.
	 *
	 * @param injectAnnotation the annotation type for external injection
	 * @param injector         the function to supply instances
	 * @param <A>              the annotation type
	 * @param <T>              the dependency type
	 * @return a new Injector instance
	 */
	<A extends Annotation, T> Injector createExternalInjector(
			Class<A> injectAnnotation,
			Function<Key<T>, T> injector);

	/**
	 * Creates a child injector with the specified modules.
	 *
	 * @param modules the modules to configure the child injector
	 * @return a new child Injector instance
	 */
	Injector createChildInjector(Module... modules);

	/**
	 * Injects dependencies into the instance's fields and methods.
	 *
	 * @param instance the instance to inject
	 */
	void injectMembers(Object instance);

	/**
	 * Injects dependencies into static fields and methods of a class.
	 *
	 * @param clazz the class to inject
	 */
	void injectStaticMembers(Class<?> clazz);

	/**
	 * Retrieves an instance of the specified type.
	 *
	 * @param type the type of the instance
	 * @param <T>  the type parameter
	 * @return the instance
	 */
	<T> T getInstance(Class<T> type);

	/**
	 * Retrieves an instance for the specified key.
	 *
	 * @param key the key identifying the dependency
	 * @param <T> the type parameter
	 * @return the instance
	 */
	<T> T getInstance(Key<T> key);

	/**
	 * Retrieves the binding for the specified type.
	 *
	 * @param type the type of the binding
	 * @param <T>  the type parameter
	 * @return the binding
	 */
	<T> Binding<T> getBinding(Class<T> type);

	/**
	 * Retrieves the binding for the specified key.
	 *
	 * @param key the key identifying the binding
	 * @param <T> the type parameter
	 * @return the binding
	 */
	<T> Binding<T> getBinding(Key<T> key);

	/**
	 * Retrieves the provider for the specified type.
	 *
	 * @param type the type of the provider
	 * @param <T>  the type parameter
	 * @return the provider
	 */
	<T> Provider<T> getProvider(Class<T> type);

	/**
	 * Retrieves the provider for the specified key.
	 *
	 * @param key the key identifying the provider
	 * @param <T> the type parameter
	 * @return the provider
	 */
	<T> Provider<T> getProvider(Key<T> key);

	/**
	 * Retrieves parameters for a method's injection points.
	 *
	 * @param method the method to analyze
	 * @return an array of parameter instances
	 */
	Object[] getParameters(Method method);

	/**
	 * Retrieves parameters for a method's injection points.
	 *
	 * @param parameterTypes array of parameter types to get where nay null elements
	 *                       are skipped
	 * @return an array of parameter instances
	 */
	Object[] getParameters(Class<?>... parameterTypes);
}