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
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadUserInfo extends ServerPacket
{
	private final int _side;
	private final Player _player;
	
	/**
	 * @param player
	 * @param side (1 = right, 2 = left)
	 */
	public ExOlympiadUserInfo(Player player, int side)
	{
		_player = player;
		_side = side;
	}
	
	@Override
	public void write()
	{
		if (_player == null)
		{
			return;
		}
		
		ServerPackets.EX_OLYMPIAD_USER_INFO.writeId(this);
		writeByte(_side);
		writeInt(_player.getObjectId());
		writeString(_player.getName());
		writeInt(_player.getClassId().getId());
		writeInt((int) _player.getCurrentHp());
		writeInt(_player.getMaxHp());
		writeInt((int) _player.getCurrentCp());
		writeInt(_player.getMaxCp());
	}
}
