package at.opendrone.opendrone.network.requests;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class ArmRequest implements IRequest, Serializable {
    @SerializedName("armed")
    private boolean armed = false;

    public ArmRequest(boolean armed) {
        this.armed = armed;
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean arm) {
        this.armed = arm;
    }

    @Override
    public HashMap<String, String> getParamMap() {
        HashMap<String, String> params = new HashMap<>();
        params.put("armed", String.valueOf(armed));
        return params;
    }
}
