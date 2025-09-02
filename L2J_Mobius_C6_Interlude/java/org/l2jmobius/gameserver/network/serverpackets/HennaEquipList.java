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

import java.util.List;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.network.ServerPackets;

public class HennaEquipList extends ServerPacket
{
	private final Player _player;
	private final List<Henna> _hennaEquipList;
	
	public HennaEquipList(Player player, List<Henna> hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}
	
	@Override
	public void write()
	{
		ServerPackets.HENNA_EQUIP_LIST.writeId(this);
		writeInt(_player.getAdena());
		writeInt(3);
		writeInt(_hennaEquipList.size());
		for (Henna temp : _hennaEquipList)
		{
			// Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(temp.getDyeId())) != null)
			{
				writeInt(temp.getSymbolId()); // symbolid
				writeInt(temp.getDyeId()); // itemid of dye
				writeInt(Henna.getRequiredDyeAmount()); // amount of dyes required
				writeInt(temp.getPrice()); // amount of adenas required
				writeInt(1); // meet the requirement or not
			}
		}
	}
}
