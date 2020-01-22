package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CityLodge;
import model.Room;
import model.StandardRoom;
import model.custom.exceptions.AddRoomException;
import model.custom.exceptions.InvalidIdException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddStandardRoomController extends GeneralController implements Initializable {

    @FXML
    private CheckBox generateId;
    @FXML
    private ChoiceBox numberOfBeds;
    @FXML
    private TextField roomId;
    @FXML
    private TextField roomSummary;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int beds : StandardRoom.RENTAL_RATES.keySet()) {
            numberOfBeds.getItems().add(beds);
        }
    }

    public void getRoomTypeWindow(ActionEvent event) throws IOException {
        super.setLoaderLocation(event, "/view/AddRoomTypeWindow.fxml");
    }

    public void generateId() {
        String generatedId = super.generateIdAutomatically(model.CityLodge.ROOM_ID_PREFIX.get(Room.STANDARD_ROOM_TYPE));
        roomId.setText(generatedId);
    }

    public void addRoom(ActionEvent event) throws IOException {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            CityLodge.getInstance().addRoom(this.roomId.getText(), this.roomSummary.getText(),
                    Integer.parseInt(this.numberOfBeds.getValue().toString()));
            super.setLoaderLocation(event, "/view/MainProgramWindow.fxml");
        } catch (InvalidIdException invIdE) {
            super.alertDialog(window, invIdE.getMessage());
        } catch (AddRoomException addRoomE) {
            super.alertDialog(window, addRoomE.getMessage());
        } catch (NullPointerException npe) {
            super.alertDialog(window, "Please fill in all the fields.");
        }
    }
}
