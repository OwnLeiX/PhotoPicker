package lx.photopicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import lx.photopicker.R;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.PhotoUtil;

/**
 * <b>已经选择的Photo的RecyclerView的Adapter</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class PickedPhotosRecyclerViewAdapter extends RecyclerView.Adapter
{

	private OnPhotoRemovedListener mOnPhotoRemovedListener;

	public OnPhotoRemovedListener getOnPhotoRemovedListener() {
		return mOnPhotoRemovedListener;
	}

	public void setOnPhotoRemovedListener(OnPhotoRemovedListener mOnPhotoRemovedListener) {
		this.mOnPhotoRemovedListener = mOnPhotoRemovedListener;
	}

	private LayoutInflater mInflater;
	private List<PhotoEntity> mPhotos;

	public PickedPhotosRecyclerViewAdapter(LayoutInflater inflater, List<PhotoEntity> photos)
	{
		this.mInflater = inflater;
		this.mPhotos = photos;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new InnerHolder(mInflater.inflate(R.layout.item_picked_photos, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
	{
		((InnerHolder) holder).setData(mPhotos.get(position));
	}

	@Override
	public int getItemCount()
	{
		return mPhotos == null ? 0 : mPhotos.size();
	}

	public void refresh()
	{
		notifyDataSetChanged();
	}

	private class InnerHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private ImageView pickedPhotos_item_iv_img;//图片预览
		private ImageView pickedPhotos_item_iv_deleted;//右上角删除按钮

		public InnerHolder(View itemView)
		{
			super(itemView);
			initView(itemView);
		}

		private void initView(View itemView)
		{
			itemView.setClickable(true);
			pickedPhotos_item_iv_img = (ImageView) itemView.findViewById(R.id.pickedPhotos_item_iv_img);
			pickedPhotos_item_iv_deleted = (ImageView) itemView.findViewById(R.id.pickedPhotos_item_iv_deleted);
			pickedPhotos_item_iv_deleted.setOnClickListener(this);
		}

		public void setData(PhotoEntity photo)
		{
			pickedPhotos_item_iv_deleted.setTag(R.id.pickedPhotos, photo);
			PhotoUtil.loadListImage(photo.getPath(), pickedPhotos_item_iv_img);
		}

		@Override
		public void onClick(View v)
		{
			if (v == pickedPhotos_item_iv_deleted) {//右上角删除按钮被点击，回调删除图片
				PhotoEntity photo = (PhotoEntity) v.getTag(R.id.pickedPhotos);
				if (mOnPhotoRemovedListener != null)
					mOnPhotoRemovedListener.onPhotoRemoved(photo);
			}
		}
	}
}
