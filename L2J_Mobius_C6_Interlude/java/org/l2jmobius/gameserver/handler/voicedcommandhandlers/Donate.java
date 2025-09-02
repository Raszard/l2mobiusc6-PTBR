package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import org.l2jmobius.gameserver.donation.DonationManager;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;

public class Donate implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"donate"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		DonationManager.getInstance().showIndexWindow(activeChar);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}