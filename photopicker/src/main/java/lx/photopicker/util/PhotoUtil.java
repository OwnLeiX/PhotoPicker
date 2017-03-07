package lx.photopicker.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * <b></b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class PhotoUtil {
    public static void loadImage(File file, ImageView iv) {
//        Picasso.with(App.getContext())
//                .load(file)
//                .into(iv);

    }

    public static void loadListImage(String localPath, ImageView iv) {
        Glide.with(iv.getContext().getApplicationContext()).load(localPath).into(iv);
//        iv.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(localPath), 100, 100));  //使用系统的一个工具类，参数列表为 Bitmap Width,Height  这里使用压缩后显示，否则在华为手机上ImageView 没有显示
    }

    public static  void loadPickImage(String localPath, ImageView iv) {
        Glide.with(iv.getContext().getApplicationContext()).load(localPath).into(iv);
//        iv.setImageBitmap(BitmapFactory.decodeFile(localPath));
    }
}
