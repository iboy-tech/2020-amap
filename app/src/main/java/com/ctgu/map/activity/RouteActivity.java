   package com.ctgu.map.activity;

   import android.app.ProgressDialog;
   import android.content.Context;
   import android.content.Intent;
   import android.location.Location;
   import android.os.Bundle;
   import android.os.Parcelable;
   import android.support.design.widget.BottomSheetBehavior;
   import android.support.design.widget.FloatingActionButton;
   import android.support.design.widget.Snackbar;
   import android.support.design.widget.TabLayout;
   import android.support.v4.app.NavUtils;
   import android.support.v7.app.AppCompatActivity;
   import android.support.v7.widget.LinearLayoutManager;
   import android.support.v7.widget.RecyclerView;
   import android.view.KeyEvent;
   import android.view.View;
   import android.view.ViewGroup;
   import android.widget.LinearLayout;
   import android.widget.TextView;

   import com.amap.api.maps.AMap;
   import com.amap.api.maps.AMapOptions;
   import com.amap.api.maps.MapView;
   import com.amap.api.maps.UiSettings;
   import com.amap.api.maps.model.LatLng;
   import com.amap.api.navi.AMapNavi;
   import com.amap.api.navi.AMapNaviListener;
   import com.amap.api.navi.model.AMapCalcRouteResult;
   import com.amap.api.navi.model.AMapLaneInfo;
   import com.amap.api.navi.model.AMapModelCross;
   import com.amap.api.navi.model.AMapNaviCameraInfo;
   import com.amap.api.navi.model.AMapNaviCross;
   import com.amap.api.navi.model.AMapNaviLocation;
   import com.amap.api.navi.model.AMapNaviPath;
   import com.amap.api.navi.model.AMapNaviRouteNotifyData;
   import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
   import com.amap.api.navi.model.AMapServiceAreaInfo;
   import com.amap.api.navi.model.AimLessModeCongestionInfo;
   import com.amap.api.navi.model.AimLessModeStat;
   import com.amap.api.navi.model.NaviInfo;
   import com.amap.api.navi.model.NaviLatLng;
   import com.amap.api.navi.view.RouteOverLay;
   import com.amap.api.services.core.PoiItem;
   import com.amap.api.services.help.Tip;
   import com.amap.api.services.poisearch.PoiResult;
   import com.amap.api.services.poisearch.PoiSearch;
   import com.amap.api.services.route.BusRouteResult;
   import com.amap.api.services.route.DriveRouteResult;
   import com.amap.api.services.route.RideRouteResult;
   import com.amap.api.services.route.RouteSearch;
   import com.amap.api.services.route.WalkRouteResult;
   import com.ctgu.map.R;
   import com.ctgu.map.adapter.RouteDetailAdapter;
   import com.ctgu.map.util.Constants;
   import com.ctgu.map.util.MapUtils;
   import com.google.gson.Gson;

   import java.util.ArrayList;
   import java.util.HashMap;
   import java.util.List;
   import java.util.Map;

   public class RouteActivity extends AppCompatActivity implements View.OnClickListener,
        AMapNaviListener, TabLayout.OnTabSelectedListener, RouteSearch.OnRouteSearchListener,
        PoiSearch.OnPoiSearchListener{

    private static final String MY_LOCATION="我的位置";
    private static final String DRIVE_TAB="驾驶";
    private static final String WALK_TAB="步行";
    private static final String RIDE_TAB="骑行";
    private static final int DRIVE_MODE=0;
    private static final int WALK_MODE=1;
    private static final int RIDE_MODE=2;

    private int isSearchingText= R.id.text_destination;
    private int curMode=0;
    private String city;

    //起点和目的地
    private NaviLatLng locationDeparture;
    private NaviLatLng locationDestination;
    private final List<NaviLatLng> from=new ArrayList<>();
    private final List<NaviLatLng> to=new ArrayList<>();
    private final List<NaviLatLng> wayPoints=new ArrayList<>();
    private RouteOverLay routeOverLay;

    private TextView textDeparture;
    private TextView textDestination;
    private MapView mapView;
    private AMap aMap;
    private AMapNavi aMapNavi;
    private ProgressDialog loadingDialog;
    private TextView textEmpty;
    private TextView textDistance;
    private TextView textTime;
    private LinearLayout bottomSheet;
    private FloatingActionButton navigate;
    private RecyclerView detailList;




    //活动跳转函数
    public static void startActivity(Context context, LatLng curLocation,
                                     LatLng targetLocation, String targetName, String city){
        Intent intent=new Intent(context, RouteActivity.class);
        if(curLocation!=null){
            //已经获取当前位置
            intent.putExtra("hasCur", true);
            intent.putExtra("curLocation", curLocation);
        } else {
            intent.putExtra("hasCur", false);
        }
        //已经有目的地了
        if(targetLocation!=null){
            intent.putExtra("hasTarget", true);
            intent.putExtra("targetLocation", targetLocation);
            intent.putExtra("name", targetName);
        } else {
            intent.putExtra("hasTarget", false);
        }
        intent.putExtra("city", city);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        mapView= findViewById(R.id.map_view_route);
        mapView.onCreate(savedInstanceState);

        initMap();
        initLayout();
        Intent intent=getIntent();
        if(intent.getBooleanExtra("hasTarget", false)){
            textDestination.setText(intent.getStringExtra("name"));
            locationDestination= MapUtils.convertToNaviLatLng(
                    (LatLng)intent.getParcelableExtra("targetLocation"));
        }
        if(intent.getBooleanExtra("hasCur", false)){
            textDeparture.setText(MY_LOCATION);
            locationDeparture=MapUtils.convertToNaviLatLng(
                    (LatLng)intent.getParcelableExtra("curLocation"));
        }
        city=getIntent().getStringExtra("city");
        RouteSearch routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
        if(locationDeparture!=null&&locationDestination!=null) {
            aMapNavi = AMapNavi.getInstance(getApplicationContext());
            aMapNavi.addAMapNaviListener(this);
        }
    }

    //初始化地图
    private void initMap(){
//        mapView.setVisibility(View.GONE);
        if(aMap==null){
            aMap=mapView.getMap();
        }
        UiSettings uiSettings=aMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
//        uiSettings.setAllGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(true); //允许手势缩放
        uiSettings.setCompassEnabled(true);// 设置指南针是否显示
        uiSettings.setZoomControlsEnabled(false);// 设置缩放按钮是否显示
    }

    //初始化界面
    private void initLayout(){

        if(NavUtils.getParentActivityName(RouteActivity.this)!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //初始化Tab栏
        TabLayout tabLayout= findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.drive).setTag(DRIVE_TAB));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.walk).setTag(WALK_TAB));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ride).setTag(RIDE_TAB));

        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab firstTab=tabLayout.getTabAt(0);
        if(firstTab!=null){
            firstTab.select();
        }

        navigate= findViewById(R.id.fab_navigate);
//        navigate.setVisibility(View.GONE);
        textDistance= findViewById(R.id.text_distance);
        textTime= findViewById(R.id.text_time);
        bottomSheet= findViewById(R.id.bottom_sheet_route);
//        bottomSheet.setVisibility(View.GONE);
        textEmpty= findViewById(R.id.text_empty);
        textEmpty.setText(String.format("%s", "No viable route. Please try other ways."));
        textDeparture= findViewById(R.id.text_departure);
        textDestination= findViewById(R.id.text_destination);
        detailList= findViewById(R.id.recyclerView_detail);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        detailList.setLayoutManager(layoutManager);
        registerListener();
    }

    //为界面组件注册监听器
    private void registerListener(){
        navigate.setOnClickListener(this);
        textDeparture.setOnClickListener(this);
        textDestination.setOnClickListener(this);
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



    //开始路线规划
    private void calculateRoute(){
        showLoadingDialog();
        switch (curMode) {
            //路径规划
            case DRIVE_MODE:
                //规划的策略
                int strategy = 0;
                try {
                    strategy = aMapNavi.strategyConvert(false, false, false, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                from.add(locationDeparture);
                to.add(locationDestination);
                aMapNavi.calculateDriveRoute(from, to, wayPoints, strategy);
                break;
            case WALK_MODE:
                aMapNavi.calculateWalkRoute(locationDeparture, locationDestination);
                break;
            case RIDE_MODE:
                aMapNavi.calculateRideRoute(locationDeparture, locationDestination);
                break;
            default:
        }
    }

    //清除地图上的路线
    private void clearOverLay(){
        if(routeOverLay!=null){
            routeOverLay.removeFromMap();
            routeOverLay.destroy();
        }
    }

    //在地图上显示路线
    private void drawOverLay(AMapNaviPath path){
        routeOverLay=new RouteOverLay(aMap, path, this);
        routeOverLay.addToMap();
        routeOverLay.zoomToSpan(200);
    }

    //开始导航
    private void startNavigate(){

//        Intent intent = new Intent(RouteActivity.this, EmulatorActivity.class);
//        Intent intent = new Intent(RouteActivity.this, AllCustomNaviActivity.class);
        //将目的地的经纬度传递过去
        //不使用内置语音导航
        Intent intent = new Intent(getApplicationContext(), RouteNaviActivity.class);
        intent.putExtra("gps", true);

//        Map<String, NaviLatLng> map=new HashMap<>();
//        map.put("start",locationDeparture);
//        map.put("end",locationDestination);
//        intent.putExtra("location",  new Gson().toJson(map));
        startActivity(intent);
//        intent.putExtra("useInnerVoice", true);
//        startActivity(intent);
    }

    //根据由地点搜索回调的数，对界面反馈进行设置
    private void setSearchingResult(NaviLatLng location, String name){
        switch (isSearchingText){
            case R.id.text_departure:
                locationDeparture=location;
                textDeparture.setText(name);
                break;
            case R.id.text_destination:
                locationDestination=location;
                textDestination.setText(name);
                break;
            default:
        }
    }




    //将界面设置为地图显示路线规划模式
    private void setMapRouteView(){
        navigate.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.VISIBLE);
        bottomSheet.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
    }

    //将界面设置为查找无结果模式
    private void setNoResultView(){
        mapView.setVisibility(View.GONE);
        bottomSheet.setVisibility(View.GONE);
        navigate.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
    }

    //将界面设置为初始状态
    private void resetView(){
        mapView.setVisibility(View.GONE);
        bottomSheet.setVisibility(View.GONE);
        navigate.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);
    }
    //通过传入id搜索对应的地点
    private void POIIdSearch(String id){
        final PoiSearch poiSearch=new PoiSearch(this, null);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIIdAsyn(id);
    }

    //返回键按下逻辑处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            BottomSheetBehavior behavior=BottomSheetBehavior.from(bottomSheet);
            if(behavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                finish();
            }
        }
        return false;
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

    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    //公交路线规划结果处理
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    //TAB的回调方法
    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //Tab被选中逻辑处理
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        System.out.println("被选中的TAB"+tab.getTag());
        if(tab!=null && tab.getTag().toString().equals(DRIVE_TAB)){
            System.out.println("驾驶模式");
            if(curMode!=DRIVE_MODE){
                curMode=DRIVE_MODE;
                if(locationDeparture!=null&&locationDestination!=null){
                    calculateRoute();
                }
            }
        } else if(tab!=null  && tab.getTag().toString().equals(WALK_TAB)){
            System.out.println("步行模式");
            if(curMode!=WALK_MODE){
                curMode=WALK_MODE;
                if(locationDeparture!=null&&locationDestination!=null){
                    calculateRoute();
                }
            }
        } else if(tab!=null && tab.getTag().toString().equals(RIDE_TAB)){
            System.out.println("骑行模式");
            if(curMode!=RIDE_MODE){
                curMode=RIDE_MODE;
                if(locationDeparture!=null&&locationDestination!=null){
                    calculateRoute();
                }
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }
//TAB的回调方法


    //界面中按钮被点击逻辑处理
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_navigate:
                //导航按钮
                startNavigate();
                break;
            case R.id.text_departure:
                isSearchingText=R.id.text_departure;
                SearchActivity.startActivity(RouteActivity.this,
                        Constants.REQUEST_ROUTE_ACTIVITY, city);
                break;
            case R.id.text_destination:
                isSearchingText=R.id.text_destination;
                SearchActivity.startActivity(RouteActivity.this,
                        Constants.REQUEST_ROUTE_ACTIVITY, city);
                break;
            default:
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //获取的搜素结果
        switch (requestCode){
            case Constants.REQUEST_ROUTE_ACTIVITY:
                if(resultCode==RESULT_OK){
                    if(data.getIntExtra("resultType", 1)==Constants.RESULT_TIP) {
                        Tip tip=data.getParcelableExtra("result");
                        POIIdSearch(tip.getPoiID());
                        //根据搜索结果设置目的地
                        setSearchingResult(MapUtils.convertToNaviLatLng(tip.getPoint()),
                                tip.getName());

                    } else {
                        PoiItem poiItem=data.getParcelableExtra("result");
                        city=poiItem.getCityName();
                        setSearchingResult(MapUtils.convertToNaviLatLng(poiItem.getLatLonPoint()),
                                poiItem.getTitle());
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearOverLay();
        if(aMapNavi!=null) {
            aMapNavi.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if(locationDeparture!=null&&locationDestination!=null){
            if(aMapNavi!=null)
                aMapNavi.destroy();
            aMapNavi = AMapNavi.getInstance(getApplicationContext());
            aMapNavi.addAMapNaviListener(this);
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
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {
            calculateRoute();
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }



    //路线规划失败处理
    @Override
    public void onCalculateRouteFailure(int i) {
        dismissLoadingDialog();
        Snackbar.make(mapView, "Route searching failed. Error code "+i,
                Snackbar.LENGTH_SHORT).show();
        resetView();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }



    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //路线规划成功处理（驾车、骑行、步行）
        dismissLoadingDialog();
        if(curMode==DRIVE_MODE) {
            to.clear();
            from.clear();
        }
        AMapNaviPath path=aMapNavi.getNaviPath();
        if(path!=null){
            clearOverLay();
            //绘制路径
            drawOverLay(path);
            String distanceStr=MapUtils.getLengthStr(path.getAllLength());
            String timeStr=MapUtils.getTimeStr(path.getAllTime());
            textDistance.setText(distanceStr);
            textTime.setText(timeStr);
            RouteDetailAdapter adapter=new RouteDetailAdapter(aMapNavi.getNaviGuideList());
            detailList.setAdapter(adapter);
            setMapRouteView();
        } else {
            setNoResultView();
        }
    }


    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(
            AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(
            AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }



    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(
            AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }


    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }
}
