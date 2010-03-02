package com.shadowolf.storage;

import java.util.concurrent.locks.ReentrantLock;

public class WriteLockedStorage {
	public ReentrantLock lock = new ReentrantLock();
	
	public void getLock() {
		if(this.lock.isHeldByCurrentThread()) {
			return;
		}
		
		this.lock.lock();
	}
	
	public void releaseLock() {
		if(this.lock.isHeldByCurrentThread()) {
			this.lock.unlock();
		}
	}
}
