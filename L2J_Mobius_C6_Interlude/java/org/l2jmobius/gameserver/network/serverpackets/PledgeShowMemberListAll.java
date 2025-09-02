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
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.ServerPackets;

public class PledgeShowMemberListAll extends ServerPacket
{
	private final Clan _clan;
	private final Player _player;
	private final Collection<ClanMember> _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
		_members = _clan.getMembers();
	}
	
	@Override
	public void write()
	{
		_pledgeType = 0;
		writePledge(0);
		for (SubPledge element : _clan.getAllSubPledges())
		{
			_player.sendPacket(new PledgeReceiveSubPledgeCreated(element));
		}
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_player.sendPacket(new PledgeShowMemberListAdd(m));
		}
		// unless this is sent sometimes, the client doesn't recognize the player as the leader
		_player.sendPacket(new UserInfo(_player));
	}
	
	void writePledge(int mainOrSubpledge)
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(this);
		writeInt(mainOrSubpledge); // c5 main clan 0 or any subpledge 1?
		writeInt(_clan.getClanId());
		writeInt(_pledgeType); // c5 - possibly pledge type?
		writeString(_clan.getName());
		writeString(_clan.getLeaderName());
		writeInt(_clan.getCrestId()); // crest id .. is used again
		writeInt(_clan.getLevel());
		writeInt(_clan.getCastleId());
		writeInt(_clan.getHideoutId());
		writeInt(ClanTable.getInstance().getTopRate(_clan.getClanId()));
		writeInt(_clan.getReputationScore()); // was activechar level
		writeInt(0); // 0
		writeInt(0); // 0
		writeInt(_clan.getAllyId());
		writeString(_clan.getAllyName());
		writeInt(_clan.getAllyCrestId());
		writeInt(_clan.isAtWar());
		writeInt(_clan.getSubPledgeMembersCount(_pledgeType));
		boolean yellow;
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			if (m.getPledgeType() == -1)
			{
				yellow = m.getSponsor() != 0;
			}
			else if (m.getPlayer() != null)
			{
				yellow = m.getPlayer().isClanLeader();
			}
			else
			{
				yellow = false;
			}
			writeString(m.getName());
			writeInt(m.getLevel());
			writeInt(m.getClassId());
			writeInt(0);
			writeInt(1);
			writeInt(m.isOnline() || (_player.getObjectId() == m.getObjectId()) ? m.getObjectId() : 0);
			writeInt(yellow);
		}
	}
}
