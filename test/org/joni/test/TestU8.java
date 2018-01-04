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

import java.util.Arrays;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Syntax;

public class TestU8 extends Test {
    @Override
    public int option() {
        return Option.DEFAULT;
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
        xx("^\\d\\d\\d-".getBytes(), new byte []{-30, -126, -84, 48, 45}, 0, 0, 0, true);
        x2s("x{2}", "xx", 0, 2, Option.IGNORECASE);
        x2s("x{2}", "XX", 0, 2, Option.IGNORECASE);
        x2s("x{3}", "XxX", 0, 3, Option.IGNORECASE);
        ns("x{2}", "x", Option.IGNORECASE);
        ns("x{2}", "X", Option.IGNORECASE);

        byte[] pat = new byte[] {(byte)227, (byte)131, (byte)160, (byte)40, (byte)46, (byte)41};
        byte[] str = new byte[]{(byte)227, (byte)130, (byte)185, (byte)227, (byte)131, (byte)145, (byte)227, (byte)131, (byte)160, (byte)227, (byte)131, (byte)143, (byte)227, (byte)131, (byte)179, (byte)227, (byte)130, (byte)175};

        x2(pat, str, 6, 12);

        x2s("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 35, Option.IGNORECASE);
        x2s("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 0, 35, Option.IGNORECASE);
        x2s("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaAAAAAAAAAAAAAAAAA", 0, 35, Option.IGNORECASE);

        pat = new byte[]{94, 40, (byte)239, (byte)188, (byte)161, 41, 92, 49, 36};
        str = new byte[]{(byte)239, (byte)188, (byte)161, 65};

        n(pat, str, Option.IGNORECASE);

        pat = new byte[]{94, (byte)195, (byte)159, 123, 50, 125, 36};
        str = new byte[]{(byte)195, (byte)159, 115, 115};

        x2(pat, str, 0, 4, Option.IGNORECASE);

        String str2 = new String(new byte[]{-61, -123, -61, -123});
        String pat2 = new String(new byte[]{'^', -61, -123, '{', '2', '}', '$'});

        // x2s(pat2, str2, 4, 4);
        // x2s(pat2, str2, 4, 4, Option.IGNORECASE);

        ns("(?i-mx:ak)a", "ema");

        x2s("(?i:!\\[CDAT)", "![CDAT", 0, 6);
        x2s("(?i:\\!\\[CDAa)", "\\![CDAa", 1, 7);
        x2s("(?i:\\!\\[CDAb)", "\\![CDAb", 1, 7);

        x2s("\\R", "\u0085", 0, 2);
        x2s("\\R", "\u2028", 0, 3);
        x2s("\\R", "\u2029", 0, 3);

        x2s("\\A\\R\\z", "\r", 0, 1);
        x2s("\\A\\R\\z", "\n", 0, 1);
        x2s("\\A\\R\\z", "\r\n", 0, 2);

        x2s("foo\\b", "foo", 0, 3);

        x2s("(x?)x*\\1", "x", 0, 1, Option.IGNORECASE);
        x2s("(x?)x*\\k<1+0>", "x", 0, 1, Option.IGNORECASE);
        x2s("(?<n>x?)(?<n>x?)\\k<n>", "x", 0, 1, Option.IGNORECASE);

        x2s("(?=((?<x>)(\\k<x>)))", "", 0, 0);

        x2s("a\\g<0>*z", "aaazzz", 0, 6);

        x2s("ab\\Kcd", "abcd", 2, 4);
        x2s("ab\\Kc(\\Kd|z)", "abcd", 3, 4);
        x2s("ab\\Kc(\\Kz|d)", "abcd", 2, 4);
        x2s("(a\\K)*", "aaab", 3, 3);
        x3s("(a\\K)*", "aaab", 2, 3, 1);
        // x2s("a\\K?a", "aa", 0, 2);             // error: differ from perl
        x2s("ab(?=c\\Kd)", "abcd", 2, 2);         // This behaviour is currently not well defined. (see: perlre)
        x2s("(?<=a\\Kb|aa)cd", "abcd", 1, 4);     // ...
        x2s("(?<=ab|a\\Ka)cd", "abcd", 2, 4);     // ...

        x2s("\\X", "\n", 0, 1);
        x2s("\\X", "\r", 0, 1);
        x2s("\\X{3}", "\r\r\n\n", 0, 4);
        x2s("\\X", "\u306F\u309A\n", 0, 6);
        x2s("\\A\\X\\z", "\u0020\u200d", 0, 4);
        x2s("\\A\\X\\z", "\u0600\u0600", 0, 4);
        x2s("\\A\\X\\z", "\u0600\u0020", 0, 3);

        x2s("\\A\\X\\z", " ‍", 0, 4);
        x2s("\\A\\X\\z", "؀؀", 0, 4);
        x2s("\\A\\X\\z", "؀", 0, 2);
        x2s("\\A\\X\\z", "☝🏻", 0, 7);
        x2s("\\A\\X\\z", "😀", 0, 4);
        x2s("\\A\\X\\z", " ̈", 0, 3); // u{1f600}

        // u{20 200d}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)32, (byte)226, (byte)128, (byte)141}, 0, 4);
        // u{600 600}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)216, (byte)128, (byte)216, (byte)128}, 0, 4);
        // u{600 20}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)216, (byte)128, (byte)32}, 0, 3);
        // u{261d 1F3FB}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)226, (byte)152, (byte)157, (byte)240, (byte)159, (byte)143, (byte)187}, 0, 7);
        // u{1f600}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)240, (byte)159, (byte)152, (byte)128}, 0, 4);
        // u{20 308}
        x2s("\\A\\X\\z", " \u0308", 0, 3);
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)32, (byte)204, (byte)136}, 0, 3);
        // u{a 308}
        x2s("\\A\\X\\z", "a\u0308", 0, 3);
        x2("\\A\\X\\X\\z".getBytes(), new byte[] {(byte)10, (byte)204, (byte)136}, 0, 3);
        // u{d 308}
        x2s("\\A\\X\\z", "d\u0308", 0, 3);
        x2("\\A\\X\\X\\z".getBytes(), new byte[] {(byte)13, (byte)204, (byte)136}, 0, 3);
        // u{1F477 1F3FF 200D 2640 FE0F}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)240, (byte)159, (byte)145, (byte)183, (byte)240, (byte)159, (byte)143, (byte)191, (byte)226, (byte)128, (byte)141, (byte)226, (byte)153, (byte)128, (byte)239, (byte)184, (byte)143}, 0, 17);
        // u{1F468 200D 1F393}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)240, (byte)159, (byte)145, (byte)168, (byte)226, (byte)128, (byte)141, (byte)240, (byte)159, (byte)142, (byte)147}, 0, 11);
        // u{1F46F 200D 2642 FE0F}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)240, (byte)159, (byte)145, (byte)175, (byte)226, (byte)128, (byte)141, (byte)226, (byte)153, (byte)130, (byte)239, (byte)184, (byte)143}, 0, 13);
        // u{1f469 200d 2764 fe0f 200d 1f469}
        x2("\\A\\X\\z".getBytes(), new byte[] {(byte)240, (byte)159, (byte)145, (byte)169, (byte)226, (byte)128, (byte)141, (byte)226, (byte)157, (byte)164, (byte)239, (byte)184, (byte)143, (byte)226, (byte)128, (byte)141, (byte)240, (byte)159, (byte)145, (byte)169}, 0, 20);

        x2s("\\A\\X\\X\\z", "\r\u0308", 0, 3);
        x2s("\\A\\X\\X\\z", "\n\u0308", 0, 3);

        x2s("[0-9-a]+", " 0123456789-a ", 1, 13);
        x2s("[0-9-\\s]+", " 0123456789-a ", 0, 12);
        x2s("[0-9-あ\\\\/\u0001]+", " 0123456789-あ\\/\u0001 ", 1, 18);
        x2s("[a-b-]+", "ab-", 0, 3);
        x2s("[a-b-&&-]+", "ab-", 2, 3);
        x2s("(?i)[a[b-あ]]+", "abあ", 0, 5);
        x2s("(?i)[\\d[:^graph:]]+", "0あ", 0, 1);
        x2s("(?ia)[\\d[:^print:]]+", "0あ", 0, 4);

        x2s("(?i:a) B", "a B", 0, 3);
        x2s("(?i:a )B", "a B", 0, 3);
        x2s("B (?i:a)", "B a", 0, 3);
        x2s("B(?i: a)", "B a", 0, 3);

        x2s("(?a)[\\p{Space}\\d]", "\u00a0", 0, 2);
        x2s("(?a)[\\d\\p{Space}]", "\u00a0", 0, 2);
        ns("(?a)[^\\p{Space}\\d]", "\u00a0");
        ns("(?a)[^\\d\\p{Space}]", "\u00a0");
        x2s("(?d)[[:space:]\\d]", "\u00a0", 0, 2);
        ns("(?d)[^\\d[:space:]]", "\u00a0");

        x2s("\\p{In_Unified_Canadian_Aboriginal_Syllabics_Extended}+", "\u18B0\u18FF", 0, 6);
        x2s("(?i)\u1ffc", "\u2126\u1fbe", 0, 6);
        x2s("(?i)\u1ffc", "\u1ff3", 0, 3);
        x2s("(?i)\u0390", "\u03b9\u0308\u0301", 0, 6);
        x2s("(?i)\u03b9\u0308\u0301", "\u0390", 0, 2);
        x2s("(?i)ff", "\ufb00", 0, 3);
        x2s("(?i)\ufb01", "fi", 0, 2);

        x2s("(?i)\u0149\u0149", "\u0149\u0149", 0, 4);
        x2s("(?i)(?<=\u0149)a", "\u02bcna", 3, 4);

        x2s("(?<=(?i)ab)cd", "ABcd", 2, 4);

        x2s("(?<=(?i)ab)cd", "ABcd", 2, 4);
        x2s("(?<=(?i:ab))cd", "ABcd", 2, 4);
        ns("(?<=(?i)ab)cd", "ABCD");
        ns("(?<=(?i:ab))cd", "ABCD");
        x2s("(?<!(?i)ab)cd", "aacd", 2, 4);
        x2s("(?<!(?i:ab))cd", "aacd", 2, 4);
        ns("(?<!(?i)ab)cd", "ABcd");
        ns("(?<!(?i:ab))cd", "ABcd");

        // Fullwidth Alphabet
        ns("ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ", "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ");
        x2s("(?i)ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ", "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ", 0, 26 * 3);
        x2s("(?i)ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ", "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ", 0, 26 * 3);
        x2s("(?i)ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ", "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ", 0, 26 * 3);
        x2s("(?i)ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ", "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ", 0, 26 * 3);

        // Greek
        ns("αβγδεζηθικλμνξοπρστυφχψω", "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ");
        x2s("(?i)αβγδεζηθικλμνξοπρστυφχψω", "αβγδεζηθικλμνξοπρστυφχψω", 0, 24 * 2);
        x2s("(?i)αβγδεζηθικλμνξοπρστυφχψω", "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ", 0, 24 * 2);
        x2s("(?i)ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ", "αβγδεζηθικλμνξοπρστυφχψω", 0, 24 * 2);
        x2s("(?i)ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ", "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ", 0, 24 * 2);

        // Cyrillic
        ns("абвгдеёжзийклмнопрстуфхцчшщъыьэюя", "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ");
        x2s("(?i)абвгдеёжзийклмнопрстуфхцчшщъыьэюя", "абвгдеёжзийклмнопрстуфхцчшщъыьэюя", 0, 33 * 2);
        x2s("(?i)абвгдеёжзийклмнопрстуфхцчшщъыьэюя", "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", 0, 33 * 2);
        x2s("(?i)АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", "абвгдеёжзийклмнопрстуфхцчшщъыьэюя", 0, 33 * 2);
        x2s("(?i)АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", 0, 33 * 2);

        x2s("(?u)\\w+", "あa#", 0, 4);
        x2s("(?a)\\w+", "あa#", 3, 4);
        x2s("(?u)\\W+", "あa#", 4, 5);
        x2s("(?a)\\W+", "あa#", 0, 3);

        x2s("(?a)\\b", "あa", 3, 3);
        x2s("(?a)\\w\\b", "aあ", 0, 1);
        x2s("(?a)\\B", "a ああ ", 2, 2);

        x2s("(?u)\\B", "あ ", 4, 4);
        x2s("(?a)\\B", "あ ", 0, 0);
        x2s("(?a)\\B", "aあ ", 4, 4);

        x2s("(?a)a\\b", " a", 1, 2);
        x2s("(?u)a\\b", " a", 1, 2);
        ns("(?a)a\\B", " a");
        ns("(?a)あ\\b", " あ");
        x2s("(?u)あ\\b", " あ", 1, 4);
        x2s("(?a)あ\\B", " あ", 1, 4);
        ns("(?u)あ\\B", " あ");

        x2s("(?a)\\p{Alpha}\\P{Alpha}", "a。", 0, 4);
        x2s("(?u)\\p{Alpha}\\P{Alpha}", "a。", 0, 4);
        x2s("(?a)[[:word:]]+", "aあ", 0, 1);
        x2s("(?a)[[:^word:]]+", "aあ", 1, 4);
        x2s("(?u)[[:word:]]+", "aあ", 0, 4);
        ns("(?u)[[:^word:]]+", "aあ");

        x2s("(?iu)\\p{lower}\\p{upper}", "Ab", 0, 2);
        x2s("(?ia)\\p{lower}\\p{upper}", "Ab", 0, 2);
        x2s("(?iu)[[:lower:]][[:upper:]]", "Ab", 0, 2);
        x2s("(?ia)[[:lower:]][[:upper:]]", "Ab", 0, 2);

        ns("(?ia)\\w+", "\u212a\u017f");
        ns("(?ia)[\\w]+", "\u212a\u017f");
        ns("(?ia)[^\\W]+", "\u212a\u017f");
        x2s("(?ia)[^\\W]+", "ks", 0, 2);
        ns("(?iu)\\p{ASCII}", "\u212a");
        ns("(?iu)\\P{ASCII}", "s");
        ns("(?iu)[\\p{ASCII}]", "\u212a");
        ns("(?iu)[\\P{ASCII}]", "s");
        ns("(?ia)\\p{ASCII}", "\u212a");
        ns("(?ia)\\P{ASCII}", "s");
        ns("(?ia)[\\p{ASCII}]", "\u212a");
        ns("(?ia)[\\P{ASCII}]", "s");
        x2s("(?iu)[s]+", "Ss\u017f ", 0, 4);
        x2s("(?ia)[s]+", "Ss\u017f ", 0, 4);
        x2s("(?iu)[^s]+", "Ss\u017f ", 4, 5);
        x2s("(?ia)[^s]+", "Ss\u017f ", 4, 5);
        x2s("(?iu)[[:lower:]]", "\u017f", 0, 2);
        ns("(?ia)[[:lower:]]", "\u017f");
        x2s("(?u)[[:upper:]]", "\u212a", 0, 3);
        ns("(?a)[[:upper:]]", "\u212a");

        // ns("x.*\\b", "x");
        // x2s("x.*\\B", "x", 0, 1);
        x2s("c.*\\b", "abc", 2, 3); // Onigmo #96
        x2s("abc.*\\b", "abc", 0, 3);
        x2s("\\b.*abc.*\\b", "abc", 0, 3);

        super.test();
    }

}
