package lx.photopicker.kernel.callback;

import java.util.List;

import lx.photopicker.entity.PhotoFolderEntity;

/**
 * <b>扫描所有Photo的回调接口</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public interface ScanCallback {
    void onScanFinished(List<PhotoFolderEntity> list);
    void onScanError(Throwable e);
    void onScanEmpty();
}
