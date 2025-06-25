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
 * Abstract base class for modules, providing a default implementation of
 * Module.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class AbstractModule implements Module {
	private Binder binder;

	/**
	 * @param <T>
	 * @param typeLiteral
	 * @return
	 * @see org.piengine.inject.Binder#bind(org.piengine.inject.TypeLiteral)
	 */
	public <T> BindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
		return binder().bind(typeLiteral);
	}

	/**
	 * @param <T>
	 * @param key
	 * @return
	 * @see org.piengine.inject.Binder#bind(org.piengine.inject.Key)
	 */
	public <T> BindingBuilder<T> bind(Key<T> key) {
		return binder().bind(key);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.piengine.inject.Binder#bind(java.lang.Class)
	 */
	public <T> BindingBuilder<T> bind(Class<T> type) {
		return binder().bind(type);
	}

	/**
	 * @param annotationType
	 * @param scope
	 * @see org.piengine.inject.Binder#bindScope(java.lang.Class,
	 *      org.piengine.inject.Scope)
	 */
	public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
		binder().bindScope(annotationType, scope);
	}

	/**
	 * @param module
	 * @see org.piengine.inject.Binder#install(org.piengine.inject.Module)
	 */
	public void install(Module module) {
		binder().install(module);
	}

	@Override
	public final void configure(Binder binder) {
		this.binder = binder;
		configure();
	}

	/**
	 * Configures bindings for this module. Subclasses should override this method.
	 */
	protected abstract void configure();

	/**
	 * Returns the binder for this module.
	 *
	 * @return the binder
	 */
	private Binder binder() {
		if (binder == null) {
			throw new IllegalStateException("Binder not initialized");
		}
		return binder;
	}
}