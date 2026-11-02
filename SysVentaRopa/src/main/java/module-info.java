module pe.edu.upeu.sysventas {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires static lombok;

    requires java.sql;
    requires java.naming;
    requires java.prefs;
    requires org.slf4j;
    requires com.zaxxer.hikari;

    requires jakarta.validation;
    requires org.jsoup;
    requires java.net.http;

    opens pe.edu.upeu.sysventas.controller to javafx.fxml;
    opens pe.edu.upeu.sysventas to javafx.fxml;
    opens pe.edu.upeu.sysventas.model;
    opens pe.edu.upeu.sysventas.dto;

    exports pe.edu.upeu.sysventas;
    exports pe.edu.upeu.sysventas.model;
}
