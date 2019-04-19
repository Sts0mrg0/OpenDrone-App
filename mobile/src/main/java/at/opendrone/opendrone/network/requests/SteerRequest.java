package at.opendrone.opendrone.network.requests;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;

public class SteerRequest implements Serializable, IRequest, Comparable<SteerRequest> {
    private int throttle;
    private int pitch;
    private int roll;
    private int yaw;

    public SteerRequest(Integer throttle, Integer pitch, Integer roll, Integer yaw) {
        this.throttle = throttle;
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    @Override
    public HashMap<String, String> getParamMap() {
        HashMap<String, String> params = new HashMap<>();
        params.put("throttle", throttle + "");
        params.put("pitch", pitch + "");
        params.put("roll", roll + "");
        params.put("yaw", yaw + "");
        return params;
    }

    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    @Override
    public int compareTo(@NonNull SteerRequest o) {
        if (o.getThrottle() == throttle && o.getYaw() == yaw && o.getPitch() == pitch && o.getRoll() == roll) {
            return 0;
        }
        return 1;
    }
}
