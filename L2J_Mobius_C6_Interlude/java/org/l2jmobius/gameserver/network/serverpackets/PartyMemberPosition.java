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

import java.util.List;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends ServerPacket
{
	private final Party _party;
	
	public PartyMemberPosition(Party party)
	{
		_party = party;
	}
	
	@Override
	public void write()
	{
		ServerPackets.PARTY_MEMBER_POSITION.writeId(this);
		
		// Sempre pegar os membros atuais do party
		List<Player> members = _party.getPartyMembers();
		writeInt(members.size());
		
		for (Player member : members)
		{
			if (member != null)
			{
				final Location loc = member.getLocation();
				writeInt(member.getObjectId());
				writeInt(loc.getX());
				writeInt(loc.getY());
				writeInt(loc.getZ());
			}
		}
	}
}
