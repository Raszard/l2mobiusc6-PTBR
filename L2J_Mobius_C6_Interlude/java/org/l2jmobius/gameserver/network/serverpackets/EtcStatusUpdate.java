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
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.skill.effects.EffectCharge;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends ServerPacket
{
	private final Player _player;
	private final EffectCharge _effect;
	
	public EtcStatusUpdate(Player player)
	{
		_player = player;
		_effect = (EffectCharge) _player.getFirstEffect(EffectType.CHARGE);
	}
	
	@Override
	public void write()
	{
		ServerPackets.ETC_STATUS_UPDATE.writeId(this);
		// several icons to a separate line (0 = disabled)
		if (_effect != null)
		{
			writeInt(_effect.getLevel()); // 1-7 increase force, level
		}
		else
		{
			writeInt(0); // 1-7 increase force, level
		}
		writeInt(_player.getWeightPenalty()); // 1-4 weight penalty, level (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeInt(_player.isInRefusalMode() || _player.isChatBanned()); // 1 = block all chat
		// writeD(0); // 1 = danger area
		writeInt(_player.isInsideZone(ZoneId.DANGER_AREA)/* || _player.isInDangerArea() */); // 1 = danger area
		writeInt(Math.min(_player.getExpertisePenalty() + _player.getMasteryPenalty() + _player.getMasteryWeapPenalty(), 1)); // 1 = grade penalty
		writeInt(_player.getCharmOfCourage()); // 1 = charm of courage (no xp loss in siege..)
		writeInt(_player.getDeathPenaltyBuffLevel()); // 1-15 death penalty, level (combat ability decreased due to death)
	}
}
