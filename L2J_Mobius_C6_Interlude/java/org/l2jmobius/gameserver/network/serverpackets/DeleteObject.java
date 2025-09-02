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

import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * sample 0000: 1e 9b da 12 40 ....@ format d
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DeleteObject extends ServerPacket
{
	private final int _objectId;
	
	public DeleteObject(WorldObject obj)
	{
		_objectId = obj.getObjectId();
	}
	
	public DeleteObject(int objId)
	{
		_objectId = objId;
	}
	
	@Override
	public void write()
	{
		ServerPackets.DELETE_OBJECT.writeId(this);
		writeInt(_objectId);
		writeInt(0); // c2
	}
}