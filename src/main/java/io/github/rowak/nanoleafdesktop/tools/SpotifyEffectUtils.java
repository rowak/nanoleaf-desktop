package io.github.rowak.nanoleafdesktop.tools;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;

public class SpotifyEffectUtils
{
	public static float getLoudness(float previousLoudness, SpecificAudioAnalysis analysis)
	{
		AudioAnalysisSegment segment = analysis.getSegment();
		if (segment != null)
		{
			float avg = (segment.getLoudnessMax() +
					segment.getLoudnessStart()+0.1f)/2f;
			return loudnessToPercent(avg, segment.getLoudnessMax());
		}
		return previousLoudness;
	}
	
	public static float loudnessToPercent(float loudness, float max)
	{
		final float MIN = -40.0f;
		if (loudness < MIN)
		{
			return 0f;
		}
		else if (loudness > max)
		{
			return 1f;
		}
		return (1 - loudness/MIN);
	}
}
