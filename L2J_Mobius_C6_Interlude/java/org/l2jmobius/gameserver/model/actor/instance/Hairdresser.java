package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Hairdresser extends Folk
{
	public Hairdresser(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("change"))
		{
			try
			{
				if (!player.isDonator())
				{
					if ((player.getInventory().getItemByItemId(9506) == null))
					{
						this.showChatWindow(player);
						player.sendMessage("Contagem de itens incorreta.");
						return;
					}
					player.destroyItemByItemId("change", 9506, 1, player, true);
				}
				String[] data = command.substring(7).split(" - ");
				String hair = data[0];
				String cHair = data[1];
				String face = data[2];
				player.getAppearance().setHairStyle(AyUtils.getInstance().hairTypeInt(hair));
				player.getAppearance().setHairColor(AyUtils.getInstance().hairTypeInt(cHair));
				player.getAppearance().setFace(AyUtils.getInstance().hairTypeInt(face));
				player.store();
				player.sendMessage("Sua aparência foi trocada.");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				this.showChatWindow(player);
				player.sendMessage("Invalid input. Please try again.");
				return;
			}
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/alpha/hairdresser/" + this.getNpcId() + ".htm";
		if (val != 0)
		{
			name = "data/html/alpha/hairdresser/" + this.getNpcId() + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		if (player.getAppearance().isFemale())
		{
			html.replace("%hair%", "Tipo A;Tipo B;Tipo C;Tipo D;Tipo E;Tipo F;Tipo G;");
		}
		else
		{
			html.replace("%hair%", "Tipo A;Tipo B;Tipo C;Tipo D;Tipo E;");
		}
		html.replace("%chair%", "Tipo A;Tipo B;Tipo C;Tipo D;");
		html.replace("%face%", "Tipo A;Tipo B;Tipo C;");
		player.sendPacket(html);
	}
}
