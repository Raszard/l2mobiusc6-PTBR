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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.RecipeManager;
import org.l2jmobius.gameserver.model.QuestList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;

public class QuestData extends RecipeManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(QuestData.class.getName());
	
	private final Map<Integer, QuestList> _lists = new HashMap<>();
	
	protected QuestData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_lists.clear();
		parseDatapackFile("data/Quests.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _lists.size() + " quests.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("quest".equalsIgnoreCase(d.getNodeName()))
				{
					NamedNodeMap attrs = d.getAttributes();
					Node att = attrs.getNamedItem("id");
					if (att == null)
					{
						LOGGER.severe(getClass().getSimpleName() + ": Missing id for recipe item, skipping.");
						continue;
					}
					int id = Integer.parseInt(att.getNodeValue());
					att = attrs.getNamedItem("name");
					if (att == null)
					{
						LOGGER.severe(getClass().getSimpleName() + ": Missing name for recipe item id: " + id + ", skipping");
						continue;
					}
					String questName = att.getNodeValue();
					int min = -1;
					int max = -1;
					int type = -1;
					String entity = "";
					int contactNpc = -1;
					String restrictions = "";
					String description = "";
					ArrayList<Integer> classes = new ArrayList<>();
					for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
					{
						if ("lvlMin".equalsIgnoreCase(c.getNodeName()))
						{
							min = Integer.parseInt(c.getAttributes().getNamedItem("val").getNodeValue());
						}
						else if ("lvlMax".equalsIgnoreCase(c.getNodeName()))
						{
							max = Integer.parseInt(c.getAttributes().getNamedItem("val").getNodeValue());
						}
						else if ("questType".equalsIgnoreCase(c.getNodeName()))
						{
							type = Integer.parseInt(c.getAttributes().getNamedItem("val").getNodeValue());
						}
						else if ("entityName".equalsIgnoreCase(c.getNodeName()))
						{
							entity = c.getAttributes().getNamedItem("val").getNodeValue();
						}
						else if ("contactNpc".equalsIgnoreCase(c.getNodeName()))
						{
							contactNpc = Integer.parseInt(c.getAttributes().getNamedItem("val").getNodeValue());
						}
						else if ("restrictions".equalsIgnoreCase(c.getNodeName()))
						{
							restrictions = c.getAttributes().getNamedItem("val").getNodeValue();
						}
						else if ("description".equalsIgnoreCase(c.getNodeName()))
						{
							description = c.getAttributes().getNamedItem("val").getNodeValue();
						}
						else if ("class".equalsIgnoreCase(c.getNodeName()))
						{
							if (c.getAttributes().getNamedItem("val").getNodeValue() != "")
							{
								for (String temp : c.getAttributes().getNamedItem("val").getNodeValue().split("-"))
								{
									classes.add(Integer.parseInt(temp));
								}
							}
						}
					}
					
					final QuestList questList = new QuestList(id, questName, min, max, type, entity, contactNpc, restrictions, description, classes);
					_lists.put(id, questList);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading recipe list", e);
		}
	}
	
	public QuestList getQuest(int questId)
	{
		return _lists.get(questId);
	}
	
	public QuestList getQuestByName(String questName)
	{
		for (QuestList quest : _lists.values())
		{
			if (quest.getQuestName() == questName)
			{
				return quest;
			}
		}
		return null;
	}
	
	public ArrayList<QuestList> getAllQuests()
	{
		ArrayList<QuestList> _quests = new ArrayList<>();
		for (QuestList quest : _lists.values())
		{
			_quests.add(quest);
		}
		return _quests;
	}
	
	public ArrayList<QuestList> getAllQuests(Player player, boolean check)
	{
		ArrayList<QuestList> _quests = new ArrayList<>();
		for (QuestList quest : _lists.values())
		{
			String stateName = QuestManager.getInstance().getQuest(quest.getId()).getName();
			QuestState state = player.getQuestState(stateName);
			
			boolean questTypeCondition = check ? quest.getQuestType() >= 3 : quest.getQuestType() < 3;
			
			if (questTypeCondition && race(player, quest) && isLevelEligible(player, quest) && (state == null))
			{
				_quests.add(quest);
			}
		}
		return _quests;
	}
	
	private boolean isLevelEligible(Player player, QuestList quest)
	{
		int playerLevel = player.getLevel();
		int levelMin = quest.getLevelMin();
		int levelMax = quest.getLevelMax();
		
		return ((levelMin == 0) && (playerLevel <= levelMax)) || ((playerLevel >= levelMin) && (levelMax == 0)) || ((playerLevel >= levelMin) && (playerLevel <= levelMax));
	}
	
	private boolean race(Player player, QuestList quest)
	{
		if (quest.getClasses().contains(player.getClassId().getId()))
		{
			return true;
		}
		else if (quest.getClasses().isEmpty())
		{
			return true;
		}
		return false;
	}
	
	public static QuestData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestData INSTANCE = new QuestData();
	}
}
