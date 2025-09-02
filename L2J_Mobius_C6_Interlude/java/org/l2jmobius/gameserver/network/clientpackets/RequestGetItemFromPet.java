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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.ReadablePacket;
import org.l2jmobius.gameserver.enums.IllegalActionPunishmentType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.util.Util;

public class RequestGetItemFromPet implements ClientPacket
{
	private int _objectId;
	private int _amount;
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_objectId = packet.readInt();
		_amount = packet.readInt();
		_unknown = packet.readInt(); // = 0 for most trades
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if ((player == null) || (player.getPet() == null) || !(player.getPet() instanceof Pet))
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("Você recebe itens de pet muito rápido.");
			return;
		}
		
		final Pet pet = (Pet) player.getPet();
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, player + " tried to use enchant exploit and got banned!", IllegalActionPunishmentType.KICKBAN);
			return;
		}
		
		if (_amount < 0)
		{
			player.setAccessLevel(-1);
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (_amount == 0)
		{
			return;
		}
		
		if (player.calculateDistanceSq3D(pet) > 40000) // 200*200
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			PacketLogger.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
		player.sendPacket(new ItemList(player, true));
	}
}
