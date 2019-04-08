package park_assistant.administrator.park_helper.filter;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Administrator on 2019/3/4 0004.
 */

public class filter implements InputFilter {
    private int min, max;
    private boolean is_equal=false;
    private int equal;
    private String type;

    public filter(int max_length,String type){
        this.max=max_length;
        this.type=type;
    }
    public filter(int equal_length)
    {
        this.equal=equal_length;
        is_equal=true;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        switch (type) {
            case "equal":
                try {
                    String input = dest.toString() + source.toString();
                    if (equal(input, equal)) {
                        return null;
                    }
                } catch (Exception nfe) {

                }
                return "";
            case "max":
                try {
                    String input = dest.toString() + source.toString();
                    if (max(input,max)) {
                        return null;
                    }
                } catch (Exception nfe) {

                }
            default:
                return "";
        }
    }
    private boolean equal(String input,int length){
        if(input.length()<=length)
        {
            return true;
        }
        return false;
    }
    private boolean max(String input,int max){
        if(input.length()<=max)
        {
            return true;
        }
        return false;
    }
}