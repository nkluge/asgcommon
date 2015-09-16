package de.uni_potsdam.hpi.asg.common.breeze.model.xml;

/*
 * Copyright (C) 2012 - 2015 Norman Kluge
 * 
 * This file is part of ASGCommon.
 * 
 * ASGCommon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGCommon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGCommon.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Channels {
	@XmlElement(name="channel")
	private List<Channel> channels;
	
	public Channel getChannel(int id) {
		for(Channel chan : channels) {
			if(chan.getId() == id) {
				return chan;
			}
		}
		return null;
	}
	
	public int getSize() {
		return channels.size();
	}
}