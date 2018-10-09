import javafx.fxml.FXML;

import java.awt.*;

public class Controller {

    @FXML
    private Label homeLbl;

    public void initialisePane(String a){
        homeLbl.setText(a);
    }

}
