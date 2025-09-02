/*
 * Decompiled with CFR 0.150.
 */
package org.l2jmobius.gameserver.auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.l2jmobius.commons.database.DatabaseFactory;

public class AuctionItem
{
	private final int auctionId;
	private final int ownerId;
	private final int itemId;
	private final int count;
	private final int enchant;
	private final int costId;
	private final int costCount;
	
	public AuctionItem(int auctionId, int ownerId, int itemId, int count, int enchant, int costId, int costCount)
	{
		this.auctionId = auctionId;
		this.ownerId = ownerId;
		this.itemId = itemId;
		this.count = count;
		this.enchant = enchant;
		this.costId = costId;
		this.costCount = costCount;
	}
	
	public String getName()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT char_name FROM characters WHERE charId = ?");)
		{
			ps.setInt(1, ownerId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					String playerName = rs.getString("char_name");
					return playerName;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public int getAuctionId()
	{
		return this.auctionId;
	}
	
	public int getOwnerId()
	{
		return this.ownerId;
	}
	
	public int getItemId()
	{
		return this.itemId;
	}
	
	public int getCount()
	{
		return this.count;
	}
	
	public int getEnchant()
	{
		return this.enchant;
	}
	
	public int getCostId()
	{
		return this.costId;
	}
	
	public int getCostCount()
	{
		return this.costCount;
	}
}
