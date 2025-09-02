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

import org.l2jmobius.gameserver.model.StoreTradeList;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Format: c ddh[hdddhhd] c - id (0xE8) d - money d - manor id h - size [ h - item type 1 d - object id d - item id d - count h - item type 2 h d - price ]
 * @author l3x
 */
public class BuyListSeed extends ServerPacket
{
	private final int _manorId;
	private List<Item> _list = new ArrayList<>();
	private final int _money;
	
	public BuyListSeed(StoreTradeList list, int manorId, int currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}
	
	@Override
	public void write()
	{
		ServerPackets.BUY_LIST_SEED.writeId(this);
		writeInt(_money); // current money
		writeInt(_manorId); // manor id
		writeShort(_list.size()); // list length
		for (Item item : _list)
		{
			writeShort(4); // item->type1
			writeInt(0); // objectId
			writeInt(item.getItemId()); // item id
			writeInt(item.getCount()); // item count
			writeShort(4); // item->type2
			writeShort(0); // unknown :)
			writeInt(item.getPriceToSell()); // price
		}
	}
}
