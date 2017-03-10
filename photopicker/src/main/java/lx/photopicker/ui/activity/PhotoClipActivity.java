package lx.photopicker.ui.activity;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import lx.photopicker.PhotoParams;
import lx.photopicker.R;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.kernel.PickerManager;
import lx.photopicker.kernel.callback.PhotoZipCallback;
import lx.photopicker.kernel.task.PhotoZipPoolTask;
import lx.photopicker.ui.widget.clipphotoview.ClipPhotoView;
import lx.photopicker.ui.widget.clipphotoview.callback.BitmapClipCallback;
import lx.photopicker.util.Pool;

/**
 * <b>浏览单个图片，并确认选择的Activity</b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class PhotoClipActivity extends BaseActivity implements View.OnClickListener, PhotoZipCallback, BitmapClipCallback {
	private ClipPhotoView photoClip_cpv_photo;
	private TextView photoClip_tv_pick;//确定和取消的按钮
	private FrameLayout photoClip_fl_back;
	private CheckBox photoClip_cb_native;
	private PhotoParams mPhotoParams;//本次Pick的Photo参数
	private PhotoEntity mCurrentPhoto;//当前显示的Photo

	@Override
	protected View onInitContentView()
	{
		return getLayoutInflater().inflate(R.layout.activity_photoclip_layout, null, false);
	}

	@Override
	protected void onInitView()
	{
		mPhotoParams = PickerManager.$().getPhotoParams();
		photoClip_cpv_photo = (ClipPhotoView) findViewById(R.id.photoClip_cpv_photo);
		photoClip_tv_pick = (TextView) findViewById(R.id.photoClip_tv_pick);
		photoClip_fl_back = (FrameLayout) findViewById(R.id.photoClip_fl_back);
		photoClip_cb_native = (CheckBox) findViewById(R.id.photoClip_cb_native);
		if (mPhotoParams.isClip() && mPhotoParams.getClipSize() != null)
		{
			photoClip_cpv_photo.setClipMode(ClipPhotoView.MODE_CLIP);
			photoClip_cpv_photo.setClipSize(mPhotoParams.getClipSize());
		}
	}

	@Override
	protected void onInitListener()
	{
		photoClip_tv_pick.setOnClickListener(this);
		photoClip_fl_back.setOnClickListener(this);
	}

	@Override
	protected void onInitData()
	{
		mCurrentPhoto = PickerManager.$().getCurrentPhoto();
		photoClip_cpv_photo.setExecutor(Pool.$());
		photoClip_cpv_photo.setUrl(mCurrentPhoto.getPath());
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.photoClip_fl_back)
		{
			finishByCancel();
		} else if (v.getId() == R.id.photoClip_tv_pick)
		{
			if (mPhotoParams.isClip())
			{//如果是裁剪模式，裁剪图片
				clipPhoto();
			} else
			{//如果不是裁剪模式，直接将图片添加到选择列表
				setNeedNative(mCurrentPhoto);
				PickerManager.$().addPickedPhoto(mCurrentPhoto);
				if (mPhotoParams.isMulti())
				{//如果是复选模式，则返回上一页
					finishByCancel();
				} else
				{//如果是单选模式，则直接结束
					finishByComplete();
				}
			}
		}
	}

	/**
	 * 点击手机返回键，视为取消操作
	 */
	@Override
	public void onBackPressed()
	{
		finishByCancel();
	}

	/**
	 * (裁剪模式才会调用)将图片裁剪至指定大小的方法
	 */
	private void clipPhoto()
	{
		if (photoClip_cpv_photo.canClip())
		{
			photoClip_cpv_photo.clipBitmap(this);
		}
	}

	/**
	 * 根据右上角的CheckBox来给PhotoEntity设置是否需要原图。
	 */
	private void setNeedNative(PhotoEntity photo)
	{
		if (photoClip_cb_native.isChecked())
		{
			photo.setNeedNative(true);
		} else
		{
			photo.setNeedNative(false);
		}
	}

	private boolean isNeedNative()
	{
		return photoClip_cb_native.isChecked();
	}

	@Override
	public void onZipFinished(PhotoEntity photo)
	{
		photo.setPicked(true);
		setNeedNative(photo);
		PickerManager.$().addPickedPhoto(photo);
		if (mPhotoParams.isMulti())
		{
			finishByCancel();
		} else
		{
			finishByComplete();
		}
	}

	@Override
	public void onZipError(Throwable e)
	{
		showShortToast("啊哦，出错了");
		e.printStackTrace();
	}

	@Override
	public void onClipCompleted(Bitmap bitmap)
	{
		String path = getCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
		Pool.execute(new PhotoZipPoolTask(bitmap, path, mPhotoParams, this));
	}
}
