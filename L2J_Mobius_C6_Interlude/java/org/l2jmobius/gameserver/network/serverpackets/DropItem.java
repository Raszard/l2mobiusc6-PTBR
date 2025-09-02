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

import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * 16 d6 6d c0 4b player id who dropped it ee cc 11 43 object id 39 00 00 00 item id 8f 14 00 00 x b7 f1 00 00 y 60 f2 ff ff z 01 00 00 00 show item-count 1=yes 7a 00 00 00 count . format dddddddd rev 377 ddddddddd rev 417
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DropItem extends ServerPacket
{
	private final Item _item;
	private final int _objectId;
	
	/**
	 * Constructor of the DropItem server packet
	 * @param item : Item designating the item
	 * @param playerObjId : int designating the player ID who dropped the item
	 */
	public DropItem(Item item, int playerObjId)
	{
		_item = item;
		_objectId = playerObjId;
	}
	
	@Override
	public void write()
	{
		ServerPackets.DROP_ITEM.writeId(this);
		writeInt(_objectId);
		writeInt(_item.getObjectId());
		writeInt(_item.getItemId());
		writeInt(_item.getX());
		writeInt(_item.getY());
		writeInt(_item.getZ());
		// only show item count if it is a stackable item
		if (_item.isStackable())
		{
			writeInt(1);
		}
		else
		{
			writeInt(0);
		}
		writeInt(_item.getCount());
		writeInt(1); // unknown
	}
}
