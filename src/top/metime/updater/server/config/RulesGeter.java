package top.metime.updater.server.config;

import java.util.HashSet;
import top.metime.updater.server.memory.MRule;

public interface RulesGeter 
{
	public HashSet<MRule> getRules();
}
