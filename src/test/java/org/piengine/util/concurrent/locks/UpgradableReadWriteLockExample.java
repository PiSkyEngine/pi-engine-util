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
package org.piengine.util.concurrent.locks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Example usage of UpgradableReadWriteLock, demonstrating read-to-write upgrades,
 * write-to-read downgrades, and multiple threads upgrading without deadlock.
 */
public class UpgradableReadWriteLockExample {
	
	/**
	 * Println.
	 *
	 * @param str the str
	 */
	private static void println(String str) {
		String thread = Long.toHexString(Thread.currentThread().threadId() & 0xFF);
		System.out.println("T" + thread + ":" + str);
	}
	
    /**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws InterruptedException the interrupted exception
	 */
    public static void main(String[] args) throws InterruptedException {
        UpgradableReadWriteLock rwLock = new UpgradableReadWriteLock();

        // Example 1: Read to Write upgrade
        println("Example 1: Read to Write upgrade");
        rwLock.readLock().lock();
        try {
            println("Acquired read lock");
            rwLock.writeLock().lock();
            try {
                println("Upgraded to write lock (now holding both)");
                rwLock.readLock().unlock();
                println("Released read lock, now only holding write lock");
            } finally {
                rwLock.writeLock().unlock();
                println("Released write lock");
            }
        } finally {
            if (rwLock.isReadLockHeldByCurrentThread()) {
                rwLock.readLock().unlock();
                println("Released read lock");
            }
        }

        // Example 2: Write to Read downgrade
        println("\nExample 2: Write to Read downgrade");
        rwLock.writeLock().lock();
        try {
            println("Acquired write lock");
            rwLock.readLock().lock();
            try {
                println("Acquired read lock (now holding both)");
                rwLock.writeLock().unlock();
                println("Released write lock, now only holding read lock");
            } finally {
                println("Attempting to release read lock");
                rwLock.readLock().unlock();
                println("Released read lock");
            }
        } finally {
            // Already unlocked
        }

        // Example 3: Multiple threads attempting upgrades
        println("\nExample 3: Multiple threads attempting upgrades");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Runnable task = () -> {
                String name = Thread.currentThread().getName();
                rwLock.readLock().lock();
                try {
                    println(name + ": Acquired read lock");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    rwLock.writeLock().lock();
                    try {
                        println(name + ": Upgraded to write lock");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        rwLock.writeLock().unlock();
                        println(name + ": Released write lock");
                    }
                } finally {
                    if (rwLock.isReadLockHeldByCurrentThread()) {
                        rwLock.readLock().unlock();
                        println(name + ": Released read lock");
                    }
                }
            };

            var t1 = executor.submit(task);
            Thread.sleep(10);
            var t2 = executor.submit(task);
            Thread.sleep(10);
            var t3 = executor.submit(task);

            try {
                t1.get();
                t2.get();
                t3.get();
            } catch (ExecutionException e) {
                System.err.println("Task failed: " + e.getCause());
                throw new RuntimeException(e.getCause());
            }
        }
    }

}