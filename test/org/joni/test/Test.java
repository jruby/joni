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

import java.io.UnsupportedEncodingException;

import org.joni.Config;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.jcodings.Encoding;
import org.joni.exception.JOniException;

public abstract class Test {

    int nsucc;
    int nerror;
    int nfail;
    
    public abstract int option();
    public abstract Encoding encoding();
    public abstract String testEncoding();
    public abstract Syntax syntax();
    
    protected String repr(byte[]bytes) {
        return new String(bytes);
    }
    
    protected int length(byte[]bytes) {
        return bytes.length;
    }
    
    public void xx(byte[]pattern, byte[]str, int from, int to, int mem, boolean not) {
        xx(pattern, str, from, to, mem, not, option());
    }
    
    public void xx(byte[]pattern, byte[]str, int from, int to, int mem, boolean not, int option) {
        Regex reg;
        
        try {
            reg = new Regex(pattern, 0, length(pattern), option, encoding(), syntax());
        } catch (JOniException je) {
            Config.err.println("Pattern: " + repr(pattern) + " Str: " + repr(str));
            je.printStackTrace(Config.err);
            Config.err.println("ERROR: " + je.getMessage());
            nerror++;
            return;
        } catch (Exception e) {
            Config.err.println("Pattern: " + repr(pattern) + " Str: " + repr(str));
            e.printStackTrace(Config.err);
            Config.err.println("SEVERE ERROR: " + e.getMessage());
            nerror++;
            return;            
        }
        
        Matcher m = reg.matcher(str, 0, length(str));
        Region region;

        int r = 0;
        try {
            r = m.search(0, length(str), Option.NONE);
            region = m.getEagerRegion();
        } catch (JOniException je) {
            Config.err.println("Pattern: " + repr(pattern) + " Str: " + repr(str));
            je.printStackTrace(Config.err);
            Config.err.println("ERROR: " + je.getMessage());
            nerror++;
            return;
        } catch (Exception e) {
            Config.err.println("Pattern: " + repr(pattern) + " Str: " + repr(str));
            e.printStackTrace(Config.err);
            Config.err.println("SEVERE ERROR: " + e.getMessage());
            nerror++;
            return;            
        }
        
        if (r == -1) {
            if (not) {
                Config.log.println("OK(N): /" + repr(pattern) + "/ '" + repr(str) + "'");
                nsucc++;
            } else {
                Config.log.println("FAIL: /" + repr(pattern) + "/ '" + repr(str) + "'");
                nfail++;
            }
        } else {
            if (not) {
                Config.log.println("FAIL(N): /" + repr(pattern) + "/ '" + repr(str) + "'");
                nfail++;
            } else {
                if (region.beg[mem] == from && region.end[mem] == to) {
                    Config.log.println("OK: /" + repr(pattern) + "/ '" +repr(str) + "'");
                    nsucc++;
                } else {
                    Config.log.println("FAIL: /" + repr(pattern) + "/ '" + repr(str) + "' " +
                            from + "-" + to + " : " + region.beg[mem] + "-" + region.end[mem]
                            );
                    nfail++;
                }
            }
        }
    }
    
    protected void x2(byte[]pattern, byte[]str, int from, int to) {
        xx(pattern, str, from, to, 0, false);
    }
    
    protected void x3(byte[]pattern, byte[]str, int from, int to, int mem) {
        xx(pattern, str, from, to, mem, false);
    }
    
    protected void n(byte[]pattern, byte[]str) {
        xx(pattern, str, 0, 0, 0, true);
    }

    public void xxs(String pattern, String str, int from, int to, int mem, boolean not) {
        xxs(pattern, str, from, to, mem, not, option());
    }
    
    public void xxs(String pattern, String str, int from, int to, int mem, boolean not, int option) {
        try{
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, mem, not, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public void x2s(String pattern, String str, int from, int to) {
        x2s(pattern, str, from, to, option());
    }
    
    public void x2s(String pattern, String str, int from, int to, int option) {
        try{
        xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, 0, false, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }
    
    public void x3s(String pattern, String str, int from, int to, int mem) {
        x3s(pattern, str, from, to, mem, option());
    }
    
    public void x3s(String pattern, String str, int from, int to, int mem, int option) {
        try{        
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), from, to, mem, false, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }
    
    public void ns(String pattern, String str) {
        ns(pattern, str, option());
    }
    
    public void ns(String pattern, String str, int option) {
        try{        
            xx(pattern.getBytes(testEncoding()), str.getBytes(testEncoding()), 0, 0, 0, true, option);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }
    
    public void printResults() {
        Config.log.println("\nRESULT   SUCC: " + nsucc + ",  FAIL: " + nfail + ",  ERROR: " + nerror +
                "   (by JONI)");
    }
    
    public abstract void test();
    
    public final void run() {
        test();
        printResults();
    }
    
}
