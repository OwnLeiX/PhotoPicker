package lx.photopicker.adapter;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>正常选择Photo的监听</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public interface OnPhotoPickedListener {
    void onPhotoPicked(PhotoEntity photo);
    void onPhotoRepicked(PhotoEntity photo);
}
