package top.metime.updater.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;
import top.metime.updater.server.excption.FileUnableParseExcption;
import top.metime.updater.server.excption.InvalidRuleLocalPathException;
import top.metime.updater.server.memory.MFileOrFolder;
import top.metime.updater.server.memory.MRule;

public class RulesLoader 
{

	/**
	 * 此静态方法可以从一个java.io.InputStream加载并读取规则文件
	 *
	 * @param inputStream java.io.InputStream，必要的输入流
	 * @param filePath 为了追踪错误而存在的参数
	 * @return HashSet(MRule)
	 * @throws IOException IO异常
	 * @throws FileUnableParseExcption 文件无法转换异常，通常是文件格式不正确
	 * @throws InvalidRuleLocalPathException 无效的规则异常，通常是localPath不存在
	 */
	public static HashSet<MRule> loadRules(InputStream inputStream, String filePath) throws IOException, FileUnableParseExcption, InvalidRuleLocalPathException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		HashSet<MRule> rulesBuffer = new HashSet<>();
		
		String tempStr;
		while((tempStr = reader.readLine())!=null)
		{
			tempStr = tempStr.trim();
			
			try
			{
				JSONObject obj = new JSONObject(tempStr);
				String ruleName = obj.getString("ruleName");
				File localPath = new File(obj.getString("serverPath"));
				
				if(!localPath.exists()||localPath.isFile())
				{
					throw new  InvalidRuleLocalPathException(ruleName, localPath.getAbsolutePath());
				}
				
				MFileOrFolder.Builder builder = new MFileOrFolder.Builder(obj);
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
