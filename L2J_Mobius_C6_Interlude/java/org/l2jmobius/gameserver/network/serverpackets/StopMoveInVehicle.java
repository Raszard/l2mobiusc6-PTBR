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
 * @author Maktakien
 */
public class StopMoveInVehicle extends ServerPacket
{
	private final Player _player;
	private final int _boatId;
	
	/**
	 * @param player
	 * @param boatid
	 */
	public StopMoveInVehicle(Player player, int boatid)
	{
		_player = player;
		_boatId = boatid;
	}
	
	@Override
	public void write()
	{
		ServerPackets.STOP_MOVE_IN_VEHICLE.writeId(this);
		writeInt(_player.getObjectId());
		writeInt(_boatId);
		writeInt(_player.getBoatPosition().getX());
		writeInt(_player.getBoatPosition().getY());
		writeInt(_player.getBoatPosition().getZ());
		writeInt(_player.getHeading());
	}
}
