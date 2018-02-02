/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.joni.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.jcodings.Encoding;
import org.joni.Config;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.WarnCallback;
import org.joni.exception.JOniException;

public abstract class Test {
    static final boolean VERBOSE = false;

    int nsucc;
    int nerror;
    int nfail;

    public abstract int option();
    public abstract Encoding encoding();
    public abstract String testEncoding();
    public abstract Syntax syntax();

    protected String repr(byte[] bytes) {
        return new String(bytes);
    }

    protected int length(byte[] bytes) {
        return bytes.length;
    }

    protected String reprTest(byte[] pattern, byte[]str, int option) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pattern: [/").append(repr(pattern)).append("/]");
        sb.append(" Str: [\"").append(repr(str)).append("\"]");
        sb.append(" Encoding: [" + encoding() + "]");
        sb.append(" Option: [" + Option.toString(option) + "]");
        sb.append(" Syntax: [" + syntax().name + "]");
        return sb.toString();
    }

    protected void assertTrue(boolean expression, String failMessage) {
        if (expression) {
            nsucc++;
        } else {
            Config.err.println(failMessage);
            nfail++;
        }
    }

    public void xerrs(String pattern, String msg) throws Exception {
        xerr(pattern.getBytes(testEncoding()), msg, option());
    }

    public void xerrs(String pattern, String msg, int option) throws Exception {
        xerr(pattern.getBytes(testEncoding()), msg, option);
    }

    public void xerr(byte[] pattern, String msg, int option) throws Exception {
        try {
            new Regex(pattern, 0, length(pattern), option, encoding(), syntax(), WarnCallback.NONE);
        } catch (JOniException je) {
            assertEquals(je.getMessage(), msg);
        }
    }

    public void xx(byte[] pattern, byte[] str, int from, int to, int mem, boolean not) throws InterruptedException {
        xx(pattern, str, from, to, mem, not, option());
    }

    public int xx(byte[] pattern, byte[] str, int from, int to, int mem, boolean not, int option) throws InterruptedException {
        Regex reg;

        try {
            reg = new Regex(pattern, 0, length(pattern), option, encoding(), syntax(), WarnCallback.NONE);
        } catch (JOniException je) {
            Config.err.println(reprTest(pattern, str, option));
            je.printStackTrace(Config.err);
            Config.err.println("ERROR: " + je.getMessage());
            nerror++;
            return Matcher.FAILED;
        } catch (Exception e) {
            Config.err.println(reprTest(pattern, str, option));
            e.printStackTrace(Config.err);
            Config.err.println("SEVERE ERROR: " + e.getMessage());
            nerror++;
            return Matcher.FAILED;
        }

        Matcher m = reg.matcher(str, 0, length(str));
        Region region;

        int r = 0;
        try {
            r = m.searchInterruptible(0, length(str), option);
            region = m.getEagerRegion();
        } catch (JOniException je) {
            Config.err.println("Pattern: " + reprTest(pattern, str, option));
            je.printStackTrace(Config.err);
            Config.err.println("ERROR: " + je.getMessage());
            nerror++;
            return Matcher.FAILED;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Config.err.println("Pattern: " + reprTest(pattern, str, option));
            e.printStackTrace(Config.err);
            Config.err.println("SEVERE ERROR: " + e.getMessage());
            nerror++;
            return Matcher.FAILED;
        }

        if (r == -1) {
            if (not) {
                if (VERBOSE) Config.log.println("OK(NOT): " + reprTest(pattern, str, option));
                nsucc++;
            } else {
                Config.log.println("FAIL: " + reprTest(pattern, str, option));
                nfail++;
            }
        } else {
            if (not) {
                Config.log.println("FAIL(NOT): " + reprTest(pattern, str, option));
                nfail++;
            } else {
                if (region.beg[mem] == from && region.end[mem] == to) {
                    if (VERBOSE) Config.log.println("OK: " + reprTest(pattern, str, option));
                    nsucc++;
                } else {
                    Config.log.println("FAIL: " + reprTest(pattern, str, option) + " Groups: [Exp " + from + "-" + to + ", Act "
                            + region.beg[mem] + "-" + region.end[mem] + "]");
                    nfail++;
                }
            }
        }
        return r;
    }

    protected void x2(byte[] pattern, byte[] str, int from, int to) throws InterruptedException {
        xx(pattern, str, from, to, 0, false);
    }

    protected void x2(byte[] pattern, byte[] str, int from, int to, int option) throws InterruptedException {
        xx(pattern, str, from, to, 0, false, option);
    }

    protected void x3(byte[] pattern, byte[] str, int from, int to, int mem) throws InterruptedException {
        xx(pattern, str, from, to, mem, false);
    }

    protected void n(byte[] pattern, byte[] str) throws InterruptedException {
        xx(pattern, str, 0, 0, 0, true);
    }

    protected void n(byte[] pattern, byte[] str, int option) throws InterruptedException {
        xx(pattern, str, 0, 0, 0, true, option);
    }

    public void xxs(String pattern, String str, int from, int to, int mem, boolean not) throws InterruptedException {
        xxs(pattern, str, from, to, mem, not, option());
    }

    public void xxs(String pattern, String str, int from, int to, int mem, boolean not, int option)
            throws InterruptedException {
        try {
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, mem, not, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public int x2s(String pattern, String str, int from, int to) throws InterruptedException {
        return x2s(pattern, str, from, to, option());
    }

    public int x2s(String pattern, String str, int from, int to, int option) throws InterruptedException {
        try {
            return xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, 0, false, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return Matcher.FAILED;
        }
    }

    public void x3s(String pattern, String str, int from, int to, int mem) throws InterruptedException {
        x3s(pattern, str, from, to, mem, option());
    }

    public void x3s(String pattern, String str, int from, int to, int mem, int option) throws InterruptedException {
        try {
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, mem, false, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public void ns(String pattern, String str) throws InterruptedException {
        ns(pattern, str, option());
    }

    public void ns(String pattern, String str, int option) throws InterruptedException {
        try {
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), 0, 0, 0, true, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    @org.junit.Test
    public void testRegexp() throws Exception {
        test();
        Config.log.println("RESULT   SUCC: " + nsucc + ",  FAIL: " + nfail + ",  ERROR: " + nerror + " Test: " + getClass().getSimpleName() + ", Encoding: " + encoding());
        assertEquals(0, nfail + nerror);
    }

    public abstract void test() throws Exception;
}
