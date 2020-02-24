package analysis;

public class Extract {
	
	static public final float [][] now(float[][] data,
								int atX, int atY,
								int width, int height) {
	
		float [][] extracted = new float[height][width];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				extracted[y][x] = data[y + atY][x + atX];
			}
		}
		
		return extracted;
	}
	
}
