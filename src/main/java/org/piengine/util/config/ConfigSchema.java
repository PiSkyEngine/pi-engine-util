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
import java.util.Map;
import java.util.function.Function;

/**
 * The Class ConfigSchema.
 */
public class ConfigSchema {

	/**
	 * The Class PropertyDef.
	 */
	public class PropertyDef extends ConfigSchema {

		/** The key. */
		private final String key;

		/** The type. */
		private final Class<?> type;

		/** The default value. */
		private final Object defaultValue;

		/** The parser. */
		private final Function<String, ?> parser;

		/** The error level. */
		private Property.ErrorLevel errorLevel = Property.ErrorLevel.NONE;

		/** The use system property. */
		private boolean useSystemProperty;

		/**
		 * Instantiates a new property def.
		 *
		 * @param key          the key.
		 * @param type         the type.
		 * @param defaultValue the default value.
		 */
		PropertyDef(String key, Class<?> type, Object defaultValue) {
			this(key, type, defaultValue, null);
		}

		/**
		 * Instantiates a new property def.
		 *
		 * @param key          the key.
		 * @param type         the type.
		 * @param defaultValue the default value.
		 * @param parser       the parser.
		 */
		PropertyDef(String key, Class<?> type, Object defaultValue, Function<String, ?> parser) {
			super(definitions, rootSchema); // Share parent's definitions and rootSchema
			this.key = key;
			this.type = type;
			this.defaultValue = defaultValue;
			this.parser = parser;
		}

		/**
		 * Critical.
		 *
		 * @return the property def
		 */
		public PropertyDef critical() {
			this.errorLevel = Property.ErrorLevel.CRITICAL;
			return this;
		}

		/**
		 * Error.
		 *
		 * @return the property def
		 */
		public PropertyDef error() {
			this.errorLevel = Property.ErrorLevel.ERROR;
			return this;
		}

		/**
		 * Warning.
		 *
		 * @return the property def
		 */
		public PropertyDef warning() {
			this.errorLevel = Property.ErrorLevel.WARNING;
			return this;
		}

		/**
		 * Use system property.
		 *
		 * @return the property def
		 */
		public PropertyDef useSystemProperty() {
			this.useSystemProperty = true;
			return this;
		}
	}

	/**
	 * Creates the.
	 *
	 * @return the config schema
	 */
	public static ConfigSchema create() {
		return new ConfigSchema();
	}

	/** The definitions. */
	private final Map<String, PropertyDef> definitions;

	/** The root schema. */
	private final ConfigSchema rootSchema;

	/**
	 * Instantiates a new config schema.
	 */
	protected ConfigSchema() {
		this(new HashMap<>(), null);
	}

	/**
	 * Instantiates a new config schema.
	 *
	 * @param definitions the definitions.
	 * @param rootSchema  the root schema.
	 */
	private ConfigSchema(Map<String, PropertyDef> definitions, ConfigSchema rootSchema) {
		this.definitions = definitions;
		this.rootSchema = rootSchema != null ? rootSchema : this;
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, boolean defaultValue) {
		return define(key, Boolean.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, byte defaultValue) {
		return define(key, Byte.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, char defaultValue) {
		return define(key, Character.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, short defaultValue) {
		return define(key, Short.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, int defaultValue) {
		return define(key, Integer.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, long defaultValue) {
		return define(key, Long.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, float defaultValue) {
		return define(key, Float.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public PropertyDef define(String key, double defaultValue) {
		return define(key, Double.class, defaultValue);
	}

	/**
	 * Define.
	 *
	 * @param <T>          the generic type
	 * @param key          the key
	 * @param type         the type
	 * @param defaultValue the default value
	 * @return the property def
	 */
	public <T> PropertyDef define(String key, Class<T> type, T defaultValue) {
		PropertyDef def = new PropertyDef(key, type, defaultValue);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Define with parser.
	 *
	 * @param key          the key
	 * @param type         the type
	 * @param defaultValue the default value
	 * @param parser       the parser
	 * @return the property def
	 */
	public PropertyDef defineWithParser(String key, Class<?> type, Object defaultValue, Function<String, ?> parser) {
		PropertyDef def = new PropertyDef(key, type, defaultValue, parser);
		definitions.put(key, def);
		return def;
	}

	/**
	 * Gets the default value.
	 *
	 * @param key the key
	 * @return the default value
	 */
	public Object getDefaultValue(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.defaultValue : null;
	}

	/**
	 * Gets the root schema.
	 *
	 * @return the root schema
	 */
	public ConfigSchema getRootSchema() {
		return rootSchema;
	}

	/**
	 * Gets the type.
	 *
	 * @param key the key
	 * @return the type
	 */
	public Class<?> getType(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.type : null;
	}

	/**
	 * Checks if is critical.
	 *
	 * @param key the key
	 * @return true, if is critical
	 */
	public boolean isCritical(String key) {
		PropertyDef def = definitions.get(key);
		return def != null && def.errorLevel == Property.ErrorLevel.CRITICAL;
	}

	/**
	 * Checks if is defined.
	 *
	 * @param key the key
	 * @return true, if is defined
	 */
	public boolean isDefined(String key) {
		return definitions.containsKey(key);
	}

	/**
	 * Use system properties.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean useSystemProperties(String key) {
		PropertyDef def = definitions.get(key);
		return def != null ? def.useSystemProperty : false;
	}
}