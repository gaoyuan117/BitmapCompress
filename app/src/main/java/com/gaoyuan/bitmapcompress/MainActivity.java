package com.gaoyuan.bitmapcompress;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.bither.util.NativeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String path, path1;
    ImageView img1, img2;
    Button take,select;
    TextView tv1, tv2;
    private List<File> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        take = (Button) findViewById(R.id.bt_main_take);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        select.setOnClickListener(this);

        take.setOnClickListener(this);
        files = new ArrayList<>();
    }


    /**
     * 调用系统相机拍一张照片
     *
     * @param activity
     * @param name     图片名字
     * @return 图片地址
     */
    public String getPhotoByCamera(Activity activity, String name) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        path = getExternalFilesDir(null) + name + ".png";
        path1 = getExternalFilesDir(null) + name + "hhaha" + ".png";

        File file = new File(path);
        Uri imageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, 100);

        return path;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {//选择的图片地址回掉
            if (resultCode == RESULT_OK) {
                Observable.just(BitmapFactory.decodeFile(path))
                        .map(new Function<Bitmap, String>() {
                            @Override
                            public String apply(Bitmap bitmap) throws Exception {
                                return compressBitmap(bitmap, path1);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Bitmap bitmap1 = BitmapFactory.decodeFile(path1);
                                img2.setImageBitmap(bitmap1);
                                tv2.setText("img2 : " + bitmap1.getRowBytes() * bitmap1.getHeight());
                            }
                        });
            }
        }

        if(requestCode==111){
            List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

            for (int i = 0; i < path.size(); i++) {
                File file = new File(path.get(i));
                files.add(file);
            }

        }
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.bt_main_take:
               getPhotoByCamera(this, System.currentTimeMillis() + "");
               break;
           case R.id.bt_main_sel:
               MultiImageSelector.create()
                       .start(this, 111);
               break;
           case R.id.bt_main_start:
                luban();
               break;
       }
    }

    private void luban(){
        Luban.get(this)
                .load(files)                     //传人要压缩的图片
                .putGear(Luban.THIRD_GEAR)      //设定压缩档次，默认三挡
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                    }
                    @Override
                    public void onSuccess(List<String> files) {
                        // TODO 压缩成功后调用，返回压缩后的图片文件
                        for (int i = 0; i < files.size(); i++) {
                            Bitmap bitmap = BitmapFactory.decodeFile(files.get(i));
                            Log.e("gy","图片大小 = "+bitmap.getByteCount());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO 当压缩过去出现问题时调用
                    }
                }).launch();    //启动压缩
    }

    /**
     *
     * @param image
     * @param filePath
     * @return
     */
    public String compressBitmap(Bitmap image, String filePath) {
        // 最大图片大小 100KB
        int maxSize = 100;
        // 获取尺寸压缩倍数
        int ratio = NativeUtil.getRatioSize(image.getWidth(), image.getHeight());
        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio, image.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
        canvas.drawBitmap(image, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            options -= 10;
            // 这里压缩options%，把压缩后的数据存放到baos中
            result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        // JNI调用保存图片到SD卡 这个关键
        NativeUtil.saveBitmap(result, options, filePath, true);
        // 释放Bitmap
        if (result != null && !result.isRecycled()) {
            result.recycle();
            result = null;
        }
        return path1;
    }
}
