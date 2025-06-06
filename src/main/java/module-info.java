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
 * The {@code pi.engine.util} module provides general-purpose utilities for the
 * Pie in the Sky (PI) 3D graphics engine. It includes classes and interfaces
 * for concurrency, event handling, and resource management, designed to support
 * multi-threaded 3D applications while being reusable in other Java projects.
 *
 * <h2>Purpose</h2>
 * This module is a standalone utility library, independent of the PI engine's
 * core functionality, making it suitable for use in other Java-based projects,
 * such as jMonkeyEngine applications. It offers tools for efficient concurrency,
 * decoupled event handling, and resource cleanup.
 *
 * <h2>Exported Packages</h2>
 * <ul>
 *   <li>{@code org.piengine.commons.util}: Core utility classes, including
 *       {@code Registration} for managing event listeners and resource cleanup.</li>
 *   <li>{@code org.piengine.commons.util.eventbus}: Event handling with the
 *       {@code Omnibus} event bus for publishing and subscribing to events.</li>
 *   <li>{@code org.piengine.commons.util.concurrent.locks}: Concurrency utilities,
 *       including {@code UpgradableReadWriteLock} and {@code Lockable} for
 *       high-performance read/write locking with upgrade support, optimized for
 *       virtual threads in 3D scene graphs.</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 * <ul>
 *   <li>{@code java.base}: Required for core Java functionality, including
 *       Java 23 features like Virtual Threads and StructuredTaskScope.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * This module is typically used in conjunction with other PI engine modules,
 * such as {@code pi.game.engine.core}, to provide concurrency and event handling
 * support. It can also be used independently in other projects requiring utility
 * abstractions.
 *
 * <h3>Example: Event Handling with Omnibus</h3>
 * <pre>
 * import org.piengine.commons.util.eventbus.Omnibus;
 * import org.piengine.commons.util.Registration;
 * import java.util.function.Consumer;
 *
 * class GameEvent {
 *     private final String message;
 *     public GameEvent(String message) { this.message = message; }
 *     public String getMessage() { return message; }
 * }
 *
 * Omnibus bus = new OmnibusImpl();
 * {@code Consumer<GameEvent> listener = event -> System.out.println("Received: " + event.getMessage());}
 * Registration reg = bus.registerListener(GameEvent.class, listener);
 * bus.publish(new GameEvent("Player Moved"));
 * reg.unregister();
 * </pre>
 *
 * <h2>Version</h2>
 * <ul>
 *   <li><b>Current Version</b>: 0.0.1-SNAPSHOT (as of May 30, 2025)</li>
 * </ul>
 *
 * @since 0.0.1
 */
module org.piengine.util {
	exports org.piengine.util;
	exports org.piengine.util.event;
	exports org.piengine.util.config;
	exports org.piengine.util.concurrent.locks;
	
	requires org.yaml.snakeyaml;
    requires com.fasterxml.jackson.databind;
}