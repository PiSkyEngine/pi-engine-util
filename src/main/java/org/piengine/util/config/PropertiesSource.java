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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * The Class PropertiesSource.
 */
public class PropertiesSource implements ConfigSource {
    
    /** The path. */
    private final String path;
    
    /** The is classpath. */
    private final boolean isClasspath;
    
    /** The config. */
    private final Config config;

    /**
	 * Instantiates a new properties source.
	 *
	 * @param path   the path
	 * @param config the config
	 */
    public PropertiesSource(String path, Config config) {
        this(path, false, config);
    }

    /**
	 * Instantiates a new properties source.
	 *
	 * @param path        the path
	 * @param isClasspath the is classpath
	 * @param config      the config
	 */
    public PropertiesSource(String path, boolean isClasspath, Config config) {
        this.path = path;
        this.isClasspath = isClasspath;
        this.config = config;
    }

    /**
     * @see org.piengine.util.config.ConfigSource#load(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void load(HierarchicalProperties properties) {
        Properties temp = new Properties();
        System.out.println("PropertiesSource: Loading from " + path + ", schema present: " + (config.schema != null));
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(path) :
                new FileInputStream(path)) {
            if (is != null) {
                temp.load(is);
                temp.forEach((k, v) -> {
                    String key = k.toString();
                    Object value = parsePropertyValue(key, v.toString());
                    String oldValue = properties.getProperty(key);
                    System.out.println("PropertiesSource: Loading key=" + key + ", value=" + value + ", type=" + (value != null ? value.getClass().getSimpleName() : "null"));
                    properties.put(key, value);
                    config.fireConfigEvent(key, oldValue, value.toString(), ConfigEvent.ChangeType.SET, false);
                });
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to load properties: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#loadOverride(java.lang.String)
     */
    @Override
    public void loadOverride(String subProperty) {
        String overridePath = path.replaceFirst("\\.(\\w+)$", "-" + subProperty + ".$1");
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(overridePath) :
                new FileInputStream(overridePath)) {
            if (is != null) {
                Properties overrideProps = new Properties();
                overrideProps.load(is);
                overrideProps.forEach((k, v) -> {
                    String key = k.toString();
                    Object value = parsePropertyValue(key, v.toString());
                    String oldValue = config.get(key).asString();
                    config.putInstance(key, value);
                    config.fireConfigEvent(key, oldValue, value.toString(), ConfigEvent.ChangeType.SET, false);
                });
            }
        } catch (IOException e) {
            // Silent if override not found
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#save(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void save(HierarchicalProperties properties) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            Properties stringProps = new Properties();
            properties.forEach((k, v) -> {
                String key = k.toString();
                String value = formatPropertyValue(v);
                stringProps.setProperty(key, value);
            });
            stringProps.store(fos, "Configuration");
        } catch (IOException e) {
            throw new ConfigException("Failed to save properties: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#merge(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void merge(HierarchicalProperties properties) {
        HierarchicalProperties existing = new HierarchicalProperties();
        try (FileInputStream fis = new FileInputStream(path)) {
            existing.load(fis);
        } catch (IOException e) {
            save(properties);
            return;
        }
        properties.forEach((k, v) -> existing.put(k, v));
        save(existing);
    }

    /**
     * Parse property value for Lists and Maps.
     *
     * @param key   the key
     * @param value the value
     * @return the parsed value
     */
    private Object parsePropertyValue(String key, String value) {
        if (config != null && config.schema != null && config.schema.isDefined(key)) {
            Class<?> type = config.schema.getType(key);
            System.out.println("PropertiesSource: Parsing key=" + key + ", schema type=" + (type != null ? type.getSimpleName() : "null"));
            if (List.class.isAssignableFrom(type)) {
                List<String> list = Arrays.stream(value.split("\\s*,\\s*"))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                System.out.println("PropertiesSource: Parsed List for key=" + key + ", value=" + list);
                return list;
            } else if (Map.class.isAssignableFrom(type)) {
                Map<String, String> map = new HashMap<>();
                if (!value.trim().isEmpty()) {
                    String[] pairs = value.split("\\s*,\\s*");
                    for (String pair : pairs) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            map.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
                System.out.println("PropertiesSource: Parsed Map for key=" + key + ", value=" + map);
                return map;
            }
        }
        System.out.println("PropertiesSource: No schema or undefined key=" + key + ", storing as String: " + value);
        return value;
    }

    /**
     * Format property value for saving.
     *
     * @param value the value
     * @return the formatted string
     */
    private String formatPropertyValue(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(","));
        }
        return value.toString();
    }
}