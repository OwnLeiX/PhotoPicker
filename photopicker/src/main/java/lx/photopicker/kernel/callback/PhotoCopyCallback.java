package lx.photopicker.kernel.callback;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b></b>
 * Created on 2017/3/10.
 *
 * @author LeiXun
 */

public interface PhotoCopyCallback {
    void onCopyFinished(PhotoEntity newPhoto, PhotoEntity oldPhoto);
    void onCopyError(Throwable e);
}
