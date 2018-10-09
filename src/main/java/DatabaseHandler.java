import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    private Connection con;

    DatabaseHandler() {
        connectDB();
    }

    //<editor-fold desc="Database Connection">
    private void connectDB() {
        try {
            Boolean createDatabase = false;
            if (!Server.DATABASE_FILE.exists()) {
                createDatabase = true;
            }
            con = DriverManager.getConnection("jdbc:sqlite:" + Server.DATABASE_FILE.getAbsolutePath());
            if (createDatabase) {
                Statement stmt = con.createStatement();
                stmt.execute("CREATE TABLE Student (" +
                        "StudentNumber TEXT PRIMARY KEY, " +
                        "Qualification TEXT, " +
                        "FirstName TEXT, " +
                        "LastName TEXT, " +
                        "Password TEXT, " +
                        "AssignedPassword INTEGER, " +
                        "Email TEXT, " +
                        "ContactNumber TEXT);");
                System.out.println("Server> Created Database");
            }
            System.out.println("Server> Connected to database");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Server> connectDB> " + ex);
            System.exit(0);
        }
    }

    public Boolean authoriseUser(String username, String password){
        return true;
    }

    public static List<MediaFile> getFiles (String mediaType) {
        List<MediaFile> files = new ArrayList<>();

        if (mediaType.equals("Music")) {
            File filesDirectory = new File(getDirectory("Music"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Movies")){
            File filesDirectory = new File(getDirectory("Movies"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Series")){
            File filesDirectory = new File(getDirectory("Series"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Pictures")){
            File filesDirectory = new File(getDirectory("Pictures"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Videos")){
            File filesDirectory = new File(getDirectory("Videos"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Documents")){ ;
            File filesDirectory = new File(getDirectory("Documents"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        }
        return files;
    }

    public static String getDirectory (String type) {
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

}
