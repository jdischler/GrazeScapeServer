package models.transform;

// example: "exp"
//------------------------------------------------------------
public class Exp implements Transform {
	
	//-------------------------------------------------
	public final Double apply(Double input) {
		return Math.exp(input);
	}

	//-------------------------------------------------
	public final String debug() {
		return "> Exp ";
	}
}
