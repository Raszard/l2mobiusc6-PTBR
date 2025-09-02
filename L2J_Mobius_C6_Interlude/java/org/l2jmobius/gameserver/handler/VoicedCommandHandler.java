/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.AutoWalk;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.BankingCmd;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.BuffShop;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.Donate;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.ExperienceGain;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.Fix;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.Indicate;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.OfflineShop;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.Online;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.StatsCmd;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.VoiceCommand;
import org.l2jmobius.gameserver.handler.voicedcommandhandlers.Wedding;

public class VoicedCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(VoicedCommandHandler.class.getName());
	
	private final Map<String, IVoicedCommandHandler> _datatable;
	
	protected VoicedCommandHandler()
	{
		_datatable = new HashMap<>();
		
		registerVoicedCommandHandler(new VoiceCommand());
		
		if (Config.BANKING_SYSTEM_ENABLED)
		{
			registerVoicedCommandHandler(new BankingCmd());
		}
		
		if (Config.ALLOW_WEDDING)
		{
			registerVoicedCommandHandler(new Wedding());
		}
		
		if (Config.ALLOW_SIMPLE_STATS_VIEW || Config.ALLOW_DETAILED_STATS_VIEW)
		{
			registerVoicedCommandHandler(new StatsCmd());
		}
		
		if (Config.ALLOW_ONLINE_VIEW)
		{
			registerVoicedCommandHandler(new Online());
		}
		
		if (Config.OFFLINE_TRADE_ENABLE && Config.OFFLINE_COMMAND2)
		{
			registerVoicedCommandHandler(new OfflineShop());
		}
		
		if (Config.ENABLE_EXP_GAIN_COMMANDS)
		{
			registerVoicedCommandHandler(new ExperienceGain());
		}
		
		registerVoicedCommandHandler(new Donate());
		
		registerVoicedCommandHandler(new Fix());
		
		registerVoicedCommandHandler(new Indicate());
		
		registerVoicedCommandHandler(new AutoWalk());
		
		LOGGER.info("VoicedCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		final String[] ids = handler.getVoicedCommandList();
		for (String id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(' ') != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(' '));
		}
		return _datatable.get(command);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler INSTANCE = new VoicedCommandHandler();
	}
}