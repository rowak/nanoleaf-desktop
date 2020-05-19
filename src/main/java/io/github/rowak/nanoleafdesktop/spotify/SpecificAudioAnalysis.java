package io.github.rowak.nanoleafdesktop.spotify;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

public class SpecificAudioAnalysis
{
	private int time;
	private float sensitivity;
	private AudioAnalysis analysis;
	
	public static SpecificAudioAnalysis getAnalysis(
			AudioAnalysis analysis, int time, float sensitivity)
	{
		final SpecificAudioAnalysis specificAnalysis =
				new SpecificAudioAnalysis();
		
		specificAnalysis.analysis = analysis;
		specificAnalysis.time = time;
		specificAnalysis.sensitivity = sensitivity;
		
		return specificAnalysis;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public AudioAnalysisMeasure getBar()
	{
		return getMeasureAtTime(analysis, analysis.getBars(), time,
				sensitivity, 0, analysis.getBars().length-1);
	}
	
	public AudioAnalysisMeasure getBeat()
	{
		return getMeasureAtTime(analysis, analysis.getBeats(), time,
				sensitivity, 0, analysis.getBeats().length-1);
	}
	
	public AudioAnalysisMeasure getTatum()
	{
		return getMeasureAtTime(analysis, analysis.getTatums(), time,
				sensitivity, 0, analysis.getTatums().length-1);
	}
	
	public AudioAnalysisSegment getSegment()
	{
		return getSegmentInTimeRange(analysis, analysis.getSegments(), time,
				sensitivity, 0, analysis.getSegments().length-1);
	}
	
	public AudioAnalysisSection getSection()
	{
		return getSectionInTimeRange(analysis, analysis.getSections(), time,
				sensitivity, 0, analysis.getSections().length-1);
	}
	
	private static AudioAnalysisMeasure getMeasureAtTime(AudioAnalysis analysis,
			AudioAnalysisMeasure[] measures, int time, float sensitivity, int left, int right)
	{
		if (right >= left)
		{
			int middle = left+(right-left)/2;
			double start = round(measures[middle].getStart(), 1);
			double pos = round((float)time/1000, 1);
			if (start == pos && isInSensitivityRange(
					measures[middle].getConfidence(), sensitivity))
			{
				return measures[middle];
			}
			
			if (start > pos)
			{
				return getMeasureAtTime(analysis, measures, time, sensitivity, left, middle-1);
			}
			
			return getMeasureAtTime(analysis, measures, time, sensitivity, middle+1, right);
		}
		return null;
	}
	
	private static AudioAnalysisSection getSectionInTimeRange(AudioAnalysis analysis,
			AudioAnalysisSection[] sections, int time, float sensitivity, int left, int right)
	{
		if (right >= left)
		{
			int middle = left+(right-left)/2;
			AudioAnalysisMeasure measure = sections[middle].getMeasure();
			double start = round(measure.getStart(), 1);
			double end = start + round(measure.getDuration(), 1);
			double pos = round((float)time/1000, 1);
			if (start < pos && pos < end && isInSensitivityRange(
					measure.getConfidence(), sensitivity))
			{
				return sections[middle];
			}
			
			if (end > pos)
			{
				return getSectionInTimeRange(analysis, sections, time,
						sensitivity, left, middle-1);
			}
			
			return getSectionInTimeRange(analysis, sections, time,
					sensitivity, middle+1, right);
		}
		return null;
	}
	
	private static AudioAnalysisSegment getSegmentInTimeRange(AudioAnalysis analysis,
			AudioAnalysisSegment[] sections, int time, float sensitivity, int left, int right)
	{
		if (right >= left)
		{
			int middle = left+(right-left)/2;
			AudioAnalysisMeasure measure = sections[middle].getMeasure();
			double start = round(measure.getStart(), 1);
			double end = start + round(measure.getDuration(), 1);
			double pos = round((float)time/1000, 1);
			if (start < pos && pos < end && isInSensitivityRange(
					measure.getConfidence(), sensitivity))
			{
				return sections[middle];
			}
			
			if (end > pos)
			{
				return getSegmentInTimeRange(analysis, sections, time,
						sensitivity, left, middle-1);
			}
			
			return getSegmentInTimeRange(analysis, sections, time,
					sensitivity, middle+1, right);
		}
		return null;
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
