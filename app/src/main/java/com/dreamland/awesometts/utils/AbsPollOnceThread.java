package com.dreamland.awesometts.utils;

public abstract class AbsPollOnceThread extends Thread {

	public AbsPollOnceThread() {
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(Runnable runnable) {
		super(runnable);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(ThreadGroup group, String threadName) {
		super(group, threadName);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public AbsPollOnceThread(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
		super(group, runnable, threadName, stackSize);
		// TODO Auto-generated constructor stub
	}
	
	
	
	private boolean isRunning = true;
	private final Object lock = new Object();
	
	public void exit(){
		synchronized(lock){
			isRunning = false;
			lock.notify();
		}
		
	}
	
	public void pollOnce(){
		synchronized(lock){
			lock.notify();
		}
	}
	
	protected boolean isRunning(){
		return isRunning;
	}
	
	public void run(){
		ready();
		while(isRunning){
			synchronized(lock){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(!isRunning){
					break;
				}
			}
			pollOnceWork();
		}
	}
	
   abstract protected void pollOnceWork();
   abstract protected void ready();
}
