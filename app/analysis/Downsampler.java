package analysis;

import java.util.ArrayList;
import java.util.Collections;

// TODO: consider some way to capture the Min and Max value and return that??
//------------------------------------------------------------------------------
public class Downsampler {
	
	// FLOAT: Returns a transformed data array of the requested size. 
	//--------------------------------------------------------------------------
	static public float [][] mean(float[][] data, int dataWidth, int dataHeight, int targetWidth, int targetHeight) throws Exception {
		
		return process(data, dataWidth, dataHeight, targetWidth, targetHeight, new ComputeMean());
	};
	//--------------------------------------------------------------------------
	static public float [][] max(float[][] data, int dataWidth, int dataHeight, int targetWidth, int targetHeight) throws Exception {
		
		return process(data, dataWidth, dataHeight, targetWidth, targetHeight, new ComputeMax());
	};
	//--------------------------------------------------------------------------
	static public float [][] median(float[][] data, int dataWidth, int dataHeight, int targetWidth, int targetHeight) throws Exception {
		
		return process(data, dataWidth, dataHeight, targetWidth, targetHeight, new ComputeMedian());
	};
	
	//--------------------------------------------------------------------------
	//
	//   Internal Implementations
	//
	//--------------------------------------------------------------------------
	private interface Computable {
		public float compute(float[][] data, int fromX, int fromY, int toX, int toY); 
	};
	
	//--------------------------------------------------------------------------
	private static class ComputeMean implements Computable {
		public final float compute(float[][] data, int fromX, int fromY, int toX, int toY) {
			
			float sum = 0, ave = -9999.0f;
			int ct = 0;
			for (int y = fromY; y < toY; y++) {
				for (int x = fromX; x < toX; x++) {
					if (data[y][x] > -9999.0f || data[y][x] < -9999.1f) {
						sum += data[y][x];
						ct++;
					}
				}
			}

			if (ct > 0) {
				ave = sum / ct;
			}
			return ave;
		}
	}

	//--------------------------------------------------------------------------
	private static class ComputeMax implements Computable {
		public final float compute(float[][] data, int fromX, int fromY, int toX, int toY) {
			
			float max = -9999.0f;
			boolean mbHasMax = false;
			for (int y = fromY; y <= toY; y++) {
				for (int x = fromX; x <= toX; x++) {
					float result = data[y][x];
					if (result > -9999.0f || result < -9999.1f) {
						if (!mbHasMax) {
							max = result;
							mbHasMax = true;
						}
						// test absolute value so we capture the largest magnitude, be it pos or neg
						if (Math.abs(result) > Math.abs(max)) {
							// but save the original max result, vs. absolute val.
							max = result;
						}
					}
				}
			}
			return max;
		}
	}
	
	//--------------------------------------------------------------------------
	private static class ComputeMedian implements Computable {
		public final float compute(float[][] data, int fromX, int fromY, int toX, int toY) {
			
			ArrayList<Float> values = new ArrayList<Float>();	
			for (int y = fromY; y <= toY; y++) {
				for (int x = fromX; x <= toX; x++) {
					float result = data[y][x];
					if (result > -9999.0f || result < -9999.1f) {
						values.add(result);
					}
				}
			}
			int size = values.size();
			if (size <= 0) return -9999.0f;
			Collections.sort(values);
			
			if (size % 2 > 0) { // odd, return the middle
				return values.get(size / 2);
			}
			else { // even, average the two middle values
			    return  (values.get(size / 2) + values.get(size / 2 - 1)) / 2.0f;
			}
		}
	}
	
	// Common processing core
	//--------------------------------------------------------------------------
	static private float [][] process(float[][] data,
								int dataWidth, int dataHeight, 
								int targetWidth, int targetHeight, Computable c) throws Exception {
	
		// early out on rare condition of no down sample
		if (dataWidth == dataHeight && targetWidth == targetHeight) return data;
		if (targetWidth > dataWidth || targetHeight > dataHeight) {
			throw new Exception("Unsupported up-sample request in Downsampler: process");
		}
		
		float [][] processedData = new float[targetHeight][targetWidth];
		
		float widthFactor = (dataWidth / (float)targetWidth);
		float heightFactor = (dataHeight / (float)targetHeight);
		float halfWidthFactor = widthFactor * 0.5f;
		float halfHeightFactor = heightFactor * 0.5f;

		int leftY = 0;
		
		for (int y = 0; y < targetHeight; y++) {
			
			//int rightY = Math.round(y * heightFactor + halfHeightFactor);
			int rightY = Math.round((y+1) * heightFactor);
			if (rightY > dataHeight) rightY = dataHeight;//-1;
			
			int leftX = 0;
			
			for (int x = 0; x < targetWidth; x++) {
//				int rightX = Math.round(x * widthFactor + halfWidthFactor);
				int rightX = Math.round((x+1) * widthFactor);
				
				if (rightX > dataWidth) rightX = dataWidth;//-1;
				
				processedData[y][x] = c.compute(data, leftX, leftY, rightX, rightY);
				leftX = rightX;
			}
			leftY = rightY;
		}
		
		return processedData;
	}
	
}
