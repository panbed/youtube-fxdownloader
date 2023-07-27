module com.panbed.ytdownloader.ytdownloader {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.panbed.ytdownloader to javafx.fxml;
    exports com.panbed.ytdownloader;
}