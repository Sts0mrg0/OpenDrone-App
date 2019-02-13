package at.opendrone.opendrone.network;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectDisconnectTasks {
    private TCPHandler mTCPHandler;
    private static ConnectDisconnectTasks instance;
    private static final String TAG = "ConnectDisconnecty";
    public static final String TARGET = "10.0.0.15";//192.168.1.254
    public static final int PORT = 2018;

    private TCPMessageReceiver receiver;

    private ConnectDisconnectTasks(){
        connect();
    }

    public static ConnectDisconnectTasks getInstance(){
        if(instance == null){
            instance = new ConnectDisconnectTasks();
        }
        return instance;
    }

    public boolean isConnected(){
        if(mTCPHandler == null){
            return false;
        }
        return mTCPHandler.mRun;
    }

    public void connect(){
        if(mTCPHandler == null){
            new ConnectTask().execute("");
        }else if(!mTCPHandler.mRun){
            new ConnectTask().execute("");
        }

    }

    public void disconnect(){
        Log.i(TAG, "DISCCONTECTINTN1");
        if(mTCPHandler != null && mTCPHandler.mRun){
            Log.i(TAG, "DISCCONTECTINTN2");
            new DisconnectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setMessageReceiver(TCPMessageReceiver receiver){
        this.receiver = receiver;
    }

    public void sendMessage(String message){
        mTCPHandler.sendMessage(message);
    }

    /**
     * Disconnects using a background task to avoid doing long/network operations on the UI thread
     */
    @SuppressLint("StaticFieldLeak")
    private class DisconnectTask extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "ONPREEXE");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... voids) {

            Log.i(TAG, "DISCONNECT");
            mTCPHandler.stopClient();
            mTCPHandler = null;

            Log.i(TAG, (mTCPHandler == null) + "");

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {

            Log.i(TAG, "ONPOSTEXE");
            super.onPostExecute(nothing);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectTask extends AsyncTask<String, String, TCPHandler> {

        @Override
        protected TCPHandler doInBackground(String... message) {

            //we create a TCPClient object and
            mTCPHandler = new TCPHandler(TARGET, PORT, new TCPHandler.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            //setButtonText();
            mTCPHandler.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(receiver!=null){
                receiver.onMessageReceived(values);
            }
            Log.i(TAG, "RECEIVE: " + values[0]);
        }
    }
}
