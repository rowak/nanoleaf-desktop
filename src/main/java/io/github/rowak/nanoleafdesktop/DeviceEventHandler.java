package io.github.rowak.nanoleafdesktop;

import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.event.EffectsEvent;
import io.github.rowak.nanoleafapi.event.Event;
import io.github.rowak.nanoleafapi.event.NanoleafEventListener;
import io.github.rowak.nanoleafapi.event.StateEvent;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class DeviceEventHandler implements NanoleafEventListener {
	
	private NanoleafDevice device;
	private PanelCanvas canvas;
	
	public DeviceEventHandler(NanoleafDevice device, PanelCanvas canvas) {
		this.device = device;
		this.canvas = canvas;
	}
	
	@Override
	public void onOpen() {}

	@Override
	public void onClosed() {}

	@Override
	public void onEvent(Event[] events) {
		for (Event event : events) {
			handleEvent(event);
		}
	}
	
	private void handleEvent(Event event) {
		if (event instanceof StateEvent) {
			handleStateEvent((StateEvent)event);
		}
		else if (event instanceof EffectsEvent) {
			handleEffectsEvent((EffectsEvent)event);
		}
	}
	
	private void handleStateEvent(StateEvent event) {
		switch (event.getAttribute()) {
			case StateEvent.ON_ATTRIBUTE:
				canvas.setOn((boolean)event.getValue(), device);
				break;
			case StateEvent.BRIGHTNESS_ATTRIBUTE:
				canvas.setBrightness(((int)event.getValue())/100f, device);
				break;
			default:
				canvas.checkAuroraState(device);
				break;
		}
	}
	
	private void handleEffectsEvent(EffectsEvent event) {
		switch (event.getAttribute()) {
			case EffectsEvent.SELECTED_EFFECT_ATTRIBUTE:
				canvas.setEffect((String)event.getValue(), device);
				break;
		}
	}
}
