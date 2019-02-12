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

    private ListProperty<String> inputDevices = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<String> outputDevices = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<String> noteList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private List<MidiDevice> devices = new ArrayList<>();
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
        MidiDevice device = devices.get(newval.intValue());
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
        MidiDevice device = devices.get(newval.intValue());
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
        if (devices.isEmpty() || forceReload) {
            Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
            for (int i = 0; i < midiDeviceInfo.length; i++) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
                    if (!(device instanceof Sequencer) && !(device instanceof Synthesizer))
                        devices.add(device);
                } catch (MidiUnavailableException e) {
                    System.err.println("Device not available: " + e);
                }
            }
        }
    }

    public List<MidiDevice> devices() {
        return devices;
    }

    public void closeAllMidiDevices() {
        splitter.close();
        for (MidiDevice device : devices)
            if (device.isOpen())
                device.close();
    }

    public void setNoteList() {
        NOTE_MAP.forEach((k, v) -> noteList.add(k));
    }

    public void setPossibleInputs() {
        inputDevices.addAll(generateDeviceInfo());
    }

    public void setPossibleOutputs() {
        outputDevices.addAll(generateDeviceInfo());
    }

    public ListProperty<String> inputDevices() {
        return inputDevices;
    }

    public ListProperty<String> outputDevices() {
        return outputDevices;
    }

    public ListProperty<String> noteList() {
        return noteList;
    }

    private List<String> generateDeviceInfo() {
        List<String> deviceList = new ArrayList<>();
        for (MidiDevice device : devices)
            deviceList.add(device.getDeviceInfo().getName() + " - " + device.getDeviceInfo().getDescription());
        return deviceList;

    }
}
