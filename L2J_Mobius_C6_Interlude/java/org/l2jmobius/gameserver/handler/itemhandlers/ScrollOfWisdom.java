package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

public class ScrollOfWisdom implements IItemHandler
{
	
	private static final int[] ITEM_IDS =
	{
		9519
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		Player player = (Player) playable;
		
		// Aplica o buff (skill ID 1000, level 1)
		MagicSkillUse msu = new MagicSkillUse(player, player, 9010, 1, 1, 0);
		player.broadcastPacket(msu);
		player.sendSkillList();
		player.useMagic(SkillTable.getInstance().getSkill(9010, 1), false, false);
		
		// Remove o pergaminho após o uso
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		// Mensagem de confirmação
		player.sendMessage("Você ganhou +50% de XP/SP por 1 hora!");
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}