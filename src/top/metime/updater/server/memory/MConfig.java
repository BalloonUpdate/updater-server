package top.metime.updater.server.memory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import top.metime.updater.server.tools.Base64;

public class MConfig 
{
	public int port;
	public int maxDownstreamSpeed;
	public int maxOnlineClient;
	//public boolean enableTray;
	public String clientJAR;
	
	public void load(InputStream inputStream) throws IOException, IllegalArgumentException, IllegalAccessException
	{
		Properties prop = new Properties();
		prop.load(inputStream);// 从输入流加载Properties
		
		for(Field per : getClass().getDeclaredFields())
		{
			int mdf = per.getModifiers();
			if(Modifier.isPublic(mdf) || !Modifier.isStatic(mdf))
			{
				String value = prop.getProperty(per.getName());
				if(per.getType()==byte.class || per.getType().isArray())
				{
					per.set(this, Base64.encodeBase64((byte[])per.get(this)));
				}else
				if(per.getType()==int.class)
				{
					per.set(this, Integer.parseInt(value));
				}else
				if(per.getType()==float.class)
				{
					per.set(this, Float.parseFloat(value));
				}else
				if(per.getType()==double.class)
				{
					per.set(this, Double.parseDouble(value));
				}else
				if(per.getType()==boolean.class)
				{
					per.set(this, Boolean.parseBoolean(value));
				}else
				if(per.getType()==String.class)
				{
					per.set(this, value);
				}
			}
		}
	}
	
	public Properties toProperties(MConfig obj) throws IllegalArgumentException, IllegalAccessException
	{
		Properties prop = new Properties();
		
		for(Field field : MConfig.class.getDeclaredFields())
		{
			int modifiers = field.getModifiers();
			if(Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
			{
				if(field.getType()==byte.class || field.getType().isArray())
				{
					prop.put(field.getName(), Base64.encodeBase64((byte[]) field.get(obj)));
				}else
				prop.put(field.getName(), String.valueOf(field.get(obj)));
			}
		}
		
		return prop;
	}
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException
	{
		MConfig mconfig = new MConfig();
		
		mconfig.port = 5398;
		mconfig.maxDownstreamSpeed = 500;
		mconfig.maxOnlineClient = 4;
//		mconfig.enableTray = true;
		mconfig.clientJAR = "clientJar";
		
		Properties prop = mconfig.toProperties(mconfig);
		
		for(Object key : prop.keySet())
		{
			System.out.println(key+"="+prop.getProperty(key.toString()));
		}
		
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		for(Field field : MConfig.class.getDeclaredFields())
		{
			int modifiers = field.getModifiers();
			if(Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
			{
				try {
					buffer.append(field.getName()+"="+String.valueOf(field.get(this))+"\n");
				} 
				catch (IllegalArgumentException | IllegalAccessException ex)
				{
					ex.printStackTrace();
				} 
			}
		}
		
		return buffer.toString();
	}
	
	
	
}
