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

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.multisell.MultiSellEntry;
import org.l2jmobius.gameserver.model.multisell.MultiSellIngredient;
import org.l2jmobius.gameserver.model.multisell.MultiSellListContainer;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MultiSellList extends ServerPacket
{
	protected int _listId;
	protected int _page;
	protected int _finished;
	protected MultiSellListContainer _list;
	
	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_list = list;
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}
	
	@Override
	public void write()
	{
		// [ddddd] [dchh] [hdhdh] [hhdh]
		ServerPackets.MULTI_SELL_LIST.writeId(this);
		writeInt(_listId); // list id
		writeInt(_page); // page
		writeInt(_finished); // finished
		writeInt(0x28); // size of pages
		writeInt(_list == null ? 0 : _list.getEntries().size()); // list length
		if (_list != null)
		{
			for (MultiSellEntry ent : _list.getEntries())
			{
				writeInt(ent.getEntryId());
				writeInt(0); // C6
				writeInt(0); // C6
				writeByte(1);
				writeShort(ent.getProducts().size());
				writeShort(ent.getIngredients().size());
				for (MultiSellIngredient i : ent.getProducts())
				{
					writeShort(i.getItemId());
					writeInt(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					writeShort(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					writeInt(i.getItemCount());
					writeShort(i.getEnchantmentLevel()); // enchtant level
					writeInt(0); // C6
					writeInt(0); // C6
				}
				for (MultiSellIngredient i : ent.getIngredients())
				{
					final int items = i.getItemId();
					int typeE = 65335;
					if ((items != 65336) && (items != 65436))
					{
						typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					}
					writeShort(items); // ID
					writeShort(typeE);
					writeInt(i.getItemCount()); // Count
					writeShort(i.getEnchantmentLevel()); // Enchant Level
					writeInt(0); // C6
					writeInt(0); // C6
				}
			}
		}
	}
}
