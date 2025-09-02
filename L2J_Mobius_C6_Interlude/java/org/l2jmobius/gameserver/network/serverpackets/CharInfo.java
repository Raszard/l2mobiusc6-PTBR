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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.ServerPackets;

public class CharInfo extends ServerPacket
{
	private final Player _player;
	private final Inventory _inventory;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final float _moveMultiplier;
	private final boolean _gmSeeInvis;
	private final boolean _autofarm;
	
	public CharInfo(Player player, boolean gmSeeInvis)
	{
		super(256);
		
		_player = player;
		_inventory = player.getInventory();
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(player.getWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
		_gmSeeInvis = gmSeeInvis;
		_autofarm = player.isAutoPlaying();
	}
	
	@Override
	public void write()
	{
		ServerPackets.CHAR_INFO.writeId(this);
		writeInt(_player.getX());
		writeInt(_player.getY());
		writeInt(_player.getZ());
		writeInt(_player.getBoat() != null ? _player.getBoat().getObjectId() : 0);
		writeInt(_player.getObjectId());
		writeString(_player.getName());
		writeInt(_player.getRace().ordinal());
		writeInt(_player.getAppearance().isFemale());
		if (_player.getClassIndex() == 0)
		{
			writeInt(_player.getClassId().getId());
		}
		else
		{
			writeInt(_player.getBaseClass());
		}
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeInt(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		// c6 new h's
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeInt(_inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
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
		writeInt(_inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeShort(0);
		writeInt(_player.getPvpFlag());
		writeInt(_player.getKarma());
		writeInt(_player.getMAtkSpd());
		writeInt(_player.getPAtkSpd());
		writeInt(_player.getPvpFlag());
		writeInt(_player.getKarma());
		writeInt(_runSpd); // base run speed
		writeInt(_walkSpd); // base walk speed
		writeInt(_runSpd); // swim run speed (calculated by getter)
		writeInt(_walkSpd); // swim walk speed (calculated by getter)
		writeInt(_flyRunSpd); // fly run speed ?
		writeInt(_flyWalkSpd); // fly walk speed ?
		writeInt(_flyRunSpd);
		writeInt(_flyWalkSpd);
		writeDouble(_moveMultiplier);
		writeDouble(_player.getAttackSpeedMultiplier());
		writeDouble(_player.getCollisionRadius());
		writeDouble(_player.getCollisionHeight());
		writeInt(_player.getAppearance().getHairStyle());
		writeInt(_player.getAppearance().getHairColor());
		writeInt(_player.getAppearance().getFace());
		writeString(_autofarm ? "Caça Automática " : _player.getTitle());
		writeInt(_player.getClanId());
		writeInt(_player.getClanCrestId());
		writeInt(_player.getAllyId());
		writeInt(_player.getAllyCrestId());
		// In UserInfo leader rights and siege flags, but here found nothing??
		// Therefore RelationChanged packet with that info is required
		writeInt(0);
		writeByte(!_player.isSitting()); // standing = 1 sitting = 0
		writeByte(_player.isRunning()); // running = 1 walking = 0
		writeByte(_player.isInCombat());
		writeByte(_player.isAlikeDead());
		writeByte(!_gmSeeInvis && _player.getAppearance().isInvisible()); // invisible = 1 visible = 0
		writeByte(_player.getMountType()); // 1 on strider 2 on wyvern 0 no mount
		writeByte(_player.getPrivateStoreType()); // 1 - sellshop
		
		writeShort(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			writeShort(cubicId);
		}
		
		writeByte(_player.isInPartyMatchRoom());
		writeInt(_gmSeeInvis ? (_player.getAbnormalEffect() | Creature.ABNORMAL_EFFECT_STEALTH) : _player.getAbnormalEffect());
		writeByte(_player.getRecomLeft());
		writeShort(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		writeInt(_player.getClassId().getId());
		writeInt(_player.getMaxCp());
		writeInt((int) _player.getCurrentCp());
		writeByte(_player.isMounted() ? 0 : _player.getEnchantEffect());
		writeByte(_player.getTeam()); // team circle around feet 1 = Blue, 2 = red
		writeInt(_player.getClanCrestLargeId());
		writeByte(_player.isNoble()); // Symbol on char menu ctrl+I
		writeByte(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) || _player.isPVPHero()); // Hero Aura
		
		writeByte(_player.isFishing()); // 1: Fishing Mode (Cant be undone by setting back to 0)
		writeInt(_player.getFishX());
		writeInt(_player.getFishY());
		writeInt(_player.getFishZ());
		
		writeInt(_player.getAppearance().getNameColor());
		writeInt(_player.getHeading());
		writeInt(_player.getPledgeClass());
		writeInt(_player.getPledgeType());
		writeInt(_player.getAppearance().getTitleColor());
		if (_player.isCursedWeaponEquiped())
		{
			writeInt(CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquipedId()));
		}
		else
		{
			writeInt(0);
		}
	}
}
