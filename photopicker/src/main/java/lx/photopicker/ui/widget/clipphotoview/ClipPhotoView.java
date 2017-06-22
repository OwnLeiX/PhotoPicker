package lx.photopicker.ui.widget.clipphotoview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import lx.photopicker.R;
import lx.photopicker.ui.widget.clipphotoview.callback.BitmapClipCallback;
import lx.photopicker.ui.widget.clipphotoview.callback.PhotoLoadCallback;
import lx.photopicker.ui.widget.clipphotoview.task.PhotoLoadTask;
import lx.photopicker.ui.widget.clipphotoview.task.Task;

/**
 * <b></b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class ClipPhotoView extends View implements PhotoLoadCallback {

    public interface OnBitmapStatusChangeListener {
        void onLoadCompleted();

        void onStartLoad();

        void onError();
    }

    private OnBitmapStatusChangeListener mOnBitmapStatusChangeListener;

    public OnBitmapStatusChangeListener getOnBitmapStatusChangeListener() {
        return mOnBitmapStatusChangeListener;
    }

    public void setOnBitmapStatusChangeListener(OnBitmapStatusChangeListener mOnBitmapStatusChangeListener) {
        this.mOnBitmapStatusChangeListener = mOnBitmapStatusChangeListener;
    }

    private volatile int mFlags = FLAG_FIRST_LOAD | FLAG_HIGH_QUALITY;
    private static final int FLAG_FIRST_LOAD = 1; //第一次读取图片
    private static final int FLAG_CLIPPING = 1 << 1; //裁剪中
    private static final int FLAG_ROTATING = 1 << 2; //旋转中
    private static final int FLAG_HIGH_QUALITY = 1 << 3; //局部加载高质量模式
    private static final int FLAG_ANIMATING = 1 << 4;// 动画执行中
    private static final int FLAG_ZIPPED = 1 << 5; //图片被压缩
    private static final int FLAG_CLIP_MODE = 1 << 6;//裁剪模式

    private Paint mPaint;
    private Path mOverlapPath = new Path(); //蒙层Path
    private int mOverlapColor = 0xAA000000; //蒙层颜色

    private int mClipRectMarginTop, mClipRectMarginBottom, mClipRectMarginLeft, mClipRectMarginRight;
    private RectF mClipRect = new RectF(); //裁剪框定位矩形
    private RectF mBitmapRect = new RectF();    //与Bitmap显示位置随动的矩形
    private RectF mLoadPercentRectF; //高质量模式 部分读取图片所用矩形

    private String mUrl; //图片地址
    private volatile Bitmap mBitmap; //图片
    private volatile Bitmap mClipBitmap; //高质量模式 部分加载的图片
    private int[] mWH = new int[2]; //控件宽高
    private int[] mClipSize = new int[]{200, 200}; //裁剪尺寸
    private int[] mBitmapWH = new int[2]; //当前图片（mBitmap）宽高
    private int[] mBitmapFullWH = new int[2]; //文件图片宽高


    // 縮放控制
    private volatile Matrix mCurrMatrix = new Matrix();
    private volatile Matrix mSavedMatrix = new Matrix();
    private volatile Matrix mScaleMatrix = new Matrix();
    //旋转控制
    private volatile Matrix mBitmapRotateMatrix = new Matrix(); //图片旋转矩阵，用于旋转图片（mBitmap）时复制mBitmap用
    private volatile int mClipRotateDegree = 0; //累计旋转角度，用于高质量模式部分读取的图片

    // 不同状态的表示：
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_DRAG = 1;
    private static final int TOUCH_ZOOM = 2;
    private int mTouchMode = TOUCH_NONE;

    // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private PointF mStartPoint = new PointF();
    private PointF mMidPoint = new PointF();
    private float oriDis = 1f;
    private float mMinScale = -1;
    private float mMaxScale = -1;

    private LoadHighQualityTask mLoadHighQualityTask; //高质量模式-部分读取任务
    private BitmapRotateTask mBitmapRotateTask; //旋转图片任务


    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();// 线程池大小
    private ThreadPoolExecutor mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);// 线程池;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ClipPhotoView(Context context) {
        this(context, null);
    }

    public ClipPhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ClipPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        ownInit(context, attr, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWH[0] = w;
        mWH[1] = h;
        createClipRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mCurrMatrix, null);
        }
        if ((mFlags & FLAG_CLIP_MODE) == 0)
            return;
        if (mTouchMode == TOUCH_NONE && (mFlags & FLAG_ANIMATING) == 0 && mClipBitmap != null) {
            canvas.save();
            canvas.translate(mClipRect.left, mClipRect.top);
            canvas.drawBitmap(mClipBitmap, mScaleMatrix, null);
            canvas.restore();
        } else {
            recycleClipBitmap();
        }
        //绘制蒙层
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(mOverlapColor);
        canvas.drawPath(mOverlapPath, mPaint);
        //绘制裁剪校准线
//        canvas.drawLine(mClipRect.left, mClipRect.centerY(), mClipRect.right, mClipRect.centerY(), mPaint);
//        canvas.drawLine(mClipRect.centerX(), mClipRect.top, mClipRect.centerX(), mClipRect.bottom, mPaint);
//        绘制边框
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(0xFF000000);
//        canvas.drawPath(mOverlapPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((mFlags & FLAG_ANIMATING) > 0)
            return super.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 单指
            case MotionEvent.ACTION_DOWN:
                recycleClipBitmap();
                mSavedMatrix.set(mCurrMatrix);
                mStartPoint.set(event.getX(), event.getY());
                mTouchMode = TOUCH_DRAG;
                break;
            // 双指
            case MotionEvent.ACTION_POINTER_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                oriDis = calculateDistance(event);
                if (oriDis > 10f) {
                    mSavedMatrix.set(mCurrMatrix);
                    mMidPoint = calculateMiddle(event);
                    mTouchMode = TOUCH_ZOOM;
                }
                break;
            // 手指放开
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                mSavedMatrix.set(mCurrMatrix);
                mTouchMode = TOUCH_NONE;
                clampZoomRange();
                return false;
            // 单指滑动事件
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TOUCH_DRAG) {
                    // 是一个手指拖动
                    mCurrMatrix.set(mSavedMatrix);
                    mCurrMatrix.postTranslate(event.getX() - mStartPoint.x, event.getY() - mStartPoint.y);
                    clampBitmapRectTranslation();
                } else if (mTouchMode == TOUCH_ZOOM) {
                    // 两个手指滑动
                    float newDist = calculateDistance(event);
                    if (newDist > 10f) {
                        mCurrMatrix.set(mSavedMatrix);
                        float scale = newDist / oriDis;
                        //					mCurrMatrix.postScale(scale, scale, mBitmapRect.centerX(), mBitmapRect.centerY());
                        //					clampBitmapRectScale();
                        mCurrMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                        clampBitmapRectScale2(mMidPoint.x, mMidPoint.y);
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap != null && !mBitmap.isRecycled())
            mBitmap.recycle();
        mBitmap = null;
        recycleClipBitmap();
        mClipBitmap = null;
    }

    @Override
    public void onLoadBitmapSize(int width, int height) {
        mBitmapFullWH[0] = width;
        mBitmapFullWH[1] = height;
    }

    /**
     * 读取图片的回调
     * 如果是第一次读取，则赋值给mBitmap
     * 如果不是第一次读取（高质量局部加载），则赋值给mClipBitmap
     */
    @Override
    public void onLoadCompleted(Bitmap bitmap, boolean isZipped, RectF loadPercentRectF) {
        if (bitmap == null) {
            if ((mFlags & FLAG_FIRST_LOAD) > 0 && mOnBitmapStatusChangeListener != null)
                mOnBitmapStatusChangeListener.onError();
            return;
        }
        if ((mFlags & FLAG_FIRST_LOAD) > 0) {
            if (isZipped) {
                mFlags |= FLAG_ZIPPED;
            } else {
                mFlags &= ~FLAG_ZIPPED;
            }
            setBitmap(bitmap);
            if (mOnBitmapStatusChangeListener != null)
                mOnBitmapStatusChangeListener.onLoadCompleted();
        } else if (loadPercentRectF != null) {
            recycleClipBitmap();
            if (mLoadPercentRectF.left != loadPercentRectF.left || mLoadPercentRectF.right != loadPercentRectF.right
                    || mLoadPercentRectF.top != loadPercentRectF.top || mLoadPercentRectF.bottom != loadPercentRectF.bottom || mTouchMode != TOUCH_NONE
                    || (mFlags & FLAG_ANIMATING) > 0) {
                bitmap.recycle();
            } else {
                this.mClipBitmap = bitmap;
                mScaleMatrix.reset();
                float scalePercent;
                final int rotate = mClipRotateDegree / 90 % 4;
                switch (rotate) {
                    case 1:
                    case -3:
                        scalePercent = mClipRect.width() / bitmap.getHeight();
                        mScaleMatrix.postTranslate(bitmap.getHeight(), 0);
                        mScaleMatrix.postRotate(90.0f, bitmap.getHeight(), 0);
                        break;
                    case 2:
                    case -2:
                        scalePercent = mClipRect.width() / bitmap.getWidth();
                        mScaleMatrix.postRotate(180.0f, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
                        break;
                    case 3:
                    case -1:
                        scalePercent = mClipRect.width() / bitmap.getHeight();
                        mScaleMatrix.postTranslate(-bitmap.getWidth(), 0);
                        mScaleMatrix.postRotate(-90.0f, 0, 0);
                        break;
                    default:
                        scalePercent = mClipRect.width() / bitmap.getWidth();
                        break;
                }
                mScaleMatrix.postScale(scalePercent, scalePercent);
                invalidate();
            }
        }
    }

    /**
     * 顺时针旋转90度
     */
    public void rotateCW() {
        if ((mFlags & FLAG_ROTATING) > 0)
            return;
        mFlags |= FLAG_ROTATING;
        mClipRotateDegree += 90;
        mBitmapRotateMatrix.postTranslate(mBitmapWH[1], 0);
        mBitmapRotateMatrix.postRotate(90.0f, mBitmapWH[1], 0);
        if (mBitmapRotateTask == null)
            mBitmapRotateTask = new BitmapRotateTask();
        mExecutor.execute(mBitmapRotateTask);
    }

    /**
     * 逆时针旋转90度
     */
    public void rotateCCW() {
        if ((mFlags & FLAG_ROTATING) > 0)
            return;
        mFlags |= FLAG_ROTATING;
        mClipRotateDegree -= 90;
        mBitmapRotateMatrix.postTranslate(-mBitmapWH[0], 0);
        mBitmapRotateMatrix.postRotate(-90.0f, 0, 0);
        if (mBitmapRotateTask == null)
            mBitmapRotateTask = new BitmapRotateTask();
        mExecutor.execute(mBitmapRotateTask);
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        if (executor == null)
            return;
        if (this.mExecutor != null)
            this.mExecutor.shutdown();
        this.mExecutor = executor;
    }

    public void setUrl(String url) {
        mUrl = url;
        mFlags |= FLAG_FIRST_LOAD;
        if (mOnBitmapStatusChangeListener != null)
            mOnBitmapStatusChangeListener.onStartLoad();
        mExecutor.execute(new PhotoLoadTask(url, this));
    }

    public void setClipSize(int[] size) {
        mClipSize = size;
    }

    public void setClipMode(boolean clip) {
        if (clip) {
            mFlags |= FLAG_CLIP_MODE;
        } else {
            mFlags &= ~FLAG_CLIP_MODE;
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mFlags &= ~FLAG_FIRST_LOAD;
        if (this.mBitmap != null && !this.mBitmap.isRecycled())
            this.mBitmap.recycle();
        this.mBitmap = bitmap;
        createClipRect();
    }

    public int getClipRectMarginTop() {
        return mClipRectMarginTop;
    }

    public void setClipRectMarginTop(int margin) {
        this.mClipRectMarginTop = margin;
        createClipRect();
    }

    public int getClipRectMarginBottom() {
        return mClipRectMarginBottom;
    }

    public void setClipRectMarginBottom(int margin) {
        this.mClipRectMarginBottom = margin;
        createClipRect();
    }

    public int getClipRectMarginLeft() {
        return mClipRectMarginLeft;
    }

    public void setClipRectMarginLeft(int margin) {
        this.mClipRectMarginLeft = margin;
        createClipRect();
    }

    public int getClipRectMarginRight() {
        return mClipRectMarginRight;
    }

    public void setClipRectMarginRight(int margin) {
        this.mClipRectMarginRight = margin;
        createClipRect();
    }

    public int getOverlapColor() {
        return mOverlapColor;
    }

    public void setOverlapColor(int color) {
        this.mOverlapColor = color;
        mPaint.setColor(color);
        postInvalidate();
    }

    /**
     * 判断当前是否可以进行图片裁剪
     *
     * @return true可裁剪 | false不可裁剪
     */
    public boolean canClip() {
        return mBitmap != null && (mFlags & FLAG_CLIPPING) == 0 && (mFlags & FLAG_ANIMATING) == 0 && mTouchMode == TOUCH_NONE;
    }

    /**
     * 获取裁剪图片的方法
     * 使用前最好调用canClip()判断是否在可裁剪状态。否则可能会出现不可预知的结果。
     *
     * @param callback 回调
     */
    public void clipBitmap(final BitmapClipCallback callback) {
        mFlags |= FLAG_CLIPPING;
        if (mClipBitmap != null && !mClipBitmap.isRecycled()) {
            Task task = new Task() {
                @Override
                protected void work() {
                    final Bitmap bitmap = resizeBitmap(mClipBitmap, mClipSize, false);
                    if (callback != null) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onClipCompleted(bitmap);
                                mClipBitmap = null;
                                mFlags &= ~FLAG_CLIPPING;
                            }
                        });
                    } else {
                        if (bitmap != null && !bitmap.isRecycled())
                            bitmap.recycle();
                        mFlags &= ~FLAG_CLIPPING;
                    }
                }
            };
            mExecutor.execute(task);
        } else {
            if (mUrl == null || (mFlags & FLAG_HIGH_QUALITY) == 0) {
                clipByDisplayBitmap(callback);
            } else {
                clipByLoadHighQuality(mUrl, callback);
            }
        }
    }

    private void ownInit(Context context, AttributeSet attr, int defStyle) {
        initAttrs(context, attr, defStyle);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attr, int defStyle) {
        if (attr != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.ClipPhotoView);
            if (typedArray != null) {
                mOverlapColor = typedArray.getColor(R.styleable.ClipPhotoView_clipPhotoView_OverlapColor, mOverlapColor);
                mClipRectMarginLeft = typedArray.getDimensionPixelSize(R.styleable.ClipPhotoView_clipPhotoView_clipRectMarginLeft, 0);
                mClipRectMarginTop = typedArray.getDimensionPixelSize(R.styleable.ClipPhotoView_clipPhotoView_clipRectMarginTop, 0);
                mClipRectMarginRight = typedArray.getDimensionPixelSize(R.styleable.ClipPhotoView_clipPhotoView_clipRectMarginRight, 0);
                mClipRectMarginBottom = typedArray.getDimensionPixelSize(R.styleable.ClipPhotoView_clipPhotoView_clipRectMarginBottom, 0);
                boolean highQualityMode = typedArray.getBoolean(R.styleable.ClipPhotoView_clipPhotoView_highQualityImage, false);
                boolean clipMode = typedArray.getBoolean(R.styleable.ClipPhotoView_clipPhotoView_clipMode, false);
                if (highQualityMode) {
                    mFlags |= FLAG_HIGH_QUALITY;
                } else {
                    mFlags &= ~FLAG_HIGH_QUALITY;
                }

                if (clipMode) {
                    mFlags |= FLAG_CLIP_MODE;
                } else {
                    mFlags &= ~FLAG_CLIP_MODE;
                }
                typedArray.recycle();
            }
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mOverlapColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeWidth(0);
    }

    /**
     * 根据ClipMode | ClipSize来计算裁剪区域
     * 根据mBitmap来进行初始缩放和平移（centerInside）
     * 并且初始化Bitmap的即时显示区域
     */
    private void createClipRect() {
        if ((mFlags & FLAG_CLIP_MODE) > 0) {
            final int clipAreaWidth = mWH[0] - mClipRectMarginLeft - mClipRectMarginRight;
            final int clipAreaHeight = mWH[1] - mClipRectMarginTop - mClipRectMarginBottom;
            float percent = mClipSize[0] * 1.0f / mClipSize[1];
            float h = clipAreaWidth / percent;
            if (h <= clipAreaHeight) {
                mClipRect.left = mClipRectMarginLeft;
                mClipRect.right = mClipRectMarginLeft + clipAreaWidth;
                float rectHeight = mClipRect.width() / percent;
                mClipRect.top = mClipRectMarginTop + clipAreaHeight / 2.0f - rectHeight / 2.0f;
                mClipRect.bottom = mClipRectMarginTop + clipAreaHeight / 2.0f + rectHeight / 2.0f;
                mOverlapPath.reset();
                mOverlapPath.addRect(0, 0, mWH[0], mWH[1], Path.Direction.CW);
                mOverlapPath.addRect(mClipRect, Path.Direction.CCW);
            } else {
                mClipRect.top = mClipRectMarginTop;
                mClipRect.bottom = mClipRectMarginTop + clipAreaHeight;
                float rectWidth = mClipRect.height() * percent;
                mClipRect.left = mClipRectMarginLeft + clipAreaWidth / 2.0f - rectWidth / 2.0f;
                mClipRect.right = mClipRectMarginLeft + clipAreaWidth / 2.0f + rectWidth / 2.0f;
                mOverlapPath.reset();
                mOverlapPath.addRect(0, 0, mWH[0], mWH[1], Path.Direction.CW);
                mOverlapPath.addRect(mClipRect, Path.Direction.CCW);
            }
        } else if ((mFlags & FLAG_CLIP_MODE) == 0) {
            mClipRect.left = 0;
            mClipRect.right = mWH[0];
            mClipRect.top = 0;
            mClipRect.bottom = mWH[1];
            if (mBitmap != null) {
                float percent = mBitmap.getWidth() * 1.0f / mBitmap.getHeight();
                float h = mWH[0] / percent;
                if (h <= mWH[1]) {
                    mClipRect.left = 0;
                    mClipRect.right = mWH[0];
                    float rectHeight = mClipRect.width() / percent;
                    mClipRect.top = mWH[1] / 2.0f - rectHeight / 2.0f;
                    mClipRect.bottom = mWH[1] / 2.0f + rectHeight / 2.0f;
                } else {
                    mClipRect.top = 0;
                    mClipRect.bottom = mWH[1];
                    float rectWidth = mClipRect.height() * percent;
                    mClipRect.left = mWH[0] / 2.0f - rectWidth / 2.0f;
                    mClipRect.right = mWH[0] / 2.0f + rectWidth / 2.0f;
                }
            }

        }
        if (mBitmap != null) {
            mBitmapWH[0] = mBitmap.getWidth();
            mBitmapWH[1] = mBitmap.getHeight();
            mBitmapRect.left = 0;
            mBitmapRect.right = mBitmapWH[0];
            mBitmapRect.top = 0;
            mBitmapRect.bottom = mBitmapWH[1];
            float transX = mClipRect.centerX() - mBitmapWH[0] / 2.0f;
            float transY = mClipRect.centerY() - mBitmapWH[1] / 2.0f;
            mCurrMatrix.setTranslate(transX, transY);
            clampBitmapRectTranslation();
            mMinScale = Math.max(mClipRect.width() / mBitmapWH[0], mClipRect.height() / mBitmapWH[1]);
            mCurrMatrix.postScale(mMinScale, mMinScale, mBitmapRect.centerX(), mBitmapRect.centerY());
            //			clampBitmapRectScale();
            clampBitmapRectScale2(mBitmapRect.centerX(), mBitmapRect.centerY());
            mMaxScale = Math.max(mBitmapFullWH[0] / mClipRect.width(), mBitmapFullWH[1] / mClipRect.height()) * 10.0f;
        }
        postInvalidate();
    }

    // 计算两个触摸点之间的距离
    private float calculateDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 计算两个触摸点的中点
    private PointF calculateMiddle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    private void calculateRectfByCurrentRotate(RectF rectF, float left, float top, float right, float bottom) {
        final int rotate = mClipRotateDegree / 90 % 4;
        switch (rotate) {
            case 1:
            case -3:
                rectF.left = top;
                rectF.top = 1.0f - right;
                rectF.right = bottom;
                rectF.bottom = 1.0f - left;
                break;
            case 2:
            case -2:
                rectF.left = 1.0f - right;
                rectF.top = 1.0f - bottom;
                rectF.right = 1.0f - left;
                rectF.bottom = 1.0f - top;
                break;
            case 3:
            case -1:
                rectF.left = 1.0f - bottom;
                rectF.top = left;
                rectF.right = 1.0f - top;
                rectF.bottom = right;
                break;
            default:
                rectF.left = left;
                rectF.top = top;
                rectF.right = right;
                rectF.bottom = bottom;
                break;
        }
    }

    private void clampBitmapRectTranslation() {
        float[] values = new float[9];
        mCurrMatrix.getValues(values);
        mBitmapRect.offsetTo(values[2], values[5]);
    }

    private void clampBitmapRectScale() {
        float[] values = new float[9];
        mCurrMatrix.getValues(values);
        final float scale = values[0];
        final float newWidth = mBitmapWH[0] * scale / 2.0f;
        final float newHeight = mBitmapWH[1] * scale / 2.0f;
        final float centerX = mBitmapRect.centerX();
        final float centerY = mBitmapRect.centerY();
        mBitmapRect.left = centerX - newWidth;
        mBitmapRect.right = centerX + newWidth;
        mBitmapRect.top = centerY - newHeight;
        mBitmapRect.bottom = centerY + newHeight;
    }

    private void clampBitmapRectScale2(float pointX, float pointY) {
        float[] values = new float[9];
        mCurrMatrix.getValues(values);
        final float scale = values[0];
        final float oldW = mBitmapRect.width();
        final float oldH = mBitmapRect.height();
        final float leftPercent = (pointX - mBitmapRect.left) / oldW;
        final float rightPercent = (mBitmapRect.right - pointX) / oldW;
        final float topPercent = (pointY - mBitmapRect.top) / oldH;
        final float bottomPercent = (mBitmapRect.bottom - pointY) / oldH;
        final float newW = mBitmapWH[0] * scale;
        final float newH = mBitmapWH[1] * scale;
        mBitmapRect.left = pointX - newW * leftPercent;
        mBitmapRect.right = pointX + newW * rightPercent;
        mBitmapRect.top = pointY - newH * topPercent;
        mBitmapRect.bottom = pointY + newH * bottomPercent;
    }

    /**
     * 修正手势拖拽位置的方法。
     */
    private void clampDragRange() {
        float[] values = new float[9];
        mCurrMatrix.getValues(values);
        float clampX = 0;
        float clampY = 0;
        if (mBitmapRect.left > mClipRect.left) {
            clampX = mClipRect.left - mBitmapRect.left;
        } else if (mBitmapRect.right < mClipRect.right) {
            clampX = mClipRect.right - mBitmapRect.right;
        }
        if (mBitmapRect.top > mClipRect.top) {
            clampY = mClipRect.top - mBitmapRect.top;
        } else if (mBitmapRect.bottom < mClipRect.bottom) {
            clampY = mClipRect.bottom - mBitmapRect.bottom;
        }
        final float dX = clampX;
        final float dY = clampY;
        if (dX != 0 || dY != 0) {
            ValueAnimator va = ValueAnimator.ofFloat(0.0f, 1.0f);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currX = animation.getAnimatedFraction() * dX;
                    float currY = animation.getAnimatedFraction() * dY;
                    mCurrMatrix.set(mSavedMatrix);
                    mCurrMatrix.postTranslate(currX, currY);
                    clampBitmapRectTranslation();
                    invalidate();
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSavedMatrix.set(mCurrMatrix);
                    invalidate();
                    mFlags &= ~FLAG_ANIMATING;
                    if ((mFlags & FLAG_HIGH_QUALITY) > 0)
                        loadHighQualityBitmap();
                }
            });
            va.setDuration((long) Math.min(250.0f, Math.max(Math.abs(dX), Math.abs(dY)) * 10.0f));
            mFlags |= FLAG_ANIMATING;
            va.start();
        } else {
            mFlags &= ~FLAG_ANIMATING;
            if ((mFlags & FLAG_HIGH_QUALITY) > 0)
                loadHighQualityBitmap();
        }
    }

    /**
     * 修正手势缩放比例的方法。
     */
    private void clampZoomRange() {

        float[] values = new float[9];
        mCurrMatrix.getValues(values);
        if (mMinScale != -1 && values[0] < mMinScale) {
            final float result = mMinScale / values[0];
            ValueAnimator va = ValueAnimator.ofFloat(1.0f, result);
            va.setDuration((long) Math.min(250.0f, Math.abs(result * 50.0f)));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float curr = (float) animation.getAnimatedValue();
                    mCurrMatrix.set(mSavedMatrix);
                    //					mCurrMatrix.postScale(curr, curr, mBitmapRect.centerX(), mBitmapRect.centerY());
                    //					clampBitmapRectScale();
                    mCurrMatrix.postScale(curr, curr, mMidPoint.x, mMidPoint.y);
                    clampBitmapRectScale2(mMidPoint.x, mMidPoint.y);
                    invalidate();
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSavedMatrix.set(mCurrMatrix);
                    //					clampBitmapRectScale();
                    clampBitmapRectScale2(mMidPoint.x, mMidPoint.y);
                    invalidate();
                    clampBitmapRectTranslation();
                    clampDragRange();
                }
            });
            mFlags |= FLAG_ANIMATING;
            va.start();
        } else if (mMaxScale != -1 && values[0] > mMaxScale) {
            final float result = mMaxScale / values[0];
            ValueAnimator va = ValueAnimator.ofFloat(1.0f, result);
            va.setDuration((long) Math.min(250.0f, Math.abs(1.0f / result * 50.0f)));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float curr = (float) animation.getAnimatedValue();
                    mCurrMatrix.set(mSavedMatrix);
                    //					mCurrMatrix.postScale(curr, curr, mBitmapRect.centerX(), mBitmapRect.centerY());
                    //					clampBitmapRectScale();
                    mCurrMatrix.postScale(curr, curr, mMidPoint.x, mMidPoint.y);
                    clampBitmapRectScale2(mMidPoint.x, mMidPoint.y);
                    invalidate();
                }
            });
            va.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSavedMatrix.set(mCurrMatrix);
                    //					clampBitmapRectScale();
                    clampBitmapRectScale2(mMidPoint.x, mMidPoint.y);
                    invalidate();
                    clampBitmapRectTranslation();
                    clampDragRange();
                }
            });
            mFlags |= FLAG_ANIMATING;
            va.start();
        } else {
            clampDragRange();
        }
    }

    /**
     * 将图片的裁剪区域部分以高质量加载的方法
     * 如果图片原本就是最高质量 | 没有图片的url，则不进行加载。
     */
    private void loadHighQualityBitmap() {
        if ((mFlags & FLAG_ZIPPED) == 0 || mUrl == null || mTouchMode != TOUCH_NONE || (mFlags & FLAG_CLIP_MODE) == 0)
            return;
        mLoadPercentRectF = new RectF();
        final float left = -(mBitmapRect.left >= mClipRect.left ? 0.0f : (mBitmapRect.left - mClipRect.left) / mBitmapRect.width());
        final float right = mBitmapRect.right <= mClipRect.right ? 1.0f : 1.0f - (mBitmapRect.right - mClipRect.right) / mBitmapRect.width();
        final float top = -(mBitmapRect.top >= mClipRect.top ? 0.0f : (mBitmapRect.top - mClipRect.top) / mBitmapRect.height());
        final float bottom = mBitmapRect.bottom <= mClipRect.bottom ? 1.0f : 1.0f - (mBitmapRect.bottom - mClipRect.bottom) / mBitmapRect.height();
        calculateRectfByCurrentRotate(mLoadPercentRectF, left, top, right, bottom);
//        final int rotate = mClipRotateDegree / 90 % 4;
//        switch (rotate) {
//            case 1:
//            case -3:
//                mLoadPercentRectF.left = top;
//                mLoadPercentRectF.top = 1.0f - right;
//                mLoadPercentRectF.right = bottom;
//                mLoadPercentRectF.bottom = 1.0f - left;
//                break;
//            case 2:
//            case -2:
//                mLoadPercentRectF.left = 1.0f - right;
//                mLoadPercentRectF.top = 1.0f - bottom;
//                mLoadPercentRectF.right = 1.0f - left;
//                mLoadPercentRectF.bottom = 1.0f - top;
//                break;
//            case 3:
//            case -1:
//                mLoadPercentRectF.left = 1.0f - bottom;
//                mLoadPercentRectF.top = left;
//                mLoadPercentRectF.right = 1.0f - top;
//                mLoadPercentRectF.bottom = right;
//                break;
//            default:
//                mLoadPercentRectF.left = left;
//                mLoadPercentRectF.top = top;
//                mLoadPercentRectF.right = right;
//                mLoadPercentRectF.bottom = bottom;
//                break;
//        }
        if (mLoadHighQualityTask == null) {
            mLoadHighQualityTask = new LoadHighQualityTask(mLoadPercentRectF);
        } else {
            mLoadHighQualityTask.updateLoadingRectF(mLoadPercentRectF);
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(mLoadHighQualityTask, 1000);
    }

    /**
     * 有图片url（setUrl(String url)）的情况下，以尽量高的质量读取裁剪区域的Bitmap。
     *
     * @param url      图片的url地址
     * @param callback 接收裁剪后的Bitmap的回调对象。
     */
    private void clipByLoadHighQuality(final String url, final BitmapClipCallback callback) {
        final RectF loadPercent = new RectF();
        final float left = -(mBitmapRect.left >= mClipRect.left ? 0.0f : (mBitmapRect.left - mClipRect.left) / mBitmapRect.width());
        final float right = mBitmapRect.right <= mClipRect.right ? 1.0f : 1.0f - (mBitmapRect.right - mClipRect.right) / mBitmapRect.width();
        final float top = -(mBitmapRect.top >= mClipRect.top ? 0.0f : (mBitmapRect.top - mClipRect.top) / mBitmapRect.height());
        final float bottom = mBitmapRect.bottom <= mClipRect.bottom ? 1.0f : 1.0f - (mBitmapRect.bottom - mClipRect.bottom) / mBitmapRect.height();
        calculateRectfByCurrentRotate(loadPercent, left, top, right, bottom);
//        final int rotate = mClipRotateDegree / 90 % 4;
//        switch (rotate) {
//            case 1:
//            case -3:
//                loadPercent.left = top;
//                loadPercent.top = 1.0f - right;
//                loadPercent.right = bottom;
//                loadPercent.bottom = 1.0f - left;
//                break;
//            case 2:
//            case -2:
//                loadPercent.left = 1.0f - right;
//                loadPercent.top = 1.0f - bottom;
//                loadPercent.right = 1.0f - left;
//                loadPercent.bottom = 1.0f - top;
//                break;
//            case 3:
//            case -1:
//                loadPercent.left = 1.0f - bottom;
//                loadPercent.top = left;
//                loadPercent.right = 1.0f - top;
//                loadPercent.bottom = right;
//                break;
//            default:
//                loadPercent.left = left;
//                loadPercent.top = top;
//                loadPercent.right = right;
//                loadPercent.bottom = bottom;
//                break;
//        }
        Task task = new Task() {
            @Override
            protected void work() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(url, options);
                options.inJustDecodeBounds = false;
                Runtime runtime = Runtime.getRuntime();
                float availableSize = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) / 32.0f / 8.0f;
                Rect rect = new Rect();
                rect.left = (int) (loadPercent.left * options.outWidth);
                rect.right = (int) (loadPercent.right * options.outWidth);
                rect.top = (int) (loadPercent.top * options.outHeight);
                rect.bottom = (int) (loadPercent.bottom * options.outHeight);
                float percent = rect.width() * 1.0f / rect.height();
                int availableW = (int) (availableSize / percent);
                int availableH = (int) (availableSize * percent);
                options.inSampleSize = Math.min(rect.width() / availableW, rect.height() / availableH);
                if (options.inSampleSize < 1)
                    options.inSampleSize = 1;
                BitmapRegionDecoder bitmapRegionDecoder = null;
                try {
                    bitmapRegionDecoder = BitmapRegionDecoder.newInstance(url, true);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                final Bitmap bitmap = resizeBitmap(bitmapRegionDecoder.decodeRegion(rect, options), mClipSize, false);
                if (callback != null) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onClipCompleted(bitmap);
                            mFlags &= ~FLAG_CLIPPING;
                        }
                    });
                } else {
                    if (bitmap != null && !bitmap.isRecycled())
                        bitmap.recycle();
                    mFlags &= ~FLAG_CLIPPING;
                }

            }
        };
        mExecutor.execute(task);
    }

    /**
     * 没有图片url（直接setBitmap(Bitmap bitmap)）的情况下，直接裁剪显示的Bitmap。
     *
     * @param callback 接收裁剪后的Bitmap的回调对象。
     */
    private void clipByDisplayBitmap(final BitmapClipCallback callback) {
        Task task = new Task() {
            @Override
            protected void work() {
                Bitmap copyBitmap = Bitmap.createBitmap(mWH[0], mWH[1], Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(copyBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(mBitmap, mCurrMatrix, null);
                final Bitmap bitmap = resizeBitmap(
                        Bitmap.createBitmap(copyBitmap, (int) mClipRect.left, (int) mClipRect.top, (int) mClipRect.width(), (int) mClipRect.height()),
                        mClipSize, true);
                if (!copyBitmap.isRecycled())
                    copyBitmap.recycle();
                if (callback != null) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onClipCompleted(bitmap);
                            mFlags &= ~FLAG_CLIPPING;
                        }
                    });
                } else {
                    mFlags &= ~FLAG_CLIPPING;
                }

            }
        };
        mExecutor.execute(task);
    }

    /**
     * 有图片url（setUrl(String url)）的情况下，以尽量高的质量读取裁剪区域的Bitmap。
     *
     * @param bitmap          图片
     * @param size            需要裁剪的大小
     * @param isBitmapRotated 传入的bitmap是否被旋转过（直接用{@link #mBitmap}裁剪时 不需要再旋转）
     * @return 符合传入size的Bitmap
     */
    private Bitmap resizeBitmap(final Bitmap bitmap, int[] size, boolean isBitmapRotated) {
        if (bitmap.getWidth() != size[0] || bitmap.getHeight() != size[1]) {
            float percent = 1.0f;
            int resizeWidth = size[0];
            int resizeHeight = size[1];
            Matrix matrix = new Matrix();
            if (!isBitmapRotated) { //
                final int rotate = mClipRotateDegree / 90 % 4;
                switch (rotate) {
                    case 1:
                    case -3:
                        percent = size[0] * 1.0f / bitmap.getHeight();
                        matrix.postTranslate(bitmap.getHeight(), 0);
                        matrix.postRotate(90.0f, bitmap.getHeight(), 0);
                        resizeWidth = size[1];
                        resizeHeight = size[0];
                        break;
                    case 2:
                    case -2:
                        percent = size[0] * 1.0f / bitmap.getWidth();
                        matrix.postRotate(180.0f, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
                        resizeWidth = size[0];
                        resizeHeight = size[1];
                        break;
                    case 3:
                    case -1:
                        percent = size[0] * 1.0f / bitmap.getHeight();
                        matrix.postTranslate(-bitmap.getWidth(), 0);
                        matrix.postRotate(-90.0f, 0, 0);
                        resizeWidth = size[1];
                        resizeHeight = size[0];
                        break;
                    default:
                        percent = size[0] * 1.0f / bitmap.getWidth();
                        resizeWidth = size[0];
                        resizeHeight = size[1];
                        break;
                }
            } else {
                percent = size[0] * 1.0f / bitmap.getWidth();
            }
            matrix.postScale(percent, percent);
            final Bitmap resizedBitmap = Bitmap.createBitmap(resizeWidth, resizeHeight, Bitmap.Config.ARGB_8888);
            Canvas resizedCanvas = new Canvas(resizedBitmap);
            resizedCanvas.drawBitmap(bitmap, matrix, null);
            if (!bitmap.isRecycled())
                bitmap.recycle();
            return resizedBitmap;
        } else {
            return bitmap;
        }
    }

    private void recycleClipBitmap() {
        if (this.mClipBitmap != null && !this.mClipBitmap.isRecycled())
            this.mClipBitmap.recycle();
        this.mClipBitmap = null;
    }

    private class LoadHighQualityTask implements Runnable {
        private RectF mLoadingRect = new RectF();

        public LoadHighQualityTask(RectF rectF) {
            this.mLoadingRect.left = rectF.left;
            this.mLoadingRect.right = rectF.right;
            this.mLoadingRect.top = rectF.top;
            this.mLoadingRect.bottom = rectF.bottom;
        }

        public LoadHighQualityTask updateLoadingRectF(RectF rectF) {
            this.mLoadingRect.left = rectF.left;
            this.mLoadingRect.right = rectF.right;
            this.mLoadingRect.top = rectF.top;
            this.mLoadingRect.bottom = rectF.bottom;
            return this;
        }

        @Override
        public void run() {
            mExecutor.execute(new PhotoLoadTask(mUrl, mLoadingRect, ClipPhotoView.this));
        }
    }

    private class BitmapRotateTask extends Task {

        @Override
        protected void work() {
            final Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmapWH[1], mBitmapWH[0], Bitmap.Config.ARGB_4444);
            Canvas rotatedCanvas = new Canvas(rotatedBitmap);
            rotatedCanvas.drawColor(Color.RED);
            rotatedCanvas.drawBitmap(mBitmap, mBitmapRotateMatrix, null);
            recycleClipBitmap();
            setBitmap(rotatedBitmap);
            mBitmapRotateMatrix.reset();
            mFlags &= ~FLAG_ROTATING;
        }
    }
}
