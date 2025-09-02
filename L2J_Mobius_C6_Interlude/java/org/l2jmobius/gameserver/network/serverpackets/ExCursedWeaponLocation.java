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

import java.util.List;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Format: (ch) d[ddddd].
 * @author -Wooden-
 */
public class ExCursedWeaponLocation extends ServerPacket
{
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	/**
	 * Instantiates a new ex cursed weapon location.
	 * @param cursedWeaponInfo the cursed weapon info
	 */
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_CURSED_WEAPON_LOCATION.writeId(this);
		if (!_cursedWeaponInfo.isEmpty())
		{
			writeInt(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeInt(w.id);
				writeInt(w.activated);
				writeInt(w.loc.getX());
				writeInt(w.loc.getY());
				writeInt(w.loc.getZ());
			}
		}
		else
		{
			writeInt(0);
			writeInt(0);
		}
	}
	
	/**
	 * The Class CursedWeaponInfo.
	 */
	public static class CursedWeaponInfo
	{
		/** The location. */
		public Location loc;
		/** The id. */
		public int id;
		/** The activated. */
		public int activated; // 0 - not activated ? 1 - activated
		
		/**
		 * Instantiates a new cursed weapon info.
		 * @param location the Location
		 * @param cwId the Id
		 * @param status the status
		 */
		public CursedWeaponInfo(Location location, int cwId, int status)
		{
			loc = location;
			id = cwId;
			activated = status;
		}
	}
}
