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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * A high-performance read-write lock with support for upgrading from read to
 * write lock, optimized for virtual threads in a multi-threaded 3D scene graph.
 * Prevents deadlocks when multiple threads upgrade by allowing upgrades when
 * all readers are upgraders or no readers remain. Used for locking juncture
 * nodes (e.g., physics, audio, geometry).
 */
public class UpgradableReadWriteLock implements ReadWriteLock {
	
	/**
	 * The Class ReadLock.
	 */
	private class ReadLock implements Lock {
		
		/**
		 * Lock.
		 *
		 * @see java.util.concurrent.locks.Lock#lock()
		 */
		@Override
		public void lock() {
			Thread current = Thread.currentThread();
			rwLock.readLock().lock();
			readHolds.compute(current, (k, v) -> (v == null) ? 1 : v + 1);
			readerCount.incrementAndGet();
		}

		/**
		 * Lock interruptibly.
		 *
		 * @throws InterruptedException the interrupted exception
		 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
		 */
		@Override
		public void lockInterruptibly() throws InterruptedException {
			Thread current = Thread.currentThread();
			rwLock.readLock().lockInterruptibly();
			readHolds.compute(current, (k, v) -> (v == null) ? 1 : v + 1);
			readerCount.incrementAndGet();
		}

		/**
		 * New condition.
		 *
		 * @return the condition
		 * @see java.util.concurrent.locks.Lock#newCondition()
		 */
		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("Conditions not supported");
		}

		/**
		 * Try lock.
		 *
		 * @return true, if successful
		 * @see java.util.concurrent.locks.Lock#tryLock()
		 */
		@Override
		public boolean tryLock() {
			Thread current = Thread.currentThread();
			if (rwLock.readLock().tryLock()) {
				readHolds.compute(current, (k, v) -> (v == null) ? 1 : v + 1);
				readerCount.incrementAndGet();
				return true;
			}
			return false;
		}

		/**
		 * Try lock.
		 *
		 * @param time the time
		 * @param unit the unit
		 * @return true, if successful
		 * @throws InterruptedException the interrupted exception
		 * @see java.util.concurrent.locks.Lock#tryLock(long,
		 *      java.util.concurrent.TimeUnit)
		 */
		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			Thread current = Thread.currentThread();
			if (rwLock.readLock().tryLock(time, unit)) {
				readHolds.compute(current, (k, v) -> (v == null) ? 1 : v + 1);
				readerCount.incrementAndGet();
				return true;
			}
			return false;
		}

		/**
		 * Unlock.
		 *
		 * @see java.util.concurrent.locks.Lock#unlock()
		 */
		@Override
		public void unlock() {
			Thread current = Thread.currentThread();
			Integer holds = readHolds.get(current);
			if (holds == null || holds == 0) {
				throw new IllegalMonitorStateException("Thread does not hold read lock");
			}

			if (holds == 1) {
				readHolds.remove(current);
			} else {
				readHolds.put(current, holds - 1);
			}
			readerCount.decrementAndGet();
			rwLock.readLock().unlock();
			// Signal upgraders if no readers remain or all are upgraders
			if (readerCount.get() == 0 || readHolds.keySet().stream().allMatch(upgraders::containsKey)) {
				upgradeLock.lock();
				try {
					upgradeCondition.signalAll();
				} finally {
					upgradeLock.unlock();
				}
			}
		}
	}

	/**
	 * The Class WriteLock.
	 */
	private class WriteLock implements Lock {
		
		/**
		 * Lock.
		 *
		 * @see java.util.concurrent.locks.Lock#lock()
		 */
		@Override
		public void lock() {
			Thread current = Thread.currentThread();
			boolean isReader = isReadLockHeldByCurrentThread();
			Integer holds = isReader ? readHolds.get(current) : 0;

			if (isReader) {
				upgraders.put(current, true);
				try {
					// Release read lock to allow write lock acquisition
					for (int i = 0; i < holds; i++) {
						rwLock.readLock().unlock();
					}
					readerCount.addAndGet(-holds);
					readHolds.remove(current);

					// Coordinate with other readers
					while (readerCount.get() > 0 && !readHolds.keySet().stream().allMatch(upgraders::containsKey)) {
						upgradeLock.lock();
						try {
							upgradeCondition.await();
						} finally {
							upgradeLock.unlock();
						}
					}

					// Acquire write lock
					rwLock.writeLock().lock();

					// Reacquire read lock to maintain state
					for (int i = 0; i < holds; i++) {
						rwLock.readLock().lock();
					}
					readerCount.addAndGet(holds);
					readHolds.put(current, holds);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					upgraders.remove(current);
					throw new RuntimeException(e);
				}
				upgraders.remove(current);
			} else {
				rwLock.writeLock().lock();
			}
		}

		/**
		 * Lock interruptibly.
		 *
		 * @throws InterruptedException the interrupted exception
		 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
		 */
		@Override
		public void lockInterruptibly() throws InterruptedException {
			Thread current = Thread.currentThread();
			boolean isReader = isReadLockHeldByCurrentThread();
			Integer holds = isReader ? readHolds.get(current) : 0;

			if (isReader) {
				upgraders.put(current, true);
				try {
					for (int i = 0; i < holds; i++) {
						rwLock.readLock().unlock();
					}
					readerCount.addAndGet(-holds);
					readHolds.remove(current);

					while (readerCount.get() > 0 && !readHolds.keySet().stream().allMatch(upgraders::containsKey)) {
						upgradeLock.lock();
						try {
							upgradeCondition.await();
						} finally {
							upgradeLock.unlock();
						}
					}

					rwLock.writeLock().lockInterruptibly();

					for (int i = 0; i < holds; i++) {
						rwLock.readLock().lock();
					}
					readerCount.addAndGet(holds);
					readHolds.put(current, holds);
				} finally {
					upgraders.remove(current);
				}
			} else {
				rwLock.writeLock().lockInterruptibly();
			}
		}

		/**
		 * New condition.
		 *
		 * @return the condition
		 * @see java.util.concurrent.locks.Lock#newCondition()
		 */
		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("Conditions not supported");
		}

		/**
		 * Try lock.
		 *
		 * @return true, if successful
		 * @see java.util.concurrent.locks.Lock#tryLock()
		 */
		@Override
		public boolean tryLock() {
			Thread current = Thread.currentThread();
			boolean isReader = isReadLockHeldByCurrentThread();
			Integer holds = isReader ? readHolds.get(current) : 0;

			if (isReader) {
				if (readerCount.get() > holds || (readerCount.get() > 0 && !readHolds.keySet().stream().allMatch(
						upgraders::containsKey))) {
					return false;
				}
				upgraders.put(current, true);
				boolean acquired = rwLock.writeLock().tryLock();
				upgraders.remove(current);
				return acquired;
			}
			return rwLock.writeLock().tryLock();
		}

		/**
		 * Try lock.
		 *
		 * @param time the time
		 * @param unit the unit
		 * @return true, if successful
		 * @throws InterruptedException the interrupted exception
		 * @see java.util.concurrent.locks.Lock#tryLock(long,
		 *      java.util.concurrent.TimeUnit)
		 */
		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			Thread current = Thread.currentThread();
			boolean isReader = isReadLockHeldByCurrentThread();
			Integer holds = isReader ? readHolds.get(current) : 0;

			if (isReader) {
				upgraders.put(current, true);
				long deadline = System.nanoTime() + unit.toNanos(time);
				try {
					while (readerCount.get() > holds || (readerCount.get() > 0 && !readHolds.keySet().stream().allMatch(
							upgraders::containsKey))) {
						long remaining = deadline - System.nanoTime();
						if (remaining <= 0) {
							upgraders.remove(current);
							return false;
						}
						upgradeLock.lock();
						try {
							upgradeCondition.await(remaining, TimeUnit.NANOSECONDS);
						} finally {
							upgradeLock.unlock();
						}
					}
					boolean acquired = rwLock.writeLock().tryLock(time, TimeUnit.NANOSECONDS);
					upgraders.remove(current);
					return acquired;
				} catch (Throwable t) {
					upgraders.remove(current);
					throw t;
				}
			}
			return rwLock.writeLock().tryLock(time, unit);
		}

		/**
		 * Unlock.
		 *
		 * @see java.util.concurrent.locks.Lock#unlock()
		 */
		@Override
		public void unlock() {
			if (!rwLock.isWriteLockedByCurrentThread()) {
				throw new IllegalMonitorStateException("Thread does not hold write lock");
			}
			rwLock.writeLock().unlock();
			// Signal upgraders using dedicated lock
			upgradeLock.lock();
			try {
				upgradeCondition.signalAll();
			} finally {
				upgradeLock.unlock();
			}
		}
	}

	/** The rw lock. */
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true); // fair mode
	
	/** The read lock. */
	private final Lock readLock = new ReadLock();

	/** The write lock. */
	private final Lock writeLock = new WriteLock();

	/** The reader count. */
	// Track total readers
	private final java.util.concurrent.atomic.AtomicInteger readerCount = new java.util.concurrent.atomic.AtomicInteger(
			0);

	/** The read holds. */
	// Track per-thread reentrant read counts
	private final ConcurrentHashMap<Thread, Integer> readHolds = new ConcurrentHashMap<>();

	/** The upgraders. */
	// Track threads attempting to upgrade
	private final ConcurrentHashMap<Thread, Boolean> upgraders = new ConcurrentHashMap<>();
	
	/** The upgrade lock. */
	// Dedicated lock and condition for upgrade coordination
	private final ReentrantLock upgradeLock = new ReentrantLock();

	/** The upgrade condition. */
	private final Condition upgradeCondition = upgradeLock.newCondition();

	/**
	 * Checks if the current thread holds a read lock.
	 *
	 * @return true, if is read lock held by current thread
	 */
	public boolean isReadLockHeldByCurrentThread() {
		return readHolds.containsKey(Thread.currentThread());
	}

	/**
	 * Read lock.
	 *
	 * @return the lock
	 * @see java.util.concurrent.locks.ReadWriteLock#readLock()
	 */
	@Override
	public Lock readLock() {
		return readLock;
	}

	/**
	 * The Class DbgLockToString.
	 */
	private static class DbgLockToString {

		/** The Constant DBG_PATT. */
		private static final Pattern DBG_PATT = Pattern.compile(""
				+ "(Write locks = \\d+, Read locks = \\d+)"
				+ "");

		/**
		 * Extract dbg str.
		 *
		 * @param lockToString the lock to string
		 * @return the string
		 */
		private static String extractDbgStr(String lockToString) {
			var match = DBG_PATT.matcher(lockToString);

			if (match.find())
				return match.group(1);

			return "";
		}

		/**
		 * To dbg str.
		 *
		 * @param writeCount the write count
		 * @param readCount  the read count
		 * @return the string
		 */
		private static String toDbgStr(int writeCount, int readCount) {

			return "Write locks = %d, Read locks = %d"
					.formatted(writeCount, readCount);
		}

	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		var dbgStr = DbgLockToString.extractDbgStr(rwLock.toString());

		return "UpgradableReadWriteLock [" + dbgStr + "]";
	}

	/**
	 * Write lock.
	 *
	 * @return the lock
	 * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
	 */
	@Override
	public Lock writeLock() {
		return writeLock;
	}
}