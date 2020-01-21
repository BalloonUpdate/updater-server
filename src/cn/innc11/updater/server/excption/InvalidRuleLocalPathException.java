package cn.innc11.updater.server.excption;

/**
 * 该类表示一个规则的本地路径是无效的（不存在）
 *
 * @author innc-table
 */
public class InvalidRuleLocalPathException extends Exception
{
	public InvalidRuleLocalPathException(String ruleName, String localPath)
	{
		super("规则"+ruleName+"的本地路径<"+localPath+">"+"找不到或者不存在");
	}
}
