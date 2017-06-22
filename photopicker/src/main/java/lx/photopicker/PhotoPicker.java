package lx.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.kernel.PickerManager;
import lx.photopicker.ui.activity.FolderListActivity;
import lx.photopicker.ui.activity.PhotoClipActivity;

/**
 * <b>模块主入口</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoPicker {
    public static void pickPhoto(Context activity, PhotoParams params, PickerCallback callback) {
        if (params != null) {
            PickerManager.init(callback, params);
        } else {
            PickerManager.init(callback, new PhotoParams.Builder().create());
        }
        Intent intent = new Intent(activity, FolderListActivity.class);
        if (!(activity instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void clipPhoto(Context context, String path, PhotoParams params, PickerCallback callback) {
        if (params != null) {
            PickerManager.init(callback, params);
        } else {
            PickerManager.init(callback, new PhotoParams.Builder().create());
        }
        PhotoEntity photoEntity = new PhotoEntity(path);
        photoEntity.setPicked(true);
        PickerManager.$().setCurrentPhoto(photoEntity);
        Intent intent = new Intent(context, PhotoClipActivity.class);
        intent.putExtra(PhotoClipActivity.KEY_JUST_CLIP,true);
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
