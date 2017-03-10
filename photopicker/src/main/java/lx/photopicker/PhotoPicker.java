package lx.photopicker;

import android.app.Activity;
import android.content.Intent;

import lx.photopicker.ui.activity.FolderListActivity;
import lx.photopicker.kernel.PickerManager;

/**
 * <b>模块主入口</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoPicker
{
	public static void pickPhoto(Activity activity, PhotoParams params, PickerCallback callback)
	{
		if (params != null)
		{
			PickerManager.init(activity,callback,params);
		}else {
			PickerManager.init(activity,callback,new PhotoParams.Builder().create());
		}
		Intent intent = new Intent(activity, FolderListActivity.class);
        activity.startActivity(intent);
	}
}
