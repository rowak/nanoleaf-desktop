package io.github.rowak.nanoleafdesktop.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.PluginEffect;

/*
 * This class can be used to map a legacy
 * plugin's UUID to it's type or vice versa.
 */
public class LegacyPluginMap {
	
	public static final String WHEEL = "6970681a-20b5-4c5e-8813-bdaebc4ee4fa";
	public static final String FLOW = "027842e4-e1d6-4a4c-a731-be74a1ebd4cf";
	public static final String EXPLODE = "713518c1-d560-47db-8991-de780af71d1e";
	public static final String FADE = "b3fd723a-aae8-4c99-bf2b-087159e0ef53";
	public static final String RANDOM = "ba632d3e-9c2b-4413-a965-510c839b3f71";
	public static final String HIGHLIGHT = "70b7c636-6bf8-491f-89c1-f4103508d642";
	
	public static final List<String> TYPES = Arrays.asList(
										  new String[]{"WHEEL", "FLOW", "EXPLODE",
										  "FADE", "RANDOM", "HIGHLIGHT"});
	
	public enum LegacyType {
		WHEEL, FLOW, EXPLODE,
		FADE, RANDOM, HIGHLIGHT
	}
	
	public static boolean isLegacy(Effect ef) {
		if (!ef.getEffectType().equals("plugin")) {
			return false;
		}
		PluginEffect pef = (PluginEffect)ef;
		if (pef.getPlugin().getType() != null &&
				pef.getPlugin().getType().equals("color")) {
			return isLegacy(pef.getPlugin().getUUID());
		}
		else if (TYPES.contains(pef.getEffectType())) {
			return true;
		}
		return false;
	}
	
	public static boolean isLegacy(String uuid) {
		return getMap().containsKey(uuid);
	}
	
	public static LegacyType getLegacyType(Effect ef)
	{
		if (!ef.getEffectType().equals("plugin")) {
			return null;
		}
		PluginEffect pef = (PluginEffect)ef;
		if (TYPES.contains(pef.getPlugin().getType().toString())) {
			return typeFromStr(pef.getEffectType().toString());
		}
		return getLegacyType(pef.getPlugin().getUUID());
	}
	
	public static LegacyType getLegacyType(String uuid) {
		if (isLegacy(uuid)) {
			return getMap().get(uuid);
		}
		return null;
	}
	
	private static LegacyType typeFromStr(String str) {
		for (LegacyType t : LegacyType.values()) {
			if (t.toString().toLowerCase().equals(str.toLowerCase())) {
				return t;
			}
		}
		return null;
	}
	
	public static Map<String, LegacyType> getMap() {
		Map<String, LegacyType> map =
				new HashMap<String, LegacyType>();
		map.put(WHEEL, LegacyType.WHEEL);
		map.put(FLOW, LegacyType.FLOW);
		map.put(EXPLODE, LegacyType.EXPLODE);
		map.put(FADE, LegacyType.FADE);
		map.put(RANDOM, LegacyType.RANDOM);
		map.put(HIGHLIGHT, LegacyType.HIGHLIGHT);
		return map;
	}
}
