module com.github.sawors.demo {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires org.kordamp.ikonli.javafx;
    requires org.eclipse.jgit;
    
    opens com.github.sawors.demo to javafx.fxml;
    exports com.github.sawors.demo;
}