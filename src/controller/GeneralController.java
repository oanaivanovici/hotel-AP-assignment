package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CityLodge;
import model.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class GeneralController {

    // This method generates the id for standard rooms and suites automatically
    public String generateIdAutomatically(String prefix) {
        String generatedId;
        do {
            generatedId = prefix + ThreadLocalRandom.current().nextInt(100, 1000);
        } while (checkForDuplicate(generatedId));
        return generatedId;
    }

    /* This method creates an alert dialog with a given message
    Source: https://stackoverflow.com/questions/8309981/how-to-create-and-show-common-dialog-error-warning-confirmation-in-javafx-2
    */
    public void alertDialog(Stage window, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.WARNING, errorMessage, ButtonType.OK);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(window.getScene().getWidth(), window.getScene().getHeight() - 1);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }
    }

    /* This method changes the GUI view without passing information between the views
    Source: https://github.com/JaretWright/GUIDemo/blob/master/src/guidemo/FXMLDocumentController.java
    */
    public void setLoaderLocation(ActionEvent event, String path) throws IOException {
        Parent view = FXMLLoader.load(getClass().getResource(path));
        Scene scene = new Scene(view);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    /*
     * This method takes the value from the GUI date picker, formats it to dd MM yyy, then splits the formated date
     * to get the day, month, and year, which are passed to a DateTime object to create the lastMaintenanceDate.
     * Source for conversion: https://stackoverflow.com/questions/28177370/how-to-format-localdate-to-string
     */
    public DateTime formatLocalDateToDateTime(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy");
        String[] formattedString = date.format(formatter).split(" ");
        DateTime dateTime = new DateTime(Integer.parseInt(formattedString[0]), Integer.parseInt(formattedString[1]),
                Integer.parseInt(formattedString[2]));
        return dateTime;
    }

    /* This method changes the GUI view to the main Room Details View and passes the roomId to the view
    Source: https://github.com/JaretWright/GUIDemo/blob/master/src/guidemo/ExampleOfTableViewController.java
    */
    public void getRoomDetailsWindow(ActionEvent event, Label roomId) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/RoomDetailWindow.fxml"));
            Parent view = loader.load();
            Scene detailedScene = new Scene(view);
            RoomDetailController controller = loader.getController();
            controller.passData(roomId.getText());
            window.setScene(detailedScene);
            window.show();
        } catch (IOException e) {
            alertDialog(window, "An input/output error has occurred");
        }
    }

    /* This method opens the import dialog from the main menu, allowing the user to import text files
    Sources: https://docs.oracle.com/javase/8/javafx/api/javafx/stage/FileChooser.html
    https://stackoverflow.com/questions/29338352/create-filechooser-in-fxml
    */
    public void importDialog(ActionEvent event, String title) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(window);
        try {
            if (selectedFile != null) {
                CityLodge.getInstance().importFromText(selectedFile);
                setLoaderLocation(event, "/view/MainProgramWindow.fxml");
            }
        } catch (FileNotFoundException fnfE) {
            alertDialog(window, "The file you selected does not exist");
        } catch (IOException ioE) {
            alertDialog(window, "An input/output error has occurred");
        }
    }

    // This method opens the export dialog from the main menu
    public void exportDialog(ActionEvent event, String title) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            CityLodge.getInstance().exportToText(selectDirectory(title, window));
        } catch (IOException ioE) {
            alertDialog(window, "An input/output error has occurred");
        }
    }

    // This method opens the generate data dialog from the main menu
    public void generateDataDialog(ActionEvent event, String title) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            CityLodge.getInstance().generateData(selectDirectory(title, window));
        } catch (IOException e) {
            alertDialog(window, "An input/output error has occurred");
        }
    }

    /* This method allows the user to select the directory in which to dump the file with export/geneated data
    Source: http://java-buddy.blogspot.com/2013/03/javafx-simple-example-of.html
    */
    private String selectDirectory(String title, Stage window) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(window);
        return selectedDirectory.getAbsolutePath();
    }

    private boolean checkForDuplicate(String roomID) {
        return CityLodge.getInstance().getAllRooms().stream().anyMatch(r -> r.get_roomID().equals(roomID));
    }
}
