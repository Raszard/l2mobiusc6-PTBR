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
package org.l2jmobius.gameserver.ayengine.search;

/**
 * @author Administrator
 */
public class MobDesc
{
	private final String _icon;
	private final String _nameType;
	private final String _desc;
	
	public MobDesc(String icon, String name, String desc)
	{
		this._icon = icon;
		this._nameType = name;
		this._desc = desc;
	}
	
	public String getIcon()
	{
		return _icon;
	}
	
	public String getNameType()
	{
		return _nameType;
	}
	
	public String getDesc()
	{
		return _desc;
	}
}
