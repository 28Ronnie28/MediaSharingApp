import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class IPAddressDialog extends CustomDialogSkin {

    ClientConnectionHandler cConhandler = new ClientConnectionHandler();

    public IPAddressDialog(Window parent) {
        initOwner(parent);
        VBox innerPane = new VBox();
        Text connectHeading = new Text("Connect to Server");
        connectHeading.getStyleClass().add("heading-text");
        TextField ipAddressField = new TextField("127.0.0.1");
        ipAddressField.setPromptText("IP Address: ex. 127.0.0.1");
        ipAddressField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 5, 0.0, 0, 1);" +
                                "-fx-border-color: black;" +
                                "-fx-border-width: 2;");
        Button connectButton = new Button("Connect");
        connectButton.getStyleClass().add("dialog-button");
        connectButton.setOnAction(e -> {
                if (!ipAddressField.getText().isEmpty()) {
                        if (ipAddressField.getText().length() >= 5) {
                            if (ipAddressField.getText().matches("[0-9.]*")) {//Do dots
                                    cConhandler.connect(ipAddressField.getText());
                                    closeAnimation();
                            } else {
                                UserNotification.showErrorMessage("Change Password", "Only numbers and dots are accepted (letters and numbers)");
                            }
                        } else {
                            UserNotification.showErrorMessage("Change Password", "IP Address must be at least 8 characters long");
                        }
                } else {
                    UserNotification.showErrorMessage("Change Password", "Field can't be empty");
                }
           
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("dialog-button");
        cancelButton.setOnAction(e -> {
            cConhandler = null;
            closeAnimation();
        });
        HBox buttonPane = new HBox(connectButton, cancelButton);
        innerPane.getChildren().addAll(connectHeading, ipAddressField, buttonPane);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(25);
        innerPane.setPadding(new Insets(20, 50, 20, 50));
        innerPane.setSpacing(20);
        innerPane.setMinWidth(600);
        innerPane.setMaxWidth(600);
        innerPane.setAlignment(Pos.CENTER);
        innerPane.setStyle("-fx-background-color: white;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;");
        VBox contentPane = new VBox(innerPane);
        contentPane.setAlignment(Pos.CENTER);
        setWidth(600);
        getDialogPane().setContent(contentPane);
        showDialog();
    }

    public ClientConnectionHandler getClientConnectionHandler() {
        return cConhandler;
    }

}
