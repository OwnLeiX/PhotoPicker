package lx.photopicker;

/**
 * <b>需要Pick的Photo的规格参数</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoParams {
    public static final int FLAG_CLIP = 1;
    public static final int FLAG_MULTI = 1 << 1;

    private int[] clipSize;
    private int maxCount = 1;
    private boolean isClip = false;
    private boolean isMulti = false;

    public int[] getClipSize() {
        return clipSize;
    }

    public boolean isClip() {
        return isClip;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public int getMaxCount() {
        return maxCount;
    }

    private PhotoParams(Builder builder){
        if ((builder.flags & FLAG_CLIP) > 0){
            this.isClip = true;
            this.clipSize = builder.size;
        }
        if ((builder.flags & FLAG_MULTI) > 0) {
            this.isMulti = true;
            this.maxCount = builder.maxCount;
        }
    }



    public static class Builder{
        private int flags = 0;
        private int[] size;
        private int maxCount;

        public Builder addFlags(int flags){
            this.flags |= flags;
            return this;
        }

        public Builder setClipSize(int[] size) {
            this.size = size;
            return this;
        }

        public Builder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public PhotoParams create(){
            return new PhotoParams(this);
        }
    }
}
