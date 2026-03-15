module com.example.authentification_front {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.authentification_front to javafx.fxml;
    opens com.example.authentification_front.client to com.google.gson;

    exports com.example.authentification_front;
}