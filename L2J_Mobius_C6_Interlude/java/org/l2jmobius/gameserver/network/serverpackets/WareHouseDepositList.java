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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * 0x53 WareHouseDepositList dh (h dddhh dhhh d)
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class WareHouseDepositList extends ServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	
	private final Player _player;
	private final int _playerAdena;
	private final List<Item> _items;
	private final int _whType;
	
	public WareHouseDepositList(Player player, int type)
	{
		_player = player;
		_whType = type;
		_playerAdena = _player.getAdena();
		_items = new ArrayList<>();
		for (Item temp : _player.getInventory().getAvailableItems(true))
		{
			_items.add(temp);
		}
		// augmented and shadow items can be stored in private wh
		if (_whType == PRIVATE)
		{
			for (Item temp : player.getInventory().getItems())
			{
				if ((temp != null) && !temp.isEquipped() && (temp.isShadowItem() || temp.isAugmented()))
				{
					_items.add(temp);
				}
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.WARE_HOUSE_DEPOSIT_LIST.writeId(this);
		/*
		 * 0x01-Private Warehouse 0x02-Clan Warehouse 0x03-Castle Warehouse 0x04-Warehouse
		 */
		writeShort(_whType);
		writeInt(_playerAdena);
		writeShort(_items.size());
		for (Item item : _items)
		{
			writeShort(item.getTemplate().getType1()); // item type1 //unconfirmed, works
			writeInt(item.getObjectId()); // unconfirmed, works
			writeInt(item.getItemId()); // unconfirmed, works
			writeInt(item.getCount()); // unconfirmed, works
			writeShort(item.getTemplate().getType2()); // item type2 //unconfirmed, works
			writeShort(0); // ? 100
			writeInt(item.getTemplate().getBodyPart()); // ?
			writeShort(item.getEnchantLevel()); // enchant level -confirmed
			writeShort(0); // ? 300
			writeShort(0); // ? 200
			writeInt(item.getObjectId()); // item id - confimed
			if (item.isAugmented())
			{
				writeInt(0x0000FFFF & item.getAugmentation().getAugmentationId());
				writeInt(item.getAugmentation().getAugmentationId() >> 16);
			}
			else
			{
				writeLong(0);
			}
		}
	}
}
