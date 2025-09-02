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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.model.siege.SiegeClan;
import org.l2jmobius.gameserver.network.ServerPackets;

public class Die extends ServerPacket
{
	private final int _objectId;
	private final boolean _fake;
	private boolean _sweepable;
	private boolean _canTeleport;
	private boolean _allowFixedRes;
	private Clan _clan;
	Creature _creature;
	
	public Die(Creature creature)
	{
		_creature = creature;
		if (creature instanceof Player)
		{
			final Player player = creature.getActingPlayer();
			_allowFixedRes = player.getAccessLevel().allowFixedRes();
			_clan = player.getClan();
			_canTeleport = !player.isPendingRevive();
		}
		_objectId = creature.getObjectId();
		_fake = !creature.isDead();
		if (creature instanceof Attackable)
		{
			_sweepable = ((Attackable) creature).isSweepActive();
		}
	}
	
	@Override
	public void write()
	{
		if (_fake)
		{
			return;
		}
		
		ServerPackets.DIE.writeId(this);
		writeInt(_objectId);
		// NOTE:
		// 6d 00 00 00 00 - to nearest village
		// 6d 01 00 00 00 - to hide away
		// 6d 02 00 00 00 - to castle
		// 6d 03 00 00 00 - to siege HQ
		// sweepable
		// 6d 04 00 00 00 - FIXED
		writeInt(_canTeleport); // 6d 00 00 00 00 - to nearest village
		if (_canTeleport && (_clan != null))
		{
			SiegeClan siegeClan = null;
			boolean isInDefense = false;
			final Castle castle = CastleManager.getInstance().getCastle(_creature);
			final Fort fort = FortManager.getInstance().getFort(_creature);
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInDefense = true;
				}
			}
			writeInt(_clan.getHideoutId() > 0); // 6d 01 00 00 00 - to hide away
			writeInt((_clan.getCastleId() > 0) || (_clan.getFortId() > 0) || isInDefense); // 6d 02 00 00 00 - to castle
			writeInt((siegeClan != null) && !isInDefense && !siegeClan.getFlag().isEmpty()); // 6d 03 00 00 00 - to siege HQ
		}
		else
		{
			writeInt(0); // 6d 01 00 00 00 - to hide away
			writeInt(0); // 6d 02 00 00 00 - to castle
			writeInt(0); // 6d 03 00 00 00 - to siege HQ
		}
		writeInt(_sweepable); // sweepable (blue glow)
		writeInt(_allowFixedRes); // 6d 04 00 00 00 - to FIXED
	}
}
