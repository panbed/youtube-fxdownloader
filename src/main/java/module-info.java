module com.panbed.ytdownloader.ytdownloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.commons.io;


    opens com.panbed.ytdownloader to javafx.fxml;
    exports com.panbed.ytdownloader;
}