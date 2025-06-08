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
package org.piengine.util.config;

/**
 * Represents a configuration event. This base class captures details about
 * changes or loads of configuration properties, used by {@link ConfigListener}
 * to monitor configuration updates.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see ConfigListener
 * @see ConfigChangeEvent
 * @see ConfigLoadEvent
 */
public class ConfigEvent {

	/**
	 * Enumeration defining types of configuration changes.
	 */
	public enum ChangeType {
		/** Indicates a property value was set or updated. */
		SET,
		/** Indicates a property was removed. */
		REMOVE
	}

	/** The configuration instance that triggered the event. */
	protected final Config source;

	/** The configuration property key. */
	protected final String key;

	/** The previous value of the property, if any. */
	protected final String oldValue;

	/** The new value of the property. */
	protected final String newValue;

	/** The expected type of the property value. */
	protected final Class<?> type;

	/** The type of change (set or remove). */
	protected final ChangeType changeType;

	/** The timestamp when the event occurred. */
	protected final long timestamp;

	/**
	 * Constructs a new configuration event.
	 *
	 * @param source     the {@link Config} instance that triggered the event
	 * @param key        the configuration property key
	 * @param oldValue   the previous value of the property, if any
	 * @param newValue   the new value of the property
	 * @param type       the expected type of the property value
	 * @param changeType the type of change (set or remove)
	 */
	public ConfigEvent(Config source, String key, String oldValue, String newValue, Class<?> type,
			ChangeType changeType) {
		this.source = source;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.type = type;
		this.changeType = changeType;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Retrieves the configuration instance that triggered the event.
	 *
	 * @return the {@link Config} instance
	 */
	public Config getSource() {
		return source;
	}

	/**
	 * Retrieves the configuration property key.
	 *
	 * @return the property key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Retrieves the previous value of the property.
	 *
	 * @return the previous value, or {@code null} if none
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * Retrieves the new value of the property.
	 *
	 * @return the new value
	 */
	public String getNewValue() {
		return newValue;
	}

	/**
	 * Retrieves the expected type of the property value.
	 *
	 * @return the property type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Retrieves the type of change.
	 *
	 * @return the {@link ChangeType} (set or remove)
	 */
	public ChangeType getChangeType() {
		return changeType;
	}

	/**
	 * Retrieves the timestamp when the event occurred.
	 *
	 * @return the timestamp in milliseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}
}