package io.github.spof95;

import java.util.Arrays;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class MidiSplitter implements Receiver, Transmitter {

    private Receiver receiver = null;
    private int[] channelMap = new int[88];

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
            byte status = (byte) ((msg << 4) + channelMap[note - 21]);
            return new ShortMessage(status, note, vel);
        }
        return message;
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

}
