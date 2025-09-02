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

import org.l2jmobius.gameserver.model.Macro;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * packet type id 0xe7 sample e7 d // unknown change of Macro edit,add,delete c // unknown c //count of Macros c // unknown d // id S // macro name S // desc S // acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdhcdSSScc (ccdcS)
 */
public class SendMacroList extends ServerPacket
{
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	
	public SendMacroList(int rev, int count, Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}
	
	@Override
	public void write()
	{
		ServerPackets.SEND_MACRO_LIST.writeId(this);
		writeInt(_rev); // macro change revision (changes after each macro edition)
		writeByte(0); // unknown
		writeByte(_count); // count of Macros
		writeByte(_macro != null); // unknown
		if (_macro != null)
		{
			writeInt(_macro.id); // Macro ID
			writeString(_macro.name); // Macro Name
			writeString(_macro.descr); // Desc
			writeString(_macro.acronym); // acronym
			writeByte(_macro.icon); // icon
			writeByte(_macro.commands.length); // count
			for (int i = 0; i < _macro.commands.length; i++)
			{
				final Macro.MacroCmd cmd = _macro.commands[i];
				writeByte(i + 1); // i of count
				writeByte(cmd.type); // type 1 = skill, 3 = action, 4 = shortcut
				writeInt(cmd.d1); // skill id
				writeByte(cmd.d2); // shortcut id
				writeString(cmd.cmd); // command name
			}
		}
	}
}
