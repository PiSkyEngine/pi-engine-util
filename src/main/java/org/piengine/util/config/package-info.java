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

/**
 * Provides a generic, standalone configuration system for managing application settings
 * in YAML, JSON, and properties file formats. This package offers a flexible, type-safe
 * framework for loading, storing, and validating configuration properties, suitable for
 * any Java application requiring runtime configuration management.
 *
 * <p>The core components include:</p>
 * <ul>
 *   <li>{@link Config}: The central interface for managing configuration data, supporting
 *       multiple sources, schema validation, and event-driven updates.</li>
 *   <li>{@link ConfigSchema}: Defines property keys, types, default values, and validation
 *       rules to enforce configuration constraints.</li>
 *   <li>{@link Property}: Provides type-safe access to configuration values with support
 *       for system property overrides and error handling.</li>
 *   <li>{@link ConfigListener}: Enables real-time monitoring of configuration changes
 *       through event notifications.</li>
 *   <li>{@link ConfigErrorHandler}: Handles validation errors with customizable severity
 *       levels (warning, error, critical).</li>
 * </ul>
 *
 * <p>Configuration data is stored internally in a package-private
 * {@link HierarchicalProperties} class, which supports hierarchical property resolution
 * using dot-separated namespaces. The package supports multiple configuration sources
 * via {@link ConfigSource}, including file-based and classpath resources, with automatic
 * format detection for YAML, JSON, and properties files.</p>
 *
 * <p>This package is designed to be independent of any specific application context,
 * offering a reusable solution for configuration needs. It includes robust error handling,
 * schema-driven validation, and event-driven architecture to facilitate dynamic
 * configuration updates.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Create a configuration schema
 * ConfigSchema schema = ConfigSchema.create()
 *     .define("app.name", "MyApp")
 *     .define("app.port", 8080).critical();
 *
 * // Load configuration from a YAML file
 * Config config = Config.load("config.yaml").withSchema(schema);
 *
 * // Access properties
 * String appName = config.get("app.name").asString();
 * int port = config.get("app.port").asInt();
 *
 * // Register a listener for configuration changes
 * config.addListener(event -> System.out.println("Config changed: " + event.getKey()));
 * </pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @since 1.0
 */
package org.piengine.util.config;