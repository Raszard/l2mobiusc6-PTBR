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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Format: (ch)ddddddd d: Number of Inventory Slots d: Number of Warehouse Slots d: Number of Freight Slots (unconfirmed) (200 for a low level dwarf) d: Private Sell Store Slots (unconfirmed) (4 for a low level dwarf) d: Private Buy Store Slots (unconfirmed) (5 for a low level dwarf) d: Dwarven
 * Recipe Book Slots d: Normal Recipe Book Slots
 * @author -Wooden- format from KenM
 */
public class ExStorageMaxCount extends ServerPacket
{
	private final Player _player;
	private final int _inventory;
	private final int _warehouse;
	private final int _freight;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _receipeD;
	private final int _recipe;
	
	public ExStorageMaxCount(Player character)
	{
		_player = character;
		_inventory = _player.getInventoryLimit();
		_warehouse = _player.getWareHouseLimit();
		_privateSell = _player.getPrivateSellStoreLimit();
		_privateBuy = _player.getPrivateBuyStoreLimit();
		_freight = _player.getFreightLimit();
		_receipeD = _player.getDwarfRecipeLimit();
		_recipe = _player.getCommonRecipeLimit();
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_STORAGE_MAX_COUNT.writeId(this);
		writeInt(_inventory);
		writeInt(_warehouse);
		writeInt(_freight);
		writeInt(_privateSell);
		writeInt(_privateBuy);
		writeInt(_receipeD);
		writeInt(_recipe);
	}
}
