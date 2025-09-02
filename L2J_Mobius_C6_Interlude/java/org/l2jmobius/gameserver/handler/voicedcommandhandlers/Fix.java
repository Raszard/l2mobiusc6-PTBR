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
package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import org.l2jmobius.gameserver.ayengine.AySql;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Fix implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"fix",
		"send"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		switch (command)
		{
			case "fix":
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(5);
				String content = HtmCache.getInstance().getHtm("data/html/ayengine/fix/fix.htm");
				html.setHtml(content);
				activeChar.sendPacket(html);
				break;
			}
			case "send":
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(5);
				String content = null;
				if (activeChar.getTarget() != null)
				{
					if (activeChar.getTarget().isNpc())
					{
						Npc npc = (Npc) activeChar.getTarget();
						String[] data = target.split(" - ");
						String type = data[0];
						String reason = data[1];
						if (AySql.getInstance().storeFix(npc.getNpcId(), type, reason, activeChar.getName()))
						{
							content = HtmCache.getInstance().getHtm("data/html/ayengine/fix/thank.htm");
						}
						else
						{
							content = HtmCache.getInstance().getHtm("data/html/ayengine/fix/exist.htm");
						}
					}
					else
					{
						content = HtmCache.getInstance().getHtm("data/html/ayengine/fix/notnpc.htm");
					}
				}
				else
				{
					content = HtmCache.getInstance().getHtm("data/html/ayengine/fix/notnpc.htm");
				}
				html.setHtml(content);
				activeChar.sendPacket(html);
				break;
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
