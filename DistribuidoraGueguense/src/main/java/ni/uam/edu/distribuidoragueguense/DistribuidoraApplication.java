package ni.uam.edu.distribuidoragueguense;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DistribuidoraApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DistribuidoraApplication.class.getResource("Distribuidora-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Distribuidora el Gueguense");
        stage.setScene(scene);
        stage.show();
    }
}
