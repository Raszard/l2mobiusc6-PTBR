package org.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IndicationRewardManager
{
	private static final Map<Integer, Map<Integer, Integer>> REWARDS = new HashMap<>();
	
	public static void load()
	{
		try
		{
			File file = new File("data/IndicationRewards.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			NodeList rewards = doc.getElementsByTagName("reward");
			
			for (int i = 0; i < rewards.getLength(); i++)
			{
				Element reward = (Element) rewards.item(i);
				int level = Integer.parseInt(reward.getAttribute("level"));
				Map<Integer, Integer> items = new HashMap<>();
				
				NodeList itemList = reward.getElementsByTagName("item");
				for (int j = 0; j < itemList.getLength(); j++)
				{
					Element item = (Element) itemList.item(j);
					int id = Integer.parseInt(item.getAttribute("id"));
					int count = Integer.parseInt(item.getAttribute("count"));
					items.put(id, count);
				}
				
				REWARDS.put(level, items);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Map<Integer, Integer> getRewards(int level)
	{
		return REWARDS.get(level);
	}
}