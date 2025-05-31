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
 * Represents a registration that can be revoked or canceled.
 * <p>
 * This interface provides a standard mechanism for handling registration and
 * unregistration of resources, listeners, or any entity that needs cleanup when
 * no longer needed. Implementations of this interface represent a handle to a
 * registration that can be used to cancel or revoke that registration.
 * </p>
 * <p>
 * This interface also provides convenient methods for registering callbacks to
 * be executed at registration time or at unregistration time.
 * </p>
 * <p>
 * Usage example:
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Registration.Cleanup cleanup = new Registration.Cleanup();
 *
 * eventBus.register(listener1).onRegistration(cleanup::add);
 * eventBus.register(listener2).onUnregister(() -> System.out.println("Unregistered"))
 *                            .onRegistration(cleanup::add);
 *
 * // Later, cleanup
 * cleanup.unregister();
 * }</pre>
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see java.util.function.Consumer
 * @since 1.0
 */
public interface Registration {

	/**
	 * A composite registration that holds a list of other registrations.
	 * <p>
	 * This class can be used to manage multiple registrations as a group. When
	 * {@link #cleanup()} or {@link #unregister()} is called, all contained
	 * registrations are unregistered and the list is cleared.
	 * </p>
	 */
	class Cleanup implements Registration {

		private final List<Registration> list = new ArrayList<>();
		private String name;

		public Cleanup() {
			this.name = "";
		}

		public Cleanup(String name) {
			this.name = name;
		}

		/**
		 * Adds a registration to the cleanup list.
		 *
		 * @param r the registration to add
		 */
		public synchronized void add(Registration r) {
			list.add(r);
		}

		/**
		 * Unregisters all contained registrations and clears the list.
		 */
		public synchronized void cleanup() {
			list.forEach(Registration::unregister);
			clear();
		}

		/**
		 * Clears the list of registrations without unregistering them.
		 */
		public synchronized void clear() {
			list.clear();
		}

		/**
		 * Returns a string representation of the cleanup registration list.
		 *
		 * @return a string representation
		 */
		@Override
		public String toString() {
			return "Cleanup [\"" + name + "\", " + list + "]";
		}

		/**
		 * Invokes {@link #cleanup()} when unregistering.
		 */
		@Override
		public void unregister() {
			cleanup();
		}
	}

	class Scoped extends Cleanup implements AutoCloseable {

		public Scoped() {
			super();
		}

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
	 * A named wrapper for a {@link Registration}.
	 * <p>
	 * Useful for identification or debugging purposes, particularly when working
	 * with collections of registrations.
	 * </p>
	 *
	 * @param name         the name of the registration
	 * @param registration the actual registration logic
	 */
	record NamedRegistration(String name, Registration registration) implements Registration {

		/**
		 * Calls {@link Registration#unregister()} on the wrapped registration.
		 */
		/**
		 * Invokes {@link #cleanup()} when unregistering.
		 */
		@Override
		public void unregister() {
			this.registration.unregister();
		}

	}

	NamedRegistration EMPTY = new NamedRegistration("empty", () -> {});

	/**
	 * Returns a shared instance of an empty named registration.
	 *
	 * @return an empty {@link NamedRegistration}
	 */
	static NamedRegistration empty() {
		return EMPTY;
	}

	/**
	 * Wraps an existing registration without modification.
	 *
	 * @param r the registration to wrap
	 * @return the same registration instance
	 */
	static Registration of(Registration r) {
		return r;
	}

	/**
	 * Returns a new registration that first unregisters this registration and then
	 * another.
	 *
	 * @param after the registration to run after this one
	 * @return a new composed {@link Registration}
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
	 * Converts this registration to a {@link NamedRegistration}, using a default
	 * name if needed.
	 *
	 * @return a {@link NamedRegistration} wrapping this registration
	 */
	default NamedRegistration named() {
		if (this instanceof NamedRegistration nr)
			return nr;

		return named("");
	}

	/**
	 * Wraps this registration in a {@link NamedRegistration} with the given name.
	 *
	 * @param name the name to associate with the registration
	 * @return a {@link NamedRegistration}
	 */
	default NamedRegistration named(String name) {
		if (this instanceof NamedRegistration nr && nr.name.equals(name))
			return nr;

		return new NamedRegistration(name, this);
	}

	/**
	 * Executes the given action on this registration and returns this registration.
	 * <p>
	 * This method is useful for performing additional setup on a newly created
	 * registration while maintaining a fluent interface pattern.
	 * </p>
	 * <p>
	 * A common use case is adding the registration to a collection for later
	 * cleanup:
	 * 
	 * <pre>
	 * // Where cleanup is a List&lt;Registration&gt; for tracking registrations
	 * Omnibus inputEventBus = injector.getInstance(Key.get(Omnibus.class, InputEvents.class));
	 * inputEventBus.register(this).onRegistration(cleanup::add);
	 * </pre>
	 * 
	 * This pattern allows for automatic tracking of registrations that can be later
	 * unregistered as a group.
	 * </p>
	 * 
	 * @param action the action to perform on this registration
	 * @return this registration, allowing for method chaining
	 * @throws NullPointerException if the action is null
	 */
	default Registration onRegistration(Consumer<Registration> action) {
		action.accept(this);

		return this;
	}

	/**
	 * Creates a new registration that will execute the given action after
	 * unregistration of this registration.
	 * <p>
	 * When the returned registration's {@code unregister()} method is called, this
	 * registration will be unregistered first, followed by the execution of the
	 * provided action.
	 * </p>
	 * <p>
	 * This method is useful for chaining cleanup operations to be performed in
	 * sequence.
	 * </p>
	 * 
	 * @param runAfter the action to run after unregistration
	 * @return a new registration that will execute the given action after
	 *         unregistration
	 * @throws NullPointerException if runAfter is null
	 */
	default Registration onUnregister(Runnable runAfter) {
		return () -> {
			Registration.this.unregister();

			runAfter.run();
		};
	}

	/**
	 * Unregisters the current registration, revoking or canceling whatever was
	 * registered.
	 * <p>
	 * This method should perform any cleanup necessary to remove a registration.
	 * For example, removing an event listener, closing a connection, or releasing
	 * resources.
	 * </p>
	 * <p>
	 * After this method is called, the registration should be considered invalid.
	 * Implementations should ensure that this method is idempotent and safe to call
	 * multiple times.
	 * </p>
	 */
	void unregister();
}