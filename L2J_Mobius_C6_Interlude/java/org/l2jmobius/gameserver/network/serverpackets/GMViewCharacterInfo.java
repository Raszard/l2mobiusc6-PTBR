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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * TODO Add support for Eval. Score dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd rev420 dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh rev478
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhhddd rev551
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo extends ServerPacket
{
	/** The _active char. */
	private final Player _player;
	
	/**
	 * Instantiates a new GM view character info.
	 * @param player the player
	 */
	public GMViewCharacterInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public void write()
	{
		final float moveMultiplier = _player.getMovementSpeedMultiplier();
		final int runSpd = (int) (_player.getRunSpeed() / moveMultiplier);
		final int walkSpd = (int) (_player.getWalkSpeed() / moveMultiplier);
		ServerPackets.GM_VIEW_CHARACTER_INFO.writeId(this);
		writeInt(_player.getX());
		writeInt(_player.getY());
		writeInt(_player.getZ());
		writeInt(_player.getHeading());
		writeInt(_player.getObjectId());
		writeString(_player.getName());
		writeInt(_player.getRace().ordinal());
		writeInt(_player.getAppearance().isFemale());
		writeInt(_player.getClassId().getId());
		writeInt(_player.getLevel());
		writeLong(_player.getExp());
		writeInt(_player.getSTR());
		writeInt(_player.getDEX());
		writeInt(_player.getCON());
		writeInt(_player.getINT());
		writeInt(_player.getWIT());
		writeInt(_player.getMEN());
		writeInt(_player.getMaxHp());
		writeInt((int) _player.getCurrentHp());
		writeInt(_player.getMaxMp());
		writeInt((int) _player.getCurrentMp());
		writeInt(_player.getSp());
		writeInt(_player.getCurrentLoad());
		writeInt(_player.getMaxLoad());
		writeInt(0x28); // unknown
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeInt(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeInt(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		// c6 new h's
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		// end of c6 new h's
		writeInt(_player.getPAtk(null));
		writeInt(_player.getPAtkSpd());
		writeInt(_player.getPDef(null));
		writeInt(_player.getEvasionRate(null));
		writeInt(_player.getAccuracy());
		writeInt(_player.getCriticalHit(null, null));
		writeInt(_player.getMAtk(null, null));
		writeInt(_player.getMAtkSpd());
		writeInt(_player.getPAtkSpd());
		writeInt(_player.getMDef(null, null));
		writeInt(_player.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeInt(_player.getKarma());
		writeInt(runSpd);
		writeInt(walkSpd);
		writeInt(runSpd); // swimspeed
		writeInt(walkSpd); // swimspeed
		writeInt(runSpd);
		writeInt(walkSpd);
		writeInt(runSpd);
		writeInt(walkSpd);
		writeDouble(moveMultiplier);
		writeDouble(_player.getAttackSpeedMultiplier()); // 2.9); //
		writeDouble(_player.getTemplate().getCollisionRadius()); // scale
		writeDouble(_player.getTemplate().getCollisionHeight()); // y offset ??!? fem dwarf 4033
		writeInt(_player.getAppearance().getHairStyle());
		writeInt(_player.getAppearance().getHairColor());
		writeInt(_player.getAppearance().getFace());
		writeInt(_player.isGM()); // builder level
		writeString(_player.getTitle());
		writeInt(_player.getClanId()); // pledge id
		writeInt(_player.getClanCrestId()); // pledge crest id
		writeInt(_player.getAllyId()); // ally id
		writeByte(_player.getMountType()); // mount type
		writeByte(_player.getPrivateStoreType());
		writeByte(_player.hasDwarvenCraft());
		writeInt(_player.getPkKills());
		writeInt(_player.getPvpKills());
		writeShort(_player.getRecomLeft());
		writeShort(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		writeInt(_player.getClassId().getId());
		writeInt(0); // special effects? circles around player...
		writeInt(_player.getMaxCp());
		writeInt((int) _player.getCurrentCp());
		writeByte(_player.isRunning()); // changes the Speed display on Status Window
		writeByte(321);
		writeInt(_player.getPledgeClass()); // changes the text above CP on Status Window
		writeByte(_player.isNoble());
		writeByte(_player.isHero());
		writeInt(_player.getAppearance().getNameColor());
		writeInt(_player.getAppearance().getTitleColor());
	}
}
