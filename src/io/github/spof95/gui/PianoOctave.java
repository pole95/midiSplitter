package io.github.spof95.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PianoOctave extends AnchorPane {
    private Rectangle[] octave = new Rectangle[12];
    private Label cLabel = new Label("C0");

    private Color[] keyColors = { Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.WHITE,
            Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE };

    public PianoOctave() {
        int i = 0;
        for (Color key : keyColors) {
            Rectangle rKey = makeRect(key);
            octave[i] = rKey;
            i++;
        }
        octave[1].setX(6.0);
        octave[2].setX(7.0);
        octave[3].setX(13.0);
        octave[4].setX(14.0);
        octave[5].setX(21.0);
        octave[6].setX(27.0);
        octave[7].setX(28.0);
        octave[8].setX(34.0);
        octave[9].setX(35.0);
        octave[10].setX(41.0);
        octave[11].setX(42.0);

        for (Rectangle key : octave) {
            if (key.getFill() == Color.WHITE)
                this.getChildren().add(0, key);
            else
                this.getChildren().add(key);
        }
        cLabel.setLayoutX(-4.0);
        cLabel.setLayoutY(64);
        cLabel.setAlignment(Pos.CENTER);
        this.getChildren().add(cLabel);

    }

    public void setLabel(String labelText) {
        cLabel.setText(labelText);
    }

    public void toggle(int key, Color highlight) {
        octave[key].setFill((highlight == null) ? keyColors[key] : highlight);

    }

    public Rectangle makeRect(Color color) {
        Rectangle rect;
        if (color == Color.WHITE)
            rect = new Rectangle(8.0, 65.0, color);
        else
            rect = new Rectangle(4.0, 40.0, color);
        rect.setSmooth(false);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(1.0);
        return rect;
    }

}
