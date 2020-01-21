package cn.innc11.updater.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.Properties;

public class Config
{
	public int port;
	public int maxDownstreamSpeed;
	public int maxOnlineClient;

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
					per.set(this, Base64.getEncoder().encode((byte[])per.get(this)));
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
	
	public Properties toProperties(Config obj) throws IllegalArgumentException, IllegalAccessException
	{
		Properties prop = new Properties();
		
		for(Field field : Config.class.getDeclaredFields())
		{
			int modifiers = field.getModifiers();
			if(Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
			{
				if(field.getType()==byte.class || field.getType().isArray())
				{
					prop.put(field.getName(), Base64.getEncoder().encode((byte[]) field.get(obj)));
				}else
				prop.put(field.getName(), String.valueOf(field.get(obj)));
			}
		}
		
		return prop;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		
		for(Field field : Config.class.getDeclaredFields())
		{
			int modifiers = field.getModifiers();
			if(Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
			{
				try {
					buffer.append(field.getName()).append("=").append(String.valueOf(field.get(this))).append("\n");
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
