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
package org.l2jmobius.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.model.siege.clanhalls.BanditStrongholdSiege;
import org.l2jmobius.gameserver.model.siege.clanhalls.WildBeastFarmSiege;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author MHard L2EmuRT
 */
public class ClanHallSiegeInfo extends Npc
{
	public ClanHallSiegeInfo(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the Player and the Npc
		{
			// Notify the Player AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showMessageWindow(player, 0);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			showMessageWindow(player, val);
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			
			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
		}
		else if (command.startsWith("Registration"))
		{
			final Clan playerClan = player.getClan();
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String str;
			str = "<html><body>Newspaper!<br>";
			
			switch (getTemplate().getNpcId())
			{
				case 35437:
				{
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
					{
						showMessageWindow(player, 1);
						return;
					}
					if (BanditStrongholdSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Seu clã já está registrado para o cerco, o que mais você deseja?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Adicionar/Remover um membro do cerco</a><br>";
					}
					else if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						str += "Seu clã já está registrado para o cerco, o que mais você deseja?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">Cancelar inscrição</a><br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Adicionar/Remover um membro do cerco</a><br>";
					}
					else
					{
						final int res = BanditStrongholdSiege.getInstance().registerClanOnSiege(player, playerClan);
						if (res == 0)
						{
							str += "Seu clã : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, registrado com sucesso para o salão do clã de cerco.<br>";
							str += "Agora você precisa selecionar no máximo 18 que participarão do cerco, um membro do seu clã.<br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Selecione os membros do cerco</a><br>";
						}
						else if (res == 1)
						{
							str += "Você não passou no teste e não se qualificou para participar do cerco dos ladrões<br>";
							str += "Volte quando terminar.";
						}
						else if (res == 2)
						{
							str += "Infelizmente, você está atrasado. Cinco líderes tribais já entraram com um pedido de registro.<br>";
							str += "Da próxima vez seja mais poderoso";
						}
					}
					break;
				}
				case 35627:
				{
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
					{
						showMessageWindow(player, 1);
						return;
					}
					if (WildBeastFarmSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Seu clã já está registrado para o cerco, o que mais você deseja?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Adicionar/Remover um membro do cerco</a><br>";
					}
					else if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						str += "Seu clã já está registrado para o cerco, o que mais você deseja?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">Cancelar inscrição</a><br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Adicionar/Remover um membro do cerco</a><br>";
					}
					else
					{
						final int res = WildBeastFarmSiege.getInstance().registerClanOnSiege(player, playerClan);
						if (res == 0)
						{
							str += "Sey clã : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, registrado com sucesso para o salão do clã de cerco.<br>";
							str += "Agora você precisa selecionar no máximo 18 que participarão do cerco, um membro do seu clã.<br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Selecione os membros do cerco</a><br>";
						}
						else if (res == 1)
						{
							str += "Você não passou no teste e não se qualificou para participar do cerco dos ladrões<br>";
							str += "Volte quando terminar.";
						}
						else if (res == 2)
						{
							str += "Infelizmente, você está atrasado. Cinco líderes tribais já entraram com um pedido de registro.<br>";
							str += "Da próxima vez seja mais poderoso";
						}
					}
					break;
				}
			}
			
			str += "</body></html>";
			html.setHtml(str);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("UnRegister"))
		{
			final Clan playerClan = player.getClan();
			if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
			{
				LOGGER.warning("Attention!!! " + player + " used packet hack, try unregister clan.");
				return;
			}
			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showMessageWindow(player, 3);
				return;
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String str;
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
			{
				if (BanditStrongholdSiege.getInstance().unRegisterClan(playerClan))
				{
					str = "<html><body>Newspaper!<br>";
					str += "Seu clã : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, removido com sucesso do registro no salão do clã do cerco.<br>";
					str += "</body></html>";
					html.setHtml(str);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
			}
			else
			{
				LOGGER.warning("Attention!!! " + player + " used packet hack, try unregister clan.");
			}
		}
		else if (command.startsWith("PlayerList"))
		{
			final Clan playerClan = player.getClan();
			if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
			{
				return;
			}
			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showMessageWindow(player, 3);
				return;
			}
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
			{
				showPlayersList(playerClan, player);
			}
		}
		else if (command.startsWith("addPlayer"))
		{
			final Clan playerClan = player.getClan();
			if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
			{
				return;
			}
			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showMessageWindow(player, 3);
				return;
			}
			final String val = command.substring(10);
			if (playerClan.getClanMember(val) == null)
			{
				return;
			}
			BanditStrongholdSiege.getInstance().addPlayer(playerClan, val);
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
			{
				showPlayersList(playerClan, player);
			}
		}
		else if (command.startsWith("removePlayer"))
		{
			final Clan playerClan = player.getClan();
			if ((playerClan == null) || !playerClan.getLeaderName().equalsIgnoreCase(player.getName()) || (playerClan.getLevel() < 4))
			{
				return;
			}
			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showMessageWindow(player, 3);
				return;
			}
			final String val = command.substring(13);
			if (playerClan.getClanMember(val) != null)
			{
				BanditStrongholdSiege.getInstance().removePlayer(playerClan, val);
			}
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
			{
				showPlayersList(playerClan, player);
			}
		}
	}
	
	public void showPlayersList(Clan playerClan, Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		str = "<html><body>Jornal!<br>";
		str += "Seu clã : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>. selecionar participantes para o cerco.<br><br>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Registrar banheiros</td><td width=110 align=center>Ação</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		for (String temp : BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan))
		{
			str += "<tr><td width=170>" + temp + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_removePlayer " + temp + "\"> Remover</a></td></tr>";
		}
		str += "</table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Clan Members</td><td width=110 align=center>Ação</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		for (ClanMember temp : playerClan.getMembers())
		{
			if (!BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan).contains(temp.getName()))
			{
				str += "<tr><td width=170>" + temp.getName() + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_addPlayer " + temp.getName() + "\"> Adicionar</a></td></tr>";
			}
		}
		str += "</table>";
		str += "</body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	public void showMessageWindow(Player player, int value)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		long startSiege = 0;
		final int npcId = getTemplate().getNpcId();
		String filename;
		if (value == 0)
		{
			filename = "data/html/default/" + npcId + ".htm";
		}
		else
		{
			filename = "data/html/default/" + npcId + "-" + value + ".htm";
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		if (npcId == 35382)
		{
			// startSiege=FortResistSiegeManager.getInstance().getSiegeDate().getTimeInMillis();
		}
		else if ((npcId == 35437) || (npcId == 35627))
		{
			ClanHall clanhall = null;
			String clans = "";
			clans += "<table width=280 border=0>";
			int clanCount = 0;
			
			switch (npcId)
			{
				case 35437:
				{
					clanhall = ClanHallTable.getInstance().getClanHallById(35);
					startSiege = BanditStrongholdSiege.getInstance().getSiegeDate().getTimeInMillis();
					for (String a : BanditStrongholdSiege.getInstance().getRegisteredClans())
					{
						clanCount++;
						clans += "<tr><td><font color=\"LEVEL\">" + a + "</font>  (Número :" + BanditStrongholdSiege.getInstance().getPlayersCount(a) + "pessoas.)</td></tr>";
					}
					break;
				}
				/*
				 * case 35627: clanhall = ClanHallManager.getInstance().getClanHallById(63); startSiege=WildBeastFarmSiege.getInstance().getSiegeDate().getTimeInMillis(); for (String a : WildBeastFarmSiege.getInstance().getRegisteredClans()) { clanCount++;
				 * clans+="<tr><td><font color=\"LEVEL\">"+a+"</font>  (Number :"+BanditStrongholdSiege.getInstance().getPlayersCount(a)+"people.)</td></tr>"; } break;
				 */
			}
			while (clanCount < 5)
			{
				clans += "<tr><td><font color=\"LEVEL\">**Não logado**</font>  (Quantidade : pessoas.)</td></tr>";
				clanCount++;
			}
			clans += "</table>";
			html.replace("%clan%", clans);
			final Clan clan = clanhall == null ? null : clanhall.getOwnerClan();
			String clanName;
			if (clan == null)
			{
				clanName = "NPC";
			}
			else
			{
				clanName = clan.getName();
			}
			html.replace("%clanname%", clanName);
		}
		
		final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		html.replace("%SiegeDate%", format.format(startSiege));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
