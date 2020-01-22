package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import model.*;
import model.custom.exceptions.MaintenanceException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RoomDetailController extends GeneralController {

    @FXML
    private ImageView roomImage;
    @FXML
    private Label roomId;
    @FXML
    private Label roomSummary;
    @FXML
    private Label roomType;
    @FXML
    private Label numberOfBeds;
    @FXML
    private Label roomStatus;
    @FXML
    private Label lastMaintenance;
    @FXML
    private ListView<HiringRecords> hiringRecordsList;

    private ObservableList<HiringRecords> observableList = FXCollections.observableArrayList();

    public static final String NO_LABEL = "N/A";

    // Get room details from database based on ID, then set the screen fields containing details and hiring records
    public void passData(String id) {
        Room roomToView = ConnectDatabase.getInstance().getRoomById(id);
        setMainDetails(roomToView);
        setHiringRecords(roomToView, new ActionEvent());
    }

    private void setMainDetails(Room roomToView) {
        roomId.setText(roomToView.get_roomID());
        roomSummary.setText(roomToView.get_summary());
        roomType.setText(roomToView.get_roomType());
        numberOfBeds.setText(String.valueOf(roomToView.get_numberOfBedrooms()));
        roomStatus.setText(roomToView.get_roomStatus());
        if ((roomToView.get_roomType()).equals(Room.SUITE_ROOM_TYPE)) {
            lastMaintenance.setText(((Suite) roomToView).getLastMaintenanceDate().toString());
        } else {
            lastMaintenance.setText(NO_LABEL);
        }
        String imageFileName = roomToView.get_roomImage();

        // Check if image exists. if not, set it to the No_image_available icon.
        boolean checkIfExists = new File("images/" + imageFileName).exists();
        String imagePath;
        if (checkIfExists) {
            imagePath = "file:images/" + imageFileName;
        } else {
            imagePath = "file:images/No_image_available.png";
        }
        Image image = new Image(imagePath);
        roomImage.setImage(image);
    }

    // Get hiring records for corresponding room from DB, then set the observable list to the list of recs
    private void setHiringRecords(Room roomToView, ActionEvent event) {
        List<HiringRecords> hiringRecords = ConnectDatabase.getInstance().getHiringRecords(roomToView.get_roomID());
        for (HiringRecords record : hiringRecords) {
            observableList.add(record);
        }
        hiringRecordsList.setItems(observableList);
    }

    public void getRentRoomWindow(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/RentRoomWindow.fxml"));
            Parent view = loader.load();
            Scene detailedScene = new Scene(view);
            RentRoomController controller = loader.getController();
            controller.passData(roomId.getText());
            window.setScene(detailedScene);
            window.show();
        } catch (IOException e) {
            super.alertDialog(window, "An input/output error has occurred");
        }
    }

    public void getReturnRoomWindow(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/ReturnRoomWindow.fxml"));
            Parent view = loader.load();
            Scene detailedScene = new Scene(view);
            ReturnRoomController controller = loader.getController();
            controller.passData(roomId.getText());
            window.setScene(detailedScene);
            window.show();
        } catch (IOException e) {
            super.alertDialog(window, "An input/output error has occurred");
        }
    }

    public void getPerformMaintenanceWindow(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            CityLodge.getInstance().performMaintenance(roomId.getText());
            roomStatus.setText(Room.MAINTENANCE_ROOM);
        } catch (MaintenanceException me) {
            super.alertDialog(window, me.getMessage());
        }
    }

    public void getCompleteMaintenanceWindow(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/CompleteMaintenanceWindow.fxml"));
            Parent view = loader.load();
            Scene detailedScene = new Scene(view);
            CompleteMaintenanceController controller = loader.getController();
            controller.passData(roomId.getText());
            window.setScene(detailedScene);
            window.show();
        } catch (IOException e) {
            super.alertDialog(window, "An input/output error has occurred");
        }
    }

    public void getMainProgramWindow(ActionEvent event) throws IOException {
        super.setLoaderLocation(event, "/view/MainProgramWindow.fxml");
    }
}
