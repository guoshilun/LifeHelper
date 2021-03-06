package com.ns.yc.lifehelper.ui.other.douMovie;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.ns.yc.lifehelper.R;
import com.ns.yc.lifehelper.api.ConstantImageApi;
import com.ns.yc.lifehelper.base.BaseActivity;
import com.ns.yc.lifehelper.ui.other.douMovie.activity.MovieDetailActivity;
import com.ns.yc.lifehelper.ui.other.douMovie.activity.MovieTopActivity;
import com.ns.yc.lifehelper.ui.other.douMovie.adapter.DouMovieAdapter;
import com.ns.yc.lifehelper.ui.other.douMovie.bean.DouHotMovieBean;
import com.ns.yc.lifehelper.ui.other.douMovie.model.HotMovieModel;
import com.ns.yc.lifehelper.ui.weight.itemLine.RecycleViewItemLine;
import com.ns.yc.lifehelper.utils.ImageUtils;

import java.util.List;

import butterknife.Bind;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/22
 * 描    述：豆瓣电影页面
 * 修订历史：
 * ================================================
 */
public class DouMovieActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.ll_title_menu)
    FrameLayout llTitleMenu;
    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.recyclerView)
    EasyRecyclerView recyclerView;
    private DouMovieActivity activity;
    private DouMovieAdapter adapter;

    @Override
    public int getContentView() {
        return R.layout.base_easy_recycle_list;
    }

    @Override
    public void initView() {
        activity = DouMovieActivity.this;
        initToolBar();
        initRecycleView();
    }

    private void initToolBar() {
        toolbarTitle.setText("豆瓣电影");
    }

    @Override
    public void initListener() {
        llTitleMenu.setOnClickListener(this);
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                List<DouHotMovieBean.SubjectsBean.CastsBean> casts = adapter.getAllData().get(position).getCasts();
                StringBuilder sb = new StringBuilder();
                for(int a=0 ; a<casts.size() ; a++){
                    sb.append(casts.get(a).getName());
                    sb.append("/");
                }

                Intent intent = new Intent(activity, MovieDetailActivity.class);
                intent.putExtra("image",adapter.getAllData().get(position).getImages().getLarge());
                intent.putExtra("title",adapter.getAllData().get(position).getTitle());
                intent.putExtra("directors",adapter.getAllData().get(position).getDirectors().get(0).getName());
                intent.putExtra("casts",sb.toString());
                intent.putExtra("genres",adapter.getAllData().get(position).getGenres().get(0));
                intent.putExtra("rating",adapter.getAllData().get(position).getRating().getAverage());
                intent.putExtra("year",adapter.getAllData().get(position).getYear());
                intent.putExtra("id",adapter.getAllData().get(position).getId());
                intent.putExtra("collect_count",String.valueOf(adapter.getAllData().get(position).getCollect_count()));
                startActivity(intent);
            }
        });
    }

    @Override
    public void initData() {
        recyclerView.showProgress();
        getHotMovieData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_title_menu:
                finish();
                break;
        }
    }

    private void initRecycleView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        final RecycleViewItemLine line = new RecycleViewItemLine(activity, LinearLayout.HORIZONTAL,
                SizeUtils.dp2px(1), Color.parseColor("#f5f5f7"));
        recyclerView.addItemDecoration(line);
        adapter = new DouMovieAdapter(activity);
        recyclerView.setAdapter(adapter);
        AddHeader();
        //刷新
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isConnected()) {
                    if (adapter.getAllData().size() > 0) {
                        getHotMovieData();
                    } else {
                        recyclerView.setRefreshing(false);
                    }
                } else {
                    recyclerView.setRefreshing(false);
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AddHeader() {
        adapter.removeAllHeader();
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view = LayoutInflater.from(activity).inflate(R.layout.head_dou_movie, parent, false);
                RelativeLayout ll_movie_top = (RelativeLayout) view.findViewById(R.id.ll_movie_top);
                ImageView iv_img = (ImageView) view.findViewById(R.id.iv_img);
                ImageUtils.loadImgByPicasso(activity, ConstantImageApi.MOVIE_URL_01,iv_img);
                ll_movie_top.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(MovieTopActivity.class);
                    }
                });
                return view;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }


    private void getHotMovieData() {
        HotMovieModel model = HotMovieModel.getInstance(this);
        model.getHotMovie()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DouHotMovieBean>() {
                    @Override
                    public void onCompleted() {
                        recyclerView.showRecycler();
                    }

                    @Override
                    public void onError(Throwable e) {
                        recyclerView.showError();
                        recyclerView.setErrorView(R.layout.view_custom_empty_data);
                    }

                    @Override
                    public void onNext(DouHotMovieBean hotMovieBean) {
                        if (adapter == null) {
                            adapter = new DouMovieAdapter(activity);
                        }

                        if (hotMovieBean != null && hotMovieBean.getSubjects() != null && hotMovieBean.getSubjects().size() > 0) {
                            adapter.clear();
                            adapter.addAll(hotMovieBean.getSubjects());
                            adapter.notifyDataSetChanged();
                        } else {
                            recyclerView.showEmpty();
                            recyclerView.setEmptyView(R.layout.view_custom_empty_data);
                        }
                    }
                });
    }


}
