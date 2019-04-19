package at.opendrone.opendrone.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import at.opendrone.opendrone.network.requests.ArmRequest;
import at.opendrone.opendrone.network.requests.IRequest;
import at.opendrone.opendrone.utils.OpenDroneUtils;

public class RESTManager {
    private static final String TAG = "RESTManagy";
    private RESTMessageReceiver receiver;
    private Context context;
    private RequestQueue queue;

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof NetworkError) {
                Toast.makeText(context, "No network available", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };


    public RESTManager(Context context) {
        this.context = context;
        queue = SingletonRequestQueue.getInstance(context).getRequestQueue();
        VolleyLog.DEBUG = true;
    }

    public void setMessageReceiver(RESTMessageReceiver receiver) {
        this.receiver = receiver;
    }

    public void removeMessageReceiver() {
        this.receiver = null;
    }

    public void isArmed() {
        String uri = OpenDroneUtils.API_ARM;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(uri, null, response -> {

            VolleyLog.wtf(response.toString(), "utf-8");
            GsonBuilder builder = new GsonBuilder();
            Gson mGson = builder.create();

            ArmRequest arm = mGson.fromJson(response.toString(), ArmRequest.class);
            if (receiver != null) {
                receiver.onMessageReceived(arm.isArmed());
            }

        }, errorListener) {

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };

    }

    public void call(IRequest request, String uri) {
        JSONObject jsonObject = new JSONObject(request.getParamMap());
        doCall(jsonObject, uri);
    }

    private void doCall(JSONObject jsonObject, String uri) {
        JsonObjectRequest jsonObjectRequest = createRequest(uri, jsonObject);
        queue.add(jsonObjectRequest);
    }

    private JsonObjectRequest createRequest(String uri, JSONObject jsonObject) {
        return new JsonObjectRequest(uri, jsonObject, response -> {
            Log.i(TAG, response.toString());
            if (receiver != null) {
                receiver.onMessageReceived(response.toString());
            }

        }, errorListener) {

            @Override
            public int getMethod() {
                return Method.POST;
            }

            @Override
            public Priority getPriority() {
                return Priority.NORMAL;
            }
        };
    }


}
