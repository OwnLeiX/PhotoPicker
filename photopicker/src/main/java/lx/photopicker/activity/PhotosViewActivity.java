package lx.photopicker.activity;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lx.photopicker.R;
import lx.photopicker.adapter.PhotoViewViewPagerAdapter;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.PickerManager;

/**
 * <b>浏览已经选则的图片列表的Activity</b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class PhotosViewActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener
{

	private TextView photoView_tv_count;
	private FrameLayout photoView_fl_back;
	private CheckBox photoView_cb_picked;
	private TextView photoView_tv_complete;
	private ViewPager photoView_vp_content;
	private PhotoViewViewPagerAdapter mAdapter;

	@Override
	protected View onInitContentView()
	{
		return getLayoutInflater().inflate(R.layout.activity_photoview_layout, null, false);
	}

	@Override
	protected void onInitView()
	{
		photoView_tv_count = (TextView) findViewById(R.id.photoView_tv_count);
		photoView_fl_back = (FrameLayout) findViewById(R.id.photoView_fl_back);
		photoView_cb_picked = (CheckBox) findViewById(R.id.photoView_cb_picked);
		photoView_tv_complete = (TextView) findViewById(R.id.photoView_tv_complete);
		photoView_vp_content = (ViewPager) findViewById(R.id.photoView_vp_content);
	}

	@Override
	protected void onInitListener()
	{
		photoView_tv_complete.setOnClickListener(this);
		photoView_fl_back.setOnClickListener(this);
		photoView_cb_picked.setOnClickListener(this);
		photoView_vp_content.addOnPageChangeListener(this);
	}

	@Override
	protected void onInitData()
	{
		List<PhotoEntity> pickedPhotos = PickerManager.$().getPickedPhotos();
		mAdapter = new PhotoViewViewPagerAdapter(new ArrayList<>(pickedPhotos));
		photoView_vp_content.setAdapter(mAdapter);
        refreshCount();
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.photoView_tv_complete)
		{
			finishByComplete();
		} else if (v.getId() == R.id.photoView_fl_back)
		{
			finishByCancel();
		} else if (v.getId() == R.id.photoView_cb_picked)
		{
			boolean checked = photoView_cb_picked.isChecked();
			int currentItem = photoView_vp_content.getCurrentItem();
			PhotoEntity photo = mAdapter.getPhotoAtPosition(currentItem);
			if (!checked)
			{
				PickerManager.$().removePickedPhoto(photo);
			} else
			{
				PickerManager.$().addPickedPhoto(photo);
			}
			refreshCount();
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{

	}

	@Override
	public void onPageSelected(int position)
	{
		PhotoEntity photo = mAdapter.getPhotoAtPosition(position);
		photoView_cb_picked.setChecked(photo.isPicked());
	}

	@Override
	public void onPageScrollStateChanged(int state)
	{

	}

	private void refreshCount()
	{
		int count = PickerManager.$().getPickedPhotos().size();
		photoView_tv_count.setText(String.valueOf(count));
		photoView_tv_complete.setEnabled(count > 0);
	}
}
