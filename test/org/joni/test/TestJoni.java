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

import junit.framework.TestCase;

public class TestJoni extends TestCase {

    private Test testa;
    private Test testc;
    private Test testu;
    private Test testnsu8;
    private Test testLookBehind;
    private Test testu8;

    protected void setUp() {
        testa = new TestA();
        testc = new TestC();
        testu = new TestU();
        testnsu8 = new TestNSU8();
        testu8 = new TestU8();
        testLookBehind = new TestLookBehind();
    }

    protected void tearDown() {
    }

    private void testJoniTest(Test test) {
        test.run();
        assertEquals(test.nerror, 0);
        assertEquals(test.nfail, 0);
    }

    public void testAscii() {
        testJoniTest(testa);
    }

    public void testEUCJP() {
        testJoniTest(testc);
    }

    public void testUnicode() {
        testJoniTest(testu);
        testJoniTest(testnsu8);
        testJoniTest(testu8);
    }

    public void testLookBehind() {
    	testJoniTest(testLookBehind);
    }
}
