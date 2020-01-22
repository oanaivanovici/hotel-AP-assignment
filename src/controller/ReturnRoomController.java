package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.CityLodge;
import model.DateTime;
import model.custom.exceptions.ReturnException;

import java.time.LocalDate;

public class ReturnRoomController extends GeneralController {

    @FXML
    private Label roomId;
    @FXML
    private DatePicker returnDate;

    public void passData(String id) {
        roomId.setText(id);
    }

    public void returnRoom(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            LocalDate rent = this.returnDate.getValue();
            DateTime returnDate = super.formatLocalDateToDateTime(rent);
            CityLodge.getInstance().returnRoom(roomId.getText(), returnDate);
            super.getRoomDetailsWindow(event, roomId);
        } catch (ReturnException re) {
            super.alertDialog(window, re.getMessage());
        } catch (NullPointerException ne) {
            super.alertDialog(window, "Please fill in all the fields.");
        } catch (Exception e) {
            super.alertDialog(window, "An error has occurred due to the room not having any hiring records");
        }
    }

    public void getRoomDetailsWindow(ActionEvent event) {
        super.getRoomDetailsWindow(event, roomId);
    }

}
