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

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Syntax;

public class TestU8 extends Test {

    public int option() {
        return Option.DEFAULT;
    }

    public Encoding encoding() {
        return UTF8Encoding.INSTANCE;
    }

    public String testEncoding() {
        return "iso-8859-1";
    }

    public Syntax syntax() {
        return Syntax.DEFAULT;
    }

    public void test() {
        xx("^\\d\\d\\d-".getBytes(), new byte []{-30, -126, -84, 48, 45}, 0, 0, 0, true);
        x2s("x{2}", "xx", 0, 2, Option.IGNORECASE);
        x2s("x{2}", "XX", 0, 2, Option.IGNORECASE);
        x2s("x{3}", "XxX", 0, 3, Option.IGNORECASE);
        ns("x{2}", "x", Option.IGNORECASE);
        ns("x{2}", "X", Option.IGNORECASE);

        byte[] pat = new byte[] {(byte)227, (byte)131, (byte)160, (byte)40, (byte)46, (byte)41};
        byte[] str = new byte[]{(byte)227, (byte)130, (byte)185, (byte)227, (byte)131, (byte)145, (byte)227, (byte)131, (byte)160, (byte)227, (byte)131, (byte)143, (byte)227, (byte)131, (byte)179, (byte)227, (byte)130, (byte)175};

        x2(pat, str, 6, 12);
    }

    public static void main(String[] args) throws Throwable {
        new TestU8().run();
    }
}
