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

import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * sample 63 01 00 00 00 count c1 b2 e0 4a object id 54 00 75 00 65 00 73 00 64 00 61 00 79 00 00 00 name 5a 01 00 00 hp 5a 01 00 00 hp max 89 00 00 00 mp 89 00 00 00 mp max 0e 00 00 00 level 12 00 00 00 class 00 00 00 00 01 00 00 00 format d (dSdddddddd)
 * @version $Revision: 1.6.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public class PartySmallWindowAll extends ServerPacket
{
	private final Party _party;
	private final Player _exclude;
	private final int _dist;
	private final int _leaderObjId;
	
	public PartySmallWindowAll(Player exclude, Party party)
	{
		_exclude = exclude;
		_party = party;
		_leaderObjId = _party.getPartyLeaderOID();
		_dist = _party.getLootDistribution();
	}
	
	@Override
	public void write()
	{
		ServerPackets.PARTY_SMALL_WINDOW_ALL.writeId(this);
		writeInt(_leaderObjId);
		writeInt(_dist);
		writeInt(_party.getMemberCount() - 1);
		for (Player member : _party.getPartyMembers())
		{
			if ((member != null) && (member != _exclude))
			{
				writeInt(member.getObjectId());
				writeString(member.getName());
				writeInt((int) member.getCurrentCp()); // c4
				writeInt(member.getMaxCp()); // c4
				writeInt((int) member.getCurrentHp());
				writeInt(member.getMaxHp());
				writeInt((int) member.getCurrentMp());
				writeInt(member.getMaxMp());
				writeInt(member.getLevel());
				writeInt(member.getClassId().getId());
				writeInt(0); // writeD(1); ??
				writeInt(member.getRace().ordinal());
			}
		}
	}
}
