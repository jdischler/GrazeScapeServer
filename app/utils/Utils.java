package utils;

//------------------------------------------------------------------------------
public final class Utils
{
	public static final float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}

