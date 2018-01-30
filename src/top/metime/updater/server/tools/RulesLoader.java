package top.metime.updater.server.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;
import top.metime.updater.server.memory.MStorage;
import top.metime.updater.server.memory.MRule;

public class RulesLoader 
{
	public static HashSet<MRule> loadRules(InputStream inputStream) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		HashSet<MRule> rulesBuffer = new HashSet<>();
		
		String tempStr;
		while((tempStr = reader.readLine())!=null)
		{
			tempStr = tempStr.trim();
			
			try
			{
				MStorage.Builder builder = new MStorage.Builder(new JSONObject(tempStr));
				rulesBuffer.add(builder.getRule());
			}
			catch(JSONException ex)
			{
				ex.printStackTrace();
				return null;
			}
			
		}
		
		return rulesBuffer;
	}

	
}
