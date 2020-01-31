package utils;

import java.util.*;

//------------------------------------------------------------------------------
public class RandomString {

	// return A-Z
	public static final String get(int numCharacters) {
	
		StringBuilder result = new StringBuilder(numCharacters);
		Random rand = new Random();
		for (int i=0; i < numCharacters; i++) {	
			int value = rand.nextInt(26) + 65; // 26 characters, A-Z, A starts at 65 ASCII
			result.append((char)value);
		}
		
		return result.toString();
	}

	// return ! - @, A-Z, [ - `, a-z, { - ~ 
	public static final String getComplex(int numCharacters) {
		
		StringBuilder result = new StringBuilder(numCharacters);
		Random rand = new Random();
		for (int i=0; i < numCharacters; i++) {	
			int value = rand.nextInt(94) + 33; // 94 characters, ! starts at 33 ASCII, returns up to ~
			result.append((char)value);
		}
		
		return result.toString();
	}
}
