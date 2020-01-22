package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CityLodge;
import model.DateTime;
import model.Room;
import model.custom.exceptions.InvalidUserInputException;
import model.custom.exceptions.RentException;

import java.time.LocalDate;

public class RentRoomController extends GeneralController {

    @FXML
    private Label roomId;
    @FXML
    private TextField customerId;
    @FXML
    private DatePicker rentDate;
    @FXML
    private TextField numberOfDays;

    public void passData(String id) {
        roomId.setText(id);
    }


    public void rentRoom(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            LocalDate rent = this.rentDate.getValue();
            DateTime rentDate = super.formatLocalDateToDateTime(rent);
            int days = Integer.parseInt(numberOfDays.getText());
            if (days < Room.MIN_RENTAL_DAYS) {
                throw new InvalidUserInputException("The number of rental days cannot be smaller than 1");
            }
            CityLodge.getInstance().rentRoom(roomId.getText(), customerId.getText(), rentDate, days);
            super.getRoomDetailsWindow(event, roomId);
        } catch (InvalidUserInputException invUserInputE) {
            super.alertDialog(window, invUserInputE.getMessage());
        } catch (RentException rentRoomE) {
            super.alertDialog(window, rentRoomE.getMessage());
        } catch (NullPointerException npe) {
            super.alertDialog(window, "Please fill in all the fields.");
        } catch (NumberFormatException nfe) {
            super.alertDialog(window, "The number of days is not valid");
        }
    }

    public void getRoomDetailsWindow(ActionEvent event) {
        super.getRoomDetailsWindow(event, roomId);
    }
}
