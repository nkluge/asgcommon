package de.uni_potsdam.hpi.asg.common.io;

/*
 * Copyright (C) 2012 - 2016 Norman Kluge
 * 
 * This file is part of ASGcommon.
 * 
 * ASGcommon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGcommon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGcommon.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.ProcessReturn.Status;

public abstract class Invoker {
	private final static Logger logger = LogManager.getLogger();
	
	protected static Invoker instance;
	
	protected String workingdir;
	
	protected Invoker(){}
	
	public static Invoker getInstance() {
		if(instance == null) {
			logger.warn("Invoker not set");
		}
		return instance;
	}
	public void setWorkingdir(String workingdir) {
		this.workingdir = workingdir;
	}
	
	protected ProcessReturn invoke(String[] cmd, String[] params, File folder) {
		return invoke(cmd, Arrays.asList(params), folder, 0);
	}	
	protected ProcessReturn invoke(String[] cmd, String[] params) {
		return invoke(cmd, Arrays.asList(params), new File(workingdir), 0);
	}
	protected ProcessReturn invoke(String[] cmd, String[] params, int timeout) {
		return invoke(cmd, Arrays.asList(params), new File(workingdir), timeout);
	}
	protected ProcessReturn invoke(String[] cmd, List<String> params, int timeout) {
		return invoke(cmd, params, new File(workingdir), timeout);
	}
	protected ProcessReturn invoke(String[] cmd, List<String> params) {
		return invoke(cmd, params, new File(workingdir), 0);
	}
	private ProcessReturn invoke(String[] cmd, List<String> params, File folder, int timeout) {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(cmd));
		command.addAll(params);
		ProcessReturn retVal = new ProcessReturn(Arrays.asList(cmd), params);
		Process process = null;
		try {
			logger.debug("Exec command: " + command.toString());
			//System.out.println(timeout + ": " + command.toString());
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(folder);
			builder.environment(); // bugfix setting env in test-mode (why this works? i dont know..)
			process = builder.start();
			
			Thread timeoutThread = null;
			if(timeout > 0) {
				timeoutThread = new Thread(new Timeout(Thread.currentThread(), timeout));
				timeoutThread.setName("Timout for " + command.toString());
				timeoutThread.start();
			}
			IOStreamReader ioreader = new IOStreamReader(process);
			Thread streamThread = new Thread(ioreader);
			streamThread.setName("StreamReader for " + command.toString());
			streamThread.start();
			process.waitFor();
			streamThread.join();
			if(timeoutThread != null) {
				timeoutThread.interrupt();
			}			
			String out = ioreader.getResult();
			//System.out.println(out);
			if(out == null) {
				//System.out.println("out = null");
				retVal.setStatus(Status.noio);
			}
			retVal.setCode(process.exitValue());
			retVal.setStream(out);
			retVal.setStatus(Status.ok);
		} catch(InterruptedException e) {
			process.destroy();
			retVal.setTimeout(timeout);
			retVal.setStatus(Status.timeout);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			retVal.setStatus(Status.ioexception);
		}		
		return retVal;
	}
	
	protected boolean errorHandling(ProcessReturn ret) {
		return errorHandling(ret, 0);
	}
	
	//todo: io as debug!
	private boolean errorHandling(ProcessReturn ret, int okcode) {
		if(ret != null) {
			switch(ret.getStatus()) {
				case ok:
					if(ret.getCode() != okcode) {
						logger.error("An error was reported while executing " + ret.getCommand());
						logger.debug("Params: " + ret.getParams());
						logger.debug("Exit code: " + ret.getCode() + " Output:");
						logger.debug("##########");
						logger.debug(ret.getStream());
						logger.debug("##########");
						return false;
					}
					break;
				case timeout:
					logger.error("Timeout while executing " + ret.getCommand());
					logger.debug("Params: " + ret.getParams());
					logger.debug("Timout after " + ret.getTimeout() / 1000 + "s");
					return false;				
				case ioexception:
				case noio:
					logger.error("I/O error while executing " + ret.getCommand());
					logger.debug("Params: " + ret.getParams());
					return false;
			}
		} else {
			logger.error("Something went really wrong while executing something. I don't even know what the command line was");
			return false;
		}
		return true;
	}
	
	protected String[] convertCmd(String cmd) {
		if(cmd == null) {
			return null;
		}
		
		String basedir = System.getProperty("basedir");
		if(SystemUtils.IS_OS_WINDOWS) {
			basedir = basedir.replaceAll("\\\\", "/");
		}
		
		cmd = cmd.replaceAll("\\$BASEDIR", basedir);
		return cmd.split(" ");
	}
}
