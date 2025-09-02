package org.l2jmobius.gameserver.model.actor.instance;

import java.util.ArrayList;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class Support extends Folk
{
	private final ArrayList<Skill> skillsF = new ArrayList<>()
	{
		{
			add(0, SkillTable.getInstance().getSkill(1204, 2)); // Wind Walk
			add(1, SkillTable.getInstance().getSkill(1040, 3)); // Shield
			add(2, SkillTable.getInstance().getSkill(1036, 2)); // Magic Barrier
			add(3, SkillTable.getInstance().getSkill(1045, 6)); // Blessed Body
			add(4, SkillTable.getInstance().getSkill(1268, 4)); // Vampiric Rage
			add(5, SkillTable.getInstance().getSkill(1044, 3)); // Regeneration
			add(6, SkillTable.getInstance().getSkill(1086, 2)); // Haste
		}
	};
	private final ArrayList<Skill> skillsM = new ArrayList<>()
	{
		{
			add(0, SkillTable.getInstance().getSkill(1204, 2)); // Wind Walk
			add(1, SkillTable.getInstance().getSkill(1040, 3)); // Shield
			add(2, SkillTable.getInstance().getSkill(1036, 2)); // Magic Barrier
			add(3, SkillTable.getInstance().getSkill(1048, 6)); // Bless the Soul
			add(4, SkillTable.getInstance().getSkill(1085, 3)); // Acumen
			add(5, SkillTable.getInstance().getSkill(1078, 6)); // Concentration
			add(6, SkillTable.getInstance().getSkill(1059, 3)); // Empower
		}
	};
	
	public Support(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("supportmax"))
		{
			makeSupportMagic(player);
		}
	}
	
	@Override
	public void makeSupportMagic(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		// Prevent a cursed weapon weilder of being buffed
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		if ((player.getLevel() <= 39))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
			html.setFile("data/html/ayengine/support/level.htm");
			player.sendPacket(html);
			return;
		}
		
		player.stopAllEffects();
		
		if (player.isMageClass())
		{
			for (Skill skill : skillsM)
			{
				if (skill.getSkillType() == SkillType.SUMMON)
				{
					player.doCast(skill);
				}
				else
				{
					broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skill.getLevel(), 0, 0));
					skill.applyEffects(this, player);
				}
			}
		}
		else
		{
			for (Skill skill : skillsF)
			{
				if (skill.getSkillType() == SkillType.SUMMON)
				{
					player.doCast(skill);
				}
				else
				{
					broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skill.getLevel(), 0, 0));
					skill.applyEffects(this, player);
				}
			}
		}
		
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		
		final Summon summon = player.getPet();
		if (summon != null)
		{
			summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
		}
		
		player.broadcastPacket(new SocialAction(player.getObjectId(), 15));
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String name = "data/html/ayengine/support/support.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
		html.setFile(name);
		html.replace("%objectId%", this.getObjectId());
		html.replace("%npcName%", this.getName());
		player.sendPacket(html);
	}
}
