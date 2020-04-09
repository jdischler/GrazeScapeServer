package models.transform;

// example: "exp"
//------------------------------------------------------------
public class Exp implements Transform {
	
	//-------------------------------------------------
	public final Float apply(Float input) {
		return (float) Math.exp(input);
	}

	//-------------------------------------------------
	public final String debug() {
		return "> Exp ";
	}
}
