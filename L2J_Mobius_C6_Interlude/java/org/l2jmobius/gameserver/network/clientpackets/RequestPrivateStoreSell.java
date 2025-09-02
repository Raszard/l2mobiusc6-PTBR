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
import org.l2jmobius.gameserver.model.ItemRequest;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class RequestPrivateStoreSell implements ClientPacket
{
	private int _storePlayerId;
	private int _count;
	private int _price;
	private ItemRequest[] _items;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_storePlayerId = packet.readInt();
		_count = packet.readInt();
		// count*20 is the size of a for iteration of each item
		if ((_count < 0) || ((_count * 20) > packet.getRemainingLength()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];
		long priceTotal = 0;
		for (int i = 0; i < _count; i++)
		{
			final int objectId = packet.readInt();
			final int itemId = packet.readInt();
			final int enchant = packet.readShort();
			packet.readShort(); // TODO analyze this
			final long count = packet.readInt();
			final int price = packet.readInt();
			if ((count > Integer.MAX_VALUE) || (count < 0))
			{
				_count = -1;
				return;
			}
			
			_items[i] = new ItemRequest(objectId, itemId, enchant, (int) count, price);
			priceTotal += price * count;
		}
		
		if ((priceTotal < 0) || (priceTotal > Integer.MAX_VALUE))
		{
			_count = -1;
			return;
		}
		
		_price = (int) priceTotal;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_count == -1)
		{
			final String msgErr = "[RequestPrivateStoreSell] player " + player.getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
			_count = -1;
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("Você está vendendo itens muito rápido");
			return;
		}
		
		final WorldObject object = World.getInstance().findObject(_storePlayerId);
		if (!(object instanceof Player))
		{
			return;
		}
		
		final Player storePlayer = (Player) object;
		if (storePlayer.getPrivateStoreType() != Player.STORE_PRIVATE_BUY)
		{
			return;
		}
		
		final TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			return;
		}
		
		// Check if player didn't choose any items
		if ((_items == null) || (_items.length == 0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Privilégios insuficientes.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (storePlayer.getItemCount(Config.SELL_ITEM, -1) < _price)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.sendPacket(SystemMessage.sendString("Você não tem itens suficientes para comprar, cancelando PrivateBuy"));
				storePlayer.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				storePlayer.broadcastUserInfo();
				return;
			}
		}
		else if (storePlayer.getAdena() < _price)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.sendMessage("Você não tem adena suficiente, cancele o PrivateBuy.");
			storePlayer.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!storeList.PrivateStoreSell(player, _items, _price))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			Util.handleIllegalPlayerAction(player, player + " provided invalid list or request! ", Config.DEFAULT_PUNISH);
			PacketLogger.warning("PrivateStore sell has failed due to invalid list or request. " + player + ", Private store of " + storePlayer);
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}
}