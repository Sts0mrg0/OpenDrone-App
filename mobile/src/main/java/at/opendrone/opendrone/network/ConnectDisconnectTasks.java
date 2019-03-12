package at.opendrone.opendrone.network;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnectDisconnectTasks {
    private TCPHandler mTCPHandler;
    private static ConnectDisconnectTasks instance;
    private static final String TAG = "ConnectDisconnecty";
    public static final String TARGET = "192.168.1.254";
    public static final int PORT = 2018;
    private boolean isArmed = false;

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
        if(mTCPHandler == null || mTCPHandler.mBufferOut == null){
            return false;
        }
        return true;
        /*if(mTCPHandler != null || mTCPHandler.mBufferOut != null){
            return mTCPHandler.socket.isConnected();
        }
        return false;*/
    }

    public boolean sendFailed(){
        return mTCPHandler.failed;
    }

    public void setFailed(){
        mTCPHandler.failed = false;
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

    public void removeMessageReceiver(){
        this.receiver = null;
    }

    public void sendMessage(String message){
        mTCPHandler.sendMessage(message);
    }

    public void setArmed(boolean armed) {
        isArmed = armed;
    }

    public boolean isArmed() {
        return isArmed;
    }

    public boolean ping(){
        runSystemCommand("ping "+TARGET);
        Log.i(TAG,"Status: "+mTCPHandler.failed);
        return mTCPHandler.failed;
    }

    public void runSystemCommand(String command) {
        Runnable stuffToDo = new Thread() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec(command);

                    BufferedReader inputStream = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));

                    String s = "";
                    // reading output stream of the command
                    if ((s = inputStream.readLine()) != null) {
                        Log.i("TAGGY","\t\t"+s);
                        if(!s.contains("Host unreachable") || !s.contains("Zielhost nicht erreichbar") || !s.contains("Request timed out")){
                            ConnectDisconnectTasks.this.mTCPHandler.failed = false;
                            Log.i("TAGGY","\tPinged");
                            return;
                        }

                    }
                    ConnectDisconnectTasks.this.mTCPHandler.failed = true;
                    Log.i("TAGGY","\tFailed");
                } catch (Exception e) {
                    ConnectDisconnectTasks.this.mTCPHandler.failed = true;
                }
            }
        };

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(stuffToDo);
        executor.shutdown(); // This does not cancel the already-scheduled task.

        try {
            future.get(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException ie) {
            this.mTCPHandler.failed = true;
            return;
        }
        catch (ExecutionException ee) {
            this.mTCPHandler.failed = true;
            return;
        }
        catch (TimeoutException te) {
            this.mTCPHandler.failed = true;
            return;
        }
        if (!executor.isTerminated())
            executor.shutdownNow(); // If you want to stop the code that hasn't finished.



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
