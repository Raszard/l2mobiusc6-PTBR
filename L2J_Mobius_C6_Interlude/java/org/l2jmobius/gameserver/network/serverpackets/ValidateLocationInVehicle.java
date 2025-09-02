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

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

public class ValidateLocationInVehicle extends ServerPacket
{
	private int _boat = 1343225858;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _playerObj;
	
	public ValidateLocationInVehicle(Creature creature)
	{
		_playerObj = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		_heading = creature.getHeading();
		_boat = ((Player) creature).getBoat().getObjectId();
	}
	
	@Override
	public void write()
	{
		ServerPackets.VALIDATE_LOCATION_IN_VEHICLE.writeId(this);
		writeInt(_playerObj);
		writeInt(_boat);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		writeInt(_heading);
	}
}