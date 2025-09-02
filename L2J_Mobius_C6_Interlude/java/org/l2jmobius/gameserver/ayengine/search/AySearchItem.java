package org.l2jmobius.gameserver.ayengine.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.l2jmobius.gameserver.ayengine.AySql;
import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.Armor;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class AySearchItem
{
	final int ITEMS_PER_PAGE = 7;
	
	public boolean useBypass(Player player, String cmd)
	{
		if (cmd.contentEquals("aysearch_searchitem"))
		{
			searchEmpty(player);
		}
		else if (cmd.contentEquals("aysearch_searchitem 1 -"))
		{
			searchEmpty(player);
		}
		else if (cmd.startsWith("aysearch_searchitem_infoitem"))
		{
			itemInfo(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchitem_moblist"))
		{
			mobList(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchitem_mobsearch"))
		{
			searchMob(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchitem_fix"))
		{
			int itemId = Integer.parseInt(cmd.substring(24));
			fixItem(player, itemId);
		}
		else if (cmd.startsWith("aysearch_searchitem_send"))
		{
			send(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchitem"))
		{
			String[] data = cmd.substring(20).split(" - ");
			int page = Integer.parseInt(data[0]);
			String search = data[1];
			if (search != "")
			{
				getListItem(player, page, search);
			}
		}
		return true;
	}
	
	public void send(Player player, String cmd)
	{
		String[] data = cmd.substring(25).split(" - ");
		int itemId = Integer.parseInt(data[0]);
		String type = data[1];
		String reason = " ";
		if (type.substring(type.length() - 1).equals("-"))
		{
			type = type.substring(0, type.length() - 2);
		}
		else
		{
			reason = data[2];
		}
		final var npcHtml = new NpcHtmlMessage(0);
		if (AySql.getInstance().storeFixItem(itemId, type, reason, player.getName()))
		{
			npcHtml.setFile("data/html/ayengine/fix/thank.htm");
		}
		else
		{
			npcHtml.setFile("data/html/ayengine/fix/exist.htm");
		}
		player.sendPacket(npcHtml);
	}
	
	public void fixItem(Player player, int itemId)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/fix/fixitem.htm");
		npcHtml.replace("%itemid%", itemId);
		player.sendPacket(npcHtml);
	}
	
	private void searchEmpty(Player player)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/searchitem.htm");
		npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
		npcHtml.replace("%itemName2%", "<center>Digite o nome do item!</center>");
		npcHtml.replace("%type2%", "");
		for (int i = 0; i < ITEMS_PER_PAGE; i++)
		{
			npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
			npcHtml.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
		}
		npcHtml.replace("%title%", "Itens");
		npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		player.sendPacket(npcHtml);
	}
	
	private void searchMob(Player player, String command)
	{
		int mobId = Integer.parseInt(command.substring(30));
		AyUtils.getInstance().MarkMobRadar(player, mobId);
	}
	
	private void mobList(Player player, String command)
	{
		String[] data = command.substring(28).split(" - ");
		int page = Integer.parseInt(data[0]);
		String search = data[1];
		int itemId = Integer.parseInt(data[2]);
		int pageMob = Integer.parseInt(data[3]);
		
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/moblist.htm");
		
		List<NpcTemplate> allMobs = NpcTable.getInstance().getAllOfItemId(itemId);
		
		if (allMobs.isEmpty())
		{
			npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
			npcHtml.replace("%mobName2%", "<center>Não há monstros que dropam!</center>");
			npcHtml.replace("%type2%", "");
			npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%back%", "bypass -h aysearch_searchitem " + page + " - " + search);
			player.sendPacket(npcHtml);
			return;
		}
		
		int totalPages = (int) Math.ceil((double) allMobs.size() / ITEMS_PER_PAGE);
		if ((pageMob < 1) || (pageMob > totalPages))
		{
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		
		int startIdx = (pageMob - 1) * ITEMS_PER_PAGE;
		int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allMobs.size());
		List<NpcTemplate> pageMobs = allMobs.subList(startIdx, endIdx);
		
		npcHtml.replace("%title%", "Monstros");
		
		int index = 1;
		for (NpcTemplate mob : pageMobs)
		{
			int npcId = mob.getNpcId();
			npcHtml.replace("%icon" + index + "%", AyUtils.getInstance().getMobDesc(npcId).getIcon());
			npcHtml.replace("%mobName" + index + "%", "<a action=\"bypass -h aysearch_searchitem_mobsearch " + npcId + "\">" + mob.getName() + "</a>");
			npcHtml.replace("%type" + index + "%", "Tipo: <font color=\"LEVEL\">" + AyUtils.getInstance().getType2(npcId) + " / " + AyUtils.getInstance().getMobDesc(npcId).getNameType() + "</font>");
			index++;
		}
		
		// Preencher espaços vazios
		for (; index <= ITEMS_PER_PAGE; index++)
		{
			npcHtml.replace("%icon" + index + "%", "L2UI.SquareBlank");
			npcHtml.replace("%mobName" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			npcHtml.replace("%type" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
		}
		
		npcHtml.replace("%pages%", String.valueOf(pageMob));
		
		// Navegação
		boolean hasPrev = pageMob > 1;
		boolean hasNext = pageMob < totalPages;
		
		String prevButton = hasPrev ? "<button action=\"bypass -h aysearch_searchitem_moblist " + page + " - " + search + " - " + itemId + " - " + (pageMob - 1) + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		String nextButton = hasNext ? "<button action=\"bypass -h aysearch_searchitem_moblist " + page + " - " + search + " - " + itemId + " - " + (pageMob + 1) + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		npcHtml.replace("%prev%", prevButton);
		npcHtml.replace("%next%", nextButton);
		
		npcHtml.replace("%back%", "bypass -h aysearch_searchitem " + page + " - " + search);
		
		player.sendPacket(npcHtml);
	}
	
	private void itemInfo(Player player, String command)
	{
		String[] data = command.substring(29).split(" - ");
		int page = Integer.parseInt(data[0]);
		String search = data[1];
		int itemId = Integer.parseInt(data[2]);
		
		ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			player.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		if ((item.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR) || (item.getType2() == ItemTemplate.TYPE2_ACCESSORY))
		{
			armorInfo(player, item, search, page);
		}
		else if (item.getType2() == ItemTemplate.TYPE2_WEAPON)
		{
			weaponInfo(player, item, search, page);
		}
		else
		{
			ectInfo(player, item, search, page);
		}
	}
	
	private void ectInfo(Player player, ItemTemplate item, String search, int page)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfo.htm");
		
		npcHtml.replace("%title%", "Informações do Item");
		npcHtml.replace("%icon%", item.getIcon());
		npcHtml.replace("%itemName%", player.isGM() ? item.getName() + " - " + item.getItemId() : item.getName());
		npcHtml.replace("%type%", AyUtils.getInstance().getTypeString(item.getItemId()));
		npcHtml.replace("%desc%", item.getDesc() != "" ? item.getDesc() : "Este item não possui descrição");
		npcHtml.replace("%weight%", item.getWeight());
		npcHtml.replace("%button%", "Monstros");
		npcHtml.replace("%drops%", "bypass -h aysearch_searchitem_moblist " + page + " - " + search + " - " + item.getItemId() + " - 1");
		npcHtml.replace("%back%", "bypass -h aysearch_searchitem " + page + " - " + search);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("<td width=57><font color=\"a3a0a3\">Chance</font></td><td width=77><font color=\"b09979\">%chance%</font></td>", "");
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void weaponInfo(Player player, ItemTemplate item, String search, int page)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoweapon.htm");
		
		npcHtml.replace("%title%", "Informações do Item");
		npcHtml.replace("%icon%", item.getIcon());
		npcHtml.replace("%itemName%", player.isGM() ? item.getName() + " - " + item.getItemId() : item.getName());
		npcHtml.replace("%type%", AyUtils.getInstance().getSlot(item.getItemId()) + " / " + AyUtils.getInstance().getWeaponType(((Weapon) item).getItemType()));
		npcHtml.replace("%desc%", item.getDesc() != "" ? item.getDesc() : "Este item não possui descrição");
		npcHtml.replace("%grade%", AyUtils.getInstance().getGrade(item.getItemId()));
		npcHtml.replace("%weight%", item.getWeight());
		npcHtml.replace("%atqf%", ((Weapon) item).getPDamage());
		npcHtml.replace("%atqm%", ((Weapon) item).getMDamage());
		npcHtml.replace("%atqvel%", ((Weapon) item).getAttackSpeed());
		npcHtml.replace("%costmp%", ((Weapon) item).getMpConsume());
		npcHtml.replace("%button%", "Monstros");
		npcHtml.replace("%drops%", "bypass -h aysearch_searchitem_moblist " + page + " - " + search + " - " + item.getItemId() + " - 1");
		npcHtml.replace("%back%", "bypass -h aysearch_searchitem " + page + " - " + search);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("<tr><td width=57><font color=\"a3a0a3\">Chance</font></td><td width=77><font color=\"b09979\">%chance%</font></td></tr>", "");
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void armorInfo(Player player, ItemTemplate item, String search, int page)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoarmor.htm");
		
		npcHtml.replace("%title%", "Informações do Item");
		npcHtml.replace("%icon%", item.getIcon());
		npcHtml.replace("%itemName%", player.isGM() ? item.getName() + " - " + item.getItemId() : item.getName());
		
		npcHtml.replace("%desc%", item.getDesc() != "" ? item.getDesc() : "Este item não possui descrição");
		npcHtml.replace("%grade%", AyUtils.getInstance().getGrade(item.getItemId()));
		npcHtml.replace("%weight%", item.getWeight());
		if (item.getBodyPart() != ItemTemplate.SLOT_L_HAND)
		{
			npcHtml.replace("%type%", item.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR ? AyUtils.getInstance().getSlot(item.getItemId()) + " / " + AyUtils.getInstance().getArmorType(((Armor) item).getItemType()) : AyUtils.getInstance().getSlot(item.getItemId()));
			npcHtml.replace("%defm%", ((Armor) item).getMDef());
			npcHtml.replace("%deff%", ((Armor) item).getPDef());
		}
		else
		{
			npcHtml.replace("%type%", "Escudo");
			npcHtml.replace("<tr><td width=57><font color=\"a3a0a3\">Def.F</font></td><td width=77><font color=\"b09979\">%deff%</font></td><td width=57><font color=\"a3a0a3\">Def.M</font></td><td width=77><font color=\"b09979\">%defm%</font></td></tr>", "<tr><td width=37><font color=\"a3a0a3\">Def.F</font></td><td width=97><font color=\"b09979\">%deff%</font></td><td width=37><font color=\"a3a0a3\">Def.Taxa</font></td><td width=97><font color=\"b09979\">%defr%</font></td></tr>");
			npcHtml.replace("<tr><td width=57><font color=\"a3a0a3\">Chance</font></td><td width=77><font color=\"b09979\">%chance%</font></td></tr>", "<tr><td width=57><font color=\"a3a0a3\">Evasão</font></td><td width=77><font color=\"b09979\">%evasion%</font></td><td width=57><font color=\"a3a0a3\">Chance</font></td><td width=77><font color=\"b09979\">%chance%</font></td></tr>");
			npcHtml.replace("%deff%", ((Weapon) item).getShieldDef());
			npcHtml.replace("%defr%", ((Weapon) item).getShieldDefRate());
			npcHtml.replace("%evasion%", ((Weapon) item).getAvoidModifier());
		}
		npcHtml.replace("%button%", "Monstros");
		npcHtml.replace("%drops%", "bypass -h aysearch_searchitem_moblist " + page + " - " + search + " - " + item.getItemId() + " - 1");
		npcHtml.replace("%back%", "bypass -h aysearch_searchitem " + page + " - " + search);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("<tr><td width=57><font color=\"a3a0a3\">Chance</font></td><td width=77><font color=\"b09979\">%chance%</font></td></tr>", "");
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void getListItem(Player player, int page, String search)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/searchitem.htm");
		
		List<ItemTemplate> allItems = Arrays.stream(ItemTable.getInstance().getTemplates()).filter(item -> (item != null) && matches(item.getName(), search)).collect(Collectors.toList());
		
		if (allItems.isEmpty())
		{
			npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
			npcHtml.replace("%itemName2%", "<center>Item não encontrado!</center>");
			npcHtml.replace("%type2%", "");
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				npcHtml.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
			npcHtml.replace("%title%", "Itens");
			npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			player.sendPacket(npcHtml);
			return;
		}
		
		int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
		if ((page < 1) || (page > totalPages))
		{
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		
		int startIdx = (page - 1) * ITEMS_PER_PAGE;
		int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allItems.size());
		List<ItemTemplate> pageItems = allItems.subList(startIdx, endIdx);
		
		npcHtml.replace("%title%", "Itens");
		
		int index = 1;
		for (ItemTemplate item : pageItems)
		{
			int itemId = item.getItemId();
			String itemName = item.getName();
			if (itemName.length() > 50)
			{
				itemName = itemName.substring(0, 25);
			}
			
			npcHtml.replace("%icon" + index + "%", item.getIcon());
			npcHtml.replace("%itemName" + index + "%", "<a action=\"bypass -h aysearch_searchitem_infoitem " + page + " - " + search + " - " + itemId + "\">" + itemName + "</a>");
			npcHtml.replace("%type" + index + "%", "Tipo: <font color=\"LEVEL\">" + AyUtils.getInstance().getTypeString(itemId) + "</font>");
			index++;
		}
		
		// Preencher espaços vazios
		for (; index <= ITEMS_PER_PAGE; index++)
		{
			npcHtml.replace("%icon" + index + "%", "L2UI.SquareBlank");
			npcHtml.replace("%itemName" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			npcHtml.replace("%type" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
		}
		
		npcHtml.replace("%pages%", String.valueOf(page));
		
		// Navegação
		boolean hasPrev = page > 1;
		boolean hasNext = page < totalPages;
		
		String prevButton = hasPrev ? "<button action=\"bypass -h aysearch_searchitem " + (page - 1) + " - " + search + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		String nextButton = hasNext ? "<button action=\"bypass -h aysearch_searchitem " + (page + 1) + " - " + search + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		npcHtml.replace("%prev%", prevButton);
		npcHtml.replace("%next%", nextButton);
		
		player.sendPacket(npcHtml);
	}
	
	private static boolean matches(String name, String search)
	{
		return Arrays.stream(search.toLowerCase().split(" ")).allMatch(result -> name.toLowerCase().contains(result));
	}
	
	private static class InstanceHolder
	{
		private static final AySearchItem _instance = new AySearchItem();
	}
	
	public static AySearchItem getInstance()
	{
		return InstanceHolder._instance;
	}
}