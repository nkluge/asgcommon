package de.uni_potsdam.hpi.asg.common.breeze.model;

/*
 * Copyright (C) 2015 Norman Kluge
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

public class SourceInformation {

	private File file;
	private int line;
	private int column;
	
	public SourceInformation(int line, int column, File file) {
		this.file = file;
		this.line = line;
		this.column = column;
	}
	
	public int getColumn() {
		return column;
	}
	public File getFile() {
		return file;
	}
	public int getLine() {
		return line;
	}
}
