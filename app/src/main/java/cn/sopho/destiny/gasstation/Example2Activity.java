package cn.sopho.destiny.gasstation;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Example2Activity extends Activity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
    // 常量
    @SuppressWarnings("unused")
    private static final String LTAG = Example2Activity.class.getSimpleName();
    private static final float INIT_ZOOM = 15.0f; // 初始缩放比例

    // 地图主控件
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;

    // 定位相关
    private LatLng mCurrentPt = null;   // 定位点
    private LocationClient mLocClient = null;
    private MyLocationListenner myListener = new MyLocationListenner();
    private String mCurCity = "苏州市";

    // 搜索模块
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private EditText mEtRadius = null;
    private AutoCompleteTextView mActvSearchkey = null;
    private ArrayAdapter<String> sugAdapter = null;
    private final int loadIndex = 0;

    // UI相关
    private boolean isFirstLoc = true; // 是否首次定位
    private TextView mTvStateBar = null;
    private SlidingPanel mPanelPopup = null;
    private TextView mTvPopupAdd = null;
    private TextView mTvPopupTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example2);

        mTvStateBar = (TextView) findViewById(R.id.tv_state);

        // 隐藏sliding
        mPanelPopup = (SlidingPanel) findViewById(R.id.panel_popup);
        mTvPopupTitle = (TextView) findViewById(R.id.tv_popup_title);
        mTvPopupAdd = (TextView) findViewById(R.id.tv_popup_add);
        mPanelPopup.setVisibility(View.GONE);
        mPanelPopup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPanelPopup.setVisibility(View.GONE);
            }
        });

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        // 打开gps
        option.setCoorType("bd09ll");   // 设置坐标类型
        option.setIsNeedAddress(true);  // 返回城市、地址
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 初始化搜索模块，注册搜索事件监听
        mEtRadius = (EditText) findViewById(R.id.et_radius);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        mActvSearchkey = (AutoCompleteTextView) findViewById(R.id.actv_searchkey);
        mActvSearchkey.setAdapter(sugAdapter);
        mActvSearchkey.setThreshold(1);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        mActvSearchkey.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                // 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(mCurCity).keyword(cs.toString()));
            }
        });

        final ButtonRectangle mBtnSearch = (ButtonRectangle) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTxt = mActvSearchkey.getText().toString();
                if (!searchTxt.isEmpty()) {
                    // 范围检索	    searchInBound()	PoiBoundSearchOption
                    // 城市内检索	searchInCity()	PoiCitySearchOption
                    // 周边检索	    searchInNearby()	PoiNearbySearchOption
                    // 详情检索     searchPoiDetail() 	PoiDetailSearchOption
//                    mPoiSearch.searchInCity((new PoiCitySearchOption())
//                            .city(editCity.getText().toString())
//                            .keyword(editSearchKey.getText().toString())
//                            .pageNum(loadIndex));
                    if (mCurrentPt != null && mActvSearchkey != null) {
                        mPoiSearch.searchNearby((new PoiNearbySearchOption()).location(mCurrentPt)
                                .radius(Integer.parseInt(mEtRadius.getText().toString()))
                                .keyword(mActvSearchkey.getText().toString()).pageCapacity(10).pageNum(loadIndex));
                    }
                } else {
                    Toast.makeText(Example2Activity.this, "请输入要查询的内容！", Toast.LENGTH_LONG).show();
                }
            }
        });

        setTitle(R.string.example2);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }

            // LatLng(double latitude, double longitude)
            mCurrentPt = new LatLng(location.getLatitude(), location.getLongitude());
            String locCity = location.getCity();
            if (locCity != null && !locCity.isEmpty())
                mCurCity = locCity;
            updateMapState();

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()) // 获取纬度坐标
                    .longitude(location.getLongitude()).build(); // 获取经度坐标
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                // MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(18.0f);
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(ll)
                        .zoom(INIT_ZOOM)
                        .build();
                MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.animateMapStatus(u);
            }
        }

        @SuppressWarnings("unused")
        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    /**
     * 更新地图状态显示面板
     */
    private void updateMapState() {
        if (mTvStateBar == null) {
            return;
        }
        String state;
        if (mCurrentPt == null) {
            state = "正在进行定位";
        } else {
            state = String.format("定位经度： %f 定位纬度：%f", mCurrentPt.longitude, mCurrentPt.latitude);
        }
        mTvStateBar.setText(state);
//        mTvStateBar.requestFocus();
    }

    /**
     * 弹出popup窗口
     */
    @SuppressWarnings("unused")
    public void showPopup(String title, String add) {
        mTvPopupTitle.setText(title);
        mTvPopupAdd.setText(add);
        mPanelPopup.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null
                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(Example2Activity.this, "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(Example2Activity.this, strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(Example2Activity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(Example2Activity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();
//            showPopup(result.getName(), result.getAddress());
            DetailActivity detailActivity = new DetailActivity(Example2Activity.this, R.mipmap.unicorn, result.getName().toString(), result.getAddress().toString());
            detailActivity.show();
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        LinkedHashSet<String> suggest = new LinkedHashSet<>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }
        sugAdapter = new ArrayAdapter<>(Example2Activity.this, android.R.layout.simple_dropdown_item_1line, new ArrayList(suggest));
        mActvSearchkey.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            // if (poi.hasCaterDetails) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            // }
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        // 退出时删除搜索模块
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        // 退出时销毁定位
        mLocClient.stop();
        mLocClient = null;
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mBaiduMap.clear();
        mBaiduMap = null;
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }
}
