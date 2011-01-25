package org.joni.test;

import junit.framework.TestCase;

public class TestJoni extends TestCase {
    
    private Test testa;
    private Test testc;
    private Test testu;
    private Test testnsu8;
    private Test testLookBehind;
    
    protected void setUp() {
        testa = new TestA();
        testc = new TestC();
        testu = new TestU();
        testnsu8 = new TestNSU8();
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
    }

    public void testLookBehind() {
    	testJoniTest(testLookBehind);
    }
}
