package analysis;

import utils.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import ar.com.hjg.pngj.chunks.*;

//------------------------------------------------------------------------------
public class RasterToPNG {
	
    private static final Logger logger = LoggerFactory.getLogger("application");

	public static class MinMax {
		
		public float mMin, mMax;
		public boolean mbHasValues = false;
		
		public MinMax(float min, float max) {
			mMin = min; mMax = max;
		}
		
		public void initialize(float min, float max) {
			this.mbHasValues = true;
			this.mMin = min;
			this.mMax = max;
		}
	}
	
	//--------------------------------------------------------------------------
	private static ArrayNode getPaletteForClient(PngChunkPLTE palette, int numColors) {
		
		ArrayNode colorArray = JsonNodeFactory.instance.arrayNode();
		
		int[] rgb = new int[3];
		
		for (int i=0; i < numColors; i++) {
			String color = "#";
			palette.getEntryRgb(i, rgb);
			for (int t=0; t<3; t++) {
				String hex = Integer.toHexString(rgb[t]);
				// prefill with zeroes..
				while(hex.length() < 2) {
					hex = "0" + hex;
				}
				color += hex;
			}
			colorArray.add(color);
		}
		
		return colorArray;
	}

/*	//--------------------------------------------------------------------------
	private static ObjectNode copyQuantizedValuesForClient(float min, float max,
								int [] quantiledBins, int quantileBinCt,
								int entries, ObjectNode sendBack) {

		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();

		// we'll start at the min value and step along by the addPerBin.
		float addPerBin = (max - min) / quantileBinCt;
		
		// Once the palIndex increases in the quantiledBin, we know we've hit a transition point, so dump
		//	the running value out to the array
		int palIndex = 0;

		valueArray.add(min);
		
		for (int idx=0; idx < quantileBinCt; idx++) {
			if (quantiledBins[idx] > palIndex) {
				valueArray.add(min + addPerBin * idx);
				palIndex = quantiledBins[idx];
			}
		}
		
		valueArray.add(max);
		
		sendBack.set("values", valueArray);
		return sendBack;
	}
*/
	//--------------------------------------------------------------------------
	private static ArrayNode getValuesForClient(MinMax mm, int entries) {
		
		float min = mm.mMin, max = mm.mMax;
		
		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();
		
		for (int i=0; i < entries; i++) {
			float value = (float)(min + i * ((max - min) / entries));
			valueArray.add(value);
		}
		
		valueArray.add(max);
		
		return valueArray;
	}

	//--------------------------------------------------------------------------
	private static PngChunkPLTE makeDetailedBlueToRed(PngChunkPLTE pal) {
		
		pal.setEntry(0, 62,  117, 178);	// blue
		pal.setEntry(1, 83,  135, 191);	
		pal.setEntry(2, 105, 153, 205);
		pal.setEntry(3, 126, 170, 218);
		pal.setEntry(4, 148, 188, 232);
		pal.setEntry(5, 165, 201, 236);
		pal.setEntry(6, 182, 215, 241);
		pal.setEntry(7, 199, 228, 245);
		pal.setEntry(8, 216, 242, 250);
		pal.setEntry(9, 225, 245, 242);
		pal.setEntry(10, 234, 249, 235);
		pal.setEntry(11, 243, 252, 227);
		
		pal.setEntry(12, 252, 255, 220); // yellow
		
		pal.setEntry(13, 253, 246, 205);
		pal.setEntry(14, 254, 237, 190);
		pal.setEntry(15, 255, 227, 175);
		pal.setEntry(16, 255, 218, 160);
		pal.setEntry(17, 251, 198, 142);
		pal.setEntry(18, 248, 179, 125);
		pal.setEntry(19, 244, 159, 107);
		pal.setEntry(20, 240, 140,  89);
		pal.setEntry(21, 232, 128,  80);
		pal.setEntry(22, 225, 116,  71);
		pal.setEntry(23, 217, 104,  61);
		pal.setEntry(24, 210,  92,  52); // red
		
		pal.setEntry(25, 255, 0, 0);// red, but transparent
		
		return pal;
	}
	
	//--------------------------------------------------------------------------
	private static PngChunkPLTE makeCoarseBlueToRed(PngChunkPLTE pal) {
		
		pal.setEntry(0, 62,  117, 178);	// blue
		pal.setEntry(1, 182, 215, 241);
		
		pal.setEntry(2, 252, 255, 220); // yellow
		
		pal.setEntry(3, 247, 179,  124);
		pal.setEntry(4, 210,  92,  52); // red
		
		pal.setEntry(5, 255, 0, 0);// red, but transparent
		
		return pal;
	}

	//--------------------------------------------------------------------------
	private static PngChunkPLTE makeBlueToRed(PngChunkPLTE pal) {
		
		pal.setEntry(0, 62,  117, 178);	// blue
		pal.setEntry(1, 148, 188, 232);
		pal.setEntry(2, 216, 242, 250);
		
		pal.setEntry(3, 252, 255, 220); // yellow
		
		pal.setEntry(4, 255, 218, 160);
		pal.setEntry(5, 240, 140,  89);
		pal.setEntry(6, 210,  92,  52); // red
		
		pal.setEntry(7, 255, 0, 0);// red, but transparent
		
		return pal;
	}

	//--------------------------------------------------------------------------
	public static JsonNode save(float [][]data, int width, int height, File file) {
		
		final int numColorEntries = 5;
		
		MinMax minMax = getMinMax(data, width, height);
		
		byte[][] idx = convertToIndexed(data, width, height, minMax, numColorEntries);
	
		Png png = new Png(width, height, 8, 1, file.getPath());
		PngChunkPLTE palette = null;
		try {
			palette = png.createPalette(numColorEntries + 1); // +1 for transparent color
		}
		catch(Exception e) {
			logger.error(e.toString());
		}

		palette = makeCoarseBlueToRed(palette);
		
		int[] alpha = new int[numColorEntries + 1];
		
		for (int i=0; i < numColorEntries; i++) {
			alpha[i] = 255;
		}
		// last color is always transparent (no-data)
		alpha[numColorEntries] = 0;
		png.setTransparentArray(alpha);
		
		JsonNode results = Json.pack("palette", getPaletteForClient(palette, numColorEntries),
									"values", getValuesForClient(minMax, numColorEntries));
		
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return results;
	}


	// ONE FILE ABSOLUTE STYLE MAP - Quantiled
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
/*	public static ObjectNode runAbsoluteQuantiled(File file, File outputFile, int downsampleFactor) {
		
		final int numPaletteEntries = 7;
		
		Binary_Reader fileReader = new Binary_Reader(file);
		if (!fileReader.readHeader()) {
			logger.error("Heatmap generation unable to open the file and/or read the header");
			return null;
		}
		
		int width = fileReader.getWidth(), height = fileReader.getHeight();
		
		float heatmap[][] = new float[height][width];
		
		for (int y=0; y < height; y++) {
			ByteBuffer buff = fileReader.readLine();
			if (buff != null) {
				for (int x=0; x < width; x++) {
					heatmap[y][x] = buff.getFloat(x * 4);
				}
			}
		}
			
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = null;
		try {
			resampled = Downsampler.mean(heatmap, width, height, newWidth, newHeight);
		} catch (Exception e) {
			logger.error(e.getStackTrace().toString());
		}
		MinMax minMax = getMinMax(resampled, newWidth, newHeight);
		float min = minMax.mMin, max = minMax.mMax;
		
		logger.info(" ...Calculating Quantile...");
		int binCount = 200; // higher values should (theoretically) yield more accurate divisions...
		
		// create and zero bins...
		int [] bins = new int[binCount]; 
		int totalCells = 0;
		
		for (int i=0; i < binCount; i++) {
			bins[i] = 0;
		}
		// count values for each bin so we get a histogram...
		for (int y=0; y < newHeight; y++) {
			for (int x=0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f || data < -9999.1f) { // FIXME: boo, hate these NoData check...
					int binIndex = (int)((data - min)/(max - min) * binCount);
					if (binIndex > binCount - 1) binIndex = binCount - 1;
					bins[binIndex]++;
					totalCells++;
				}
			}
		}
		
		// What is an even distribution?..divide and round...
		int desiredCountPerQuantile = (int)((float)totalCells / numPaletteEntries + 0.5f);
		
		// Now clump our bins together so the sum of a string of adjacent bins will be 
		//	roughly equal to our even distribution..
		// stuff a remapped index to a color palette as we go so we can easily figure out color assignments...
		int countTracker = 0;
		int indexRemapperTracker = 0;
		for (int i=0; i < binCount; i++) {
			countTracker += bins[i];
			bins[i] = indexRemapperTracker;// overwrite the count in the bin, reusing it to store a palette index remapper
			// does the current running count for bins put us where we need to be?
			// we should also check if adding the next bin will put us over by too far...
			// We do a look-ahead by one bin so we don't get stuck adding way too many counts in case that
			//	next bin were much bigger than normal...
			if (countTracker >= desiredCountPerQuantile ||  
				(i < binCount - 1 && countTracker + bins[i+1] / 2 > desiredCountPerQuantile)) {
					// step to next index for the remapper...
					indexRemapperTracker++;
					countTracker -= desiredCountPerQuantile; // track leftovers/remainders..
			}
		}

		logger.info(" ...Generating IDX array");

		byte[][] idx = new byte[newHeight][newWidth];

		// Now generate the heatmap image
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f || data < -9999.1f) { // BOO...hate these NoData checks!
					int index = (int)((data - min)/(max - min) * binCount);
					if (index > binCount - 1) index = binCount - 1;
					int palIndex = bins[index];
					if (palIndex < 0) palIndex = 0;
					else if (palIndex > numPaletteEntries - 1) palIndex = numPaletteEntries - 1; 
					idx[y][x] = (byte)(palIndex);
				}
				else {
					idx[y][x] = numPaletteEntries; // last color is transparent color
				}
			}
		}

		logger.info("Creating png");
		Png png = new Png(newWidth, newHeight, 8, 1, outputFile.getPath());
	
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyQuantizedValuesForClient(min, max, bins, binCount, numPaletteEntries, sendBack);
		sendBack = createPalette(png, numPaletteEntries, minMax, sendBack);
	
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}*/
	
	//--------------------------------------------------------------------------
	private static MinMax getMinMax(float data[][], int width, int height) {
	
		MinMax minMax = new MinMax(0,0);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = data[y][x];
				if (val > -9999.0f || val < -9999.1f) {
					if (!minMax.mbHasValues) {
						minMax.initialize(val, val);
					}
					if (val > minMax.mMax) {
						minMax.mMax = val;
					}
					else if (val < minMax.mMin) {
						minMax.mMin = val;
					}
				}
			}
		}
		
		return minMax;
	}
	
	//--------------------------------------------------------------------------
	private static byte[][] convertToIndexed(float data[][], int width, int height, MinMax mm, int numVals) {
		
		byte[][] idxs = new byte[height][width];
		float min = mm.mMin, max = mm.mMax;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				float val = data[y][x];
				if (val > -9999.0f || val < -9999.1f) {
					int bin = Math.round((val - min) / (max - min) * numVals);
					if (bin < 0) 
						bin = 0;
					else if (bin > numVals-1) 
						bin = numVals-1;
					idxs[y][x] = (byte)(bin);
				}
				else {
					idxs[y][x] = (byte)numVals; // transparent
				}
			}
		}
		
		return idxs;
	}
}

