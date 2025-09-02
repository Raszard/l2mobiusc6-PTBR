/*
 * Decompiled with CFR 0.150.
 */
package org.l2jmobius.gameserver.auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;

public class AuctionTable
{
	private static final Logger LOGGER = Logger.getLogger(AuctionTable.class.getName());
	private static final String RESTORE_AUCTION_ITEM = "SELECT * FROM auction_table";
	private static final String ADD_AUCTION_ITEM = "INSERT INTO auction_table (ownerid, itemid, count, enchant, costid, costcount) VALUES (?,?,?,?,?,?)";
	private static final String DELETE_AUCTION_ITEM = "DELETE FROM auction_table WHERE auctionid=?";
	private final Set<AuctionItem> _items = ConcurrentHashMap.newKeySet();
	
	protected AuctionTable()
	{
		updateTable();
		LOGGER.info("Loaded " + this._items.size() + " Auction items.");
	}
	
	public void addItem(AuctionItem item)
	{
		this._items.add(item);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_AUCTION_ITEM);)
		{
			ps.setInt(1, item.getOwnerId());
			ps.setInt(2, item.getItemId());
			ps.setInt(3, item.getCount());
			ps.setInt(4, item.getEnchant());
			ps.setInt(5, item.getCostId());
			ps.setInt(6, item.getCostCount());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteItem(AuctionItem item)
	{
		this._items.remove(item);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_AUCTION_ITEM);)
		{
			ps.setInt(1, item.getAuctionId());
			ps.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public AuctionItem getItem(int auctionId)
	{
		return this._items.stream().filter(x -> x.getAuctionId() == auctionId).findFirst().orElse(null);
	}
	
	public Set<AuctionItem> getItems()
	{
		return _items;
	}
	
	public void updateTable()
	{
		this._items.clear();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_AUCTION_ITEM);
			ResultSet rs = ps.executeQuery();)
		{
			while (rs.next())
			{
				this._items.add(new AuctionItem(rs.getInt("auctionid"), rs.getInt("ownerid"), rs.getInt("itemid"), rs.getInt("count"), rs.getInt("enchant"), rs.getInt("costid"), rs.getInt("costcount")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static AuctionTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionTable INSTANCE = new AuctionTable();
		
		private SingletonHolder()
		{
		}
	}
}
