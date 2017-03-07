package lx.photopicker.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * <b></b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class PhotoFolderEntity {
    private List<PhotoEntity> photoEntities;
    private String folderName;
    private PhotoEntity lastPhoto;

    public PhotoFolderEntity(ArrayList<PhotoEntity> photos, String folderName) {
        this.photoEntities = photos;
        this.folderName = folderName;
        if (photos.size() > 0) {
            this.lastPhoto = photos.get(photos.size() - 1);
        }
    }

    public List<PhotoEntity> getPhotoEntities() {
        return photoEntities;
    }

    public String getFolderName() {
        return folderName;
    }

    public PhotoEntity getLastPhoto() {
        return lastPhoto;
    }
}
