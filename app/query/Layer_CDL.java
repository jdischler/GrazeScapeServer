package query;

// This is a simple wrapper meant to centralize access to the CDL data, which 
//	is one of the primary "driver" layers for core processing.
// It could also provide a centralized place for replacing the string crop lookups
//	with enums?
//------------------------------------------------------------------------------
public class Layer_CDL
{
	public static Layer_Integer get() {
		return (Layer_Integer)Layer_Base.getLayer("wisc_land_2"); 
	}
}

