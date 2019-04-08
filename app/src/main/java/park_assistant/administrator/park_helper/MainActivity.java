package park_assistant.administrator.park_helper;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.test.mock.MockApplication;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import park_assistant.administrator.park_helper.filter.*;
import park_assistant.administrator.park_helper.activity.BaseActivity;
import park_assistant.administrator.park_helper.activity.MyIntent;
import park_assistant.administrator.park_helper.http.*;
import park_assistant.administrator.park_helper.view.buttom;
import park_assistant.administrator.park_helper.sqlite.*;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import park_assistant.administrator.park_helper.file.*;

import park_assistant.administrator.park_helper.view.*;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Marker rent_marker=null;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();
    private Button bt;
    private Button button;
    private Button buttons;
    private LatLng latLng;
    private ListView to_list;
    private EditText mylocation;
    private OnGetSuggestionResultListener listener;
    private EditText where_i_go;
    private boolean isFirstLoc = true; // 是否首次定位
    private SuggestionSearch suggestionSearch;
    private String city=null;
    private SimpleAdapter adapter;
    private List<Map<String,Object>> list=new ArrayList<>();
    private LinearLayout show;
    private EditText showresult;
    private EditText rent_location;
    private EditText rent_city;
    private map_operate map_operate;
    private http http;
    private Animation animation;
    private ImageView imageView;
    private filter filter;//创建文本过滤器
    private String image_name;
    private String work_path="/storage/emulated/0/park_helper/";//初始化工作路径
    private HashMap <String,String> post_data=new HashMap<>();
    private LinearLayout console;
    private sqlite sqlite;
    private ImageView code;//初始化验证码的容器
    private AlertDialog nowdialog=null;//获取当前显示的dialog
    private String user_name=null;//登录时和获取登录名
    private HashMap user_info=null;//初始化用户数据
    private Boolean is_login=false;//判断是否登录
    private ImageView head_view=null;//设置头像对象
    private BDLocation maplocation=null;//获取百度地图位置对象
    private android.support.v7.widget.Toolbar toolbar;
    private Boolean is_load=false;//判断地图数据是否加载
    private LinearLayout op_borad=null;//初始化操作面板
    private Button op_show=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        init();
    }
    private void initPermission() {
        onRequestPermission(new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, new park_assistant.administrator.park_helper.activity.OnPermissionCallbackListener() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                finish();
            }
        });
    }
    public void init() {;
        //setContentView(R.layout.activity_main);
        initPermission();
        initView();
        initMap();
        file.file_path_create(work_path);
        installData();
        sqlite=new sqlite(this,"user.db",null,1);
        is_login=is_login();
        if(!is_login)
        {
            show_login();
        }
    }
    public void installData(){

    }
    public void get_park_location(String privince,String city){
        HashMap map=new HashMap();
        map.put("type","get_park_location");
        http.get(String.format("http://39.108.236.127/php/public/index.php/map/user_get_park?privince=%s&city=%s",privince,city),map);
    }
    public void set_now_location(){
        get_park_location(maplocation.getProvince(),maplocation.getCity());
    }
    public Boolean is_login(){
        try {
            Gson gson=new Gson();
            String data=file.readFileSdcardFile(work_path+"user.txt");
            if(data.length()!=0)
            {
                user_info=gson.fromJson(data,HashMap.class);
                TextView textView=(TextView)findViewById(R.id.user_name);
                textView.setText(user_info.get("name")+"");
                head_view=(ImageView)findViewById(R.id.head_img);
                HashMap map=new HashMap();
                map.put("type","set_img");
                map.put("stream_type","png");
                http.get(user_info.get("head_img")+"",map);
                return true;
            }
            else {
                user_info=null;
            }
        }
        catch (Exception E)
        {
            return false;
        }
        return false;
    }
    private void initMap() {
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        //默认显示普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        //mBaiduMap.setTrafficEnabled(true);
        //开启热力图
        //mBaiduMap.setBaiduHeatMapEnabled(true);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MainActivity.this, latLng.latitude + "_" + latLng.longitude, Toast.LENGTH_LONG).show();
                Log.i("show", latLng.longitude + "," + latLng.latitude);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                //点击地图上的poi图标获取描述信息：mapPoi.getName()，经纬度：mapPoi.getPosition()
                return false;
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法
            //若响应点击事件，返回true，否则返回false
            //默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MainActivity.this,"showdata",Toast.LENGTH_LONG).show();
                return false;
            }
        });
        map_operate=new map_operate(mBaiduMap,msgHandler);
    }

    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true); // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {

            //在Marker拖拽过程中回调此方法，这个Marker的位置可以通过getPosition()方法获取
            //marker 被拖动的Marker对象
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            //在Marker拖动完成后回调此方法， 这个Marker的位可以通过getPosition()方法获取
            //marker 被拖拽的Marker对象
            @Override
            public void onMarkerDragEnd(Marker marker) {
                post_data.put("latitude",rent_marker.getPosition().latitude+"");
                post_data.put("longitude",rent_marker.getPosition().longitude+"");
            }

            //在Marker开始被拖拽时回调此方法， 这个Marker的位可以通过getPosition()方法获取
            //marker 被拖拽的Marker对象
            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });
    }
    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(maplocation==null)
            {
                maplocation=location;
            }
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            if(city==null)
            {
                city=location.getCity();
                init_dynamic_view();
            }
            mylocation.setText(location.getAddrStr().replace("中国",""));
            // 当不需要定位图层时关闭定位图层
            //mBaiduMap.setMyLocationEnabled(false);
            if(!is_load)
            {
                set_now_location();
                is_load=true;
            }
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();

                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();

                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(MainActivity.this, "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(MainActivity.this, "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(MainActivity.this, "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(this);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        buttons = (Button) findViewById(R.id.buttons);
        buttons.setOnClickListener(this);
        mylocation = (EditText) findViewById(R.id.mylocation);
        where_i_go = (EditText) findViewById(R.id.where_i_go);
        rent_location=(EditText)findViewById(R.id.rent_detail_address);
        show=(LinearLayout)findViewById(R.id.go_now);
        rent_city=(EditText)findViewById(R.id.rent_city);
        showresult=where_i_go;
        init_adpter();
        http=new http(msgHandler);
        console=(LinearLayout)findViewById(R.id.console);
        op_borad=(LinearLayout)findViewById(R.id.op_borad);
        op_show=(Button)findViewById(R.id.show_op);
    }
    public void init_dynamic_view(){
        View root = this.getWindow().getDecorView();
        map_operate.suggest_view_init(adapter,list,city,where_i_go,root);
        //map_operate.suggest_view_init(adapter,list,city,rent_location,root);
        map_operate.suggest_view_init(adapter,list,city,rent_city,root);
    }
    public void init_adpter(){
        int[] to = {R.id.text_list};
        String[] from = {"data"};
        to_list = (ListView) findViewById(R.id.to_list);
        adapter = new SimpleAdapter(this, list,
                R.layout.list_item, from, to);
        to_list.setAdapter(adapter);
        to_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Map<String, Object> map = list.get(arg2);
                String data=map.get("data").toString();
                showresult.setText(data);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if(mBaiduMap!=null){
            mMapView.onResume();
        }
    }
    private void go_now(){
        EditText editText=(EditText)findViewById(R.id.where_i_go);
        if(editText.getText().length()!=0)
        {
            HashMap map=new HashMap();
            map.put("type","get_end_point");
            String url= String.format("http://39.108.236.127/php/public/index.php/map/query_coordinate?adress=%s&is_detail=is",editText.getText());
            http.get(url,map);
        }
    }
    private final Handler msgHandler = new Handler(){
        public void handleMessage(Message msg) {
            HashMap callback;
            Gson gson=new Gson();
            callback=(HashMap) msg.obj;
            switch (msg.what){
                case 1://get
                    callback=(HashMap)msg.obj;
                    switch ((String)callback.get("type")){
                        case "choose_rent_location":
                            HashMap map= gson.fromJson((String)callback.get("data"), HashMap.class);
                            if(rent_marker!=null)
                            {
                                rent_marker.remove();
                                rent_marker=null;
                            }
                            rent_marker=map_operate.mark((Double)map.get("lat"),(Double) map.get("lng"),true,true);
                            post_data.put("latitude",String.valueOf(rent_marker.getPosition().latitude));
                            post_data.put("longitude",String.valueOf(rent_marker.getPosition().longitude));
                            Toast.makeText(MainActivity.this,"定位可能不精确，拖动试试！",Toast.LENGTH_LONG).show();
                            break;
                        case "get_code_cookies":
                            //HashMap map1= gson1.fromJson((String)callback.get("response"), HashMap.class);
                            Toast.makeText(MainActivity.this,callback.get("data")+"",Toast.LENGTH_LONG).show();
                            if(callback.get("stream_type").equals("png"))
                            {
                                try {
                                    Bitmap bitmap=(Bitmap) callback.get("img");
                                    code.setImageBitmap(bitmap);
                                }
                                catch (Exception E)
                                {

                                }

                            }
                            break;
                        case "get_park_location":
                            ArrayList location=new ArrayList();
                            ArrayList list=file.json_list(callback.get("data")+"");
                            for(int i=0;i<list.size();i++)
                            {
                                HashMap position=(HashMap)list.get(i);
                                String[] info=String.valueOf(position.get("latitude_longitude")).split(",");
                                map_operate.mark(Double.valueOf(info[0]),Double.valueOf(info[1]),false,false);
                            }
                            break;
                        case "set_img":
                            Toast.makeText(MainActivity.this,"show",Toast.LENGTH_LONG).show();
                            Bitmap bitmap=file.get_cycle((Bitmap)callback.get("img"));
                            head_view.setImageBitmap(bitmap);
                            break;
                        case "get_end_point":
                            HashMap map1= gson.fromJson((String)callback.get("data"), HashMap.class);
                            Marker end_point_marker=map_operate.mark_end_point((Double)map1.get("lat"),(Double) map1.get("lng"),true,true);
                            HashMap map2=new HashMap();
                            map2.put("type","get_park_location");
                            http.get(String.format("http://39.108.236.127/php/public/index.php/map/user_get_park?privince=%s&city=%s",map1.get("privince")+"",map1.get("city")+""),map2);
                            break;
                    }
                    break;
                case 2:
                    showresult=findViewById(msg.getData().getInt("id"));
                    break;
                case 3:
                    callback=(HashMap)msg.obj;
                    switch (String.valueOf(callback.get("type")))
                    {
                    case "login":
                        HashMap data = gson.fromJson(callback.get("data") + "", HashMap.class);
                        data.put("name",user_name);
                        if (data.get("code").equals("200")) {
                            Toast.makeText(MainActivity.this, "欢迎回来", Toast.LENGTH_LONG).show();
                            try {
                                file.writeFileSdcardFile(work_path + "user.txt", gson.toJson(data).replace("\\u0026", "&").replace("\\u003d", "="));
                            } catch (Exception E) {

                            }
//                        SQLiteDatabase db=sqlite.getWritableDatabase();
//                        ContentValues values=new ContentValues();
//                        values.put("name",user_name);
//                        values.put("email",data.get("email")+"");
//                        values.put("head_img",data.get("head_img")+"");
//                        db.insert("user",null,values);
//                        Cursor cursor=db.query("user",null,null,null,null,null,null);
//                        if(cursor.moveToFirst())
//                        {
//                            do{
//                                Log.i("data",cursor.getString((cursor.getColumnIndex("name"))));
//                                Log.i("data",cursor.getString((cursor.getColumnIndex("email"))));
//                                Log.i("data",cursor.getString((cursor.getColumnIndex("head_img"))));
//                            }
//                            while(cursor.moveToNext());
//                        }
                            nowdialog.dismiss();
                        } else {
                            if (data.get("data").equals("code_error")) {
                                Toast.makeText(MainActivity.this, "验证码错误", Toast.LENGTH_LONG).show();
                            }
                            if (data.get("data").equals("password_error")) {
                                Toast.makeText(MainActivity.this, "密码码错误或用户输入错误", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case "update_message":
                            Toast.makeText(MainActivity.this,callback.get("data")+"",Toast.LENGTH_LONG).show();
                            HashMap map=gson.fromJson(callback.get("data")+"",HashMap.class);
                            if(map.get("code").equals("405"))
                            {
                                Toast.makeText(MainActivity.this,"登录失效",Toast.LENGTH_LONG).show();
                                relogin();
                            }
                    }
                    break;
            }
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        if(mBaiduMap!=null) {
            mMapView.onPause();
        }
    }
    private void showDialog(){
        final View view = LayoutInflater.from(this).inflate(R.layout.showdialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        final Button btn_cancel_high_opion = view.findViewById(R.id.btn_cancel_high_opion);
        final Button btn_agree_high_opion = view.findViewById(R.id.btn_agree_high_opion);
        final EditText cimmit=view.findViewById(R.id.rent_detail_commit);
        final EditText tele=view.findViewById(R.id.tele);
        final EditText contacts=view.findViewById(R.id.contacts);
        final ImageView imageView1=view.findViewById(R.id.view1);
        final ImageView imageView2=view.findViewById(R.id.view2);
        final ImageView imageView3=view.findViewById(R.id.view3);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView=imageView1;
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
                image_name="img1";
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView=imageView2;
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
                image_name="img2";
            }
        });
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView=imageView3;
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
                image_name="img3";
            }
        });
        cimmit.setFilters(new InputFilter[]{ new filter(200,"max")});
        tele.setFilters(new InputFilter[]{ new filter(11,"max")});
        btn_cancel_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_cancel_high_opion.getText().equals("上一步"))
                {
                    LinearLayout image_view=view.findViewById(R.id.image_view);
                    image_view.setVisibility(View.GONE);
                    image_view=view.findViewById(R.id.base_message);
                    image_view.setVisibility(View.VISIBLE);
                    btn_agree_high_opion.setText("下一步");
                    btn_cancel_high_opion.setText("取消");
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        btn_agree_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //... To-d
                if(cimmit.getText().length()<10&&tele.getText().length()<8&&contacts.getText().length()<2)
                {
                    Toast.makeText(MainActivity.this,"输入不规范",Toast.LENGTH_LONG).show();
                    return;
                }
                if(btn_agree_high_opion.getText().equals("下一步"))
                {
                    LinearLayout image_view=view.findViewById(R.id.base_message);
                    image_view.setVisibility(View.GONE);
                    image_view=view.findViewById(R.id.image_view);
                    image_view.setVisibility(View.VISIBLE);
                    btn_cancel_high_opion.setText("上一步");
                    btn_agree_high_opion.setText("上传");
                    post_data.put("tele",String.valueOf(tele.getText()));
                    post_data.put("contacts",String.valueOf(contacts.getText()));
                    post_data.put("owner_info",String.valueOf(cimmit.getText()));
                }
                else {
                    if(String.valueOf(post_data.get("img1")).equals("")||String.valueOf(post_data.get("img1")).equals("")||String.valueOf(post_data.get("img1")).equals(""))
                    {
                        Toast.makeText(MainActivity.this,"请上传三张图片",Toast.LENGTH_LONG).show();
                        return;
                    }
                    else {
                        HashMap callback=new HashMap();
                        callback.put("type","update_message");
                        http.post("http://39.108.236.127/php/public/index.php/map/upload_park_message",post_data,null,callback);
                        dialog.dismiss();

                    }
                }
            }
        });
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
        Window window = dialog.getWindow();
        //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.dialog_anim);
        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的7/8 注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/8*7),LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String path="";
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null,null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
            //Bitmap bitmap=file.jpeg_bitmap_compress(path,600,500);
            //imageView.setImageBitmap(bitmap);
            try {
                Boolean is_compress=false;
                double multiple=1;
                double length=file.readFileSdcardFile_Bytes(path).length;
                if((length/1000)>600)
                {
                    is_compress=true;
                    multiple=600/length*1024;
                    Log.i("le",length+"");
                }
                int l=(int)(multiple*1000*0.2);
                Bitmap bitmap=file.jpeg_bitmap_compress(path,is_compress,l,work_path);
                Log.i("le",l+"");
                imageView.setImageBitmap(bitmap);
                if(is_compress) {
                    path=work_path+ String.format("cache.%s",file.get_file_type(path));
                }
                String da = file.jpeg_base64(path,is_compress);
                Date date=new Date();
                file.writeFileSdcardFile(work_path+ date.getTime()+".txt",da);
                post_data.put(image_name,da.replace("+","*"));
            }
            catch (Exception E)
            {
                Toast.makeText(MainActivity.this,E.getMessage(),Toast.LENGTH_LONG).show();
            }
        }

    }
    public void show_login(){
        final HashMap hashMap=new HashMap();
        hashMap.put("type","get_code_cookies");
        hashMap.put("stream_type","png");
        http.get("http://39.108.236.127/php/admin/code.php?1552467982240",hashMap);
        final View view = LayoutInflater.from(this).inflate(R.layout.login,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        final Button btn_cancel_high_opion = view.findViewById(R.id.btn_cancel_high_opion);
        final Button btn_agree_high_opion = view.findViewById(R.id.btn_agree_high_opion);
        final EditText name=view.findViewById(R.id.name);
        final EditText password=view.findViewById(R.id.password);
        final EditText code_num=view.findViewById(R.id.code_num);
        final ImageView imageView1=view.findViewById(R.id.code);
        nowdialog=dialog;
        code=imageView1;
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                http.get("http://39.108.236.127/php/admin/code.php?1552467982240",hashMap);
            }
        });
        btn_cancel_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_agree_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!code_num.getText().equals("")&&!name.getText().equals("")&&!password.getText().equals(""))
                {
                    Toast.makeText(MainActivity.this,"sfdfsd",Toast.LENGTH_LONG).show();
                    HashMap post_data=new HashMap();
                    user_name=name.getText()+"";
                    post_data.put("name",name.getText());
                    post_data.put("password",password.getText());
                    post_data.put("code",code_num.getText());
                    HashMap hashMap1=new HashMap();
                    HashMap callback=new HashMap();
                    callback.put("type","login");
                    http.post("http://39.108.236.127/php/public/index.php/user/login",post_data,hashMap1,callback);
                }
            }
        });
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
        Window window = dialog.getWindow();
        //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.dialog_anim);
        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的7/8 注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/8*7),LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    public void show_user(){
        ConstraintLayout layout=(ConstraintLayout)findViewById(R.id.user);
        layout.setVisibility(View.VISIBLE);
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.enter);
        layout.startAnimation(animation);
    }
    private void op_borad(){
        if(op_borad.getVisibility()==View.GONE){
            op_borad.setVisibility(View.VISIBLE);
            animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
            op_borad.startAnimation(animation);
            op_show.setVisibility(View.GONE);
        }
        else {
            op_borad.setVisibility(View.GONE);
            op_show.setVisibility(View.VISIBLE);
            animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
            op_show.startAnimation(animation);
        }
    }
    private void relogin(){
        is_login=false;
        File f=new File(work_path+"cookies.txt");
        if(f.exists())
        {
            f.delete();
        }
        f=new File(work_path+"user.txt");
        if(f.exists())
        {
            f.delete();
        }
        user_info=null;
        if(!is_login()){
            show_login();
        }
    }
    @Override
    public void onClick(View v) {
        Handler M=new Handler();
        switch (v.getId()) {
            case R.id.bt:
                HashMap<String,String> map=new HashMap<String, String>();
//                map_operate map_operate=new map_operate(mBaiduMap);
//                map_operate.mark(29.564642,103.739047);
                //http.post("http://39.108.236.127/php/admin/code.php",map);
                //http.get("http://39.108.236.127/php/public/index.php/sodijsi");
                //http.test();
                break;
            case R.id.button:
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.buttons:
                //普通地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.rent_btn:
                show.setVisibility(View.GONE);
                show=(LinearLayout)findViewById(R.id.rent);
                show.setVisibility(View.VISIBLE);
                break;
            case R.id.go_now_btn:
                show.setVisibility(View.GONE);
                show=(LinearLayout)findViewById(R.id.go_now);
                show.setVisibility(View.VISIBLE);
                break;
            case R.id.rent_btn_post:
                is_login();
                HashMap post_data=new HashMap();
                if((rent_city.getText()+"").length()<2&&(rent_location.getText()+"").length()<2)
                {
                    Toast.makeText(MainActivity.this,"亲！关键字太少了停车帮也找不到啊!",Toast.LENGTH_LONG).show();
                    return;
                }
                post_data.put("city",city);
                HashMap callback=new HashMap();
                callback.put("type","choose_rent_location");
                String url= String.format("http://39.108.236.127/php/public/index.php/map/query_coordinate?adress=%s",rent_city.getText()+""+rent_location.getText());
                http.get(url,callback);
                console.setVisibility(View.VISIBLE);
                op_borad();
                break;
            case R.id.refresh:
                mBaiduMap.setMyLocationEnabled(true);
                animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
                buttom refresh=(buttom)findViewById(R.id.refresh);
                refresh.startAnimation(animation);
                if(rent_marker!=null){
                    rent_marker.remove();
                    rent_marker=null;
                }
                console.setVisibility(View.GONE);
                break;
            case R.id.delete:
                animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
                buttom delete=(buttom)findViewById(R.id.delete);
                delete.startAnimation(animation);
                if(rent_marker!=null){
                    rent_marker.remove();
                    rent_marker=null;
                }
                break;
            case R.id.ok:
                showDialog();
                break;
            case R.id.dis_user:
                ConstraintLayout user=(ConstraintLayout)findViewById(R.id.user);
                user.setVisibility(View.GONE);
                break;
            case R.id.show_user:
                show_user();
                break;
            case R.id.go_now_see:
                go_now();
                op_borad();
                break;
            case R.id.show_op:
                op_borad();
                break;
        }
    }
}