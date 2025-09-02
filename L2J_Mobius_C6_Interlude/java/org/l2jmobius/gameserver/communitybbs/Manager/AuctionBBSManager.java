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
package org.l2jmobius.gameserver.communitybbs.Manager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.auction.AuctionItem;
import org.l2jmobius.gameserver.auction.AuctionTable;
import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.Armor;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class AuctionBBSManager extends BaseBBSManager
{
	
	private static final String AUCTION_BUY = "INSERT INTO items VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	protected AuctionBBSManager()
	{
	}
	
	public static AuctionBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.startsWith("auction"))
		{
			try
			{
				String[] data = command.substring(8).split(" - ");
				int page = Integer.parseInt(data[0]);
				String search = data[1];
				int type = Integer.parseInt(data[2]);
				this.showAuction(activeChar, page, search, type);
			}
			catch (Exception e)
			{
				// this.showChatWindow(activeChar);
				activeChar.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
		else if (command.startsWith("back"))
		{
			String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/auction.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("info"))
		{
			itemInfo(activeChar, command);
		}
		else if (command.startsWith("buy"))
		{
			buyItem(activeChar, command);
		}
		else if (command.startsWith("addit2"))
		{
			try
			{
				if ((activeChar.getInventory().getItemByItemId(9500) != null) || (activeChar.getInventory().getItemByItemId(9501) != null) || (activeChar.getInventory().getItemByItemId(9502) != null))
				{
					long contador = AuctionTable.getInstance().getItems().stream().filter(item -> item.getOwnerId() == activeChar.getObjectId()).count();
					int contadorInt = (int) contador;
					if (((activeChar.getInventory().getItemByItemId(9500) != null) && (contadorInt < 5)) || ((activeChar.getInventory().getItemByItemId(9501) != null) && (contadorInt < 10)) || ((activeChar.getInventory().getItemByItemId(9502) != null) && (contadorInt < 15)))
					{
						String[] data = command.substring(7).split(" ");
						int itemId = Integer.parseInt(data[0]);
						String costitemtype = data[1];
						int costCount = Integer.parseInt(data[2]);
						int itemAmount = Integer.parseInt(data[3]);
						if (activeChar.getInventory().getItemByObjectId(itemId) == null)
						{
							activeChar.sendMessage("Item inválido. Por favor, tente novamente.");
							return;
						}
						if (activeChar.getInventory().getItemByObjectId(itemId).getCount() < itemAmount)
						{
							activeChar.sendMessage("Item inválido. Por favor, tente novamente.");
							return;
						}
						if (!activeChar.getInventory().getItemByObjectId(itemId).isTradeable())
						{
							activeChar.sendMessage("Item inválido. Por favor, tente novamente.");
							return;
						}
						int costId = 0;
						if (costitemtype.equals("Adena"))
						{
							costId = 57;
						}
						else if (costitemtype.equals("Prestígio"))
						{
							costId = 9503;
						}
						else if (costitemtype.equals("Kali"))
						{
							costId = 9509;
						}
						AuctionTable.getInstance().addItem(new AuctionItem(0, activeChar.getObjectId(), activeChar.getInventory().getItemByObjectId(itemId).getItemId(), itemAmount, activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel(), costId, costCount));
						activeChar.destroyItem("auction", itemId, itemAmount, null, true);
						activeChar.sendPacket(new InventoryUpdate());
						activeChar.sendMessage("Você adicionou um item à venda na Casa de Leilões.");
						AuctionTable.getInstance().updateTable();
						this.parseCmd("myitems 1 - 1 - 0", activeChar);
					}
					else
					{
						this.parseCmd("myitems 1 - 1 - 0", activeChar);
						activeChar.sendMessage("Limite atingido. Melhore seu Cupom para vender mais.");
					}
				}
				else
				{
					this.parseCmd("myitems 1 - 1 - 0", activeChar);
					activeChar.sendMessage("Item inválido. você não tem o Cupom do Leilão.");
				}
			}
			catch (Exception e)
			{
				this.parseCmd("myitems 1 - 1 - 0", activeChar);
				activeChar.sendMessage("Valor inválido. Por favor, tente novamente.");
				return;
			}
		}
		else if (command.startsWith("myitems"))
		{
			
			String[] data = command.substring(8).split(" - ");
			int page = Integer.parseInt(data[0]);
			int addpage = Integer.parseInt(data[1]);
			int objId = 0;
			if (data[2] != null)
			{
				objId = Integer.parseInt(data[2]);
			}
			this.showMyItems(activeChar, page, addpage, objId);
		}
		else if (command.startsWith("remove"))
		{
			removeItem(activeChar, command);
		}
		else if (command.startsWith("multisell"))
		{
			final int listId = Integer.parseInt(command.substring(9).trim());
			MultisellData.getInstance().separateAndSend(listId, activeChar, false, 0);
			String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/auction.htm");
			separateAndSend(content, activeChar);
		}
		else
		{
			super.parseCmd(command, activeChar);
		}
	}
	
	private void itemInfo(Player activeChar, String command)
	{
		String[] data = command.substring(5).split(" - ");
		int auctionId = Integer.parseInt(data[0]);
		AuctionItem itemAu = AuctionTable.getInstance().getItem(auctionId);
		int itemId = itemAu.getItemId();
		
		ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		if ((item.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR) || (item.getType2() == ItemTemplate.TYPE2_ACCESSORY))
		{
			itemInfoArmorBuy(activeChar, command);
		}
		else if (item.getType2() == ItemTemplate.TYPE2_WEAPON)
		{
			itemInfoWeaponBuy(activeChar, command);
		}
		else
		{
			itemInfoBuy(activeChar, command);
		}
	}
	
	private void itemInfoBuy(Player activeChar, String command)
	{
		String[] data = command.substring(5).split(" - ");
		int auctionId = Integer.parseInt(data[0]);
		int page = Integer.parseInt(data[1]);
		String search = data[2];
		int type = Integer.parseInt(data[3]);
		
		AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
		if (item == null)
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/iteminfo.htm");
		content = content.replace("%icon%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
		content = content.replace("%itemName%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
		content = content.replace("%cost%", "<font color=\"LEVEL\">" + item.getCostCount() + " " + ItemTable.getInstance().getTemplate(item.getCostId()).getName() + "(s)</font>");
		content = content.replace("%qty%", String.valueOf(item.getCount()));
		content = content.replace("%grade%", AyUtils.getInstance().getGrade(item.getItemId()));
		content = content.replace("%enc%", String.valueOf(item.getEnchant()));
		content = content.replace("%owner%", item.getName());
		content = content.replace("%type%", AyUtils.getInstance().getTypeString(item.getItemId()));
		content = content.replace("%desc%", ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() != "" ? ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() : "Item sem descrição.");
		content = content.replace("%weight%", ItemTable.getInstance().getTemplate(item.getItemId()).getWeight() + "");
		content = content.replace("%buy%", "bypass _bbsauction_buy " + item.getAuctionId());
		content = content.replace("%back%", "bypass _bbsauction_auction " + page + " - " + search + " - " + type);
		
		separateAndSend(content, activeChar);
	}
	
	private void itemInfoWeaponBuy(Player activeChar, String command)
	{
		String[] data = command.substring(5).split(" - ");
		int auctionId = Integer.parseInt(data[0]);
		int page = Integer.parseInt(data[1]);
		String search = data[2];
		int type = Integer.parseInt(data[3]);
		
		AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
		if (item == null)
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/iteminfoweapon.htm");
		content = content.replace("%icon%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
		content = content.replace("%itemName%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
		content = content.replace("%cost%", "<font color=\"LEVEL\">" + item.getCostCount() + " " + ItemTable.getInstance().getTemplate(item.getCostId()).getName() + "(s)</font>");
		content = content.replace("%qty%", String.valueOf(item.getCount()));
		content = content.replace("%grade%", AyUtils.getInstance().getGrade(item.getItemId()));
		content = content.replace("%enc%", String.valueOf(item.getEnchant()));
		content = content.replace("%owner%", item.getName());
		content = content.replace("%atqf%", ((Weapon) ItemTable.getInstance().getTemplate(item.getItemId())).getPDamage() + "");
		content = content.replace("%atqm%", ((Weapon) ItemTable.getInstance().getTemplate(item.getItemId())).getMDamage() + "");
		content = content.replace("%atqvel%", ((Weapon) ItemTable.getInstance().getTemplate(item.getItemId())).getAttackSpeed() + "");
		content = content.replace("%costmp%", ((Weapon) ItemTable.getInstance().getTemplate(item.getItemId())).getMpConsume() + "");
		content = content.replace("%type%", AyUtils.getInstance().getSlot(item.getItemId()) + " / " + AyUtils.getInstance().getWeaponType(((Weapon) ItemTable.getInstance().getTemplate(item.getItemId())).getItemType()));
		content = content.replace("%desc%", ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() != "" ? ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() : "Item sem descrição.");
		content = content.replace("%weight%", ItemTable.getInstance().getTemplate(item.getItemId()).getWeight() + "");
		content = content.replace("%buy%", "bypass _bbsauction_buy " + item.getAuctionId());
		content = content.replace("%back%", "bypass _bbsauction_auction " + page + " - " + search + " - " + type);
		
		separateAndSend(content, activeChar);
	}
	
	private void itemInfoArmorBuy(Player activeChar, String command)
	{
		String[] data = command.substring(5).split(" - ");
		int auctionId = Integer.parseInt(data[0]);
		int page = Integer.parseInt(data[1]);
		String search = data[2];
		int type = Integer.parseInt(data[3]);
		
		AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
		if (item == null)
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/iteminfoarmor.htm");
		content = content.replace("%icon%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
		content = content.replace("%itemName%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
		content = content.replace("%cost%", "<font color=\"LEVEL\">" + item.getCostCount() + " " + ItemTable.getInstance().getTemplate(item.getCostId()).getName() + "(s)</font>");
		content = content.replace("%qty%", String.valueOf(item.getCount()));
		content = content.replace("%grade%", AyUtils.getInstance().getGrade(item.getItemId()));
		content = content.replace("%enc%", String.valueOf(item.getEnchant()));
		content = content.replace("%owner%", item.getName());
		content = content.replace("%desc%", ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() != "" ? ItemTable.getInstance().getTemplate(item.getItemId()).getDesc() : "Item sem descrição.");
		content = content.replace("%weight%", ItemTable.getInstance().getTemplate(item.getItemId()).getWeight() + "");
		if (ItemTable.getInstance().getTemplate(item.getItemId()).getBodyPart() != ItemTemplate.SLOT_L_HAND)
		{
			content = content.replace("%type%", ItemTable.getInstance().getTemplate(item.getItemId()).getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR ? AyUtils.getInstance().getSlot(item.getItemId()) + " / " + AyUtils.getInstance().getArmorType(((Armor) ItemTable.getInstance().getTemplate(item.getItemId())).getItemType()) : AyUtils.getInstance().getSlot(item.getItemId()));
			content = content.replace("%defm%", ((Armor) ItemTable.getInstance().getTemplate(item.getItemId())).getMDef() + "");
			content = content.replace("%deff%", ((Armor) ItemTable.getInstance().getTemplate(item.getItemId())).getPDef() + "");
		}
		else
		{
			content = content.replace("%type%", "Escudo");
			content = content.replace("<tr><td width=37>Def.F</td><td width=97><font color=\"ad9d46\">%deff%</font></td><td width=37>Def.M</td><td width=97><font color=\"ad9d46\">%defm%</font></td></tr>", "");
		}
		content = content.replace("%buy%", "bypass _bbsauction_buy " + item.getAuctionId());
		content = content.replace("%back%", "bypass _bbsauction_auction " + page + " - " + search + " - " + type);
		
		separateAndSend(content, activeChar);
	}
	
	private void buyItem(Player activeChar, String command)
	{
		int auctionId = Integer.parseInt(command.substring(4));
		AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
		if (!activeChar.getInventory().validateCapacity(1))
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM);
			return;
		}
		if (item == null)
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		if ((activeChar.getInventory().getItemByItemId(item.getCostId()) == null) || (activeChar.getInventory().getItemByItemId(item.getCostId()).getCount() < item.getCostCount()))
		{
			this.parseCmd("auction 1 - *null* - 10", activeChar);
			activeChar.sendMessage("Contagem de itens incorreta.");
			return;
		}
		activeChar.destroyItemByItemId("auction", item.getCostId(), item.getCostCount(), null, true);
		Player owner = World.getInstance().getPlayer(item.getOwnerId());
		if ((owner != null) && owner.isOnline())
		{
			owner.getInventory().addItem("auction", item.getCostId(), item.getCostCount(), owner, null);
			owner.sendMessage("Você vendeu um item na Casa de Leilões.");
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement(AUCTION_BUY))
			{
				
				ps.setInt(1, item.getOwnerId());
				ps.setInt(2, IdManager.getInstance().getNextId());
				ps.setInt(3, item.getCostId());
				ps.setInt(4, item.getCostCount());
				ps.setInt(5, 0);
				ps.setString(6, "INVENTORY");
				ps.setInt(7, 0);
				ps.setInt(8, 0);
				ps.setInt(9, 0);
				ps.setNull(10, Types.INTEGER);
				ps.setInt(11, 0);
				ps.setInt(12, 0); // ou outro valor adequado para o 12º parâmetro
				ps.setBigDecimal(13, new BigDecimal(-1)); // ou outro valor adequado para o 13º parâmetro
				
				// Execute a consulta de atualização
				ps.executeUpdate();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			
		}
		Item i = activeChar.getInventory().addItem("auction", item.getItemId(), item.getCount(), activeChar, null);
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
		sm.addItemName(item.getItemId());
		sm.addNumber(item.getCount());
		activeChar.sendPacket(sm);
		i.setEnchantLevel(item.getEnchant());
		activeChar.sendPacket(new InventoryUpdate());
		activeChar.sendMessage("Você comprou um item na Casa de Leilões.");
		AuctionTable.getInstance().deleteItem(item);
		this.parseCmd("auction 1 - *null* - 10", activeChar);
	}
	
	private void removeItem(Player activeChar, String command)
	{
		int auctionId = Integer.parseInt(command.substring(7));
		AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
		if (item == null)
		{
			this.parseCmd("back", activeChar);
			activeChar.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		AuctionTable.getInstance().deleteItem(item);
		Item i = activeChar.getInventory().addItem("auction", item.getItemId(), item.getCount(), activeChar, null);
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
		sm.addItemName(item.getItemId());
		sm.addNumber(item.getCount());
		activeChar.sendPacket(sm);
		i.setEnchantLevel(item.getEnchant());
		activeChar.sendPacket(new InventoryUpdate());
		activeChar.sendMessage("Você removeu um item na Casa de Leilões.");
		this.parseCmd("myitems 1 - 1 - 0", activeChar);
	}
	
	private void showMyItems(Player activeChar, int page, int addpage, int objId)
	{
		final int ITEMS_PER_PAGE = 9;
		String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/myitems.htm");
		Map<Integer, ArrayList<AuctionItem>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<AuctionItem> temp = new ArrayList<>();
		for (AuctionItem auctionItem : AuctionTable.getInstance().getItems())
		{
			if (auctionItem.getOwnerId() != activeChar.getObjectId())
			{
				continue;
			}
			temp.add(auctionItem);
			if (++counter != ITEMS_PER_PAGE)
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
			this.parseCmd("back", activeChar);
			activeChar.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		for (AuctionItem item : items.get(page))
		{
			content = content.replace("%icon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			content = content.replace("%itemName" + index + "%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
			content = content.replace("%remove" + index + "%", "bypass _bbsauction_remove " + item.getAuctionId());
			content = content.replace("%cost" + index + "%", "<font color=\"a3a0a3\">Preço:</font> <font color=\"LEVEL\">" + item.getCostCount() + " " + ItemTable.getInstance().getTemplate(item.getCostId()).getName() + "(s)</font>");
			// StringUtil.append(sb, "<td fixwidth=71><button value=\"Comprar\" action=\"bypass -h npc_" + this.getObjectId() + "_buy " + item.getAuctionId() + "\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">");
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				content = content.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				content = content.replace("<button value=\"Remover\" action=\"%remove" + (i + 1) + "%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "<img src=\"L2UI.SquareBlank\" width=65 height=21>");
				content = content.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				content = content.replace("%cost" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
		}
		content = content.replace("%pages%", String.valueOf(page));
		if ((items.keySet().size() > 1))
		{
			int nextPage = page + 1;
			if (nextPage <= items.keySet().size())
			{
				if (items.get(nextPage).size() == 0)
				{
					content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
				}
			}
			if (page > 1)
			{
				// L2UI_CH3.prev1_over - L2UI_CH3.prev1_down - L2UI_CH3.Basic_Outline1
				content = content.replace("%prev%", "<button action=\"bypass _bbsauction_myitems " + (page - 1) + " - " + addpage + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > page)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				content = content.replace("%next%", "<button action=\"bypass _bbsauction_myitems " + (page + 1) + " - " + addpage + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
				content = content.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
			else
			{
				content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
		}
		else
		{
			content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			content = content.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			content = content.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		}
		long contador = AuctionTable.getInstance().getItems().stream().filter(item -> item.getOwnerId() == activeChar.getObjectId()).count();
		int contadorInt = (int) contador;
		int total = 0;
		if (activeChar.getInventory().getItemByItemId(9500) != null)
		{
			total = 5;
		}
		else if (activeChar.getInventory().getItemByItemId(9501) != null)
		{
			total = 10;
		}
		else if ((activeChar.getInventory().getItemByItemId(9502) != null) || activeChar.isDonator())
		{
			total = 15;
		}
		content = content.replace("%vdnt%", total + "");
		content = content.replace("%vdn%", contadorInt + "");
		content = content.replace("%back%", "bypass _bbsauction_back");
		content = showAddPanel(activeChar, page, addpage, content, objId);
		content = showAddPanel2(activeChar, objId, content);
		separateAndSend(content, activeChar);
	}
	
	private String showAddPanel2(Player activeChar, int itemId, String content)
	{
		String contentTemp = content;
		if (itemId != 0)
		{
			Item item = activeChar.getInventory().getItemByObjectId(itemId);
			contentTemp = contentTemp.replace("%icon%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			contentTemp = contentTemp.replace("%itemName%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
			contentTemp = contentTemp.replace("%qty%", "<edit var=\"amm\" type=number width=120 height=17><br>");
			contentTemp = contentTemp.replace("%coin%", "<combobox width=120 height=17 var=\"ebox\" list=Adena;Prestígio;Kali;><br>");
			contentTemp = contentTemp.replace("%cost%", "<edit var=\"count\" type=number width=120 height=17><br>");
			contentTemp = contentTemp.replace("%itemNameQty%", item.getCount() + "");
			contentTemp = contentTemp.replace("%add%", "bypass _bbsauction_addit2 " + itemId + " $ebox $count " + (item.isStackable() ? "$amm" : "1"));
		}
		else
		{
			contentTemp = contentTemp.replace("%icon%", "L2UI.SquareBlank");
			contentTemp = contentTemp.replace("%itemName%", "");
			contentTemp = contentTemp.replace("%itemNameQty%", "");
			contentTemp = contentTemp.replace("%qty%", "<edit var=\"amm\" type=number width=120 height=17><br>");
			contentTemp = contentTemp.replace("%coin%", "<combobox width=120 height=17 var=\"ebox\" list=Adena;Prestígio;Kali;><br>");
			contentTemp = contentTemp.replace("%cost%", "<edit var=\"count\" type=number width=120 height=17><br>");
		}
		return contentTemp;
	}
	
	private String showAddPanel(Player activeChar, int page, int addpage, String content, int objId)
	{
		final int ITEMS_PER_PAGE = 16;
		String contentTemp = content;
		Map<Integer, ArrayList<Item>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<Item> temp = new ArrayList<>();
		for (Item item : activeChar.getInventory().getItems())
		{
			if ((item.getItemId() == 57) || !item.isTradeable() || item.isEquipped())
			{
				continue;
			}
			temp.add(item);
			if (++counter != ITEMS_PER_PAGE)
			{
				continue;
			}
			items.put(curr, temp);
			temp = new ArrayList<>();
			++curr;
			counter = 0;
		}
		items.put(curr, temp);
		if (!items.containsKey(addpage))
		{
			this.parseCmd("back", activeChar);
			activeChar.sendMessage("Página inválida. Por favor, tente novamente.");
		}
		for (Item item : items.get(addpage))
		{
			contentTemp = contentTemp.replace("%addicon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			contentTemp = contentTemp.replace("%addiconbp" + index + "%", "bypass _bbsauction_myitems " + page + " - " + addpage + " - " + item.getObjectId());
			// StringUtil.append(sb, "<td fixwidth=71><button value=\"Comprar\" action=\"bypass -h npc_" + this.getObjectId() + "_buy " + item.getAuctionId() + "\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">");
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				contentTemp = contentTemp.replace("%addicon" + (i + 1) + "%", "L2UI.SquareBlank");
			}
		}
		contentTemp = contentTemp.replace("%addpages%", String.valueOf(addpage));
		if ((items.keySet().size() > 1))
		{
			int nextPage = addpage + 1;
			if (nextPage <= items.keySet().size())
			{
				if (items.get(nextPage).size() == 0)
				{
					contentTemp = contentTemp.replace("%addnext%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
				}
			}
			if (addpage > 1)
			{
				// L2UI_CH3.prev1_over - L2UI_CH3.prev1_down - L2UI_CH3.Basic_Outline1
				contentTemp = contentTemp.replace("%addprev%", "<button action=\"bypass _bbsauction_myitems " + page + " - " + (addpage - 1) + " - " + objId + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > addpage)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				contentTemp = contentTemp.replace("%addnext%", "<button action=\"bypass _bbsauction_myitems " + page + " - " + (addpage + 1) + " - " + objId + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
				contentTemp = contentTemp.replace("%addprev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
			else
			{
				contentTemp = contentTemp.replace("%addnext%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
		}
		else
		{
			contentTemp = contentTemp.replace("%addnext%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			contentTemp = contentTemp.replace("%addprev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			contentTemp = contentTemp.replace("%addpages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		}
		return contentTemp;
	}
	
	private void showAuction(Player activeChar, int page, String search, int type)
	{
		final int ITEMS_PER_PAGE = 9;
		String content = HtmCache.getInstance().getHtm(CB_PATH + "auction/itemlist.htm");
		// final StringBuilder sb = new StringBuilder();
		boolean src = !search.equals("*null*");
		Map<Integer, ArrayList<AuctionItem>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<AuctionItem> temp = new ArrayList<>();
		List<AuctionItem> itemList = new ArrayList<>(AuctionTable.getInstance().getItems());
		Collections.sort(itemList, (item1, item2) -> Integer.compare(item1.getAuctionId(), item2.getAuctionId()));
		for (AuctionItem auctionItem : itemList)
		{
			if ((auctionItem.getOwnerId() != activeChar.getObjectId()) && (!src || (src && ItemTable.getInstance().getTemplate(auctionItem.getItemId()).getName().toLowerCase().contains(search.toLowerCase()))))
			{
				if (type < 10)
				{
					if (type > 5)
					{
						if (type > AyUtils.getInstance().getTypeInt(auctionItem.getItemId()))
						{
							continue;
						}
					}
					else if (type != AyUtils.getInstance().getTypeInt(auctionItem.getItemId()))
					{
						continue;
					}
				}
				temp.add(auctionItem);
				if (++counter != ITEMS_PER_PAGE)
				{
					continue;
				}
				items.put(curr, temp);
				temp = new ArrayList<>();
				++curr;
				counter = 0;
			}
		}
		items.put(curr, temp);
		if (!items.containsKey(page))
		{
			this.parseCmd("back", activeChar);
			activeChar.sendMessage("Invalid page. Please try again.");
			return;
		}
		for (AuctionItem item : items.get(page))
		{
			content = content.replace("%icon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			content = content.replace("%itemName" + index + "%", item.getCount() == 1 ? formatName(item) : formatName(item) + " (" + item.getCount() + ")");
			content = content.replace("%cost" + index + "%", "<font color=\"a3a0a3\">Preço:</font> <font color=\"LEVEL\">" + item.getCostCount() + " " + ItemTable.getInstance().getTemplate(item.getCostId()).getName() + "(s)</font>");
			content = content.replace("%buy" + index + "%", "bypass _bbsauction_info " + item.getAuctionId() + " - " + page + " - " + search + " - " + type);
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				content = content.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				content = content.replace("<button value=\"Comprar\" action=\"%buy" + (i + 1) + "%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "<img src=\"L2UI.SquareBlank\" width=65 height=21>");
				content = content.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				content = content.replace("%cost" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
		}
		content = content.replace("%pages%", String.valueOf(page));
		if ((items.keySet().size() > 1))
		{
			int nextPage = page + 1;
			if (nextPage <= items.keySet().size())
			{
				if (items.get(nextPage).size() == 0)
				{
					content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
				}
			}
			if (page > 1)
			{
				// L2UI_CH3.prev1_over - L2UI_CH3.prev1_down - L2UI_CH3.Basic_Outline1
				content = content.replace("%prev%", "<button action=\"bypass _bbsauction_auction " + (page - 1) + " - " + search + " - " + type + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > page)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				content = content.replace("%next%", "<button action=\"bypass _bbsauction_auction " + (page + 1) + " - " + search + " - " + type + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
				content = content.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
			else
			{
				content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			}
		}
		else
		{
			content = content.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			content = content.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			content = content.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		}
		content = content.replace("%adena%", activeChar.getAdena() + "");
		content = content.replace("%pres%", activeChar.getInventory().getInventoryItemCount(9503, 0) + "");
		content = content.replace("%kali%", activeChar.getInventory().getInventoryItemCount(9509, 0) + "");
		content = content.replace("%vdn%", calculateTotal(items) + "");
		content = content.replace("%vdn%", calculateTotal(items) + "");
		content = content.replace("%back%", "bypass _bbsauction_Chat 1");
		separateAndSend(content, activeChar);
	}
	
	private String formatName(AuctionItem item)
	{
		String itemName = item.getEnchant() > 0 ? "<font color=\"b09979\">+" + item.getEnchant() + "</font> " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() : ItemTable.getInstance().getTemplate(item.getItemId()).getName();
		if (itemName.contains(" - "))
		{
			String[] crysItem = itemName.split(" - ");
			itemName = crysItem[0] + "<font color=\"LEVEL\"> " + crysItem[1] + "</font>";
		}
		return itemName;
	}
	
	private String formatName(Item item)
	{
		String itemName = item.getEnchantLevel() > 0 ? "<font color=\"b09979\">+" + item.getEnchantLevel() + "</font> " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() : ItemTable.getInstance().getTemplate(item.getItemId()).getName();
		if (itemName.contains(" - "))
		{
			String[] crysItem = itemName.split(" - ");
			itemName = crysItem[0] + "<font color=\"LEVEL\"> " + crysItem[1] + "</font>";
		}
		return itemName;
	}
	
	private static int calculateTotal(Map<Integer, ArrayList<AuctionItem>> itemsMap)
	{
		int tamanhoTotal = 0;
		for (ArrayList<AuctionItem> itemList : itemsMap.values())
		{
			tamanhoTotal += itemList.size();
		}
		
		return tamanhoTotal;
	}
	
	@Override
	protected String getFolder()
	{
		return "auction/";
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionBBSManager INSTANCE = new AuctionBBSManager();
	}
}