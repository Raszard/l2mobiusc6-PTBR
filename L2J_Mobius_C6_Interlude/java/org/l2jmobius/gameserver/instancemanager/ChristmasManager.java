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
package org.l2jmobius.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Control for sequence of Christmas.
 * @version 1.00
 * @author Darki699
 */
public class ChristmasManager
{
	private static final Logger LOGGER = Logger.getLogger(ChristmasManager.class.getName());
	
	protected List<Npc> objectQueue = new ArrayList<>();
	protected Random rand = new Random();
	
	// X-Mas message list
	protected String[] message =
	{
		"Ho Ho Ho... Feliz Natal!",
		"Deus é amor...",
		"O Natal é tudo sobre amor...",
		"O Natal é, portanto, sobre Deus e amor...",
		"O amor é a chave para a paz entre todas as criaturas da linhagem...",
		"O amor é a chave para a paz e felicidade dentro de toda a criação...",
		"O amor precisa ser praticado - O amor precisa fluir - O amor precisa fazer as pessoas felizes...",
		"O amor começa com seu parceiro, filhos e família e se expande para todo o mundo.",
		"Deus abençoe a todos.",
		"Deus abençoe a Linhagem.",
		"Perdoe a todos.",
		"Peça perdão, até mesmo para aqueles que já se foram.",
		"Dê amor de várias maneiras diferentes para seus familiares, parentes, vizinhos e 'estrangeiros'.",
		"Amplie o sentimento dentro de si de ser membro de uma família muito maior do que sua família física.",
		"O MAIS importante - o Natal é uma festa de BÊNÇÃO dada a VOCÊ por Deus e todos os entes queridos de volta a Deus!",
		"Abra-se para toda a bem-aventurança divina, perdão e ajuda divina que lhe são oferecidos por muitos outros E por DEUS.",
		"Leve com calma. Relaxe nos próximos dias.",
		"Todo dia é dia de Natal - DEPENDE DE VOCÊ criar a atitude interna apropriada e se abrir para o amor pelos outros E dos outros dentro de SI MESMO!",
		"Paz e silêncio. Atividades reduzidas. Mais tempo para sua família mais próxima. Se possível, NENHUM outro compromisso ou viagem pode ajudá-lo a realmente RECEBER toda a bênção oferecida.",
		"Qualquer coisa que seja oferecida a você por Deus ou entra no SEU coração e alma ou é PERDIDA para SEMPRE!!! ou pelo menos até outro dia assim - no próximo Natal ou algo assim!!",
		"A bem-aventurança e o amor divino NUNCA podem ser armazenados e recebidos depois.",
		"Há, o ano todo, uma grande quantidade de amor e bênçãos disponíveis de Deus, de seu Guru e de outras almas amorosas, mas os dias de Natal são um período prolongado PARA TODO O PLANETA.",
		"Por favor, abra seu coração e aceite todo o amor e bênção - Para o seu benefício, assim como para o benefício de todos os seus entes queridos.",
		"Amados filhos de Deus",
		"Além dos dias de Natal e da temporada de Natal - o amor de Natal continua, a bem-aventurança do Natal continua, o sentimento de Natal se expande.",
		"O espírito santo do Natal é o espírito santo de Deus e o amor de Deus para todos os dias.",
		"Quando o espírito de Natal vive e continua...",
		"Quando o poder do amor criado durante os dias pré-Natal é mantido vivo e crescendo.",
		"A paz entre toda a humanidade também está crescendo =)",
		"O presente sagrado do amor é um presente eterno de amor colocado em seu coração como uma semente.",
		"Dezenas de milhões de humanos em todo o mundo estão mudando em seus corações durante semanas de preparação para o Natal e encontram seu pico de poder de amor nas noites e dias de Natal.",
		"O que é especial nesses dias é dar a todos vocês esse poder muito especial de amor, o poder de perdoar, o poder de fazer os outros felizes, o poder de se juntar à pessoa amada no caminho da vida amorosa.",
		"É apenas a sua decisão agora que faz a diferença!",
		"É apenas o seu foco de vida agora que faz todas as mudanças. É a sua mudança de assuntos puramente mundanos para o poder do amor de Deus que habita em todos nós, que lhe deu o poder de mudar seu próprio comportamento do seu comportamento normal ao longo do ano.",
		"A decisão de amor, paz e felicidade é a decisão certa.",
		"O que quer que você foque enche sua mente e, subsequentemente, enche seu coração.",
		"Ninguém além de você pode mudar seu foco nesses últimos dias de Natal e nos dias de amor que você pode ter experimentado em temporadas de Natal anteriores.",
		"O amor de Deus está sempre presente.",
		"O amor de Deus sempre esteve com o mesmo poder, pureza e quantidade disponível para todos vocês.",
		"Expanda o espírito do amor de Natal e a alegria do Natal para abranger todo o ano da sua vida...",
		"Faça durante todo o ano o que cria esse sentimento especial de Natal de amor, alegria e felicidade.",
		"Expanda o verdadeiro sentimento de Natal, expanda o amor que você já deu em seus dias de máximo poder de amor...",
		"Expanda o poder do amor por mais e mais dias.",
		"Refocalize no que trouxe seu amor ao seu pico de poder e refocalize nesses objetos e ações no seu foco mental e ações.",
		"Lembre-se das pessoas e do ambiente que você tinha quando se sentia mais feliz, mais amado, mais mágico.",
		"Pessoas de verdadeiro espírito amoroso - quem estava presente, lembre-se de seus nomes, lembre-se daquela pessoa que pode ter tido o maior impacto em amor naquelas horas de momentos mágicos de amor...",
		"A decoração do seu ambiente - A decoração pode ajudar a focar no amor - Ou a falta de decoração pode fazer você se desviar para a escuridão ou para os negócios - longe do amor...",
		"Músicas de amor, músicas cheias de alegria - qualquer uma das milhares de verdadeiras músicas de amor tocantes e músicas felizes contribuem para o estabelecimento de uma atitude interna perceptível de amor.",
		"As músicas podem afinar e abrir nosso coração para o amor de Deus e de nossos entes queridos.",
		"Seu poder de vontade e foco mental podem manter o Amor de Natal e a alegria do Natal vivos além da temporada de Natal, por toda a eternidade.",
		"Aproveite seu amor para sempre!",
		"O Natal pode ser todos os dias - Assim que você realmente amar todos os dias =)",
		"O Natal é quando você ama a todos e é amado por todos.",
		"O Natal é quando você é verdadeiramente feliz ao criar verdadeira felicidade nos outros com amor do fundo do seu coração.",
		"O segredo da criação de Deus é que nenhuma pessoa pode realmente amar sem a ignição de seu amor.",
		"Você precisa de outra pessoa para amar e para receber amor, uma pessoa para realmente se apaixonar para acender seu próprio fogo divino de amor.",
		"Deus criou muitos e todos são feitos de amor e todos são feitos para amar...",
		"O milagre do amor só funciona se você quiser se tornar um membro plenamente amoroso da família do amor divino.",
		"Uma vez que você começou a se apaixonar pela pessoa que Deus criou para você - toda a sua vida eterna será um fogo permanente de milagres de amor... Eternamente!",
		"Que todos tenham um tempo feliz no Natal de cada ano. Feliz Natal!",
		"O dia de Natal é um tempo de amor. É um tempo para mostrar nossa afeição por nossos entes queridos. É tudo sobre amor.",
		"Tenha um maravilhoso Natal. Que Deus abençoe nossa família. Eu amo vocês todos.",
		"Deseje a todas as criaturas vivas um Feliz Natal e um Feliz Ano Novo! A propósito, gostaria que compartilhássemos uma calorosa amizade em todos os lugares.",
		"Assim como os animais precisam de paz de espírito, as pessoas e também as árvores precisam de paz de espírito. É por isso que eu digo, todas as criaturas estão esperando no Senhor pela salvação. Que Deus abençoe todas as criaturas no mundo inteiro.",
		"Feliz Natal!",
		"Que a graça do nosso Poderoso Pai esteja com todos vocês nesta véspera de Natal. Tenham um Natal abençoado e um feliz Ano Novo.",
		"Feliz Natal, meus filhos. Que este novo ano lhes dê todas as coisas que vocês merecem. E que a paz finalmente seja de vocês.",
		"Desejo a todos um Feliz Natal! Que o Espírito Santo esteja com vocês o tempo todo.",
		"Que você tenha o melhor Natal este ano e que todos os seus sonhos se tornem realidade.",
		"Que o milagre do Natal encha seu coração com calor e amor. Feliz Natal!"
	};
	
	protected String[] sender =
	{
		"Santa Claus",
		"Papai Noel",
		"Shengdan Laoren",
		"Santa",
		"Viejo Pascuero",
		"Sinter Klaas",
		"Father Christmas",
		"Saint Nicholas",
		"Joulupukki",
		"Pere Noel",
		"Saint Nikolaus",
		"Kanakaloka",
		"De Kerstman",
		"Winter grandfather",
		"Babbo Natale",
		"Hoteiosho",
		"Kaledu Senelis",
		"Black Peter",
		"Kerstman",
		"Julenissen",
		"Swiety Mikolaj",
		"Ded Moroz",
		"Julenisse",
		"El Nino Jesus",
		"Jultomten",
		"Reindeer Dasher",
		"Reindeer Dancer",
		"Christmas Spirit",
		"Reindeer Prancer",
		"Reindeer Vixen",
		"Reindeer Comet",
		"Reindeer Cupid",
		"Reindeer Donner",
		"Reindeer Donder",
		"Reindeer Dunder",
		"Reindeer Blitzen",
		"Reindeer Bliksem",
		"Reindeer Blixem",
		"Reindeer Rudolf",
		"Christmas Elf"
	};
	
	// Presents List:
	protected int[] presents =
	{
		5560,
		5560,
		5560,
		5560,
		5560, /* x-mas tree */
		5560,
		5560,
		5560,
		5560,
		5560,
		5561,
		5561,
		5561,
		5561,
		5561, /* special x-mas tree */
		5562,
		5562,
		5562,
		5562, /* 1st Carol */
		5563,
		5563,
		5563,
		5563, /* 2nd Carol */
		5564,
		5564,
		5564,
		5564, /* 3rd Carol */
		5565,
		5565,
		5565,
		5565, /* 4th Carol */
		5566,
		5566,
		5566,
		5566, /* 5th Carol */
		5583,
		5583, /* 6th Carol */
		5584,
		5584, /* 7th Carol */
		5585,
		5585, /* 8th Carol */
		5586,
		5586, /* 9th Carol */
		5587,
		5587, /* 10th Carol */
		6403,
		6403,
		6403,
		6403, /* Star Shard */
		6403,
		6403,
		6403,
		6403,
		6406,
		6406,
		6406,
		6406, /* FireWorks */
		6407,
		6407, /* Large FireWorks */
		5555, /* Token of Love */
		7836, /* Santa Hat #1 */
		9138, /* Santa Hat #2 */
		8936, /* Santa's Antlers Hat */
		6394, /* Red Party Mask */
		5808, /* Black Party Mask */
	};
	
	protected Future<?> _XMasMessageTask = null;
	protected Future<?> _XMasPresentsTask = null;
	protected int isManagerInit = 0;
	protected long _IntervalOfChristmas = 600000; // 10 minutes
	private static final int FIRST = 25000;
	private static final int LAST = 73099;
	
	/************************************** Initial Functions **************************************/
	
	/**
	 * Empty constructor Does nothing
	 */
	public ChristmasManager()
	{
		//
	}
	
	/**
	 * @return an instance of <b>this</b> InstanceManager.
	 */
	public static ChristmasManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * initialize <b>this</b> ChristmasManager
	 * @param player
	 */
	public void init(Player player)
	{
		if (isManagerInit > 0)
		{
			player.sendMessage("O Gerenciador de Natal já começou ou está processando. Por favor, seja paciente....");
			return;
		}
		
		player.sendMessage("Iniciado!!!! Isso levará de 2 a 3 horas (para reduzir os atrasos do sistema ao mínimo), seja paciente... O processo está funcionando em segundo plano e gerará NPCs, dará presentes e mensagens a uma taxa fixa.");
		
		// Tasks:
		spawnTrees();
		
		startFestiveMessagesAtFixedRate();
		isManagerInit++;
		
		givePresentsAtFixedRate();
		isManagerInit++;
		
		checkIfOkToAnnounce();
	}
	
	/**
	 * ends <b>this</b> ChristmasManager
	 * @param player
	 */
	public void end(Player player)
	{
		if (isManagerInit < 4)
		{
			if (player != null)
			{
				player.sendMessage("O Gerenciador de Natal ainda não foi ativado. Já terminou ou está processando....");
			}
			
			return;
		}
		
		if (player != null)
		{
			player.sendMessage("Terminando! Isso pode demorar um pouco, por favor, seja paciente...");
		}
		
		ThreadPool.execute(new DeleteSpawns());
		endFestiveMessagesAtFixedRate();
		isManagerInit--;
		
		endPresentGivingAtFixedRate();
		isManagerInit--;
		
		checkIfOkToAnnounce();
	}
	
	/**
	 * Main function - spawns all trees.
	 */
	public void spawnTrees()
	{
		final GetTreePos gtp = new GetTreePos(FIRST);
		ThreadPool.execute(gtp);
	}
	
	/**
	 * returns a random X-Mas tree Npc Id.
	 * @return int tree Npc Id.
	 */
	protected int getTreeId()
	{
		final int[] ids =
		{
			13006,
			13007
		};
		return ids[rand.nextInt(ids.length)];
	}
	
	/**
	 * gets random world positions according to spawned world objects and spawns x-mas trees around them...
	 */
	public class GetTreePos implements Runnable
	{
		private int _iterator;
		private Future<?> _task;
		
		public GetTreePos(int iter)
		{
			_iterator = iter;
		}
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		@Override
		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}
			try
			{
				WorldObject obj = null;
				
				while (obj == null)
				{
					obj = SpawnTable.getInstance().getTemplate(_iterator).getLastSpawn();
					_iterator++;
					
					if ((obj instanceof Attackable) && (rand.nextInt(100) > 10))
					{
						obj = null;
					}
				}
				
				if (rand.nextInt(100) > 50)
				{
					spawnOneTree(getTreeId(), (obj.getX() + rand.nextInt(200)) - 100, (obj.getY() + rand.nextInt(200)) - 100, obj.getZ());
				}
			}
			catch (Throwable t)
			{
			}
			
			if (_iterator >= LAST)
			{
				isManagerInit++;
				
				final SpawnSantaNPCs ssNPCs = new SpawnSantaNPCs(FIRST);
				_task = ThreadPool.schedule(ssNPCs, 300);
				ssNPCs.setTask(_task);
				
				return;
			}
			
			_iterator++;
			final GetTreePos gtp = new GetTreePos(_iterator);
			_task = ThreadPool.schedule(gtp, 300);
			gtp.setTask(_task);
		}
	}
	
	/**
	 * Delete all x-mas spawned trees from the world. Delete all x-mas trees spawns, and clears the Npc tree queue.
	 */
	public class DeleteSpawns implements Runnable
	{
		@Override
		public void run()
		{
			if ((objectQueue == null) || objectQueue.isEmpty())
			{
				return;
			}
			
			for (Npc deleted : objectQueue)
			{
				if (deleted == null)
				{
					continue;
				}
				
				try
				{
					World.getInstance().removeObject(deleted);
					
					deleted.decayMe();
					deleted.deleteMe();
				}
				catch (Throwable t)
				{
				}
			}
			
			objectQueue.clear();
			objectQueue = null;
			isManagerInit = isManagerInit - 2;
			checkIfOkToAnnounce();
		}
	}
	
	/**
	 * Spawns one x-mas tree at a given location x,y,z
	 * @param id - int tree npc id.
	 * @param x - int loc x
	 * @param y - int loc y
	 * @param z - int loc z
	 */
	protected void spawnOneTree(int id, int x, int y, int z)
	{
		try
		{
			final NpcTemplate template1 = NpcTable.getInstance().getTemplate(id);
			final Spawn spawn = new Spawn(template1);
			spawn.setId(IdManager.getInstance().getNextId());
			
			spawn.setX(x);
			spawn.setY(y);
			spawn.setZ(z);
			
			final Npc tree = spawn.doSpawn();
			World.getInstance().storeObject(tree);
			objectQueue.add(tree);
		}
		catch (Throwable t)
		{
		}
	}
	
	/**
	 * Ends X-Mas messages sent to players, and terminates the thread.
	 */
	private void endFestiveMessagesAtFixedRate()
	{
		if (_XMasMessageTask != null)
		{
			_XMasMessageTask.cancel(true);
			_XMasMessageTask = null;
		}
	}
	
	/**
	 * Starts X-Mas messages sent to all players, and initialize the thread.
	 */
	
	private void startFestiveMessagesAtFixedRate()
	{
		final SendXMasMessage xmasMessage = new SendXMasMessage();
		_XMasMessageTask = ThreadPool.scheduleAtFixedRate(xmasMessage, 60000, _IntervalOfChristmas);
	}
	
	/**
	 * Sends X-Mas messages to all world players.
	 */
	class SendXMasMessage implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				for (Player pc : World.getInstance().getAllPlayers())
				{
					if (pc == null)
					{
						continue;
					}
					else if (!pc.isOnline())
					{
						continue;
					}
					
					pc.sendPacket(getXMasMessage());
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	/**
	 * Returns a random X-Mas message.
	 * @return CreatureSay message
	 */
	protected CreatureSay getXMasMessage()
	{
		return new CreatureSay(0, ChatType.HERO_VOICE, getRandomSender(), getRandomXMasMessage());
	}
	
	/**
	 * Returns a random name of the X-Mas message sender, sent to players
	 * @return String of the message sender's name
	 */
	
	private String getRandomSender()
	{
		return sender[rand.nextInt(sender.length)];
	}
	
	/**
	 * Returns a random X-Mas message String
	 * @return String containing the random message.
	 */
	
	private String getRandomXMasMessage()
	{
		return message[rand.nextInt(message.length)];
	}
	
	/**
	 * Starts X-Mas Santa presents sent to all players, and initialize the thread.
	 */
	private void givePresentsAtFixedRate()
	{
		final XMasPresentGivingTask xmasPresents = new XMasPresentGivingTask();
		_XMasPresentsTask = ThreadPool.scheduleAtFixedRate(xmasPresents, _IntervalOfChristmas, _IntervalOfChristmas * 3);
	}
	
	class XMasPresentGivingTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				for (Player pc : World.getInstance().getAllPlayers())
				{
					if (pc == null)
					{
						continue;
					}
					else if (!pc.isOnline())
					{
						continue;
					}
					else if (pc.getInventoryLimit() <= pc.getInventory().getSize())
					{
						pc.sendMessage("O Papai Noel queria te dar um presente, mas seu inventário estava cheio :(");
						continue;
					}
					else if (rand.nextInt(100) < 50)
					{
						final int itemId = getSantaRandomPresent();
						final Item item = ItemTable.getInstance().createItem("Christmas Event", itemId, 1, pc);
						pc.getInventory().addItem("Christmas Event", item.getItemId(), 1, pc, pc);
						final String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
						SystemMessage sm;
						sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
						sm.addString(itemName + " da Sacola de Presentes do Papai Noel...");
						pc.broadcastPacket(sm);
					}
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	protected int getSantaRandomPresent()
	{
		return presents[rand.nextInt(presents.length)];
	}
	
	/**
	 * Ends X-Mas present giving to players, and terminates the thread.
	 */
	private void endPresentGivingAtFixedRate()
	{
		if (_XMasPresentsTask != null)
		{
			_XMasPresentsTask.cancel(true);
			_XMasPresentsTask = null;
		}
	}
	
	// NPC Ids: 31863 , 31864
	public class SpawnSantaNPCs implements Runnable
	{
		private int _iterator;
		
		private Future<?> _task;
		
		public SpawnSantaNPCs(int iter)
		{
			_iterator = iter;
		}
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		@Override
		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}
			
			try
			{
				WorldObject obj = null;
				while (obj == null)
				{
					obj = SpawnTable.getInstance().getTemplate(_iterator).getLastSpawn();
					_iterator++;
					if (obj instanceof Attackable)
					{
						obj = null;
					}
				}
				
				if ((rand.nextInt(100) < 80) && (obj instanceof Npc))
				{
					spawnOneTree(getSantaId(), (obj.getX() + rand.nextInt(500)) - 250, (obj.getY() + rand.nextInt(500)) - 250, obj.getZ());
				}
			}
			catch (Throwable t)
			{
			}
			
			if (_iterator >= LAST)
			{
				isManagerInit++;
				checkIfOkToAnnounce();
				return;
			}
			
			_iterator++;
			final SpawnSantaNPCs ssNPCs = new SpawnSantaNPCs(_iterator);
			_task = ThreadPool.schedule(ssNPCs, 300);
			ssNPCs.setTask(_task);
		}
	}
	
	protected int getSantaId()
	{
		return rand.nextInt(100) < 50 ? 31863 : 31864;
	}
	
	protected void checkIfOkToAnnounce()
	{
		if (isManagerInit == 4)
		{
			AnnouncementsTable.getInstance().announceToAll("O evento de Natal começou, tenham um Feliz Natal e um Próspero Ano Novo.");
			AnnouncementsTable.getInstance().announceToAll("O evento de Natal terminará em 24 horas.");
			LOGGER.info("ChristmasManager:Init ChristmasManager was started successfully, have a festive holiday.");
			
			final EndEvent ee = new EndEvent();
			final Future<?> task = ThreadPool.schedule(ee, 86400000);
			ee.setTask(task);
			
			isManagerInit = 5;
		}
		
		if (isManagerInit == 0)
		{
			AnnouncementsTable.getInstance().announceToAll("O evento de Natal terminou... Espero que tenham aproveitado as festividades.");
			LOGGER.info("ChristmasManager:Terminated ChristmasManager.");
			isManagerInit = -1;
		}
	}
	
	public class EndEvent implements Runnable
	{
		private Future<?> _task;
		
		public void setTask(Future<?> task)
		{
			_task = task;
		}
		
		@Override
		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}
			
			end(null);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ChristmasManager INSTANCE = new ChristmasManager();
	}
}
