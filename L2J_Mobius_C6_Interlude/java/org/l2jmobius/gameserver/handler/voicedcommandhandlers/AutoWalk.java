package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import java.util.List;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.geoengine.pathfinding.AbstractNodeLoc;
import org.l2jmobius.gameserver.geoengine.pathfinding.PathFinding;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;

public class AutoWalk implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"goto"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equalsIgnoreCase("goto"))
		{
			if ((target == null) || target.isEmpty())
			{
				activeChar.sendMessage("Uso: .goto x y z");
				return false;
			}
			
			try
			{
				String[] coords = target.split(" ");
				int destX = Integer.parseInt(coords[0]);
				int destY = Integer.parseInt(coords[1]);
				int destZ = Integer.parseInt(coords[2]);
				
				activeChar.sendMessage("Indo para: " + destX + ", " + destY + ", " + destZ);
				new Thread(() -> walkInSteps(activeChar, destX, destY, destZ)).start();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Coordenadas inválidas!");
				return false;
			}
		}
		return true;
	}
	
	private void walkInSteps(Player player, int destX, int destY, int destZ)
	{
		int stepDistance = 2000; // Distância máxima entre checkpoints
		
		int curX = player.getX();
		int curY = player.getY();
		int curZ = player.getZ();
		
		while (distance3D(curX, curY, curZ, destX, destY, destZ) > stepDistance)
		{
			double angle = Math.atan2(destY - curY, destX - curX);
			curX += (int) (Math.cos(angle) * stepDistance);
			curY += (int) (Math.sin(angle) * stepDistance);
			
			if (!moveTo(player, curX, curY, destZ))
			{
				player.sendMessage("Caminho bloqueado!");
				return;
			}
			
			waitUntilArrives(player);
		}
		
		moveTo(player, destX, destY, destZ);
	}
	
	private boolean moveTo(Player player, int x, int y, int z)
	{
		List<AbstractNodeLoc> path = PathFinding.getInstance().findPath(player.getX(), player.getY(), player.getZ(), x, y, z, player.getInstanceId(), true);
		
		if ((path != null) && !path.isEmpty())
		{
			for (AbstractNodeLoc node : path)
			{
				Location loc = new Location(node.getX(), node.getY(), node.getZ());
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
				}
			}
			return true;
		}
		return false;
	}
	
	private void waitUntilArrives(Player player)
	{
		while (player.isMoving())
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	private double distance3D(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
