package top.metime.updater.server.memory;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

public class MFolder extends MStorage
{
	private final LinkedList<MStorage> sublist = new LinkedList<>();
	
	public MFolder(String name)
	{
		this.name = name;
	}
	public MFolder(JSONObject ObjString)
	{
		name = ObjString.getString("name");
		JSONArray array = ObjString.getJSONArray("child");
		
		for(int c=0;c<array.length();c++)
		{
			JSONObject obj = array.getJSONObject(c);
			//System.out.println(array.get(c));
			if(obj.has("child"))
			{
				sublist.add(new MFolder(obj));
			}
			else
			{
				sublist.add(new MFile(obj));
			}
			
		}
	}
	
	public void append(MStorage d)
	{
		sublist.add(d);
	}
	
	public LinkedList<MStorage> getAllList()
	{
		return sublist;
	}
	
	@Override
	public String toString() 
	{
		JSONObject obj = new JSONObject();
		JSONArray child = new JSONArray();
		
		for(MStorage per : getAllList())
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
		
		for(MStorage per : getAllList())
		{
			child.put(per.toJSONObject());
		}
		
		obj.put("name", getName());
		obj.put("child", child);
		
		return obj;
	}

}
