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

public class TestJava extends Test {

    public int option() {
        return Option.DEFAULT;
    }

    public Encoding encoding() {
        return UTF8Encoding.INSTANCE;
    }

    public String testEncoding() {
        return "utf-8";
    }

    public Syntax syntax() {
        return Syntax.Java;
    }

    public void test() throws InterruptedException {
        // test ignorecase for Latin-1 Supplement
        x2s("[\\u00e0-\\u00e5]", "\u00c2", 0, 2, Option.IGNORECASE);
        x2s("[\\u00e2]", "\u00c2", 0, 2, Option.IGNORECASE);
        x2s("\\u00e2", "\u00c2", 0, 2, Option.IGNORECASE);
    }

    public static void main(String[] args) throws Throwable {
        new TestJava().run();
    }
}
