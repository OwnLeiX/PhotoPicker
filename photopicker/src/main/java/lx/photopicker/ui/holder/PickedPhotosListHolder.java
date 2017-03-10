package lx.photopicker.ui.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import lx.photopicker.PhotoParams;
import lx.photopicker.R;
import lx.photopicker.ui.adapter.PickedPhotosRecyclerViewAdapter;
import lx.photopicker.kernel.PickerManager;

/**
 * <p>(Multi模式才会使用)已经Picked的Photo的视图</p><br/>
 *
 * @author Lx
 * @date 2017/3/5
 */

public class PickedPhotosListHolder implements View.OnClickListener {
    private LayoutInflater mInflater;
    private View mRootView;
    private PickedPhotosRecyclerViewAdapter mAdapter;//(复选状态才有)显示已选择的Photo的RecyclerView的Adapter
    private TextView part_picked_tv_currentCount;//已经选择的张数
    private TextView part_picked_tv_maxCount;//最大张数
    private TextView part_picked_tv_finish;//确认
    private TextView part_picked_tv_view;//预览
    private RecyclerView part_picked_rv_pickedPhotos;//已经选择Photo视图列表

    private PickedPhotosListener mPickedPhotosListener;

    public PickedPhotosListener getPickedListCallback() {
        return mPickedPhotosListener;
    }

    public void setPickedListCallback(PickedPhotosListener callback) {
        this.mPickedPhotosListener = callback;
        mAdapter.setOnPhotoRemovedListener(callback);
    }

    public PickedPhotosListHolder(LayoutInflater inflater, ViewGroup parent) {
        this.mInflater = inflater;
        initView(inflater,parent);
    }

    private void initView(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.part_picked_list,parent,false);
        parent.addView(mRootView);
        part_picked_tv_maxCount= (TextView) mRootView.findViewById(R.id.part_picked_tv_maxCount);
        part_picked_tv_currentCount= (TextView) mRootView.findViewById(R.id.part_picked_tv_currentCount);
        part_picked_tv_finish= (TextView) mRootView.findViewById(R.id.part_picked_tv_finish);
        part_picked_tv_view= (TextView) mRootView.findViewById(R.id.part_picked_tv_view);
        part_picked_rv_pickedPhotos= (RecyclerView) mRootView.findViewById(R.id.part_picked_rv_pickedPhotos);
        part_picked_rv_pickedPhotos.setLayoutManager(new LinearLayoutManager(mInflater.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new PickedPhotosRecyclerViewAdapter(mInflater, PickerManager.$().getPickedPhotos());
        part_picked_rv_pickedPhotos.setAdapter(mAdapter);
    }

    public void init(PhotoParams params) {
        part_picked_tv_maxCount.setText(String.valueOf(params.getMaxCount()));
        part_picked_tv_currentCount.setText(String.valueOf(0));
        part_picked_tv_finish.setEnabled(false);
        part_picked_tv_view.setEnabled(false);
        part_picked_tv_finish.setOnClickListener(this);
        part_picked_tv_view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.part_picked_tv_finish) {
            if (mPickedPhotosListener != null)
                mPickedPhotosListener.onCompleteClicked();
        }else if (v.getId() == R.id.part_picked_tv_view){
            if (mPickedPhotosListener != null)
            mPickedPhotosListener.onViewClicked();
        }
    }

    public void setCurrentCount(int count) {
        part_picked_tv_currentCount.setText(String.valueOf(count));
        mAdapter.refresh();
        if (count > 0) {
            part_picked_tv_finish.setEnabled(true);
            part_picked_tv_view.setEnabled(true);
        }else {
            part_picked_tv_finish.setEnabled(false);
            part_picked_tv_view.setEnabled(false);
        }
    }

    public View getRootView() {
        return mRootView;
    }
}
