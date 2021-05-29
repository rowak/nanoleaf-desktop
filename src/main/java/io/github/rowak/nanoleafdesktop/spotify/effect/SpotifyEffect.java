package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;

public abstract class SpotifyEffect {
	
	protected boolean requiresExtControl;
	protected int paletteIndex;
	protected SpotifyEffectType type;
	protected NanoleafGroup group;
	protected Map<NanoleafDevice, List<Panel>> panels;
	protected Color[] palette;
	protected List<UserOption> userOptions;
	
	public SpotifyEffect(SpotifyEffectType type,
			Color[] palette, NanoleafGroup group) {
		
		this.type = type;
		this.palette = palette;
		this.group = group;
		panels = new HashMap<NanoleafDevice, List<Panel>>();
		group.forEach((device) -> {
			try {
				panels.put(device, device.getPanelsRotated());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		userOptions = new ArrayList<UserOption>();
	}
	
	// Called when the effect is first created or needs to be hard-reset
	public abstract void init()
			throws NanoleafException, IOException;
	
	// Called when the progress of the local player increases
	public abstract void run(SpecificAudioAnalysis analysis)
			throws NanoleafException, IOException;
	
	// Called when the status of the track is modified (play/pause/seek)
	public abstract void reset();
	
	public SpotifyEffectType getType() {
		return type;
	}
	
	public boolean requiresExtControl() {
		return requiresExtControl;
	}
	
	public List<UserOption> getUserOptions() {
		return userOptions;
	}
	
	public void setPalette(Color[] palette)
			throws IOException, NanoleafException {
		this.palette = palette;
		paletteIndex = palette.length > 1 ? 1 : 0;
		init();
	}
	
	protected void setNextPaletteColor() {
		if (paletteIndex == palette.length-1) {
			paletteIndex = 0;
		}
		else {
			paletteIndex++;
		}
	}
}
