package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.donation.DonationManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Bank extends Folk
{
	public Bank(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("donate"))
		{
			DonationManager.getInstance().showIndexWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/alpha/bank/" + this.getNpcId() + ".htm";
		if (val != 0)
		{
			name = "data/html/alpha/bank/" + this.getNpcId() + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		player.sendPacket(html);
	}
}
