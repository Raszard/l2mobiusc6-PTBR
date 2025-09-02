/*
 * Decompiled with CFR 0.150.
 */
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.communitybbs.Manager.BaseBBSManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

public class AuctionManager extends Folk
{
	
	public AuctionManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/auction/auction.htm");
		BaseBBSManager.separateAndSend(content, player);
	}
}
