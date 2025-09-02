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

import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * format(packet 0xFE) ch cd [ddddcdcd] c - id h - sub id c d - size [ d - level d - seed price d - seed level d - crop price c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowManorDefaultInfo extends ServerPacket
{
	private List<Integer> _crops = null;
	
	public ExShowManorDefaultInfo()
	{
		_crops = ManorSeedData.getInstance().getAllCrops();
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SHOW_MANOR_DEFAULT_INFO.writeId(this);
		writeByte(0);
		writeInt(_crops.size());
		for (int cropId : _crops)
		{
			writeInt(cropId); // crop Id
			writeInt(ManorSeedData.getInstance().getSeedLevelByCrop(cropId)); // level
			writeInt(ManorSeedData.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeInt(ManorSeedData.getInstance().getCropBasicPrice(cropId)); // crop price
			writeByte(1); // reward 1 Type
			writeInt(ManorSeedData.getInstance().getRewardItem(cropId, 1)); // Reward 1 Type Item Id
			writeByte(1); // reward 2 Type
			writeInt(ManorSeedData.getInstance().getRewardItem(cropId, 2)); // Reward 2 Type Item Id
		}
	}
}
