package lx.photopicker.util;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池+等待队列管理工具
 */
public class Pool
{

	private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();// 线程池大小
	private volatile static int count = 0;// 工作线程数
	private static ThreadPoolExecutor THREAD_POOL = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);// 线程池
	private static LinkedList<Task> TASK_QUEUE = new LinkedList<>();// 等待队列

	/**
	 * 执行任务
	 * @param task
	 * @return 是否执行，否：放入等待队列
	 */
	public static boolean execute(Task task)
	{
		boolean isExecute = false;
		if (count < POOL_SIZE)
		{
			count++;
			isExecute = true;
			THREAD_POOL.execute(task);
		} else
		{
			// 排队
			TASK_QUEUE.addLast(task);
		}
		return isExecute;
	}

	/**
	 * 删除等待任务
	 * @param id
	 * @return
	 */
	public static boolean cancelWaitTask(long id)
	{
		boolean isCancel = false;

		Task target = null;
		for (Task item : TASK_QUEUE)
		{
			if (item.id == id)
			{
				target = item;
				break;
			}
		}
		if (target != null)
		{
			TASK_QUEUE.remove(target);
			isCancel = true;
		}
		return isCancel;
	}

	public static void destroy()
	{
		TASK_QUEUE.clear();
		THREAD_POOL.shutdownNow();
	}

	/**
	 * 线程池中执行的任务
	 */
	public static abstract class Task implements Runnable
	{
		// 使用id去标识任务，当存在排队等待的任务时可以依据id进行删除操作。
		public long id;
		private boolean isCompleted = false;
		protected Handler mHandler;

		@Override
		public void run()
		{
			isCompleted = false;
			// 耗时工作
			work();
			// 完成任务后通知主线程，线程池有一个空余线程,并试图从等待队列中获取下载任务
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					count--;
					Task first = TASK_QUEUE.pollFirst();
					if (first != null)
					{
						execute(first);
					}
				}
			});
			isCompleted = true;
		}

		protected abstract void work();

		public boolean isCompleted()
		{
			return isCompleted;
		}

		protected Handler getHandler()
		{
			if (mHandler == null)
				mHandler = new Handler(Looper.getMainLooper());
			return mHandler;
		}
	}

}
