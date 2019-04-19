package at.opendrone.opendrone.network.requests;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class GoHomeRequest implements IRequest {
    @SerializedName("goHome")
    private boolean goHome = false;

    public GoHomeRequest(boolean goHome) {
        this.goHome = goHome;
    }

    public boolean isGoHome() {
        return goHome;
    }

    public void setGoHome(boolean goHome) {
        this.goHome = goHome;
    }

    @Override
    public HashMap<String, String> getParamMap() {
        HashMap<String, String> params = new HashMap<>();
        params.put("goHome", String.valueOf(goHome));
        return params;
    }
}
