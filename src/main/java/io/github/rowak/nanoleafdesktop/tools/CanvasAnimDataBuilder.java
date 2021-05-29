package io.github.rowak.nanoleafdesktop.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.Panel;

@Deprecated
public class CanvasAnimDataBuilder {
	
    private List<Panel> panels;
    private Map<Integer, List<Frame>> frames;

    public CanvasAnimDataBuilder(Aurora controller)
            throws NanoleafException, IOException {
        panels = controller.getPanels();
        frames = new HashMap<>();
        for (Panel panel : panels)
            frames.put(panel.getId(), new ArrayList<>());
    }

    public CanvasAnimDataBuilder(List<Panel> panels) {
        this.panels = panels;
        frames = new HashMap<>();
        for (Panel panel : panels)
            frames.put(panel.getId(), new ArrayList<>());
    }

    public Map<Integer, List<Frame>> getFrames() {
        return frames;
    }

    public String build() throws NanoleafException, IOException {
        int numPanels = 0;
        for (Panel p : panels) {
            if (frames.get(p.getId()).size() > 0) {
                numPanels++;
            }
        }
        StringBuilder data = new StringBuilder();
        data.append(intToBigEndian(numPanels));
        for (int i = 0; i < panels.size(); i++) {
            Panel panel = panels.get(i);
            int numFrames = frames.get(panel.getId()).size();
            if (numFrames > 0) {
                data.append(" " + intToBigEndian(panel.getId()));

                for (int j = 0; j < numFrames; j++) {
                    Frame frame = frames.get(panel.getId()).get(j);
                    data.append(" " +
                                        frame.getRed() + " " +
                                        frame.getGreen() + " " +
                                        frame.getBlue() + " " +
                                        0 + " " +
                                        intToBigEndian(frame.getTransitionTime()));
                }
            }
        }
        return data.toString();
    }

    public CanvasAnimDataBuilder addFrameToAllPanels(Frame frame) {
        for (Panel p : panels) {
            frames.get(p.getId()).add(frame);
        }
        return this;
    }

    public CanvasAnimDataBuilder addFrame(Panel panel, Frame frame) {
        return addFrame(panel.getId(), frame);
    }

    public CanvasAnimDataBuilder addFrame(int panelId, Frame frame) {
        if (panelIdIsValid(panelId)) {
            frames.get(panelId).add(frame);
        } else {
            throw new IllegalArgumentException("Panel with id " +
                                                       panelId + " does not exist.");
        }
        return this;
    }

    public CanvasAnimDataBuilder removeFrame(Panel panel, Frame frame) {
        return removeFrame(panel.getId(), frame);
    }

    public CanvasAnimDataBuilder removeFrame(int panelId, Frame frame) {
        if (panelIdIsValid(panelId)) {
            frames.get(panelId).remove(frame);
        } else {
            throw new IllegalArgumentException("Panel with id " +
                                                       panelId + " does not exist.");
        }
        return this;
    }

    private boolean panelIdIsValid(int panelId) {
        for (Panel p : panels) {
            if (p.getId() == panelId) {
                return true;
            }
        }
        return false;
    }

    private static String intToBigEndian(int num) {
        final int BYTE_SIZE = 256;
        int times = Math.floorDiv(num, BYTE_SIZE);
        return String.format("%s %s", times, num - (BYTE_SIZE * times));
    }
}
