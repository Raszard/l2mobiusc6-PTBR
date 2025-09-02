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
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * 61 d6 6d c0 4b door id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 00 00 00 00 ?? format dddd rev 377 ID:%d X:%d Y:%d Z:%d ddddd rev 419
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class DoorStatusUpdate extends ServerPacket
{
	private final Door _door;
	private final Player _player;
	
	public DoorStatusUpdate(Door door, Player player)
	{
		_door = door;
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.DOOR_STATUS_UPDATE.writeId(this);
		writeInt(_door.getObjectId());
		writeInt(!_door.isOpen());
		writeInt(_door.getDamage());
		writeInt(_door.isEnemyOf(_player));
		writeInt(_door.getDoorId());
		writeInt(_door.getMaxHp());
		writeInt((int) _door.getCurrentHp());
	}
}
