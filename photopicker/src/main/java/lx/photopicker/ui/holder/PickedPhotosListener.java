package lx.photopicker.ui.holder;

import lx.photopicker.ui.adapter.OnPhotoRemovedListener;

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
