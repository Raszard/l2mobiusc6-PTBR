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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * A packet used to draw points and lines on client.<br/>
 * <b>Note:</b> Names in points and lines are bugged they will appear even when not looking at them.
 * @author NosBit
 */
public class ExServerPrimitive extends ServerPacket
{
	private final String _name;
	private final int _x;
	private final int _y;
	private final int _z;
	private final List<Point> _points = new ArrayList<>();
	private final List<Line> _lines = new ArrayList<>();
	
	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 * @param x the x coordinate usually middle of drawing area
	 * @param y the y coordinate usually middle of drawing area
	 * @param z the z coordinate usually middle of drawing area
	 */
	public ExServerPrimitive(String name, int x, int y, int z)
	{
		_name = name;
		_x = x;
		_y = y;
		_z = z;
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z)
	{
		_points.add(new Point(name, color, isNameColored, x, y, z));
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(int color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z)
	{
		addPoint(name, color.getRGB(), isNameColored, x, y, z);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(Color color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		_lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(int color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}
	
	public void addCircle(final Color color, final int radius, final int circlePoint, final int z)
	{
		for (int i = 0; i < circlePoint; ++i)
		{
			final double angle1 = (i * 2 * Math.PI) / circlePoint;
			final double angle2 = ((i + 1) * 2 * Math.PI) / circlePoint;
			
			final int x = (int) (_x + (radius * Math.cos(angle1)));
			final int y = (int) (_y + (radius * Math.sin(angle1)));
			
			final int x2 = (int) (_x + (radius * Math.cos(angle2)));
			final int y2 = (int) (_y + (radius * Math.sin(angle2)));
			
			addLine(color, x, y, _z + z, x2, y2, _z + z);
		}
	}
	
	public void addCircle(final int color, final int radius, final int circlePoint, final int z)
	{
		for (int i = 0; i < circlePoint; ++i)
		{
			final double angle1 = (i * 2 * Math.PI) / circlePoint;
			final double angle2 = ((i + 1) * 2 * Math.PI) / circlePoint;
			
			final int x = (int) (_x + (radius * Math.cos(angle1)));
			final int y = (int) (_y + (radius * Math.sin(angle1)));
			
			final int x2 = (int) (_x + (radius * Math.cos(angle2)));
			final int y2 = (int) (_y + (radius * Math.sin(angle2)));
			
			addLine(color, x, y, _z + z, x2, y2, _z + z);
		}
	}
	
	@Override
	public void write()
	{
		ServerPackets.EX_SERVER_PRIMITIVE.writeId(this);
		writeString(_name);
		writeInt(_x);
		writeInt(_y);
		writeInt(_z - 1000);
		writeInt(65535); // has to do something with display range and angle
		writeInt(65535); // has to do something with display range and angle
		writeInt(_points.size() + _lines.size());
		for (Point point : _points)
		{
			writeByte(1); // Its the type in this case Point
			writeString(point.getName());
			final int color = point.getColor();
			writeInt((color >> 16) & 0xFF); // R
			writeInt((color >> 8) & 0xFF); // G
			writeInt(color & 0xFF); // B
			writeInt(point.isNameColored());
			writeInt(point.getX());
			writeInt(point.getY());
			writeInt(point.getZ());
		}
		for (Line line : _lines)
		{
			writeByte(2); // Its the type in this case Line
			writeString(line.getName());
			final int color = line.getColor();
			writeInt((color >> 16) & 0xFF); // R
			writeInt((color >> 8) & 0xFF); // G
			writeInt(color & 0xFF); // B
			writeInt(line.isNameColored());
			writeInt(line.getX());
			writeInt(line.getY());
			writeInt(line.getZ() + 20);
			writeInt(line.getX2());
			writeInt(line.getY2());
			writeInt(line.getZ2() + 20);
		}
	}
	
	private static class Point
	{
		private final String _name;
		private final int _color;
		private final boolean _isNameColored;
		private final int _x;
		private final int _y;
		private final int _z;
		
		public Point(String name, int color, boolean isNameColored, int x, int y, int z)
		{
			_name = name;
			_color = color;
			_isNameColored = isNameColored;
			_x = x;
			_y = y;
			_z = z;
		}
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * @return the color
		 */
		public int getColor()
		{
			return _color;
		}
		
		/**
		 * @return the isNameColored
		 */
		public boolean isNameColored()
		{
			return _isNameColored;
		}
		
		/**
		 * @return the x
		 */
		public int getX()
		{
			return _x;
		}
		
		/**
		 * @return the y
		 */
		public int getY()
		{
			return _y;
		}
		
		/**
		 * @return the z
		 */
		public int getZ()
		{
			return _z;
		}
	}
	
	private static class Line extends Point
	{
		private final int _x2;
		private final int _y2;
		private final int _z2;
		
		public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
		{
			super(name, color, isNameColored, x, y, z);
			_x2 = x2;
			_y2 = y2;
			_z2 = z2;
		}
		
		/**
		 * @return the x2
		 */
		public int getX2()
		{
			return _x2;
		}
		
		/**
		 * @return the y2
		 */
		public int getY2()
		{
			return _y2;
		}
		
		/**
		 * @return the z2
		 */
		public int getZ2()
		{
			return _z2;
		}
	}
}