package org.l2jmobius.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author Mobius
 * @refactor [Ay] - Refatoração completa para melhor performance e legibilidade
 */
public class AutoUseTaskManager
{
	// Configurações
	private static final int POOL_SIZE = 300;
	private static final int TASK_DELAY = 300;
	
	// Estruturas de dados otimizadas
	private final List<Set<Player>> playerPools = new CopyOnWriteArrayList<>();
	private final Set<Player> allPlayers = ConcurrentHashMap.newKeySet();
	
	private final Map<Player, Monster> sweepTargets = new ConcurrentHashMap<>();
	private final Map<Player, Player> healTargets = new ConcurrentHashMap<>();
	
	private final ScheduledExecutorService scheduler;
	
	// Instância singleton
	private static final AutoUseTaskManager INSTANCE = new AutoUseTaskManager();
	
	private AutoUseTaskManager()
	{
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		this.scheduler = Executors.newScheduledThreadPool(availableProcessors);
		initializePools();
	}
	
	private void initializePools()
	{
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
		{
			Set<Player> pool = ConcurrentHashMap.newKeySet(POOL_SIZE);
			playerPools.add(pool);
			scheduler.scheduleAtFixedRate(new AutoUseTask(pool), TASK_DELAY, TASK_DELAY, TimeUnit.MILLISECONDS);
		}
	}
	
	public static AutoUseTaskManager getInstance()
	{
		return INSTANCE;
	}
	
	// Classe principal de tarefa de auto uso
	private class AutoUseTask implements Runnable
	{
		private final Set<Player> playersPool;
		
		public AutoUseTask(Set<Player> playersPool)
		{
			this.playersPool = playersPool;
		}
		
		@Override
		public void run()
		{
			if (playersPool.isEmpty())
			{
				return;
			}
			
			// Processa cada jogador no pool
			playersPool.forEach(this::processPlayer);
			
			// Remove jogadores offline
			playersPool.removeIf(player -> !player.isOnline() && stopAutoUseTask(player));
		}
		
		private void processPlayer(Player player)
		{
			if (!shouldProcessPlayer(player))
			{
				return;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE);
			
			// Processa cura primeiro (alta prioridade)
			if (findHealSkill(player).isPresent())
			{
				processHeal(player, isInPeaceZone);
			}
			
			// NOVO: Processa spoil (alta prioridade também)
			if (findSweepSkill(player).isPresent())
			{
				processSweep(player, isInPeaceZone);
			}
			
			if (findSpoilSkill(player).isPresent())
			{
				processSpoil(player, isInPeaceZone);
			}
			
			processAutoSkills(player, isInPeaceZone);
		}
		
		private boolean shouldProcessPlayer(Player player)
		{
			return player.isOnline() && !player.isSitting() && !player.isStunned() && !player.isSleeping() && !player.isParalyzed() && !player.isAfraid() && !player.isAlikeDead() && !player.isMounted();
		}
	}
	
	// Método para definir alvo de cura
	public void setHealTarget(Player healer, Player target)
	{
		healTargets.put(healer, target);
	}
	
	// Método para remover alvo de cura
	public void removeHealTarget(Player healer)
	{
		healTargets.remove(healer);
	}
	
	// NOVO: Método para processar cura
	private boolean processHeal(Player player, boolean isInPeaceZone)
	{
		// Verifica se tem alvo de cura definido
		Player healTarget = healTargets.get(player);
		if ((healTarget == null) || healTarget.isDead())
		{
			healTargets.remove(player);
			return false;
		}
		
		// Verifica se ainda precisa de cura
		if (healTarget.getCurrentHpPercent() >= 60)
		{
			healTargets.remove(player);
			return false;
		}
		
		// Não cura em zona de paz
		if (isInPeaceZone)
		{
			return false;
		}
		
		// Busca skill de cura
		Optional<Integer> healSkill = findHealSkill(player);
		if (!healSkill.isPresent())
		{
			healTargets.remove(player);
			return false;
		}
		
		// Tenta usar a skill de cura
		if (tryUseHealSkill(player, healTarget, healSkill.get()))
		{
			return true;
		}
		player.getAutoUseSettings().incrementSkillOrder();
		return false;
		
	}
	
	// NOVO: Encontrar skill de cura
	private Optional<Integer> findHealSkill(Player player)
	{
		// Busca nas skills automáticas do jogador
		List<Integer> autoSkills = player.getAutoUseSettings().getAutoSkills();
		for (Integer skillId : autoSkills)
		{
			Skill skill = player.getKnownSkill(skillId);
			if ((skill != null) && isHealSkill(skill))
			{
				return Optional.of(skillId);
			}
		}
		
		return Optional.empty();
	}
	
	// NOVO: Verificar se é skill de cura
	private boolean isHealSkill(Skill skill)
	{
		return skill.getSkillType() == SkillType.HEAL;
	}
	
	// NOVO: Tentar usar skill de cura
	private boolean tryUseHealSkill(Player player, Player target, int healSkillId)
	{
		Skill skill = player.getKnownSkill(healSkillId);
		if (skill == null)
		{
			return false;
		}
		
		// Verifica se pode usar a skill
		if (!canUseMagic(player, target, skill))
		{
			return false;
		}
		
		// Usa a skill de cura
		WorldObject savedTarget = player.getTarget();
		player.setTarget(target);
		
		if (player.useMagic(skill, true, false))
		{
			// Sucesso ao curar - remove o alvo de cura se estiver com HP suficiente
			if (target.getCurrentHpPercent() >= 60)
			{
				healTargets.remove(player);
			}
			return true;
		}
		
		// Restaura o target original se falhar
		player.setTarget(savedTarget);
		return false;
	}
	
	// Método para definir alvo de spoil
	public void setSweepTarget(Player player, Monster target)
	{
		sweepTargets.put(player, target);
	}
	
	// Método para remover alvo de spoil
	public void removeSweepTarget(Player player)
	{
		sweepTargets.remove(player);
	}
	
	// NOVO: Método para processar spoil
	private boolean processSweep(Player player, boolean isInPeaceZone)
	{
		// Verifica se tem alvo de spoil definido
		Monster sweepTarget = sweepTargets.get(player);
		if ((sweepTarget == null) || !sweepTarget.isAlikeDead())
		{
			sweepTargets.remove(player);
			return false;
		}
		
		// Não usa spoil em zona de paz
		if (isInPeaceZone)
		{
			sweepTargets.remove(player);
			return false;
		}
		
		// Busca skill de spoil
		Optional<Integer> spoilSkill = findSweepSkill(player);
		if (!spoilSkill.isPresent())
		{
			sweepTargets.remove(player);
			return false;
		}
		
		// Tenta usar a skill de spoil
		if (tryUseSweepSkill(player, sweepTarget, spoilSkill.get()))
		{
			return true;
		}
		
		player.getAutoUseSettings().incrementSkillOrder();
		return false;
	}
	
	// NOVO: Método para encontrar skill de spoil
	private Optional<Integer> findSweepSkill(Player player)
	{
		// Busca nas skills automáticas do jogador
		List<Integer> autoSkills = player.getAutoUseSettings().getAutoSkills();
		for (Integer skillId : autoSkills)
		{
			Skill skill = player.getKnownSkill(skillId);
			if ((skill != null) && isSweepSkill(skill))
			{
				return Optional.of(skillId);
			}
		}
		
		return Optional.empty();
	}
	
	// NOVO: Método para verificar se é skill de spoil
	private boolean isSweepSkill(Skill skill)
	{
		return skill.getSkillType() == SkillType.SWEEP;
	}
	
	// NOVO: Método para tentar usar skill de spoil
	private boolean tryUseSweepSkill(Player player, Monster target, int spoilSkillId)
	{
		Skill skill = player.getKnownSkill(spoilSkillId);
		if (skill == null)
		{
			sweepTargets.remove(player);
			return false;
		}
		
		// Verifica se pode usar a skill
		if (!canUseMagic(player, target, skill))
		{
			sweepTargets.remove(player);
			return false;
		}
		
		// Verifica range da skill
		if (Util.calculateDistance(player, target, true) > skill.getCastRange())
		{
			// Se estiver fora do range, move-se para perto do alvo
			if (!player.isMoving())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
			}
			return true;
		}
		
		// Usa a skill de spoil
		WorldObject savedTarget = player.getTarget();
		player.setTarget(target);
		
		boolean success = player.useMagic(skill, true, false);
		
		// Restaura o target original
		player.setTarget(savedTarget);
		
		// Remove o alvo de spoil se conseguiu usar ou se não for mais válido
		if (success || (target == null))
		{
			sweepTargets.remove(player);
		}
		
		return success;
	}
	
	// NOVO: Método para processar spoil (durante o combate)
	private boolean processSpoil(Player player, boolean isInPeaceZone)
	{
		WorldObject target = player.getTarget();
		if (!(target instanceof Monster))
		{
			return false;
		}
		
		Monster spoilTarget = (Monster) target;
		
		// Não em zona de paz
		if (isInPeaceZone)
		{
			return false;
		}
		
		// Se já está spoilado, não precisa mais
		if (spoilTarget.isSpoil())
		{
			return false;
		}
		
		// Busca skill de spoil
		Optional<Integer> spoilSkill = findSpoilSkill(player);
		if (!spoilSkill.isPresent())
		{
			return false;
		}
		
		// Tenta usar spoil
		return tryUseSpoilSkill(player, spoilTarget, spoilSkill.get());
	}
	
	// NOVO: Encontrar skill de spoil
	private Optional<Integer> findSpoilSkill(Player player)
	{
		List<Integer> autoSkills = player.getAutoUseSettings().getAutoSkills();
		for (Integer skillId : autoSkills)
		{
			Skill skill = player.getKnownSkill(skillId);
			if ((skill != null) && isSpoilSkill(skill))
			{
				return Optional.of(skillId);
			}
		}
		return Optional.empty();
	}
	
	// NOVO: Tentar usar spoil
	private boolean tryUseSpoilSkill(Player player, Monster target, int spoilSkillId)
	{
		Skill skill = player.getKnownSkill(spoilSkillId);
		if (skill == null)
		{
			return false;
		}
		
		if (!canUseMagic(player, target, skill))
		{
			return false;
		}
		
		if (Util.calculateDistance(player, target, true) > skill.getCastRange())
		{
			if (!player.isMoving())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
			}
			return true;
		}
		
		WorldObject savedTarget = player.getTarget();
		player.setTarget(target);
		boolean success = player.useMagic(skill, true, false);
		player.setTarget(savedTarget);
		
		return success;
	}
	
	private boolean isSpoilSkill(Skill skill)
	{
		return skill.getSkillType() == SkillType.SPOIL;
	}
	
	private Skill findSkill(Player player, Integer skillId)
	{
		Skill skill = player.getKnownSkill(skillId);
		if (skill != null)
		{
			return skill;
		}
		
		Summon summon = player.getPet();
		if (summon != null)
		{
			return summon.getKnownSkill(skillId);
		}
		
		return null;
	}
	
	private Playable determineCaster(Player player, Integer skillId)
	{
		Skill playerSkill = player.getKnownSkill(skillId);
		if (playerSkill != null)
		{
			return player;
		}
		
		Summon summon = player.getPet();
		if (summon != null)
		{
			Skill summonSkill = summon.getKnownSkill(skillId);
			if (summonSkill != null)
			{
				return summon;
			}
		}
		
		return null;
	}
	
	// Processamento de skills automáticas
	private void processAutoSkills(Player player, boolean isInPeaceZone)
	{
		if (!Config.ENABLE_AUTO_SKILL || !player.isAutoPlaying())
		{
			return;
		}
		if (player.isCastingNow() || player.isTeleporting())
		{
			return;
		}
		
		Integer skillId = player.getAutoUseSettings().getNextSkillId();
		if ((skillId == null))
		{
			return;
		}
		
		processAutoSkill(player, skillId);
	}
	
	private void processAutoSkill(Player player, Integer skillId)
	{
		Skill skill = findSkill(player, skillId);
		if (skill == null)
		{
			player.getAutoUseSettings().getAutoSkills().remove(skillId);
			player.getAutoUseSettings().resetSkillOrder();
			return;
		}
		
		if (isHealSkill(skill) || isSweepSkill(skill) || isSpoilSkill(skill))
		{
			player.getAutoUseSettings().incrementSkillOrder();
			return;
		}
		
		WorldObject target = player.getTarget();
		
		if (!isValidSkillTarget(player, target, skill))
		{
			return;
		}
		
		Playable caster = determineCaster(player, skillId);
		if (caster == null)
		{
			return;
		}
		
		if (canUseMagic(player, target, skill) && caster.useMagic(skill, true, false))
		{
			player.getAutoUseSettings().incrementSkillOrder();
		}
	}
	
	private boolean isValidSkillTarget(Player player, WorldObject target, Skill skill)
	{
		// Auto-targeting self is not allowed for combat skills
		if (target == player)
		{
			return false;
		}
		
		// Target must exist and be alive
		if (!(target instanceof Creature) || ((Creature) target).isDead())
		{
			return false;
		}
		
		// Peace zone and auto attackable checks
		Creature creatureTarget = (Creature) target;
		if (creatureTarget.isInsideZone(ZoneId.PEACE) || !target.isAutoAttackable(player))
		{
			return false;
		}
		
		// Do not attack guards unless in NPC or Any Target mode
		if (target instanceof Guard)
		{
			int targetMode = player.getAutoPlaySettings().getNextTargetMode();
			return (targetMode == 3 /* NPC */) || (targetMode == 0 /* Any Target */);
		}
		
		return true;
	}
	
	// Método auxiliar para verificar se pode usar magia
	private boolean canUseMagic(Player player, WorldObject target, Skill skill)
	{
		// Verifica consumo de itens
		if ((skill.getItemConsume() > 0) && (player.getInventory().getInventoryItemCount(skill.getItemConsumeId(), -1) < skill.getItemConsume()))
		{
			return false;
		}
		
		// Verifica consumo de MP
		if ((skill.getMpConsume() > 0) && (player.getCurrentMp() < skill.getMpConsume()))
		{
			return false;
		}
		
		// Verifica se a skill está disponível e condições são atendidas
		return !player.isSkillDisabled(skill, false) && skill.checkCondition(player, target, false);
	}
	
	// Gerenciamento de tarefas de auto uso
	public void startAutoUseTask(Player player)
	{
		if (allPlayers.contains(player))
		{
			return;
		}
		
		allPlayers.add(player);
		
		// Encontra o pool com menos jogadores
		Set<Player> leastLoadedPool = playerPools.stream().min(Comparator.comparingInt(Set::size)).orElse(playerPools.get(0));
		
		leastLoadedPool.add(player);
	}
	
	public boolean stopAutoUseTask(Player player)
	{
		if (!allPlayers.remove(player))
		{
			return false;
		}
		
		player.getAutoUseSettings().resetSkillOrder();
		healTargets.remove(player); // Limpa alvo de cura
		sweepTargets.remove(player); // NOVO: Limpa alvo de spoil
		
		// Remove o jogador de todos os pools
		playerPools.forEach(pool -> pool.remove(player));
		
		return true;
	}
	
	// Método para desligamento graceful
	public void shutdown()
	{
		scheduler.shutdown();
		try
		{
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
			{
				scheduler.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
		
		// Para o auto uso de todos os jogadores
		new ArrayList<>(allPlayers).forEach(this::stopAutoUseTask);
	}
	
}