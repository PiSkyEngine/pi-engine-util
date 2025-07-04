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

import java.util.concurrent.locks.Lock;

/**
 * The Interface Locked.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Locked extends AutoCloseable {

	/**
	 * The Class LockedSupport.
	 */
	public class LockedSupport implements Locked {
		
		/** The locked. */
		private final Locked locked;

		/**
		 * Instantiates a new locked support.
		 *
		 * @param locked the locked
		 */
		public LockedSupport(Locked locked) {
			this.locked = locked;
		}

		/**
		 * Instantiates a new locked support.
		 *
		 * @param lock the lock
		 */
		public LockedSupport(Lock lock) {
			this(() -> lock.unlock());
		}

		/**
		 * Unlock.
		 *
		 * @throws InterruptedException the interrupted exception
		 * @see org.piengine.util.concurrent.locks.Locked#unlock()
		 */
		@Override
		public void unlock() throws InterruptedException {
			locked.unlock();
		}
	}

	/**
	 * Close.
	 *
	 * @throws InterruptedException the interrupted exception
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	default void close() throws InterruptedException {
		unlock();
	}

	/**
	 * Unlock.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	void unlock() throws InterruptedException;
}
