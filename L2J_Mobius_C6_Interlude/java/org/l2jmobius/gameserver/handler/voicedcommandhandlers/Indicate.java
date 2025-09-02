package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;

public class Indicate implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"indiquei"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String targetName)
	{
		if ((targetName == null) || targetName.isEmpty())
		{
			player.sendMessage("Uso correto: .indiquei NomeDoPlayer");
			return false;
		}
		
		if (wasIndicated(player.getAccountName(), player.getClient().getIp()))
		{
			player.sendMessage("Essa conta/IP já foi indicada por alguém.");
			return false;
		}
		
		if (getIpIndicator(getAccountNameIndicator(targetName)).equals(player.getClient().getIp()))
		{
			player.sendMessage("Você não pode indicar alguém com o mesmo IP.");
			return false;
		}
		
		if (player.getLevel() > 10)
		{
			player.sendMessage("Apenas players com level menor que 10 podem ser indicados.");
			return false;
		}
		
		if (getPlayerIdByName(targetName) == null)
		{
			player.sendMessage("Personagem com esse nome não existe.");
			return false;
		}
		
		insertIndication(player.getAccountName(), getPlayerIdByName(targetName), player.getObjectId(), player.getClient().getIp());
		
		player.sendMessage("Indicação registrada com sucesso! Agradeça a " + targetName + ".");
		
		return true;
	}
	
	public boolean wasIndicated(String accountName, String accountIp)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT 1 FROM indicated_players WHERE account_name = ? OR account_ip = ? LIMIT 1"))
		{
			
			ps.setString(1, accountName);
			ps.setString(2, accountIp);
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next();
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void insertIndication(String accountName, int indicatedId, int playerId, String accountIp)
	{
		String sql = "INSERT INTO indicated_players (account_Name, indicated_id, player_id, account_ip) VALUES (?, ?, ?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			
			ps.setString(1, accountName);
			ps.setInt(2, indicatedId);
			ps.setInt(3, playerId);
			ps.setString(4, accountIp);
			
			ps.executeUpdate();
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public Integer getPlayerIdByName(String charName)
	{
		String sql = "SELECT charId FROM characters WHERE char_name = ? LIMIT 1";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			
			ps.setString(1, charName);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getInt("charId");
				}
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getIpIndicator(String accountName)
	{
		String sql = "SELECT lastIp FROM accounts WHERE login = ? LIMIT 1";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			
			ps.setString(1, accountName);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getString("lastIp");
				}
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getAccountNameIndicator(String charName)
	{
		String sql = "SELECT account_name FROM characters WHERE char_name = ? LIMIT 1";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			
			ps.setString(1, charName);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getString("account_name");
				}
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return null; // Se não encontrar
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}