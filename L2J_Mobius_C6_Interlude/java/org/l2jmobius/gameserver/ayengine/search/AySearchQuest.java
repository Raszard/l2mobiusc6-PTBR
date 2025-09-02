package org.l2jmobius.gameserver.ayengine.search;

import java.util.List;

import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.data.xml.QuestData;
import org.l2jmobius.gameserver.model.QuestList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class AySearchQuest
{
	final int ITEMS_PER_PAGE = 7;
	
	public boolean useBypass(Player player, String cmd)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/questlist.htm");
		if (cmd.startsWith("aysearch_searchquest_infoquest"))
		{
			questInfo(player, cmd);
		}
		else if (cmd.startsWith("aysearch_searchquest_local"))
		{
			int npcID = Integer.parseInt(cmd.substring(27));
			player.getRadar().removeAllMarkers();
			for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
			{
				if (npcID == spawn.getNpcId())
				{
					player.getRadar().addMarker(spawn.getX(), spawn.getY(), spawn.getZ());
				}
			}
		}
		else if (cmd.startsWith("aysearch_searchquest_home"))
		{
			questHome(player);
		}
		else if (cmd.startsWith("aysearch_searchquest_un"))
		{
			int page = Integer.parseInt(cmd.substring(24));
			getListItem(player, page, npcHtml, true);
		}
		else if (cmd.startsWith("aysearch_searchquest_rep"))
		{
			int page = Integer.parseInt(cmd.substring(25));
			getListItem(player, page, npcHtml, false);
		}
		return true;
	}
	
	private void questHome(Player player)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/quest.htm");
		player.sendPacket(npcHtml);
	}
	
	private void questInfo(Player player, String command)
	{
		String[] data = command.substring(31).split(" - ");
		int page = Integer.parseInt(data[0]);
		int questId = Integer.parseInt(data[1]);
		int check = Integer.parseInt(data[2]);
		QuestList quest = QuestData.getInstance().getQuest(questId);
		ectInfo(player, quest, page, check);
	}
	
	private void ectInfo(Player player, QuestList quest, int page, int check)
	{
		final var npcHtml = new NpcHtmlMessage(0);
		npcHtml.setFile("data/html/ayengine/search/questinfo.htm");
		
		boolean checkBool = check == 1 ? true : false;
		
		npcHtml.replace("%title%", "Informações da Missão");
		npcHtml.replace("%icon%", "L2UI_CH3.QuestBtn_light");
		npcHtml.replace("%questName%", quest.getQuestName());
		npcHtml.replace("%level%", levelFormat(quest.getLevelMin(), quest.getLevelMax()));
		npcHtml.replace("%type%", quest.getQuestType() < 3 ? "Missão Repetível" : "Missão Única");
		npcHtml.replace("%desc%", quest.getDescription());
		npcHtml.replace("%npc%", quest.getContactNpc() != 0 ? NpcTable.getInstance().getTemplate(quest.getContactNpc()).getName() : "Desconhecido");
		npcHtml.replace("%req%", quest.getRestrictions());
		npcHtml.replace("%button%", "Local");
		npcHtml.replace("%local%", "bypass -h aysearch_searchquest_local " + quest.getContactNpc());
		npcHtml.replace("%back%", "bypass -h aysearch_searchquest_" + (checkBool ? "un " : "rep ") + page);
		
		player.sendPacket(npcHtml);
	}
	
	private void getListItem(Player player, int page, NpcHtmlMessage npcHtml, boolean check)
	{
		int checkInt = check ? 1 : 0;
		
		List<QuestList> allQuests = QuestData.getInstance().getAllQuests(player, check);
		
		int totalPages = (int) Math.ceil((double) allQuests.size() / ITEMS_PER_PAGE);
		if ((page < 1) || (page > totalPages))
		{
			player.sendMessage("Página inválida. Por favor, tente novamente.");
			return;
		}
		
		int startIdx = (page - 1) * ITEMS_PER_PAGE;
		int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allQuests.size());
		List<QuestList> pageQuests = allQuests.subList(startIdx, endIdx);
		
		npcHtml.replace("%title%", "Missões");
		
		// Preenche a lista de quests
		int index = 1;
		for (QuestList quest : pageQuests)
		{
			npcHtml.replace("%icon" + index + "%", "L2UI_CH3.QuestBtn_light");
			npcHtml.replace("%questName" + index + "%", "<a action=\"bypass -h aysearch_searchquest_infoquest " + page + " - " + quest.getId() + " - " + checkInt + "\">" + quest.getQuestName() + "</a>");
			String type = quest.getQuestType() < 3 ? "Missão Repetível" : "Missão Única";
			npcHtml.replace("%level" + index + "%", "Level: <font color=\"LEVEL\">" + levelFormat(quest.getLevelMin(), quest.getLevelMax()) + "</font>" + " Tipo: <font color=\"LEVEL\">" + type + "</font>");
			index++;
		}
		
		// Preenche os espaços vazios se sobrar
		for (; index <= ITEMS_PER_PAGE; index++)
		{
			npcHtml.replace("%icon" + index + "%", "L2UI.SquareBlank");
			npcHtml.replace("%questName" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
			npcHtml.replace("%level" + index + "%", "<img src=\"L2UI.SquareBlank\" width=1 height=19>");
		}
		
		npcHtml.replace("%pages%", String.valueOf(page));
		
		// Navegação
		boolean hasPrev = page > 1;
		boolean hasNext = page < totalPages;
		
		String prevButton = hasPrev ? "<button action=\"bypass -h aysearch_searchquest_" + (check ? "un " : "rep ") + (page - 1) + "\" width=16 height=16 back=\"L2UI_CH3.prev1_over\" fore=\"L2UI_CH3.prev1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		String nextButton = hasNext ? "<button action=\"bypass -h aysearch_searchquest_" + (check ? "un " : "rep ") + (page + 1) + "\" width=16 height=16 back=\"L2UI_CH3.next1_over\" fore=\"L2UI_CH3.next1\">" : "<img src=\"L2UI.SquareBlank\" width=16 height=16>";
		
		npcHtml.replace("%prev%", prevButton);
		npcHtml.replace("%next%", nextButton);
		
		player.sendPacket(npcHtml);
	}
	
	private String levelFormat(int min, int max)
	{
		if ((min == 0) && (max == 0))
		{
			return "Qualquer";
		}
		else if (max == 0)
		{
			return min + " +";
		}
		return min + " ~ " + max;
	}
	
	private static class InstanceHolder
	{
		private static final AySearchQuest _instance = new AySearchQuest();
	}
	
	public static AySearchQuest getInstance()
	{
		return InstanceHolder._instance;
	}
}