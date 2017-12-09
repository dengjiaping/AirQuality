package com.honeywell.net.core;

import java.util.ArrayList;
import java.util.LinkedList;

import com.honeywell.net.utils.Logger;

/**
 ******************************************************************
 * 文件名称: TubeThreadPool.java</Br>
 * 文件描述: 线程池类</Br>
 * 设计模式: </Br>
 * 版权声明: </Br>
 * 修改历史: 2016-03-15 1.00 初始版本</Br>
 ***************************************************************** 
 * @hide
 */
public final class TubeThreadPool extends ThreadGroup {
	// TAG
	private final static String TAG = "TubeThreadPool";

	/**
	 * 线程池是否关闭
	 */
	private boolean isClosed = false;

	/**
	 * 工作队列
	 */
	private LinkedList<Runnable> workQueue = new LinkedList<Runnable>();

	/**
	 * 线程池的id
	 */
	private static final String POOL_ID = "TubeThreadPool";

	/**
	 * 线程池实例
	 */
	private static TubeThreadPool mDownQueue = null;

	/**
	 * 工作线程列表
	 */
	private ArrayList<WorkThread> workThreadList = new ArrayList<WorkThread>();

	/**
	 * 初始化线程池
	 * 
	 * @param poolSize
	 *            线程池种的线程数量
	 */
	private TubeThreadPool(int poolSize) {

		super(POOL_ID);
		setDaemon(true);// 线程池守护
		for (int i = 0; i < poolSize; i++) {
			// 创建并启动工作线程
			WorkThread workThread = new WorkThread(i);
			workThreadList.add(workThread);
			workThread.start();
		}
		Logger.d(TAG, "TubeThreadPool is working!");
	}

	public static TubeThreadPool getInstance(int count) {
		if (mDownQueue == null) {
			mDownQueue = new TubeThreadPool(count);
		}
		return mDownQueue;
	}
	
	public static TubeThreadPool create(int count){
	     return new TubeThreadPool(count);
	}

	/**
	 * 向线程池中增加任务
	 * 
	 * @param task
	 */
	public synchronized void execute(Runnable task) {
		if (isClosed) {
			throw new IllegalStateException();
		}
		if (task != null) {
			workQueue.add(task);
			notifyAll();
		}
	}

	/**
	 * 在线程池中取出一个任务并执行
	 * 
	 * @param threadid
	 * @return
	 * @throws InterruptedException
	 */
	private synchronized Runnable getTask(int threadid)
			throws InterruptedException {
		while (workQueue.size() == 0) {
			if (isClosed) {
				return null;
			}
			wait(); // 如果工作队列中没有任务,就等待任务
		}

		return workQueue.removeFirst();
	}

	/**
	 * 关闭线程池
	 */
	public synchronized void closePool() {
		if (!isClosed) {
			waitFinish();
			isClosed = true;
			workQueue.clear();
			interrupt();
		}
	}

	/**
	 * 等待所有任务执行完毕
	 */
	public void waitFinish() {
		synchronized (this) {
			isClosed = true;
			notifyAll();// 唤醒所有还在getTask()方法中等待任务的工作线程
		}

		Thread[] threads = new Thread[activeCount()]; // activeCount()
														// 返回该线程组中活动线程的估计值。
		int count = enumerate(threads); // enumerate()方法继承自ThreadGroup类，根据活动线程的估计值获得线程组中当前所有活动的工作线程
		for (int i = 0; i < count; i++) { // 等待所有工作线程结束
			threads[i].interrupt();// 直接打断工作线程
		}
	}

	/**
	 * 内部类,工作线程,负责从工作队列中取出任务,并执行
	 * 
	 * @author hanwei
	 * 
	 */
	private class WorkThread extends Thread {
		private int id;
		private Runnable task = null;

		public WorkThread(int id) {
			super(TubeThreadPool.this, "TubeThread" + id);
			this.id = id;
			Logger.d(TAG, "---creat WorkThread id=" + id);
		}

		public void run() {
			while (!isInterrupted()) {
				try {
					task = getTask(id);
				} catch (InterruptedException ex) {
					Logger.d(TAG, ex.getLocalizedMessage());
				}

				if (task == null) {
					return;
				}

				try {
					task.run();
				} catch (Throwable t) {
					Logger.d(TAG, t.getLocalizedMessage());
				}

				task = null;
			}
		}
	}
}
