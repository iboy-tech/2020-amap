package com.ctgu.map.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.ctgu.map.R;
import com.ctgu.map.adapter.PoiItemAdapter;
import com.ctgu.map.adapter.TipAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SearchActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener,
        Inputtips.InputtipsListener{

    private String city;

    private RecyclerView recyclerView;
    private List<Tip> tipList;
    private List<PoiItem> poiItemList;
    private TipAdapter tipAdapter;
    private PoiItemAdapter poiItemAdapter;

    private ProgressDialog loadingDialog;
    private SearchView searchView;
    private TextView noResult;

    private ImageButton imageButton;

    private EditText editText;

    //从main跳转活动跳转函数
    public static void startActivity(AppCompatActivity activity, int REQUEST_CODE, String city){
        Intent intent=new Intent(activity, SearchActivity.class);
        intent.putExtra("city", city);
        //获取搜索结果
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        city=getIntent().getStringExtra("city");
        editText=findViewById(R.id.search);
        imageButton=findViewById(R.id.search_ico);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword=editText.getText().toString();
                if(keyword=="" || keyword==null || StringUtils.isAllBlank(keyword)){
                    Toast.makeText(getApplicationContext(),"搜索内容不能为空",Toast.LENGTH_LONG).show();
                }else{
                    searchPOI(keyword);
                }
            }
        });


        //监听搜索文本变化
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(tipList!=null){
                    tipList.clear();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tipList!=null){
                    tipList.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newText=s.toString();
                System.out.println("搜索内容变为："+s);
                //输入了有效的目标地点
                if(newText!=null&&!newText.equals("")){
                    InputtipsQuery inputtipsQuery = new InputtipsQuery(newText, city);
                    Inputtips inputTips = new Inputtips(SearchActivity.this.getApplicationContext(),
                            inputtipsQuery);
                    inputTips.setInputtipsListener(SearchActivity.this);
                    inputTips.requestInputtipsAsyn();
                } else {
                    if (tipAdapter != null && tipList != null) {
                        tipList.clear();
                        tipAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        noResult= findViewById(R.id.text_no_result);
        recyclerView= findViewById(R.id.recyclerView_tip);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar= findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    //设置搜索框的文本并直接开始搜索
    public void setQuery(String keyword){
        searchView.setQuery(keyword, true);
    }


    //开始地点搜索
    private void searchPOI(String keyword){
        showLoadingDialog();
        PoiSearch.Query query=new PoiSearch.Query(keyword, "", city);
        query.setPageSize(50);
        query.setPageNum(0);
        PoiSearch poiSearch=new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    //显示等待对话框
    private void showLoadingDialog(){
        if(loadingDialog==null){
            loadingDialog=new ProgressDialog(this);
            loadingDialog.setTitle("请稍等");
            loadingDialog.setMessage("加载中...");
            loadingDialog.setCancelable(true);
            loadingDialog.show();
        }
    }

    //关闭等待对话框
    private void dismissLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
            loadingDialog=null;
        }
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }





    //获取关键词搜索结果
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if(i==1000){
            tipList=list;
            tipAdapter=new TipAdapter(tipList, this);
            recyclerView.setAdapter(tipAdapter);
            tipAdapter.notifyDataSetChanged();
            if(tipList.size()!=0){
                noResult.setVisibility(View.GONE);
            } else {
                noResult.setVisibility(View.VISIBLE);
            }
        } /*else {
            Toast.makeText(this, "Get input tips failed. Please check your settings.",
                    Toast.LENGTH_SHORT).show();
        }*/
    }

    //获取地点搜索结果
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        dismissLoadingDialog();
        if(i==1000){
            //渲染结果
            if(poiResult!=null&&poiResult.getQuery()!=null&&poiResult.getPois()!=null){
                poiItemList=poiResult.getPois();
                poiItemAdapter=new PoiItemAdapter(poiItemList, this);
                recyclerView.setAdapter(poiItemAdapter);
                poiItemAdapter.notifyDataSetChanged();
                if(poiItemList.size()!=0) {
                    noResult.setVisibility(View.GONE);
                } else {
                    noResult.setVisibility(View.VISIBLE);
                }
            } else {
                noResult.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Poi searching failed. Error code "+i,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    //toolbar菜单项被选中事件处理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
}
