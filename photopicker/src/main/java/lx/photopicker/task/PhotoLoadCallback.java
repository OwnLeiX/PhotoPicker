package lx.photopicker.task;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.Nullable;

/**
 * <b></b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public interface PhotoLoadCallback {
    void onLoadBitmapSize(int width, int height);
    void onLoadFinished(@Nullable Bitmap bitmap, boolean isZipped, @Nullable RectF loadPercentRectF);
}
