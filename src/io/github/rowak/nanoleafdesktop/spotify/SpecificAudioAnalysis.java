package io.github.rowak.nanoleafdesktop.spotify;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

public class SpecificAudioAnalysis
{
	private AudioAnalysisMeasure bar, beat, tatum;
	private AudioAnalysisSection section;
	private AudioAnalysisSegment segment;
	
	public static SpecificAudioAnalysis getAnalysis(
			AudioAnalysis analysis, int time, float sensitivity)
	{
		final SpecificAudioAnalysis specificAnalysis =
				new SpecificAudioAnalysis();
		
		specificAnalysis.bar = getBarAtPos(analysis, time, sensitivity);
		specificAnalysis.beat = getBeatAtPos(analysis, time, sensitivity);
		specificAnalysis.tatum = getTatumAtPos(analysis, time, sensitivity);
		specificAnalysis.section = getSectionAtPos(analysis, time, sensitivity);
		specificAnalysis.segment = getSegmentAtPos(analysis, time, sensitivity);
		
		return specificAnalysis;
	}
	
	public AudioAnalysisMeasure getBar()
	{
		return bar;
	}
	
	public AudioAnalysisMeasure getBeat()
	{
		return beat;
	}
	
	public AudioAnalysisMeasure getTatum()
	{
		return tatum;
	}
	
	public AudioAnalysisSection getSection()
	{
		return section;
	}
	
	public AudioAnalysisSegment getSegment()
	{
		return segment;
	}
	
	private static AudioAnalysisMeasure getBarAtPos(AudioAnalysis analysis,
			int time, float sensitivity)
	{
		for (AudioAnalysisMeasure measure : analysis.getBars())
		{
			if (measureAtTime(measure, time, sensitivity))
			{
				return measure;
			}
		}
		return null;
	}
	
	private static AudioAnalysisMeasure getBeatAtPos(AudioAnalysis analysis,
			int time, float sensitivity)
	{
		for (AudioAnalysisMeasure measure : analysis.getBeats())
		{
			if (measureAtTime(measure, time, sensitivity))
			{
				return measure;
			}
		}
		return null;
	}
	
	private static AudioAnalysisMeasure getTatumAtPos(AudioAnalysis analysis,
			int time, float sensitivity)
	{
		for (AudioAnalysisMeasure measure : analysis.getTatums())
		{
			if (measureAtTime(measure, time, sensitivity))
			{
				return measure;
			}
		}
		return null;
	}
	
	private static AudioAnalysisSegment getSegmentAtPos(AudioAnalysis analysis,
			int time, float sensitivity)
	{
		for (AudioAnalysisSegment segment : analysis.getSegments())
		{
			AudioAnalysisMeasure measure = segment.getMeasure();
			if (measureAtTime(measure, time, sensitivity))
			{
				return segment;
			}
		}
		return null;
	}
	
	private static AudioAnalysisSection getSectionAtPos(AudioAnalysis analysis,
			int time, float sensitivity)
	{
		for (AudioAnalysisSection section : analysis.getSections())
		{
			AudioAnalysisMeasure measure = section.getMeasure();
			if (measureInTimeRange(measure, time, sensitivity))
			{
				return section;
			}
		}
		return null;
	}
	
	private static boolean measureAtTime(AudioAnalysisMeasure measure,
			int time, float sensitivity)
	{
		double start = round(measure.getStart(), 1);
		double pos = round((float)time/1000, 1);
		if (start == pos && isInSensitivityRange(
				measure.getConfidence(), sensitivity))
		{
			return true;
		}
		return false;
	}
	
	private static boolean measureInTimeRange(AudioAnalysisMeasure measure,
			int time, float sensitivity)
	{
		double start = round(measure.getStart(), 1);
		double pos = round((float)time/1000, 1);
		double end = start + round(measure.getDuration(), 1);
		if (start < pos && pos < end && isInSensitivityRange(
				measure.getConfidence(), sensitivity))
		{
			return true;
		}
		return false;
	}
	
	private static double round(float num, int decimals)
	{
		return (double)Math.round((double)num * 10d)/10d;
	}
	
	private static boolean isInSensitivityRange(float confidence, float sensitivity)
	{
		return confidence >= (float)(10-sensitivity)/10f;
	}
}
