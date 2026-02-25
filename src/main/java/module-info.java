module edu.dccc.taskmanagerapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.dccc.taskmanagerapp to javafx.fxml;
    exports edu.dccc.taskmanagerapp;
    exports edu.dccc.utils;
    opens edu.dccc.utils to javafx.fxml;
}