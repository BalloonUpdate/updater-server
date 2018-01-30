package top.metime.updater.server.tools;

import java.lang.reflect.Method;

public class Base64 
{
	public static String encodeBase64(byte[]input)
	{
		String ret = null;
		try {
			Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
			Method mainMethod= clazz.getMethod("encode", byte[].class);
			mainMethod.setAccessible(true);
			Object retObj=mainMethod.invoke(null, new Object[]{input});
			ret = (String)retObj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	public static byte[] decodeBase64(String input)
	{
		byte[] ret = null;
		
		try {
			Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
			Method mainMethod= clazz.getMethod("decode", String.class);
			mainMethod.setAccessible(true);
			Object retObj=mainMethod.invoke(null, input);
			ret = (byte[])retObj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ret;
	}

}
