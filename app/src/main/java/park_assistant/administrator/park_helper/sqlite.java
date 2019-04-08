package park_assistant.administrator.park_helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/2/27 0027.
 */

public class sqlite extends SQLiteOpenHelper {
    public static final String CREATE_BOOK = "create table user ("
            + "name varchar(20) primary key, "
            + "cookies text, "
            + "email varchar(20), "
            + "updated_at timestamp, "
            + "sex char(5), "
            + "head_img varchar(225))";
    private Context mContext;
    public sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }


    @Override

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
    }


    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}