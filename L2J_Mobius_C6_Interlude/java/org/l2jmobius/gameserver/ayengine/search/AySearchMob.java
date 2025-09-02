package org.l2jmobius.gameserver.ayengine.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.l2jmobius.gameserver.ayengine.AySql;
import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.model.DropCategory;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.Armor;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class AySearchMob
{
	final int ITEMS_PER_PAGE = 7;
	
	public boolean useBypass(Player player, String cmd)
	{
		if (cmd.contentEquals("aysearch_searchmob"))
		{
			searchEmpty(player);
		}
		else if (cmd.contentEquals("aysearch_searchmob 1 -"))
		{
			searchEmpty(player);
		}
		else if (cmd.startsWith("aysearch_searchmob_mobinfo"))
		{
			mobInfo(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_mobsearch"))
		{
			searchMob(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_droplisttarget"))
		{
			dropListTarget(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_infoitemtarget"))
		{
			itemInfoTarget(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_droplist"))
		{
			dropList(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_infoitem"))
		{
			itemInfo(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob_fix"))
		{
			String[] data = cmd.substring(24).split(" - ");
			int mobId = Integer.parseInt(data[0]);
			fix(player, mobId);
		}
		else if (cmd.startsWith("aysearch_searchmob_send"))
		{
			send(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchmob"))
		{
			String[] data = cmd.substring(19).split(" - ");
			int page = Integer.parseInt(data[0]);
			String search = data[1];
			if (search != "")
			{
				getListMob(player, page, search);
			}
		}
		return true;
	}
	
	public void send(Player player, String cmd)
	{
		String[] data = cmd.substring(24).split(" - ");
		int mobId = Integer.parseInt(data[0]);
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
		if (AySql.getInstance().storeFix(mobId, type, reason, player.getName()))
		{
			npcHtml.setFile("data/html/ayengine/fix/thank.htm");
		}
		else
		{
			npcHtml.setFile("data/html/ayengine/fix/exist.htm");
		}
		player.sendPacket(npcHtml);
	}
	
	public void fix(Player activeChar, int mobId)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/fix/fix.htm");
		npcHtml.replace("%mobid%", mobId);
		activeChar.sendPacket(npcHtml);
	}
	
	private void searchEmpty(Player player)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/searchmob.htm");
		npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
		npcHtml.replace("%mobName2%", "<center>Digite o nome do monstro!</center>");
		npcHtml.replace("%type2%", "");
		for (int i = 0; i < ITEMS_PER_PAGE; i++)
		{
			npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
			npcHtml.replace("%mobName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
		}
		npcHtml.replace("%title%", "Monstros");
		npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
		player.sendPacket(npcHtml);
	}
	
	private void searchMob(Player player, String command)
	{
		int mobId = Integer.parseInt(command.substring(29));
		AyUtils.getInstance().MarkMobRadar(player, mobId);
	}
	
	private void mobInfo(Player player, String command)
	{
		String[] data = command.substring(27).split(" - ");
		int page = Integer.parseInt(data[0]);
		String search = data[1];
		int mobId = Integer.parseInt(data[2]);
		
		NpcTemplate mob = NpcTable.getInstance().getTemplate(mobId);
		
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/mobInfo.htm");
		
		npcHtml.replace("%title%", "Informações do Monstro");
		npcHtml.replace("%icon%", AyUtils.getInstance().getMobDesc(mob.getNpcId()).getIcon());
		npcHtml.replace("%mobName%", mob.getName());
		npcHtml.replace("%type%", AyUtils.getInstance().getMobDesc(mob.getNpcId()).getNameType());
		npcHtml.replace("%lvl%", mob.getLevel());
		npcHtml.replace("%desc%", AyUtils.getInstance().getMobDesc(mob.getNpcId()).getDesc());
		npcHtml.replace("%type2%", AyUtils.getInstance().getType2(mob.getNpcId()));
		npcHtml.replace("%button%", "Drops");
		npcHtml.replace("%button2%", "Local");
		npcHtml.replace("%local%", "bypass -h aysearch_searchmob_mobsearch " + mob.getNpcId());
		npcHtml.replace("%fix%", "bypass -h aysearch_searchmob_fix " + mob.getNpcId());
		npcHtml.replace("%drops%", "bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mob.getNpcId() + " - 1");
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob " + page + " - " + search);
		
		player.sendPacket(npcHtml);
	}
	
	private void getListMob(Player player, int page, String search)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/searchmob.htm");
		
		List<NpcTemplate> allNpc = Arrays.stream(NpcTable.getInstance().getTemplates()).filter(item -> (item != null) && matches(item.getName(), search)).collect(Collectors.toList());
		
		if (allNpc.isEmpty())
		{
			npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
			npcHtml.replace("%mobName2%", "<center>Monstro não encontrado!</center>");
			npcHtml.replace("%type2%", "");
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				npcHtml.replace("%mobName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
			npcHtml.replace("%title%", "Monstros");
			npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%prev%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			npcHtml.replace("%pages%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
			player.sendPacket(npcHtml);
			return;
		}
		
		int totalPages = (int) Math.ceil((double) allNpc.size() / ITEMS_PER_PAGE);
		
		if ((page < 1) || (page > totalPages))
		{
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		
		int startIdx = (page - 1) * ITEMS_PER_PAGE;
		int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allNpc.size());
		List<NpcTemplate> pageMobs = allNpc.subList(startIdx, endIdx);
		
		npcHtml.replace("%title%", "Monstros");
		
		int index = 1;
		for (NpcTemplate mob : pageMobs)
		{
			int npcId = mob.getNpcId();
			String mobName = mob.getName();
			
			npcHtml.replace("%icon" + index + "%", AyUtils.getInstance().getMobDesc(npcId).getIcon());
			npcHtml.replace("%mobName" + index + "%", "<a action=\"bypass -h aysearch_searchmob_mobinfo " + page + " - " + search + " - " + npcId + "\">" + mobName + "</a>");
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
		
		npcHtml.replace("%pages%", String.valueOf(page));
		
		// Navegação
		boolean hasPrev = page > 1;
		boolean hasNext = page < totalPages;
		
		String prevButton = hasPrev ? "<button action=\"bypass -h aysearch_searchmob " + (page - 1) + " - " + search + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		String nextButton = hasNext ? "<button action=\"bypass -h aysearch_searchmob " + (page + 1) + " - " + search + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		npcHtml.replace("%prev%", prevButton);
		npcHtml.replace("%next%", nextButton);
		
		player.sendPacket(npcHtml);
	}
	
	private void dropList(Player player, String command)
	{
		String[] data = command.substring(28).split(" - ");
		int page = Integer.parseInt(data[0]);
		String search = data[1];
		int mobId = Integer.parseInt(data[2]);
		int pageItem = Integer.parseInt(data[3]);
		
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/itemlist.htm");
		
		final NpcTemplate npcData = NpcTable.getInstance().getTemplate(mobId);
		List<ItemTemplate> allItems = new ArrayList<>();
		for (DropCategory cat : npcData.getDropData())
		{
			for (DropData drop : cat.getAllDrops())
			{
				allItems.add(ItemTable.getInstance().getTemplate(drop.getItemId()));
			}
		}
		if (allItems.isEmpty())
		{
			npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
			npcHtml.replace("%itemName2%", "<center>Monstro sem drops!</center>");
			npcHtml.replace("%type2%", "");
		}
		Map<Integer, ArrayList<ItemTemplate>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<ItemTemplate> temp = new ArrayList<>();
		for (ItemTemplate template : allItems)
		{
			temp.add(template);
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
		if (!items.containsKey(pageItem))
		{
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		npcHtml.replace("%title%", "Drops");
		for (ItemTemplate item : items.get(pageItem))
		{
			int chance = 0;
			int category = 0;
			for (DropCategory cat : npcData.getDropData())
			{
				for (DropData drop : cat.getAllDrops())
				{
					if (item.getItemId() == drop.getItemId())
					{
						chance = drop.getChance();
						category = cat.getCategoryType();
					}
				}
			}
			
			String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
			
			npcHtml.replace("%icon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			String name = item.getName().length() > 45 ? item.getName().substring(0, Math.min(item.getName().length(), 45)) + "..." : item.getName();
			npcHtml.replace("%itemName" + index + "%", "<a action=\"bypass -h aysearch_searchmob_infoitemtarget " + page + " - " + mobId + " - " + item.getItemId() + " - " + chance + " - " + npcData.getName() + "\">" + name + "</a><br1><font color=\"a3a0a3\">Chance:</font> <font color=\"b09979\">" + (chanceMob.equals("0.00") ? "0.01" : chanceMob) + "%</font> <font color=\"LEVEL\">" + (category == -1 ? "Saquear" : "") + "</font>");
			npcHtml.replace("%type" + index + "%", "Tipo: <font color=\"LEVEL\">" + AyUtils.getInstance().getTypeString(item.getItemId()) + "</font>");
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				npcHtml.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			}
		}
		npcHtml.replace("%pages%", String.valueOf(pageItem));
		if ((items.keySet().size() > 1))
		{
			int nextPage = pageItem + 1;
			if (nextPage <= items.keySet().size())
			{
				if (items.get(nextPage).size() == 0)
				{
					npcHtml.replace("%next%", "<img src=\"L2UI.SquareBlank\" width=16 height=16>");
				}
			}
			if (pageItem > 1)
			{
				// L2UI_CH3.prev1_over - L2UI_CH3.prev1_down - L2UI_CH3.Basic_Outline1
				npcHtml.replace("%prev%", "<button action=\"bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mobId + " - " + (pageItem - 1) + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > pageItem)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				npcHtml.replace("%next%", "<button action=\"bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mobId + " - " + (pageItem + 1) + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
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
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob " + page + " - " + search);
		player.sendPacket(npcHtml);
	}
	
	private void itemInfo(Player player, String command)
	{
		String[] data = command.substring(28).split(" - ");
		int page = Integer.parseInt(data[0]);
		String search = data[1];
		int mobId = Integer.parseInt(data[2]);
		int pageItem = Integer.parseInt(data[3]);
		int itemId = Integer.parseInt(data[4]);
		int chance = Integer.parseInt(data[5]);
		
		ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			player.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		if ((item.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR) || (item.getType2() == ItemTemplate.TYPE2_ACCESSORY))
		{
			armorInfo(player, item, search, page, mobId, pageItem, chance);
		}
		else if (item.getType2() == ItemTemplate.TYPE2_WEAPON)
		{
			weaponInfo(player, item, search, page, mobId, pageItem, chance);
		}
		else
		{
			ectInfo(player, item, search, page, mobId, pageItem, chance);
		}
	}
	
	private void ectInfo(Player player, ItemTemplate item, String search, int page, int mobId, int pageItem, int chance)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfo.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
		npcHtml.replace("%title%", "Informações do Item");
		npcHtml.replace("%icon%", item.getIcon());
		npcHtml.replace("%itemName%", player.isGM() ? item.getName() + " - " + item.getItemId() : item.getName());
		npcHtml.replace("%type%", AyUtils.getInstance().getTypeString(item.getItemId()));
		npcHtml.replace("%desc%", item.getDesc() != "" ? item.getDesc() : "Este item não possui descrição");
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%weight%", item.getWeight());
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mobId + " - " + pageItem);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void weaponInfo(Player player, ItemTemplate item, String search, int page, int mobId, int pageItem, int chance)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoweapon.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
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
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mobId + " - " + pageItem);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void armorInfo(Player player, ItemTemplate item, String search, int page, int mobId, int pageItem, int chance)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoarmor.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
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
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplist " + page + " - " + search + " - " + mobId + " - " + pageItem);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	// Target
	
	private void dropListTarget(Player player, String command)
	{
		String[] data = command.substring(34).split(" - ");
		int page = Integer.parseInt(data[0]);
		int mobId = Integer.parseInt(data[1]);
		
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/itemlist.htm");
		
		final NpcTemplate npcData = NpcTable.getInstance().getTemplate(mobId);
		List<ItemTemplate> allItems = new ArrayList<>();
		for (DropCategory cat : npcData.getDropData())
		{
			for (DropData drop : cat.getAllDrops())
			{
				allItems.add(ItemTable.getInstance().getTemplate(drop.getItemId()));
			}
		}
		if (allItems.isEmpty())
		{
			npcHtml.replace("<img src=\"%icon2%\" width=32 height=32 align=center>", "");
			npcHtml.replace("%itemName2%", "<center>Monstro sem drops!</center>");
			npcHtml.replace("%type2%", "");
		}
		Map<Integer, ArrayList<ItemTemplate>> items = new ConcurrentHashMap<>();
		int curr = 1;
		int counter = 0;
		int index = 1;
		ArrayList<ItemTemplate> temp = new ArrayList<>();
		for (ItemTemplate template : allItems)
		{
			temp.add(template);
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
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		npcHtml.replace("%title%", "Drops");
		for (ItemTemplate item : items.get(page))
		{
			int chance = 0;
			int category = 0;
			for (DropCategory cat : npcData.getDropData())
			{
				for (DropData drop : cat.getAllDrops())
				{
					if (item.getItemId() == drop.getItemId())
					{
						chance = drop.getChance();
						category = cat.getCategoryType();
					}
				}
			}
			
			String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
			
			npcHtml.replace("%icon" + index + "%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			String name = item.getName().length() > 45 ? item.getName().substring(0, Math.min(item.getName().length(), 45)) + "..." : item.getName();
			npcHtml.replace("%itemName" + index + "%", "<a action=\"bypass -h aysearch_searchmob_infoitemtarget " + page + " - " + mobId + " - " + item.getItemId() + " - " + chance + " - " + npcData.getName() + "\">" + name + "</a><br1><font color=\"a3a0a3\">Chance:</font> <font color=\"b09979\">" + (chanceMob.equals("0.00") ? "0.01" : chanceMob) + "%</font>  <font color=\"LEVEL\">" + (category == -1 ? "Saquear" : "") + "</font>");
			npcHtml.replace("%type" + index + "%", "Tipo: <font color=\"LEVEL\">" + AyUtils.getInstance().getTypeString(item.getItemId()) + "</font>");
			index++;
		}
		if (index <= ITEMS_PER_PAGE)
		{
			for (int i = 0; i < ITEMS_PER_PAGE; i++)
			{
				npcHtml.replace("%icon" + (i + 1) + "%", "L2UI.SquareBlank");
				npcHtml.replace("%itemName" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
				npcHtml.replace("%type" + (i + 1) + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
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
				npcHtml.replace("%prev%", "<button action=\"bypass -h aysearch_searchmob_droplisttarget " + (page - 1) + " - " + mobId + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">");
			}
			if (items.keySet().size() > page)
			{
				// L2UI_CH3.next1_over - L2UI_CH3.next1_down
				npcHtml.replace("%next%", "<button action=\"bypass -h aysearch_searchmob_droplisttarget " + (page + 1) + " - " + mobId + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">");
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
		npcHtml.replace("<button value=\"Voltar\" action=\"%back%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("Pesquisa de Itens", npcData.getName() + " - Lv " + npcData.getLevel());
		player.sendPacket(npcHtml);
	}
	
	private void itemInfoTarget(Player player, String command)
	{
		String[] data = command.substring(34).split(" - ");
		int page = Integer.parseInt(data[0]);
		int mobId = Integer.parseInt(data[1]);
		int itemId = Integer.parseInt(data[2]);
		int chance = Integer.parseInt(data[3]);
		String mobName = data[4];
		
		ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			player.sendMessage("Escolha inválida. Por favor, tente novamente.");
			return;
		}
		if ((item.getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR) || (item.getType2() == ItemTemplate.TYPE2_ACCESSORY))
		{
			armorInfoTarget(player, item, page, mobId, chance, mobName);
		}
		else if (item.getType2() == ItemTemplate.TYPE2_WEAPON)
		{
			weaponInfoTarget(player, item, page, mobId, chance, mobName);
		}
		else
		{
			ectInfoTarget(player, item, page, mobId, chance, mobName);
		}
	}
	
	private void ectInfoTarget(Player player, ItemTemplate item, int page, int mobId, int chance, String mobName)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfo.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
		npcHtml.replace("%title%", "Informações do Item");
		npcHtml.replace("%icon%", item.getIcon());
		npcHtml.replace("%itemName%", player.isGM() ? item.getName() + " - " + item.getItemId() : item.getName());
		npcHtml.replace("%type%", AyUtils.getInstance().getTypeString(item.getItemId()));
		npcHtml.replace("%desc%", item.getDesc() != "" ? item.getDesc() : "Este item não possui descrição");
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%weight%", item.getWeight());
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplisttarget " + page + " - " + mobId);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("Pesquisa de Itens", mobName);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void weaponInfoTarget(Player player, ItemTemplate item, int page, int mobId, int chance, String mobName)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoweapon.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
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
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplisttarget " + page + " - " + mobId);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("Pesquisa de Itens", mobName);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private void armorInfoTarget(Player player, ItemTemplate item, int page, int mobId, int chance, String mobName)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/iteminfoarmor.htm");
		
		String chanceMob = String.format(Locale.ENGLISH, "%.2f", (chance / 10000.0));
		
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
		npcHtml.replace("<button value=\"%button%\" action=\"%drops%\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", "");
		npcHtml.replace("%back%", "bypass -h aysearch_searchmob_droplisttarget " + page + " - " + mobId);
		npcHtml.replace("%chance%", chanceMob.equals("0.00") ? "0.01" : chanceMob + "%");
		npcHtml.replace("Pesquisa de Itens", mobName);
		npcHtml.replace("%fix%", "bypass -h aysearch_searchitem_fix " + item.getItemId());
		npcHtml.replace("%admin%", player.isGM() ? "<button value=\"Pegar\" action=\"bypass -h admin_create_item " + item.getItemId() + " 1\" width=65 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
		
		player.sendPacket(npcHtml);
	}
	
	private static boolean matches(String name, String search)
	{
		return Arrays.stream(search.toLowerCase().split(" ")).allMatch(result -> name.toLowerCase().contains(result));
	}
	
	private static class InstanceHolder
	{
		private static final AySearchMob _instance = new AySearchMob();
	}
	
	public static AySearchMob getInstance()
	{
		return InstanceHolder._instance;
	}
}