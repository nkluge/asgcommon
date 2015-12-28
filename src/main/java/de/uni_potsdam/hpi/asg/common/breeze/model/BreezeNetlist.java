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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.AbstractBreezeElement;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezeComponentElement;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezeElementFactory;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezeImport;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezePartElement;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezeparser.BreezeParser;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezeparser.ParseException;
import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;

public class BreezeNetlist extends AbstractBreezeNetlist {
	private static final Logger logger = LogManager.getLogger();

	private BreezeNetlist(String name, BreezeProject project) {
		super(name, project);
	}

	public static boolean isBreezeFile(File file) {
		try {
			FileReader filereader = new FileReader(file.getCanonicalFile());
			BreezeParser parser = new BreezeParser(filereader);
			parser.ParseBreezeNet();
			return true;
		} catch(ParseException e) {
			return false;
		} catch(Exception e) {
			logger.error("Something went wrong with file " + file.getAbsolutePath());
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static boolean create(File file, boolean skipUndefinedComponents, boolean skipSubComponents, BreezeProject project) {
		String newfilename = "orig_" + file.getName();
		if(!FileHelper.getInstance().copyfile(file, newfilename)) {
			return false;
		}
		try {
			FileReader filereader = new FileReader(WorkingdirGenerator.getInstance().getWorkingdir() + newfilename);
			BreezeParser parser = new BreezeParser(filereader);
			Object p = parser.ParseBreezeNet();
			if(p instanceof LinkedList<?>) {
				for(Object item : (LinkedList<Object>)p) {
					AbstractBreezeElement ae = BreezeElementFactory.baseElement(item);
					if(ae instanceof BreezeImport) {
						if(!skipSubComponents) {
							BreezeImport imp = (BreezeImport)ae;
							String str = imp.getImportString().replace("\"", "");
							if(str.startsWith("balsa.")) {
								continue;
							}
							File newbreeze = new File(file.getParentFile(), str + FileHelper.getFileEx(Filetype.breeze));
							if(newbreeze.exists()) {
								if(!BreezeNetlist.create(newbreeze, skipUndefinedComponents, skipSubComponents, project)) {
									logger.error("Could not create Breeze netlist for " + newbreeze);
									return false;
								}
							} else {
								logger.warn("Importfile " + str + " not found");
							}
						}
					} else if(ae instanceof BreezePartElement) {
						BreezeComponentElement.resetComponent_counter();
						BreezePartElement part = (BreezePartElement)ae;
						BreezeNetlist netlist = new BreezeNetlist(part.getName(), project);
						if(!netlist.parseBreeze(part, skipUndefinedComponents)) {
							return false;
						}
						if(!netlist.connectPorts(skipUndefinedComponents)) {
							return false;
						}
						project.getNetlists().put(netlist.getName(), netlist);
						logger.info(netlist.getName() + ": found " + netlist.getAllHSInstances().size() + " HS-component and " + netlist.subBreezeInst.size() + " Sub-component instances");
					}
				}
			}
			return true;
		} catch(ParseException e) {
			logger.error("Error while parsing " + newfilename + " : " + e.getLocalizedMessage());
		} catch(FileNotFoundException e1) {
			logger.error("File " + newfilename + " not found");
		}
		return false;
	}
}
