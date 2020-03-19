package models.transform;

// scale=1.2
//------------------------------------------------------------
public class Multiply implements Transform {
	
	private Double mCoefficient;
	
	//------------------------------------------------------------
	public Multiply(String value) {
		mCoefficient = Double.valueOf(value); 
	}
	
	//------------------------------------------------------------
	public final Double apply(Double input) {
		return input * mCoefficient;
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> Multiply: <" + mCoefficient + "> ";
	}
}
