package at.opendrone.opendrone.network;

public interface RESTMessageReceiver<T> {
    public void onMessageReceived(T msg);
}
