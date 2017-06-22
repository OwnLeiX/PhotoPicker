package lx.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import lx.photopicker.PhotoParams;
import lx.photopicker.PhotoPicker;
import lx.photopicker.PickerCallback;
import lx.photopicker.entity.PhotoEntity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PickerCallback {
    private Button btn1,btn2,btn3,btn4;
    private GridView gv;
    private GridViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initListener() {
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
    }

    private void initView() {
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        gv = (GridView) findViewById(R.id.gv);
        gv.setNumColumns(2);
        mAdapter = new GridViewAdapter(null);
        gv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        PhotoParams photoParams;
        switch (v.getId()) {
            case R.id.btn1:
                PhotoPicker.pickPhoto(this,null,this);
                break;
            case R.id.btn2:
                photoParams = new PhotoParams.Builder()
                        .addFlags(PhotoParams.FLAG_MULTI)
                        .setMaxSize(200 * 1024)
                        .setMaxPixel(100)
                        .setMaxCount(9)
                        .create();
                PhotoPicker.pickPhoto(this,photoParams,this);
                break;
            case R.id.btn3:
                photoParams = new PhotoParams.Builder()
                        .addFlags(PhotoParams.FLAG_CLIP)
                        .setClipSize(new int[]{400,200})
                        .setMaxSize(200 * 1024)
                        .create();
                PhotoPicker.pickPhoto(this,photoParams,this);
                break;
            case R.id.btn4:
                photoParams = new PhotoParams.Builder()
                        .addFlags(PhotoParams.FLAG_CLIP)
                        .setClipSize(new int[]{200,400})
                        .addFlags(PhotoParams.FLAG_MULTI)
                        .setMaxCount(9)
                        .setMaxSize(200 * 1024)
                        .create();
                PhotoPicker.pickPhoto(this,photoParams,this);
                break;
        }
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPicked(List<PhotoEntity> pickedPhotos,PhotoParams params) {
        mAdapter.update(pickedPhotos);
    }
}
