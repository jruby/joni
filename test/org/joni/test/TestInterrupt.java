/*
 * The MIT License
 *
 * Copyright 2013 enebo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.joni.test;

import java.util.Timer;
import java.util.TimerTask;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Syntax;

/**
 * These are fairly long-running tests but we want a large time slice to reduce misfires
 * on slow ci boxes.
 */
public class TestInterrupt extends Test {
    interface InterruptibleRunnable {
        void run() throws InterruptedException;
    }

    @Override
    public int option() {
        return Option.DEFAULT;
    }
    @Override
    public Encoding encoding() {
        return ASCIIEncoding.INSTANCE;
    }
    @Override
    public String testEncoding() {
        return "iso-8859-2";
    }
    @Override
    public Syntax syntax() {
        return Syntax.DEFAULT;
    }

    @org.junit.Test
    @Override
    public void test() throws Exception {
        interruptAfter(new InterruptibleRunnable() {
            @Override
            public void run() throws InterruptedException {
                x2s("a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?aaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0);
            }
        }, 1000, 15000);

        final int status[] = new int[1];

        interruptAfter(new InterruptibleRunnable() {
            @Override
            public void run() throws InterruptedException {
                try {
                    x2s("a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?aaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0);
                } catch (InterruptedException ie) {
                    status[0] = Matcher.INTERRUPTED;
                }
            }
        }, 1000, 15000);

        assertTrue(status[0] == Matcher.INTERRUPTED, "Status was not INTERRUPTED: " + status[0]);
    }

    private void interruptAfter(InterruptibleRunnable block, int delayBeforeInterrupt, int acceptableMaximumTime) {
        final long start[] = new long[1];

        final Thread currentThread = Thread.currentThread();

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                start[0] = System.currentTimeMillis();
                System.out.println("INTERRUPTING at " + start[0]);
                currentThread.interrupt();
            }
        }, delayBeforeInterrupt);

        try {
            block.run();
        } catch (InterruptedException e) {

        }
        long total = System.currentTimeMillis() - start[0];
        System.out.println("Time taken: " + total);
        assertTrue(total < acceptableMaximumTime, "Took too long to interrupt: " + total + " > " + acceptableMaximumTime);
    }
}
