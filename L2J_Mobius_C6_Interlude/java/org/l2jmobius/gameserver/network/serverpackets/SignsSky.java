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

import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Changes the sky color depending on the outcome of the Seven Signs competition. packet type id 0xf8 format: c h
 * @author Tempy
 */
public class SignsSky extends ServerPacket
{
	private int _state = 0;
	
	public SignsSky()
	{
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			if (compWinner == SevenSigns.CABAL_DAWN)
			{
				_state = 2;
			}
			else if (compWinner == SevenSigns.CABAL_DUSK)
			{
				_state = 1;
			}
		}
	}
	
	public SignsSky(int state)
	{
		_state = state;
	}
	
	@Override
	public void write()
	{
		ServerPackets.SIGNS_SKY.writeId(this);
		if (_state == 2)
		{
			writeShort(258);
		}
		else if (_state == 1)
		{
			writeShort(257);
			// else
			// writeShort(256);
		}
	}
}
