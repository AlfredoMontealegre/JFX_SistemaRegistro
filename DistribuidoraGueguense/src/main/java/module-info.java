module ni.uam.edu.distribuidoragueguense {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.uam.edu.distribuidoragueguense to javafx.fxml;
    exports ni.uam.edu.distribuidoragueguense;
}