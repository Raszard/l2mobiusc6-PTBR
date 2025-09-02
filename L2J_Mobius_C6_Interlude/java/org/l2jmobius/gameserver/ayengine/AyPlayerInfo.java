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
package org.l2jmobius.gameserver.ayengine;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.Util;

public class AyPlayerInfo
{
	private static final String[] subArray =
	{
		"%hair%",
		"%head%",
		"%face%",
		"%shirt%",
		"%neck%",
		"%lhand%",
		"%chest%",
		"%rhand%",
		"%learring%",
		"%rearring%",
		"%gloves%",
		"%legs%",
		"%feet%",
		"%lring%",
		"%rring%"
	};
	
	private static final String[] iconArray =
	{
		"L2UI_CUSTOM.InventoryEar",
		"L2UI_CUSTOM.InventoryHead",
		"L2UI_CUSTOM.InventoryEar",
		"L2UI_CUSTOM.InventoryShirt",
		"L2UI_CUSTOM.InventoryNeck",
		"L2UI_CUSTOM.InventoryLHand",
		"L2UI_CUSTOM.InventoryChest",
		"L2UI_CUSTOM.InventoryRHand",
		"L2UI_CUSTOM.InventoryEarring",
		"L2UI_CUSTOM.InventoryEarring",
		"L2UI_CUSTOM.InventoryGloves",
		"L2UI_CUSTOM.InventoryLegs",
		"L2UI_CUSTOM.InventoryFeet",
		"L2UI_CUSTOM.InventoryRing",
		"L2UI_CUSTOM.InventoryRing"
	};
	
	private static final int[] typesArray =
	{
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_FACE,
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_NECK,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_LEAR,
		Inventory.PAPERDOLL_REAR,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_LFINGER,
		Inventory.PAPERDOLL_RFINGER
	};
	
	public void gatherCharacterInfo(Player activeChar, Player player)
	{
		final NpcHtmlMessage stats = new NpcHtmlMessage(5);
		stats.setFile("data/html/ayengine/charinfo.htm");
		stats.replace("%name%", player.getName());
		stats.replace("%level%", String.valueOf(player.getLevel()));
		final Clan playerClan = ClanTable.getInstance().getClan(player.getClanId());
		if (playerClan != null)
		{
			stats.replace("%clan%", playerClan.getName());
		}
		else
		{
			stats.replace("%clan%", "Sem Clã");
		}
		stats.replace("%xp%", String.valueOf(player.getExp()));
		stats.replace("%sp%", String.valueOf(player.getSp()));
		stats.replace("%class%", player.getTemplate().getClassName());
		stats.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		stats.replace("%karma%", String.valueOf(player.getKarma()));
		stats.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		stats.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		stats.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		stats.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		stats.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		stats.replace("%pkkills%", String.valueOf(player.getPkKills()));
		stats.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / player.getMaxLoad()) * 100, 2)));
		stats.replace("%patk%", String.valueOf(player.getPAtk(null)));
		stats.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		stats.replace("%pdef%", String.valueOf(player.getPDef(null)));
		stats.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		stats.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		stats.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		stats.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		stats.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		stats.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		stats.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		stats.replace("%str%", player.getSTR());
		stats.replace("%dex%", player.getDEX());
		stats.replace("%con%", player.getCON());
		stats.replace("%int%", player.getINT());
		stats.replace("%wit%", player.getWIT());
		stats.replace("%men%", player.getMEN());
		stats.replace("%bypassstat%", "ayplayerinfo_stat " + player.getObjectId());
		stats.replace("%bypassinv%", "ayplayerinfo_inv " + player.getObjectId() + " - 0 - 0");
		activeChar.sendPacket(stats);
	}
	
	private void getInv(Player player, Player otherPlayer, int itemObjId, int isLeg)
	{
		final NpcHtmlMessage inv = new NpcHtmlMessage(5);
		inv.setFile("data/html/ayengine/charinv.htm");
		final Item item = otherPlayer.getInventory().getItemByObjectId(itemObjId);
		inv.replace("%bypassstat%", "ayplayerinfo_stat " + otherPlayer.getObjectId());
		inv.replace("%bypassinv%", "ayplayerinfo_inv " + otherPlayer.getObjectId() + " - 0 - 0");
		for (int i = 0; i < typesArray.length; i++)
		{
			if (otherPlayer.getInventory().getPaperdollItem(typesArray[i]) != null)
			{
				// ItemTable.getInstance().getTemplate(temp.getItemId()).getBodyPart()
				final Item newItem = otherPlayer.getInventory().getPaperdollItem(typesArray[i]);
				final String icon = ItemTable.getInstance().getTemplate(newItem.getItemId()).getIcon();
				final int bodyPart = ItemTable.getInstance().getTemplate(newItem.getItemId()).getBodyPart();
				if (bodyPart == ItemTemplate.SLOT_FULL_ARMOR)
				{
					String iconU = icon.replace("_ul_", "_u_");
					String iconL = icon.replace("_ul_", "_l_");
					inv.replace("%legs%", iconL);
					inv.replace("%width12%", "32");
					inv.replace("%height12%", "32");
					inv.replace(subArray[i], iconU);
					inv.replace("%width" + (i + 1) + "%", "32");
					inv.replace("%height" + (i + 1) + "%", "32");
					inv.replace("%bypass" + (i + 1) + "%", "ayplayerinfo_inv " + otherPlayer.getObjectId() + " - " + newItem.getObjectId() + " - " + 0);
					inv.replace("%bypass12%", "ayplayerinfo_inv " + otherPlayer.getObjectId() + " - " + newItem.getObjectId() + " - " + 1);
				}
				else
				{
					inv.replace(subArray[i], icon);
					inv.replace("%width" + (i + 1) + "%", "32");
					inv.replace("%height" + (i + 1) + "%", "32");
					inv.replace("%bypass" + (i + 1) + "%", "ayplayerinfo_inv " + otherPlayer.getObjectId() + " - " + newItem.getObjectId() + " - " + 0);
				}
			}
			else
			{
				inv.replace(subArray[i], iconArray[i]);
				inv.replace("%width" + (i + 1) + "%", "34");
				inv.replace("%height" + (i + 1) + "%", "34");
			}
		}
		if ((itemObjId != 0) && (item != null))
		{
			inv.replace("%icon1%", ItemTable.getInstance().getTemplate(item.getItemId()).getIcon());
			inv.replace("%itemName1%", formatName(item));
			inv.replace("%grade1%", item.getGrade());
			inv.replace("%widthicon1%", "32");
			inv.replace("%heighticon1%", "32");
		}
		else
		{
			inv.replace("%icon1%", "L2UI_CUSTOM.TutorialHelp");
			inv.replace("%itemName1%", "Selecione um item!");
			inv.replace("%grade1%", "");
			inv.replace("%widthicon1%", "34");
			inv.replace("%heighticon1%", "34");
		}
		player.sendPacket(inv);
	}
	
	public void useBypass(Player player, String cmd)
	{
		if (cmd.startsWith("ayplayerinfo_inv"))
		{
			final String[] data = cmd.substring(17).split(" - ");
			final int playerObjId = Integer.parseInt(data[0]);
			final int itemObjId = Integer.parseInt(data[1]);
			final int isLeg = Integer.parseInt(data[2]);
			final Player otherPlayer = World.getInstance().getPlayer(playerObjId);
			getInv(player, otherPlayer, itemObjId, isLeg);
		}
		else if (cmd.startsWith("ayplayerinfo_stat"))
		{
			final int playerObjId = Integer.parseInt(cmd.substring(18));
			final Player otherPlayer = World.getInstance().getPlayer(playerObjId);
			gatherCharacterInfo(player, otherPlayer);
		}
	}
	
	private String formatName(Item item)
	{
		String itemName = item.getEnchantLevel() > 0 ? "<font color=\"b09979\">+" + item.getEnchantLevel() + "</font> " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() : ItemTable.getInstance().getTemplate(item.getItemId()).getName();
		if (itemName.contains(" - "))
		{
			String[] crysItem = itemName.split(" - ");
			itemName = crysItem[0] + "<font color=\"LEVEL\"> " + crysItem[1] + "</font>";
		}
		return itemName;
	}
	
	private static class InstanceHolder
	{
		private static final AyPlayerInfo _instance = new AyPlayerInfo();
	}
	
	public static AyPlayerInfo getInstance()
	{
		return InstanceHolder._instance;
	}
}
