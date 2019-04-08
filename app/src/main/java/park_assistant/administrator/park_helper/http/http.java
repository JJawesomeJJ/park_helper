package park_assistant.administrator.park_helper.http;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.baidu.mapapi.http.HttpClient;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.in;
import park_assistant.administrator.park_helper.file.file;

/**
 * Created by Administrator on // .
 */

public class http {
    private String work_path="/storage/emulated/0/park_helper/";
    private Handler handler = null;
    private HttpURLConnection connection;
    private String acceptData = "";

    public http(Handler mhandler) {
        handler = mhandler;
    }

    public void get(final String url_path, final HashMap callback) {
        new Thread(new Runnable() {
            public void run() {
                String data = "";
                String response="";
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(url_path);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(80000);
                    connection.setReadTimeout(80000);
                    set_cookies(connection);
                    InputStream inputStream = connection.getInputStream();
                    if(callback.get("stream_type")!=null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        callback.put("img",bitmap);
                        FileOutputStream b = new FileOutputStream(work_path + "code.png");
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
                    }
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    response=getResponseHeader(connection);
                    callback.put("header",getResponseHeader(connection));
                    while ((line = bufferedReader.readLine()) != null) {
                        data += line;
                    }
                } catch (Exception E) {
                    data = E.getMessage();
                } finally {
                    callback.put("data",data);
                    callback.put("response",response);
                    message(callback,"get");
                    connection.disconnect();
                }
            }
        }).start();
    }
    public void message(HashMap datamap,String type) {
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("result",(Parcelable)datamap);
        Message msg = handler.obtainMessage();
        //msg.setData(bundle);
        msg.obj=datamap;
        if(type.equals("get")) {
            msg.what = 1;
        }
        else {
            msg.what=3;
        }
        handler.sendMessage(msg);
    }

    public void post(final String url_path, final HashMap<String, String> post_data_list, final HashMap <String,String> header,final HashMap callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data="";
                try {
                    URL url=new URL(url_path);
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    set_cookies(connection);
                    if(post_data_list!=null&&post_data_list.size()>0) {
                        String post_data ="";
                        for (Map.Entry<String, String> entry : post_data_list.entrySet()) {
                            post_data += String.valueOf(entry.getKey()) + "=" + String.valueOf(entry.getValue()) + "&";
                        }
                        post_data = post_data.substring(0, post_data.length() - 1);
                        OutputStream outputStream=connection.getOutputStream();
                        outputStream.write(post_data.getBytes());
                    }
                    if(header!=null&&header.size()>0)
                    {
                        for (Map.Entry<String, String> entry : post_data_list.entrySet()) {
                            connection.setRequestProperty(entry.getKey(),entry.getValue());
                        }
                    }
                    connection.setConnectTimeout(80000);
                    connection.setReadTimeout(80000);
                    InputStream inputStream = connection.getInputStream();
                    callback.put("header",getResponseHeader(connection));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        data += line;
                    }
                } catch (Exception E) {
                    data = E.getMessage();
                } finally {
                    callback.put("data",data);
                   message(callback,"post");
                    connection.disconnect();
                }
            }
        }).start();
    }
    private boolean store_cookies(String data){
        String[] cookies_list=data.split("=");
        String name=cookies_list[0];
        String[] parameter=cookies_list[1].split(";");
        return true;
    }
    private String getResponseHeader(HttpURLConnection conn)throws Exception {
        Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
        int size = responseHeaderMap.size();
        StringBuilder sbResponseHeader = new StringBuilder();
        String url=String.valueOf(conn.getURL()).split("/")[2];
        for(int i = 0; i < size; i++) {
            String responseHeaderKey = conn.getHeaderFieldKey(i);
            String responseHeaderValue = conn.getHeaderField(i);
            if(responseHeaderKey==null)
            {
                continue;
            }
            if(responseHeaderKey.equals("Set-Cookie")) {
                Gson gson=new Gson();
                String filename=work_path+"cookies.txt";
                HashMap map=null;
                File f=new File(filename);
                String[] list=responseHeaderValue.split(";");
                if(!f.exists())
                {
                    f.createNewFile();
                }
                String data=file.readFileSdcardFile(filename);
                if(data.length()>0)
                {
                    try {
                        map=gson.fromJson(data,HashMap.class);
                    }
                    catch (Exception E)
                    {
                        map=new HashMap();
                    }
                }
                else {
                    map=new HashMap();
                }
                if(map.get(url)==null)
                {
                    map.put(url,list[0].split(";")[0]+";");
                }
                else {
                    String cookies=String.valueOf(map.get(url));
                    if(cookies.indexOf(list[0].split(";")[0].split("=")[0])!=-1)
                    {
                        String reg=list[0].split(";")[0].split("=")[0]+"(.*?)"+";";
                        cookies=cookies.replaceAll(reg,list[0].split(";")[0]+";");
                    }
                    else {
                        cookies=cookies+list[0].split(";")[0]+";";
                    }
                    map.put(url,cookies);
                }
                String cookies_data=gson.toJson(map);
                Log.i("ds",cookies_data);
                file.writeFileSdcardFile(filename,cookies_data.replace("null","").replace("\\u003d","="));
            }
            sbResponseHeader.append(responseHeaderKey);
            sbResponseHeader.append(":");
            sbResponseHeader.append(responseHeaderValue);
            sbResponseHeader.append("\n");
        }
        Log.i("hsow",sbResponseHeader.toString());
        return sbResponseHeader.toString();
    }
    private void set_cookies(HttpURLConnection con) throws Exception{
        Gson gson=new Gson();
        String filename=work_path+"cookies.txt";
        HashMap map=null;
        File f=new File(filename);
        if(!f.exists())
        {
            return;
        }
        String data=file.readFileSdcardFile(filename);
        map=gson.fromJson(data,HashMap.class);
        if(map.get(String.valueOf(con.getURL()).split("/")[2])!=null)
        {
            String cookies=map.get(String.valueOf(con.getURL()).split("/")[2])+"";
            con.setRequestProperty("Cookie",cookies.substring(0,cookies.length()-1));
        }
    }
}
