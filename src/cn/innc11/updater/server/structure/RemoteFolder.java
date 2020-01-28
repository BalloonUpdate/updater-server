package cn.innc11.updater.server.structure;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

public class RemoteFolder extends RemoteObject
{
	private final LinkedList<RemoteObject> sublist = new LinkedList<>();
	
	public RemoteFolder(String name)
	{
		this.name = name;
	}
	public RemoteFolder(JSONObject ObjString)
	{
		name = ObjString.getString("name");
		JSONArray array = ObjString.getJSONArray("child");
		
		for(int c=0;c<array.length();c++)
		{
			JSONObject obj = array.getJSONObject(c);
			//System.out.println(array.get(c));
			if(obj.has("child"))
			{
				sublist.add(new RemoteFolder(obj));
			}
			else
			{
				sublist.add(new RemoteFile(obj));
			}
			
		}
	}
	
	public void append(RemoteObject d)
	{
		sublist.add(d);
	}
	
	public LinkedList<RemoteObject> getAllList()
	{
		return sublist;
	}
	
	@Override
	public String toString() 
	{
		JSONObject obj = new JSONObject();
		JSONArray child = new JSONArray();
		
		for(RemoteObject per : getAllList())
		{
			child.put(per.toJSONObject());
		}
		
		obj.put("name", getName());
		obj.put("child", child);
		
		return obj.toString();
		
	}
	@Override
	public JSONObject toJSONObject() 
	{
		JSONObject obj = new JSONObject();
		JSONArray child = new JSONArray();
		
		for(RemoteObject per : getAllList())
		{
			child.put(per.toJSONObject());
		}
		
		obj.put("name", getName());
		obj.put("child", child);
		
		return obj;
	}

}
