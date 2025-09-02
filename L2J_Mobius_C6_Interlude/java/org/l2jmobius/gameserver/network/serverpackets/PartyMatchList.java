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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jmobius.gameserver.network.ServerPackets;

public class PartyMatchList extends ServerPacket
{
	private final Player _player;
	private final int _loc;
	private final int _lim;
	private final List<PartyMatchRoom> _rooms;
	
	public PartyMatchList(Player player, int auto, int location, int limit)
	{
		_player = player;
		_loc = location;
		_lim = limit;
		_rooms = new ArrayList<>();
	}
	
	@Override
	public void write()
	{
		for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if ((room.getMembers() < 1) || (room.getOwner() == null) || !room.getOwner().isOnline() || (room.getOwner().getPartyRoom() != room.getId()))
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			if ((_loc > 0) && (_loc != room.getLocation()))
			{
				continue;
			}
			if ((_lim == 0) && ((_player.getLevel() < room.getMinLevel()) || (_player.getLevel() > room.getMaxLevel())))
			{
				continue;
			}
			_rooms.add(room);
		}
		int count = 0;
		final int size = _rooms.size();
		
		ServerPackets.PARTY_MATCH_LIST.writeId(this);
		if (size > 0)
		{
			writeInt(1);
		}
		else
		{
			writeInt(0);
		}
		writeInt(_rooms.size());
		while (size > count)
		{
			writeInt(_rooms.get(count).getId());
			writeString(_rooms.get(count).getTitle());
			writeInt(_rooms.get(count).getLocation());
			writeInt(_rooms.get(count).getMinLevel());
			writeInt(_rooms.get(count).getMaxLevel());
			writeInt(_rooms.get(count).getMembers());
			writeInt(_rooms.get(count).getMaxMembers());
			writeString(_rooms.get(count).getOwner().getName());
			count++;
		}
	}
}