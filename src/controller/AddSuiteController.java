package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CityLodge;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.time.LocalDate;

import model.DateTime;
import model.Room;
import model.custom.exceptions.AddRoomException;
import model.custom.exceptions.InvalidIdException;

public class AddSuiteController extends GeneralController {

    @FXML
    private CheckBox generateId;
    @FXML
    private TextField roomId;
    @FXML
    private TextField roomSummary;
    @FXML
    private DatePicker lastMaintenanceDate;

    public void generateId() {
        String generatedId = super.generateIdAutomatically(model.CityLodge.ROOM_ID_PREFIX.get(Room.SUITE_ROOM_TYPE));
        roomId.setText(generatedId);
    }

    public void addSuite(ActionEvent event) throws IOException {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            LocalDate lastMaintenace = this.lastMaintenanceDate.getValue();
            DateTime lastMaintenanceDate = super.formatLocalDateToDateTime(lastMaintenace);
            CityLodge.getInstance().addRoom(this.roomId.getText(), this.roomSummary.getText(), lastMaintenanceDate);
            super.setLoaderLocation(event, "/view/MainProgramWindow.fxml");
        } catch (InvalidIdException invIdE) {
            super.alertDialog(window, invIdE.getMessage());
        } catch (AddRoomException addRoomE) {
            super.alertDialog(window, addRoomE.getMessage());
        } catch (NullPointerException npe) {
            super.alertDialog(window, "Please fill in all the fields.");
        }
    }

    public void getRoomTypeWindow(javafx.event.ActionEvent event) throws IOException {
        super.setLoaderLocation(event, "/view/AddRoomTypeWindow.fxml");
    }
}
