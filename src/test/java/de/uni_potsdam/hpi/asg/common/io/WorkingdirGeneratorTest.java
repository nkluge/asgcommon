package de.uni_potsdam.hpi.asg.common.io;

/*
 * Copyright (C) 2016 Norman Kluge
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

import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

public class WorkingdirGeneratorTest {

    public class TestThread extends Thread {
        public String         workingdir;
        private CyclicBarrier gate;
        private boolean       delete;

        public TestThread(CyclicBarrier gate, boolean delete) {
            this.gate = gate;
            this.delete = delete;
        }

        public void run() {
            try {
                gate.await();
                WorkingdirGenerator gen = new WorkingdirGenerator();
                gen.create(null, null, "test", null);
                workingdir = gen.getWorkingdir();
                File f = new File(workingdir);
                if(!f.exists()) {
                    fail("Working dir does not exist");
                }
                if(delete) {
                    gen.delete();
                }
            } catch(InterruptedException e) {
                fail(e.getLocalizedMessage());
            } catch(BrokenBarrierException e) {
                fail(e.getLocalizedMessage());
            }
        }
    }

    @Test
    public void testCreateAndDelete() {
        internalSetup(true);
    }

    @Test
    public void testCreate() {
        TestThread threads[] = internalSetup(false);
        for(TestThread t : threads) {
            for(TestThread t2 : threads) {
                if((t != t2) && t.workingdir.equals(t2.workingdir)) {
                    fail("Two working dirs are equal");
                }
            }
        }
    }

    private TestThread[] internalSetup(boolean delete) {
        final int numOfThreads = 1000;
        final CyclicBarrier gate = new CyclicBarrier(numOfThreads);

        TestThread threads[] = new TestThread[numOfThreads];

        for(int i = 0; i < numOfThreads; i++) {
            TestThread t = new TestThread(gate, delete);
            threads[i] = t;
        }
        for(Thread t : threads) {
            t.start();
        }
        for(Thread t : threads) {
            try {
                t.join();
            } catch(InterruptedException e) {
                fail(e.getLocalizedMessage());
            }
        }

        return threads;
    }
}
