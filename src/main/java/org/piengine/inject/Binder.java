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

/**
 * Facilitates the configuration of bindings in a Module.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Binder {

	/**
	 * Binds a type literal to a provider or implementation.
	 *
	 * @param typeLiteral the type literal to bind
	 * @param <T>         the type parameter
	 * @return a binding builder for further configuration
	 */
	<T> BindingBuilder<T> bind(TypeLiteral<T> typeLiteral);

	/**
	 * Binds a key to a provider or implementation.
	 *
	 * @param key the key to bind
	 * @param <T> the type parameter
	 * @return a binding builder for further configuration
	 */
	<T> BindingBuilder<T> bind(Key<T> key);

	/**
	 * Binds a class to a provider or implementation.
	 *
	 * @param type the class to bind
	 * @param <T>  the type parameter
	 * @return a binding builder for further configuration
	 */
	<T> BindingBuilder<T> bind(Class<T> type);

	/**
	 * Binds an annotation type to a scope.
	 *
	 * @param annotationType the annotation type
	 * @param scope          the scope to bind
	 */
	void bindScope(Class<? extends Annotation> annotationType, Scope scope);

	/**
	 * Installs a module for additional bindings.
	 *
	 * @param module the module to install
	 */
	void install(Module module);

}