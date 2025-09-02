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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

public class SellListProcure extends ServerPacket
{
	private final Player _player;
	private final int _money;
	private final Map<Item, Integer> _sellList = new HashMap<>();
	private List<CropProcure> _procureList = new ArrayList<>();
	private final int _castle;
	
	public SellListProcure(Player player, int castleId)
	{
		_money = player.getAdena();
		_player = player;
		_castle = castleId;
		_procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for (CropProcure c : _procureList)
		{
			final Item item = _player.getInventory().getItemByItemId(c.getId());
			if ((item != null) && (c.getAmount() > 0) && (item.getItemLocation() == ItemLocation.INVENTORY))
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.SELL_LIST_PROCURE.writeId(this);
		writeInt(_money); // money
		writeInt(0); // lease ?
		writeShort(_sellList.size()); // list size
		for (Entry<Item, Integer> entry : _sellList.entrySet())
		{
			final Item item = entry.getKey();
			writeShort(item.getTemplate().getType1());
			writeInt(item.getObjectId());
			writeInt(item.getItemId());
			writeInt(entry.getValue()); // count
			writeShort(item.getTemplate().getType2());
			writeShort(0); // unknown
			writeInt(0); // price, you shouldnt get any adena for crops, only raw materials
		}
	}
}
