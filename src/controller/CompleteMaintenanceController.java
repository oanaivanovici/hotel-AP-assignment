package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.CityLodge;
import model.DateTime;
import model.custom.exceptions.MaintenanceException;

import java.time.LocalDate;

public class CompleteMaintenanceController extends GeneralController {

    @FXML
    private Label roomId;
    @FXML
    private DatePicker completeMaintenanceDate;

    public void passData(String id) {
        roomId.setText(id);
    }

    public void completeMaintenance(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            LocalDate rent = this.completeMaintenanceDate.getValue();
            DateTime completeMaintenanceDate = super.formatLocalDateToDateTime(rent);
            CityLodge.getInstance().completeMaintenance(roomId.getText(), completeMaintenanceDate);
            super.getRoomDetailsWindow(event, roomId);
        } catch (MaintenanceException me) {
            super.alertDialog(window, me.getMessage());
        } catch (NullPointerException ne) {
            super.alertDialog(window, "Please fill in all the fields.");
        }
    }

    public void getRoomDetailsWindow(ActionEvent event) {
        super.getRoomDetailsWindow(event, roomId);
    }

}
