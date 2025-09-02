package org.l2jmobius.gameserver.ayengine.autofarm;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.taskmanager.AutoUseTaskManager;

public class FarmCommandHandler
{
	private static FarmCommandHandler instance;
	private final Map<String, BiConsumer<Player, String[]>> commands = new HashMap<>();
	private static final Integer AUTO_ATTACK_ACTION = 2;
	
	public static FarmCommandHandler getInstance()
	{
		if (instance == null)
		{
			instance = new FarmCommandHandler();
			instance.initializeCommands();
		}
		return instance;
	}
	
	private void initializeCommands()
	{
		commands.put("on", (player, params) ->
		{
			AutoFarmTask.getInstance().startAutoPlay(player);
			AutoUseTaskManager.getInstance().startAutoUseTask(player);
			player.sendMessage("Auto farm ativado!");
		});
		
		commands.put("off", (player, params) ->
		{
			AutoFarmTask.getInstance().stopAutoPlay(player);
			AutoUseTaskManager.getInstance().stopAutoUseTask(player);
			player.sendMessage("Auto farm desativado!");
		});
		
		commands.put("skill", (player, params) ->
		{
			int skillId = Integer.parseInt(params[0]);
			
			Integer skillToAdd = skillId;
			
			if (skillToAdd == AUTO_ATTACK_ACTION)
			{
				player.getAutoUseSettings().getAutoActions().add(AUTO_ATTACK_ACTION);
			}
			player.getAutoUseSettings().getAutoSkills().add(skillToAdd);
		});
		
		commands.put("rskill", (player, params) ->
		{
			int skillId = Integer.parseInt(params[0]);
			
			Integer skillToRemove = skillId;
			
			if (player.getAutoUseSettings().getAutoSkills().contains(skillToRemove))
			{
				player.getAutoUseSettings().getAutoSkills().remove(skillToRemove);
			}
			if (skillToRemove == AUTO_ATTACK_ACTION)
			{
				player.getAutoUseSettings().getAutoActions().remove(AUTO_ATTACK_ACTION);
			}
		});
		commands.put("inc_radius", (player, params) ->
		{
			player.getAutoPlaySettings().setShortRange(false);
		});
		commands.put("dec_radius", (player, params) ->
		{
			player.getAutoPlaySettings().setShortRange(true);
		});
		commands.put("inc_viewer", (player, params) ->
		{
			player.getAutoPlaySettings().setLimitZone(true);
		});
		commands.put("dec_viewer", (player, params) ->
		{
			player.getAutoPlaySettings().setLimitZone(false);
		});
		commands.put("deassist", (player, params) ->
		{
			player.getAutoPlaySettings().setNextTargetMode(1);
		});
		commands.put("assist", (player, params) ->
		{
			player.getAutoPlaySettings().setNextTargetMode(2);
		});
		commands.put("respect", (player, params) ->
		{
			player.getAutoPlaySettings().setRespectfulHunting(true);
		});
		commands.put("desrespect", (player, params) ->
		{
			player.getAutoPlaySettings().setRespectfulHunting(false);
		});
	}
	
	public void handleCommand(String fullCommand, Player player)
	{
		StringTokenizer tokenizer = new StringTokenizer(fullCommand);
		
		if (tokenizer.hasMoreTokens())
		{
			tokenizer.nextToken();
		}
		
		String command = tokenizer.nextToken();
		
		String[] params = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++)
		{
			params[i] = tokenizer.nextToken();
		}
		
		BiConsumer<Player, String[]> action = commands.get(command.toLowerCase());
		
		if (action != null)
		{
			action.accept(player, params);
		}
	}
	
	public void addCommand(String commandName, BiConsumer<Player, String[]> action)
	{
		commands.put(commandName.toLowerCase(), action);
	}
}