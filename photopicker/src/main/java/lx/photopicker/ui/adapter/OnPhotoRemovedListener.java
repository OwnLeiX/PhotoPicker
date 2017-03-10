package lx.photopicker.ui.adapter;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>（已经选择的Photo栏）点击右上角区域取消选择该Photo的监听</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public interface OnPhotoRemovedListener {
    void onPhotoRemoved(PhotoEntity photo);
}
