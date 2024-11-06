package org.anticorruption_application.anticorruptionapplication;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab createReportTab;

    @FXML
    private Tab processReportsTab;

    @FXML
    private Tab adminTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserSession userSession = UserSession.getInstance();

        // Показываем вкладки в зависимости от групп доступа
        createReportTab.setDisable(!userSession.hasGroup("CreateReport"));
        processReportsTab.setDisable(!userSession.hasGroup("ViewReport") && !userSession.hasGroup("AccessToAllReports"));
        adminTab.setDisable(!userSession.hasGroup("ManageUserGroups"));

        // Удаляем недоступные вкладки
        mainTabPane.getTabs().removeIf(Tab::isDisable);
    }
}