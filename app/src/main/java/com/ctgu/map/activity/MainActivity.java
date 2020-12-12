package com.ctgu.map.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.help.Tip;
import com.ctgu.map.R;
import com.ctgu.map.util.Constants;
import com.ctgu.map.util.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity implements AMap.OnPOIClickListener,
        AMap.OnMyLocationChangeListener, View.OnClickListener,
       NavigationView.OnNavigationItemSelectedListener {

    private Marker marker;
    private String marker_title;
    //当前位置的经纬度
    private LatLng curLocation;

    private boolean isFirstLocate=true;
    private boolean isFirstLocateFailed=false;
    private boolean isBackClickOnce=false;
    private boolean isOnResultBack=false;

    private MapView mapView;
    private AMap aMap;


    private DrawerLayout drawerLayout;
    private FloatingActionButton locate;
    private FloatingActionButton plan;
    private TextView search;
    private ImageButton searchIcon;
    private ImageButton menu;
    private NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        //获取权限
        try {
            checkPermission();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //请求必要权限
    private void checkPermission() throws Exception {
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            init();
        }
    }

    //初始化
    private void init() {
        initLayout();
        initMap();
    }

    //自定义导航图标，对图标进行缩放
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }



    //初始化地图
    private void initMap() {
        if(aMap==null){
            aMap=mapView.getMap();
        }
        UiSettings uiSettings=aMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false); //我的位置
        uiSettings.setCompassEnabled(false);// 设置指南针是否显示
        uiSettings.setZoomControlsEnabled(false);// 设置缩放按钮是否显示
        //定位蓝点
        MyLocationStyle myLocationStyle=new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //会导致标记点太远，自动定位到地图中心
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.direction);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(scaleBitmap(bitmap,0.12f)));
        myLocationStyle.strokeColor(Color.argb(100, 255, 144, 147));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 255, 144, 233));// 设置圆形的填充颜色
        //定位频率
        myLocationStyle.interval(1000);
        //精度圈边框宽度自定义方法如下
        myLocationStyle.strokeWidth(0.1f);
        aMap.setMyLocationStyle(myLocationStyle);
        //设置初始缩放比
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        //为地图注册监听器
        registerMapListener();
    }

    //为地图注册监听器
    private void registerMapListener(){
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnPOIClickListener(this);
    }

    //为界面组件注册监听器
    private void registerLayoutListener(){
        locate.setOnClickListener(this);
        plan.setOnClickListener(this);
        search.setOnClickListener(this);
        menu.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        searchIcon.setOnClickListener(this);
    }


    //初始化布局
    private void initLayout() {
        //沉浸式透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //绑定组件
        bindView();
        //为界面组件添加监听器
        registerLayoutListener();
    }

    //绑定组件
    private void bindView(){
        locate = findViewById(R.id.fab_locate);
        plan = findViewById(R.id.fab_plan);
        search = findViewById(R.id.search);

        menu = findViewById(R.id.expanded_menu);
        searchIcon=findViewById(R.id.search_ico);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigate_view);
        navigationView.setCheckedItem(R.id.map_standard);
    }


    //标记功能
    private void setMarkerLayout(LatLng location, String name,String pid){
        //移除之前的标记信息，不然会有多个标记
        if (marker != null) {
            marker.remove();
        }
        //通过首页的标记导航
        marker_title=name;
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(location);
        String distanceStr="距离不详";
        if (curLocation != null) {
            float distance = AMapUtils.calculateLineDistance(curLocation, location);
            distanceStr=String.format("%s",
                    "距离你：" + MapUtils.getLengthStr(distance));
        }
        markerOption.title( "POI编号："+pid+"\n名称："+name).snippet("经度："+ location.longitude+"\n维度："+location.latitude+"\n"+distanceStr);
        markerOption.draggable(false);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(),R.drawable.location_marker)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        marker = aMap.addMarker(markerOption);
        marker.showInfoWindow();
    }


    //侧边菜单栏菜单项选择事件处理
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.map_standard:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.map_log:
                startActivity(new Intent(this,LogActivity.class));
                break;
            case R.id.map_about:
                startActivity(new Intent(this,AboutActivity.class));
                break;
            default:
        }
        return true;
    }

    //点击地图上地点事件处理
    @Override
    public void onPOIClick(Poi poi) {
        System.out.println("点击信息点："+poi);
        Toast.makeText(this,poi.getName(),Toast.LENGTH_LONG).show();
        marker_title=poi.getName();
        //标记信息
        setMarkerLayout(poi.getCoordinate(), poi.getName(),poi.getPoiId());
        //将地图移动到信息点
        aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(poi.getCoordinate().latitude,
                poi.getCoordinate().longitude)));
    }

    //监听用户位置的改变
    @Override
    public void onMyLocationChange(Location location) {
        if(location!=null&&location.getExtras().getInt("errorCode", 1)==0) {
            //获取当前位置的经纬度
            curLocation= MapUtils.convertToLatLng(location);
            //如果是首次定位，或者之前定位失败，就重新将地图移动到当前位置
            if (isFirstLocate||isFirstLocateFailed) {
                isFirstLocate = false;
                isFirstLocateFailed=false;
                aMap.animateCamera(CameraUpdateFactory.
                        newLatLngZoom(new LatLng(location.getLatitude(),
                                location.getLongitude()), 16f));
            }
        } else {

            if(isFirstLocate) {
                isFirstLocate=false;
                isFirstLocateFailed=true;
                Toast.makeText(this,"定位失败，请检查网络情况",Toast.LENGTH_LONG).show();
            }
        }
    }

    //界面中按钮点击事件处理
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.fab_locate:
                //我的位置
                if(curLocation!=null) {
                    //实现将地图移到当前定位点
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(curLocation));
                } else {
                    Toast.makeText(this,"定位失败，请检查网络设置",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.fab_plan:
                //路径规划
                if(marker!=null) {
                    System.out.println("标记地点存在"+marker);
                    //标记点目的地
                    RouteActivity.startActivity(this, curLocation, marker.getPosition(),
                            marker_title);
                } else {
                    //空白的路径，需要先进行搜索
                    RouteActivity.startActivity(MainActivity.this, curLocation, null, null);
                }
                break;
            //地点搜索
            case R.id.search:
            case R.id.search_ico:
                //调用静态方法搜索
                Intent intent=new Intent(this,SearchPoiActivity.class);
                startActivityForResult(intent, Constants.REQUEST_MAIN_ACTIVITY);
                break;
            case R.id.expanded_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
    }

    //权限请求结果处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this, "You denied the permission",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                        init();
                } else {
                    Toast.makeText(MainActivity.this, "Unknown mistake",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    //返回键按下逻辑处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            doubleClickExit();
        }
        return false;
    }

    //双击返回实现
    private void doubleClickExit(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
           drawerLayout.closeDrawer(GravityCompat.START);
        } else if(!isBackClickOnce){
            isBackClickOnce=true;
            Snackbar.make(mapView, "请连续按两次退出", Snackbar.LENGTH_SHORT).show();
            Timer exitTimer=new Timer();
            exitTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isBackClickOnce = false;
                }
            }, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    //接收查询的回调数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_MAIN_ACTIVITY:
                //接受从搜索页返回的数据
                if(resultCode==RESULT_OK){
                    if(data.getIntExtra("resultType", 1)==Constants.RESULT_TIP) {
                        Tip tip=data.getParcelableExtra("result");
                        marker_title=tip.getName();
                        setMarkerLayout(MapUtils.convertToLatLng(tip.getPoint()), tip.getName(),tip.getPoiID());
                        isOnResultBack=true;
                 }
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if(isOnResultBack&&marker!=null){
            aMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            isOnResultBack=false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
