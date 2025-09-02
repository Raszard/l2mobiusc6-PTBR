package org.l2jmobius.gameserver.ayengine.autofarm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.taskmanager.AutoUseTaskManager;
import org.l2jmobius.gameserver.util.Util;

public class AutoFarmTask
{
	// Configurações
	private static final int POOL_SIZE = 300;
	private static final int TASK_DELAY = 300;
	private static final int MAX_IDLE_COUNT = 10;
	private static final int PICKUP_RANGE = 1000;
	private static final int SHORT_RANGE = 600;
	private static final int LONG_RANGE = 1000;
	private static final Integer AUTO_ATTACK_ACTION = 2;
	
	// Estruturas de dados otimizadas
	private final Map<Player, Integer> idleCountMap = new ConcurrentHashMap<>();
	private final Set<Player> allPlayers = ConcurrentHashMap.newKeySet();
	private final List<Set<Player>> playerPools = new CopyOnWriteArrayList<>();
	private final ScheduledExecutorService scheduler;
	private final Map<Player, Location> farmStartLocations = new ConcurrentHashMap<>();
	
	// Instância singleton
	private static final AutoFarmTask INSTANCE = new AutoFarmTask();
	
	private AutoFarmTask()
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
			scheduler.scheduleAtFixedRate(new AutoPlayTask(pool), TASK_DELAY, TASK_DELAY, TimeUnit.MILLISECONDS);
		}
	}
	
	public static AutoFarmTask getInstance()
	{
		return INSTANCE;
	}
	
	// Classe principal de tarefa de autoplay
	private class AutoPlayTask implements Runnable
	{
		private final Set<Player> playersPool;
		
		public AutoPlayTask(Set<Player> playersPool)
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
			playersPool.removeIf(player -> !player.isOnline() && stopAutoPlay(player));
		}
		
		private void processPlayer(Player player)
		{
			if (!shouldProcessPlayer(player))
			{
				return;
			}
			
			if (processCurrentTarget(player))
			{
				return;
			}
			
			if (processPickup(player))
			{
				return;
			}
			
			processNewTarget(player);
		}
		
		private boolean shouldProcessPlayer(Player player)
		{
			return player.isOnline() && !player.isSitting() && !player.isCastingNow() && (player.getQueuedSkill() == null) && !player.isDisabled();
		}
	}
	
	// Processamento do alvo atual
	private boolean processCurrentTarget(Player player)
	{
		WorldObject target = player.getTarget();
		if (!(target instanceof Creature))
		{
			return false;
		}
		
		Creature creature = (Creature) target;
		int targetMode = player.getAutoPlaySettings().getNextTargetMode();
		
		// NOVO: Verifica se o alvo está morrendo e é um monstro
		if (creature.isAlikeDead() && (creature instanceof Monster))
		{
			// Delega o uso de spoil para o AutoUseTaskManager
			AutoUseTaskManager.getInstance().setSweepTarget(player, (Monster) creature);
		}
		
		// Verifica se o alvo ainda é válido
		if (creature.isAlikeDead() || !isTargetModeValid(targetMode, player, creature))
		{
			player.setTarget(null);
			return false;
		}
		
		// Se o alvo está atacando o jogador ou não tem alvo
		if ((creature.getTarget() == player) || (creature.getTarget() == null))
		{
			if (isMageCaster(player))
			{
				return true;
			}
			
			return handleCombatSituation(player, creature);
		}
		
		return false;
	}
	
	private boolean handleCombatSituation(Player player, Creature creature)
	{
		// Verifica se precisa iniciar ataque
		if (shouldStartAttack(player))
		{
			if (!creature.isAutoAttackable(player) || !canSeeTarget(player, creature))
			{
				player.setTarget(null);
				return false;
			}
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
			return true;
		}
		
		// Lida com situações de idle (personagem parado)
		if (isPlayerIdleInCombat(player, creature))
		{
			handleIdleCombatSituation(player, creature);
			return true;
		}
		
		return false;
	}
	
	private boolean isPlayerIdleInCombat(Player player, Creature creature)
	{
		return player.hasAI() && (player.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) && creature.hasAI() && !creature.getAI().isAutoAttacking();
	}
	
	private void handleIdleCombatSituation(Player player, Creature creature)
	{
		Weapon weapon = player.getActiveWeaponItem();
		if (weapon == null)
		{
			return;
		}
		
		int idleCount = idleCountMap.getOrDefault(player, 0);
		if (idleCount > MAX_IDLE_COUNT)
		{
			repositionPlayer(player, creature, weapon);
			idleCountMap.remove(player);
		}
		else
		{
			idleCountMap.put(player, idleCount + 1);
		}
	}
	
	private void repositionPlayer(Player player, Creature creature, Weapon weapon)
	{
		boolean isRanged = weapon.getItemType() == WeaponType.BOW;
		double angle = Util.calculateHeadingFrom(player, creature);
		double radian = Math.toRadians(angle);
		double course = Math.toRadians(180);
		
		double distance = (isRanged ? player.getCollisionRadius() : player.getCollisionRadius() + creature.getTemplate().getCollisionRadius()) * 2;
		
		int xOffset = (int) (Math.cos(Math.PI + radian + course) * distance);
		int yOffset = (int) (Math.sin(Math.PI + radian + course) * distance);
		
		Location newLocation = isRanged ? new Location(player.getX() + xOffset, player.getY() + yOffset, player.getZ()) : new Location(creature.getX() + xOffset, creature.getY() + yOffset, player.getZ());
		
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, newLocation);
	}
	
	// Processamento de coleta de itens
	private boolean processPickup(Player player)
	{
		if (!player.getAutoPlaySettings().doPickup())
		{
			return false;
		}
		
		// Busca itens próximos elegíveis para coleta
		Optional<Item> pickupItem = findPickupItem(player);
		if (!pickupItem.isPresent())
		{
			return false;
		}
		
		Item item = pickupItem.get();
		
		// Move-se para o item se estiver longe
		if (player.calculateDistance2D(item) > 20)
		{
			if (!player.isMoving())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(item.getX(), item.getY(), item.getZ()));
			}
			return true;
		}
		
		// Coleta o item
		player.doPickupItem(item);
		return true;
	}
	
	private Optional<Item> findPickupItem(Player player)
	{
		return player.getKnownList().getKnownObjects().values().stream().filter(Objects::nonNull).filter(WorldObject::isItem).map(obj -> (Item) obj).filter(item -> Util.calculateDistance(player, item, true) <= PICKUP_RANGE).filter(item -> isItemPickupAllowed(player, item)).filter(item -> isItemReachable(player, item)).min(Comparator.comparingDouble(item -> player.calculateDistance2D(item)));
	}
	
	private boolean isItemPickupAllowed(Player player, Item item)
	{
		return !item.getDropProtection().isProtected() || (item.getOwnerId() == player.getObjectId());
	}
	
	private boolean isItemReachable(Player player, Item item)
	{
		return item.isSpawned() && GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), item.getX(), item.getY(), item.getZ(), player.getInstanceId());
	}
	
	// Processamento de busca por novos alvos
	private void processNewTarget(Player player)
	{
		idleCountMap.remove(player); // Reset idle count
		
		Optional<Creature> target = findTarget(player);
		target.ifPresent(creature ->
		{
			player.setTarget(creature);
			if (!isMageCaster(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
			}
		});
	}
	
	private Optional<Creature> findTarget(Player player)
	{
		// Verifica se deve assistir o líder do party
		if (player.getAutoPlaySettings().getNextTargetMode() != 1)
		{
			Optional<Creature> partyTarget = findPartyTarget(player);
			if (partyTarget.isPresent())
			{
				return partyTarget;
			}
		}
		
		// Busca por alvos normais
		return findNormalTarget(player);
	}
	
	private Optional<Creature> findPartyTarget(Player player)
	{
		Party party = player.getParty();
		if (party == null)
		{
			return Optional.empty();
		}
		
		Player leader = party.getLeader();
		if ((leader == null) || (leader == player) || leader.isDead())
		{
			return Optional.empty();
		}
		
		// NOVO: Verifica se o líder precisa de cura (menos de 60% HP)
		if (leader.getCurrentHpPercent() < 60)
		{
			// Define o líder como target para cura e delega para o AutoUseTaskManager
			AutoUseTaskManager.getInstance().setHealTarget(player, leader);
			return Optional.empty(); // Retorna vazio pois está priorizando a cura
		}
		
		// Verifica se está no range do líder
		if (Util.calculateDistance(leader, player, true) >= (Config.ALT_PARTY_RANGE * 2))
		{
			return Optional.empty();
		}
		
		// Segue o líder se não tiver alvo válido
		WorldObject leaderTarget = leader.getTarget();
		if (!(leaderTarget instanceof Creature) || !isValidPartyTarget(party, (Creature) leaderTarget))
		{
			if ((player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) && !player.isDisabled())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, leader);
			}
			return Optional.empty();
		}
		
		return Optional.of((Creature) leaderTarget);
	}
	
	private boolean isValidPartyTarget(Party party, Creature target)
	{
		return target.isAttackable() || (target.isPlayable() && !party.getPartyMembers().contains(target));
	}
	
	private Optional<Creature> findNormalTarget(Player player)
	{
		int targetMode = player.getAutoPlaySettings().getNextTargetMode();
		int searchRadius = player.getAutoPlaySettings().isShortRange() && (targetMode != 2) ? SHORT_RANGE : LONG_RANGE;
		
		// Filtro para criaturas válidas
		Predicate<Creature> filter = creature -> (creature != null) && !creature.isAlikeDead() && isRespectfulHuntingValid(player, creature) && isTargetModeValid(targetMode, player, creature) && isWithinZRange(player, creature) && canSeeTarget(player, creature) && canMoveToTarget(player, creature);
		
		// Busca a criatura mais próxima que atenda aos critérios
		return player.getKnownList().getKnownCharactersInRadius(searchRadius).stream().filter(filter).min(Comparator.comparingDouble(creature -> player.calculateDistance2D(creature))).or(() -> handleAutoMonsterTargeting(player, targetMode, searchRadius));
	}
	
	private Optional<Monster> handleAutoMonsterTargeting(Player player, int targetMode, int searchRadius)
	{
		if (player.getAutoUseSettings().getAutoMonsters().isEmpty())
		{
			return Optional.empty();
		}
		
		// Pega posição inicial salva (quando começou o auto farm)
		Location startLoc = farmStartLocations.get(player);
		
		// Tenta encontrar o próximo monstro na lista de prioridade
		int nextMonsterId = player.getAutoUseSettings().getNextMonsterId();
		
		return player.getKnownList().getKnownCharactersInRadius(searchRadius).stream().filter(creature -> creature instanceof Monster).map(creature -> (Monster) creature)
			// ID do monstro
			.filter(monster -> monster.getNpcId() == nextMonsterId)
			// Target mode válido
			.filter(monster -> isTargetModeValid(targetMode, player, monster))
			// NOVO: respeitar Limit Zone
			.filter(monster ->
			{
				if (player.getAutoPlaySettings().isLimitZone() && (startLoc != null))
				{
					return Util.calculateDistance(startLoc.getX(), startLoc.getY(), startLoc.getZ(), monster.getLocation().getX(), monster.getLocation().getY(), monster.getLocation().getZ(), true) <= searchRadius;
				}
				return true;
			})
			// Escolhe o mais próximo
			.min(Comparator.comparingDouble(monster -> player.calculateDistance2D(monster))).or(() ->
			{
				player.getAutoUseSettings().incrementMonsterOrder();
				return Optional.empty();
			});
	}
	
	private boolean isRespectfulHuntingValid(Player player, Creature creature)
	{
		if (!player.getAutoPlaySettings().isRespectfulHunting() || creature.isPlayable())
		{
			return true;
		}
		
		// Verifica se a criatura está atacando outro jogador ou summon
		WorldObject creatureTarget = creature.getTarget();
		if ((creatureTarget == null) || (creatureTarget == player))
		{
			return true;
		}
		
		Summon playerSummon = player.getPet();
		return (playerSummon != null) && (creatureTarget.getObjectId() == playerSummon.getObjectId());
	}
	
	private boolean isWithinZRange(Player player, Creature creature)
	{
		return Math.abs(player.getZ() - creature.getZ()) < 180;
	}
	
	private boolean canSeeTarget(Player player, Creature creature)
	{
		return GeoEngine.getInstance().canSeeTarget(player, creature);
	}
	
	private boolean canMoveToTarget(Player player, Creature creature)
	{
		return GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), creature.getX(), creature.getY(), creature.getZ(), player.getInstanceId());
	}
	
	private boolean shouldStartAttack(Player player)
	{
		return player.hasAI() && !player.isAttackingNow() && !player.isCastingNow() && !player.isMoving() && !player.isDisabled() && (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK);
	}
	
	// Métodos auxiliares
	private boolean isMageCaster(Player player)
	{
		return !player.getAutoUseSettings().getAutoActions().contains(AUTO_ATTACK_ACTION);
	}
	
	private boolean isTargetModeValid(int mode, Player player, Creature creature)
	{
		switch (mode)
		{
			case 1: // Monster
				return creature.isMonster() && creature.isAutoAttackable(player);
			case 2: // Characters
				return creature.isPlayable() && creature.isAutoAttackable(player);
			case 3: // NPC
				return creature.isNpc() && !creature.isMonster() && !creature.isInsideZone(ZoneId.PEACE);
			default: // Any Target
				return (creature.isNpc() && !creature.isInsideZone(ZoneId.PEACE)) || (creature.isPlayable() && creature.isAutoAttackable(player));
		}
	}
	
	// Gerenciamento de autoplay
	public void startAutoPlay(Player player)
	{
		if (allPlayers.contains(player))
		{
			return;
		}
		
		player.setAutoPlaying(true);
		player.onActionRequest();
		allPlayers.add(player);
		farmStartLocations.put(player, new Location(player.getX(), player.getY(), player.getZ()));
		
		// Encontra o pool com menos jogadores
		Set<Player> leastLoadedPool = playerPools.stream().min(Comparator.comparingInt(Set::size)).orElse(playerPools.get(0));
		
		leastLoadedPool.add(player);
	}
	
	public boolean stopAutoPlay(Player player)
	{
		if (!allPlayers.remove(player))
		{
			return false;
		}
		
		player.setAutoPlaying(false);
		idleCountMap.remove(player);
		farmStartLocations.remove(player);
		
		// Remove o jogador de todos os pools
		playerPools.forEach(pool -> pool.remove(player));
		
		// Faz o pet seguir o dono
		Optional.ofNullable(player.getPet()).ifPresent(Summon::followOwner);
		
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
		
		// Para o autoplay de todos os jogadores
		new ArrayList<>(allPlayers).forEach(this::stopAutoPlay);
	}
}