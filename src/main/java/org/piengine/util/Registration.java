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
package org.piengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Interface Registration.
 */
public interface Registration {

	/**
	 * The Class Cleanup.
	 */
	class Cleanup implements Registration {

		/** The list. */
		private final List<Registration> list = new ArrayList<>();
		
		/** The name. */
		private String name;

		/**
		 * Instantiates a new cleanup.
		 */
		public Cleanup() {
			this.name = "";
		}

		/**
		 * Instantiates a new cleanup.
		 *
		 * @param name the name
		 */
		public Cleanup(String name) {
			this.name = name;
		}

		/**
		 * Adds the.
		 *
		 * @param r the r
		 */
		public synchronized void add(Registration r) {
			list.add(r);
		}

		/**
		 * Cleanup.
		 */
		public synchronized void cleanup() {
			list.forEach(Registration::unregister);
			clear();
		}

		/**
		 * Clear.
		 */
		public synchronized void clear() {
			list.clear();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Cleanup [\"" + name + "\", " + list + "]";
		}

		/**
		 * @see org.piengine.util.Registration#unregister()
		 */
		@Override
		public void unregister() {
			cleanup();
		}
	}

	/**
	 * The Class Scoped.
	 */
	class Scoped extends Cleanup implements AutoCloseable {

		/**
		 * Instantiates a new scoped.
		 */
		public Scoped() {
			super();
		}

		/**
		 * Instantiates a new scoped.
		 *
		 * @param name the name
		 */
		public Scoped(String name) {
			super(name);
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() throws RuntimeException {
			super.cleanup();
		}

	}

	/**
	 * The Record NamedRegistration.
	 *
	 * @param name         the name
	 * @param registration the registration
	 */
	record NamedRegistration(String name, Registration registration) implements Registration {

		/**
		 * @see org.piengine.util.Registration#unregister()
		 */
		@Override
		public void unregister() {
			this.registration.unregister();
		}

	}

	/** The empty. */
	NamedRegistration EMPTY = new NamedRegistration("empty", () -> {});

	/**
	 * Empty.
	 *
	 * @return the named registration
	 */
	static NamedRegistration empty() {
		return EMPTY;
	}

	/**
	 * Of.
	 *
	 * @param r the r
	 * @return the registration
	 */
	static Registration of(Registration r) {
		return r;
	}

	/**
	 * And then.
	 *
	 * @param after the after
	 * @return the registration
	 */
	default Registration andThen(Registration after) {
		Registration before = this;

		return new NamedRegistration(
				String.join("->", before.toString(), after.toString()),
				() -> {
					before.unregister();
					after.unregister();
				});
	}

	/**
	 * Named.
	 *
	 * @return the named registration
	 */
	default NamedRegistration named() {
		if (this instanceof NamedRegistration nr)
			return nr;

		return named("");
	}

	/**
	 * Named.
	 *
	 * @param name the name
	 * @return the named registration
	 */
	default NamedRegistration named(String name) {
		if (this instanceof NamedRegistration nr && nr.name.equals(name))
			return nr;

		return new NamedRegistration(name, this);
	}

	/**
	 * On registration.
	 *
	 * @param action the action
	 * @return the registration
	 */
	default Registration onRegistration(Consumer<Registration> action) {
		action.accept(this);

		return this;
	}

	/**
	 * On unregister.
	 *
	 * @param runAfter the run after
	 * @return the registration
	 */
	default Registration onUnregister(Runnable runAfter) {
		return () -> {
			Registration.this.unregister();

			runAfter.run();
		};
	}

	/**
	 * Unregister.
	 */
	void unregister();
}