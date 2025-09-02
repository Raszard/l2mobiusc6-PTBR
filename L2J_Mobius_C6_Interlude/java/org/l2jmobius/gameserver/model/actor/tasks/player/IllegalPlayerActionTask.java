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
package org.l2jmobius.gameserver.model.actor.tasks.player;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.enums.IllegalActionPunishmentType;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * Task that handles illegal player actions.
 */
public class IllegalPlayerActionTask implements Runnable
{
	private static final Logger AUDIT_LOGGER = Logger.getLogger("audit");
	
	private final String _message;
	private final IllegalActionPunishmentType _punishment;
	private final Player _actor;
	
	public IllegalPlayerActionTask(Player actor, String message, IllegalActionPunishmentType punishment)
	{
		_message = message;
		_punishment = punishment;
		_actor = actor;
		
		switch (punishment)
		{
			case KICK:
			{
				_actor.sendMessage("Você será expulso por uma ação ilegal, GM informado.");
				break;
			}
			case KICKBAN:
			{
				if (!_actor.isGM())
				{
					_actor.setAccessLevel(-1);
					_actor.setAccountAccesslevel(-1);
				}
				_actor.sendMessage("Você foi banido por uma ação ilegal, GM informado.");
				break;
			}
			case JAIL:
			{
				_actor.sendMessage("Ação ilegal realizada!");
				_actor.sendMessage("Você será teletransportado para a área de consulta do GM e preso.");
				break;
			}
		}
	}
	
	@Override
	public void run()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("AUDIT, ");
		sb.append(_message);
		sb.append(", ");
		sb.append(_actor);
		sb.append(", ");
		sb.append(_punishment);
		AUDIT_LOGGER.info(sb.toString());
		
		if (!_actor.isGM())
		{
			switch (_punishment)
			{
				case CHAT:
				{
					return;
				}
				case KICK:
				{
					_actor.logout(true);
					break;
				}
				case KICKBAN:
				{
					AdminData.broadcastMessageToGMs(_message);
					_actor.logout(true);
					break;
				}
				case JAIL:
				{
					AdminData.broadcastMessageToGMs(_message);
					_actor.setPunishLevel(IllegalActionPunishmentType.JAIL, Config.DEFAULT_PUNISH_PARAM);
					break;
				}
			}
		}
	}
}
