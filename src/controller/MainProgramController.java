package controller;


import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import model.*;
import javafx.fxml.FXML;
import javafx.fxml.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainProgramController extends GeneralController implements Initializable {

    @FXML
    private ListView<Room> roomList;

    private ObservableList<Room> observableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Room> hotelRooms = ConnectDatabase.getInstance().getAllRooms();
        for (Room room : hotelRooms) {
            observableList.add(room);
            CityLodge.getInstance().getAllRooms().add(room);
        }
        roomList.setItems(observableList);
    }

    /*
     * When the user clicks on a certain item in the room list, this method sets the scene to the RoomDetailWindow
     * and passes the information of the selected item in roomList to the controller RoomDetailController,
     * which will then take care of displaying the details of the room.
     * Source: https://github.com/JaretWright/GUIDemo/blob/master/src/guidemo/ExampleOfTableViewController.java
     * https://github.com/JaretWright/GUIDemo/blob/master/src/guidemo/PersonViewController.java
     * */
    public void clickOnItem(MouseEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/RoomDetailWindow.fxml"));
            Parent view = loader.load();
            Scene detailedScene = new Scene(view);
            RoomDetailController controller = loader.getController();
            controller.passData(roomList.getSelectionModel().getSelectedItem().get_roomID());
            window.setScene(detailedScene);
            window.show();
        } catch (Exception e) {
            // No need for exception handling if user clicks outside box
        }

    }

    public void getAddRoomTypeScreen(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            super.setLoaderLocation(event, "/view/AddRoomTypeWindow.fxml");
        } catch (IOException ioE) {
            super.alertDialog(window, ioE.getMessage());
        }
    }

    public void generateData(ActionEvent event) {
        String generateDataTitle = "Generate Data Location";
        super.generateDataDialog(event, generateDataTitle);
    }

    public void getImportRoomDialog(ActionEvent event) {
        String importTitle = "Import Room Data";
        super.importDialog(event, importTitle);
    }

    public void getExportRoomScreen(ActionEvent event) {
        String exportTitle = "Export Room Data";
        super.exportDialog(event, exportTitle);
    }

    public void quitProgram(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
