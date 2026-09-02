package ni.uam.edu.distribuidoragueguense;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DitribuidoraController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
