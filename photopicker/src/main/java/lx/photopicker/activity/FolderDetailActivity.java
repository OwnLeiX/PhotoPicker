package lx.photopicker.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import lx.photopicker.PhotoParams;
import lx.photopicker.R;
import lx.photopicker.adapter.FolderDetailRecyclerViewAdapter;
import lx.photopicker.adapter.OnPhotoPickedListener;
import lx.photopicker.adapter.OnPhotoQuickPickedListener;
import lx.photopicker.entity.PhotoEntity;
import lx.photopicker.entity.PhotoFolderEntity;
import lx.photopicker.holder.PickedPhotosListHolder;
import lx.photopicker.holder.PickedPhotosListener;
import lx.photopicker.util.PickerManager;

/**
 * <b>相册单个文件夹内内容的Activity</b>
 * Created on 2017/3/2.
 *
 * @author LeiXun
 */

public class FolderDetailActivity extends BaseActivity
        implements View.OnClickListener, OnPhotoPickedListener, OnPhotoQuickPickedListener, PickedPhotosListener {

    private static final int CODE_PHOTO_CLIP = 1;//打开照片裁剪
    private static final int CODE_PHOTO_VIEW = 2;//打开已选择的照片浏览

    private TextView part_title_tv_title;//标题的TextView
    private FrameLayout part_title_fl_back;//标题的返回区域
    private FrameLayout folderDetail_fl_container;//内容的容器
    private RecyclerView mRecyclerView;//显示当前Folder的所有图片的RecyclerView

    private FolderDetailRecyclerViewAdapter mAdapter;//显示当前Folder的所有图片的RecyclerView的Adapter
    private PhotoParams mPhotoParams;//本次pick的photo的参数
    private PickedPhotosListHolder mPickedPhotosListHolder;


    @Override
    protected View onInitContentView() {
        return getLayoutInflater().inflate(R.layout.activity_folderdetail_layout, null, false);
    }

    @Override
    protected void onInitView() {
        mPhotoParams = PickerManager.$().getPhotoParams();
        initTitle();
        initRecyclerView();
        initPickedPhotos();
    }

    /**
     * 初始化标题栏
     */
    private void initTitle() {
        part_title_tv_title = (TextView) findViewById(R.id.part_title_tv_title);
        part_title_fl_back = (FrameLayout) findViewById(R.id.part_title_fl_back);
    }

    /**
     * 初始化内容
     */
    private void initRecyclerView() {
        folderDetail_fl_container = (FrameLayout) findViewById(R.id.folderDetail_fl_container);
        mRecyclerView = new RecyclerView(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));
        folderDetail_fl_container.addView(mRecyclerView);
    }

    /**
     * 初始化已经选择的Photo的列表
     */
    private void initPickedPhotos() {
        if (!mPhotoParams.isMulti())//如果不是复选状态，直接return
            return;
        mPickedPhotosListHolder = new PickedPhotosListHolder(getLayoutInflater(), folderDetail_fl_container);
        mPickedPhotosListHolder.init(mPhotoParams);
        mPickedPhotosListHolder.setPickedListCallback(this);
        refreshCurrentCount();
    }

    @Override
    protected void onInitListener() {
        part_title_fl_back.setOnClickListener(this);
    }

    @Override
    protected void onInitData() {
        PhotoFolderEntity selectedFolder = PickerManager.$().getCurrentFolder();
        String folderName = selectedFolder.getFolderName();
        part_title_tv_title.setText(folderName);
        if (mAdapter == null) {
            mAdapter = new FolderDetailRecyclerViewAdapter(getLayoutInflater(),
                    selectedFolder.getPhotoEntities(),
                    mPhotoParams.isMulti() && !mPhotoParams.isClip());//Multi模式 + 不clip模式，才能进行快速选择
            mAdapter.setOnPhotoPickedListener(this);
            if (mPhotoParams.isMulti() && !mPhotoParams.isClip())
                mAdapter.setOnPhotoQuickPickedListener(this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.update(selectedFolder.getPhotoEntities());
        }
    }

    /**
     * 按手机返回键退出，当做取消选择相片处理
     */
    @Override
    public void onBackPressed() {
        finishByCancel();
    }

    /**
     * 按左上角返回按钮退出，当做取消选择相片处理
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.part_title_fl_back) {
            finishByCancel();
        } else if (v.getId() == R.id.part_picked_tv_finish) {
            finishByComplete();
        } else if (v.getId() == R.id.part_picked_tv_view) {
            startActivityForResult(new Intent(this,PhotosViewActivity.class),CODE_PHOTO_VIEW);
        }
    }

    /**
     * 单张相片被选择的回调
     */
    @Override
    public void onPhotoPicked(PhotoEntity photo) {
        if (!isFullCount()) {
            PickerManager.$().setCurrentPhoto(photo);
            Intent intent = new Intent(this, PhotoClipActivity.class);
            startActivityForResult(intent, CODE_PHOTO_CLIP);
        } else {
            showShortToast("只能选择" + mPhotoParams.getMaxCount() + "张哦");
        }
    }

    /**
     * 已经选择的Photo再次被点击的回调
     */
    @Override
    public void onPhotoRepicked(PhotoEntity photo) {
        showShortToast("已经选择了该图片");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_PHOTO_CLIP) {//打开裁剪页面后
            switch (resultCode) {
                case CODE_CANCEL://如果是取消回来的，则不进行任何动作
                    break;
                case CODE_COMPLETE://如果是直接结束，finish自己。
                    finishByComplete();
                    break;
            }
        } else if (requestCode == CODE_PHOTO_VIEW) {//打开已选择的图片列表后
            switch (resultCode) {
                case CODE_CANCEL://如果是取消回来的，则不进行任何动作
                break;
                case CODE_COMPLETE://如果是直接结束，finish自己。
                    finishByComplete();
                    break;
            }
        }
        mAdapter.refresh();
        refreshCurrentCount();//刷新已选Photo视图列表
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * (复选状态才有)已经被选择的列表被点击右上角的区域删除的回调
     */
    @Override
    public void onPhotoRemoved(PhotoEntity photo) {
        removePhoto(photo);
    }

    /**
     * (复选状态才有)Photo被点击右上角的CheckBox的快速选择的回调
     */
    @Override
    public void onPhotoQuickPicked(PhotoEntity photo) {
        if (!isFullCount()) {
            quickAddPhoto(photo);
        } else {
            showShortToast("只能选择" + mPhotoParams.getMaxCount() + "张哦");
        }
    }

    /**
     * (复选状态才有)Photo被点击右上角的CheckBox的快速取消的回调
     */
    @Override
    public void onPhotoQuickRemoved(PhotoEntity photo) {
        removePhoto(photo);
    }

    /**
     * （复选状态才会调用）快速添加选中的照片的方法
     */
    private void quickAddPhoto(PhotoEntity photo) {
        PickerManager.$().addPickedPhoto(photo);
        refreshCurrentCount();
    }

    /**
     * （复选状态才会调用）快速取消选中的照片的方法
     */
    private void removePhoto(PhotoEntity photo) {
        PickerManager.$().removePickedPhoto(photo);
        mAdapter.refresh();
        refreshCurrentCount();
    }

    /**
     * (Multi模式才会使用)已选择的Photo的视图列表"预览"按钮被点击
     */
    @Override
    public void onViewClicked() {
        startActivityForResult(new Intent(this,PhotosViewActivity.class),CODE_PHOTO_VIEW);
    }

    /**
     * (Multi模式才会使用)已选择的Photo的视图列表"确认"按钮被点击
     */
    @Override
    public void onCompleteClicked() {
        finishByComplete();
    }

    /**
     * (复选状态才会用)刷新已经选择的Photo数量的方法
     */
    private void refreshCurrentCount() {
        if (mPickedPhotosListHolder != null)//只有Multi模式，这个控件才不为null
            mPickedPhotosListHolder.setCurrentCount(PickerManager.$().getPickedCount());
    }

    /**
     * (复选状态才会用)计算是否已经达到数量上限的方法
     *
     * @return true 已经达到上限 | false 还没达到上限
     */
    private boolean isFullCount() {
        int maxCount = mPhotoParams.getMaxCount();
        int currentCount = PickerManager.$().getPickedCount();
        return maxCount <= currentCount;
    }

}
