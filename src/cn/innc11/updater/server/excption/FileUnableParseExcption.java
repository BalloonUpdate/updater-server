package cn.innc11.updater.server.excption;

/**
 * 该类代表文件格式错误无法解析的异常
 *
 * @author innc-table
 */
public class FileUnableParseExcption extends Exception
{
	public FileUnableParseExcption(String file, String reson)
	{
		super("文件无法解析，原因："+reson+"，文件："+file);
	}
}
