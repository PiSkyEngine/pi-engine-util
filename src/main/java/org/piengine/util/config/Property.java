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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The Class Property.
 */
public class Property {

	/** The key. */
	private final String key;

	/** The properties. */
	private final HierarchicalProperties properties;

	/** The schema. */
	private final ConfigSchema schema;

	/** The config. */
	private final Config config;

	/** The use system property. */
	private boolean useSystemProperty;

	/** The error level. */
	private ErrorLevel errorLevel = ErrorLevel.NONE;

	/**
	 * The Enum ErrorLevel.
	 */
	public enum ErrorLevel {
		/** The none. */
		NONE,
		/** The warning. */
		WARNING,
		/** The error. */
		ERROR,
		/** The critical. */
		CRITICAL
	}

	/**
	 * Instantiates a new property.
	 *
	 * @param key        the key.
	 * @param properties the properties.
	 * @param schema     the schema.
	 * @param config     the config.
	 */
	Property(String key, HierarchicalProperties properties, ConfigSchema schema, Config config) {
		this.key = key;
		this.properties = properties;
		this.schema = schema;
		this.config = config;
		this.useSystemProperty = schema != null ? schema.useSystemProperties(key) : false;
	}

	/**
	 * Use system property.
	 *
	 * @return the property
	 */
	public Property useSystemProperty() {
		this.useSystemProperty = true;
		return this;
	}

	/**
	 * As warning.
	 *
	 * @return the property
	 */
	public Property asWarning() {
		this.errorLevel = ErrorLevel.WARNING;
		return this;
	}

	/**
	 * As error.
	 *
	 * @return the property
	 */
	public Property asError() {
		this.errorLevel = ErrorLevel.ERROR;
		return this;
	}

	/**
	 * As critical.
	 *
	 * @return the property
	 */
	public Property asCritical() {
		this.errorLevel = ErrorLevel.CRITICAL;
		return this;
	}

	/**
	 * Validate type.
	 *
	 * @param expectedType the expected type
	 * @return true, if successful
	 */
	private boolean validateType(Class<?> expectedType) {
		if (schema != null && schema.isDefined(key)) {
			Class<?> definedType = schema.getType(key);
			return definedType != null && expectedType.isAssignableFrom(definedType);
		}
		return true; // No schema or undefined key, allow conversion
	}

	/**
	 * As int.
	 *
	 * @return the int
	 */
	public int asInt() {
		if (!validateType(Integer.class)) {
			handleError("Type mismatch: expected Integer");
			if (errorLevel == ErrorLevel.WARNING) {
				return 0;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected Integer");
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return 0;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			handleError("Invalid integer: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return 0;
			}
			throw new ConfigException("Invalid integer for " + key + ": " + value);
		}
	}

	/**
	 * Or int.
	 *
	 * @param defaultValue the default value
	 * @return the int
	 */
	public int orInt(int defaultValue) {
		if (!validateType(Integer.class)) {
			handleError("Type mismatch: expected Integer");
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			handleError("Invalid integer: " + value);
			return defaultValue;
		}
	}

	/**
	 * As boolean.
	 *
	 * @return true, if successful
	 */
	public boolean asBoolean() {
		if (!validateType(Boolean.class)) {
			handleError("Type mismatch: expected Boolean");
			if (errorLevel == ErrorLevel.WARNING) {
				return false;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected Boolean");
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return false;
			}
			throw new ConfigException("Missing property: " + key);
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * Or boolean.
	 *
	 * @param defaultValue the default value
	 * @return true, if successful
	 */
	public boolean orBoolean(boolean defaultValue) {
		if (!validateType(Boolean.class)) {
			handleError("Type mismatch: expected Boolean");
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * As double.
	 *
	 * @return the double
	 */
	public double asDouble() {
		if (!validateType(Double.class)) {
			handleError("Type mismatch: expected Double");
			if (errorLevel == ErrorLevel.WARNING) {
				return 0.0;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected Double");
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return 0.0;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			handleError("Invalid double: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return 0.0;
			}
			throw new ConfigException("Invalid double for " + key + ": " + value);
		}
	}

	/**
	 * Or double.
	 *
	 * @param defaultValue the default value
	 * @return the double
	 */
	public double orDouble(double defaultValue) {
		if (!validateType(Double.class)) {
			handleError("Type mismatch: expected Double");
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			handleError("Invalid double: " + value);
			return defaultValue;
		}
	}

	/**
	 * As string.
	 *
	 * @return the string
	 */
	public String asString() {
		if (!validateType(String.class)) {
			handleError("Type mismatch: expected String");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected String");
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		return value;
	}

	/**
	 * Or string.
	 *
	 * @param defaultValue the default value
	 * @return the string
	 */
	public String orString(String defaultValue) {
		if (!validateType(String.class)) {
			handleError("Type mismatch: expected String");
			return defaultValue;
		}
		String value = getValue();
		return value != null ? value : defaultValue;
	}

	/**
	 * As enum.
	 *
	 * @param <T>       the generic type
	 * @param enumClass the enum class
	 * @return the t
	 */
	public <T extends Enum<T>> T asEnum(Class<T> enumClass) {
		if (!validateType(enumClass)) {
			handleError("Type mismatch: expected " + enumClass.getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected " + enumClass.getSimpleName());
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException e) {
			handleError("Invalid enum value: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Invalid enum value for " + key + ": " + value);
		}
	}

	/**
	 * Or enum.
	 *
	 * @param <T>          the generic type
	 * @param enumClass    the enum class
	 * @param defaultValue the default value
	 * @return the t
	 */
	public <T extends Enum<T>> T orEnum(Class<T> enumClass, T defaultValue) {
		if (!validateType(enumClass)) {
			handleError("Type mismatch: expected " + enumClass.getSimpleName());
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException e) {
			handleError("Invalid enum value: " + value);
			return defaultValue;
		}
	}

	/**
	 * As enum with.
	 *
	 * @param <T>       the generic type
	 * @param enumClass the enum class
	 * @param parser    the parser
	 * @return the t
	 */
	public <T> T asEnumWith(Class<T> enumClass, Function<String, T> parser) {
		if (!validateType(enumClass)) {
			handleError("Type mismatch: expected " + enumClass.getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected " + enumClass.getSimpleName());
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return parser.apply(value);
		} catch (Exception e) {
			handleError("Invalid enum value: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Invalid enum value for " + key + ": " + value);
		}
	}

	/**
	 * Or enum with.
	 *
	 * @param <T>          the generic type
	 * @param enumClass    the enum class
	 * @param parser       the parser
	 * @param defaultValue the default value
	 * @return the t
	 */
	public <T> T orEnumWith(Class<T> enumClass, Function<String, T> parser, T defaultValue) {
		if (!validateType(enumClass)) {
			handleError("Type mismatch: expected " + enumClass.getSimpleName());
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return parser.apply(value);
		} catch (Exception e) {
			handleError("Invalid enum value: " + value);
			return defaultValue;
		}
	}

	/**
	 * As class.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @return the t
	 */
	public <T> T asClass(Class<T> clazz) {
		if (!validateType(clazz)) {
			handleError("Type mismatch: expected " + clazz.getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected " + clazz.getSimpleName());
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return clazz.getConstructor(String.class).newInstance(value);
		} catch (Exception e) {
			handleError("Cannot instantiate class: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Cannot instantiate " + clazz.getName() + " for " + key + ": " + value);
		}
	}

	/**
	 * As instance.
	 *
	 * @param <T> the generic type
	 * @return the t
	 */
	public <T> T asInstance() {
		if (!validateType(Object.class)) {
			handleError("Type mismatch: expected Object");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected Object");
		}
		Object value = getRawValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		return (T) value;
	}

	/**
	 * Or class.
	 *
	 * @param <T>          the generic type
	 * @param clazz        the clazz
	 * @param defaultValue the default value
	 * @return the t
	 */
	public <T> T orClass(Class<T> clazz, T defaultValue) {
		if (!validateType(clazz)) {
			handleError("Type mismatch: expected " + clazz.getSimpleName());
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return clazz.getConstructor(String.class).newInstance(value);
		} catch (Exception e) {
			handleError("Cannot instantiate class: " + value);
			return defaultValue;
		}
	}

	/**
	 * As class with.
	 *
	 * @param <T>    the generic type
	 * @param clazz  the clazz
	 * @param parser the parser
	 * @return the t
	 */
	public <T> T asClassWith(Class<T> clazz, Function<String, T> parser) {
		if (!validateType(clazz)) {
			handleError("Type mismatch: expected " + clazz.getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected " + clazz.getSimpleName());
		}
		String value = getValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		try {
			return parser.apply(value);
		} catch (Exception e) {
			handleError("Cannot instantiate class: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Cannot instantiate " + clazz.getName() + " for " + key + ": " + value);
		}
	}

	/**
	 * Or class with.
	 *
	 * @param <T>          the generic type
	 * @param clazz        the clazz
	 * @param parser       the parser
	 * @param defaultValue the default value
	 * @return the t
	 */
	public <T> T orClassWith(Class<T> clazz, Function<String, T> parser, T defaultValue) {
		if (!validateType(clazz)) {
			handleError("Type mismatch: expected " + clazz.getSimpleName());
			return defaultValue;
		}
		String value = getValue();
		if (value == null) {
			return defaultValue;
		}
		try {
			return parser.apply(value);
		} catch (Exception e) {
			handleError("Cannot instantiate class: " + value);
			return defaultValue;
		}
	}

	/**
	 * As list.
	 *
	 * @param <T>         the generic type
	 * @param elementType the element type
	 * @return the list
	 */
	public <T> List<T> asList(Class<T> elementType) {
		if (!validateType(List.class)) {
			handleError("Type mismatch: expected List");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected List");
		}
		if (schema != null && schema.getElementType(key) != null && !elementType.isAssignableFrom(schema.getElementType(key))) {
			handleError("Element type mismatch: expected " + schema.getElementType(key).getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Element type mismatch for " + key + ": expected " + schema.getElementType(key).getSimpleName());
		}
		Object value = getRawValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		if (!(value instanceof List)) {
			handleError("Value is not a List: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Value for " + key + " is not a List: " + value);
		}
		return (List<T>) value;
	}

	/**
	 * Or list.
	 *
	 * @param <T>          the generic type
	 * @param elementType  the element type
	 * @param defaultValue the default value
	 * @return the list
	 */
	public <T> List<T> orList(Class<T> elementType, List<T> defaultValue) {
		if (!validateType(List.class)) {
			handleError("Type mismatch: expected List");
			return defaultValue;
		}
		if (schema != null && schema.getElementType(key) != null && !elementType.isAssignableFrom(schema.getElementType(key))) {
			handleError("Element type mismatch: expected " + schema.getElementType(key).getSimpleName());
			return defaultValue;
		}
		Object value = getRawValue();
		if (value == null && schema != null && schema.getDefaultValue(key) != null) {
			value = schema.getDefaultValue(key);
		}
		if (value == null) {
			return defaultValue;
		}
		if (!(value instanceof List)) {
			handleError("Value is not a List: " + value);
			return defaultValue;
		}
		return (List<T>) value;
	}

	/**
	 * As map.
	 *
	 * @param <T>       the generic type
	 * @param valueType the value type
	 * @return the map
	 */
	public <T> Map<String, T> asMap(Class<T> valueType) {
		if (!validateType(Map.class)) {
			handleError("Type mismatch: expected Map");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Type mismatch for " + key + ": expected Map");
		}
		if (schema != null && schema.getElementType(key) != null && !valueType.isAssignableFrom(schema.getElementType(key))) {
			handleError("Value type mismatch: expected " + schema.getElementType(key).getSimpleName());
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Value type mismatch for " + key + ": expected " + schema.getElementType(key).getSimpleName());
		}
		Object value = getRawValue();
		if (value == null) {
			handleError("Missing property");
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Missing property: " + key);
		}
		if (!(value instanceof Map)) {
			handleError("Value is not a Map: " + value);
			if (errorLevel == ErrorLevel.WARNING) {
				return null;
			}
			throw new ConfigException("Value for " + key + " is not a Map: " + value);
		}
		return (Map<String, T>) value;
	}

	/**
	 * Or map.
	 *
	 * @param <T>          the generic type
	 * @param valueType    the value type
	 * @param defaultValue the default value
	 * @return the map
	 */
	public <T> Map<String, T> orMap(Class<T> valueType, Map<String, T> defaultValue) {
		if (!validateType(Map.class)) {
			handleError("Type mismatch: expected Map");
			return defaultValue;
		}
		if (schema != null && schema.getElementType(key) != null && !valueType.isAssignableFrom(schema.getElementType(key))) {
			handleError("Value type mismatch: expected " + schema.getElementType(key).getSimpleName());
			return defaultValue;
		}
		Object value = getRawValue();
		if (value == null && schema != null && schema.getDefaultValue(key) != null) {
			value = schema.getDefaultValue(key);
		}
		if (value == null) {
			return defaultValue;
		}
		if (!(value instanceof Map)) {
			handleError("Value is not a Map: " + value);
			return defaultValue;
		}
		return (Map<String, T>) value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	private String getValue() {
		if (useSystemProperty) {
			String sysValue = System.getProperty(key);
			if (sysValue != null) {
				return sysValue;
			}
		}
		if (schema != null && schema.getDefaultValue(key) != null) {
			String defaultValue = schema.getDefaultValue(key).toString();
			String propValue = properties.getProperty(key);
			return propValue != null ? propValue : defaultValue;
		}
		return properties.getProperty(key);
	}

	/**
	 * Gets the raw value.
	 *
	 * @return the raw value
	 */
	private Object getRawValue() {
		if (useSystemProperty) {
			String sysValue = System.getProperty(key);
			if (sysValue != null) {
				if (schema != null && schema.isDefined(key)) {
					Class<?> type = schema.getType(key);
					if (List.class.isAssignableFrom(type)) {
						return Arrays.asList(sysValue.split("\\s*,\\s*"));
					} else if (Map.class.isAssignableFrom(type)) {
						Map<String, String> map = new HashMap<>();
						if (!sysValue.trim().isEmpty()) {
							String[] pairs = sysValue.split("\\s*,\\s*");
							for (String pair : pairs) {
								String[] kv = pair.split("=");
								if (kv.length == 2) {
									map.put(kv[0].trim(), kv[1].trim());
								}
							}
						}
						return map;
					}
				}
				return sysValue;
			}
		}
		return properties.getRawProperty(key);
	}

	/**
	 * Handle error.
	 *
	 * @param message the message
	 */
	private void handleError(String message) {
		boolean isCritical = errorLevel == ErrorLevel.CRITICAL || (schema != null && schema.isCritical(key));
		ConfigErrorHandler handler = null;
		switch (errorLevel) {
		case WARNING:
			handler = config.getWarningHandler();
			break;
		case ERROR:
			handler = config.getErrorHandler();
			break;
		case CRITICAL:
			handler = config.getCriticalHandler();
			break;
		default:
			break;
		}
		if (handler != null) {
			handler.handleError(key, message, isCritical);
		}
		if (errorLevel == ErrorLevel.CRITICAL || errorLevel == ErrorLevel.ERROR) {
			throw new ConfigException(message + ": " + key);
		}
	}
}