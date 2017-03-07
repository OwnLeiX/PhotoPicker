package lx.photopicker.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.Pool;

/**
 * <b>将图片裁剪至2000px以下边，200KB以下Size的任务</b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class SizeClipTask extends Pool.Task {
    private List<PhotoEntity> mPhotos;
    private List<PhotoEntity> mClipPhotos = new ArrayList<>();
    private String mDir;
    private SizeClipCallback mCallback;

    public SizeClipTask(List<PhotoEntity> mPhotos, String mDir, SizeClipCallback mCallback) {
        this.mPhotos = mPhotos;
        this.mDir = mDir;
        this.mCallback = mCallback;
    }

    @Override
    protected void work() {
        for (PhotoEntity photo : mPhotos) {
            PhotoEntity photoEntity = clipToSize(photo);
            if (photoEntity != null)
                mClipPhotos.add(photoEntity);
        }
        if (mCallback != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onSizeClipFinished(mClipPhotos);
                }
            });
        }
    }

    private PhotoEntity clipToSize(PhotoEntity photo) {
        PhotoEntity photoEntity = photo;
        try {
            //通过设置读取压缩比，来控制图片的像素
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photo.getPath(), options);
            if (options.outWidth > 2000 || options.outHeight > 2000)
                options.inSampleSize = calculateInSampleSize(options, 2000, 2000);
            options.inJustDecodeBounds = false;
            File file = new File(photo.getPath());
            if (file.length() / 1024.0f > 200.0f || options.inSampleSize != 1) {

                Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath(), options);
                //通过设置图片质量，来控制图片文件的大小
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                float size = baos.toByteArray().length / 1024.0f;
                baos.reset();
                quality = (int) (200.0f / size * quality);
                if (quality < 5) {
                    quality = 0;
                } else if (quality > 100) {
                    quality = 100;
                }
                Log.wtf("1","quality(): " + quality);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                if (!bitmap.isRecycled())
                    bitmap.recycle();
                bitmap = null;
                String path = mDir + System.currentTimeMillis() + ".jpg";
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                baos.close();
                photoEntity = new PhotoEntity(path);
                photoEntity.setNeedNative(photo.isNeedNative());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return photoEntity;
        }

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 保存图片原宽高值
        final int height = options.outHeight;
        final int width = options.outWidth;
        // 初始化压缩比例为1
        int inSampleSize = 1;

        // 当图片宽高值任何一个大于所需压缩图片宽高值时,进入循环计算系统
        if (height > reqHeight || width > reqWidth) {

            final float halfHeight = height / 2.0f;
            final float halfWidth = width / 2.0f;

            // 压缩比例值每次循环两倍增加,
            // 直到原图宽高值的一半除以压缩值后都~大于所需宽高值为止
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
