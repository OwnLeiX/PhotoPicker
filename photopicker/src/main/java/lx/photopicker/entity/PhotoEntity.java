package lx.photopicker.entity;

/**
 * <b></b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoEntity {
    private String path = "";
    private boolean isPicked = false;
    private boolean isNeedNative = false;

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

    public boolean isNeedNative() {
        return isNeedNative;
    }

    public void setNeedNative(boolean needNative) {
        isNeedNative = needNative;
    }
}
