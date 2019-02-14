package io.github.spof95.gui;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import io.github.spof95.MidiModel;
import io.github.spof95.MidiSplitter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class MainController implements Observer {
    @FXML
    ComboBox<String> inputList, outputList, noteList, channelInput;
    @FXML
    Button clearButton, addButton;
    @FXML
    TableView<TableEntry> splitTable;
    @FXML
    TableColumn<TableEntry, String> keyColumn, channelColumn;
    @FXML
    HBox pianoView;

    PianoView piano = new PianoView();

    private ObservableList<TableEntry> tableEntries = FXCollections.observableArrayList();
    private ObservableList<String> channels = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8",
            "9", "10", "11", "12", "13", "14", "15", "16");
    private Color[] channelColors = { null, Color.RED, Color.YELLOW, Color.BLUE, Color.BLUEVIOLET, Color.CHARTREUSE,
            Color.CYAN, Color.CRIMSON, Color.AQUAMARINE, Color.GOLDENROD, Color.LAVENDERBLUSH, Color.GOLD,
            Color.HOTPINK, Color.TEAL, Color.VIOLET, Color.ORANGE };

    private MidiModel model;

    public void initialize() {
        pianoView.getChildren().add(piano);
    }

    public void setModel(MidiModel model) {
        this.model = model;

        inputList.itemsProperty().bind(this.model.inputDescriptions());
        inputList.setPromptText("Select Input Device");
        inputList.getSelectionModel().selectedIndexProperty()
                .addListener((arg, oldv, newv) -> model.changeMidiInput(newv));

        outputList.itemsProperty().bindBidirectional(this.model.outputDescriptions());
        outputList.setPromptText("Select Output Device");
        outputList.getSelectionModel().selectedIndexProperty()
                .addListener((arg, oldv, newv) -> model.changeMidiOutput(newv));

        this.model.setNoteList();
        noteList.itemsProperty().bind(this.model.noteList());

        splitTable.setItems(tableEntries);
        keyColumn.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("note"));
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("channel"));

        channelInput.setItems(channels);
        observe(this.model.splitter());

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int chnlIdx = channelInput.getSelectionModel().getSelectedIndex();
                int noteIdx = noteList.getSelectionModel().getSelectedIndex();
                if (chnlIdx > -1 && noteIdx > -1) {
                    String note = noteList.getSelectionModel().getSelectedItem();
                    String channel = channelInput.getSelectionModel().getSelectedItem();
                    tableEntries.add(new TableEntry(note, channel));
                    MainController.this.model.addSplit(note, chnlIdx);
                    colorKeys();
                }
            }
        });

        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MainController.this.model.clearAllSplits();
                tableEntries.clear();
                piano.clearHighlights();
            }
        });

    }

    private void colorKeys() {
        Map<Integer, Integer> map = model.splitMap();
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            for (int i = entry.getKey(); i < 108; i++)
                piano.highlight(i, channelColors[entry.getValue()]);
        }
    }

    public static class TableEntry {
        private String note;
        private String channel;

        public TableEntry(String note, String channel) {
            this.note = note;
            this.channel = channel;
        }

        public String getNote() {
            return note;
        }

        public String getChannel() {
            return channel;
        }

    }

    private void observe(Observable o) {
        o.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        List<Integer> keys = ((MidiSplitter) o).getPressedKeys();
        piano.clearHighlights();
        colorKeys();
        keys.forEach(k -> piano.highlight(k, Color.LIGHTGRAY));
    }
}