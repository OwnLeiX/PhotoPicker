package lx.photopicker.kernel.callback;

import java.util.List;

import lx.photopicker.PhotoParams;
import lx.photopicker.entity.PhotoEntity;

/**
 * <b>图片尺寸修正的回调</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public interface SizeClipCallback {
    void onSizeClipFinished(List<PhotoEntity> photos, PhotoParams params);
}
