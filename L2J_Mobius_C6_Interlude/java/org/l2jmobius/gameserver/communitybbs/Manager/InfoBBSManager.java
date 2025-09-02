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
package org.l2jmobius.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.ayengine.AyUtils;
import org.l2jmobius.gameserver.donation.DonationManager;
import org.l2jmobius.gameserver.model.actor.Player;

public class InfoBBSManager extends BaseBBSManager
{
	protected InfoBBSManager()
	{
	}
	
	public static InfoBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.startsWith("_bbsinfo;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			String infoCommand = st.nextToken();
			
			if (infoCommand.startsWith("npcs"))
			{
				int npcId = Integer.parseInt(st.nextToken());
				NpcLocation(npcId, player);
			}
			else if (infoCommand.startsWith("donate"))
			{
				DonationManager.getInstance().showIndexWindow(player);
			}
		}
		else
		{
			super.parseCmd(command, player);
		}
	}
	
	private void NpcLocation(int st, Player player)
	{
		AyUtils.getInstance().MarkMobRadar(player, st);
	}
	
	@Override
	protected String getFolder()
	{
		return "top/";
	}
	
	private static class SingletonHolder
	{
		protected static final InfoBBSManager INSTANCE = new InfoBBSManager();
	}
}