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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author -Wooden-
 */
public class PledgeShowMemberListUpdate extends ServerPacket
{
	private final Player _player;
	private final int _pledgeType;
	private boolean _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _isOnline;
	
	public PledgeShowMemberListUpdate(Player player)
	{
		_player = player;
		_pledgeType = _player.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0;
		}
		else if (_player.isOnline())
		{
			_hasSponsor = _player.isClanLeader();
		}
		else
		{
			_hasSponsor = false;
		}
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_isOnline = _player.isOnline() ? _player.getObjectId() : 0;
	}
	
	public PledgeShowMemberListUpdate(ClanMember member)
	{
		_player = member.getPlayer();
		_name = member.getName();
		_level = member.getLevel();
		_classId = member.getClassId();
		_isOnline = member.isOnline() ? member.getObjectId() : 0;
		_pledgeType = member.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0;
		}
		else if (member.isOnline())
		{
			_hasSponsor = _player.isClanLeader();
		}
		else
		{
			_hasSponsor = false;
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.PLEDGE_SHOW_MEMBER_LIST_UPDATE.writeId(this);
		writeString(_name);
		writeInt(_level);
		writeInt(_classId);
		writeInt(0);
		writeInt(1);
		writeInt(_isOnline);
		writeInt(_pledgeType);
		writeInt(_hasSponsor);
	}
}
