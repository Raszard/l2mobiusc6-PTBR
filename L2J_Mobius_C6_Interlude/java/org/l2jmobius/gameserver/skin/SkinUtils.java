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
package org.l2jmobius.gameserver.skin;

import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

/**
 * @author Administrator
 */
public class SkinUtils
{
	private static final int[] typesArray =
	{
		Inventory.PAPERDOLL_FACE,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET
	};
	
	private static final String[] subArray =
	{
		"%face%",
		"%head%",
		"%hair%",
		"%chest%",
		"%gloves%",
		"%legs%",
		"%feet%"
	};
	
	private static final String[] iconArray =
	{
		"L2UI_CUSTOM.InventoryEar",
		"L2UI_CUSTOM.InventoryHead",
		"L2UI_CUSTOM.InventoryEar",
		"L2UI_CUSTOM.InventoryChest",
		"L2UI_CUSTOM.InventoryGloves",
		"L2UI_CUSTOM.InventoryLegs",
		"L2UI_CUSTOM.InventoryFeet"
	};
	
	private static final String[] bypassArray =
	{
		"itemlist 1 - 1",
		"itemlist 1 - 2",
		"itemlist 1 - 0",
		"itemlist 1 - 4",
		"itemlist 1 - 3",
		"itemlist 1 - 5",
		"itemlist 1 - 6"
	};
	
	public static int getSkinPositionNumber(int slot)
	{
		switch (slot)
		{
			case 0x01:
			{
				return 0;
			}
			case 0x04:
			{
				return 1;
			}
			case 0x02:
			{
				return 2;
			}
			case 0x08:
			{
				return 3;
			}
			case 0x20:
			{
				return 4;
			}
			case 0x10:
			{
				return 5;
			}
			case 0x40:
			{
				return 6;
			}
			case 0x80:
			{
				return 7;
			}
			case 0x0100:
			{
				return 8;
			}
			case 0x0200:
			{
				return 9;
			}
			case 0x0400:
			{
				return 10;
			}
			case 0x0800:
			{
				return 11;
			}
			case 0x1000:
			{
				return 12;
			}
			case 0x2000:
			{
				return 13;
			}
			case 0x4000:
			{
				return 14;
			}
			case 0x040000:
			{
				return 15;
			}
			case 0x010000:
			{
				return 16;
			}
			case 0x080000:
			{
				return 17;
			}
		}
		return slot;
	}
	
	public static int getBodyPart(int bodyPart)
	{
		switch (bodyPart)
		{
			case 0:
			{
				
				return ItemTemplate.SLOT_HAIR;
			}
			case 1:
			{
				
				return ItemTemplate.SLOT_FACE;
			}
			case 2:
			{
				
				return ItemTemplate.SLOT_HEAD;
			}
			case 3:
			{
				
				return ItemTemplate.SLOT_GLOVES;
			}
			case 4:
			{
				
				return ItemTemplate.SLOT_CHEST;
			}
			case 5:
			{
				
				return ItemTemplate.SLOT_LEGS;
			}
			case 6:
			{
				
				return ItemTemplate.SLOT_FEET;
			}
			case 7:
			{
				
				return ItemTemplate.SLOT_BACK;
			}
			case 8:
			{
				
				return ItemTemplate.SLOT_FULL_ARMOR;
			}
			default:
			{
				return ItemTemplate.SLOT_NONE;
			}
		}
	}
	
	public static boolean allNull(Object[] array)
	{
		for (Object elemento : array)
		{
			if (elemento != null)
			{
				return false;
			}
		}
		return true;
	}
	
	public static int[] getTypesArray()
	{
		return typesArray;
	}
	
	public static String[] getSubArray()
	{
		return subArray;
	}
	
	public static String[] getIconArray()
	{
		return iconArray;
	}
	
	public static String[] getBypassArray()
	{
		return bypassArray;
	}
}
