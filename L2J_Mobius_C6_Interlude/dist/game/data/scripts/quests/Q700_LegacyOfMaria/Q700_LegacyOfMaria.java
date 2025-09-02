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
package quests.Q700_LegacyOfMaria;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q700_LegacyOfMaria extends Quest
{
	// Items
	private static final int MARIA_LETTER = 9512;
	
	public Q700_LegacyOfMaria()
	{
		super(700, "Legado de Maria");
		registerQuestItems(MARIA_LETTER);
		addStartNpc(84004); // Michaelle
		addTalkId(84004, 84003, 84002);
		addKillId(84005, 84006);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("84004-02.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("84003-02.htm"))
		{
			st.setCond(2);
		}
		else if (event.equals("84004-06.htm"))
		{
			st.takeItems(MARIA_LETTER, -1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() < 75)
				{
					htmltext = "84004-00.htm";
				}
				else
				{
					htmltext = "84004-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case 84004:
					{
						if (cond == 1)
						{
							htmltext = "84004-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "84004-04.htm";
						}
						break;
					}
					case 84003:
					{
						if (cond == 1)
						{
							htmltext = "84003-01.htm";
						}
						break;
					}
					case 84002:
					{
						if (cond == 2)
						{
							htmltext = "84002-01.htm";
							st.setCond(3);
						}
						else if (cond == 3)
						{
							htmltext = "84002-02.htm";
						}
						break;
					}
				}
				
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, 3);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case 84005:
			case 84006:
			{
				if (st.dropItems(MARIA_LETTER, 1, 1, 300000))
				{
					st.setCond(4);
				}
				break;
			}
		}
		
		return null;
	}
}