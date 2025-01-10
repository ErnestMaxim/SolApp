module me.solapp.core {
    requires com.dlsc.formsfx; // Assuming this is still needed
    requires javafx.graphics;
    requires javafx.controls; // Add this for controls like Button, TextField, etc.
    requires javafx.fxml;      // Add this for FXML support

    opens me.solapp.core to javafx.fxml; // Opens the package for JavaFX reflection
    opens me.solapp to javafx.fxml;     // Opens this package for JavaFX reflection

    exports me.solapp.core; // Makes this package available to other modules
    exports me.solapp;      // Makes this package available to other modules
}
