package lx.photopicker.task;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.Pool;

/**
 * <b>压缩保存图片的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoCondenseTask extends Pool.Task
{
	private Bitmap mBitmap;
	private PhotoSaveCallback mCallback;
	private String mPath;

	public PhotoCondenseTask(Bitmap bitmap, String path, PhotoSaveCallback callback)
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int options = 100;
			mBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
			while (baos.toByteArray().length / 1024 >= 200 && options > 0)
			{
				baos.reset();
				options -= 10;
				if (options < 0)
					options = 0;
				mBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
			}
			if (!mBitmap.isRecycled())
				mBitmap.recycle();
			mBitmap = null;
			FileOutputStream fos = new FileOutputStream(mPath);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
			baos.close();
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
