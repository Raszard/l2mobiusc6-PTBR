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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * 01 // Packet Identifier<br>
 * c6 37 50 40 // ObjectId<br>
 * <br>
 * 01 00 // Number of Attribute Trame of the Packet<br>
 * <br>
 * c6 37 50 40 // Attribute Identifier : 01-Level, 02-Experience, 03-STR, 04-DEX, 05-CON, 06-INT, 07-WIT, 08-MEN, 09-Current HP, 0a, Max HP...<br>
 * cd 09 00 00 // Attribute Value<br>
 * format d d(dd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class StatusUpdate extends ServerPacket
{
	public static final int LEVEL = 0x01;
	public static final int EXP = 0x02;
	public static final int STR = 0x03;
	public static final int DEX = 0x04;
	public static final int CON = 0x05;
	public static final int INT = 0x06;
	public static final int WIT = 0x07;
	public static final int MEN = 0x08;
	public static final int CUR_HP = 0x09;
	public static final int MAX_HP = 0x0a;
	public static final int CUR_MP = 0x0b;
	public static final int MAX_MP = 0x0c;
	public static final int SP = 0x0d;
	public static final int CUR_LOAD = 0x0e;
	public static final int MAX_LOAD = 0x0f;
	public static final int P_ATK = 0x11;
	public static final int ATK_SPD = 0x12;
	public static final int P_DEF = 0x13;
	public static final int EVASION = 0x14;
	public static final int ACCURACY = 0x15;
	public static final int CRITICAL = 0x16;
	public static final int M_ATK = 0x17;
	public static final int CAST_SPD = 0x18;
	public static final int M_DEF = 0x19;
	public static final int PVP_FLAG = 0x1a;
	public static final int KARMA = 0x1b;
	public static final int CUR_CP = 0x21;
	public static final int MAX_CP = 0x22;
	
	private Player _actor;
	private List<Attribute> _attributes;
	public int _objectId;
	
	class Attribute
	{
		// id values 09 - current health 0a - max health 0b - current mana 0c - max mana
		public int id;
		public int value;
		
		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public StatusUpdate(Player actor)
	{
		_actor = actor;
	}
	
	public StatusUpdate(int objectId)
	{
		_attributes = new ArrayList<>();
		_objectId = objectId;
	}
	
	public void addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
	}
	
	@Override
	public void write()
	{
		ServerPackets.STATUS_UPDATE.writeId(this);
		if (_actor != null)
		{
			writeInt(_actor.getObjectId());
			writeInt(28); // all the attributes
			writeInt(LEVEL);
			writeInt(_actor.getLevel());
			writeInt(EXP);
			writeInt((int) _actor.getExp());
			writeInt(STR);
			writeInt(_actor.getSTR());
			writeInt(DEX);
			writeInt(_actor.getDEX());
			writeInt(CON);
			writeInt(_actor.getCON());
			writeInt(INT);
			writeInt(_actor.getINT());
			writeInt(WIT);
			writeInt(_actor.getWIT());
			writeInt(MEN);
			writeInt(_actor.getMEN());
			writeInt(CUR_HP);
			writeInt((int) _actor.getCurrentHp());
			writeInt(MAX_HP);
			writeInt(_actor.getMaxHp());
			writeInt(CUR_MP);
			writeInt((int) _actor.getCurrentMp());
			writeInt(MAX_MP);
			writeInt(_actor.getMaxMp());
			writeInt(SP);
			writeInt(_actor.getSp());
			writeInt(CUR_LOAD);
			writeInt(_actor.getCurrentLoad());
			writeInt(MAX_LOAD);
			writeInt(_actor.getMaxLoad());
			writeInt(P_ATK);
			writeInt(_actor.getPAtk(null));
			writeInt(ATK_SPD);
			writeInt(_actor.getPAtkSpd());
			writeInt(P_DEF);
			writeInt(_actor.getPDef(null));
			writeInt(EVASION);
			writeInt(_actor.getEvasionRate(null));
			writeInt(ACCURACY);
			writeInt(_actor.getAccuracy());
			writeInt(CRITICAL);
			writeInt(_actor.getCriticalHit(null, null));
			writeInt(M_ATK);
			writeInt(_actor.getMAtk(null, null));
			writeInt(CAST_SPD);
			writeInt(_actor.getMAtkSpd());
			writeInt(M_DEF);
			writeInt(_actor.getMDef(null, null));
			writeInt(PVP_FLAG);
			writeInt(_actor.getPvpFlag());
			writeInt(KARMA);
			writeInt(_actor.getKarma());
			writeInt(CUR_CP);
			writeInt((int) _actor.getCurrentCp());
			writeInt(MAX_CP);
			writeInt(_actor.getMaxCp());
		}
		else
		{
			writeInt(_objectId);
			writeInt(_attributes.size());
			for (int i = 0; i < _attributes.size(); i++)
			{
				final Attribute temp = _attributes.get(i);
				writeInt(temp.id);
				writeInt(temp.value);
			}
		}
	}
}
