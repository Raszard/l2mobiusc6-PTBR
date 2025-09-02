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

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.Ride;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

public class Doorman extends Folk
{
	private ClanHall _clanHall;
	private static int COND_ALL_FALSE = 0;
	private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static int COND_CASTLE_OWNER = 2;
	private static int COND_HALL_OWNER = 3;
	private static int COND_FORT_OWNER = 4;
	
	public Doorman(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	public ClanHall getClanHall()
	{
		if (_clanHall == null)
		{
			_clanHall = ClanHallTable.getInstance().getNearbyClanHall(getX(), getY(), 500);
		}
		return _clanHall;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
			return;
		}
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			return;
		}
		else if ((condition == COND_CASTLE_OWNER) || (condition == COND_HALL_OWNER) || (condition == COND_FORT_OWNER))
		{
			if (command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if (command.startsWith("open_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(true);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>A porta do Salão do Clã está <font color=\"LEVEL\">aberta</font>.<br>Forasteiros podem entrar no salão do clã enquanto a porta estiver aberta. Por favor, feche-a quando terminar.<br><center><button value=\"Fechar\" action=\"bypass -h npc_" + getObjectId() + "_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					final StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					
					while (st.hasMoreTokens())
					{
						getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
					}
					
					return;
				}
				else // if (condition == COND_FORT_OWNER)
				{
					final StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					
					while (st.hasMoreTokens())
					{
						getFort().openDoor(player, Integer.parseInt(st.nextToken()));
					}
					
					return;
				}
			}
			
			if (command.startsWith("RideWyvern"))
			{
				if (!player.isClanLeader())
				{
					player.sendMessage("Apenas os líderes do Clã são permitidos.");
					return;
				}
				if (player.getPet() == null)
				{
					if (player.isMounted())
					{
						player.sendMessage("Você já tem um pet ou está montado.");
					}
					else
					{
						player.sendMessage("Convoque seu Strider primeiro.");
					}
					return;
				}
				else if ((player.getPet().getNpcId() == 12526) || (player.getPet().getNpcId() == 12527) || (player.getPet().getNpcId() == 12528))
				{
					if ((player.getInventory().getItemByItemId(1460) != null) && (player.getInventory().getItemByItemId(1460).getCount() >= 10))
					{
						if (player.getPet().getLevel() < 55)
						{
							player.sendMessage("Seu Strider não atingiu o nível necessário.");
						}
						else
						{
							if (!player.disarmWeapons())
							{
								return;
							}
							player.getPet().unSummon(player);
							player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
							final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
							player.sendPacket(mount);
							player.broadcastPacket(mount);
							player.setMountType(mount.getMountType());
							player.addSkill(SkillTable.getInstance().getSkill(4289, 1));
							player.sendMessage("O wyvern foi convocado com sucesso!");
						}
					}
					else
					{
						player.sendMessage("Você precisa de 10 Cristais: Grau B.");
					}
					return;
				}
				else
				{
					player.sendMessage("Desinvoque seu pet.");
					return;
				}
			}
			else if (command.startsWith("close_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>A porta do Salão do Clã está <font color=\"LEVEL\">fechada</font>.<br>Bom dia!<br><center><button value=\"Voltar\" action=\"bypass -h npc_" + getObjectId() + "_Chat\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					final StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					
					while (st.hasMoreTokens())
					{
						getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
					}
					
					return;
				}
				else if (condition == COND_FORT_OWNER)
				{
					final StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					
					while (st.hasMoreTokens())
					{
						getFort().closeDoor(player, Integer.parseInt(st.nextToken()));
					}
					
					return;
				}
			}
		}
		
		super.onBypassFeedback(player, command);
	}
	
	/**
	 * this is called when a player interacts with this NPC.
	 * @param player the player
	 */
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
			
			// Send a Server->Client packet MyTargetSelected to the Player player
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
			showMessageWindow(player);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Show message window.
	 * @param player the player
	 */
	public void showMessageWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/doorman/" + getTemplate().getNpcId() + "-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "data/html/doorman/" + getTemplate().getNpcId() + "-busy.htm"; // Busy because of siege
		}
		else if (condition == COND_CASTLE_OWNER)
		{
			filename = "data/html/doorman/" + getTemplate().getNpcId() + ".htm"; // Owner message window
		}
		else if (condition == COND_FORT_OWNER)
		{
			filename = "data/html/doorman/fortress/" + getTemplate().getNpcId() + ".htm"; // Owner message window
		}
		
		// Prepare doorman for clan hall
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		if (getClanHall() != null)
		{
			if (condition == COND_HALL_OWNER)
			{
				str = "<html><body>Olá!<br><font color=\"55FFFF\">" + getName() + "</font>, Sinto-me honrado em servir ao seu clã.<br>Como posso ajudá-lo?<br>";
				str += "<center><br><button value=\"Abra a Porta\" action=\"bypass -h npc_%objectId%_open_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>";
				str += "<button value=\"Feche a Porta\" action=\"bypass -h npc_%objectId%_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>";
				if ((getClanHall().getId() >= 36) && (getClanHall().getId() <= 41))
				{
					str += "<button value=\"Trocar Wyvern\" action=\"bypass -h npc_%objectId%_RideWyvern\" width=85 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
				}
				else
				{
					str += "</center></body></html>";
				}
			}
			else
			{
				final Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
				if ((owner != null) && (owner.getLeader() != null))
				{
					str = "<html><body>Olá!<br>Este salão do clã é propriedade de <font color=\"55FFFF\">" + owner.getLeader().getName() + " que é o Líder do Clã ";
					str += owner.getName() + "</font>.<br>";
					str += "Lamento, mas apenas os membros do clã que pertencem ao Clã <font color=\"55FFFF\">" + owner.getName() + "</font> pode entrar no Salão do Clã.</body></html>";
				}
				else
				{
					str = "<html><body>" + getName() + ":<br1>Salão do Clã <font color=\"LEVEL\">" + getClanHall().getName() + "</font> não tem clã proprietário.<br>Você pode alugá-lo em leiloeiros.</body></html>";
				}
			}
			html.setHtml(str);
		}
		else
		{
			html.setFile(filename);
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	/**
	 * Validate condition.
	 * @param player the player
	 * @return the int
	 */
	private int validateCondition(Player player)
	{
		if (player.getClan() != null)
		{
			// Prepare doorman for clan hall
			if (getClanHall() != null)
			{
				if (player.getClanId() == getClanHall().getOwnerId())
				{
					return COND_HALL_OWNER;
				}
				return COND_ALL_FALSE;
			}
			// Prepare doorman for Castle
			if ((getCastle() != null) && (getCastle().getCastleId() > 0) && (getCastle().getOwnerId() == player.getClanId()))
			{
				return COND_CASTLE_OWNER; // Owner
			}
			// Prepare doorman for Fortress
			if ((getFort() != null) && (getFort().getFortId() > 0) && (getFort().getOwnerId() == player.getClanId()))
			{
				return COND_FORT_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
}
