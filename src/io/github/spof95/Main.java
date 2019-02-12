package io.github.spof95;

import io.github.spof95.gui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 240);
            MainController controller = loader.getController();

            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
            primaryStage.setTitle("MIDI Splitter");

            MidiModel model = new MidiModel();
            model.getAllMidiDevices(true);
            model.setPossibleOutputs();
            controller.setModel(model);

            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent we) {
                    model.closeAllMidiDevices();
                    Platform.exit();
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
