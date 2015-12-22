package cn.sopho.destiny.gasstation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

public class Example1Activity extends AppCompatActivity {
    // 常量
    @SuppressWarnings("unused")
    private static final String LTAG = Example1Activity.class.getSimpleName();
    private static final float INIT_ZOOM = 13.0f; // 初始缩放比例
    private static final float HUQIU_LONGITUDE = 120.586717f;  // 虎丘塔经度
    private static final float HUQIU_LATITUDE = 31.342300f;    // 虎丘塔纬度
    private static final float PANMEN_LONGITUDE = 120.623161f;  // 盘门经度
    private static final float PANMEN_LATITUDE = 31.295132f;    // 盘门纬度
    private static final float ZHUOZHENGYUAN_LONGITUDE = 120.635684f;  // 拙政园经度
    private static final float ZHUOZHENGYUAN_LATITUDE = 31.330279f;    // 拙政园纬度
    private static final float HANSHANSI_LONGITUDE = 120.574752f;  // 寒山寺经度
    private static final float HANSHANSI_LATITUDE = 31.316143f;    // 寒山寺纬度

    private static final String TITLE_HUQIU = "虎丘";
    private static final String TITLE_PANMEN = "盘门";
    private static final String TITLE_ZHUOZHENGYUAN = "拙政园";
    private static final String TITLE_HANSHANSI = "寒山寺";

    // 地图主控件
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;

    // 覆盖物
    private Marker mMarkerHuqiu = null;
    private Marker mMarkerPanmen = null;
    private Marker mMarkerZhuozhengyuan = null;
    private Marker mMarkerHanshansi = null;

    // 地图选点
    private LatLng mSelectPt = null;

    // 定位相关
    private LatLng mCurrentPt = null;   // 定位点
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();

    // UI相关
    private boolean isFirstLoc = true; // 是否首次定位
    private TextView mStateBar;
    private SlidingPanel mPopupPanel;
    private TextView mPopupTitle;
    private ImageView mPopupImage;

    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.mipmap.icon_marka);
    BitmapDescriptor bdB = BitmapDescriptorFactory.fromResource(R.mipmap.icon_markb);
    BitmapDescriptor bdC = BitmapDescriptorFactory.fromResource(R.mipmap.icon_markc);
    BitmapDescriptor bdD = BitmapDescriptorFactory.fromResource(R.mipmap.icon_markd);
    BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
    BitmapDescriptor bdGround = BitmapDescriptorFactory.fromResource(R.mipmap.ground_overlay);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example1);

        mStateBar = (TextView) findViewById(R.id.state);

        // 底部sliding
        mPopupPanel = (SlidingPanel) findViewById(R.id.popup_panel);
        mPopupTitle = (TextView) findViewById(R.id.popup_title);
        mPopupImage = (ImageView) findViewById(R.id.popup_image);
        mPopupPanel.setVisibility(View.GONE);
        mPopupPanel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPopupPanel.setVisibility(View.GONE);
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
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 监听地图点击事件
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                mSelectPt = point;
                updateMapState();
            }

            public boolean onMapPoiClick(MapPoi poi) {
                return false;
            }
        });

        // 添加覆盖物
        initOverlay();

        setTitle(R.string.example1);
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

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    /**
     * 更新地图状态显示面板
     */
    private void updateMapState() {
        if (mStateBar == null) {
            return;
        }
        String state;
        if (mCurrentPt == null) {
            state = "正在进行定位";
        } else {
            state = String.format("定位经度： %f 定位纬度：%f", mCurrentPt.longitude, mCurrentPt.latitude);
        }
        state += "\n";
        if (mSelectPt == null) {
            state += "点击地图以获取经纬度和地图状态";
        } else {
            state += String.format("所选经度： %f 所选纬度：%f", mSelectPt.longitude, mSelectPt.latitude);
        }
        state += "\n";
        MapStatus ms = mBaiduMap.getMapStatus();
        state += String.format("zoom=%.1f rotate=%d overlook=%d", ms.zoom, (int) ms.rotate, (int) ms.overlook);
        mStateBar.setText(state);
    }

    /**
     * 添加覆盖物
     */
    public void initOverlay() {
        // Add marker overlay
        LatLng llA = new LatLng(HUQIU_LATITUDE, HUQIU_LONGITUDE);
        LatLng llB = new LatLng(PANMEN_LATITUDE, PANMEN_LONGITUDE);
        LatLng llC = new LatLng(ZHUOZHENGYUAN_LATITUDE, ZHUOZHENGYUAN_LONGITUDE);
        LatLng llD = new LatLng(HANSHANSI_LATITUDE, HANSHANSI_LONGITUDE);

        // zIndex - 深度，值越小越在下面
        // period - 帧数， 刷新周期，值越小速度越快。默认为20，最小为1
        // draggable - 是否可以拖拽，默认不可拖拽
        // perspective - 是否开启 marker 覆盖物近大远小效果，默认开启
        // anchor - marker 覆盖物的锚点比例，默认（0.5f, 1.0f）水平居中，垂直下对齐
        // rotate - marker 覆盖物旋转角度，逆时针
        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).zIndex(1).draggable(true).title(TITLE_HUQIU);
        // ooA.animateType(MarkerOptions.MarkerAnimateType.drop);// 掉下动画
        ooA.animateType(MarkerOptions.MarkerAnimateType.grow);// 生长动画
        mMarkerHuqiu = (Marker) (mBaiduMap.addOverlay(ooA));

        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB).zIndex(2).draggable(true).title(TITLE_PANMEN);
        // ooB.animateType(MarkerOptions.MarkerAnimateType.drop); // 掉下动画
        ooB.animateType(MarkerOptions.MarkerAnimateType.grow); // 生长动画
        mMarkerPanmen = (Marker) (mBaiduMap.addOverlay(ooB));

        MarkerOptions ooC = new MarkerOptions().position(llC).icon(bdC).zIndex(3).draggable(true).title(TITLE_ZHUOZHENGYUAN); // .perspective(false).anchor(0.5f, 0.5f).rotate(30)
        ooC.animateType(MarkerOptions.MarkerAnimateType.grow);  // 生长动画
        mMarkerZhuozhengyuan = (Marker) (mBaiduMap.addOverlay(ooC));

//        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
//        giflist.add(bdA);
//        giflist.add(bdB);
//        giflist.add(bdC);
//        MarkerOptions ooD = new MarkerOptions().position(llD).icons(giflist).zIndex(0).period(10);
        MarkerOptions ooD = new MarkerOptions().position(llD).icon(bdD).zIndex(4).draggable(true).title(TITLE_HANSHANSI); // .period(10)
        ooD.animateType(MarkerOptions.MarkerAnimateType.grow);  // 生长动画
        mMarkerHanshansi = (Marker) (mBaiduMap.addOverlay(ooD));

        // Add ground overlay
        LatLng southwest = new LatLng(31.285132f, 120.564752f);
        LatLng northeast = new LatLng(31.352300f, 120.645684f);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();

        OverlayOptions ooGround = new GroundOverlayOptions().positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        // 监听覆盖物拖拽事件
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(
                        Example1Activity.this, marker.getTitle() + "拖拽结束，新位置：" + marker.getPosition().latitude + ", " + marker.getPosition().longitude,
                        Toast.LENGTH_LONG).show();
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });

        // 监听覆盖物点击事件
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                if (marker == mMarkerHuqiu) {
                    showPopup(TITLE_HUQIU, R.mipmap.pic_huqiu);
                } else if (marker == mMarkerPanmen) {
                    showPopup(TITLE_PANMEN, R.mipmap.pic_panmen);
                } else if (marker == mMarkerZhuozhengyuan) {
                    showPopup(TITLE_ZHUOZHENGYUAN, R.mipmap.pic_zhuozhengyuan);
                } else if (marker == mMarkerHanshansi) {
                    showPopup(TITLE_HANSHANSI, R.mipmap.pic_hanshansi);
                }

                return true;
            }
        });
    }

    /**
     * 弹出popup窗口
     */
    public void showPopup(String title, int image) {
        mPopupTitle.setText(title);
        mPopupImage.setImageResource(image);
        mPopupPanel.setVisibility(View.VISIBLE);
    }

    /**
     * 清除所有Overlay
     */
    public void clearOverlay(View view) {
        mBaiduMap.clear();
        mMarkerHuqiu = null;
        mMarkerPanmen = null;
        mMarkerZhuozhengyuan = null;
        mMarkerHanshansi = null;
    }

    /**
     * 重新添加Overlay
     */
    public void resetOverlay(View view) {
        clearOverlay(null);
        initOverlay();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();

        // 回收 bitmap 资源
        bdA.recycle();
        bdB.recycle();
        bdC.recycle();
        bdD.recycle();
        bd.recycle();
        bdGround.recycle();
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
