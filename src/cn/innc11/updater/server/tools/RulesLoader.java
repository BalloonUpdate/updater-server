package cn.innc11.updater.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import cn.innc11.updater.server.excption.FileUnableParseExcption;
import cn.innc11.updater.server.excption.InvalidRuleLocalPathException;
import cn.innc11.updater.server.structure.RemoteObject;
import cn.innc11.updater.server.structure.RuleInstance;
import org.json.JSONException;
import org.json.JSONObject;

public class RulesLoader 
{
	public static HashSet<RuleInstance> loadRules(InputStream inputStream, String filePath) throws IOException, FileUnableParseExcption, InvalidRuleLocalPathException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		HashSet<RuleInstance> rulesBuffer = new HashSet<>();
		
		String tempStr;
		while((tempStr = reader.readLine())!=null)
		{
			tempStr = tempStr.trim();
			
			try {
				JSONObject obj = new JSONObject(tempStr);
				String ruleName = obj.getString("ruleName");
				File localPath = new File(obj.getString("serverPath"));
				
				if(!localPath.exists()||localPath.isFile())
				{
					throw new  InvalidRuleLocalPathException(ruleName, localPath.getAbsolutePath());
				}
				
				RemoteObject.Builder builder = new RemoteObject.Builder(obj);
				rulesBuffer.add(builder.build());
			}
			catch(JSONException ex)
			{
				ex.printStackTrace();
				throw new FileUnableParseExcption(filePath, tempStr);
			}
			
		}
		
		return rulesBuffer;
	}

	
}
