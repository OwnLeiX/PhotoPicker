package lx.photopicker.ui.adapter;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>点击CheckBox快速选择 | 取消选择 Photo的监听</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public interface OnPhotoQuickPickedListener {
    void onPhotoQuickPicked(PhotoEntity photo);
    void onPhotoQuickRemoved(PhotoEntity photo);
}
