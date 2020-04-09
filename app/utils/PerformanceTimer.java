package utils;

public class PerformanceTimer {
	
	private long mStartTime;
	private Boolean mbDebugLog = true;
	
	public PerformanceTimer() {
		restartTimer(); 
	}

	// Lame crutch for timeReport variant....returning the String and the Double for elapsedTime is messy so doing this instead
	public PerformanceTimer(Boolean debugLog) {
		mbDebugLog = debugLog;
		restartTimer(); 
	}
	
	/**
	 * Restart the timer to measure time elapsed from NOW (when called)  
	 */	
	public void restartTimer() {
		mStartTime = System.nanoTime(); 
	}
	
	public Double elapsedMilliseconds() {
		return (System.nanoTime() - mStartTime)/1000000.0;
	}
	
	public String stringMilliseconds(Integer places) {
		return String.format("%." + places + "f", elapsedMilliseconds());
	}
	
	public Double elapsedSeconds() {
		return (System.nanoTime() - mStartTime)/1000000000.0;
	}
	
	public String stringSeconds(Integer places) {
		return String.format("%." + places + "f", elapsedSeconds());
	}
	
	// -Example usage:
	// 		PerformanceTimer timer = new PerformanceTimer();
	// 		Double elapsedTime = 0;
	// -do some work
	// 		Logger.debug(timer.timeReport(" -- Timing for section 1: ", elapsedTime, " -- Total time: "));
	// -do some more work
	// 		Logger.debug(timer.timeReport(" -- Timing for section 2: ", elapsedTime, " -- Total time: "));
	// - Yields:
	//  -- Timing for section 1: 0.45s -- Total time: 0.45s
	//  -- Timing for section 2: 0.2s -- Total time: 0.65s
	public Double timeReport(String sectionTimingMessage, Double timingHelper, String totalTimingMessage) {
		Double el = elapsedSeconds();
		if (!mbDebugLog) return el;
		
		Double diff = el - timingHelper;
		//Logger.debug(sectionTimingMessage + diff + "s " + totalTimingMessage + el + "s");
		return el;
	}

}
