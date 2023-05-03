package org.joni.test;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Syntax;

public class TestCaseInsensitive extends Test {

    @Override
    public int option() {
        return Option.IGNORECASE;
    }
    @Override
    public Encoding encoding() {
        return UTF8Encoding.INSTANCE;
    }
    @Override
    public String testEncoding() {
        return "utf-8";
    }
    @Override
    public Syntax syntax() {
        return Syntax.TEST;
    }

    @Override
    public void test() throws Exception {
        xx("^\\d\\d\\d-".getBytes(), new byte[]{-30, -126, -84, 48, 45}, 0, 0, 0, true);
        x2s("ab", "\uD835\uDC4D ab", 5, 7);
    }
}
