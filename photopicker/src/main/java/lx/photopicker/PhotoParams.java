package lx.photopicker;

/**
 * <b>需要Pick的Photo的规格参数</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PhotoParams {
    private static final int FLAG_CLIP = 1;
    private static final int FLAG_MULTI = 1 << 1;
    private static final int FLAG_LIMIT_SIZE = 1 << 2;
    private static final int FLAG_LIMIT_PIXEL = 1 << 3;
    private static final int FLAG_NATIVE_PHOTO = 1 << 4;

    private int mFlags;
    private int[] clipSize;
    private int maxCount = 1;
    private int maxPixel;
    private long maxSize;
    private long nativeMaxSize;

    public int[] getClipSize() {
        return clipSize;
    }

    public boolean isClip() {
        return (mFlags & FLAG_CLIP) > 0;
    }

    public boolean isMulti() {
        return (mFlags & FLAG_MULTI) > 0;
    }

    public boolean isLimitSize() {
        return (mFlags & FLAG_LIMIT_SIZE) > 0;
    }

    public boolean isLimitPixel() {
        return (mFlags & FLAG_LIMIT_PIXEL) > 0;
    }

    public boolean isNativePhoto() {
        return (mFlags & FLAG_NATIVE_PHOTO) > 0;
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

    private PhotoParams(Builder builder) {
        this.mFlags = builder.flags;
        this.maxCount = builder.maxCount;
        this.maxPixel = builder.maxPixel;
        this.maxSize = builder.maxSize;
        this.nativeMaxSize = builder.nativeMaxSize;
        this.clipSize = builder.size;
    }


    public static class Builder {
        private int flags = 0;
        private int[] size;
        private int maxCount = 1;
        private int maxPixel = Integer.MAX_VALUE;
        private long maxSize = Long.MAX_VALUE;
        private long nativeMaxSize = Long.MAX_VALUE;

        public Builder setClipSize(int[] size) {
            this.size = size;
            flags |= FLAG_CLIP;
            return this;
        }

        public Builder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            if (maxCount > 1) {
                flags |= FLAG_MULTI;
            } else {
                flags &= ~FLAG_MULTI;
            }
            return this;
        }

        public Builder setMaxPixel(int maxPixel) {
            this.maxPixel = maxPixel;
            flags |= FLAG_LIMIT_PIXEL;
            return this;
        }

        public Builder setMaxSize(long maxSize) {
            this.maxSize = maxSize;
            flags |= FLAG_LIMIT_SIZE;
            return this;
        }

        public Builder setNativeMaxSize(long nativeMaxSize) {
            this.nativeMaxSize = nativeMaxSize;
            flags |= FLAG_NATIVE_PHOTO;
            if (maxSize > nativeMaxSize)
                maxSize = nativeMaxSize;
            return this;
        }

        public PhotoParams create() {
            return new PhotoParams(this);
        }
    }
}
