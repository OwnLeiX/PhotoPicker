package lx.photopicker.kernel;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lx.photopicker.PhotoParams;
import lx.photopicker.PickerCallback;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.entity.PhotoFolderEntity;
import lx.photopicker.kernel.callback.DoCallbackCallback;
import lx.photopicker.kernel.callback.ScanCallback;
import lx.photopicker.kernel.callback.SizeClipCallback;
import lx.photopicker.kernel.task.FileDeletePoolTask;
import lx.photopicker.kernel.task.ScanPoolTask;
import lx.photopicker.kernel.task.SizeClipPoolTask;
import lx.photopicker.util.Pool;

/**
 * <b>Photo处理相关的类</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PickerManager implements ScanCallback, SizeClipCallback
{
	private static PickerManager mInstance;//单一实例

	private ScanPoolTask mTask;//扫描任务
	private ScanCallback mScanCallback;//扫描回调
	private PickerCallback mPickerCallback;//选择完毕的回调
	private DoCallbackCallback mDoCallbackCallback;
	private PhotoParams mPhotoParams;//本次Pick的Photo的规格
	private String mCachedDir;

	public volatile PhotoFolderEntity mCurrentFolder;//当前选中的Folder
	public volatile PhotoEntity mCurrentPhoto;//当前选中的Photo
	public List<PhotoFolderEntity> mFolders;//扫描出来的所有的Folder
	public List<PhotoEntity> mPickedPhotos = new ArrayList<>();//已选择的Photo的列表

	/**
	 * 初始化
	 * @param params 需要的图片类型（单张 | 多张） （原图 | 裁剪）
	 * @param callback 获取选取完毕后的图片的回调
	 * */
	public static void init(Activity activity, PickerCallback callback, PhotoParams params)
	{
		mInstance = new PickerManager(callback, params);
		mInstance.mCachedDir = activity.getCacheDir().getAbsolutePath() + "/";
	}

	/**
	 * 销毁自己
	 * */
	private static void destroy()
	{
		if (mInstance != null)
			mInstance.cancel();
		mInstance = null;//将static变量置为null，对象内成员变量皆不可达。
	}

	private PickerManager(PickerCallback callback, PhotoParams params)
	{
		mPickerCallback = callback;
		mPhotoParams = params;
		mInstance = this;
	}

	public static PickerManager $()
	{
		if (mInstance == null)
		{
			synchronized (PickerManager.class)
			{
				if (mInstance == null)
					mInstance = new PickerManager(null, new PhotoParams.Builder().create());
			}
		}
		return mInstance;
	}

	/**
	 * 查找当前手机的图片
	 * @param context 使用内容提供者需要用到的上下文
	 * @param callback 查找完成后的回调
	 * */
	public void scan(Context context, ScanCallback callback)
	{
		mScanCallback = callback;
		mTask = new ScanPoolTask(context, this);
		Pool.execute(mTask);
	}

	/**
	 * 没太大用 取消查找任务
	 * */
	private void cancel()
	{
		if (mTask != null && !mTask.isCompleted())
			Pool.cancelWaitTask(mTask.id);
	}

	/**
	 * 查找成功的回调，记录查找结果，并回调给外部
	 * */
	@Override
	public void onScanFinished(List<PhotoFolderEntity> list)
	{
		mFolders = list;
		mScanCallback.onScanFinished(list);
	}

	/**
	 * 查找失败的回调，回调给外部
	 * */
	@Override
	public void onScanError(Throwable e)
	{
		mScanCallback.onScanError(e);
	}

	/**
	 * 查找成功，但是没有记录的回调，回调给外部
	 * */
	@Override
	public void onScanEmpty()
	{
		mScanCallback.onScanEmpty();
	}

	/**
	 * 本次PhotoPick完成调用的方法，会将结果返回给初始化时传入的Callback，并且销毁掉自己
	 * @return 是否需要等待处理图片 否 true | 是 false
	 * */
	public boolean doCallback(DoCallbackCallback callbackCallback)
	{
		mDoCallbackCallback = callbackCallback;
		if (mPickerCallback != null)
		{
			if (mPickedPhotos != null && mPickedPhotos.size() > 0)
			{
				Pool.execute(new SizeClipPoolTask(mPickedPhotos, mCachedDir, mPhotoParams,this));//修正所有选中图片的尺寸
				return false;
			} else
			{
				try
				{
					mPickerCallback.onCancel();//取消
					return true;
				} finally
				{
					PickerManager.destroy();//销毁自己，避免内存泄漏
				}
			}
		} else
		{
			PickerManager.destroy();//销毁自己，避免内存泄漏
			return true;
		}

	}

	/**
	 * 获取所有Photo文件夹的方法
	 * @return 查找到的所有文件夹
	 * */
	public List<PhotoFolderEntity> getAllFolders()
	{
		return mFolders;
	}

	/**
	 * 获取已选择的Photo的方法
	 * @return 已选择的Photos
	 * */
	public List<PhotoEntity> getPickedPhotos()
	{
		return mPickedPhotos;
	}

	/**
	 * 获取已选择张数的方法
	 * @return 已经pick的Photo的数量
	 * */
	public int getPickedCount()
	{
		return mPickedPhotos.size();
	}

	/**
	 * 将Photo添加至已选择列表的方法
	 * */
	public void addPickedPhoto(PhotoEntity photo)
	{
		if (mPickedPhotos.contains(photo))
			return;
		photo.setPicked(true);
		mPickedPhotos.add(photo);
	}

	/**
	 * 将Photo从已选择列表移除的方法
	 * */
	public void removePickedPhoto(PhotoEntity photo)
	{
		if (mPickedPhotos.remove(photo))
			photo.setPicked(false);
		if (photo.getPath().startsWith(mCachedDir))
		{
			File file = new File(photo.getPath());
			if (file.exists())
				file.delete();
		}
	}

	/**
	 * (可能需要改进)设置当前将要预览的PhotoFolder的方法
	 * */
	public void setCurrentFolder(PhotoFolderEntity folder)
	{
		this.mCurrentFolder = folder;
	}

	/**
	 * (可能需要改进)获取当前将要预览的PhotoFolder的方法
	 * @return 当前需要预览的PhotoFolder
	 * */
	public PhotoFolderEntity getCurrentFolder()
	{
		return mCurrentFolder;
	}

	/**
	 * (可能需要改进)获取当前将要预览的Photo的方法
	 * @return 当前需要预览的Photo
	 * */
	public PhotoEntity getCurrentPhoto()
	{
		return mCurrentPhoto;
	}

	/**
	 * (可能需要改进)设置当前将要预览的Photo的方法
	 * */
	public void setCurrentPhoto(PhotoEntity mCurrentPhoto)
	{
		this.mCurrentPhoto = mCurrentPhoto;
	}

	/**
	 * 获取本次Pick的Photo的参数的方法
	 * @return 本次Pick的Photo规格
	 * */
	public PhotoParams getPhotoParams()
	{
		return mPhotoParams;
	}

	/**
	 * (取消时使用)清楚所有已选择的Photo的方法
	 * */
	public void clearPickedPhoto()
	{
		List<PhotoEntity> list = new ArrayList<>(mPickedPhotos);
		mPickedPhotos.clear();
		FileDeletePoolTask fileDeletePoolTask = new FileDeletePoolTask(list, mCachedDir);
		Pool.execute(fileDeletePoolTask);
	}

	/**
	 * 最后的裁剪图片尺寸任务的回调
	 * */
	@Override
	public void onSizeClipFinished(List<PhotoEntity> photos)
	{
		if (mPickerCallback != null)
			mPickerCallback.onPicked(photos);
		if (mDoCallbackCallback != null)
			mDoCallbackCallback.onCompleted();
		PickerManager.destroy();//销毁自己，避免内存泄漏
	}
}
