package at.opendrone.opendrone.network;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import at.opendrone.opendrone.MainActivity;

public class TCPHandler {
    private static final String TAG = TCPHandler.class.getSimpleName();
    private static final int TIME_BETWEEN_TRYING_S = 5;
    private static final int MAX_TRIES = 3;
    public String serverIP = "192.168.1.254"; //server IP address
    public int serverPort = 2018;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    protected boolean mRun = false;
    // used to send messages
    public PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    public Socket socket;
    public boolean failed = false;
    private int tries = 0;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPHandler(String serverIP, int serverPort, OnMessageReceived listener) {
        mMessageListener = listener;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                    failed = false;
                }else{
                    Log.i(TAG, "BUFFER IS NULL");
                    failed = true;
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        Log.i(TAG, " stop");
        mRun = false;
        Log.i(TAG, mRun+" run");
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
            Log.i(TAG, mBufferOut.checkError()+" <ERROR");
        }
        Log.i(TAG, " after if");
        mMessageListener = null;
        Log.i(TAG, (mMessageListener == null)?"null":"not null"+" after if");
        mBufferIn = null;
        Log.i(TAG, (mBufferIn == null)?"null":"not null"+" after if");
        mBufferOut = null;
        Log.i(TAG, (mBufferOut == null)?"null":"not null"+" after if");
        mServerMessage = null;
        Log.i(TAG, (mServerMessage == null)?"null":"not null"+" after if");
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.i("TCPClient", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, serverPort);
            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    tries = 0;
                    mServerMessage = "";
                    int c;
                    while((c = mBufferIn.read()) != '*'){
                        mServerMessage += (char)c;
                    }

                    //mServerMessage = mBufferIn.readLine();
                    Log.i("TCPClientMessage", mServerMessage);
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.i("RESPONSEFROMSERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCPERROR", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCPERROR", "C: Error", e);
            Log.i(TAG, "trying again...");
            try {
                tries++;
                if (tries < MAX_TRIES) {
                    Thread.sleep(TIME_BETWEEN_TRYING_S * 1000);
                    this.run();
                } else {
                    Log.i(TAG, "Tried " + tries + " times without success. Giving up :(");
                }
            } catch (InterruptedException e1) {
                Log.e(TAG, e.getMessage(), e);
            }
            //
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}