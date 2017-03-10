package lx.photopicker.kernel.callback;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>保存图片的回调</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public interface PhotoZipCallback {
    void onZipFinished(PhotoEntity photo);
    void onZipError(Throwable e);
}
