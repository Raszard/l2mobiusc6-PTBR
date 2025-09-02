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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.ControlTower;
import org.l2jmobius.gameserver.model.actor.instance.FortSiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.model.actor.instance.SiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.SiegeNpc;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends ServerPacket
{
	private Creature _creature;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _displayId;
	private boolean _isAttackable;
	private boolean _isSummoned;
	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _rhand;
	private int _lhand;
	private float _collisionHeight;
	private float _collisionRadius;
	protected int _clanCrest;
	protected int _allyCrest;
	protected int _allyId;
	protected int _clanId;
	private String _name = "";
	private String _title = "";
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(Npc cha, Creature attacker)
	{
		if (cha.getFakePlayer() != null)
		{
			attacker.sendPacket(new FakePlayerInfo(cha));
			attacker.broadcastPacket(new StopRotation(cha, cha.getHeading(), 0));
			return;
		}
		_creature = cha;
		_displayId = cha.getTemplate().getDisplayId();
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_isSummoned = false;
		_collisionHeight = cha.getTemplate().getFCollisionHeight();
		_collisionRadius = cha.getTemplate().getFCollisionRadius();
		if (Config.SHOW_NPC_CLAN_CREST && (cha.getCastle() != null) && (cha.getCastle().getOwnerId() != 0) && !cha.isMonster() && !cha.isArtefact() && !(cha instanceof ControlTower))
		{
			if (cha.isInsideZone(ZoneId.TOWN) || cha.isInsideZone(ZoneId.CASTLE) //
				|| (cha instanceof Guard) || (cha instanceof SiegeGuard) || (cha instanceof FortSiegeGuard) || (cha instanceof SiegeNpc))
			{
				final Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
				_clanCrest = clan.getCrestId();
				_clanId = clan.getClanId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
			}
		}
		if (cha.getTemplate().isServerSideName())
		{
			_name = cha.getTemplate().getName();
		}
		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			_title = Config.CHAMP_TITLE;
		}
		else if (cha.getTemplate().isServerSideTitle())
		{
			_title = cha.getTemplate().getTitle();
		}
		else
		{
			_title = cha.getTitle();
		}
		// Custom level titles
		if (cha.isMonster() && (Config.SHOW_NPC_LEVEL || Config.SHOW_NPC_AGGRESSION))
		{
			String t1 = "";
			if (Config.SHOW_NPC_LEVEL)
			{
				t1 += "Lv " + cha.getLevel();
			}
			String t2 = "";
			if (Config.SHOW_NPC_AGGRESSION)
			{
				if (!t1.isEmpty())
				{
					t2 += " ";
				}
				final Monster monster = (Monster) cha;
				if (monster.isAggressive())
				{
					t2 += "[A]"; // Aggressive.
				}
			}
			t1 += t2;
			if ((_title != null) && !_title.isEmpty())
			{
				t1 += " " + _title;
			}
			_title = cha.isChampion() ? Config.CHAMP_TITLE + " " + t1 : t1;
		}
		_x = _creature.getX();
		_y = _creature.getY();
		_z = _creature.getZ();
		_heading = _creature.getHeading();
		_mAtkSpd = _creature.getMAtkSpd();
		_pAtkSpd = _creature.getPAtkSpd();
		_runSpd = _creature.getRunSpeed();
		_walkSpd = _creature.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(Summon cha, Creature attacker)
	{
		_creature = cha;
		_displayId = cha.getTemplate().getDisplayId();
		_isAttackable = cha.isAutoAttackable(attacker); // (cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = cha.isShowSummonAnimation();
		_collisionHeight = _creature.getTemplate().getFCollisionHeight();
		_collisionRadius = _creature.getTemplate().getFCollisionRadius();
		if (cha.getTemplate().isServerSideName() || (cha instanceof Pet) || (cha instanceof Servitor))
		{
			_name = _creature.getName();
			_title = cha.getTitle();
		}
		_x = _creature.getX();
		_y = _creature.getY();
		_z = _creature.getZ();
		_heading = _creature.getHeading();
		_mAtkSpd = _creature.getMAtkSpd();
		_pAtkSpd = _creature.getPAtkSpd();
		_runSpd = _creature.getRunSpeed();
		_walkSpd = _creature.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	public NpcInfo(Player play, Npc cha, Creature attacker)
	{
		if (cha.getFakePlayer() != null)
		{
			attacker.sendPacket(new FakePlayerInfo(cha));
			attacker.broadcastPacket(new StopRotation(cha, cha.getHeading(), 0));
			return;
		}
		_creature = cha;
		_displayId = cha.getTemplate().getDisplayId();
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_isSummoned = false;
		_collisionHeight = cha.getTemplate().getFCollisionHeight();
		_collisionRadius = cha.getTemplate().getFCollisionRadius();
		if (Config.SHOW_NPC_CLAN_CREST && (cha.getCastle() != null) && (cha.getCastle().getOwnerId() != 0) && !cha.isMonster() && !cha.isArtefact() && !(cha instanceof ControlTower))
		{
			if (cha.isInsideZone(ZoneId.TOWN) || cha.isInsideZone(ZoneId.CASTLE) //
				|| (cha instanceof Guard) || (cha instanceof SiegeGuard) || (cha instanceof FortSiegeGuard) || (cha instanceof SiegeNpc))
			{
				final Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
				_clanCrest = clan.getCrestId();
				_clanId = clan.getClanId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
			}
		}
		if (cha.getTemplate().isServerSideName())
		{
			_name = cha.getTemplate().getName();
		}
		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			_title = Config.CHAMP_TITLE;
		}
		else if (cha.getTemplate().isServerSideTitle())
		{
			_title = cha.getTemplate().getTitle();
		}
		else
		{
			_title = cha.getTitle();
		}
		// Custom level titles
		if (cha.isMonster() && (Config.SHOW_NPC_LEVEL || Config.SHOW_NPC_AGGRESSION))
		{
			String t1 = "";
			if (Config.SHOW_NPC_LEVEL)
			{
				t1 += "Lv " + cha.getLevel();
			}
			String t2 = "";
			if (Config.SHOW_NPC_AGGRESSION)
			{
				if (!t1.isEmpty())
				{
					t2 += " ";
				}
				final Monster monster = (Monster) cha;
				if (monster.isAggressive())
				{
					t2 += "[A]"; // Aggressive.
				}
			}
			t1 += t2;
			if ((_title != null) && !_title.isEmpty())
			{
				t1 += " " + _title;
			}
			_title = cha.isChampion() ? Config.CHAMP_TITLE + " " + t1 : t1;
		}
		_x = _creature.getX();
		_y = _creature.getY();
		_z = _creature.getZ();
		_heading = _creature.getHeading();
		_mAtkSpd = _creature.getMAtkSpd();
		_pAtkSpd = _creature.getPAtkSpd();
		_runSpd = _creature.getRunSpeed();
		_walkSpd = _creature.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	@Override
	public void write()
	{
		if ((_creature == null) || ((_creature instanceof Summon) && (((Summon) _creature).getOwner() != null) && ((Summon) _creature).getOwner().getAppearance().isInvisible()))
		{
			return;
		}
		
		ServerPackets.NPC_INFO.writeId(this);
		writeInt(_creature.getObjectId());
		writeInt(_displayId + 1000000); // npctype id
		writeInt(_isAttackable);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z);
		writeInt(_heading);
		writeInt(0);
		writeInt(_mAtkSpd);
		writeInt(_pAtkSpd);
		writeInt(_runSpd);
		writeInt(_walkSpd);
		writeInt(_swimRunSpd/* 0x32 */); // swimspeed
		writeInt(_swimWalkSpd/* 0x32 */); // swimspeed
		writeInt(_flRunSpd);
		writeInt(_flWalkSpd);
		writeInt(_flyRunSpd);
		writeInt(_flyWalkSpd);
		writeDouble(1.1/* _activeChar.getProperMultiplier() */);
		// writeF(1/*_activeChar.getAttackSpeedMultiplier()*/);
		writeDouble(_pAtkSpd / 277.478340719);
		writeDouble(_collisionRadius);
		writeDouble(_collisionHeight);
		writeInt(_rhand); // right hand weapon
		writeInt(0);
		writeInt(_lhand); // left hand weapon
		writeByte(1); // name above char 1=true ... ??
		writeByte(_creature.isRunning());
		writeByte(_creature.isInCombat());
		writeByte(_creature.isAlikeDead());
		writeByte(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeString(_name);
		writeString(_title);
		if (_creature instanceof Summon)
		{
			writeInt(1); // Title color 0=client default
			writeInt(((Summon) _creature).getPvpFlag());
			writeInt(((Summon) _creature).getKarma());
		}
		else
		{
			writeInt(0);
			writeInt(0);
			writeInt(0);
		}
		writeInt(_creature.getAbnormalEffect()); // C2
		writeInt(_clanId); // C2
		writeInt(_clanCrest); // C2
		writeInt(_allyId); // C2
		writeInt(_allyCrest); // C2
		writeByte(0); // C2
		if (Config.CHAMPION_ENABLE)
		{
			writeByte(_creature.isChampion() ? Config.CHAMPION_AURA : 0);
		}
		else
		{
			writeByte(0); // C3 team circle 1-blue, 2-red
		}
		writeDouble(_collisionRadius);
		writeDouble(_collisionHeight);
		writeInt(0); // C4
		writeInt(0); // C6
	}
}
