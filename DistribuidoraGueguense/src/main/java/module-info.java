module ni.uam.edu.distribuidoragueguense {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens ni.uam.edu.distribuidoragueguense to javafx.fxml;
    opens ni.uam.edu.distribuidoragueguense.Controllers to javafx.fxml;
    opens ni.uam.edu.distribuidoragueguense.Modelo to javafx.base;

    exports ni.uam.edu.distribuidoragueguense;
    exports ni.uam.edu.distribuidoragueguense.Controllers;
    exports ni.uam.edu.distribuidoragueguense.Modelo;
    exports ni.uam.edu.distribuidoragueguense.Dao;
    exports ni.uam.edu.distribuidoragueguense.Interfaces;
}