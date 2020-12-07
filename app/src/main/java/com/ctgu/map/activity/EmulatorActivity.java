package com.ctgu.map.activity;

import android.content.Intent;
import android.os.Bundle;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.NaviLatLng;
import com.ctgu.map.R;
import com.ctgu.map.util.TTSController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EmulatorActivity extends BaseActivity {
    private TTSController controller;
    protected NaviLatLng mEndLatlng = new NaviLatLng(40.084894,116.603039);
    protected NaviLatLng mStartLatlng = new NaviLatLng(39.825934,116.342972);
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        Map<String,NaviLatLng> map = new Gson().fromJson(intent.getStringExtra("location"), new TypeToken<HashMap<String,NaviLatLng>>(){}.getType());

        Map<String, NaviLatLng> hashMap=map;
        System.out.println("获取的map"+hashMap);
        mStartLatlng= hashMap.get("start");
        mEndLatlng= hashMap.get("end");

        System.out.println("导航页的起点位置信息"+mStartLatlng);
        System.out.println("导航页的终点位置信息"+mEndLatlng);

        setContentView(R.layout.activity_basic_navi);
        mAMapNaviView = findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        boolean isUseInnerVoice = getIntent().getBooleanExtra("useInnerVoice", true);

        if (isUseInnerVoice) {
            /**
             * 设置使用内部语音播报，
             * 使用内部语音播报，用户注册的AMapNaviListener中的onGetNavigationText 方法将不再回调
             */
            mAMapNavi.setUseInnerVoice(isUseInnerVoice,true);
        }

//        AMapNaviViewOptions options=new AMapNaviViewOptions();
//        options.setAutoDrawRoute(true);
//        options.setScreenAlwaysBright(true);
//        mAMapNaviView.setViewOptions(options);
        controller= TTSController.getInstance(getApplicationContext());
        mAMapNavi= AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(controller);
        AMapNavi.setTtsPlaying(false);
        //设置模拟导航的行车速度
        mAMapNavi.setEmulatorNaviSpeed(75);
        sList.add(mStartLatlng);
        eList.add(mEndLatlng);
    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        //路径规划成功之后开始导航
        super.onCalculateRouteSuccess(aMapCalcRouteResult);
//        mAMapNavi.startNavi(NaviType.EMULATOR);
        mAMapNavi.startNavi(NaviType.GPS);
    }
}
