import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Display extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("MediaSharingApp " /*+ getBuild()*/);
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("MSALogo.png")));//Logo
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);
        stage.setMinWidth(1280);
        stage.setMinHeight(800);
        stage.setMaximized(true);

        /*FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("HomePane.fxml"));
        Parent home = loader.load();
        Scene scene = new Scene(home);
        //Scene scene = new Scene(contentPane);
        //scene.getStylesheets().add(getClass().getClassLoader().getResource("NomdlaEnterpriseStyle.css").toExternalForm());
        Controller controller = loader.getController();
        controller.initialisePane("Bye");*/

        Parent root = FXMLLoader.load(getClass().getResource("HomePane.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("MediaSharingAppStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
