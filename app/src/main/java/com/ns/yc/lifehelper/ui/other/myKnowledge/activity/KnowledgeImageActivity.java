package com.ns.yc.lifehelper.ui.other.myKnowledge.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ns.yc.lifehelper.R;
import com.ns.yc.lifehelper.base.BaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/28
 * 描    述：我的干货页面  生活福利图片页面
 * 修订历史：
 * ================================================
 */
public class KnowledgeImageActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.vp_image)
    ViewPager vpImage;
    @Bind(R.id.tv_text)
    TextView tvText;
    @Bind(R.id.tv_save_image)
    TextView tvSaveImage;

    private int code;                               //接收穿过来当前选择的图片的数量
    private int selector;                           //用于判断是头像还是文章图片 1:头像 2：文章大图
    private boolean isLocal;                        //用于判断是否是加载本地图片
    private int page;                               //当前页数
    private ArrayList<String> imageUri;             //接收传过来的uri地址
    private boolean isApp;                          //是否是本应用中的图片
    private int imageId;
    private KnowledgeImageActivity activity;


    @Override
    public int getContentView() {
        return R.layout.activity_view_big_image;
    }

    @Override
    public void initView() {
        activity = KnowledgeImageActivity.this;
        initIntentData();
        initViewPagerData();
    }



    private void initIntentData() {
        Bundle bundle = getIntent().getExtras();
        code = bundle.getInt("code");
        selector = bundle.getInt("selector");
        isLocal = bundle.getBoolean("isLocal", false);
        imageUri = bundle.getStringArrayList("imageUri");
        /**是否是本应用中的图片*/
        isApp = bundle.getBoolean("isApp", false);
        /**本应用图片的id*/
        imageId = bundle.getInt("id", 0);
    }

    @Override
    public void initListener() {
        tvSaveImage.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_save_image:
                toSaveImage();
                break;
        }
    }

    /**
     * 给viewpager设置适配器
     */
    private void initViewPagerData() {
        if (isApp) {
            MyPageAdapter myPageAdapter = new MyPageAdapter();
            vpImage.setAdapter(myPageAdapter);
            vpImage.setEnabled(false);
        } else {
            ViewPagerAdapter adapter = new ViewPagerAdapter();
            vpImage.setAdapter(adapter);
            vpImage.setCurrentItem(code);
            page = code;
            vpImage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    // 每当页数发生改变时重新设定一遍当前的页数和总页数
                    tvText.setText((position + 1) + " / " + imageUri.size());
                    page = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            vpImage.setEnabled(false);
            // 设定当前的页数和总页数
            if (selector == 2) {
                tvText.setText((code + 1) + " / " + imageUri.size());
            }
        }
    }


    /**
     * 保存图片到本地文件
     */
    private void toSaveImage() {
        ToastUtils.showShortSafe("开始下载图片");
        if (isApp) {            // 本地图片
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);
            if (bitmap != null) {
                saveImageToGallery(activity, bitmap);
                ToastUtils.showShortSafe("保存成功");
            }
        } else {                // 网络图片
            final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 子线程获得图片路径
                    final String imagePath = getImagePath(imageUri.get(page));
                    // 主线程更新
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (imagePath != null) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                                if (bitmap != null) {
                                    saveImageToGallery(activity, bitmap);
                                    ToastUtils.showShortSafe("已保存至"+ Environment.getExternalStorageDirectory().getAbsolutePath()+"/生活助手");
                                }
                            }
                        }
                    });
                }
            }).start();
        }
    }



    /**
     * 本应用图片适配器
     */
    class MyPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.view_pager_very_image, container, false);
            ImageView iv_image = (ImageView) view.findViewById(R.id.iv_image);
            ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
            spinner.setVisibility(View.GONE);
            if (imageId != 0) {
                iv_image.setImageResource(imageId);
            }
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }



    /**
     * ViewPager的适配器
     */
    class ViewPagerAdapter extends PagerAdapter {

        LayoutInflater inflater;

        ViewPagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.view_pager_very_image, container, false);
            final  ImageView iv_image = (ImageView) view.findViewById(R.id.iv_image);
            final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
            // 保存网络图片的路径
            String adapter_image_Entity = (String) getItem(position);
            String imageUrl;
            if (isLocal) {
                imageUrl = "file://" + adapter_image_Entity;
                tvSaveImage.setVisibility(View.GONE);
            } else {
                imageUrl = adapter_image_Entity;
            }

            spinner.setVisibility(View.VISIBLE);
            spinner.setClickable(false);
            Glide.with(activity)
                    .load(imageUrl)
                    .crossFade(700)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Toast.makeText(getApplicationContext(), "资源加载异常", Toast.LENGTH_SHORT).show();
                            spinner.setVisibility(View.GONE);
                            return false;
                        }

                        //这个用于监听图片是否加载完成
                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            spinner.setVisibility(View.GONE);

                            /**这里应该是加载成功后图片的高*/
                            int height = iv_image.getHeight();

                            int wHeight = getWindowManager().getDefaultDisplay().getHeight();
                            if (height > wHeight) {
                                iv_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                iv_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                            return false;
                        }
                    }).into(iv_image);
            container.addView(view, 0);
            return view;
        }

        @Override
        public int getCount() {
            if (imageUri == null || imageUri.size() == 0) {
                return 0;
            }
            return imageUri.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        Object getItem(int position) {
            return imageUri.get(position);
        }
    }



    /**
     * 保存图片至相册
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "生活助手");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsoluteFile())));
    }


    /**
     * Glide 获得图片缓存路径
     */
    private String getImagePath(String imgUrl) {
        String path = null;
        FutureTarget<File> future = Glide.with(activity).load(imgUrl).downloadOnly(500, 500);
        try {
            File cacheFile = future.get();
            path = cacheFile.getAbsolutePath();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return path;
    }



}
