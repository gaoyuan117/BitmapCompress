package com.gaoyuan.bitmapcompress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaoyuan.bitmapcompress.Luban2.Luban2;
import com.gaoyuan.bitmapcompress.Luban3.ImageCompressUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    Button take, select, start;
    TextView tv1, tv2;
    private List<File> files;
    private ProgressDialog progressDialog;
    private List<String> path2, path3;
    private long s1, s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img1 = (ImageView) findViewById(R.id.img1);
        img2 = (ImageView) findViewById(R.id.img2);
        take = (Button) findViewById(R.id.bt_main_take);
        select = (Button) findViewById(R.id.bt_main_sel);
        start = (Button) findViewById(R.id.bt_main_start);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在压缩图片");

        select.setOnClickListener(this);
        take.setOnClickListener(this);
        start.setOnClickListener(this);

        files = new ArrayList<>();
        path3 = new ArrayList<>();
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
                                return BitmapUtils.compressBitmap(MainActivity.this, path1);
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

        if (requestCode == 111) {
            path2 = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_main_take:
                getPhotoByCamera(this, System.currentTimeMillis() + "");
                break;
            case R.id.bt_main_sel:
                MultiImageSelector.create()
                        .start(this, 111);
                break;
            case R.id.bt_main_start:
                s1 = System.currentTimeMillis();
                a();
//                luban2();
                break;
        }
    }


    private void luban2() {
        Luban2.get(this)
                .load(path2)                     //传人要压缩的图片
                .putGear(Luban2.THIRD_GEAR)     //设定压缩档次，默认三挡
                .setCompressListener(new Luban2.OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }
                    @Override
                    public void onSuccess(List<String> files) {
                        progressDialog.dismiss();
                        s2 = System.currentTimeMillis();
                        Log.e("gy", "时间 ：" + (s2 - s1));
                        for (int i = 0; i < files.size(); i++) {
                            Log.e("gy", "地址 " + files.get(i));
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        Log.e("gy", e.getMessage());
                    }
                }).launch();//启动压缩
    }


    private void a() {
        for (int i = 0; i < path2.size(); i++) {
            final int finalI = i;
            ImageCompressUtils.from(this).load(path2.get(i))
                    .putGear(ImageCompressUtils.THIRD_GEAR)
                    .execute(new ImageCompressUtils.OnCompressListener() {
                        @Override
                        public void onSuccess(File file) {
                            Log.e("gy", file.getAbsolutePath());
                            if (finalI == 8) {
                                s2 = System.currentTimeMillis();
                                Log.e("gy", "时间 ：" + (s2 - s1));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
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
}
