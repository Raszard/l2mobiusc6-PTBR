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

import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * dd d(dd) d(ddd)
 */
public class RecipeShopManageList extends ServerPacket
{
	private final Player _seller;
	private final boolean _isDwarven;
	private Collection<RecipeList> _recipes;
	
	public RecipeShopManageList(Player seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;
		if (_isDwarven && _seller.hasDwarvenCraft())
		{
			_recipes = _seller.getDwarvenRecipeBook();
		}
		else
		{
			_recipes = _seller.getCommonRecipeBook();
		}
		// clean previous recipes
		if (_seller.getCreateList() != null)
		{
			final ManufactureList list = _seller.getCreateList();
			for (ManufactureItem item : list.getList())
			{
				if (item.isDwarven() != _isDwarven)
				{
					list.getList().remove(item);
				}
			}
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.RECIPE_SHOP_MANAGE_LIST.writeId(this);
		writeInt(_seller.getObjectId());
		writeInt(_seller.getAdena());
		writeInt(!_isDwarven);
		if (_recipes == null)
		{
			writeInt(0);
		}
		else
		{
			writeInt(_recipes.size()); // number of items in recipe book
			int count = 0;
			for (RecipeList recipe : _recipes)
			{
				count++;
				writeInt(recipe.getId());
				writeInt(count);
			}
		}
		if (_seller.getCreateList() == null)
		{
			writeInt(0);
		}
		else
		{
			final ManufactureList list = _seller.getCreateList();
			writeInt(list.size());
			for (ManufactureItem item : list.getList())
			{
				writeInt(item.getRecipeId());
				writeInt(0);
				writeInt(item.getCost());
			}
		}
	}
}
