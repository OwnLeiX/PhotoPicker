package lx.photopicker.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import lx.photopicker.PhotoParams;
import lx.photopicker.R;
import lx.photopicker.adapter.FolderListRecyclerViewAdapter;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.entity.PhotoFolderEntity;
import lx.photopicker.holder.PickedPhotosListHolder;
import lx.photopicker.holder.PickedPhotosListener;
import lx.photopicker.task.ScanCallback;
import lx.photopicker.util.DoCallbackCallback;
import lx.photopicker.util.PickerManager;
import lx.photopicker.widget.OwnStateLayout;

/**
 * <b>系统相册目录的Activity</b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class FolderListActivity extends BaseActivity implements View.OnClickListener, FolderListRecyclerViewAdapter.OnFolderSelectedListener,
        OwnStateLayout.OnRetryListener, ScanCallback, PickedPhotosListener, DoCallbackCallback {
    private static final String TAG = "FolderListActivity";

    private static final int CODE_FOLDER_DETAIL = 0;//打开文件夹
    private static final int CODE_PHOTO_VIEW = 2;//打开已选择的照片浏览

    private TextView part_title_tv_title;//标题的TextView
    private FrameLayout part_title_fl_back;//标题的返回区域
    private FrameLayout folderList_fl_container;//内容的容器

    private RecyclerView mRecyclerView;//显示相册目录的RecyclerView
    private OwnStateLayout mOwnStateLayout;//状态布局

    private FolderListRecyclerViewAdapter mAdapter;//相册目录的RecyclerView的Adapter
    private PhotoParams mPhotoParams;//本次裁剪需要的图片参数
    private PickedPhotosListHolder mPickedPhotosListHolder;

    @Override
    protected View onInitContentView() {
        return getLayoutInflater().inflate(R.layout.activity_folderlist_layout, null, false);
    }

    @Override
    protected void onInitView() {
        mPhotoParams = PickerManager.$().getPhotoParams();
        folderList_fl_container = (FrameLayout) findViewById(R.id.folderList_fl_container);
        initTitle();
        initStateLayout();
        initPickedPhotos();
    }

    /**
     * 初始化标题栏
     */
    private void initTitle() {
        part_title_tv_title = (TextView) findViewById(R.id.part_title_tv_title);
        part_title_tv_title.setText("相册");
        part_title_fl_back = (FrameLayout) findViewById(R.id.part_title_fl_back);
    }

    /**
     * 初始化状态布局
     */
    private void initStateLayout() {
        mRecyclerView = new RecyclerView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mOwnStateLayout = new OwnStateLayout.Builder(this).setEmptyView(R.layout.state_empty)
                .setFailedViewWithRetryView(R.layout.state_failed, R.id.state_failed_tryAgain).setOnRetryListener(this).setLoadingView(R.layout.state_loading)
                .setSucessView(mRecyclerView).create();
        mOwnStateLayout.setState(OwnStateLayout.STATE_LOADING);
        folderList_fl_container.addView(mOwnStateLayout);
    }

    /**
     * 初始化已经选择的Photo的列表
     */
    private void initPickedPhotos() {
        if (!mPhotoParams.isMulti())//如果不是复选状态，直接return
            return;
        mPickedPhotosListHolder = new PickedPhotosListHolder(getLayoutInflater(), folderList_fl_container);
        mPickedPhotosListHolder.init(mPhotoParams);
        mPickedPhotosListHolder.setPickedListCallback(this);
    }

    @Override
    protected void onInitListener() {
        part_title_fl_back.setOnClickListener(this);
    }

    @Override
    protected void onInitData() {
        PickerManager.$().scan(this, this);
    }

    /**
     * 按手机返回键退出，当做取消选择相片处理
     */
    @Override
    public void onBackPressed() {
        PickerManager.$().clearPickedPhoto();
        finalFinish();
    }

    /**
     * 按左上角返回按钮退出，当做取消选择相片处理
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.part_title_fl_back) {
            PickerManager.$().clearPickedPhoto();
            finalFinish();
        }
    }

    /**
     * PickerManager扫描完毕后的回调
     */
    @Override
    public void onScanFinished(List<PhotoFolderEntity> list) {
        if (mAdapter == null) {
            mAdapter = new FolderListRecyclerViewAdapter(getLayoutInflater(), list);
            mAdapter.setOnFolderSelectedListener(this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.update(list);
        }
        mOwnStateLayout.setState(OwnStateLayout.STATE_SUCCESS);
    }

    /**
     * PickerManager扫描出现错误的回调。
     */
    @Override
    public void onScanError(Throwable e) {
        e.printStackTrace();
        mOwnStateLayout.setState(OwnStateLayout.STATE_FAILED);
    }

    @Override
    public void onScanEmpty() {
        mOwnStateLayout.setState(OwnStateLayout.STATE_EMPTY);
    }

    /**
     * 相册目录的RecyclerView条目被点击的回调。
     */
    @Override
    public void onFolderSelected(PhotoFolderEntity photoFolderEntity) {
        PickerManager.$().setCurrentFolder(photoFolderEntity);
        Intent intent = new Intent(this, FolderDetailActivity.class);
        startActivityForResult(intent, CODE_FOLDER_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_FOLDER_DETAIL) {//打开文件夹后
            switch (resultCode) {
                case CODE_CANCEL://如果是取消回来的，则不进行任何动作
                    break;
                case CODE_COMPLETE://如果是直接结束，finish自己。
                    finalFinish();
                    break;
            }
        } else if (requestCode == CODE_PHOTO_VIEW) {//打开已选择的图片列表后
            switch (resultCode) {
                case CODE_CANCEL://如果是取消回来的，则不进行任何动作
                    break;
                case CODE_COMPLETE://如果是直接结束，finish自己。
                    finalFinish();
                    break;
            }
        }
        refreshCurrentCount();//刷新已选Photo视图列表
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * PickerManager扫描出现错误后，重试被点击的回调
     */
    @Override
    public void onRetry() {
        mOwnStateLayout.setState(OwnStateLayout.STATE_LOADING);
        PickerManager.$().scan(this, this);
    }

    /**
     * (复选状态才有)复选框内相片被点击右上角快速移除的回调
     */
    @Override
    public void onPhotoRemoved(PhotoEntity photo) {
        PickerManager.$().removePickedPhoto(photo);
        refreshCurrentCount();
    }

    /**
     * (Multi模式才会使用)已选择的Photo的视图列表"预览"按钮被点击
     */
    @Override
    public void onViewClicked() {
        startActivityForResult(new Intent(this, PhotosViewActivity.class), CODE_PHOTO_VIEW);
    }

    /**
     * (Multi模式才会使用)已选择的Photo的视图列表"确认"按钮被点击
     */
    @Override
    public void onCompleteClicked() {
        finalFinish();
    }

    /**
     * (Multi模式才会用)刷新已经选择的Photo数量的方法
     */
    private void refreshCurrentCount() {
        if (mPickedPhotosListHolder != null)//只有Multi模式，这个控件才不为null
            mPickedPhotosListHolder.setCurrentCount(PickerManager.$().getPickedCount());
    }

    private void finalFinish() {
        mOwnStateLayout.setState(OwnStateLayout.STATE_LOADING);
        if (mPickedPhotosListHolder != null)
            folderList_fl_container.removeView(mPickedPhotosListHolder.getRootView());
        part_title_fl_back.setVisibility(View.INVISIBLE);
        part_title_fl_back.setEnabled(false);
        if (PickerManager.$().doCallback(this)) {
            finish();
        }
    }

    @Override
    public void onCompleted() {
        finish();
    }
}
