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

import java.util.Collection;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewItemList extends ServerPacket
{
	private final Collection<Item> _items;
	private final Player _player;
	private final String _playerName;
	
	public GMViewItemList(Player player)
	{
		_items = player.getInventory().getItems();
		_playerName = player.getName();
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.GM_VIEW_ITEM_LIST.writeId(this);
		writeString(_playerName);
		writeInt(_player.getInventoryLimit()); // inventory limit
		writeShort(1); // show window ??
		writeShort(_items.size());
		for (Item temp : _items)
		{
			if ((temp == null) || (temp.getTemplate() == null))
			{
				continue;
			}
			writeShort(temp.getTemplate().getType1());
			writeInt(temp.getObjectId());
			writeInt(temp.getItemId());
			writeInt(temp.getCount());
			writeShort(temp.getTemplate().getType2());
			writeShort(temp.getCustomType1());
			writeShort(temp.isEquipped());
			writeInt(temp.getTemplate().getBodyPart());
			writeShort(temp.getEnchantLevel());
			writeShort(temp.getCustomType2());
			if (temp.isAugmented())
			{
				writeInt(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeInt(0);
			}
			writeInt(-1); // C6
		}
	}
}
