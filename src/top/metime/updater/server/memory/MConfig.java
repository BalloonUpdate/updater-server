package top.metime.updater.server.memory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import top.metime.updater.server.tools.Base64;

/**
 * 该类表示一个服务对象正常启动所需要的信息
 *
 * @author innc
 */
public class MConfig 
{

	/**
	 * 端口
	 */
	public int port;

	/**
	 * 最大下行速率（单位Kb/s）
	 */
	public int maxDownstreamSpeed;

	/**
	 * 最大同时在线客户端数
	 */
	public int maxOnlineClient;

	/**
	 * 客户端Jar文件路径
	 */
	public String clientJAR;
	//public boolean enableTray;
	
	/**
	 * 从一个输入流（Properties文件的输入流）加载
	 *
	 * @param inputStream 输入流
	 * @throws IOException IOException
	 * @throws IllegalArgumentException IllegalArgumentException
	 * @throws IllegalAccessException IllegalAccessException
	 */
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
	
	/**
	 * 把Config对象转换成Propertie对象
	 *
	 * @param obj Config对象
	 * @return 转换后的Propertie对象
	 * @throws IllegalArgumentException IllegalArgumentException
	 * @throws IllegalAccessException IllegalAccessException
	 */
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
	
/*
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
*/
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		
		for(Field field : MConfig.class.getDeclaredFields())
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
