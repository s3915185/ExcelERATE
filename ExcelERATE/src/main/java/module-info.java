module com.example.excelerate {
    requires javafx.controls;
    requires javafx.fxml;
    requires poi;
    requires poi.ooxml;
    requires java.desktop;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.support;

    opens com.example.excelerate to javafx.fxml;
    exports com.example.excelerate;
}
