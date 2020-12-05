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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.ctgu.map.R;
import com.ctgu.map.utils.Constants;
import com.ctgu.map.utils.MapUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements LocationSource, AMap.OnPOIClickListener,
        AMap.OnMyLocationChangeListener, View.OnClickListener,
        GeocodeSearch.OnGeocodeSearchListener, NavigationView.OnNavigationItemSelectedListener,
        PoiSearch.OnPoiSearchListener{

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    private Marker marker;
    private String marker_title;
    private String city=Constants.DEFAULT_CITY;
    private LatLng curLocation;

    private boolean isFirstLocate=true;
    private boolean isFirstLocateFailed=false;
    private boolean isBackClickOnce=false;
    private boolean isOnResultBack=false;

    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    private MapView mapView;
    private AMap aMap;

    private NestedScrollView bottomSheet;
    private TextView textName;
    private TextView textDistance;
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
        try {
            checkPermission();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         LatLng latLng = new LatLng(22.56686, 114.170988);
         MarkerOptions markerOption = new MarkerOptions();
         markerOption.title("我是Title").snippet("market desc market desc");
         markerOption.draggable(true);//设置Marker可拖动
         markerOption.position(latLng);
         //        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.main_marker_icon));
         //设置覆盖物比例m
         markerOption.anchor(0.5f, 0.5f);
         Marker marker = mapView.getMap().addMarker(markerOption);
         */
        /**
        mapView.getMap().setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                } else {
                    marker.hideInfoWindow();
                }
                return true;
            }
        });*/

        // 定义 Marker 点击事件监听
//        AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
//            // marker 对象被点击时回调的接口
//            // 返回 true 则表示接口已响应事件，否则返回false
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                System.out.println("点击标记点"+marker);
//                if(marker!=null){
//                    marker.showInfoWindow();
//                    return true;
//                }
//                marker.setTitle("未知");
//                marker.setSnippet("暂无");
//                return true;
//            }
//        };
//        // 绑定 Marker 被点击事件
//        aMap.setOnMarkerClickListener(markerClickListener);
    }

    //请求必要权限
    private void checkPermission() throws IOException {
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

    private void init() throws IOException {
        initLayout();
        initMap();
    }

    //自定义导航图标
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
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.direction);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(scaleBitmap(bitmap,0.12f)));
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
//        myLocationStyle.strokeColor(Color.argb(0,0,0,0)).radiusFillColor(Color.argb(0,255,192,203)).
//                interval(10000);
        myLocationStyle.strokeColor(Color.argb(100, 255, 144, 147));// 设置圆形的边框颜色

        myLocationStyle.radiusFillColor(Color.argb(100, 255, 144, 233));// 设置圆形的填充颜色



        //频率
        myLocationStyle.interval(100000);
//        精度圈边框宽度自定义方法如下
        myLocationStyle.strokeWidth(0.1f);
        aMap.setMyLocationStyle(myLocationStyle);
        registerMapListener();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
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

    //点击标记弹出框
    //初始化界面
    private void initLayout() {
        //沉浸式透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        bindView();
        registerLayoutListener();
    }

    //绑定组件
    private void bindView(){
        locate = findViewById(R.id.fab_locate);
        plan = findViewById(R.id.fab_plan);
//        textName = findViewById(R.id.text_name);
//        textName.setText("我的位置");
        textDistance = findViewById(R.id.text_distance);
        bottomSheet = findViewById(R.id.bottom_sheet_main);
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        search = findViewById(R.id.search);

        menu = findViewById(R.id.expanded_menu);
        searchIcon=findViewById(R.id.search_ico);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigate_view);
        navigationView.setCheckedItem(R.id.map_standard);
    }


    //在地图上设置标记
    private void setMarkerLayout(LatLng location, String name,String pid){
        //移除之前的标记信息
        if (marker != null) {
            marker.remove();
        }

        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(location);
//        textName.setText(marker_title);
        String distanceStr="距离不详";
        if (curLocation != null) {
            float distance = AMapUtils.calculateLineDistance(curLocation, location);
            distanceStr=String.format("%s",
                    "距离你：" + MapUtils.getLengthStr(distance));
        }
        markerOption.title( "编号："+pid+"\n名称："+name).snippet("经度："+ location.latitude+"\n维度："+location.longitude+"\n"+distanceStr);
        markerOption.draggable(false);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(),R.drawable.location_marker)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        marker = aMap.addMarker(markerOption);
        marker.showInfoWindow();
    }

    //开始地理位置逆编码
    private void geocodeSearch(LatLonPoint location){
        final GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
        final RegeocodeQuery query = new RegeocodeQuery(location, 50, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
    }

    //通过传入id搜索对应的地点
    private void POIIdSearch(String id){
        final PoiSearch poiSearch=new PoiSearch(this, null);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIIdAsyn(id);
    }

    //根据id搜索对应地点的搜索结果处理
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        if(i==1000&&poiItem!=null){
            city=poiItem.getCityName();
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        System.out.println("搜索记录"+poiResult);
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

    //地理逆编码结果处理
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if(i==1000){
            city=regeocodeResult.getRegeocodeAddress().getCity();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    //点击地图上地点事件处理
    @Override
    public void onPOIClick(Poi poi) {
        System.out.println("点击信息点："+poi);
        Toast.makeText(this,poi.getName(),Toast.LENGTH_LONG).show();
        marker_title=poi.getName();
        //标记信息
        setMarkerLayout(poi.getCoordinate(), poi.getName(),poi.getPoiId());
        //将地图移动到定位点
        aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(poi.getCoordinate().latitude,
                poi.getCoordinate().longitude)));
    }

    //监听用户位置的改变
    @Override
    public void onMyLocationChange(Location location) {
        String json=new Gson().toJson(location);
//        System.out.println("当前位置"+json);
        /**
        Request request = new Request.Builder().url("http://wechat.iboy.tech/location")
                .post(RequestBody.create(JSON, json)).build();
        Call call=client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Fail");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
         */
        if(location!=null&&location.getExtras().getInt("errorCode", 1)==0) {
            curLocation= MapUtils.convertToLatLng(location);
            if (isFirstLocate||isFirstLocateFailed) {
                isFirstLocate = false;
                isFirstLocateFailed=false;
                geocodeSearch(MapUtils.convertToLatLonPoint(location));
                aMap.animateCamera(CameraUpdateFactory.
                        newLatLngZoom(new LatLng(location.getLatitude(),
                                location.getLongitude()), 16f));
            }
        } else {
            if(isFirstLocate) {
                isFirstLocate=false;
                isFirstLocateFailed=true;
                Snackbar.make(mapView, "Locate failed. Please check your settings.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    //界面中按钮点击事件处理
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_locate:
                if(curLocation!=null) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(curLocation));
                    geocodeSearch(MapUtils.convertToLatLonPoint(curLocation));
                } else {
                    Snackbar.make(mapView, "定位失败",
                            Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.fab_plan:
                //路径规划
                if(marker!=null) {
                    //标记点目的地
                    RouteActivity.startActivity(this, curLocation, marker.getPosition(),
                            marker_title, city);
                } else {
                    //空白的路径，需要先进行搜索
                    RouteActivity.startActivity(MainActivity.this, curLocation, null, null, city);
                }
                break;
            //路径规划
            case R.id.search:
            case R.id.search_ico:
                //调用静态方法搜索
                SearchActivity.startActivity(MainActivity.this,
                        Constants.REQUEST_MAIN_ACTIVITY, city);
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
                    try {
                        init();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        BottomSheetBehavior behavior=BottomSheetBehavior.from(bottomSheet);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
           drawerLayout.closeDrawer(GravityCompat.START);
        } else if(behavior.getState()== BottomSheetBehavior.STATE_EXPANDED){
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

    //跳转活动返回数据处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_MAIN_ACTIVITY:
                if(resultCode==RESULT_OK){
                    if(data.getIntExtra("resultType", 1)==Constants.RESULT_TIP) {
                        Tip tip=data.getParcelableExtra("result");
                        POIIdSearch(tip.getPoiID());
                        setMarkerLayout(MapUtils.convertToLatLng(tip.getPoint()), tip.getName(),tip.getPoiID());
                        isOnResultBack=true;
                    } else {
                        PoiItem poiItem=data.getParcelableExtra("result");
                        city=poiItem.getCityName();
                        setMarkerLayout(MapUtils.convertToLatLng(poiItem.getLatLonPoint()),
                                poiItem.getTitle(),poiItem.getPoiId());
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

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener((AMapLocationListener) this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //该方法默认为false，true表示只定位一次
            mLocationOption.setOnceLocation(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}
