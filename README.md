# PI Engine Util Module

**Version**: 0.0.1-SNAPSHOT  
**Date**: May 30, 2025  
**License**: MIT  

The `pi-engine-util` module is a core component of the [Pie in the Sky (PI) 3D Graphics Engine](https://www.piengine.org), providing essential utility classes for concurrency, event handling, and general-purpose functionality. This module offers tools like high-performance read-write locks, an event bus, and registration interfaces, designed to be reusable across projects, including the PI engine and other frameworks like jMonkeyEngine.

## Overview

The `pi-engine-util` module includes:

- **Concurrency Utilities**: `UpgradableReadWriteLock` and `Lockable` for efficient read/write locking with upgrade support, optimized for virtual threads in multi-threaded 3D scene graphs.
- **Event Handling**: `Omnibus` event bus for publishing and subscribing to events.
- **Registration**: `Registration` interface for managing event listeners and resource cleanup.
- **General Utilities**: Planned utilities for logging, collections, and string manipulation.

This module is independent of the PI engine’s core, making it suitable for use in other Java-based projects requiring utility abstractions.

## Installation

The `pi-engine-util` module is available on Maven Central. Add it to your project’s `pom.xml`:

```xml
<dependency>
    <groupId>org.piengine</groupId>
    <artifactId>pi-engine-util</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Requirements
- **Java**: 23 or higher (with `--enable-preview` for features like StructuredTaskScope and Virtual Threads).
- **Dependencies**: None (minimal dependencies for maximum reusability).

## Usage Examples

### Example 1: Using UpgradableReadWriteLock with Lockable
Use `UpgradableReadWriteLock` and `Lockable` to safely manage read/write access to a shared resource, with support for upgrading a read lock to a write lock:

```java
import org.piengine.util.concurrent.locks.Lockable;
import org.piengine.util.concurrent.locks.UpgradableReadWriteLock;
import org.piengine.util.concurrent.locks.Locked;

// Shared resource (e.g., a scene node)
class SceneNode {
    private String data = "Initial Data";

    public String getData() { return data; }
    public void setData(String newData) { this.data = newData; }
}

public class LockExample {
    public static void main(String[] args) throws InterruptedException {
        SceneNode node = new SceneNode();
        Lockable<Locked, Locked> lockable = new Lockable.LockableReadWrite(new UpgradableReadWriteLock());

        // Read operation
        try (Locked readLock = lockable.lockForRead()) {
            System.out.println("Read: " + node.getData());
            // Simulate considering an upgrade to write lock
            if (node.getData().equals("Initial Data")) {
                // Upgrade to write lock (releases read lock temporarily, reacquires after)
                try (Locked writeLock = lockable.lockForWrite()) {
                    node.setData("Updated Data");
                    System.out.println("Write: " + node.getData());
                }
            }
        }
    }
}
```

**Output**:
```
Read: Initial Data
Write: Updated Data
```

### Example 2: Using Omnibus Event Bus
Use the `Omnibus` event bus to publish and subscribe to events in a decoupled manner:

```java
import org.piengine.commons.util.eventbus.Omnibus;
import org.piengine.commons.util.Registration;
import java.util.function.Consumer;

// Event class
class GameEvent {
    private final String message;
    public GameEvent(String message) { this.message = message; }
    public String getMessage() { return message; }
}

public class EventBusExample {
    public static void main(String[] args) {
        Omnibus bus = new OmnibusImpl();

        // Register a listener for GameEvent
        Consumer<GameEvent> listener = event -> System.out.println("Received: " + event.getMessage());
        Registration reg = bus.registerListener(GameEvent.class, listener);

        // Publish an event
        bus.publish(new GameEvent("Player Moved"));

        // Unregister the listener
        reg.unregister();
    }
}
```

**Output**:
```
Received: Player Moved
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository: `https://github.com/PiSkyEngine/pisky-engine`.
2. Create a branch for your feature (`git checkout -b feature/new-utility`).
3. Commit your changes (`git commit -m "Add new logging utility"`).
4. Push to your branch (`git push origin feature/new-utility`).
5. Open a pull request on GitHub.

For more details, see the [Contributing Guide](https://github.com/PiSkyEngine/pisky-engine/wiki/Contributing) in the project wiki.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

```text
MIT License

Copyright (c) 2025 Sly Technologies Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

## Links
- **Project Website**: [www.piengine.org](https://www.piengine.org)
- **GitHub Wiki**: [PiSkyEngine/pisky-engine Wiki](https://github.com/PiSkyEngine/pisky-engine/wiki)
- **Repository**: [PiSkyEngine/pisky-engine/pi-engine-util](https://github.com/PiSkyEngine/pisky-engine/tree/main/pi-engine-util)
- **Issue Tracker**: [GitHub Issues](https://github.com/PiSkyEngine/pisky-engine/issues)



---

### **Explanation of the `README.md`**

#### **Structure**
- **Header**: Includes the module name (`pi-engine-util`), version (`0.0.1-SNAPSHOT`), date (May 30, 2025, as per current date), and license (MIT).
- **Overview**: Describes the module’s purpose (utilities for concurrency, event handling, etc.), key features (`UpgradableReadWriteLock`, `Omnibus`, `Registration`), and its independence for use in other projects (e.g., jMonkeyEngine).
- **Installation**:
  - Provides Maven dependency snippet for `pi-engine-util`.
  - Lists requirements (Java 23 with `--enable-preview`, no dependencies for minimal footprint).
- **Usage Examples**:
  - **Example 1**: Demonstrates using `UpgradableReadWriteLock` with `Lockable` to manage read/write access to a shared `SceneNode`, including upgrading a read lock to a write lock. This showcases the concurrency utilities in a 3D graphics context.
    - Output: Shows reading and updating the `SceneNode`’s data safely.
  - **Example 2**: Shows using the `Omnibus` event bus to register a listener for a `GameEvent` and publish an event, demonstrating event handling and the `Registration` interface.
    - Output: Prints the event message when received.
- **Contributing**: Provides standard GitHub contribution steps, linking to a wiki page for detailed guidelines.
- **License**: Includes the MIT license text, replacing the previous Sly Technologies Free License to match the project’s license (as per prior discussions).
- **Links**:
  - Project website: `www.piengine.org`.
  - GitHub wiki: `https://github.com/PiSkyEngine/pisky-engine/wiki`.
  - Repository path: Assumed as `PiSkyEngine/pisky-engine/pi-engine-util`.
  - Issue tracker: Links to GitHub issues.

#### **Alignment with PI Engine**
- **Module Name**: Reflects the updated `pi-engine-util` name (singular, as decided).
- **Packages**: Uses `org.piengine.commons.util` (singular, matching the module name) and `org.piengine.commons.util.eventbus`, as per the specification.
- **Version**: Matches the project version (`0.0.1-SNAPSHOT`).
- **Java 23**: Noted in requirements, with `--enable-preview` for features like StructuredTaskScope and Virtual Threads.
- **Independence**: Highlighted for use in other projects, supporting your jMonkeyEngine game.

#### **Examples**
- **UpgradableReadWriteLock with Lockable**: Shows how to safely read and write to a shared resource (e.g., a scene node), a common use case in 3D graphics where concurrent access to scene data is critical. The upgrade feature demonstrates the lock’s advanced capability.
- **Omnibus Event Bus**: Demonstrates decoupled event handling, useful for game events (e.g., player actions), with `Registration` for cleanup.