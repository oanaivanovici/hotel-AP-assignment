package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ConnectDatabase;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ConnectDatabase.getInstance().connectToDatabase();
        ConnectDatabase.getInstance().createTables();

        Parent root = FXMLLoader.load(getClass().getResource("MainProgramWindow.fxml"));
        primaryStage.setTitle("City Lodge Hotel");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
