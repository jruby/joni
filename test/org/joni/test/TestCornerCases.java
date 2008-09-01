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
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public class TestCornerCases extends Test {
    public int option() {
        return Option.DEFAULT;
    }

    public Encoding encoding() {
        return ASCIIEncoding.INSTANCE;
    }
    
    public String testEncoding() {
        return "cp1250";
    }

    public Syntax syntax() {
        return Syntax.DEFAULT;
    }   

    public void test() {
        byte[] reg = "l.".getBytes();
        byte[] str = "hello,lo".getBytes();

        Regex p = new Regex(reg,0,reg.length,Option.DEFAULT,ASCIIEncoding.INSTANCE,Syntax.DEFAULT);
        int result = p.matcher(str, 0, str.length).search(3, 0, Option.NONE);
        if(result != 3) {
            Config.log.println("FAIL: /l./ 'hello,lo' - with reverse, 3,0");
            nfail++;
        }
    }
    
    public static void main(String[] args) throws Throwable{
        new TestCornerCases().run();
    }
}
