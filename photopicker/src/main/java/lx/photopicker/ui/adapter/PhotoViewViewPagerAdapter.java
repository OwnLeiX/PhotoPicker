package lx.photopicker.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.PhotoUtil;

/**
 * <b></b>
 * Created on 2017/3/6.
 *
 * @author LeiXun
 */

public class PhotoViewViewPagerAdapter extends PagerAdapter
{
	private List<PhotoEntity> mPhotos;

	public PhotoViewViewPagerAdapter(List<PhotoEntity> photos)
	{
		this.mPhotos = photos;
	}

	@Override
	public int getCount()
	{
		return mPhotos == null ? 0 : mPhotos.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		ImageView iv = new ImageView(container.getContext());
		iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		PhotoUtil.loadPickImage(mPhotos.get(position).getPath(),iv);
		container.addView(iv);
		return iv;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
	}

	public PhotoEntity getPhotoAtPosition(int position)
	{
		if (position >= mPhotos.size())
			return null;
		return mPhotos.get(position);
	}
}
