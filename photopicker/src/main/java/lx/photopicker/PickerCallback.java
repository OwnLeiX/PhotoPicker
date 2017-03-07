package lx.photopicker;

import java.util.List;

import lx.photopicker.entity.PhotoEntity;

/**
 * <b>本次Pick结果的回调</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public interface PickerCallback {
    void onCancel();
    void onPicked(List<PhotoEntity> pickedPhotos);
}
