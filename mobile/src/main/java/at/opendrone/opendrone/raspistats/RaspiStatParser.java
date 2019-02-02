package at.opendrone.opendrone.raspistats;

import android.content.Context;
import android.util.Log;
import android.view.View;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

public class RaspiStatParser {
    private static final String TAG = "RaspiParser";
    private Context context;
    private View view;

    private List<Class<? extends RaspiStat>> statClasses = new LinkedList<>();

    public RaspiStatParser(View view, Context context) {
        this.context = context;
        this.view = view;
        getAllInterfaces();
    }

    public RaspiStat parse(String rawMessage) {
        Log.i(TAG, "GOT: "+rawMessage);
        String[] dataAr = rawMessage.split(";");
        try {
            int code = Integer.parseInt(dataAr[0]);
            Log.i(TAG, "Code: "+code);
            String[] values = new String[dataAr.length - 1];
            System.arraycopy(dataAr, 1, values, 0, values.length);
            Log.i(TAG, "ValueLength: "+values.length);
            return getCorrectStat(code, values);
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
            return new EmptyStat(new String[]{"Error parsing"}, context, view);
        }
    }

    private RaspiStat getCorrectStat(int code, String[] dataAr) throws Exception {
        RaspiStat statObj = new EmptyStat(new String[]{"Did not find key"}, context, view);
        for (Class<? extends RaspiStat> statClass : statClasses) {
            Constructor<?> constructor = statClass.getConstructor(String[].class, Context.class, View.class);
            statObj = (RaspiStat) constructor.newInstance(dataAr, context, view);
            if(statObj.getCode() == code){
                Log.i(TAG, statObj.getCode() + " StatCode");
                return statObj;
            }
        }
        return statObj;
    }

    private void getAllInterfaces(){
        statClasses.add(AirTempStat.class);
        statClasses.add(ControllerTempStat.class);
        statClasses.add(EmptyStat.class);
        statClasses.add(ErrorStat.class);
        statClasses.add(HeightStat.class);
        statClasses.add(PositionStat.class);
        statClasses.add(StatusStat.class);
        statClasses.add(VelocityStat.class);
    }


}
