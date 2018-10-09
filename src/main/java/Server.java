import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.EOFException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    static final File FILES_FOLDER = new File(DisplayOld.APPLICATION_FOLDER.getAbsolutePath() + "/Files");
    static final File DATABASE_FILE = new File(DisplayOld.APPLICATION_FOLDER.getAbsolutePath() + "/MediaSharingAppDB.db");
    static final File LOG_FILE = new File(DisplayOld.APPLICATION_FOLDER.getAbsolutePath() + "/MediaSharingAppLogFile.txt");
    static final int BUFFER_SIZE = 4194304;
    public static ObservableList<ServerConnectionHandler> connectionsList = FXCollections.observableArrayList();
    public static final int PORT = 28028;
    public static final int MAX_CONNECTIONS = 500;
    public DatabaseHandler dh = new DatabaseHandler();

    public Server() {
        if (!FILES_FOLDER.exists()) {
            FILES_FOLDER.mkdirs();
        }
        if (!DATABASE_FILE.exists()) {
            DATABASE_FILE.mkdirs();
        }
        new ClientListener().start();
    }

    public class ClientListener extends Thread {
        @Override
        public void run() {
            try {
                /*System.out.println("Server> Trying to set up client on port " + PORT);
                System.setProperty("javax.net.ssl.keyStore", DisplayOld.APPLICATION_FOLDER.getAbsolutePath() + "/Server/mediasharingapp.store");//TODO
                System.setProperty("javax.net.ssl.keyStorePassword", "mediasharingapp1");
                System.out.println("Server> Set up client on port " + PORT);
                ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(PORT);*/
                ServerSocket ss = new ServerSocket(PORT);
                while (true) {
                    while (connectionsList.size() <= MAX_CONNECTIONS) {
                        System.out.println("Server> Waiting for new connection");
                        Socket s = ss.accept();
                        s.setKeepAlive(true);
                        System.out.println("Server> Connection established on " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
                        new LoginManager(s).start();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class LoginManager extends Thread {

        private Socket s;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        public LoginManager(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                objectInputStream = new ObjectInputStream(s.getInputStream());
                objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                StopClass:
                while (true) {
                    Object inputObject;
                    try {
                        while ((inputObject = objectInputStream.readObject()) == null) ;
                        if (inputObject instanceof String) {
                            String input = (String) inputObject;
                            if (input.startsWith("aur:")) {
                                System.out.println("Server> Authorising Remote User : " + input.substring(4).split(":")[0]);
                                if (authoriseUser(input.substring(4).split(":")[0], input.substring(4).split(":")[1])) {
                                    System.out.println("Server> Authorised Remote User : " + input.substring(4).split(":")[0]);
                                    objectOutputStream.writeObject("aur:y");
                                    objectOutputStream.flush();
                                    RemoteServerConnectionHandler remoteConnectionHandler = new RemoteServerConnectionHandler(s, objectInputStream, objectOutputStream, input.substring(4).split(":")[0], connectionsList, dh);
                                    Thread t = new Thread(remoteConnectionHandler);
                                    t.start();
                                    connectionsList.add(remoteConnectionHandler);
                                    break StopClass;
                                } else {
                                    System.out.println("Server> Authorising RemoteUser : " + input.substring(3).split(":")[0] + " Failed");
                                    objectOutputStream.writeObject("aur:n");
                                    objectOutputStream.flush();
                                }
                            } else if (input.startsWith("flp:")) {
                                System.out.println("Lecturer > Requested Forgot Password");
                                //dh.emailLecturerPassword(input.substring(4));
                            }
                        }
                    } catch (SocketException e) {
                        System.out.println("Server> User Disconnected");
                        this.stop();
                        //connectionsList.remove(this);//TODO
                    } catch (EOFException e) {

                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Boolean authoriseUser(String username, String password) {
        return dh.authoriseUser(username, password);
    }

    public static void main(String[] args) {
        new Server();
    }

}
