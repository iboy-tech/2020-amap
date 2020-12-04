package com.ctgu.map.activity;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ctgu.map.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileName: AboutActivity
 * Author: Admin
 * Date: 2020/12/4 12:24
 * Description: 关于
 */

public class LogActivity extends AppCompatActivity {


    private static final String[] logs={"1.0.0 显示静态地图","1.0.1 加入侧边栏","1.0.2 加入导航"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        // 这句必须写，不写不调用onOptionsItemSelected
        if(NavUtils.getParentActivityName(LogActivity.this)!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ListView listView=findViewById(R.id.log_list);

        List<Map<String,String>> list= new ArrayList<>();

        for (int i = 0; i < logs.length; i++) {
            Map map=new HashMap();
            map.put("text",logs[i]);
            list.add(map);
        }
        System.out.println("更新日志"+list);
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,list,R.layout.item_log,new String[]{"text"},new int[]{R.id.item_log_detail});
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LogActivity.this, "点击了"+position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 点击返回按钮，退回上一层Activity
                if (NavUtils.getParentActivityName(LogActivity.this) != null) {
                    finish();
                    // 启动父Activity
//                    NavUtils.navigateUpFromSameTask(LogActivity.this);
//                    new Thread () {
//                        public void run () {
//                            try {
//                                Instrumentation inst= new Instrumentation();
//                                inst.sendKeyDownUpSync(KeyEvent. KEYCODE_BACK);
//                            } catch(Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();
//                    drawerLayout.openDrawer(Gravity.LEFT);
//                    drawerLayout.openDrawer(navigationView);
//                    navigationView.setCheckedItem(R.id.map_standard);
                    System.out.println("从日志页面返回");
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

