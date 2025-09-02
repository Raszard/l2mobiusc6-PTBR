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

import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

public class RecipeShopSellList extends ServerPacket
{
	private final Player _buyer;
	private final Player _player;
	
	public RecipeShopSellList(Player buyer, Player player)
	{
		_buyer = buyer;
		_player = player;
	}
	
	@Override
	public void write()
	{
		final ManufactureList createList = _player.getCreateList();
		if (createList != null)
		{
			ServerPackets.RECIPE_SHOP_SELL_LIST.writeId(this);
			writeInt(_player.getObjectId());
			writeInt((int) _player.getCurrentMp()); // Creator's MP
			writeInt(_player.getMaxMp()); // Creator's MP
			writeInt(_buyer.getAdena()); // Buyer Adena
			writeInt(createList.size());
			for (ManufactureItem item : createList.getList())
			{
				writeInt(item.getRecipeId());
				writeInt(0); // unknown
				writeInt(item.getCost());
			}
		}
	}
}
