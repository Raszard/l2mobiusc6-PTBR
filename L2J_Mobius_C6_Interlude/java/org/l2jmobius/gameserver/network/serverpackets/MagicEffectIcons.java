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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * MagicEffectIcons format h (dhd)
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class MagicEffectIcons extends ServerPacket
{
	private final List<Effect> _effects;
	private final List<Effect> _debuffs;
	
	private class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;
		
		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
	
	public MagicEffectIcons()
	{
		_effects = new ArrayList<>();
		_debuffs = new ArrayList<>();
	}
	
	public void addEffect(int skillId, int level, int duration, boolean debuff)
	{
		if ((skillId == 2031) || (skillId == 2032) || (skillId == 2037) || (skillId == 9007) || (skillId == 9008) || (skillId == 9009))
		{
			return;
		}
		if (debuff)
		{
			_debuffs.add(new Effect(skillId, level, duration));
		}
		else
		{
			_effects.add(new Effect(skillId, level, duration));
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.MAGIC_EFFECT_ICONS.writeId(this);
		writeShort(_effects.size() + _debuffs.size());
		for (Effect temp : _effects)
		{
			writeInt(temp._skillId);
			writeShort(temp._level);
			if (temp._duration == -1)
			{
				writeInt(-1);
			}
			else
			{
				writeInt(temp._duration / 1000);
			}
		}
		for (Effect temp : _debuffs)
		{
			writeInt(temp._skillId);
			writeShort(temp._level);
			if (temp._duration == -1)
			{
				writeInt(-1);
			}
			else
			{
				writeInt(temp._duration / 1000);
			}
		}
	}
}