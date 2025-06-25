package org.piengine.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A stable value is a holder of contents that can be set at most once.
 * Once set, the contents are immutable and can be retrieved multiple times.
 * This implementation provides thread-safe, at-most-once initialization,
 * mimicking the behavior of the JDK 25 preview StableValue API (JEP 502).
 *
 * @param <T> the type of the content held by this stable value
 */
public interface StableValue<T> {

    /**
     * Creates an unset StableValue with no content.
     *
     * @param <T> the type of the content
     * @return a new unset StableValue
     */
    static <T> StableValue<T> of() {
        return new StableValueImpl<>();
    }

    /**
     * Creates a set StableValue with the provided content.
     *
     * @param value the content to set
     * @param <T>   the type of the content
     * @return a new StableValue with the specified content
     * @throws NullPointerException if value is null
     */
    static <T> StableValue<T> of(T value) {
        Objects.requireNonNull(value, "value must not be null");
        return new StableValueImpl<>(value);
    }

    /**
     * Creates a stable supplier that caches the value of the provided supplier.
     * The supplier is invoked at most once, and the result is stored in a StableValue.
     *
     * @param supplier the supplier to compute the value
     * @param <T>      the type of the content
     * @return a Supplier that caches the computed value
     * @throws NullPointerException if supplier is null
     */
    static <T> Supplier<T> supplier(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new StableSupplier<>(supplier);
    }

    /**
     * Sets the content if not already set, using the provided supplier, and returns the content.
     * If the content is already set, the supplier is not invoked.
     *
     * @param supplier the supplier to compute the content
     * @return the content of this StableValue
     * @throws NullPointerException if supplier is null
     */
    T orElseSet(Supplier<? extends T> supplier);

    /**
     * Retrieves the content, throwing an exception if not set.
     *
     * @return the content of this StableValue
     * @throws IllegalStateException if the content is not set
     */
    T orElseThrow();

    /**
     * Retrieves the content, or returns the provided default if not set.
     *
     * @param other the default value to return if not set
     * @return the content of this StableValue, or the default value
     */
    T orElse(T other);

    /**
     * Attempts to set the content if not already set.
     *
     * @param value the content to set
     * @return true if the content was set, false if already set
     * @throws NullPointerException if value is null
     */
    boolean trySet(T value);
}