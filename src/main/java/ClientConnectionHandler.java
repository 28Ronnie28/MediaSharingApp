import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;

public class ClientConnectionHandler {

    public static final int PORT = 28028;
    public volatile ObservableList<String> outputQueue = FXCollections.observableArrayList();
    public volatile ObservableList<Object> inputQueue = FXCollections.observableArrayList();
    public volatile ObservableList<MediaFile> files = FXCollections.observableArrayList();
    public String connectionType = "Local";
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Boolean logOut = false;
    private String connectedIP = "";
    public static String mediaType = "None";

    public ClientConnectionHandler() {

    }

    //<editor-fold desc="Connection">
    public void connect(String ipAddress) {
        if (!connectIP(ipAddress)) {
                UserNotification.showErrorMessage("Connection Error", "Failed to connect to MediaSharingApp Servers! (" + ipAddress + ")\nPlease check your network connection and try again!");
                System.out.println("Exiting..");
                System.exit(0);
        }
        new InputProcessor().start();
        new OutputProcessor().start();
    }

    private Boolean connectIP(String ipAddress) {
        System.out.println("Trying to connect to local server... (" + ipAddress + ")");
        try {
            /*System.setProperty("javax.net.ssl.trustStore", DisplayOld.APPLICATION_FOLDER + "/Client/mediasharingapp.store");
            socket = SSLSocketFactory.getDefault().createSocket(ipAddress, PORT);*/
            socket = new Socket(ipAddress, PORT);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            connectedIP = ipAddress;
            System.out.println("Socket is connected");
            return true;
        } catch (Exception ex) {
            System.out.println("Could not connect to local server");
            ex.printStackTrace();
        }
        return false;
    }
    //</editor-fold>

    public String getConnectionType() {
        return connectionType;
    }

    //<editor-fold desc="Commands">
    public Boolean authorise(String studentNumber, String password) {//do Commands
            outputQueue.add("aur:" + studentNumber + ":" + password);
            return getStringReply("aur:");
    }

    public void forgotPassword(String email) {
        outputQueue.add("fop:" + email);
    }

    public void getFiles (String type) {
        outputQueue.add("gtf:" + type);
    }

    public void deleteFile(int classID, String fileName) {
        new File(DisplayOld.LOCAL_CACHE + "/" + classID + "/" + fileName).delete();
    }

    public void logOut() {
        sendData("lgt:");
        logOut = true;
    }

    public void sendData(String data) {
        try {
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object getReply() {
        try {
            Object input;
            while ((input = objectInputStream.readObject()) == null) ;
            return input;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (!logOut) {
                System.exit(0);
            }
        }
        return null;
    }

    public String getConnectedIP() {
        return connectedIP;
    }

    public Boolean getStringReply(String startsWith) {
        Boolean result;
        Object objectToRemove;
        ReturnResult:
        while (true) {
            for (int i = 0; i < inputQueue.size(); i++) {
                Object object = inputQueue.get(i);
                if (object instanceof String) {
                    String in = (String) object;
                    if (in.startsWith(startsWith)) {
                        objectToRemove = object;
                        result = in.charAt(startsWith.length()) == 'y';
                        break ReturnResult;
                    }
                }
            }
        }
        inputQueue.remove(objectToRemove);
        return result;
    }

    public List<String> listFiles(String directoryName, List<String> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();;
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.getName());
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), files);
            }
        }
        return files;
    }

    public String getDirectory (String type) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(DisplayOld.APPLICATION_FOLDER + "/Settings.txt"));
            String line;
            while ((line = bf.readLine()) != null){
                if(line.startsWith(type + " Directory")){
                    return line.substring((type.length() + 12), line.length());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private class InputProcessor extends Thread {
        public void run() {
            while (!logOut) {
                Object input;
                if ((input = getReply()) != null) {
                    /*if (input instanceof Student) {
                        student.setStudent((Student) input);
                        updateSavedFiles();
                        student.update();
                        System.out.println("Updated Student");
                    } else*/ if (input instanceof List<?>) {
                        List list = (List) input;
                        if (!list.isEmpty() && list.get(0) instanceof MediaFile) {
                            files.clear();
                            if (!((MediaFile) list.get(0)).getFileName().equals("NoFiles")) {
                                files.addAll(list);
                            }
                            System.out.println("Updated Files");
                            DisplayOld.setOnlineFilesList(files);
                        } /*else if (!list.isEmpty() && list.get(0) instanceof Notification) {
                            notifications.clear();
                            System.out.println("hello? " + ((Notification) list.get(0)).getHeading());
                            if (!((Notification) list.get(0)).getHeading().equals("NoNotification")) {
                                notifications.addAll(list);
                            }
                            System.out.println("Updated Notifications (" + notifications.size() + ")");
                        } else if (!list.isEmpty() && list.get(0) instanceof ContactDetails) {
                            contactDetails.clear();
                            if (!((ContactDetails) list.get(0)).getName().equals("NoContactDetails")) {
                                contactDetails.addAll(list);
                            }
                            System.out.println("Updated Contact Details");
                        } else if (!list.isEmpty() && list.get(0) instanceof ImportantDate) {
                            importantDates.clear();
                            if (!((ImportantDate) list.get(0)).getDate().equals("NoImportantDate")) {
                                importantDates.addAll(list);
                            }
                            System.out.println("Updated Important Dates");
                        } else if (!list.isEmpty() && list.get(0) instanceof AssignmentDetails) {
                            assignments.clear();
                            if (!((AssignmentDetails) list.get(0)).getAssignmentName().equals("NoAssignments")) {
                                assignments.addAll(list);
                            }
                            System.out.println("Updated Assignments");
                        }*/
                    } else {
                        inputQueue.add(input);
                    }
                }
            }
        }
    }

    private class OutputProcessor extends Thread {
        public void run() {
            while (true) {
                if (!outputQueue.isEmpty()) {
                    sendData(outputQueue.get(0));
                    outputQueue.remove(0);
                }
            }
        }
    }

    public class FileDownloader extends Thread {

        public volatile IntegerProperty size;
        public volatile DoubleProperty progress;
        MediaFile file;
        byte[] bytes;

        public FileDownloader(MediaFile file) {
            this.file = file;
            bytes = new byte[file.getFileLength()];
            size = new SimpleIntegerProperty(0);
            progress = new SimpleDoubleProperty(0);
        }

        @Override
        public void run() {
                outputQueue.add("gf:"+ file.getFileName());
                Done:
                while (true) {
                    FilePart filePartToRemove = null;
                    BreakSearch:
                    for (int i = inputQueue.size() - 1; i > -1; i--) {
                        try {
                            Object object = inputQueue.get(i);
                            if (object instanceof FilePart) {
                               FilePart filePart = (FilePart) object;
                                if (filePart.getFileName().equals(file.getFileName())) {
                                    filePartToRemove = filePart;
                                    break BreakSearch;
                                }
                            }
                        } catch (IndexOutOfBoundsException ex) {
                        }
                    }
                    if (filePartToRemove != null) {
                        for (int i = 0; i < filePartToRemove.getFileBytes().length; i++) {
                            bytes[size.get() + i] = filePartToRemove.getFileBytes()[i];
                        }
                        size.set(size.get() + filePartToRemove.getFileBytes().length);
                        progress.set(1D * size.get() / bytes.length);
                        //Platform.runLater(() -> student.update());
                        inputQueue.remove(filePartToRemove);
                    }
                    if (size.get() == file.getFileLength()) {
                        System.out.println("File successfully downloaded!");
                        File f = new File(DisplayOld.LOCAL_CACHE + "/" + file.getFileName());
                        f.getParentFile().mkdirs();
                        try {
                            Files.write(f.toPath(), bytes);
                            //updateSavedFiles();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break Done;
                    }
                }
            }
    }
}


