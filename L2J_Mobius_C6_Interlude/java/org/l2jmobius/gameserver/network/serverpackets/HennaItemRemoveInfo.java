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
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.network.ServerPackets;

public class HennaItemRemoveInfo extends ServerPacket
{
	private final Player _player;
	private final Henna _henna;
	
	public HennaItemRemoveInfo(Henna henna, Player player)
	{
		_henna = henna;
		_player = player;
	}
	
	@Override
	public void write()
	{
		ServerPackets.HENNA_ITEM_REMOVE_INFO.writeId(this);
		writeInt(_henna.getSymbolId()); // symbol Id
		writeInt(_henna.getDyeId()); // item id of dye
		writeInt(Henna.getRequiredDyeAmount() / 2); // amount of given dyes
		writeInt(_henna.getPrice() / 5); // amount of required adenas
		writeInt(1); // able to remove or not 0 is false and 1 is true
		writeInt(_player.getAdena());
		writeInt(_player.getINT()); // current INT
		writeByte(_player.getINT() - _henna.getINT()); // equip INT
		writeInt(_player.getSTR()); // current STR
		writeByte(_player.getSTR() - _henna.getSTR()); // equip STR
		writeInt(_player.getCON()); // current CON
		writeByte(_player.getCON() - _henna.getCON()); // equip CON
		writeInt(_player.getMEN()); // current MEM
		writeByte(_player.getMEN() - _henna.getMEN()); // equip MEM
		writeInt(_player.getDEX()); // current DEX
		writeByte(_player.getDEX() - _henna.getDEX()); // equip DEX
		writeInt(_player.getWIT()); // current WIT
		writeByte(_player.getWIT() - _henna.getWIT()); // equip WIT
	}
}