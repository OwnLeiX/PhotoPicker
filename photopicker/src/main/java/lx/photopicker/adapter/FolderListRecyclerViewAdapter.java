package lx.photopicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import lx.photopicker.R;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.entity.PhotoFolderEntity;
import lx.photopicker.util.PhotoUtil;

/**
 * <b>相册文件夹列表的RecyclerView的Adapter</b>
 * Created on 2017/3/3.
 *
 * @author LeiXun
 */

public class FolderListRecyclerViewAdapter extends RecyclerView.Adapter {

    /**
     * RecyclerView条目被点击的的回调接口
     * */
    public interface OnFolderSelectedListener{
        void onFolderSelected(PhotoFolderEntity photoFolderEntity);
    }

    private OnFolderSelectedListener mOnFolderSelectedListener;

    public OnFolderSelectedListener getOnFolderSelectedListener() {
        return mOnFolderSelectedListener;
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener mOnFolderSelectedListener) {
        this.mOnFolderSelectedListener = mOnFolderSelectedListener;
    }

    private LayoutInflater mInflater;
    private List<PhotoFolderEntity> mPhotoFolderEntities;

    public FolderListRecyclerViewAdapter(LayoutInflater inflater, List<PhotoFolderEntity> photoFolderEntities) {
        this.mInflater = inflater;
        this.mPhotoFolderEntities = photoFolderEntities;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InnerHolder(mInflater.inflate(R.layout.item_folderlist_list,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		((InnerHolder) holder).setData(mPhotoFolderEntities.get(position));
    }

    @Override
    public int getItemCount() {
        return mPhotoFolderEntities == null ? 0 : mPhotoFolderEntities.size();
    }

    public void update(List<PhotoFolderEntity> list) {
        if (list == null)
            return;
        mPhotoFolderEntities.clear();
        mPhotoFolderEntities.addAll(list);
        notifyDataSetChanged();
    }

    
    private class InnerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView folderList_item_iv_img, folderList_item_iv_enter;
        private TextView folderList_item_tv_folderName, folderList_item_tv_photosCount;
        public InnerHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View itemView) {
            folderList_item_iv_img = (ImageView) itemView.findViewById(R.id.folderList_item_iv_img);
            folderList_item_iv_enter = (ImageView) itemView.findViewById(R.id.folderList_item_iv_enter);
            folderList_item_tv_folderName = (TextView) itemView.findViewById(R.id.folderList_item_tv_folderName);
            folderList_item_tv_photosCount = (TextView) itemView.findViewById(R.id.folderList_item_tv_photosCount);
        }

        public void setData(PhotoFolderEntity folder) {
            if (folder == null)
            {
                itemView.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
            } else
            {
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(R.id.folderList,folder);
                itemView.setOnClickListener(this);
                folderList_item_tv_folderName.setText(folder.getFolderName());
                List<PhotoEntity> photos = folder.getPhotoEntities();
                if (photos != null)
                {
                    folderList_item_tv_photosCount.setText(String.valueOf(photos.size()));
                    folderList_item_iv_enter.setOnClickListener(this);
                    PhotoUtil.loadListImage(folder.getLastPhoto().getPath(), folderList_item_iv_img);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                PhotoFolderEntity folder = (PhotoFolderEntity) v.getTag(R.id.folderList);
                if (mOnFolderSelectedListener != null)
                    mOnFolderSelectedListener.onFolderSelected(folder);
            }
        }
    }
}
