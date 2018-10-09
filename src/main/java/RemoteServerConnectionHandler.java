import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class RemoteServerConnectionHandler extends ServerConnectionHandler implements Runnable {

    private String username;
    private volatile ObservableList<Object> outputQueue = FXCollections.observableArrayList();

    public RemoteServerConnectionHandler(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, String username, ObservableList<ServerConnectionHandler> connectionsList, DatabaseHandler dh) {
        super(socket, objectInputStream, objectOutputStream, connectionsList, dh);
        this.username = username;
    }

    public void run() {
        new InputProcessor().start();
        new OutputProcessor().start();
    }

    private class InputProcessor extends Thread {
        public void run() {
            while (running.get()) {
                Object input;
                if ((input = getReply()) != null) {
                    if (input instanceof String) {
                        String text = input.toString();
                        /*if (text.startsWith("lo:")) {
                            System.out.println("User: " + username + "> Requested ClassLecturer Online");
                            /*if (isLecturerOnline(text.substring(3))) {
                                outputQueue.add(0, "lo:y");
                            } else {
                                outputQueue.add(0, "lo:n");
                            }
                        } else*/ if (text.startsWith("cp:")) {
                            System.out.println("User: " + username + "> Requested Change Password");
                            changePassword(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                        } else if (text.startsWith("gtf:")) {
                            outputQueue.add(dh.getFiles(text.substring(4)));
                        }
                    }
                }
            }
        }
    }

    private class OutputProcessor extends Thread {
        public void run() {
            while (running.get()) {
                try {
                    if (!outputQueue.isEmpty()) {
                        Object out = outputQueue.get(0);
                        if (out instanceof List && (((List) out).isEmpty() || ((List) out).get(0) == null)) {
                            outputQueue.remove(out);
                        } else {
                            sendData(out);
                            outputQueue.remove(out);
                        }
                    }
                    Thread.sleep(20);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendData(Object data) {
        try {
            synchronized (objectOutputStream) {
                objectOutputStream.writeObject(data);
                objectOutputStream.flush();
                objectOutputStream.reset();
            }
        } catch (Exception ex) {
            terminateConnection();
            ex.printStackTrace();
        }
    }

    private void changePassword(String prevPassword, String newPassword) {
        /*String sPassword = dh.getStudentPassword(studentNumber);
        if (prevPassword.matches(sPassword) && dh.changeStudentPassword(studentNumber, newPassword)) {
            outputQueue.add(0, "cp:y");
        } else {
            outputQueue.add(0, "cp:n");
        }*/
    }

    private void changeDefaultPassword(String newPassword) {
        /*if (dh.changeStudentDefaultPassword(studentNumber, newPassword)) {
            outputQueue.add(0, "cdp:y");
        } else {
            outputQueue.add(0, "cdp:n");
        }*/
    }

    private void isDefaultPassword() {
        /*if (dh.isDefaultStudentPassword(studentNumber)) {
            outputQueue.add(0, "idp:y");
        } else {
            outputQueue.add(0, "idp:n");
        }*/
    }

    private void getFile(String classID, String fileName) {
        /*File file = new File(Server.FILES_FOLDER.getAbsolutePath() + "/" + classID + "/" + fileName);
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            int size = 0;
            while (size < fileBytes.length) {
                System.out.println(Math.min(Server.BUFFER_SIZE, fileBytes.length - size));
                outputQueue.add(new FilePart(Arrays.copyOfRange(fileBytes, size, size + Math.min(Server.BUFFER_SIZE, fileBytes.length - size)), Integer.parseInt(classID), fileName));
                size += Math.min(Server.BUFFER_SIZE, fileBytes.length - size);
                System.out.println("Student " + studentNumber + "> Successfully Downloaded File: " + fileName + " For Class: " + classID);
                System.out.println("Server> Successfully Downloaded File: " + fileName + " For Class: " + classID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

}
