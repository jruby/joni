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

import java.nio.charset.StandardCharsets;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.joni.Config;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;

public class TestCornerCases extends Test {
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
        return "cp1250";
    }
    @Override
    public Syntax syntax() {
        return Syntax.TEST;
    }

    @Override
    public void test() throws Exception {
        byte[] reg = "l.".getBytes();
        byte[] str = "hello,lo".getBytes();

        Regex p = new Regex(reg,0,reg.length,Option.DEFAULT,ASCIIEncoding.INSTANCE,Syntax.DEFAULT);
        int result = p.matcher(str, 0, str.length).search(3, 0, Option.NONE);
        if(result != 3) {
            Config.log.println("FAIL: /l./ 'hello,lo' - with reverse, 3,0");
            nfail++;
        }
    }

    @org.junit.Test(timeout = 30000)
    public void testNestedRepeatExpansionOverflow() {
        byte[] input = "xxxxxxxxxx".getBytes(StandardCharsets.US_ASCII);
        for (String source : new String[]{"(?:.{90000,}){90000}", "(?:.{46341,}){46341}",
                                          "(?:.{90000,}){90000,}", "(?:.{46341,}){46341,}"}) {
            byte[] pattern = source.getBytes(StandardCharsets.US_ASCII);
            Regex regex = new Regex(pattern, 0, pattern.length, Option.DEFAULT, ASCIIEncoding.INSTANCE, Syntax.DEFAULT);
            assertEquals(source, Matcher.FAILED, regex.matcher(input).search(0, input.length, Option.NONE));
        }
    }

}
