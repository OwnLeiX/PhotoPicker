package lx.photopicker.task;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>保存图片的回调</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public interface PhotoSaveCallback {
    void onSaveFinished(PhotoEntity photo);
    void onSaveError(Throwable e);
}
