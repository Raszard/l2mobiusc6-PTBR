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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.4.2.3.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class SellList extends ServerPacket
{
	private final Player _player;
	private final int _money;
	private final List<Item> _selllist = new ArrayList<>();
	
	public SellList(Player player)
	{
		_player = player;
		_money = _player.getAdena();
		for (Item item : _player.getInventory().getItems())
		{
			if ((item != null) && !item.isEquipped() && // Not equipped
				(item.getItemLocation() == ItemLocation.INVENTORY) && // exploit fix
				item.getTemplate().isSellable() && // Item is sellable
				(item.getTemplate().getItemId() != 57) && // Adena is not sellable
				((_player.getPet() == null) || // Pet not summoned or
					(item.getObjectId() != _player.getPet().getControlItemId()))) // Pet is summoned and not the item that summoned the pet
			{
				_selllist.add(item);
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.SELL_LIST.writeId(this);
		writeInt(_money);
		writeInt(0);
		writeShort(_selllist.size());
		for (Item item : _selllist)
		{
			writeShort(item.getTemplate().getType1());
			writeInt(item.getObjectId());
			writeInt(item.getItemId());
			writeInt(item.getCount());
			writeShort(item.getTemplate().getType2());
			writeShort(0);
			writeInt(item.getTemplate().getBodyPart());
			writeShort(item.getEnchantLevel());
			writeShort(0);
			writeShort(0);
			writeInt(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : item.getTemplate().getReferencePrice() / 2);
		}
	}
}