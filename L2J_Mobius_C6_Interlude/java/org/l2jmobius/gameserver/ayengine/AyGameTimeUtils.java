package org.l2jmobius.gameserver.ayengine;

import java.util.Calendar;

public class AyGameTimeUtils
{
	public static boolean isWeekend()
	{
		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		return ((dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY) || (dayOfWeek == Calendar.FRIDAY));
	}
}