package park_assistant.administrator.park_helper.view;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.AppCompatButton;

/**包   名：com.example.demo
 * 类    名：EduSohoIconView.java
 * 描   述：
 * Copyright: Copyright (c) 2011
 * 时    间：2015-3-10	下午9:26:25
 * @version V1.0
 */
public class buttom extends AppCompatButton{

    private Context mContext;

    public buttom(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public buttom(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }
    private void initView()
    {
        Typeface iconfont = Typeface.createFromAsset(mContext.getAssets(), "iconfont/iconfont.ttf");
        setTypeface(iconfont);
    }
}