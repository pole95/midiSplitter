package io.github.spof95;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class MidiSplitter implements Receiver, Transmitter {

    private Receiver receiver = null;
    private int[] channelMap = new int[88];
    private List<Integer> pressedKeys = new ArrayList<>();

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void send(MidiMessage message, long timeStamp) {
        try {
            MidiMessage shifted = shiftMessage(message);
            if (receiver != null)
                receiver.send(shifted, timeStamp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MidiMessage shiftMessage(MidiMessage message) throws InvalidMidiDataException {
        byte[] body = message.getMessage();
        byte msg = (byte) ((body[0] & 0xf0) >>> 4);
        if (0x8 <= msg && msg <= 0xE) {
            byte note = body[1];
            byte vel = body[2];
            processKeyEvent(note, vel);
            byte status = (byte) ((msg << 4) + channelMap[note - 21]);
            return new ShortMessage(status, note, vel);
        }
        return message;
    }

    private void processKeyEvent(byte note, byte vel) {
        List<Integer> old = new ArrayList<>(pressedKeys);
        if (vel > 0)
            pressedKeys.add(Integer.valueOf(Byte.toUnsignedInt(note)));
        else
            pressedKeys.remove(Integer.valueOf(Byte.toUnsignedInt(note)));

        pcs.firePropertyChange("pressedKeys", old, pressedKeys);
    }

    @Override
    public void close() {
        if (receiver != null)
            receiver.close();

    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    public void setSplits(Map<Integer, Integer> splitMap) {
        splitMap.forEach((k, v) -> {
            for (int i = k - 21; i < channelMap.length; i++)
                channelMap[i] = v;
        });
    }

    public void clearSplits() {
        Arrays.fill(channelMap, 0);
    }

    public void addObserver(PropertyChangeListener l) {
        pcs.addPropertyChangeListener("pressedKeys", l);
    }
}
