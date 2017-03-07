package lx.photopicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.List;

import lx.photopicker.R;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.util.PhotoUtil;

/**
 * <b>相册图片列表的RecyclerView的Adapter</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class FolderDetailRecyclerViewAdapter extends RecyclerView.Adapter
{
	private OnPhotoPickedListener mOnPhotoPickedListener;
	private OnPhotoQuickPickedListener mOnPhotoQuickPickedListener;

	public OnPhotoPickedListener getOnFolderSelectedListener()
	{
		return mOnPhotoPickedListener;
	}

	public void setOnPhotoPickedListener(OnPhotoPickedListener mOnPhotoPickedListener)
	{
		this.mOnPhotoPickedListener = mOnPhotoPickedListener;
	}

	public OnPhotoQuickPickedListener getOnPhotoQuickPickedListener()
	{
		return mOnPhotoQuickPickedListener;
	}

	public void setOnPhotoQuickPickedListener(OnPhotoQuickPickedListener mOnPhotoQuickPickedListener)
	{
		this.mOnPhotoQuickPickedListener = mOnPhotoQuickPickedListener;
	}

	private LayoutInflater mInflater;
	private List<PhotoEntity> mPhotos;
	private boolean isQuickable;//是否可以快速选择

	public FolderDetailRecyclerViewAdapter(LayoutInflater inflater, List<PhotoEntity> photos, boolean isQuickable)
	{
		this.mInflater = inflater;
		this.mPhotos = photos;
		this.isQuickable = isQuickable;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new InnerHolder(mInflater.inflate(R.layout.item_folderdetail_detail, parent, false), isQuickable);
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

	public void update(List<PhotoEntity> list)
	{
		if (list == null)
			return;
		mPhotos.clear();
		mPhotos.addAll(list);
		notifyDataSetChanged();
	}

	public void refresh()
	{
		notifyDataSetChanged();
	}

	private class InnerHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private ImageView folderDetail_item_iv_img;//图片预览
		private CheckBox folderDetail_item_cb_isPicked;//右上角的CheckBox
		private boolean isQuickable;//是否可以快速选择

		public InnerHolder(View itemView, boolean isQuickable)
		{
			super(itemView);
			this.isQuickable = isQuickable;
			initView(itemView);
		}

		private void initView(View itemView)
		{
			folderDetail_item_iv_img = (ImageView) itemView.findViewById(R.id.folderDetail_item_iv_img);
			folderDetail_item_cb_isPicked = (CheckBox) itemView.findViewById(R.id.folderDetail_item_cb_isPicked);
			folderDetail_item_cb_isPicked.setVisibility(isQuickable ? View.VISIBLE : View.GONE);//如果是单选模式，GONE掉CheckBox
			if (isQuickable)
				folderDetail_item_cb_isPicked.setOnClickListener(this);//因为CheckBox可能会被其他非点击方式改变，所以不适用OnCheckedChangeListener，而使用OnClickListener。

		}

		public void setData(PhotoEntity photo)
		{
			folderDetail_item_cb_isPicked.setChecked(photo.isPicked());
			itemView.setTag(R.id.folderDetail, photo);
			folderDetail_item_iv_img.setOnClickListener(this);
			PhotoUtil.loadListImage(photo.getPath(), folderDetail_item_iv_img);
		}

		@Override
		public void onClick(View v)
		{
			if (v == folderDetail_item_iv_img)
			{
				PhotoEntity photo = (PhotoEntity) itemView.getTag(R.id.folderDetail);
				if (mOnPhotoPickedListener != null)
				{//Photo被点击
					if (photo.isPicked())
					{
						mOnPhotoPickedListener.onPhotoRepicked(photo);
					} else
					{
						mOnPhotoPickedListener.onPhotoPicked(photo);
					}
				}
			}
			if (v == folderDetail_item_cb_isPicked)
			{//CheckBox被点击改变
				PhotoEntity photo = (PhotoEntity) itemView.getTag(R.id.folderDetail);
				if (photo.isPicked())
				{
					if (mOnPhotoQuickPickedListener != null)
						mOnPhotoQuickPickedListener.onPhotoQuickRemoved(photo);

				} else
				{
					if (mOnPhotoQuickPickedListener != null)
						mOnPhotoQuickPickedListener.onPhotoQuickPicked(photo);
				}
			}
		}
	}
}
