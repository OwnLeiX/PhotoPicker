# PhotoPicker
一个照片选择与裁剪的Demo


> 调用

####
* 0 获取一张图片

<pre><code>PhotoPicker.pickPhoto((Activity)this,null,(PickerCallback)this);
</code></pre>

* 1 获取9张图片,指定长边最大宽度不超过100px，文件尺寸不超过200KB
<pre><code>PhotoParams photoParams = new PhotoParams.Builder()
                  .addFlags(PhotoParams.FLAG_MULTI)
                  .setMaxCount(9)
                  .setMaxSize(200 * 1024)
                  .setMaxPixel(100)
                  .create();
PhotoPicker.pickPhoto((Activity)this,photoParams,(PickerCallback)this);
</code></pre>

* 2 获取一张400x300像素的图片，文件尺寸不超过200KB
<pre><code>PhotoParams photoParams = new PhotoParams.Builder()
.addFlags(PhotoParams.FLAG_CLIP)
.setClipSize(new int[]{400,300})
.setMaxSize(200 * 1024)
.create();
PhotoPicker.pickPhoto((Activity)this,photoParams,(PickerCallback)this);
</code></pre>
                
* 3 获取9张300x400像素图片，文件尺寸不超过200KB
<pre><code>PhotoParams photoParams = new PhotoParams.Builder()
                  .addFlags(PhotoParams.FLAG_CLIP)
                  .setClipSize(new int[]{300,400})
                  .addFlags(PhotoParams.FLAG_MULTI)
                  .setMaxSize(200 * 1024)
                  .setMaxCount(9)
                  .create();
PhotoPicker.pickPhoto((Activity)this,photoParams,(PickerCallback)this);
</code></pre>

> 回调

<pre><code>
@Override
public void onCancel() {
    //取消选择
}

@Override
public void onPicked(List<PhotoEntity> pickedPhotos) {
    //图片路径
    String filePath = pickedPhotos.get(position).getPath();
    //是否原图
    pickedPhotos.get(position).isNative();
}
</code></pre>
