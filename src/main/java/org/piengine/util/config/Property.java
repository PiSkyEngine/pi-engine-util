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
 * Represents a single configuration property in the Pie in the Sky (PI) 3D Graphics Engine,
 * providing methods to access and validate its value. Part of the {@code pi-engine-util} module,
 * this class integrates with the engine's YAML-based configuration system, supporting properties
 * defined in a {@link ConfigSchema} and stored in a {@link HierarchicalProperties} instance.
 * Used by {@link Config} to retrieve values for settings like threading, window properties, and plugins,
 * it supports type-safe access, system property overrides, and error handling with customizable levels.
 *
 * <p>Methods are provided to convert property values to various types (e.g., {@code int}, {@code String},
 * {@code List}), with optional default values for fallback. Validation ensures type consistency with
 * the schema, and errors can be configured as warnings, errors, or critical failures.</p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Config
 * @see ConfigSchema
 * @see HierarchicalProperties
 * @see ErrorLevel
 */
public class Property {

    /** The configuration property key. */
    private final String key;

    /** The hierarchical properties storing the configuration data. */
    private final HierarchicalProperties properties;

    /** The schema defining the property's type and constraints. */
    private final ConfigSchema schema;

    /** The configuration instance managing this property. */
    private final Config config;

    /** Indicates whether the property can be overridden by a system property. */
    private boolean useSystemProperty;

    /** The error level for validation failures. */
    private ErrorLevel errorLevel = ErrorLevel.NONE;

    /**
     * Enumeration defining error levels for configuration property validation failures.
     * Used to control the severity of errors when accessing or validating property values.
     */
    public enum ErrorLevel {
        /** No error handling; validation failures are ignored. */
        NONE,
        /** Validation failures are logged as warnings but do not throw exceptions. */
        WARNING,
        /** Validation failures are logged as errors and may throw exceptions. */
        ERROR,
        /** Validation failures are critical, always throwing exceptions. */
        CRITICAL
    }

    /**
     * Constructs a new property instance with the specified key, properties, schema, and config.
     *
     * @param key        the configuration property key
     * @param properties the {@link HierarchicalProperties} storing the configuration data
     * @param schema     the {@link ConfigSchema} defining the property's type and constraints
     * @param config     the {@link Config} instance managing this property
     */
    Property(String key, HierarchicalProperties properties, ConfigSchema schema, Config config) {
        this.key = key;
        this.properties = properties;
        this.schema = schema;
        this.config = config;
        this.useSystemProperty = schema != null ? schema.useSystemProperties(key) : false;
    }

    /**
     * Enables the property to be overridden by a system property, allowing runtime configuration.
     *
     * @return this {@code Property} instance for method chaining
     */
    public Property useSystemProperty() {
        this.useSystemProperty = true;
        return this;
    }

    /**
     * Sets the error level to warning, causing validation failures to be logged without throwing exceptions.
     *
     * @return this {@code Property} instance for method chaining
     * @see ErrorLevel#WARNING
     */
    public Property asWarning() {
        this.errorLevel = ErrorLevel.WARNING;
        return this;
    }

    /**
     * Sets the error level to error, causing validation failures to be logged and potentially throw exceptions.
     *
     * @return this {@code Property} instance for method chaining
     * @see ErrorLevel#ERROR
     */
    public Property asError() {
        this.errorLevel = ErrorLevel.ERROR;
        return this;
    }

    /**
     * Sets the error level to critical, causing validation failures to always throw exceptions.
     *
     * @return this {@code Property} instance for method chaining
     * @see ErrorLevel#CRITICAL
     */
    public Property asCritical() {
        this.errorLevel = ErrorLevel.CRITICAL;
        return this;
    }

    /**
     * Validates that the property's type matches the expected type according to the schema.
     *
     * @param expectedType the expected type of the property value
     * @return {@code true} if the type is valid or no schema is defined, {@code false} otherwise
     */
    private boolean validateType(Class<?> expectedType) {
        if (schema != null && schema.isDefined(key)) {
            Class<?> definedType = schema.getType(key);
            return definedType != null && expectedType.isAssignableFrom(definedType);
        }
        return true; // No schema or undefined key, allow conversion
    }

    /**
     * Retrieves the property value as an integer.
     *
     * @return the integer value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as an integer, or a default value if the value is missing or invalid.
     *
     * @param defaultValue the default integer value
     * @return the integer value, or the default value if retrieval fails
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
     * Retrieves the property value as a boolean.
     *
     * @return the boolean value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as a boolean, or a default value if the value is missing or invalid.
     *
     * @param defaultValue the default boolean value
     * @return the boolean value, or the default value if retrieval fails
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
     * Retrieves the property value as a double.
     *
     * @return the double value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as a double, or a default value if the value is missing or invalid.
     *
     * @param defaultValue the default double value
     * @return the double value, or the default value if retrieval fails
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
     * Retrieves the property value as a string.
     *
     * @return the string value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as a string, or a default value if the value is missing or invalid.
     *
     * @param defaultValue the default string value
     * @return the string value, or the default value if retrieval fails
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
     * Retrieves the property value as an enum of the specified type.
     *
     * @param <T>       the enum type
     * @param enumClass the enum class
     * @return the enum value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as an enum, or a default value if the value is missing or invalid.
     *
     * @param <T>          the enum type
     * @param enumClass    the enum class
     * @param defaultValue the default enum value
     * @return the enum value, or the default value if retrieval fails
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
     * Retrieves the property value as an enum using a custom parser.
     *
     * @param <T>       the enum type
     * @param enumClass the enum class
     * @param parser    the parser to convert the string value to the enum
     * @return the enum value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as an enum using a custom parser, or a default value if retrieval fails.
     *
     * @param <T>          the enum type
     * @param enumClass    the enum class
     * @param parser       the parser to convert the string value to the enum
     * @param defaultValue the default enum value
     * @return the enum value, or the default value if retrieval fails
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
     * Retrieves the property value as an instance of the specified class, instantiated with a string constructor.
     *
     * @param <T>   the type of the instance
     * @param clazz the class to instantiate
     * @return the instantiated object
     * @throws ConfigException if the value is missing, invalid, or cannot be instantiated
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
     * Retrieves the property value as an instance of a class, without type validation.
     *
     * @param <T> the type of the instance
     * @return the instance
     * @throws ConfigException if the value is missing or invalid
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
     * Retrieves the property value as an instance of the specified class, or a default value if retrieval fails.
     *
     * @param <T>          the type of the instance
     * @param clazz        the class to instantiate
     * @param defaultValue the default instance
     * @return the instantiated object, or the default value if retrieval fails
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
     * Retrieves the property value as an instance of the specified class using a custom parser.
     *
     * @param <T>    the type of the instance
     * @param clazz  the class to instantiate
     * @param parser the parser to convert the string value to the instance
     * @return the instantiated object
     * @throws ConfigException if the value is missing, invalid, or cannot be instantiated
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
     * Retrieves the property value as an instance of the specified class using a custom parser,
     * or a default value if retrieval fails.
     *
     * @param <T>          the type of the instance
     * @param clazz        the class to instantiate
     * @param parser       the parser to convert the string value to the instance
     * @param defaultValue the default instance
     * @return the instantiated object, or the default value if retrieval fails
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
     * Retrieves the property value as a list of the specified element type.
     *
     * @param <T>         the type of the list elements
     * @param elementType the element type
     * @return the list value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as a list, or a default value if the value is missing or invalid.
     *
     * @param <T>          the type of the list elements
     * @param elementType  the element type
     * @param defaultValue the default list value
     * @return the list value, or the default value if retrieval fails
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
     * Retrieves the property value as a map with string keys and the specified value type.
     *
     * @param <T>       the type of the map values
     * @param valueType the value type
     * @return the map value
     * @throws ConfigException if the value is missing, invalid, or does not match the expected type
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
     * Retrieves the property value as a map, or a default value if the value is missing or invalid.
     *
     * @param <T>          the type of the map values
     * @param valueType    the value type
     * @param defaultValue the default map value
     * @return the map value, or the default value if retrieval fails
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
     * Retrieves the string value of the property, prioritizing system properties if enabled.
     *
     * @return the string value, or the schema's default value if defined, or {@code null} if not found
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
     * Retrieves the raw value of the property, handling system properties and collections.
     *
     * @return the raw value, which may be a string, list, map, or other type, or {@code null} if not found
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
     * Handles validation errors according to the configured error level and schema.
     *
     * @param message the error message
     * @throws ConfigException if the error level is {@code ERROR} or {@code CRITICAL}
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