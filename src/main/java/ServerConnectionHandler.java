import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionHandler {

    public volatile BooleanProperty running = new SimpleBooleanProperty(true);
    Socket socket;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    ObservableList<ServerConnectionHandler> connectionsList;
    DatabaseHandler dh;

    public ServerConnectionHandler(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ObservableList<ServerConnectionHandler> connectionsList, DatabaseHandler dh) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        this.connectionsList = connectionsList;
        this.dh = dh;
    }

    public void terminateConnection() {
        try {
            running.set(false);
            socket.close();
            connectionsList.remove(this);
            System.out.println("Num connections: " + connectionsList.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object getReply() {
        try {
            Object input = null;
            while (running.get() && (input = objectInputStream.readObject()) == null) ;
            return input;
        } catch (Exception ex) {
            terminateConnection();
            ex.printStackTrace();
        }
        return null;
    }

}
