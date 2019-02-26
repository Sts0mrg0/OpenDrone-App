package at.opendrone.opendrone.raspistats;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import at.opendrone.opendrone.utils.OpenDroneUtils;
import at.opendrone.opendrone.R;

public class HeightStat implements RaspiStat {
    private static final int CODE = OpenDroneUtils.CODE_HEIGHT;
    private String[] data;
    private Context context;
    private View view;

    public HeightStat(String[] data, Context context, View view) {
        this.data = data;
        this.view = view;
        this.context = context;
    }

    @Override
    public void doStuff() {
        try{
            TextView txtView = view.findViewById(R.id.txt_MF_Height);
            String txt = context.getString(R.string.manual_flight_TxtView_Height);
            txtView.setText(String.format(txt, data[0]));
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    @Override
    public int getCode() {
        return CODE;
    }
}
