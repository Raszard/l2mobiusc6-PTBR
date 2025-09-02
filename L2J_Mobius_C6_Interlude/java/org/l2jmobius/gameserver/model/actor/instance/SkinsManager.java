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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.skin.SkinTable;
import org.l2jmobius.gameserver.skin.SkinUtils;

public class SkinsManager extends Folk
{
	
	public Item[] _skinsPaperdoll = new Item[0x12];
	
	public SkinsManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final Inventory _inventory = player.getInventory();
		if (command.startsWith("itemlist"))
		{
			// _inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_CHEST, new Item(0, 6379));
			String[] data = command.substring(9).split(" - ");
			int page = Integer.parseInt(data[0]);
			int type = Integer.parseInt(data[1]);
			itemList(player, page, type);
		}
		else if (command.startsWith("remove"))
		{
			if (!SkinUtils.allNull(_inventory.getSkinsPaperdoll()))
			{
				for (int i = 0; i < SkinUtils.getTypesArray().length; i++)
				{
					_inventory.setSkinsPaperdoll(SkinUtils.getTypesArray()[i], null);
					_skinsPaperdoll[SkinUtils.getTypesArray()[i]] = null;
				}
				SkinTable.getInstance().deleteStore(player.getObjectId());
				_inventory.reloadEquippedItems();
				player.broadcastUserInfo();
			}
		}
		else if (command.startsWith("change"))
		{
			try
			{
				String[] data = command.substring(7).split(" - ");
				int itemId = Integer.parseInt(data[0]);
				int type = Integer.parseInt(data[1]);
				Item item = player.getInventory().getItemByObjectId(itemId);
				int bodyPart = SkinUtils.getBodyPart(type);
				Item dhair = _skinsPaperdoll[Inventory.PAPERDOLL_FACE];
				if ((item != null) && ((item.getItemId() == 9302) || (item.getItemId() == 9303)))
				{
					final Item placeholder = Item.restoreFromDb(268477702);
					_skinsPaperdoll[Inventory.PAPERDOLL_FACE] = placeholder;
					_skinsPaperdoll[Inventory.PAPERDOLL_HAIR] = placeholder;
				}
				if ((item != null) && ((item.getItemId() != 9302) || (item.getItemId() != 9303)) && ((dhair != null) && (dhair.getItemId() == 9350)))
				{
					_skinsPaperdoll[Inventory.PAPERDOLL_FACE] = null;
					_skinsPaperdoll[Inventory.PAPERDOLL_HAIR] = null;
				}
				if (((bodyPart == ItemTemplate.SLOT_FACE) || (bodyPart == ItemTemplate.SLOT_HAIR)) && ((dhair != null) && (dhair.getItemId() == 9350)))
				{
					setSkin(_inventory, player, true);
				}
				else
				{
					_skinsPaperdoll[SkinUtils.getSkinPositionNumber(bodyPart)] = item;
					setSkin(_inventory, player, true);
				}
			}
			catch (Exception e)
			{
				for (int i = 0; i < SkinUtils.getTypesArray().length; i++)
				{
					_skinsPaperdoll[SkinUtils.getTypesArray()[i]] = null;
				}
			}
		}
		else if (command.startsWith("pierceremove"))
		{
			int type = Integer.parseInt(command.substring(13));
			int bodyPart = SkinUtils.getBodyPart(type);
			Item dhair = _skinsPaperdoll[Inventory.PAPERDOLL_FACE];
			if ((bodyPart == ItemTemplate.SLOT_CHEST) && ((_skinsPaperdoll[Inventory.PAPERDOLL_CHEST].getItemId() == 9302) || (_skinsPaperdoll[Inventory.PAPERDOLL_CHEST].getItemId() == 9303)))
			{
				_skinsPaperdoll[Inventory.PAPERDOLL_FACE] = null;
				_skinsPaperdoll[Inventory.PAPERDOLL_HAIR] = null;
			}
			if (((bodyPart == ItemTemplate.SLOT_FACE) || (bodyPart == ItemTemplate.SLOT_HAIR)) && ((dhair != null) && (dhair.getItemId() == 9350)))
			{
				setSkin(_inventory, player, true);
			}
			else
			{
				_skinsPaperdoll[SkinUtils.getSkinPositionNumber(bodyPart)] = null;
				setSkin(_inventory, player, true);
			}
		}
		else if (command.startsWith("back"))
		{
			showChatWindow(player);
		}
		else if (command.startsWith("setskin"))
		{
			setSkin(_inventory, player, false);
		}
		else if (command.startsWith("save"))
		{
			if (!player.reduceAdena("Skins", 10000, player.getLastFolkNPC(), true))
			{
				return;
			}
			setBodyPart(_inventory);
			_inventory.reloadEquippedItems();
			SkinTable.getInstance().deleteStore(player.getObjectId());
			SkinTable.getInstance().store(_inventory, player.getObjectId());
			player.broadcastUserInfo();
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void setSkin(Inventory _inventory, Player player, Boolean bool)
	{
		final var npcHtml = new NpcHtmlMessage(getObjectId());
		npcHtml.setFile("data/html/alpha/skins/skins.htm");
		if (!bool)
		{
			if (!SkinUtils.allNull(_inventory.getSkinsPaperdoll()))
			{
				_skinsPaperdoll = _inventory.getSkinsPaperdoll();
			}
		}
		for (int i = 0; i < SkinUtils.getTypesArray().length; i++)
		{
			if (_skinsPaperdoll[SkinUtils.getTypesArray()[i]] != null)
			{
				// ItemTable.getInstance().getTemplate(temp.getItemId()).getBodyPart()
				String icon = ItemTable.getInstance().getTemplate(_skinsPaperdoll[SkinUtils.getTypesArray()[i]].getItemId()).getIcon();
				if (ItemTable.getInstance().getTemplate(_skinsPaperdoll[SkinUtils.getTypesArray()[i]].getItemId()).getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				{
					String iconU = icon.replace("_ul_", "_u_");
					String iconL = icon.replace("_ul_", "_l_");
					npcHtml.replace("%legs%", iconL);
					npcHtml.replace("%width6%", "32");
					npcHtml.replace("%height6%", "32");
					npcHtml.replace(SkinUtils.getSubArray()[i], iconU);
					npcHtml.replace("%width" + (i + 1) + "%", "32");
					npcHtml.replace("%height" + (i + 1) + "%", "32");
				}
				else
				{
					npcHtml.replace(SkinUtils.getSubArray()[i], icon);
					npcHtml.replace("%width" + (i + 1) + "%", "32");
					npcHtml.replace("%height" + (i + 1) + "%", "32");
				}
			}
			else
			{
				npcHtml.replace(SkinUtils.getSubArray()[i], SkinUtils.getIconArray()[i]);
				npcHtml.replace("%width" + (i + 1) + "%", "34");
				npcHtml.replace("%height" + (i + 1) + "%", "34");
			}
			npcHtml.replace("%bypass" + (i + 1) + "%", "bypass -h npc_" + getObjectId() + "_" + SkinUtils.getBypassArray()[i]);
		}
		npcHtml.replace("%save%", "bypass -h npc_" + getObjectId() + "_save");
		npcHtml.replace("%back%", "bypass -h npc_" + getObjectId() + "_back");
		player.sendPacket(npcHtml);
	}
	
	private void itemList(Player player, int page, int type)
	{
		final int ITEMS_PER_PAGE = 7;
		final var npcHtml = new NpcHtmlMessage(getObjectId());
		npcHtml.setFile("data/html/alpha/skins/itemlist.htm");
		Map<Integer, ArrayList<Item>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<Item> temp = new ArrayList<>();
		for (Item item : player.getInventory().getItems())
		{
			int bodyPart = ItemTable.getInstance().getTemplate(item.getItemId()).getBodyPart();
			if (bodyPart == ItemTemplate.SLOT_FULL_ARMOR)
			{
				bodyPart = ItemTemplate.SLOT_CHEST;
			}
			if ((bodyPart != SkinUtils.getBodyPart(type)))
			{
				continue;
			}
			temp.add(item);
			if (++counter != 7)
			{
				continue;
			}
			items.put(curr, temp);
			temp = new ArrayList<>();
			++curr;
			counter = 0;
		}
		items.put(curr, temp);
		if (!items.containsKey(page))
		{
			this.showChatWindow(player);
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		npcHtml.replace("%title%", "Meus Itens");
		for (Item item : items.get(page))
		{
			String itemName = item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() + " " + item.getItemName() : item.getItemName();
			npcHtml.replace("%icon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			npcHtml.replace("%itemName" + index + "%", "<a action=\"bypass -h npc_" + getObjectId() + "_change " + item.getObjectId() + " - " + type + "\">" + itemName + "</a>");
			npcHtml.replace("%cost" + index + "%", "Quantidade: " + item.getCount());
			// StringUtil.append(sb, "<td fixwidth=71><button value=\"Comprar\" action=\"bypass -h npc_" + this.getObjectId() + "_buy " + item.getAuctionId() + "\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">");
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				npcHtml.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				npcHtml.replace("%cost" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
		}
		npcHtml.replace("%pages%", String.valueOf(page));
		if ((items.keySet().size() > 1))
		{
			int nextPage = page + 1;
			if (nextPage <= items.keySet().size())
			{
				if (items.get(nextPage).size() == 0)
				{
					npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
				}
			}
			if (page > 1)
			{
				// L2UI_CH3.prev1_over - L2UI_CH3.prev1_down - L2UI_CH3.Basic_Outline1
				npcHtml.replace("%prev%", "<button action=\"bypass -h npc_" + this.getObjectId() + "_itemlist " + (page - 1) + " - " + type + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > page)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				npcHtml.replace("%next%", "<button action=\"bypass -h npc_" + this.getObjectId() + "_itemlist " + (page + 1) + " - " + type + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
				npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
			else
			{
				npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
		}
		else
		{
			npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		}
		npcHtml.replace("%remove%", "bypass -h npc_" + getObjectId() + "_pierceremove " + type);
		npcHtml.replace("%back%", "bypass -h npc_" + getObjectId() + "_setskin");
		player.sendPacket(npcHtml);
	}
	
	private void setBodyPart(Inventory _inventory)
	{
		for (int i = 0; i < 18; i++)
		{
			_inventory.setSkinsPaperdoll(i, _skinsPaperdoll[i]);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/alpha/skins/" + this.getNpcId() + ".htm";
		if (val != 0)
		{
			name = "data/html/alpha/skins/" + this.getNpcId() + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		player.sendPacket(html);
	}
}
