package lx.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import lx.photopicker.entity.PhotoEntity;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 2017/3/4
 */

public class GridViewAdapter extends BaseAdapter {
    private List<PhotoEntity> mPicked;

    public GridViewAdapter(List<PhotoEntity> mPicked) {
        this.mPicked = mPicked;
    }

    @Override
    public int getCount() {
        return mPicked == null ? 0 : mPicked.size();
    }

    @Override
    public Object getItem(int position) {
        return mPicked.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        ImageView iv = (ImageView) convertView.findViewById(R.id.iv);
        Glide.with(iv.getContext().getApplicationContext()).load(mPicked.get(position).getPath()).into(iv);
        return convertView;
    }

    public void update(List<PhotoEntity> pickedPhotos) {
        mPicked = pickedPhotos;
        notifyDataSetChanged();
    }
}
