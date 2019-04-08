package park_assistant.administrator.park_helper.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.baidu.mapapi.SDKInitializer;

import park_assistant.administrator.park_helper.R;

/**
 * Created by Administrator on 2019/3/8 0008.
 */

public class my_rent_info extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        //setContentView(R.layout.activity_main);
    }
}
