package models.transform;


// some examples: "power=2"
//	exp
// 	multiply=1.3
//-------------------------------------------------------
public class TransformFactory {
	
	public static Transform create(String text) {
		// Split and minimally sanitize
		String ex[] = text.split("=");
		for (int i = 0; i < ex.length; i++) {
			ex[i] = ex[i].trim().toLowerCase();
		}

		switch(ex[0]) {
			case "multiply": {
				// example: "multiply=1.2"
				return new Multiply(ex[1]);
			}
			case "clamp": {
				// example: "clamp=?/56"
				return new Clamp(ex[1]);
			}
			case "quadratic": {
				// example: "quadratic=2.0393/0.157/0.0001"  (intercept/x/x2)
				return new Quadratic(ex[1]);
			}
			case "unit-convert": {
				// example: "unit-convert:feet-to-meters"
				return new UnitConvert(ex[1]);
			}
			case "exp": {
				// example: "exp"
				return new Exp();
			}
			case "slope-percent-to-angle": {
				return new SlopePercentToAngle();
			}
			
			case "pass-through": {
				return new PassThrough();
			}
			
			default:
				return null;
		}
	}
}
