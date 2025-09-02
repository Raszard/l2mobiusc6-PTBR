package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.util.Util;

public class Aio extends Folk
{
	public Aio(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("changename"))
		{
			try
			{
				String val = command.substring(11);
				if (!Util.isAlphaNumeric(val))
				{
					player.sendMessage("Nome de personagem inválido.");
					return;
				}
				
				if (CharNameTable.getInstance().getPlayerObjectId(val) > 0)
				{
					player.sendMessage("Aviso, o nome " + val + " já existe.");
					return;
				}
				
				if (!player.isDonator())
				{
					if ((player.getInventory().getItemByItemId(9504) == null))
					{
						this.showChatWindow(player);
						player.sendMessage("Contagem de itens incorreta.");
						return;
					}
					player.destroyItemByItemId("Name Change", 9504, 1, player, true);
				}
				World.getInstance().removeFromAllPlayers(player);
				player.setName(val);
				player.store();
				World.getInstance().addToAllPlayers(player);
				player.sendMessage("Seu nome foi alterado para " + val);
				player.broadcastUserInfo();
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (Player member : player.getParty().getPartyMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(player, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().updateClanMember(player);
					player.getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
					player.sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("A caixa do nome do jogador não pode estar vazia.");
			}
		}
		// Change clan name
		else if (command.startsWith("changeclanname"))
		{
			try
			{
				String val = command.substring(15);
				if ((player.getClan() == null) || (!player.isClanLeader()))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				if (!Util.isAlphaNumeric(val))
				{
					player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
					return;
				}
				
				if (ClanTable.getInstance().getClanByName(val) != null)
				{
					player.sendMessage("Aviso, o nome de clã " + val + " já existe.");
					return;
				}
				
				if (!player.isDonator())
				{
					if ((player.getInventory().getItemByItemId(9504) == null))
					{
						this.showChatWindow(player);
						player.sendMessage("Contagem de itens incorreta.");
						return;
					}
					player.destroyItemByItemId("Clan Name Change", 9504, 1, player, true);
				}
				player.getClan().setName(val);
				player.getClan().updateClanInDB();
				player.sendMessage("O nome do seu clã foi alterado para " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (Player member : player.getParty().getPartyMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("A caixa do nome do clã não pode estar vazia.");
			}
		}
		else if (command.startsWith("changesex"))
		{
			changeSex(player, command);
		}
		else if (command.startsWith("changegender"))
		{
			if (!player.isDonator())
			{
				if ((player.getInventory().getItemByItemId(9505) == null))
				{
					this.showChatWindow(player);
					player.sendMessage("Contagem de itens incorreta.");
					return;
				}
				player.destroyItemByItemId("changeGender", 9505, 1, player, true);
			}
			String[] data = command.substring(13).split(" - ");
			String hair = data[0];
			String cHair = data[1];
			String face = data[2];
			player.getAppearance().setHairStyle(AyUtils.getInstance().hairTypeInt(hair));
			player.getAppearance().setHairColor(AyUtils.getInstance().hairTypeInt(cHair));
			player.getAppearance().setFace(AyUtils.getInstance().hairTypeInt(face));
			player.sendMessage("Sua aparência foi alterada.");
			if (player.getAppearance().isFemale())
			{
				player.getAppearance().setMale();
			}
			else
			{
				player.getAppearance().setFemale();
			}
			Player.setSexDB(player, 1);
			player.spawnMe(player.getX(), player.getY(), player.getZ());
			player.sendPacket(new InventoryUpdate());
			player.sendPacket(new StatusUpdate(player));
			player.sendMessage("Seu gênero foi alterado, você será desconectado em 3 segundos!");
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe();
			ThreadPool.schedule(() -> player.logout(false), 3000);
		}
		else if (command.startsWith("back"))
		{
			showChatWindow(player, 0);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void changeSex(Player player, String command)
	{
		String name = "data/html/alpha/aio/" + this.getNpcId() + "-" + 2 + ".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		if (player.getAppearance().isFemale())
		{
			html.replace("%hair%", "Tipo A;Tipo B;Tipo C;Tipo D;Tipo E;");
		}
		else
		{
			html.replace("%hair%", "Tipo A;Tipo B;Tipo C;Tipo D;Tipo E;Tipo F;Tipo G;");
		}
		html.replace("%chair%", "Tipo A;Tipo B;Tipo C;Tipo D;");
		html.replace("%face%", "Tipo A;Tipo B;Tipo C;");
		html.replace("%change%", "bypass -h npc_" + getObjectId() + "_changegender \\$hair - \\$chair - \\$face");
		html.replace("%back%", "bypass -h npc_" + getObjectId() + "_back");
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/alpha/aio/" + this.getNpcId() + ".htm";
		if (val != 0)
		{
			name = "data/html/alpha/aio/" + this.getNpcId() + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		player.sendPacket(html);
	}
}
