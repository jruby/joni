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

import org.joni.Config;
import org.joni.Option;
import org.joni.Syntax;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public class TestCrnl extends Test {

    public int option() {
        return Option.DEFAULT;
    }

    public Encoding encoding() {
        return ASCIIEncoding.INSTANCE;
    }
    
    public String testEncoding() {
        return "ascii";
    }

    public Syntax syntax() {
        return Syntax.DEFAULT;
    }   

    public void test() {
        x2s("",        "\r\n",        0,  0);
        x2s(".",       "\r\n",        0,  1);
        ns("..",      "\r\n");
        x2s("^",       "\r\n",        0,  0);
        x2s("\\n^",    "\r\nf",       1,  2);
        x2s("\\n^a",   "\r\na",       1,  3);
        x2s("$",       "\r\n",        0,  0);
        x2s("T$",      "T\r\n",       0,  1);
        x2s("T$",      "T\raT\r\n",   3,  4);
        x2s("\\z",     "\r\n",        2,  2);
        ns("a\\z",    "a\r\n");
        x2s("\\Z",     "\r\n",        0,  0);
        x2s("\\Z",     "\r\na",       3,  3);
        x2s("\\Z",     "\r\n\r\n\n",  4,  4);
        x2s("\\Z",     "\r\n\r\nX",   5,  5);
        x2s("a\\Z",    "a\r\n",       0,  1);
        x2s("aaaaaaaaaaaaaaa\\Z",   "aaaaaaaaaaaaaaa\r\n",  0,  15);
        x2s("a|$",     "b\r\n",       1,  1);
        x2s("$|b",     "\rb",         1,  2);
        x2s("a$|ab$",  "\r\nab\r\n",  2,  4);

        x2s("a|\\Z",       "b\r\n",       1,  1);
        x2s("\\Z|b",       "\rb",         1,  2);
        x2s("a\\Z|ab\\Z",  "\r\nab\r\n",  2,  4);
        x2s("(?=a$).",     "a\r\n",       0,  1);
        ns("(?=a$).",     "a\r");
        x2s("(?!a$)..",    "a\r",         0,  2);
        x2s("(?<=a$).\\n", "a\r\n",       1,  3);
        ns("(?<!a$).\\n", "a\r\n");
        x2s("(?=a\\Z).",     "a\r\n",       0,  1);
        ns("(?=a\\Z).",     "a\r");
        x2s("(?!a\\Z)..",    "a\r",         0,  2);
        
        if (nfail > 0 || nerror > 0) Config.err.println("make sure to enable USE_CRNL_AS_LINE_TERMINATOR");
    }
    
    public static void main(String[] args) throws Throwable{
        new TestCrnl().run();
    }
}
