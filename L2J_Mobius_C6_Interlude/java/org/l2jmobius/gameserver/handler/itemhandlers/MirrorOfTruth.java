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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.ayengine.AyPlayerInfo;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;

public class MirrorOfTruth implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		9510
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		final Player player = (Player) playable;
		if (player.getTarget() != null)
		{
			if (player.getTarget().isPlayer())
			{
				Player otherPlayer = (Player) player.getTarget();
				AyPlayerInfo.getInstance().gatherCharacterInfo(player, otherPlayer);
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
