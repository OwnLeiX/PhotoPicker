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
    private int maxPixel;
    private long maxSize;
    private long nativeMaxSize;
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

    public int getMaxPixel() {
        return maxPixel;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getNativeMaxSize() {
        return nativeMaxSize;
    }

    private PhotoParams(Builder builder){
        if ((builder.flags & FLAG_CLIP) > 0){
            this.isClip = true;
            this.clipSize = builder.size;
        }
        if ((builder.flags & FLAG_MULTI) > 0) {
            this.isMulti = true;
        }
        this.maxCount = builder.maxCount;
        this.maxPixel = builder.maxPixel;
        this.maxSize = builder.maxSize;
        this.nativeMaxSize = builder.nativeMaxSize;
    }



    public static class Builder{
        private int flags = 0;
        private int[] size;
        private int maxCount = 1;
        private int maxPixel = 2000;
        private long maxSize = 200 * 1024;
        private long nativeMaxSize = 3 * 1024 * 1024;

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

        public Builder setMaxPixel(int maxPixel) {
            this.maxPixel = maxPixel;
            return this;
        }

        public Builder setMaxSize(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder setNativeMaxSize(long nativeMaxSize) {
            this.nativeMaxSize = nativeMaxSize;
            return this;
        }

        public PhotoParams create(){
            return new PhotoParams(this);
        }
    }
}
