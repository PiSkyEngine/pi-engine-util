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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.piengine.util.config.ConfigSchema.PropertyDef;

/**
 * Defines a schema for configuration properties in a configuration system. This
 * class enables the specification of property keys, types, default values, and
 * validation rules, used by {@link Config} to enforce constraints in a
 * standalone configuration framework. It supports primitive types, custom
 * objects, collections, and system property overrides, with error levels for
 * validation.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Config
 * @see PropertyDef
 * @see Property.ErrorLevel
 */
public sealed class ConfigSchema permits PropertyDef {

	/**
	 * Inner class representing a single configuration property definition within a
	 * schema. Specifies the property's key, type, default value, parser, error
	 * level, and system property usage, supporting method chaining for
	 * configuration.
	 */
	public final class PropertyDef extends ConfigSchema {

		/** The configuration property key. */
		private final String key;

		/** The expected type of the property value. */
		private final Class<?> type;

		/** The default value for the property. */
		private final Object defaultValue;

		/**
		 * The parser for converting string inputs to the property's type, if
		 * applicable.
		 */
		private final Function<String, ?> parser;

		/** The error level for validation failures. */
		private Property.ErrorLevel errorLevel = Property.ErrorLevel.NONE;

		/** Indicates whether the property can be overridden by a system property. */
		private boolean useSystemProperty;

		/** The element type for collections, if applicable. */
		private final Class<?> elementType;

		/**
		 * Constructs a new property definition with the specified key, type, and
		 * default value.
		 *
		 * @param key          the configuration property key
		 * @param type         the expected type of the property value
		 * @param defaultValue the default value for the property
		 */
		PropertyDef(String key, Class<?> type, Object defaultValue) {
			this(key, type, defaultValue, null, null);
		}

		/**
		 * Constructs a new property definition with a custom parser for string inputs.
		 *
		 * @param key          the configuration property key
		 * @param type         the expected type of the property value
		 * @param defaultValue the default value for the property
		 * @param parser       the parser for converting string inputs to the property's
		 *                     type
		 */
		PropertyDef(String key, Class<?> type, Object defaultValue, Function<String, ?> parser) {
			this(key, type, defaultValue, parser, null);
		}

		/**
		 * Constructs a new property definition with an element type for collections.
		 *
		 * @param key          the configuration property key
		 * @param type         the expected type of the property value (e.g.,
		 *                     {@code List.class})
		 * @param defaultValue the default value for the property
		 * @param parser       the parser for converting string inputs to the property's
		 *                     type
		 * @param elementType  the element type for collections (e.g.,
		 *                     {@code String.class} for a {@code List<String>})
		 */
		PropertyDef(String key, Class<?> type, Object defaultValue, Function<String, ?> parser, Class<?> elementType) {
			super(definitions, rootSchema);
			this.key = key;
			this.type = type;
			this.defaultValue = defaultValue;
			this.parser = parser;
			this.elementType = elementType;
		}

		/**
		 * Marks the property as critical, causing validation failures to throw an
		 * exception.
		 *
		 * @return this {@code PropertyDef} instance for method chaining
		 * @see Property.ErrorLevel#CRITICAL
		 */
		public PropertyDef critical() {
			this.errorLevel = Property.ErrorLevel.CRITICAL;
			return this;
		}

		/**
		 * Marks the property as having an error level, causing validation failures to
		 * log an error.
		 *
		 * @return this {@code PropertyDef} instance for method chaining
		 * @see Property.ErrorLevel#ERROR
		 */
		public PropertyDef error() {
			this.errorLevel = Property.ErrorLevel.ERROR;
			return this;
		}

		/**
		 * Marks the property as having a warning level, causing validation failures to
		 * log a warning.
		 *
		 * @return this {@code PropertyDef} instance for method chaining
		 * @see Property.ErrorLevel#WARNING
		 */
		public PropertyDef warning() {
			this.errorLevel = Property.ErrorLevel.WARNING;
			return this;
		}

		/**
		 * Enables the property to be overridden by a system property.
		 *
		 * @return this {@code PropertyDef} instance for method chaining
		 */
		public PropertyDef useSystemProperty() {
			this.useSystemProperty = true;
			return this;
		}

		/**
		 * Retrieves the element type for collections, if applicable.
		 *
		 * @return the element type for collections, or {@code null} if not a collection
		 */
		public Class<?> getElementType() {
			return elementType;
		}
	}

	/**
	 * Creates a new, empty configuration schema.
	 *
	 * @return a new {@code ConfigSchema} instance
	 */
	public static ConfigSchema create() {
		return new ConfigSchema();
	}

	/** The map of property definitions, keyed by their configuration keys. */
	private final Map<String, PropertyDef> definitions;

	/** The root schema, used to maintain a reference to the top-level schema. */
	private final ConfigSchema rootSchema;

	/**
	 * Constructs a new, empty configuration schema.
	 */
	public ConfigSchema() {
		this(new HashMap<>(), null);
	}

	/**
	 * Constructs a configuration schema with shared definitions and a root schema
	 * reference.
	 *
	 * @param definitions the map of property definitions
	 * @param rootSchema  the root schema, or {@code null} if this is the root
	 */
	private ConfigSchema(Map<String, PropertyDef> definitions, ConfigSchema rootSchema) {
		this.definitions = definitions;
		this.rootSchema = rootSchema != null ? rootSchema : this;
	}

	/**
	 * Defines a boolean configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default boolean value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, boolean defaultValue) {
		return define(key, Boolean.class, defaultValue);
	}

	/**
	 * Defines a byte configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default byte value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, byte defaultValue) {
		return define(key, Byte.class, defaultValue);
	}

	/**
	 * Defines a char configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default char value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, char defaultValue) {
		return define(key, Character.class, defaultValue);
	}

	/**
	 * Defines a short configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default short value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, short defaultValue) {
		return define(key, Short.class, defaultValue);
	}

	/**
	 * Defines an int configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default int value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, int defaultValue) {
		return define(key, Integer.class, defaultValue);
	}

	/**
	 * Defines a long configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default long value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, long defaultValue) {
		return define(key, Long.class, defaultValue);
	}

	/**
	 * Defines a float configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default float value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, float defaultValue) {
		return define(key, Float.class, defaultValue);
	}

	/**
	 * Defines a double configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default double value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, double defaultValue) {
		return define(key, Double.class, defaultValue);
	}

	/**
	 * Defines a String configuration property with a default value.
	 *
	 * @param key          the configuration property key
	 * @param defaultValue the default String value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef define(String key, String defaultValue) {
		return define(key, String.class, defaultValue);
	}

	/**
	 * Defines a configuration property with a specified type and default value.
	 *
	 * @param <T>          the type of the property value
	 * @param key          the configuration property key
	 * @param type         the expected type of the property value
	 * @param defaultValue the default value for the property
	 * @return a {@code PropertyDef} for further configuration
	 */
	public <T> PropertyDef define(String key, Class<T> type, T defaultValue) {
		PropertyDef def = new PropertyDef(key, type, defaultValue);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Defines a configuration property for a custom object instance with a default
	 * value.
	 *
	 * @param <T>          the type of the property value
	 * @param key          the configuration property key
	 * @param type         the expected type of the property value
	 * @param defaultValue the default instance for the property
	 * @return a {@code PropertyDef} for further configuration
	 */
	public <T> PropertyDef defineInstance(String key, Class<T> type, T defaultValue) {
		PropertyDef def = new PropertyDef(key, type, defaultValue);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Defines a configuration property for a list of elements with a specified
	 * element type.
	 *
	 * @param <T>          the type of the list elements
	 * @param key          the configuration property key
	 * @param elementType  the type of elements in the list
	 * @param defaultValue the default list value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public <T> PropertyDef defineList(String key, Class<T> elementType, List<T> defaultValue) {
		PropertyDef def = new PropertyDef(key, List.class, defaultValue, null, elementType);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Defines a configuration property for a map with string keys and a specified
	 * value type.
	 *
	 * @param <T>          the type of the map values
	 * @param key          the key
	 * @param valueType    the value type
	 * @param defaultValue the default value
	 * @return a {@code PropertyDef} for further configuration
	 */
	public <T> PropertyDef defineMap(String key, Class<T> valueType, Map<String, T> defaultValue) {
		PropertyDef def = new PropertyDef(key, Map.class, defaultValue, null, valueType);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Defines a configuration property with a custom parser for string inputs.
	 *
	 * @param key          the configuration property key
	 * @param type         the expected type of the property value
	 * @param defaultValue the default value for the property
	 * @param parser       the parser for converting string inputs to the property's
	 *                     type
	 * @return a {@code PropertyDef} for further configuration
	 */
	public PropertyDef defineWithParser(String key, Class<?> type, Object defaultValue, Function<String, ?> parser) {
		PropertyDef def = new PropertyDef(key, type, defaultValue, parser);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Retrieves the default value for a configuration property.
	 *
	 * @param key the configuration property key
	 * @return the default value, or {@code null} if the property is not defined
	 */
	public Object getDefaultValue(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.defaultValue : null;
	}

	/**
	 * Retrieves the root schema of the configuration.
	 *
	 * @return the root {@code ConfigSchema} instance
	 */
	public ConfigSchema getRootSchema() {
		return rootSchema;
	}

	/**
	 * Retrieves the type of a configuration property.
	 *
	 * @param key the configuration property key
	 * @return the property's type, or {@code null} if the property is not defined
	 */
	public Class<?> getType(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.type : null;
	}

	/**
	 * Retrieves the element type for a collection-based configuration property.
	 *
	 * @param key the configuration property key
	 * @return the element type for collections, or {@code null} if not a collection
	 *         or not defined
	 */
	public Class<?> getElementType(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.elementType : null;
	}

	/**
	 * Checks if a configuration property is marked as critical.
	 *
	 * @param key the configuration property key
	 * @return {@code true} if the property is critical, {@code false} otherwise
	 * @see Property.ErrorLevel#CRITICAL
	 */
	public boolean isCritical(String key) {
		PropertyDef def = definitions.get(key);
		return def != null && def.errorLevel == Property.ErrorLevel.CRITICAL;
	}

	/**
	 * Checks if a configuration property is defined in the schema.
	 *
	 * @param key the configuration property key
	 * @return {@code true} if the property is defined, {@code false} otherwise
	 */
	public boolean isDefined(String key) {
		return definitions.containsKey(key);
	}

	/**
	 * Checks if a configuration property can be overridden by a system property.
	 *
	 * @param key the configuration property key
	 * @return {@code true} if system property override is enabled, {@code false}
	 *         otherwise
	 */
	public boolean useSystemProperties(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.useSystemProperty : false;
	}
}