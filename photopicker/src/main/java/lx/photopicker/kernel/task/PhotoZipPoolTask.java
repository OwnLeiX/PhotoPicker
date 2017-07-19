package lx.photopicker.kernel.task;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import lx.photopicker.PhotoParams;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.kernel.callback.PhotoZipCallback;
import lx.photopicker.util.Pool;

/**
 * <b>压缩保存图片的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoZipPoolTask extends Pool.PoolTask {
    private Bitmap mBitmap;
    private PhotoZipCallback mCallback;
    private String mPath;
    private PhotoParams mParams;
    private boolean isNative;

    public PhotoZipPoolTask(Bitmap bitmap, String path, PhotoParams params, boolean isNeedNative, PhotoZipCallback callback) {
        this.mBitmap = bitmap;
        this.mCallback = callback;
        this.mPath = path;
        this.mParams = params;
        this.isNative = isNeedNative && params.isNativePhoto();
    }

    @Override
    protected void work() {
        try {
            long maxSize = isNative ? mParams.getNativeMaxSize() : mParams.getMaxSize();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            mBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            byte[] bytes = baos.toByteArray();
            while (mParams.isLimitSize() && bytes.length >= maxSize && options > 0) {
                baos.reset();
                options -= 10;
                if (options < 0)
                    options = 0;
                mBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                bytes = baos.toByteArray();
            }
            if (!mBitmap.isRecycled())
                mBitmap.recycle();
            mBitmap = null;
            File target = new File(mPath);
            File parentFile = target.getParentFile();
            if (!parentFile.exists())
                parentFile.mkdirs();
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(bytes);
            fos.flush();
            fos.close();
            baos.close();
            final PhotoEntity photoEntity = new PhotoEntity(mPath);
            photoEntity.setNative(isNative);
            if (mCallback != null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onZipFinished(photoEntity);

                    }
                });
            }
        } catch (final Exception e) {
            if (mCallback != null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onZipError(e);
                    }
                });
            }
        }
    }
}
