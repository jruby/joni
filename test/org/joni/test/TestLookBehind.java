package org.joni.test;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.joni.Option;
import org.joni.Syntax;

public class TestLookBehind extends Test {

	public int option() {
		return Option.DEFAULT;
	}

	public Encoding encoding() {
		return ASCIIEncoding.INSTANCE;
	}

	public String testEncoding() {
		return "iso-8859-1";
	}

	public Syntax syntax() {
		return Syntax.DEFAULT;
	}

	@Override
	public void test() {
		x2s("(?<=a).*b", "aab", 1, 3);
	}

	public static void main(String[] args) throws Throwable {
		new TestLookBehind().run();
	}

}
