package com.ctgu.map.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.autonavi.ae.utils.NaviUtils;
import com.ctgu.map.R;

/**
 * FileName: AboutActivity
 * Author: Admin
 * Date: 2020/12/4 12:24
 * Description: 关于
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // 这句必须写，不写不调用onOptionsItemSelected


        if(NavUtils.getParentActivityName(AboutActivity.this)!=null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 点击返回按钮，退回上一层Activity
                if (NavUtils.getParentActivityName(AboutActivity.this) != null) {
                    // 启动父Activity
                    NavUtils.navigateUpFromSameTask(AboutActivity.this);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
