package io.github.spof95;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class MidiModel {
    private static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    private static final int MIDI_START_NOTE = 21;
    private static final int MIDI_END_NOTE = 108;
    private static final Map<String, Integer> NOTE_MAP = new LinkedHashMap<>();

    private Map<Integer, Integer> splitMap = new TreeMap<>();

    private ListProperty<String> inputDescriptions = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<String> outputDescriptions = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<String> noteList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private List<MidiDevice> inDevices = new ArrayList<>();
    private List<MidiDevice> outDevices = new ArrayList<>();

    private Receiver midiReceiver = null;
    private Transmitter midiTransmitter = null;
    private MidiSplitter splitter = new MidiSplitter();

    public MidiModel() {
        generateNoteNameMap(MIDI_START_NOTE, MIDI_END_NOTE);
    }

    private void generateNoteNameMap(int start, int end) {
        NOTE_MAP.clear();
        for (int i = start; i <= end; i++) {
            NOTE_MAP.put(NOTE_NAMES[i % 12] + Integer.toString((i / 12) - 1), i);
        }
    }

    public void clearAllSplits() {
        splitMap.clear();
        splitter.clearSplits();
    }

    public void addSplit(String note, int channel) {
        int midiNoteNumber = NOTE_MAP.get(note);
        splitMap.put(midiNoteNumber, channel);
        splitter.setSplits(splitMap);

    }

    public void changeMidiInput(Number newval) {
        MidiDevice device = inDevices.get(newval.intValue());
        try {
            device.open();
            midiTransmitter = device.getTransmitter();
        } catch (MidiUnavailableException e) {
            System.err.println("Could not Set Input: " + e);
            midiTransmitter = null;
        }
        midiTransmitter.setReceiver(splitter);
    }

    public void changeMidiOutput(Number newval) {
        MidiDevice device = outDevices.get(newval.intValue());
        try {
            device.open();
            midiReceiver = device.getReceiver();
        } catch (MidiUnavailableException e) {
            System.err.println("Could not Set Input: " + e);
            midiReceiver = null;
        }
        splitter.setReceiver(midiReceiver);

    }

    public void getAllMidiDevices(boolean forceReload) {
        if (outDevices.isEmpty() || inDevices.isEmpty() || forceReload) {
            outDevices.clear();
            inDevices.clear();
            Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
            for (int i = 0; i < midiDeviceInfo.length; i++) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
                    int maxRx = device.getMaxReceivers();
                    int maxTx = device.getMaxTransmitters();
                    if (!(device instanceof Sequencer) && !(device instanceof Synthesizer)) {
                        if (maxTx == 0)
                            outDevices.add(device);
                        if (maxRx == 0)
                            inDevices.add(device);
                    }
                } catch (MidiUnavailableException e) {
                    System.err.println("Device not available: " + e);
                }
            }
        }
    }

    public Map<Integer, Integer> splitMap() {
        return splitMap;
    }

    public List<MidiDevice> outDevices() {
        return outDevices;
    }

    public List<MidiDevice> inDevices() {
        return inDevices;
    }

    public void closeAllMidiDevices() {
        splitter.close();
        outDevices.forEach(d -> d.close());
        inDevices.forEach(d -> d.close());
    }

    public void setNoteList() {
        NOTE_MAP.forEach((k, v) -> noteList.add(k));
    }

    public void setPossibleInputs() {
        inputDescriptions.addAll(generateDeviceInfo(inDevices));
    }

    public void setPossibleOutputs() {
        outputDescriptions.addAll(generateDeviceInfo(outDevices));
    }

    public ListProperty<String> inputDescriptions() {
        return inputDescriptions;
    }

    public ListProperty<String> outputDescriptions() {
        return outputDescriptions;
    }

    public ListProperty<String> noteList() {
        return noteList;
    }

    private List<String> generateDeviceInfo(List<MidiDevice> devices) {
        List<String> deviceList = new ArrayList<>();
        for (MidiDevice device : devices)
            deviceList.add(device.getDeviceInfo().getName() + " - " + device.getDeviceInfo().getDescription());
        return deviceList;

    }
}
