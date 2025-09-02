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
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Sdh(h dddhh [dhhh] d) Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList extends ServerPacket
{
	private final Collection<Item> _items;
	private final String _playerName;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(Player player)
	{
		_items = player.getWarehouse().getItems();
		_playerName = player.getName();
		_money = player.getAdena();
	}
	
	public GMViewWarehouseWithdrawList(Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdena();
	}
	
	@Override
	public void write()
	{
		ServerPackets.GM_VIEW_WAREHOUSE_WITHDRAW_LIST.writeId(this);
		writeString(_playerName);
		writeInt(_money);
		writeShort(_items.size());
		for (Item item : _items)
		{
			writeShort(item.getTemplate().getType1());
			writeInt(item.getObjectId());
			writeInt(item.getItemId());
			writeInt(item.getCount());
			writeShort(item.getTemplate().getType2());
			writeShort(item.getCustomType1());
			switch (item.getTemplate().getType2())
			{
				case ItemTemplate.TYPE2_WEAPON:
				{
					writeInt(item.getTemplate().getBodyPart());
					writeShort(item.getEnchantLevel());
					writeShort(((Weapon) item.getTemplate()).getSoulShotCount());
					writeShort(((Weapon) item.getTemplate()).getSpiritShotCount());
					break;
				}
				case ItemTemplate.TYPE2_SHIELD_ARMOR:
				case ItemTemplate.TYPE2_ACCESSORY:
				case ItemTemplate.TYPE2_PET_WOLF:
				case ItemTemplate.TYPE2_PET_HATCHLING:
				case ItemTemplate.TYPE2_PET_STRIDER:
				case ItemTemplate.TYPE2_PET_BABY:
				{
					writeInt(item.getTemplate().getBodyPart());
					writeShort(item.getEnchantLevel());
					writeShort(0);
					writeShort(0);
					break;
				}
			}
			writeInt(item.getObjectId());
			switch (item.getTemplate().getType2())
			{
				case ItemTemplate.TYPE2_WEAPON:
				{
					if (item.isAugmented())
					{
						writeInt(0x0000FFFF & item.getAugmentation().getAugmentationId());
						writeInt(item.getAugmentation().getAugmentationId() >> 16);
					}
					else
					{
						writeInt(0);
						writeInt(0);
					}
					break;
				}
				case ItemTemplate.TYPE2_SHIELD_ARMOR:
				case ItemTemplate.TYPE2_ACCESSORY:
				case ItemTemplate.TYPE2_PET_WOLF:
				case ItemTemplate.TYPE2_PET_HATCHLING:
				case ItemTemplate.TYPE2_PET_STRIDER:
				case ItemTemplate.TYPE2_PET_BABY:
				{
					writeInt(0);
					writeInt(0);
				}
			}
		}
	}
}
