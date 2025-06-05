package org.piengine.util.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.piengine.util.Registration;

class ConfigTest {
    private static final String PROP_CONTENT = """
            app.debug=true
            app.theme.color=BLUE
            server.port=8080
            server=prod
            """;
    private static final String YAML_CONTENT = """
            app:
              debug: true
              theme:
                color: BLUE
            server:
              port: 8080
            """;
    private static final String JSON_CONTENT = """
            {
              "app": {
                "debug": true,
                "theme": {
                  "color": "BLUE"
                }
              },
              "server": {
                "port": 8080
              }
            }
            """;
    private static final String PROP_OVERRIDE_CONTENT = """
            server.port=9090
            app.debug=false
            """;
    private static final String SERVER_PROP_CONTENT = """
            server.port=9090
            server.host=example.com
            """;

    @TempDir
    Path tempDir;
    private Path propFile;
    private Path yamlFile;
    private Path jsonFile;
    private Path propOverrideFile;
    private Path serverPropFile;

    @BeforeEach
    void setUp() throws IOException {
        propFile = tempDir.resolve("config.properties");
        yamlFile = tempDir.resolve("config.yaml");
        jsonFile = tempDir.resolve("config.json");
        propOverrideFile = tempDir.resolve("config-prod.properties");
        serverPropFile = tempDir.resolve("server.properties");

        Files.writeString(propFile, PROP_CONTENT);
        Files.writeString(yamlFile, YAML_CONTENT);
        Files.writeString(jsonFile, JSON_CONTENT);
        Files.writeString(propOverrideFile, PROP_OVERRIDE_CONTENT);
        Files.writeString(serverPropFile, SERVER_PROP_CONTENT);

        // Clear system properties
        System.clearProperty("server.port");
        System.clearProperty("app.debug");
        System.clearProperty("server.host");
    }

    enum Color { RED, BLUE, GREEN }

    @Test
    void testLoadProperties() {
        Config config = Config.load(propFile.toString());
        assertEquals("true", config.get("app.debug").asString());
        assertEquals(8080, config.get("server.port").asInt());
        assertEquals("BLUE", config.get("app.theme.color").asString());
    }

    @Test
    void testLoadYaml() {
        Config config = Config.load(yamlFile.toString());
        assertEquals("true", config.get("app.debug").asString());
        assertEquals(8080, config.get("server.port").asInt());
        assertEquals("BLUE", config.get("app.theme.color").asString());
    }

    @Test
    void testLoadJson() {
        Config config = Config.load(jsonFile.toString());
        assertEquals("true", config.get("app.debug").asString());
        assertEquals(8080, config.get("server.port").asInt());
        assertEquals("BLUE", config.get("app.theme.color").asString());
    }

    @Test
    void testAutoDetectExtension() {
        Config config = Config.load(tempDir.resolve("config").toString());
        assertEquals("true", config.get("app.debug").asString()); // Should load config.properties
    }

    @Test
    void testLoadMissingFile() {
        assertThrows(ConfigException.class, () -> Config.load(tempDir.resolve("nonexistent").toString()));
    }

    @Test
    void testHierarchicalProperties() {
        Config config = Config.load(propFile.toString());
        assertEquals("prod", config.get("server.host").asString()); // Falls back to "server=prod"
        assertNull(config.get("app.unknown").orString(null)); // No fallback
    }

    @Test
    void testSchemaConstraints() {
        ConfigSchema schema = ConfigSchema.create()
                .define("server.port", Integer.class, 8080).critical()
                .define("app.debug", Boolean.class, Boolean.FALSE)
                .define("app.theme.color", Color.class, Color.RED).warning();

        Config config = Config.load(propFile.toString()).withSchema(schema);
        assertEquals(8080, config.get("server.port").asInt());
        assertTrue(config.get("app.debug").asBoolean());
        assertEquals(Color.BLUE, config.get("app.theme.color").asEnum(Color.class));

        // Undefined key
        assertThrows(ConfigException.class, () -> config.get("undefined.key"));

        // Type mismatch
        assertThrows(ConfigException.class, () -> config.get("server.port").asBoolean());
    }

    @Test
    void testErrorHandlers() {
        AtomicInteger warnings = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger criticals = new AtomicInteger();

        ConfigSchema schema = ConfigSchema.create()
                .define("server.port", Integer.class, 8080).critical()
                .define("app.debug", Boolean.class, Boolean.FALSE).warning();

        Config config = Config.load(propFile.toString())
                .withSchema(schema)
                .withWarningHandler((key, msg, critical) -> warnings.incrementAndGet())
                .withErrorHandler((key, msg, critical) -> errors.incrementAndGet())
                .withCriticalHandler((key, msg, critical) -> criticals.incrementAndGet());

        // Trigger warning
        config.get("app.debug").asWarning().asInt();
        assertEquals(1, warnings.get());

        // Trigger critical
        assertThrows(ConfigException.class, () -> config.get("server.port").asCritical().asString());
        assertEquals(1, criticals.get());
    }

    @Test
    void testSystemPropertyOverride() {
        System.setProperty("server.port", "9999");
        Config config = Config.load(propFile.toString());
        assertEquals(9999, config.get("server.port").useSystemProperty().asInt());
        assertEquals(8080, config.get("server.port").asInt()); // Without override
    }

    @Test
    void testPutAndRemove() {
        Config config = Config.load(propFile.toString());
        config.put("server.port", 9090);
        assertEquals(9090, config.get("server.port").asInt());

        config.remove("server.port");
        assertEquals("prod", config.get("server.port").orString(null)); // Expect fallback to "server=prod"
    }

    @Test
    void testConfigChangeEvent() {
        Config config = Config.load(propFile.toString());
        List<ConfigEvent> events = new ArrayList<>();
        Registration reg = config.addListener(events::add);

        config.put("server.port", 9090);
        config.remove("app.debug");

        assertEquals(2, events.size());
        assertInstanceOf(ConfigChangeEvent.class, events.get(0));
        assertEquals("server.port", events.get(0).getKey());
        assertEquals("8080", events.get(0).getOldValue());
        assertEquals("9090", events.get(0).getNewValue());
        assertEquals(ConfigEvent.ChangeType.SET, events.get(0).getChangeType());

        assertInstanceOf(ConfigChangeEvent.class, events.get(1));
        assertEquals("app.debug", events.get(1).getKey());
        assertEquals("true", events.get(1).getOldValue());
        assertNull(events.get(1).getNewValue());
        assertEquals(ConfigEvent.ChangeType.REMOVE, events.get(1).getChangeType());

        reg.unregister();
    }

    @Test
    void testConfigLoadEvent() {
        Config config = Config.load(propFile.toString());
        List<ConfigEvent> events = new ArrayList<>();
        Registration reg = config.addListener(events::add);

        config.withOverrideConfig("prod");

        assertTrue(events.stream().anyMatch(e -> e instanceof ConfigLoadEvent &&
                e.getKey().equals("server.port") &&
                e.getNewValue().equals("9090")));
        assertTrue(events.stream().anyMatch(e -> e instanceof ConfigLoadEvent &&
                e.getKey().equals("app.debug") &&
                e.getNewValue().equals("false")));

        reg.unregister();
    }

    @Test
    void testFilteredListener() {
        Config config = Config.load(propFile.toString());
        List<ConfigEvent> events = new ArrayList<>();
        Registration reg = config.addListener("server.port", events::add);

        config.put("server.port", 9090);
        config.put("app.debug", false);

        assertEquals(1, events.size());
        assertEquals("server.port", events.get(0).getKey());
        assertEquals("9090", events.get(0).getNewValue());

        reg.unregister();
    }

    @Test
    void testPredicateFilteredListener() {
        Config config = Config.load(propFile.toString());
        List<ConfigEvent> events = new ArrayList<>();
        Predicate<String> filter = key -> key.startsWith("app.");
        Registration reg = config.addListener(filter, events::add);

        config.put("server.port", 9090);
        config.put("app.debug", false);
        config.put("app.theme.color", "RED");

        assertEquals(2, events.size());
        assertTrue(events.stream().allMatch(e -> e.getKey().startsWith("app.")));
        assertTrue(events.stream().anyMatch(e -> e.getKey().equals("app.debug")));
        assertTrue(events.stream().anyMatch(e -> e.getKey().equals("app.theme.color")));

        reg.unregister();
    }

    @Test
    void testSaveProperties() throws IOException {
        Config config = Config.load(propFile.toString());
        config.put("server.port", 9090);
        Path newFile = tempDir.resolve("new.properties");
        config.save(newFile.toString());

        Config loaded = Config.load(newFile.toString());
        assertEquals(9090, loaded.get("server.port").asInt());
    }

    @Test
    void testSaveYaml() throws IOException {
        Config config = Config.load(yamlFile.toString());
        config.put("server.port", 9090);
        Path newFile = tempDir.resolve("new.yaml");
        config.save(newFile.toString());

        Config loaded = Config.load(newFile.toString());
        assertEquals(9090, loaded.get("server.port").asInt());
    }

    @Test
    void testSaveJson() throws IOException {
        Config config = Config.load(jsonFile.toString());
        config.put("server.port", 9090);
        Path newFile = tempDir.resolve("new.json");
        config.save(newFile.toString());

        Config loaded = Config.load(newFile.toString());
        assertEquals(9090, loaded.get("server.port").asInt());
    }

    @Test
    void testMerge() throws IOException {
        Config config = Config.load(propFile.toString());
        config.put("new.key", "value");
        config.merge(propFile.toString());

        Config loaded = Config.load(propFile.toString());
        assertEquals("value", loaded.get("new.key").asString());
        assertEquals(8080, loaded.get("server.port").asInt());
    }

    @Test
    void testThreadSafetyListeners() throws InterruptedException {
        Config config = Config.load(propFile.toString());
        List<ConfigEvent> events = Collections.synchronizedList(new ArrayList<>());
        Registration reg1 = config.addListener(events::add);
        Registration reg2 = config.addListener(events::add);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                config.put("server.port", 9000 + i);
            }
        };

        Thread t1 = Thread.ofVirtual().start(task);
        Thread t2 = Thread.ofVirtual().start(task);
        t1.join();
        t2.join();

        assertEquals(400, events.size()); // 2 listeners * 2 threads * 100 updates
        assertTrue(events.stream().allMatch(e -> e instanceof ConfigChangeEvent));

        reg1.unregister();
        reg2.unregister();
    }

    @Test
    void testServerConfigDefaults() {
        ServerConfig config = new ServerConfig();
        assertEquals(8080, config.getServerPort());
        assertEquals("localhost", config.getServerHost());
    }

    @Test
    void testServerConfigLoad() {
        ServerConfig config = new ServerConfig(serverPropFile.toString());
        assertEquals(9090, config.getServerPort());
        assertEquals("example.com", config.getServerHost());
    }

    @Test
    void testServerConfigSchema() {
        ServerConfig config = new ServerConfig();
        assertThrows(ConfigException.class, () -> config.get("server.port").asString()); // Type mismatch
        assertThrows(ConfigException.class, () -> config.get("undefined.key")); // Undefined key
    }

    @Test
    void testServerConfigAccessors() {
        ServerConfig config = new ServerConfig();
        config.setServerPort(9090).setServerHost("example.com");
        assertEquals(9090, config.getServerPort());
        assertEquals("example.com", config.getServerHost());
    }
}