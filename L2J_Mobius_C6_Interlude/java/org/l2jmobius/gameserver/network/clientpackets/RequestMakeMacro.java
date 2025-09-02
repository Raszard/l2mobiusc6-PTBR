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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.ReadablePacket;
import org.l2jmobius.gameserver.model.Macro;
import org.l2jmobius.gameserver.model.Macro.MacroCmd;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class RequestMakeMacro implements ClientPacket
{
	private Macro _macro;
	private int _commandsLength = 0;
	private static final int MAX_MACRO_LENGTH = 12;
	
	/**
	 * packet type id 0xc1 sample c1 d // id S // macro name S // unknown desc S // unknown acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdSSScc (ccdcS)
	 */
	@Override
	public void read(ReadablePacket packet)
	{
		final int id = packet.readInt();
		final String name = packet.readString();
		final String desc = packet.readString();
		final String acronym = packet.readString();
		final int icon = packet.readByte();
		int count = packet.readByte();
		if (count < 0)
		{
			return;
		}
		
		if (count > MAX_MACRO_LENGTH)
		{
			count = MAX_MACRO_LENGTH;
		}
		
		final MacroCmd[] commands = new MacroCmd[count];
		for (int i = 0; i < count; i++)
		{
			final int entry = packet.readByte();
			final int type = packet.readByte(); // 1 = skill, 3 = action, 4 = shortcut
			final int d1 = packet.readInt(); // skill or page number for shortcuts
			final int d2 = packet.readByte();
			final String command = packet.readString();
			_commandsLength += command.length() + 1;
			commands[i] = new MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new Macro(id, icon, name, desc, acronym, commands);
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Macro exploit fix
		if (!client.getFloodProtectors().canUseMacro())
		{
			return;
		}
		
		if (_commandsLength > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
			return;
		}
		
		if (player.getMacroses().getAllMacroses().size() > 24)
		{
			// You may create up to 24 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS);
			return;
		}
		
		if (_macro.name.length() == 0)
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}
		
		if (_macro.descr.length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}
		
		// Security Check
		for (MacroCmd command : _macro.commands)
		{
			if (!checkSecurityOnCommand(command))
			{
				// Invalid macro. Refer to the Help file for instructions.
				player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
				player.sendMessage("SecurityCheck: not more then 2x ',' or 2x ';' in the same command");
				return;
			}
		}
		
		player.registerMacro(_macro);
	}
	
	private boolean checkSecurityOnCommand(MacroCmd cmd)
	{
		// not more then 2x ;
		if ((cmd.cmd != null) && (cmd.cmd.split(";").length > 2))
		{
			return false;
		}
		
		// not more then 2x ,
		if ((cmd.cmd != null) && (cmd.cmd.split(",").length > 2))
		{
			return false;
		}
		
		return true;
	}
}
