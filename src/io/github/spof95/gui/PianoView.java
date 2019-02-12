package io.github.spof95.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class PianoView extends AnchorPane {
    private PianoOctave[] octaves = new PianoOctave[7];
    private List<Integer> highlighted = new ArrayList<>();

    public PianoView() {
        for (int i = 0; i < octaves.length; i++) {
            PianoOctave octave = new PianoOctave();
            octave.setLabel("C" + Integer.toString(i + 1));
            octave.setLayoutX(i * 49.0);
            this.getChildren().add(octave);
            octaves[i] = octave;
        }

    }

    public void clearHighlights() {
        for (Integer key : highlighted) {
            int midiOctave = (key / 12) - 2;
            if (midiOctave > -1)
                octaves[midiOctave].toggle(key % 12, Color.BLACK);
        }
        highlighted.clear();
    }

    public void highlight(int midiKey, Color color) {
        int midiOctave = (midiKey / 12) - 2;
        if (midiOctave > -1) {
            octaves[midiOctave].toggle(midiKey % 12, color);
            highlighted.add(midiKey);
        }

    }

}
