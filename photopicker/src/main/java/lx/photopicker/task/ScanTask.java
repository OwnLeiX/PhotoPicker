package lx.photopicker.task;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.entity.PhotoFolderEntity;
import lx.photopicker.util.Pool;

/**
 * <b>扫描所有Photo的任务</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class ScanTask extends Pool.Task
{
	public static final String CAMERA = "Camera";
	public static final String SCREEN_SHOT = "ScreenShot";
	public static final String OTHER = "Other";

	private ScanCallback mCallback;
	private Context mContext;

	public ScanTask(Context context, ScanCallback callback)
	{
		this.mCallback = callback;
		this.mContext = context.getApplicationContext();
	}

	@Override
	protected void work()
	{
		try
		{
			List<String> photoPaths = new ArrayList<>();
			Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, "date_added DESC");
			while (cursor.moveToNext())
			{
				photoPaths.add(cursor.getString(cursor.getColumnIndex("_data")));
			}
			cursor.close();

			if (photoPaths.size() <= 0)
			{
				if (mCallback != null)
				{
					getHandler().post(new Runnable()
					{
						@Override
						public void run()
						{
							mCallback.onScanEmpty();
						}
					});
				}
				return;
			}

			String path;
			final List<PhotoFolderEntity> photoFolderEntities = new ArrayList<>();
			ArrayList<PhotoEntity> cameras = new ArrayList<>();
			ArrayList<PhotoEntity> screenshots = new ArrayList<>();
			ArrayList<PhotoEntity> others = new ArrayList<>();
			for (int i = 0; i < photoPaths.size(); i++)
			{
				path = photoPaths.get(i);
				if (path.contains("Camera"))
				{
					cameras.add(new PhotoEntity(path));
				} else if (path.contains("Screenshots"))
				{
					screenshots.add(new PhotoEntity(path));
				} else
				{
					others.add(new PhotoEntity(path));
				}
			}
			if (cameras.size() > 0)
				photoFolderEntities.add(new PhotoFolderEntity(cameras, CAMERA));
			if (screenshots.size() > 0)
				photoFolderEntities.add(new PhotoFolderEntity(screenshots, SCREEN_SHOT));
			if (others.size() > 0)
				photoFolderEntities.add(new PhotoFolderEntity(others, OTHER));
			if (mCallback != null)
			{
				getHandler().post(new Runnable()
				{
					@Override
					public void run()
					{
						mCallback.onScanFinished(photoFolderEntities);
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
						mCallback.onScanError(e);
					}
				});
			}
		}
	}
}
