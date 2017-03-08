package lx.photopicker.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.IOException;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.Pool;

/**
 * <b>从内存中加载图片的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoLoadTask extends Pool.Task
{
	private String mPath;
	private PhotoLoadCallback mListener;
	private RectF mLoadPercentRectF;
	private boolean isZipped = false;

	public PhotoLoadTask(PhotoEntity photo, PhotoLoadCallback listener)
	{
		this(photo.getPath(), null, listener);
	}

	public PhotoLoadTask(PhotoEntity photo, RectF loadRect, PhotoLoadCallback listener)
	{
		this(photo.getPath(), loadRect, listener);
	}

	public PhotoLoadTask(String path, PhotoLoadCallback listener)
	{
		this(path, null, listener);
	}

	public PhotoLoadTask(String path, RectF loadRect, PhotoLoadCallback listener)
	{
		this.mPath = path;
		this.mListener = listener;
		this.mLoadPercentRectF = loadRect;
	}

	public void upDatePath(String path)
	{
		this.mPath = path;
	}

	public void updateRect(RectF rectF)
	{
		this.mLoadPercentRectF = rectF;
	}

	public String getPath()
	{
		return mPath;
	}

	@Override
	protected void work()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mPath, options);
		options.inJustDecodeBounds = false;
		Runtime runtime = Runtime.getRuntime();
		if (mListener != null)
		{
			final int width = options.outWidth;
			final int height = options.outHeight;
			getHandler().post(new Runnable()
			{
				@Override
				public void run()
				{
					mListener.onLoadBitmapSize(width, height);

				}
			});
		}
		float availableSize = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) / 64.0f / 8.0f;
		if (mLoadPercentRectF == null)
		{
			loadFullBitmap(options, availableSize);
		} else
		{
			loadPartBitmap(options, availableSize);
		}

	}

	private void loadFullBitmap(BitmapFactory.Options options, float availableSize)
	{
		float percent = options.outHeight * 1.0f / options.outWidth;
		int availableW = (int) (availableSize / percent);
		int availableH = (int) (availableSize * percent);
		if (availableW > 1080)
			availableW = 1080;
		if (availableH > 1920)
			availableH = 1920;
		options.inSampleSize = calculateInSampleSize(options, availableW, availableH);
		isZipped = options.inSampleSize > 1.0f;
		final Bitmap bitmap = BitmapFactory.decodeFile(mPath, options);
		if (mListener != null)
		{
			getHandler().post(new Runnable()
			{
				@Override
				public void run()
				{
					mListener.onLoadFinished(bitmap, isZipped, mLoadPercentRectF);

				}
			});
		}
	}

	private void loadPartBitmap(BitmapFactory.Options options, float availableSize)
	{
		Rect rect = new Rect();
		rect.left = (int) (-mLoadPercentRectF.left * options.outWidth);
		rect.right = (int) (mLoadPercentRectF.right * options.outWidth);
		rect.top = (int) (-mLoadPercentRectF.top * options.outHeight);
		rect.bottom = (int) (mLoadPercentRectF.bottom * options.outHeight);
		float percent = rect.width() * 1.0f / rect.height();
		int availableW = (int) (availableSize / percent);
		if (availableW > 1920)
			availableW = 1920;
		int availableH = (int) (availableSize * percent);
		if (availableH > 1920)
			availableH = 1920;
		if (availableW <= 5 || availableH <= 5)
			return;
		options.inSampleSize = Math.max(rect.width() / availableW, rect.height() / availableH);
		if (options.inSampleSize < 1)
			options.inSampleSize = 1;
		isZipped = options.inSampleSize > 1.0f;
		BitmapRegionDecoder bitmapRegionDecoder = null;
		try
		{
			bitmapRegionDecoder = BitmapRegionDecoder.newInstance(mPath, true);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		final Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, options);
		if (mListener != null)
		{
			getHandler().post(new Runnable()
			{
				@Override
				public void run()
				{
					mListener.onLoadFinished(bitmap, isZipped, mLoadPercentRectF);

				}
			});
		}
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// 保存图片原宽高值
		final int height = options.outHeight;
		final int width = options.outWidth;
		// 初始化压缩比例为1
		int inSampleSize = 1;

		// 当图片宽高值任何一个大于所需压缩图片宽高值时,进入循环计算系统
		if (height > reqHeight || width > reqWidth)
		{

			final float halfHeight = height / 2.0f;
			final float halfWidth = width / 2.0f;
			// 压缩比例值每次循环两倍增加,
			// 直到原图宽高值的一半除以压缩值后都~大于所需宽高值为止
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
			{
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}
