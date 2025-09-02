/*
 * Decompiled with CFR 0.150.
 */
package org.l2jmobius.gameserver.skin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

public class SkinTable
{
	// private static final Logger LOGGER = Logger.getLogger(SkinTable.class.getName());
	private static final String RESTORE_SKIN_ITEM = "SELECT * FROM skins WHERE playerId = ?";
	private static final String ADD_SKIN_ITEM = "INSERT INTO skins (playerId, face, head, gloves, chest, legs, feet, back, hair) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_SKIN_ITEM = "DELETE FROM skins WHERE playerId=?";
	
	public void restore(Inventory _inventory, int _playerId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_SKIN_ITEM);
			statement.setInt(1, _playerId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				Integer faceInt = rs.getInt("face");
				Integer headInt = rs.getInt("head");
				Integer glovesInt = rs.getInt("gloves");
				Integer chestInt = rs.getInt("chest");
				Integer legsInt = rs.getInt("legs");
				Integer feetInt = rs.getInt("feet");
				Integer backInt = rs.getInt("back");
				Integer hairInt = rs.getInt("hair");
				if (hairInt != 0)
				{
					
					Item hair = Item.restoreFromDb(hairInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_HAIR, hair);
				}
				
				if (faceInt != 0)
				{
					Item face = Item.restoreFromDb(faceInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_FACE, face);
				}
				if (headInt != 0)
				{
					Item head = Item.restoreFromDb(headInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_HEAD, head);
				}
				if (glovesInt != 0)
				{
					Item gloves = Item.restoreFromDb(glovesInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_GLOVES, gloves);
				}
				if (chestInt != 0)
				{
					Item chest = Item.restoreFromDb(chestInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_CHEST, chest);
				}
				if (legsInt != 0)
				{
					Item legs = Item.restoreFromDb(legsInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_LEGS, legs);
				}
				if (feetInt != 0)
				{
					Item feet = Item.restoreFromDb(feetInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_FEET, feet);
				}
				if (backInt != 0)
				{
					Item back = Item.restoreFromDb(backInt);
					_inventory.setSkinsPaperdoll(Inventory.PAPERDOLL_BACK, back);
				}
			}
			if (SkinUtils.allNull(_inventory.getSkinsPaperdoll()))
			{
				deleteStore(_playerId);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void store(Inventory _inventory, int _playerId)
	{
		Item[] skins = _inventory.getSkinsPaperdoll();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_SKIN_ITEM);)
		{
			ps.setInt(1, _playerId);
			if (skins[Inventory.PAPERDOLL_FACE] != null)
			{
				ps.setInt(2, skins[Inventory.PAPERDOLL_FACE].getObjectId());
			}
			else
			{
				ps.setNull(2, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_HEAD] != null)
			{
				ps.setInt(3, skins[Inventory.PAPERDOLL_HEAD].getObjectId());
			}
			else
			{
				ps.setNull(3, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_GLOVES] != null)
			{
				ps.setInt(4, skins[Inventory.PAPERDOLL_GLOVES].getObjectId());
			}
			else
			{
				ps.setNull(4, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_CHEST] != null)
			{
				ps.setInt(5, skins[Inventory.PAPERDOLL_CHEST].getObjectId());
			}
			else
			{
				ps.setNull(5, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_LEGS] != null)
			{
				ps.setInt(6, skins[Inventory.PAPERDOLL_LEGS].getObjectId());
			}
			else
			{
				ps.setNull(6, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_FEET] != null)
			{
				ps.setInt(7, skins[Inventory.PAPERDOLL_FEET].getObjectId());
			}
			else
			{
				ps.setNull(7, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_BACK] != null)
			{
				ps.setInt(8, skins[Inventory.PAPERDOLL_BACK].getObjectId());
			}
			else
			{
				ps.setNull(8, Types.INTEGER);
			}
			if (skins[Inventory.PAPERDOLL_HAIR] != null)
			{
				ps.setInt(9, skins[Inventory.PAPERDOLL_HAIR].getObjectId());
			}
			else
			{
				ps.setNull(9, Types.INTEGER);
			}
			ps.execute();
			ps.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteStore(int playerId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_SKIN_ITEM);)
		{
			ps.setInt(1, playerId);
			ps.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static SkinTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkinTable INSTANCE = new SkinTable();
		
		private SingletonHolder()
		{
		}
	}
}
