package lx.photopicker.task;

import android.graphics.Bitmap;

import java.io.FileOutputStream;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.Pool;

/**
 * <b>直接保存原图的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoSaveTask extends Pool.Task
{
	private Bitmap mBitmap;
	private PhotoSaveCallback mCallback;
	private String mPath;

	public PhotoSaveTask(Bitmap bitmap, String path, PhotoSaveCallback callback)
	{
		this.mBitmap = bitmap;
		this.mCallback = callback;
		this.mPath = path;
	}

	@Override
	protected void work()
	{
		try
		{
			int options = 100;
			FileOutputStream fos = new FileOutputStream(mPath);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, options, fos);
			fos.flush();
			fos.close();
			if (!mBitmap.isRecycled())
				mBitmap.recycle();
			mBitmap = null;
			final PhotoEntity photoEntity = new PhotoEntity(mPath);
			if (mCallback != null)
			{
				getHandler().post(new Runnable()
				{
					@Override
					public void run()
					{
						mCallback.onSaveFinished(photoEntity);

					}
				});
			}
		} catch (final Exception e)
		{
			if (mCallback != null)
			{
				getHandler().post(new Runnable()
				{
					@Override
					public void run()
					{
						mCallback.onSaveError(e);
					}
				});
			}
		}
	}
}
