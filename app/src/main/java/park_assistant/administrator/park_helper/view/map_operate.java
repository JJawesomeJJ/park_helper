package park_assistant.administrator.park_helper.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import park_assistant.administrator.park_helper.R;
import park_assistant.administrator.park_helper.http.*;

/**
 * Created by Administrator on 2019/2/28 0028.
 */

public class map_operate {
    private OnGetSuggestionResultListener listener;
    private SuggestionSearch suggestionSearch;
    private BaiduMap mBaiduMap;
    public map_operate(BaiduMap mBaiduMap,Handler mhandler){
        this.mBaiduMap=mBaiduMap;
        this.handler=mhandler;
    }
    private android.os.Handler handler = null;
    public Marker mark(double lng, double lat, boolean is_move, boolean is_drag){
        LatLng point = new LatLng(lng,lat);
//构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .perspective(true)
                .title("目标停车点")
                .draggable(is_drag);
//在地图上添加Marker，并显示
        Marker marker=(Marker)mBaiduMap.addOverlay(option);
        if(is_move){
            chooseMyLocation(lng,lat);
        }
        return marker;
    }
    public Marker mark_end_point(double lng, double lat, boolean is_move, boolean is_drag){
        LatLng point = new LatLng(lng,lat);
//构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_en);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .perspective(true)
                .title("目标停车点")
                .draggable(is_drag);
//在地图上添加Marker，并显示
        Marker marker=(Marker)mBaiduMap.addOverlay(option);
        if(is_move){
            chooseMyLocation(lng,lat);
        }
        return marker;
    }
    public void suggest(final SimpleAdapter adapter,final List<Map<String,Object>> list){
        listener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                    return;
                }
                list.clear();
                for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
                    HashMap<String,Object> map=new HashMap<String,Object>();
                    map.put("data",info.key);
                    list.add(map);
                }
                adapter.notifyDataSetChanged();
            }
        };
        suggestionSearch= SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(listener);
    }
    public void suggest_detory(final SimpleAdapter adapter,final List<Map<String,Object>> list){
        list.clear();
        adapter.notifyDataSetChanged();
        suggestionSearch.destroy();
    }
    public void suggest_view_init(final SimpleAdapter adapter, final List<Map<String,Object>> list, final String city, final EditText editText, final View root){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                suggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(city)
                        .keyword(String.valueOf(editText.getText())));
            }
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable edit) {

            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    suggest(adapter,list);
                    message(root.findFocus().getId());
                }
                else {
                    suggest_detory(adapter,list);
                }
            }
        });
    }
    public void message(int data) {
        Bundle bundle = new Bundle();
        bundle.putInt("id",data);
        Message msg = handler.obtainMessage();
        msg.setData(bundle);
        msg.what =2;
        handler.sendMessage(msg);
    }
    private void chooseMyLocation(double la,double lo) {
        // 开启定位功能
        mBaiduMap.setMyLocationEnabled(true);
        // 构造定位数据
        MyLocationData locationData = new MyLocationData.Builder()
                .latitude(la)
                .longitude(lo)
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locationData);
        // 自定以图表
        BitmapDescriptor marker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        // 设置定位图层的配置，设置图标跟随状态（图标一直在地图中心）
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, false, null);
        mBaiduMap.setMyLocationConfigeration(config);
        mBaiduMap.setMyLocationEnabled(false);
        // 当不需要定位时，关闭定位图层
        // mBaiduMap.setMyLocationEnabled(false);
    }
}
