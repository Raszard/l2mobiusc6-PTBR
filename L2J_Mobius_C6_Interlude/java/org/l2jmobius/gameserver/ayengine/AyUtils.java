package org.l2jmobius.gameserver.ayengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.l2jmobius.gameserver.ayengine.search.MobDesc;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.enums.NpcRace;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.type.ArmorType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.spawn.Spawn;

public class AyUtils
{
	public int hairTypeInt(String type)
	{
		switch (type)
		{
			case "Tipo A":
			{
				
				return 0;
			}
			case "Tipo B":
			{
				
				return 1;
			}
			case "Tipo C":
			{
				
				return 2;
			}
			case "Tipo D":
			{
				
				return 3;
			}
			case "Tipo E":
			{
				
				return 4;
			}
			case "Tipo F":
			{
				
				return 5;
			}
			case "Tipo G":
			{
				
				return 6;
			}
			default:
			{
				return 0;
			}
		}
	}
	
	public MobDesc getMobDesc(int mobId)
	{
		NpcRace raceId = NpcTable.getInstance().getTemplate(mobId).getRace();
		MobDesc mob;
		if (getType2(mobId) == "NPC")
		{
			mob = new MobDesc("icon.skill4295", "Humanóide", "Eles têm dois braços, duas pernas e andam eretos. a cultura e a vida comunitária variam de acordo com a raça.");
			return mob;
		}
		switch (raceId)
		{
			case UNDEAD:
			{
				mob = new MobDesc("icon.skill4290", "Morto-vivo", "Chamados de seus túmulos por magia negra, uma maldição ou o poder de uma mente maligna, a maioria dessas criaturas carece de inteligência e pode realizar apenas ações simples. no entanto, alguns mortos-vivos de alto escalão possuem grande conhecimento e sofisticação, mesmo quando comparados aos humanóides comuns.");
				return mob;
			}
			case MAGICCREATURE:
			{
				mob = new MobDesc("icon.skill4291", "Criatura Mágica", "O nome comum para os itens biotecnológicos das Eras da Magia e dos Titãs. Seu Atq.M. é geralmente forte e às vezes usam magia de alta habilidade, como teletransporte.");
				return mob;
			}
			case BEAST:
			{
				mob = new MobDesc("icon.skill4292", "Fera", "Eles são animais que caçam humanóides para se alimentar. Alguns deles são criaturas mitológicas que existiam antes das origens da humanidade.");
				return mob;
			}
			case ANIMAL:
			{
				mob = new MobDesc("icon.skill4293", "Animal", "Esses animais gerais são caçados por humanóides. Às vezes, eles atacam humanóides, mas apenas para se defender ou defender seu território.");
				return mob;
			}
			case PLANT:
			{
				mob = new MobDesc("icon.skill4294", "Planta", "Estes consistem em árvores, gramíneas, cogumelos e fungos. Criaturas instintivas, geralmente é impossível conversar com elas.");
				return mob;
			}
			case HUMANOID:
			{
				mob = new MobDesc("icon.skill4295", "Humanóide", "Eles têm dois braços, duas pernas e andam eretos. a cultura e a vida comunitária variam de acordo com a raça.");
				return mob;
			}
			case SPIRIT:
			{
				mob = new MobDesc("icon.skill4296", "Espírito", "Eles não são necessariamente criaturas, mas seres de energia elementar. Eles basicamente pertencem aos 4 elementos, que são água, fogo, vento e terra, mas também habitam em objetos naturais. Eles são conhecidos por serem governados pelos deuses da água, fogo, vento e terra.");
				return mob;
			}
			case ANGEL:
			{
				mob = new MobDesc("icon.skill4297", "Anjo", "Essas criaturas do reino celestial ou da raça possuem bênçãos sagradas e eram originalmente criaturas dos espíritos de luz. em alguns casos muito raros, os anjos podem manifestar a vontade dos deuses na realidade.");
				return mob;
			}
			case DEMON:
			{
				mob = new MobDesc("icon.skill4298", "Demônio", "Esta raça das trevas se opõe aos anjos. Os seres que são alterados pela maldição das trevas também se tornam demônios.");
				return mob;
			}
			case DRAGON:
			{
				mob = new MobDesc("icon.skill4299", "Dragão", "É o nome geral para as criaturas mais poderosas existentes - Dragão Verdadeiro, que representa as criaturas malignas produzidas por Shilen e seus parentes. a maioria desta raça voa com duas asas como a dos répteis e emana forte energia pela boca.");
				return mob;
			}
			case GIANT:
			{
				mob = new MobDesc("icon.skill4300", "Gigante", "Eles eram os governantes do mundo durante a Era dos Deuses. Eles têm uma aparência semelhante aos humanóides e são 2 a 3 vezes maiores que os humanóides. Eles já tiveram uma cultura altamente civilizada, mas evoluíram para que ajam de acordo com seus instintos muitas vezes.");
				return mob;
			}
			case BUG:
			{
				mob = new MobDesc("icon.skill4301", "Inseto", "Eles consistem em insetos, aranhas e vermes e agem de acordo com seus instintos altamente refinados. Às vezes, seus instintos se apresentam de forma mais desenvolvida do que a da sociedade humanóide.");
				return mob;
			}
			case FAIRIE:
			{
				mob = new MobDesc("icon.skill4302", "Fada", "Muitos membros desta raça antiga deixaram nosso mundo quando ele mudou para se tornar mais hostil. Alguns estudiosos teorizam que as Fadas eram na verdade obras de arte vivas criadas pelos deuses para seu entretenimento.");
				return mob;
			}
			case HUMAN:
			{
				mob = new MobDesc("icon.skill4416_human", "Humando", "A última raça criada pelos deuses, os Humanos venceram a guerra racial pelo domínio do mundo inteiro. Seu forte desejo de dominação talvez seja alimentado pelo fato de que seu tempo de vida é o mais curto entre todas as raças.");
				return mob;
			}
			case ELVE:
			{
				mob = new MobDesc("icon.skill4416_elf", "Elfo", "Esta raça de água adora Eva e ama a paz. Possuem vasto conhecimento, mas carecem de vontade e força para implementá-lo. Os alunos originais da magia, desde então, ensinaram seus princípios aos humanos.");
				return mob;
			}
			case DARKELVE:
			{
				mob = new MobDesc("icon.skill4416_darkelf", "Elfo Sombrio", "Esses elfos formaram uma aliança há muito tempo e se dedicaram a entender as artes das trevas que eram proibidas por seus parentes. Gradualmente, eles se tornaram mais sombrios em sua aparência e personalidade com o passar das gerações. Eles agora são adeptos da prática e uso da magia negra e continuam a adorar Shilen como seu Deus.");
				return mob;
			}
			case ORC:
			{
				mob = new MobDesc("icon.skill4416_orc", "Orc", "Criados por Pa'agrio, os Orcs são fisicamente os mais fortes de todas as raças. Eles adoram os espíritos do passado e têm uma cultura muito tribal.");
				return mob;
			}
			case DWARVE:
			{
				mob = new MobDesc("icon.skill4416_dwarf", "Anão", "Embora incapazes de usar magia, os Anões são fortes e bons com as mãos. Eles têm olhos afiados e mentes inteligentes. Seu conhecimento de negócios é impressionante e às vezes eles inventam estranhos dispositivos mecânicos.");
				return mob;
			}
			case OTHER:
			{
				mob = new MobDesc("icon.skill4416_etc", "Outro", "Uma forma de vida desconhecida que nunca foi descoberta antes.");
				return mob;
			}
			case NONLIVING:
			{
				mob = new MobDesc("icon.skill4416_none", "Seres Não Vivos", "Objetos inanimados");
				return mob;
			}
			case SIEGEWEAPON:
			{
				mob = new MobDesc("icon.skill4416_siegeweapon", "Armas de Cerco", "Essas enormes armas feitas por anões são construídas para sitiar castelos.");
				return mob;
			}
			case DEFENDINGARMY:
			{
				mob = new MobDesc("icon.skill4416_castleguard", "Exército de Defesa", "Os soldados contratados para proteger castelos; normalmente contratado por um senhor.");
				return mob;
			}
			case MERCENARIE:
			{
				mob = new MobDesc("icon.skill4416_mercenary", "Mercenários", "Os soldados contratados para atacar castelos; normalmente contratado por um senhor com pagamento.");
				return mob;
			}
			default:
			{
				mob = new MobDesc("icon.skill4416_etc", "Outro", "Uma forma de vida desconhecida que nunca foi descoberta antes.");
				return mob;
			}
		}
	}
	
	public String getType2(int mobId)
	{
		NpcTemplate mob = NpcTable.getInstance().getTemplate(mobId);
		switch (mob.getType())
		{
			case "Monster":
			{
				
				return "Monstro";
			}
			case "GrandBoss":
			{
				
				return "Chefe Grande";
			}
			case "RaidBoss":
			{
				
				return "Chefe Raid";
			}
			case "Minion":
			{
				
				return "Minion";
			}
			default:
			{
				return "NPC";
			}
		}
	}
	
	public String getGrade(int itemId)
	{
		int Grade = ItemTable.getInstance().getTemplate(itemId).getCrystalType();
		switch (Grade)
		{
			case 1:
			{
				return "<img src=\"symbol.Icon.grade_d\" width=16 height=16>";
			}
			case 2:
			{
				return "<img src=\"symbol.Icon.grade_c\" width=16 height=16>";
			}
			case 3:
			{
				return "<img src=\"symbol.Icon.grade_b\" width=16 height=16>";
			}
			case 4:
			{
				return "<img src=\"symbol.Icon.grade_a\" width=16 height=16>";
			}
			case 5:
			{
				return "<img src=\"symbol.Icon.grade_s\" width=16 height=16>";
			}
			default:
			{
				return "Sem Grau";
			}
		}
	}
	
	public String getTypeString(int itemId)
	{
		int type = ItemTable.getInstance().getTemplate(itemId).getType2();
		switch (type)
		{
			case 0:
			{
				return "Arma";
			}
			case 1:
			{
				return "Armadura";
			}
			case 2:
			{
				return "Acessório";
			}
			case 3:
			{
				return "Missão";
			}
			case 4:
			{
				return "Moeda";
			}
			case 5:
			{
				return "Outros";
			}
			case 6:
			{
				return "Pet";
			}
			case 7:
			{
				return "Pet";
			}
			case 8:
			{
				return "Pet";
			}
			case 9:
			{
				return "Pet";
			}
			default:
			{
				return "Outros";
			}
		}
	}
	
	public String getSlot(int itemId)
	{
		int type = ItemTable.getInstance().getTemplate(itemId).getBodyPart();
		switch (type)
		{
			case ItemTemplate.SLOT_UNDERWEAR:
			{
				return "Roupa de Baixo";
			}
			case ItemTemplate.SLOT_HEAD:
			{
				return "Cabeça";
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				return "Uma Mão";
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				return "Luvas";
			}
			case ItemTemplate.SLOT_CHEST:
			{
				return "Peito";
			}
			case ItemTemplate.SLOT_LEGS:
			{
				return "Pernas";
			}
			case ItemTemplate.SLOT_LR_HAND:
			{
				return "Duas Mãos";
			}
			case ItemTemplate.SLOT_FULL_ARMOR:
			{
				return "Peito e Pernas";
			}
			case ItemTemplate.SLOT_NECK:
			{
				return "Colar";
			}
			case 6:
			{
				return "Brinco";
			}
			case 48:
			{
				return "Anel";
			}
			default:
			{
				return "Outros";
			}
		}
	}
	
	public String getArmorType(ArmorType armorType)
	{
		switch (armorType)
		{
			case NONE:
			{
				return "None";
			}
			case LIGHT:
			{
				return "Leve";
			}
			case HEAVY:
			{
				return "Pesada";
			}
			case MAGIC:
			{
				return "Manto";
			}
			case PET:
			{
				return "Pet";
			}
			default:
			{
				return "Outros";
			}
		}
	}
	
	public String getWeaponType(WeaponType weaponType)
	{
		switch (weaponType)
		{
			case NONE:
			{
				return "None";
			}
			case SWORD:
			{
				return "Espada";
			}
			case BLUNT:
			{
				return "Contusão";
			}
			case DAGGER:
			{
				return "Adaga";
			}
			case BOW:
			{
				return "Arco";
			}
			case POLE:
			{
				return "Lança";
			}
			case ETC:
			{
				return "Outros";
			}
			case FIST:
			{
				return "Punho";
			}
			case DUAL:
			{
				return "Lâmina Dupla";
			}
			case DUALFIST:
			{
				return "Punho";
			}
			case BIGSWORD:
			{
				return "Espada";
			}
			case BIGBLUNT:
			{
				return "Contusão";
			}
			case PET:
			{
				return "Pet";
			}
			case ROD:
			{
				return "Vara";
			}
			
			default:
			{
				return "Outros";
			}
		}
	}
	
	public void MarkMobRadar(Player player, int id)
	{
		int mobId = id;
		player.getRadar().removeAllMarkers();
		List<Spawn> spawns = new ArrayList<>();
		for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if (mobId == spawn.getNpcId())
			{
				spawns.add(spawn);
			}
		}
		
		Collections.sort(spawns, (npc1, npc2) ->
		{
			double dist1 = calcularDistancia(player, npc1);
			double dist2 = calcularDistancia(player, npc2);
			return Double.compare(dist2, dist1);
		});
		
		if (!spawns.isEmpty())
		{
			for (Spawn spawn : spawns)
			{
				player.getRadar().addMarker(spawn.getX(), spawn.getY(), spawn.getZ());
			}
			player.sendMessage("Alvos Localizados!");
		}
		else
		{
			player.sendMessage("Alvos não Localizado!");
		}
	}
	
	private static double calcularDistancia(Player player, Spawn npc)
	{
		return Math.sqrt(Math.pow(npc.getX() - player.getX(), 2) + Math.pow(npc.getY() - player.getY(), 2) + Math.pow(npc.getZ() - player.getZ(), 2));
	}
	
	public int getTypeInt(int itemId)
	{
		if (ItemTable.getInstance().getTemplate(itemId).getType2() < 6)
		{
			return ItemTable.getInstance().getTemplate(itemId).getType2();
		}
		return 6;
	}
	
	private static class InstanceHolder
	{
		private static final AyUtils _instance = new AyUtils();
	}
	
	public static AyUtils getInstance()
	{
		return InstanceHolder._instance;
	}
	
}
