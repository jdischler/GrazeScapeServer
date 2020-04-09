package models.transform;

//------------------------------------------------------------
public class PassThrough implements Transform {
	
	//------------------------------------------------------------
//	public SlopePercentToAngle(String value) {}
	
	//------------------------------------------------------------
	public final Float apply(Float input) {
		return input;
	}
	
	//-------------------------------------------------
	public final String debug() {
		return "> PassThrough ";
	}
}
