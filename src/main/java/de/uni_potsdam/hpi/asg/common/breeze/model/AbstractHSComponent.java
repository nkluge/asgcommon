package de.uni_potsdam.hpi.asg.common.breeze.model;

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

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Component;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Parameter;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezeComponentElement;

/**
 * Represents a handshake component
 * (e.g. BrzVariable or BrzCallMux)
 * 
 */
public abstract class AbstractHSComponent {

	private Map<String, HSComponentType>						typeMap;
	private Map<AbstractBreezeNetlist, List<HSComponentInst>>	instances;
	protected Component											comp;

	public AbstractHSComponent(Component comp) {
		typeMap = new HashMap<String, HSComponentType>();
		instances = new HashMap<AbstractBreezeNetlist, List<HSComponentInst>>();
		this.comp = comp;
	}

	public abstract boolean createInstance(BreezeComponentElement be, AbstractBreezeNetlist netlist);

	protected HSComponentType internalCreateInstanceType(BreezeComponentElement be) {
		HSComponentType type = null;
		String typeidstr = getTypeID(be);
		if(typeMap.containsKey(typeidstr)) {
			type = (HSComponentType)typeMap.get(typeidstr);
		}
		if(type == null) {
			type = HSComponentType.create(comp.getParameters(), be.parameters, this);
			if(type == null) {
				return null;
			}
			typeMap.put(typeidstr, type);
		}
		return type;
	}

	protected HSComponentInst internalCreateInstanceInst(BreezeComponentElement be, AbstractBreezeNetlist netlist, HSComponentType type) {
		SourceInformation sinfo = null;
		if(be.get(0) instanceof LinkedList<?>) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> data = (LinkedList<Object>)be.get(0);
			if(data.get(1) instanceof Integer && data.get(2) instanceof Integer && data.get(3) instanceof String) {
				sinfo = new SourceInformation((Integer)data.get(1), (Integer)data.get(2), new File(((String)data.get(3)).replace("\"", "")));
			}
		}

		HSComponentInst inst = HSComponentInst.create(be.getID(), be.channels, type, comp.getChannels(), netlist, this, sinfo);
		if(inst == null) {
			return null;
		}

		if(!instances.containsKey(netlist)) {
			instances.put(netlist, new ArrayList<HSComponentInst>());
		}

		instances.get(netlist).add(inst);
		return inst;
	}

	private String getTypeID(BreezeComponentElement be) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		if(comp.getParameters() != null) {
			for(Object o : be.parameters) {
				Parameter param = comp.getParameters().getParameter(i);
				if(param != null) {
					str.append(o.toString());
				}
				i++;
			}
		}
		return str.toString();
	}

	public int getNumChans() {
		return comp.getChannels().getSize();
	}

	public List<HSComponentType> getTypes() {
		return new ArrayList<HSComponentType>(typeMap.values());
	}

	public List<HSComponentInst> getInstances(AbstractBreezeNetlist netlist) {
		if(!instances.containsKey(netlist)) {
			instances.put(netlist, new ArrayList<HSComponentInst>());
		}
		return instances.get(netlist);
	}

	public String getSymbol() {
		return comp.getSymbol();
	}

	public String getBrzString() {
		return comp.getBreezename();
	}

	public Component getComp() {
		return comp;
	}
}
