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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Implementation of StableValue using AtomicReference for thread-safety.
 */
class StableValueImpl<T> implements StableValue<T> {
    private final AtomicReference<T> content;

    StableValueImpl() {
        this.content = new AtomicReference<>();
    }

    StableValueImpl(T value) {
        this.content = new AtomicReference<>(Objects.requireNonNull(value, "value must not be null"));
    }

    @Override
    public T orElseSet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        // Check if already set
        T current = content.get();
        if (current != null) {
            return current;
        }
        // Compute value and attempt to set
        T newValue = supplier.get();
        Objects.requireNonNull(newValue, "supplier returned null");
        boolean set = content.compareAndSet(null, newValue);
        return set ? newValue : content.get();
    }

    @Override
    public T orElseThrow() {
        T value = content.get();
        if (value == null) {
            throw new IllegalStateException("StableValue is not set");
        }
        return value;
    }

    @Override
    public T orElse(T other) {
        T value = content.get();
        return value != null ? value : other;
    }

    @Override
    public boolean trySet(T value) {
        Objects.requireNonNull(value, "value must not be null");
        return content.compareAndSet(null, value);
    }
}