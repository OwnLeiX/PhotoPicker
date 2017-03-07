package lx.photopicker.task;

import android.graphics.Bitmap;

/**
 * <b></b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public interface PhotoLoadCallback {
    void onLoadFinished(Bitmap bitmap,boolean isZipped);
}
