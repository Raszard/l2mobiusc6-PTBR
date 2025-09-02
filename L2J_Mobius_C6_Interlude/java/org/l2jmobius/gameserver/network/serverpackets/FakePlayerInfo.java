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

import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.network.ServerPackets;

public class FakePlayerInfo extends ServerPacket
{
	private final Npc _activeChar;
	
	public FakePlayerInfo(Npc cha)
	{
		super(256);
		
		_activeChar = cha;
		_activeChar.setClientX(_activeChar.getX());
		_activeChar.setClientY(_activeChar.getY());
		_activeChar.setClientZ(_activeChar.getZ());
	}
	
	@Override
	public void write()
	{
		ServerPackets.CHAR_INFO.writeId(this);
		writeInt(_activeChar.getX());
		writeInt(_activeChar.getY());
		writeInt(_activeChar.getZ());
		writeInt(_activeChar.getHeading());
		writeInt(_activeChar.getObjectId());
		writeString(_activeChar.getFakePlayer().getName());
		writeInt(_activeChar.getFakePlayer().getRace());
		writeInt(_activeChar.getFakePlayer().isFemaleSex());
		writeInt(_activeChar.getFakePlayer().getClassId());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		writeInt(0);
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_RHAND());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_LHAND());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_GLOVES());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_CHEST());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_LEGS());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_FEET());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_RHAND());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		writeInt(_activeChar.getFakePlayer().PAPERDOLL_HAIR2());
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
		writeInt(_activeChar.getFakePlayer().getPvpFlag());
		writeInt(_activeChar.getFakePlayer().getKarma());
		writeInt(_activeChar.getMAtkSpd());
		writeInt(_activeChar.getPAtkSpd());
		writeInt(_activeChar.getFakePlayer().getPvpFlag());
		writeInt(_activeChar.getFakePlayer().getKarma());
		writeInt(_activeChar.getRunSpeed());
		writeInt(_activeChar.getRunSpeed() / 2);
		writeInt(_activeChar.getRunSpeed() / 3);
		writeInt(_activeChar.getRunSpeed() / 3);
		writeInt(_activeChar.getRunSpeed());
		writeInt(_activeChar.getRunSpeed());
		writeInt(_activeChar.getRunSpeed());
		writeInt(_activeChar.getRunSpeed());
		writeDouble(_activeChar.getStat().getMovementSpeedMultiplier());
		writeDouble(_activeChar.getStat().getAttackSpeedMultiplier());
		writeDouble(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayer().getClassId()).getCollisionRadius());
		writeDouble(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayer().getClassId()).getCollisionHeight());
		writeInt(_activeChar.getFakePlayer().getHairStyle());
		writeInt(_activeChar.getFakePlayer().getHairColor());
		writeInt(_activeChar.getFakePlayer().getFace());
		writeString(_activeChar.getFakePlayer().getTitle());
		writeInt(_activeChar.getFakePlayer().getClanId());
		writeInt(_activeChar.getFakePlayer().getClanCrestId());
		writeInt(_activeChar.getFakePlayer().getAllyId());
		writeInt(_activeChar.getFakePlayer().getAllyCrestId());
		writeInt(0);
		writeByte(1);
		writeByte(_activeChar.isRunning());
		writeByte(_activeChar.isInCombat());
		writeByte(_activeChar.isAlikeDead());
		writeByte(0);
		writeByte(0);
		writeByte(0);
		writeShort(0);
		writeByte(0);
		writeInt(_activeChar.getAbnormalEffect());
		writeByte(0);
		writeShort(0);
		writeInt(_activeChar.getFakePlayer().getClassId());
		writeInt(_activeChar.getMaxCp());
		writeInt((int) _activeChar.getStatus().getCurrentCp());
		writeByte(_activeChar.getFakePlayer().getEnchantWeapon());
		writeByte(0);
		writeInt(0); // clan crest
		writeByte(_activeChar.getFakePlayer().isNoble());
		writeByte(_activeChar.getFakePlayer().isHero());
		writeByte(0);
		writeInt(0);
		writeInt(0);
		writeInt(0);
		writeInt(_activeChar.getFakePlayer().nameColor());
		writeInt(0);
		writeInt(_activeChar.getFakePlayer().getPledgeClass());
		writeInt(0);
		writeInt(_activeChar.getFakePlayer().titleColor());
		writeInt(0);
	}
}
