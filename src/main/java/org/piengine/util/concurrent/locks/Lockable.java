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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import org.piengine.util.concurrent.locks.Locked.LockedSupport;

/**
 * The Interface Lockable.
 *
 * @param <R> the generic type
 * @param <W> the generic type
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Lockable<R extends Locked, W extends Locked> extends ReadWriteLock {

	/**
	 * The Class LockableReadWrite.
	 */
	class LockableReadWrite extends LockableSupport<LockedSupport, LockedSupport> {

		/**
		 * Instantiates a new lockable read write.
		 */
		public LockableReadWrite() {
			this(new UpgradableReadWriteLock());
		}

		/**
		 * Instantiates a new lockable read write.
		 *
		 * @param rwLock the rw lock
		 */
		public LockableReadWrite(ReadWriteLock rwLock) {
			super(rwLock, new LockedSupport(rwLock.readLock()), new LockedSupport(rwLock.writeLock()));
		}

	}

	/**
	 * The Class LockableSupport.
	 *
	 * @param <R> the generic type
	 * @param <W> the generic type
	 */
	class LockableSupport<R extends Locked, W extends Locked> implements Lockable<R, W>, ReadWriteLock {

		/**
		 * To string.
		 *
		 * @return the string
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "LockableSupport [rwLock=" + rwLock + "]";
		}

		/** The rw lock. */
		private final ReadWriteLock rwLock;
		
		/** The read locked. */
		private final Supplier<R> readLocked;
		
		/** The write locked. */
		private final Supplier<W> writeLocked;

		/**
		 * Instantiates a new lockable support.
		 *
		 * @param readLocked  the read locked
		 * @param writeLocked the write locked
		 */
		public LockableSupport(R readLocked, W writeLocked) {
			this(new UpgradableReadWriteLock(), () -> readLocked, () -> writeLocked);
		}

		/**
		 * Instantiates a new lockable support.
		 *
		 * @param rwLock      the rw lock
		 * @param readLocked  the read locked
		 * @param writeLocked the write locked
		 */
		public LockableSupport(ReadWriteLock rwLock, R readLocked, W writeLocked) {
			this(rwLock, () -> readLocked, () -> writeLocked);
		}

		/**
		 * Instantiates a new lockable support.
		 *
		 * @param rwLock      the rw lock
		 * @param readLocked  the read locked
		 * @param writeLocked the write locked
		 */
		public LockableSupport(ReadWriteLock rwLock, Supplier<R> readLocked, Supplier<W> writeLocked) {
			this.rwLock = rwLock;
			this.readLocked = readLocked;
			this.writeLocked = writeLocked;
		}

		/**
		 * Instantiates a new lockable support.
		 *
		 * @param readLocked  the read locked
		 * @param writeLocked the write locked
		 */
		public LockableSupport(Supplier<R> readLocked, Supplier<W> writeLocked) {
			this(new UpgradableReadWriteLock(), readLocked, writeLocked);
		}

		/**
		 * Lock for read.
		 *
		 * @return the r
		 * @throws InterruptedException the interrupted exception
		 * @see org.piengine.util.concurrent.locks.Lockable#lockForRead()
		 */
		@Override
		public R lockForRead() throws InterruptedException {
			rwLock.readLock().lock();

			return readLocked.get();
		}

		/**
		 * Lock for read.
		 *
		 * @param timeout the timeout
		 * @param unit    the unit
		 * @return the r
		 * @throws InterruptedException the interrupted exception
		 * @throws TimeoutException     the timeout exception
		 * @see org.piengine.util.concurrent.locks.Lockable#lockForRead(long,
		 *      java.util.concurrent.TimeUnit)
		 */
		@Override
		public R lockForRead(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
			if (!rwLock.readLock().tryLock(timeout, unit))
				throw new TimeoutException();

			return readLocked.get();
		}

		/**
		 * Lock for write.
		 *
		 * @return the w
		 * @throws InterruptedException the interrupted exception
		 * @see org.piengine.util.concurrent.locks.Lockable#lockForWrite()
		 */
		@Override
		public W lockForWrite() throws InterruptedException {
			rwLock.writeLock().lock();

			return writeLocked.get();
		}

		/**
		 * Lock for write.
		 *
		 * @param timeout the timeout
		 * @param unit    the unit
		 * @return the w
		 * @throws InterruptedException the interrupted exception
		 * @throws TimeoutException     the timeout exception
		 * @see org.piengine.util.concurrent.locks.Lockable#lockForWrite(long,
		 *      java.util.concurrent.TimeUnit)
		 */
		@Override
		public W lockForWrite(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
			if (!rwLock.writeLock().tryLock(timeout, unit))
				throw new TimeoutException();

			return writeLocked.get();
		}

		/**
		 * Read lock.
		 *
		 * @return the lock
		 * @see java.util.concurrent.locks.ReadWriteLock#readLock()
		 */
		@Override
		public Lock readLock() {
			return rwLock.readLock();
		}

		/**
		 * Write lock.
		 *
		 * @return the lock
		 * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
		 */
		@Override
		public Lock writeLock() {
			return rwLock.writeLock();
		}

	}

	/**
	 * Lock for read.
	 *
	 * @return the r
	 * @throws InterruptedException the interrupted exception
	 */
	R lockForRead() throws InterruptedException;

	/**
	 * Lock for read.
	 *
	 * @param timeout the timeout
	 * @param unit    the unit
	 * @return the r
	 * @throws InterruptedException the interrupted exception
	 * @throws TimeoutException     the timeout exception
	 */
	R lockForRead(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

	/**
	 * Lock for write.
	 *
	 * @return the w
	 * @throws InterruptedException the interrupted exception
	 */
	W lockForWrite() throws InterruptedException;

	/**
	 * Lock for write.
	 *
	 * @param timeout the timeout
	 * @param unit    the unit
	 * @return the w
	 * @throws InterruptedException the interrupted exception
	 * @throws TimeoutException     the timeout exception
	 */
	W lockForWrite(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

}
