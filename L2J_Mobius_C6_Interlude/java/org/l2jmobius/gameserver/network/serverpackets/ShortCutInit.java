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

import java.util.Collection;

import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * ShortCutInit format d *(1dddd)/(2ddddd)/(3dddd)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends ServerPacket
{
	private Collection<ShortCut> _shortCuts;
	private Player _player;
	
	public ShortCutInit(Player player)
	{
		_player = player;
		if (_player == null)
		{
			return;
		}
		_shortCuts = _player.getAllShortCuts();
	}
	
	@Override
	public void write()
	{
		ServerPackets.SHORT_CUT_INIT.writeId(this);
		writeInt(_shortCuts.size());
		for (ShortCut sc : _shortCuts)
		{
			writeInt(sc.getType());
			writeInt(sc.getSlot() + (sc.getPage() * 12));
			switch (sc.getType())
			{
				case ShortCut.TYPE_ITEM: // 1
				{
					writeInt(sc.getId());
					writeInt(1);
					writeInt(-1);
					writeInt(0);
					writeInt(0);
					writeShort(0);
					writeShort(0);
					break;
				}
				case ShortCut.TYPE_SKILL: // 2
				{
					writeInt(sc.getId());
					writeInt(sc.getLevel());
					writeByte(0); // C5
					writeInt(1); // C6
					break;
				}
				case ShortCut.TYPE_ACTION: // 3
				{
					writeInt(sc.getId());
					writeInt(1); // C6
					break;
				}
				case ShortCut.TYPE_MACRO: // 4
				{
					writeInt(sc.getId());
					writeInt(1); // C6
					break;
				}
				case ShortCut.TYPE_RECIPE: // 5
				{
					writeInt(sc.getId());
					writeInt(1); // C6
					break;
				}
				default:
				{
					writeInt(sc.getId());
					writeInt(1); // C6
				}
			}
		}
	}
}
