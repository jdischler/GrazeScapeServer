package utils;

import play.*;

import java.awt.Color;
import java.io.*;

// PNGJ api docs....
// http://pngj.googlecode.com/git/pnjg/doc/api/index.html
import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

// Provides a wrapper and helpers around the PNGJ library
//------------------------------------------------------------------------------
public class Png {

	//------------------------	
	public static class RGB {
		public int mR, mG, mB;
		
		public RGB(int r, int g, int b) {
			mR = r;
			mG = g;
			mB = b;
		}
		public RGB(String hexString) {
			Color c = Color.decode(hexString);
			mR = c.getRed();
			mG = c.getGreen();
			mB = c.getBlue();
		}
	}

	protected ImageInfo mImageInfo;
	public PngWriter mPngWriter;
	
	// Creates indexed image if channels == 1 
	//	Creates RGB image if channels == 3 
	//	Creates RGBA image if channels == 4
	// OutputFile is the filename with path to write
	//--------------------------------------------------------------------------
	public Png(int width, int height, int bitDepth, int channels, String outputFile) {
		
		if (channels == 1) {
			// width, height, bitDepth, alpha, grayscale, indexed
			mImageInfo = new ImageInfo(width, height, bitDepth, false, false, true);
		}
		else if (channels == 3) {
			// width, height, bitDepth, alpha
			mImageInfo = new ImageInfo(width, height, bitDepth, false);
		}
		else if (channels == 4) {
			// width, height, bitDepth, alpha
			mImageInfo = new ImageInfo(width, height, bitDepth, true);
		}
		
		File dir = new File(outputFile).getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
			if (!dir.exists()) {
			}
		}
		
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outputFile);
		}
		catch (Exception e) {
		}
		
		mPngWriter = new PngWriter(outputStream, mImageInfo);

		// set default compression and dpi?		
		//mPngWriter.setCompLevel(9); // compression level not working?
		//mPngWriter.getMetadata().setDpi(306.98);
	}

	// Not sure if we generally need to set a DPI for the DSS?	
	//--------------------------------------------------------------------------
	public void setDPI(float dpi) {
		
		mPngWriter.getMetadata().setDpi(dpi);
	}
	
	// Only for indexed (palettized) images
	// returns PNGJ palette chunk object
	//--------------------------------------------------------------------------
	public PngChunkPLTE createPalette(int numPaletteEntries) throws Exception {
		
		if (!mImageInfo.indexed) {
			throw new Exception();
		}
		
		PngChunkPLTE palette = mPngWriter.getMetadata().createPLTEChunk();
		palette.setNentries(numPaletteEntries);
		
		return palette;
	}
	
	// Only for indexed (palettized) images
	//--------------------------------------------------------------------------
	public void setTransparentIndex(int paletteIndex) {
		
		PngChunkTRNS trans = mPngWriter.getMetadata().createTRNSChunk();
		trans.setIndexEntryAsTransparent(paletteIndex);
	}

	// Only for indexed (palettized) images
	//--------------------------------------------------------------------------
	public void setTransparentArray(int[] arrayOfAlphas) {
		
		PngChunkTRNS trans = mPngWriter.getMetadata().createTRNSChunk();
		trans.setPalletteAlpha(arrayOfAlphas);
	}
	
	//--------------------------------------------------------------------------
	public void writeArray(int[][] imagePixels) {
		
		mPngWriter.writeRowsInt(imagePixels);
		mPngWriter.end();
	}

	//--------------------------------------------------------------------------
	public void writeArray(byte[][] imagePixels) {
		
		mPngWriter.writeRowsByte(imagePixels);
		mPngWriter.end();
	}

	//--------------------------------------------------------------------------
	public void setColor(PngChunkPLTE palette, int index, Png.RGB color) {
		
		palette.setEntry(index, color.mR, color.mG, color.mB);
	}
	
	//--------------------------------------------------------------------------
	private static int interpolateElement(int startIdx, int endIdx, int curIdx, int startClrVal, int endClrVal) {
		
		int colorVal = Math.round(
				((float)(endClrVal - startClrVal) / (float)(endIdx - startIdx)) 
				* (curIdx - startIdx) + startClrVal
			);
		
		if (colorVal < 0) colorVal = 0;
		else if (colorVal > 255) colorVal = 255;
		
		return colorVal;
	}
	
	//--------------------------------------------------------------------------
	public static void interpolatePaletteEntries(PngChunkPLTE palette, 
			int startIndex, int startR, int startG, int startB,
			int endIndex, int endR, int endG, int endB) {
	
		for (int idx = startIndex; idx <= endIndex; idx++) {

			int r = interpolateElement(startIndex, endIndex, idx, startR, endR);
			int g = interpolateElement(startIndex, endIndex, idx, startG, endG);
			int b = interpolateElement(startIndex, endIndex, idx, startB, endB);
			palette.setEntry(idx, r, g, b);
		}
	}
}

