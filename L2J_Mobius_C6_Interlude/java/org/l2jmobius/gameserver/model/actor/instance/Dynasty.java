package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Dynasty extends Folk
{
	public Dynasty(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("multisell"))
		{
			String stateName = QuestManager.getInstance().getQuest(700).getName();
			QuestState state = player.getQuestState(stateName);
			if (state != null)
			{
				if (state.isCompleted())
				{
					super.onBypassFeedback(player, command);
				}
				else
				{
					showChatWindow(player, 1);
				}
			}
			else
			{
				showChatWindow(player, 1);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/alpha/dynasty/" + this.getNpcId() + ".htm";
		if (val != 0)
		{
			name = "data/html/alpha/dynasty/" + this.getNpcId() + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		player.sendPacket(html);
	}
}
