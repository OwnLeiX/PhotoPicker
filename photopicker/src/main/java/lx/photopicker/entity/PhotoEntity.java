package lx.photopicker.entity;

/**
 * <b></b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoEntity {
    private String path = "";
    private String oldPath = "";
    private boolean isPicked = false;
    private boolean isNative = false;

    public PhotoEntity(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean isPicked() {
        return isPicked;
    }

    public void setPicked(boolean picked) {
        isPicked = picked;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean is) {
        isNative = is;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }
}
