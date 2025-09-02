package org.l2jmobius.gameserver.ayengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.l2jmobius.commons.database.DatabaseFactory;

public class AySql
{
	private static final String STORE_AYSQL = "INSERT INTO ay_fix (npc_id, type, reason, name) VALUES (?,?,?,?)";
	private static final String STORE_AYSQL_ITEM = "INSERT INTO ay_fix_item (item_id, type, reason, name) VALUES (?,?,?,?)";
	private static final String CHECK_EXISTING = "SELECT COUNT(*) FROM ay_fix WHERE npc_id = ?";
	private static final String CHECK_EXISTING_ITEM = "SELECT COUNT(*) FROM ay_fix_item WHERE item_id = ?";
	
	public boolean storeFix(int npcId, String type, String reason, String name)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement checkPs = con.prepareStatement(CHECK_EXISTING);
			PreparedStatement ps = con.prepareStatement(STORE_AYSQL);)
		{
			checkPs.setInt(1, npcId);
			try (ResultSet rs = checkPs.executeQuery())
			{
				if (rs.next() && (rs.getInt(1) > 0))
				{
					return false;
				}
			}
			ps.setInt(1, npcId);
			ps.setString(2, type);
			ps.setString(3, reason);
			ps.setString(4, name);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean storeFixItem(int itemId, String type, String reason, String name)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement checkPs = con.prepareStatement(CHECK_EXISTING_ITEM);
			PreparedStatement ps = con.prepareStatement(STORE_AYSQL_ITEM);)
		{
			checkPs.setInt(1, itemId);
			try (ResultSet rs = checkPs.executeQuery())
			{
				if (rs.next() && (rs.getInt(1) > 0))
				{
					return false;
				}
			}
			ps.setInt(1, itemId);
			ps.setString(2, type);
			ps.setString(3, reason);
			ps.setString(4, name);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public static AySql getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AySql INSTANCE = new AySql();
		
		private SingletonHolder()
		{
			
		}
	}
}
