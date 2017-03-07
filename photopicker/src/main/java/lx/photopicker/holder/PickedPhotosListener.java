package lx.photopicker.holder;

import lx.photopicker.adapter.OnPhotoRemovedListener;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 2017/3/5
 */

public interface PickedPhotosListener extends OnPhotoRemovedListener {
    void onViewClicked();
    void onCompleteClicked();
}
