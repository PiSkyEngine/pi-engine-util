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
 * The Class ConfigEvent.
 */
public class ConfigEvent {
    
    /**
	 * The Enum ChangeType.
	 */
    public enum ChangeType { 
 /** The set. */
 SET, 
 /** The remove. */
 REMOVE }

    /** The source. */
    protected final Config source;
    
    /** The key. */
    protected final String key;
    
    /** The old value. */
    protected final String oldValue;
    
    /** The new value. */
    protected final String newValue;
    
    /** The type. */
    protected final Class<?> type;
    
    /** The change type. */
    protected final ChangeType changeType;
    
    /** The timestamp. */
    protected final long timestamp;

    /**
	 * Instantiates a new config event.
	 *
	 * @param source     the source
	 * @param key        the key
	 * @param oldValue   the old value
	 * @param newValue   the new value
	 * @param type       the type
	 * @param changeType the change type
	 */
    public ConfigEvent(Config source, String key, String oldValue, String newValue, Class<?> type, ChangeType changeType) {
        this.source = source;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.type = type;
        this.changeType = changeType;
        this.timestamp = System.currentTimeMillis();
    }

    /**
	 * Gets the source.
	 *
	 * @return the source
	 */
    public Config getSource() {
        return source;
    }

    /**
	 * Gets the key.
	 *
	 * @return the key
	 */
    public String getKey() {
        return key;
    }

    /**
	 * Gets the old value.
	 *
	 * @return the old value
	 */
    public String getOldValue() {
        return oldValue;
    }

    /**
	 * Gets the new value.
	 *
	 * @return the new value
	 */
    public String getNewValue() {
        return newValue;
    }

    /**
	 * Gets the type.
	 *
	 * @return the type
	 */
    public Class<?> getType() {
        return type;
    }

    /**
	 * Gets the change type.
	 *
	 * @return the change type
	 */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
    public long getTimestamp() {
        return timestamp;
    }
}