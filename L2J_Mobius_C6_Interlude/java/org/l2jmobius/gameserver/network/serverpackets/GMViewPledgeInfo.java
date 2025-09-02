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

import java.util.Collection;

import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * format SdSS dddddddd d (Sddddd)
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewPledgeInfo extends ServerPacket
{
	private final Clan _clan;
	private final Player _player;
	
	public GMViewPledgeInfo(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
	}
	
	@Override
	public void write()
	{
		final int TOP = ClanTable.getInstance().getTopRate(_clan.getClanId());
		ServerPackets.GM_VIEW_PLEDGE_INFO.writeId(this);
		writeString(_player.getName());
		writeInt(_clan.getClanId());
		writeInt(0);
		writeString(_clan.getName());
		writeString(_clan.getLeaderName());
		writeInt(_clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeInt(_clan.getLevel());
		writeInt(_clan.getCastleId());
		writeInt(_clan.getHideoutId());
		writeInt(TOP);
		writeInt(_clan.getReputationScore());
		writeInt(0);
		writeInt(0);
		writeInt(_clan.getAllyId()); // c2
		writeString(_clan.getAllyName()); // c2
		writeInt(_clan.getAllyCrestId()); // c2
		writeInt(_clan.isAtWar()); // c3
		final Collection<ClanMember> members = _clan.getMembers();
		writeInt(members.size());
		for (ClanMember member : members)
		{
			writeString(member.getName());
			writeInt(member.getLevel());
			writeInt(member.getClassId());
			writeInt(0);
			writeInt(1);
			writeInt(member.isOnline() ? member.getObjectId() : 0);
			writeInt(0);
		}
	}
}
