package utils;

import play.*;
//import play.db.ebean.Model.Finder;

public class ClientUser {
	public String email;
	public String password;
	public String organization;
	public String passwordSalt;
	public boolean admin;
	public int accessFlags;
	
	// Requires Java 7 for binary literals...Please use ALL CAPS for names to reduce chance of
	//	grid panel data collisions with the client admin panel. :/
	public enum ACCESS {
		CRP			(0b00001),
		AG_LANDS	(0b00010);
		
		public int value;
		private ACCESS(int value) { this.value = value; }
		public boolean matches(int testValue) {
			return (this.value & testValue) > 0;
		}
		public int addOption(int currentMask) {
			return currentMask | this.value;
		}
		
		public static ACCESS getEnumForString(String enumName) {
			for (ClientUser.ACCESS e : ClientUser.ACCESS.values()) {
				if (e.name().equals(enumName)) {
					return e;
				}
			}
			return null;
		}
	};
	
	public static int getMaskForAccessOptions(ACCESS ... options) {
		int mask = 0;
		for (int i = 0; i < options.length; i++) {
			mask = options[i].addOption(mask);
		}
		return mask;
	}
	
	public ClientUser(String email, String organization, String password, String pwdSalt){
		this.email = email;
		this.organization = organization;
		this.password = password;
		this.passwordSalt = pwdSalt;
		this.admin = false;
	}
	
	public static ClientUser authenticate(String email, String password) {
		return null;
	}
	
	public int getAccessFlags() {
		 return this.accessFlags; 
	}
	
	public void updateAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
	}

}
