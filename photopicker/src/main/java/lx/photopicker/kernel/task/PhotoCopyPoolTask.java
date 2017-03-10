package lx.photopicker.kernel.task;

import android.graphics.Bitmap;

import java.io.FileOutputStream;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.kernel.callback.PhotoCopyCallback;
import lx.photopicker.util.Pool;

/**
 * <b>直接保存原图的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoCopyPoolTask extends Pool.PoolTask
{
	private Bitmap mBitmap;
	private PhotoCopyCallback mCallback;
	private String mPath;
	private PhotoEntity mOldPhoto;

	public PhotoCopyPoolTask(Bitmap bitmap, String path, PhotoEntity oldPhoto,PhotoCopyCallback callback)
	{
		this.mBitmap = bitmap;
		this.mCallback = callback;
		this.mPath = path;
		this.mOldPhoto = oldPhoto;
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
			final PhotoEntity photoEntity = new PhotoEntity(mPath);
			photoEntity.setPicked(mOldPhoto.isPicked());
			if (mCallback != null)
			{
				getHandler().post(new Runnable()
				{
					@Override
					public void run()
					{
						mCallback.onCopyFinished(photoEntity,mOldPhoto);

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
						mCallback.onCopyError(e);
					}
				});
			}
		}
	}
}
