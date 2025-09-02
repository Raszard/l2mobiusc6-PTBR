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

import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Shows the Siege Info<br>
 * <br>
 * packet type id 0xc9<br>
 * format: cdddSSdSdd<br>
 * <br>
 * c = c9<br>
 * d = CastleID<br>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<br>
 * d = Owner ClanID<br>
 * S = Owner ClanName<br>
 * S = Owner Clan LeaderName<br>
 * d = Owner AllyID<br>
 * S = Owner AllyName<br>
 * d = current time (seconds)<br>
 * d = Siege time (seconds) (0 for selectable)<br>
 * d = (UNKNOW) Siege Time Select Related?
 * @author KenM
 */
public class SiegeInfo extends ServerPacket
{
	private final Player _player;
	private final int _residenceId;
	private final int _ownerId;
	private final long _siegeDate;
	private final String _name;
	
	public SiegeInfo(Castle castle, Player player)
	{
		_player = player;
		_residenceId = castle.getCastleId();
		_ownerId = castle.getOwnerId();
		_siegeDate = castle.getSiege().getSiegeDate().getTimeInMillis() / 1000;
		_name = castle.getName();
	}
	
	public SiegeInfo(Fort fort, Player player)
	{
		_player = player;
		_residenceId = fort.getFortId();
		_ownerId = fort.getOwnerId();
		_siegeDate = fort.getSiege().getSiegeDate().getTimeInMillis() / 1000;
		_name = fort.getName();
	}
	
	@Override
	public void write()
	{
		ServerPackets.SIEGE_INFO.writeId(this);
		writeInt(_residenceId);
		writeInt((_ownerId == _player.getClanId()) && _player.isClanLeader());
		writeInt(_ownerId);
		if (_ownerId > 0)
		{
			final Clan owner = ClanTable.getInstance().getClan(_ownerId);
			if (owner != null)
			{
				writeString(owner.getName()); // Clan Name
				writeString(owner.getLeaderName()); // Clan Leader Name
				writeInt(owner.getAllyId()); // Ally ID
				writeString(owner.getAllyName()); // Ally Name
			}
			else
			{
				PacketLogger.warning("Null owner for castle: " + _name);
			}
		}
		else
		{
			writeString("NPC"); // Clan Name
			writeString(""); // Clan Leader Name
			writeInt(0); // Ally ID
			writeString(""); // Ally Name
		}
		writeInt((int) (System.currentTimeMillis() / 1000));
		writeInt((int) _siegeDate);
		writeInt(0); // number of choices?
	}
}
