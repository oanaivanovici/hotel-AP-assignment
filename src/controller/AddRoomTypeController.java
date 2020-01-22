package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.StandardRoom;
import model.Suite;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddRoomTypeController extends GeneralController implements Initializable {

    @FXML
    private ChoiceBox roomType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roomType.getItems().add(StandardRoom.STANDARD_ROOM_TYPE);
        roomType.getItems().add(Suite.SUITE_ROOM_TYPE);
    }

    /* Sources:
     * https://stackoverflow.com/questions/28937392/javafx-alerts-and-their-size,
     * https://stackoverflow.com/questions/8309981/how-to-create-and-show-common-dialog-error-warning-confirmation-in-javafx-2
     * */
    public void addRoomDetails(ActionEvent event) throws IOException {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            String type = this.roomType.getValue().toString();
            if (type.equals(StandardRoom.STANDARD_ROOM_TYPE)) {
                setLoaderLocation(event, "/view/AddStandardRoomWindow.fxml");
            } else if (type.equals(Suite.SUITE_ROOM_TYPE)) {
                setLoaderLocation(event, "/view/AddSuiteWindow.fxml");
            }
        } catch (NullPointerException npe) {
            super.alertDialog(window, "Please select a room type.");
        }
    }

    public void getMainProgramWindow(ActionEvent event) throws IOException {
        super.setLoaderLocation(event, "/view/MainProgramWindow.fxml");
    }

}
