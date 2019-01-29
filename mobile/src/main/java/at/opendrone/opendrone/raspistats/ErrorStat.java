package at.opendrone.opendrone.raspistats;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import at.opendrone.opendrone.OpenDroneUtils;
import at.opendrone.opendrone.R;

public class ErrorStat implements RaspiStat {
    private static final int CODE = OpenDroneUtils.CODE_ERROR;
    private String[] data;
    private Context context;
    private View view;

    public ErrorStat(String[] data, Context context, View view) {
        this.data = data;
        this.view = view;
        this.context = context;
    }

    @Override
    public void doStuff() {
        try{
            TextView errorTxtView = view.findViewById(R.id.errorTxtView);
            errorTxtView.setText(String.format(context.getString(R.string.error_txt), Integer.parseInt(data[0])));
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
            AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
            fadeIn.setRepeatCount(Animation.INFINITE);
            fadeOut.setRepeatCount(Animation.INFINITE);
            fadeIn.setDuration(500);
            fadeOut.setDuration(1200);
            errorTxtView.setVisibility(View.VISIBLE);
            errorTxtView.startAnimation(fadeIn);
            errorTxtView.startAnimation(fadeOut);
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    @Override
    public int getCode() {
        return CODE;
    }
}
