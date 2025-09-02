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

import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.ServerPackets;

public class PledgeShowInfoUpdate extends ServerPacket
{
	private final Clan _clan;
	
	public PledgeShowInfoUpdate(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public void write()
	{
		// ddddddddddSdd
		ServerPackets.PLEDGE_SHOW_INFO_UPDATE.writeId(this);
		// sending empty data so client will ask all the info in response ;)
		writeInt(_clan.getClanId());
		writeInt(_clan.getCrestId());
		writeInt(_clan.getLevel()); // clan level
		writeInt(_clan.getFortId() != 0 ? _clan.getFortId() : _clan.getCastleId());
		writeInt(_clan.getHideoutId());
		writeInt(ClanTable.getInstance().getTopRate(_clan.getClanId()));
		writeInt(_clan.getReputationScore()); // clan reputation score
		writeInt(0);
		writeInt(0);
		writeInt(_clan.getAllyId());
		writeString(_clan.getAllyName());
		writeInt(_clan.getAllyCrestId());
		writeInt(_clan.isAtWar());
	}
}
