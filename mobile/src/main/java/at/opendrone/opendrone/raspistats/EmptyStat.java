package at.opendrone.opendrone.raspistats;

import android.content.Context;
import android.util.Log;
import android.view.View;

import at.opendrone.opendrone.utils.OpenDroneUtils;

public class EmptyStat implements RaspiStat {
    private static final int CODE = OpenDroneUtils.CODE_LOCAL_FAIL;
    private String[] data;
    private Context context;
    private View view;

    public EmptyStat(String[] data, Context context, View view) {
        this.data = data;
        this.view = view;
        this.context = context;
    }

    @Override
    public void doStuff() {
        Log.i(TAG, data[0]);
    }

    @Override
    public int getCode() {
        return CODE;
    }
}
