module com.github.sawors.demo {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires org.eclipse.jgit;
    requires com.google.gson;
    requires org.apache.commons.io;
    
    opens com.github.sawors.demo to javafx.fxml;
    exports com.github.sawors.demo;
}