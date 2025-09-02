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
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;

public class QuestList
{
	private final int _id;
	
	private final String _questName;
	
	private final int _levelMin;
	
	private final int _levelMax;
	
	private final int _questType;
	
	private final String _entityName;
	
	private final int _contactNpc;
	
	private final String _restrictions;
	
	private final String _description;
	
	private final ArrayList<Integer> _classes;
	
	/**
	 * Constructor of RecipeList (create a new Recipe).
	 * @param id
	 * @param name
	 * @param min
	 * @param max
	 * @param type
	 * @param entity
	 * @param contact
	 * @param restrictions
	 * @param description
	 * @param classes
	 */
	public QuestList(int id, String name, int min, int max, int type, String entity, int contact, String restrictions, String description, ArrayList<Integer> classes)
	{
		_id = id;
		_questName = name;
		_levelMin = min;
		_levelMax = max;
		_questType = type;
		_entityName = entity;
		_contactNpc = contact;
		_restrictions = restrictions;
		_description = description;
		_classes = classes;
	}
	
	/**
	 * @return the _id
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the _questName
	 */
	public String getQuestName()
	{
		return _questName;
	}
	
	/**
	 * @return the _levelMin
	 */
	public int getLevelMin()
	{
		return _levelMin;
	}
	
	/**
	 * @return the _levelMax
	 */
	public int getLevelMax()
	{
		return _levelMax;
	}
	
	/**
	 * @return the _questType
	 */
	public int getQuestType()
	{
		return _questType;
	}
	
	/**
	 * @return the _entityName
	 */
	public String getEntityName()
	{
		return _entityName;
	}
	
	/**
	 * @return the _contactNpc
	 */
	public int getContactNpc()
	{
		return _contactNpc;
	}
	
	/**
	 * @return the _restrictions
	 */
	public String getRestrictions()
	{
		return _restrictions;
	}
	
	/**
	 * @return the _description
	 */
	public String getDescription()
	{
		return _description;
	}
	
	/**
	 * @return the _classes
	 */
	public ArrayList<Integer> getClasses()
	{
		return _classes;
	}
	
}
