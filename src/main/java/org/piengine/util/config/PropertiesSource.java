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
import java.util.Properties;

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
        HierarchicalProperties temp = new HierarchicalProperties();
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(path) :
                new FileInputStream(path)) {
            if (is != null) {
                temp.load(is);
                temp.forEach((k, v) -> {
                    String key = k.toString();
                    String oldValue = properties.getProperty(key);
                    properties.setProperty(key, v.toString());
                    config.fireConfigEvent(key, oldValue, v.toString(), ConfigEvent.ChangeType.SET, false);
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
                    String oldValue = config.get(key).asString();
                    config.put(key, v.toString());
                    config.fireConfigEvent(key, oldValue, v.toString(), ConfigEvent.ChangeType.SET, false);
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
            properties.store(fos, "Configuration");
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
        existing.putAll(properties);
        save(existing);
    }
}