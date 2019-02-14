package at.opendrone.opendrone.network;

public interface TCPMessageReceiver {
    void onMessageReceived(String... values);
}
