package org.piengine.util.config;

/**
 * Component-specific configuration for server settings.
 */
public class ServerConfig extends Config {
    private static final String SERVER_PORT_KEY = "server.port";
    private static final String SERVER_HOST_KEY = "server.host";

    public ServerConfig() {
        super();
        // Set defaults
        properties.setProperty(SERVER_PORT_KEY, "8080");
        properties.setProperty(SERVER_HOST_KEY, "localhost");

        // Define schema
        schema = ConfigSchema.create()
                .define(SERVER_PORT_KEY, Integer.class, 8080).critical()
                .define(SERVER_HOST_KEY, String.class, "localhost");
    }

    public ServerConfig(String path) {
        this();
        addSource(path);
    }

    public int getServerPort() {
        return get(SERVER_PORT_KEY).asInt();
    }

    public String getServerHost() {
        return get(SERVER_HOST_KEY).asString();
    }

    public ServerConfig setServerPort(int port) {
        put(SERVER_PORT_KEY, port);
        return this;
    }

    public ServerConfig setServerHost(String host) {
        put(SERVER_HOST_KEY, host);
        return this;
    }
}