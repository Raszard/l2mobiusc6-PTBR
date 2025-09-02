package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Donator implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		9507,
		9511
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		Player activeChar = (Player) playable;
		if (activeChar.isDonator())
		{
			activeChar.sendMessage("Sua conta já é doadora!");
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
		}
		else
		{
			if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{
				return;
			}
			int days = item.getItemId() == 9511 ? 15 : 30;
			activeChar.setDonator(true);
			activeChar.updateNameTitleColor();
			final long donatorTime = Long.valueOf(days) * 24 * 60 * 60 * 1000;
			activeChar.getAccountVariables().set("CustomDonatorEnd", System.currentTimeMillis() + donatorTime);
			activeChar.sendMessage("Você ganhou o status de doador por " + days + " dias!");
			activeChar.sendMessage("Muito obrigado pelo apoio!");
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
