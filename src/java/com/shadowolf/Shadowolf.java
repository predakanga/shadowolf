package com.shadowolf;


public class Shadowolf {
	
	public static void main(String[] args) throws Exception {
		ShadowolfContext context = ShadowolfContext.createNewContext("conf/shadowolf.properties");
		context.start();
	}

}
