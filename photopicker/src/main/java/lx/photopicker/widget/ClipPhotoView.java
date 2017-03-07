package lx.photopicker.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import lx.photopicker.task.PhotoLoadCallback;
import lx.photopicker.task.PhotoLoadTask;
import lx.photopicker.util.Pool;

/**
 * <b></b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class ClipPhotoView extends View implements PhotoLoadCallback
{

	public interface BitmapClipCallback
	{
		void onClipFinished(Bitmap bitmap);
	}

	public static final int MODE_NONE = 1;
	public static final int MODE_CLIP = 2;

	private int mMode = MODE_NONE;
	private Paint mPaint;
	private Path mOverlapPath = new Path();
	private int mOverlapColor = 0xAA000000;
	private RectF mClipRect = new RectF();
	private int[] mClipSize = new int[] { 200, 200 };
	private int[] mBitmapWH = new int[2];
	private RectF mBitmapRect = new RectF();

	private volatile Bitmap mBitmap;
	private int[] mWH = new int[2];

	private boolean isAnimating = false;
	private volatile boolean isClipping = false;
	private boolean isFirstLoad = true;

	// 縮放控制
	private volatile Matrix mCurrMatrix = new Matrix();
	private volatile Matrix mSavedMatrix = new Matrix();

	// 不同状态的表示：
	private static final int TOUCH_NONE = 0;
	private static final int TOUCH_DRAG = 1;
	private static final int TOUCH_ZOOM = 2;
	private int mTouchMode = TOUCH_NONE;

	// 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
	private PointF mStartPoint = new PointF();
	private PointF mMidPoint = new PointF();
	private float oriDis = 1f;
	private float mMinScale;
	private float mPreScale;

	private PhotoLoadTask mLoadTask;
	private boolean isZipped;
	private String mUrl;
	private boolean isRegionMode = false;

	public ClipPhotoView(Context context)
	{
		super(context);
		ownInit();
	}

	public ClipPhotoView(Context context, AttributeSet attr)
	{
		super(context, attr);
		ownInit();
	}

	public ClipPhotoView(Context context, AttributeSet attr, int defStyle)
	{
		super(context, attr, defStyle);
		ownInit();
	}

	private void ownInit()
	{
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(mOverlapColor);
		mPaint.setFilterBitmap(true);
		mPaint.setStrokeWidth(1);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		mWH[0] = w;
		mWH[1] = h;
		setClipRect();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (mBitmap != null)
		{
			if (!isRegionMode)
			{
				canvas.drawBitmap(mBitmap, mCurrMatrix, null);
			} else
			{
				canvas.save();
				canvas.translate(mWH[0] * mRegionTrans[2], mWH[1] * mRegionTrans[3]);
				canvas.drawBitmap(mBitmap, mCurrMatrix, null);
				canvas.restore();
			}
		}
		if (mMode == MODE_NONE)
			return;
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mOverlapColor);
		canvas.drawPath(mOverlapPath, mPaint);
		canvas.drawLine(mClipRect.left, mClipRect.centerY(), mClipRect.right, mClipRect.centerY(), mPaint);
		canvas.drawLine(mClipRect.centerX(), mClipRect.top, mClipRect.centerX(), mClipRect.bottom, mPaint);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(0xFF000000);
		canvas.drawPath(mOverlapPath, mPaint);
	}

	private void setClipRect()
	{
		if (mMode == MODE_CLIP)
		{
			float percent = mClipSize[0] * 1.0f / mClipSize[1];
			float h = mWH[0] /percent;
			if (h <= mWH[1])
			{
				mClipRect.left = 20;
				mClipRect.right = mWH[0] - 20;
				float rectHeight = mClipRect.width() / percent;
				mClipRect.top = mWH[1] / 2.0f - rectHeight / 2.0f;
				mClipRect.bottom = mWH[1] / 2.0f + rectHeight / 2.0f;
				mOverlapPath.reset();
				mOverlapPath.addRect(0, 0, mWH[0], mWH[1], Path.Direction.CW);
				mOverlapPath.addRect(mClipRect, Path.Direction.CCW);
			} else
			{
				mClipRect.top = 20;
				mClipRect.bottom = mWH[1] - 20;
				float rectWidth = mClipRect.height() * percent;
				mClipRect.left = mWH[0] / 2.0f - rectWidth / 2.0f;
				mClipRect.right = mWH[0] / 2.0f + rectWidth / 2.0f;
				mOverlapPath.reset();
				mOverlapPath.addRect(0, 0, mWH[0], mWH[1], Path.Direction.CW);
				mOverlapPath.addRect(mClipRect, Path.Direction.CCW);
			}
		} else if (mMode == MODE_NONE)
		{
			mClipRect.left = 0;
			mClipRect.right = mWH[0];
			mClipRect.top = 0;
			mClipRect.bottom = mWH[1];
			if (mBitmap != null) {
				float percent = mBitmap.getWidth() * 1.0f / mBitmap.getHeight();
				float h = mWH[0] /percent;
				if (h <= mWH[1]) {
					mClipRect.left = 0;
					mClipRect.right = mWH[0];
					float rectHeight = mClipRect.width() / percent;
					mClipRect.top = mWH[1] / 2.0f - rectHeight / 2.0f;
					mClipRect.bottom = mWH[1] / 2.0f + rectHeight / 2.0f;
				}else {
					mClipRect.top = 0;
					mClipRect.bottom = mWH[1];
					float rectWidth = mClipRect.height() * percent;
					mClipRect.left = mWH[0] / 2.0f - rectWidth / 2.0f;
					mClipRect.right = mWH[0] / 2.0f + rectWidth / 2.0f;
				}
			}

		}

		if (mBitmap != null)
		{
			mBitmapWH[0] = mBitmap.getWidth();
			mBitmapWH[1] = mBitmap.getHeight();
			mBitmapRect.left = 0;
			mBitmapRect.right = mBitmapWH[0];
			mBitmapRect.top = 0;
			mBitmapRect.bottom = mBitmapWH[1];
			float transX = mWH[0] / 2.0f - mBitmapWH[0] / 2.0f;
			float transY = mWH[1] / 2.0f - mBitmapWH[1] / 2.0f;
			mCurrMatrix.setTranslate(transX, transY);
			translateBitmapRect();
			mMinScale = Math.max(mClipRect.width() / mBitmapWH[0], mClipRect.height() / mBitmapWH[1]);
			mPreScale = mMinScale;
			mCurrMatrix.postScale(mMinScale, mMinScale, mBitmapRect.centerX(), mBitmapRect.centerY());
			scaleBitmapRect();
		}
	}

	private void translateBitmapRect()
	{
		float[] values = new float[9];
		mCurrMatrix.getValues(values);
		mBitmapRect.offsetTo(values[2], values[5]);
	}

	private void scaleBitmapRect()
	{
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

	public void setClipSize(int[] size)
	{
		mClipSize = size;
	}

	public void setClipMode(int mode)
	{
		mMode = mode;
	}

	public boolean canClip()
	{
		return mBitmap != null && !isClipping && !isAnimating;
	}

	public void clipBitmap(final BitmapClipCallback callback)
	{
		isClipping = true;
		Pool.Task task = new Pool.Task()
		{
			@Override
			protected void work()
			{
				Bitmap copyBitmap = Bitmap.createBitmap(mWH[0], mWH[1], Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(copyBitmap);
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(mBitmap, mCurrMatrix, null);
				final Bitmap bitmap = Bitmap.createBitmap(copyBitmap, (int) mClipRect.left, (int) mClipRect.top, (int) mClipRect.width(),
						(int) mClipRect.height());
				if (!copyBitmap.isRecycled())
					copyBitmap.recycle();
				if (bitmap.getWidth() != mClipSize[0])
				{
					float percent = mClipSize[0] * 1.0f / bitmap.getWidth();
					final Bitmap resizedBitmap = Bitmap.createBitmap(mClipSize[0], mClipSize[1], Bitmap.Config.ARGB_8888);
					Canvas resizedCanvas = new Canvas(resizedBitmap);
					Matrix matrix = new Matrix();
					matrix.setScale(percent, percent);
					resizedCanvas.drawBitmap(bitmap, matrix, null);
					if (!bitmap.isRecycled())
						bitmap.recycle();
					if (callback != null)
					{
						getHandler().post(new Runnable()
						{
							@Override
							public void run()
							{
								callback.onClipFinished(resizedBitmap);
							}
						});
					}
				} else
				{
					if (callback != null)
					{
						getHandler().post(new Runnable()
						{
							@Override
							public void run()
							{
								callback.onClipFinished(bitmap);
							}
						});
					}

				}
				isClipping = false;
			}
		};
		Pool.execute(task);
	}

	// 计算两个触摸点之间的距离
	private float calculateDistance(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	// 计算两个触摸点的中点
	private PointF calculateMiddle(MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		return new PointF(x / 2, y / 2);
	}

	//校准是否拖拽超出了裁剪框边界
	private void clampDragRange()
	{
		float[] values = new float[9];
		mCurrMatrix.getValues(values);
		float clampX = 0;
		float clampY = 0;
		if (mBitmapRect.left > mClipRect.left)
		{
			clampX = mClipRect.left - mBitmapRect.left;
		} else if (mBitmapRect.right < mClipRect.right)
		{
			clampX = mClipRect.right - mBitmapRect.right;
		}
		if (mBitmapRect.top > mClipRect.top)
		{
			clampY = mClipRect.top - mBitmapRect.top;
		} else if (mBitmapRect.bottom < mClipRect.bottom)
		{
			clampY = mClipRect.bottom - mBitmapRect.bottom;
		}
		final float dX = clampX;
		final float dY = clampY;
		if (dX != 0 || dY != 0)
		{
			ValueAnimator va = ValueAnimator.ofFloat(0.0f, 1.0f);
			va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator animation)
				{
					float currX = animation.getAnimatedFraction() * dX;
					float currY = animation.getAnimatedFraction() * dY;
					mCurrMatrix.set(mSavedMatrix);
					mCurrMatrix.postTranslate(currX, currY);
					translateBitmapRect();
					invalidate();
				}
			});
			va.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mSavedMatrix.set(mCurrMatrix);
					invalidate();
					isAnimating = false;
				}
			});
			va.setDuration((long) Math.min(250.0f, Math.max(Math.abs(dX), Math.abs(dY)) * 10.0f));
			isAnimating = true;
			va.start();
		} else
		{
			isAnimating = false;
		}
	}

	private void clampScaleRange()
	{

		float[] values = new float[9];
		mCurrMatrix.getValues(values);
		if (values[0] < mMinScale)
		{
			final float result = mMinScale / values[0];
			com.nineoldandroids.animation.ValueAnimator va = com.nineoldandroids.animation.ValueAnimator.ofFloat(1.0f, result);
			va.setDuration((long) Math.min(250.0f, Math.abs(result * 50.0f)));
			va.addUpdateListener(new com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(com.nineoldandroids.animation.ValueAnimator animation)
				{
					final float curr = (float) animation.getAnimatedValue();
					mCurrMatrix.set(mSavedMatrix);
					mCurrMatrix.postScale(curr, curr, mBitmapRect.centerX(), mBitmapRect.centerY());
					scaleBitmapRect();
					invalidate();
				}
			});
			va.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mSavedMatrix.set(mCurrMatrix);
					scaleBitmapRect();
					invalidate();
					translateBitmapRect();
					clampDragRange();
				}
			});
			isAnimating = true;
			va.start();
		} else
		{
			clampDragRange();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (isAnimating)
			return super.onTouchEvent(event);
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		// 单指
		case MotionEvent.ACTION_DOWN:
			mSavedMatrix.set(mCurrMatrix);
			mStartPoint.set(event.getX(), event.getY());
			mTouchMode = TOUCH_DRAG;
			break;
		// 双指
		case MotionEvent.ACTION_POINTER_DOWN:
			getParent().requestDisallowInterceptTouchEvent(true);
			oriDis = calculateDistance(event);
			if (oriDis > 10f)
			{
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
			if (mTouchMode == TOUCH_ZOOM)
			{
				clampScaleRange();
			} else if (mTouchMode == TOUCH_DRAG)
			{
				clampDragRange();
			}
			mTouchMode = TOUCH_NONE;
			return false;
		// 单指滑动事件
		case MotionEvent.ACTION_MOVE:
			if (mTouchMode == TOUCH_DRAG)
			{
				// 是一个手指拖动
				mCurrMatrix.set(mSavedMatrix);
				mCurrMatrix.postTranslate(event.getX() - mStartPoint.x, event.getY() - mStartPoint.y);
				translateBitmapRect();
			} else if (mTouchMode == TOUCH_ZOOM)
			{
				// 两个手指滑动
				float newDist = calculateDistance(event);
				if (newDist > 10f)
				{
					mCurrMatrix.set(mSavedMatrix);
					float scale = newDist / oriDis;
					mCurrMatrix.postScale(scale, scale, mBitmapRect.centerX(), mBitmapRect.centerY());
					scaleBitmapRect();
				}
			}
			break;
		}
		invalidate();
		return true;
	}

	public Bitmap getBitmap()
	{
		return mBitmap;
	}

	public void setBitmap(Bitmap bitmap)
	{
		isFirstLoad = false;
		if (this.mBitmap != null && !this.mBitmap.isRecycled())
			this.mBitmap.recycle();
		this.mBitmap = bitmap;
		setClipRect();
		postInvalidate();
	}

	public void setPath(String path)
	{
		mUrl = path;
		isFirstLoad = true;
		mLoadTask = new PhotoLoadTask(path, this);
		Pool.execute(mLoadTask);
	}

	@Override
	public void onLoadFinished(Bitmap bitmap, boolean isZipped)
	{
		if (isFirstLoad)
		{
			this.isZipped = isZipped;
			setBitmap(bitmap);
		} else
		{
			if (this.mBitmap != null && !this.mBitmap.isRecycled())
				this.mBitmap.recycle();
			this.mBitmap = bitmap;
			isRegionMode = true;
			invalidate();
		}
	}

	//TODO 配合缩放这个太难算了..
	private void calculateIsNeedLoad()
	{
		if (!isZipped)
			return;
		scaleBitmapRect();
		translateBitmapRect();
		float[] values = new float[9];
		mCurrMatrix.getValues(values);
		RectF rectF = new RectF();
		rectF.left = mBitmapRect.left >= 0 ? 0.0f : mBitmapRect.left / mBitmapRect.width();
		rectF.right = mBitmapRect.right <= mWH[0] ? 1.0f : 1.0f - (mBitmapRect.right - (float) mWH[0]) / mBitmapRect.width();
		rectF.top = mBitmapRect.top >= 0 ? 0 : mBitmapRect.top / mBitmapRect.height();
		rectF.bottom = mBitmapRect.bottom <= mWH[1] ? 1.0f : 1.0f - (mBitmapRect.bottom - (float) mWH[1]) / mBitmapRect.height();
		if (mLoadTask == null)
		{
			mLoadTask = new PhotoLoadTask(mUrl, rectF, this);
		} else
		{
			updateRegionTrans(rectF);
			mLoadTask.updateRect(rectF);
		}
		Pool.execute(mLoadTask);
	}

	private volatile float[] mRegionTrans = new float[4];

	private void updateRegionTrans(RectF rectF)
	{
		mRegionTrans[0] = 1.0f - (1.0f - rectF.right) - rectF.left;
		mRegionTrans[1] = 1.0f - (1.0f - rectF.bottom) - rectF.top;
		mRegionTrans[2] = (rectF.left + (1.0f - rectF.right));
		mRegionTrans[3] = (rectF.top + (1.0f - rectF.bottom));
	}

	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		if (mBitmap != null && !mBitmap.isRecycled())
			mBitmap.recycle();
	}
}
