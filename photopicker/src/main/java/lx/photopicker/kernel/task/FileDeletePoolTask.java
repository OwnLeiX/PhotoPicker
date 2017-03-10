package lx.photopicker.kernel.task;

import java.io.File;
import java.util.List;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.Pool;

/**
 * <b></b>
 * Created on 2017/3/10.
 *
 * @author LeiXun
 */

public class FileDeletePoolTask extends Pool.PoolTask {
    private List<PhotoEntity> mPhotos;
    private String mDir;

    public FileDeletePoolTask(List<PhotoEntity> mPhotos, String targetDir) {
        this.mPhotos = mPhotos;
        this.mDir = targetDir;
    }

    @Override
    protected void work() {
        File file;
        String path;
        for (PhotoEntity photo : mPhotos) {
            path = photo.getPath();
            if (path.startsWith(mDir)) {
                file = new File(path);
                if (file.exists())
                    file.delete();
            }
        }
    }
}
