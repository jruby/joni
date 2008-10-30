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

import org.joni.Option;
import org.joni.Syntax;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public class TestA extends Test {

    public int option() {
        return Option.DEFAULT;
    }

    public Encoding encoding() {
        return ASCIIEncoding.INSTANCE;
    }
    
    public String testEncoding() {
        return "iso-8859-2";
    }

    public Syntax syntax() {
        return Syntax.DEFAULT;
    }   

    public void test() {
        x2s("", "", 0, 0);
        x2s("^", "", 0, 0);
        x2s("$", "", 0, 0);
        x2s("\\G", "", 0, 0);
        x2s("\\A", "", 0, 0);
        x2s("\\Z", "", 0, 0);
        x2s("\\z", "", 0, 0);
        x2s("^$", "", 0, 0);
        x2s("\\ca", "\001", 0, 1);
        x2s("\\C-b", "\002", 0, 1);
        x2s("\\c\\\\", "\034", 0, 1);
        x2s("q[\\c\\\\]", "q\034", 0, 2);
        x2s("", "a", 0, 0);
        x2s("a", "a", 0, 1);
        x2s("\\x61", "a", 0, 1);
        x2s("aa", "aa", 0, 2);
        x2s("aaa", "aaa", 0, 3);
        x2s("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 35);
        x2s("ab", "ab", 0, 2);
        x2s("b", "ab", 1, 2);
        x2s("bc", "abc", 1, 3);
        x2s("(?i:#RET#)", "#INS##RET#", 5, 10);
        x2s("\\17", "\017", 0, 1);
        x2s("\\x1f", "\u001f", 0, 1);
        x2s("\\xED\\xF2", "\u00ed\u0148", 0, 2);
        x2s("a(?#....\\\\JJJJ)b", "ab", 0, 2);
        x2s("(?x)  G (o O(?-x)oO) g L", "GoOoOgLe", 0, 7);
        x2s(".", "a", 0, 1);
        ns(".", "");
        x2s("..", "ab", 0, 2);
        x2s("\\w", "e", 0, 1);
        ns("\\W", "e");
        x2s("\\s", " ", 0, 1);
        x2s("\\S", "b", 0, 1);
        x2s("\\d", "4", 0, 1);
        ns("\\D", "4");
        x2s("\\b", "z ", 0, 0);
        x2s("\\b", " z", 1, 1);
        x2s("\\B", "zz ", 1, 1);
        x2s("\\B", "z ", 2, 2);
        x2s("\\B", " z", 0, 0);
        x2s("[ab]", "b", 0, 1);
        ns("[ab]", "c");
        x2s("[a-z]", "t", 0, 1);
        ns("[^a]", "a");
        x2s("[^a]", "\n", 0, 1);
        x2s("[]]", "]", 0, 1);
        ns("[^]]", "]");
        x2s("[\\^]+", "0^^1", 1, 3);
        x2s("[b-]", "b", 0, 1);
        x2s("[b-]", "-", 0, 1);
        x2s("[\\w]", "z", 0, 1);
        ns("[\\w]", " ");
        x2s("[\\W]", "b$", 1, 2);
        x2s("[\\d]", "5", 0, 1);
        ns("[\\d]", "e");
        x2s("[\\D]", "t", 0, 1);
        ns("[\\D]", "3");
        x2s("[\\s]", " ", 0, 1);
        ns("[\\s]", "a");
        x2s("[\\S]", "b", 0, 1);
        ns("[\\S]", " ");
        x2s("[\\w\\d]", "2", 0, 1);
        ns("[\\w\\d]", " ");
        x2s("[[:upper:]]", "B", 0, 1);
        x2s("[*[:xdigit:]+]", "+", 0, 1);
        x2s("[*[:xdigit:]+]", "GHIKK-9+*", 6, 7);
        x2s("[*[:xdigit:]+]", "-@^+", 3, 4);
        ns("[[:upper]]", "A");
        x2s("[[:upper]]", ":", 0, 1);
        x2s("[\\044-\\047]", "\046", 0, 1);
        x2s("[\\x5a-\\x5c]", "\u005b", 0, 1);
        x2s("[\\x6A-\\x6D]", "\u006c", 0, 1);
        ns("[\\x6A-\\x6D]", "\u006e");
        ns("^[0-9A-F]+ 0+ UNDEF ", "75F 00000000 SECT14A notype ()    External    | _rb_apply");
        x2s("[\\[]", "[", 0, 1);
        x2s("[\\]]", "]", 0, 1);
        x2s("[&]", "&", 0, 1);
        x2s("[[ab]]", "b", 0, 1);
        x2s("[[ab]c]", "c", 0, 1);
        ns("[[^a]]", "a");
        ns("[^[a]]", "a");
        x2s("[[ab]&&bc]", "b", 0, 1);
        ns("[[ab]&&bc]", "a");
        ns("[[ab]&&bc]", "c");
        x2s("[a-z&&b-y&&c-x]", "w", 0, 1);
        ns("[^a-z&&b-y&&c-x]", "w");
        x2s("[[^a&&a]&&a-z]", "b", 0, 1);
        ns("[[^a&&a]&&a-z]", "a");
        x2s("[[^a-z&&bcdef]&&[^c-g]]", "h", 0, 1);
        ns("[[^a-z&&bcdef]&&[^c-g]]", "c");
        x2s("[^[^abc]&&[^cde]]", "c", 0, 1);
        x2s("[^[^abc]&&[^cde]]", "e", 0, 1);
        ns("[^[^abc]&&[^cde]]", "f");
        x2s("[a-&&-a]", "-", 0, 1);
        ns("[a\\-&&\\-a]", "&");
        ns("\\wabc", " abc");
        x2s("a\\Wbc", "a bc", 0, 4);
        x2s("a.b.c", "aabbc", 0, 5);
        x2s(".\\wb\\W..c", "abb bcc", 0, 7);
        x2s("\\s\\wzzz", " zzzz", 0, 5);
        x2s("aa.b", "aabb", 0, 4);
        ns(".a", "ab");
        x2s(".a", "aa", 0, 2);
        x2s("^a", "a", 0, 1);
        x2s("^a$", "a", 0, 1);
        x2s("^\\w$", "a", 0, 1);
        ns("^\\w$", " ");
        x2s("^\\wab$", "zab", 0, 3);
        x2s("^\\wabcdef$", "zabcdef", 0, 7);
        x2s("^\\w...def$", "zabcdef", 0, 7);
        x2s("\\w\\w\\s\\Waaa\\d", "aa  aaa4", 0, 8);
        x2s("\\A\\Z", "", 0, 0);
        x2s("\\Axyz", "xyz", 0, 3);
        x2s("xyz\\Z", "xyz", 0, 3);
        x2s("xyz\\z", "xyz", 0, 3);
        x2s("a\\Z", "a", 0, 1);
        x2s("\\Gaz", "az", 0, 2);
        ns("\\Gz", "bza");
        ns("az\\G", "az");
        ns("az\\A", "az");
        ns("a\\Az", "az");
        x2s("\\^\\$", "^$", 0, 2);
        x2s("^x?y", "xy", 0, 2);
        x2s("^(x?y)", "xy", 0, 2);
        x2s("\\w", "_", 0, 1);
        ns("\\W", "_");
        x2s("(?=z)z", "z", 0, 1);
        ns("(?=z).", "a");
        x2s("(?!z)a", "a", 0, 1);
        ns("(?!z)a", "z");
        x2s("(?i:a)", "a", 0, 1);
        x2s("(?i:a)", "A", 0, 1);
        x2s("(?i:A)", "a", 0, 1);
        ns("(?i:A)", "b");
        x2s("(?i:[A-Z])", "a", 0, 1);
        x2s("(?i:[f-m])", "H", 0, 1);
        x2s("(?i:[f-m])", "h", 0, 1);
        ns("(?i:[f-m])", "e");
        x2s("(?i:[A-c])", "D", 0, 1);
        x2s("(?i:[!-k])", "Z", 0, 1);
        x2s("(?i:[!-k])", "7", 0, 1);
        x2s("(?i:[T-}])", "b", 0, 1);
        x2s("(?i:[T-}])", "{", 0, 1);
        x2s("(?i:\\?a)", "?A", 0, 2);
        x2s("(?i:\\*A)", "*a", 0, 2);
        ns(".", "\n");
        x2s("(?m:.)", "\n", 0, 1);
        x2s("(?m:a.)", "a\n", 0, 2);
        x2s("(?m:.b)", "a\nb", 1, 3);
        x2s(".*abc", "dddabdd\nddabc", 8, 13);
        x2s("(?m:.*abc)", "dddabddabc", 0, 10);
        ns("(?i)(?-i)a", "A");
        ns("(?i)(?-i:a)", "A");
        x2s("a?", "", 0, 0);
        x2s("a?", "b", 0, 0);
        x2s("a?", "a", 0, 1);
        x2s("a*", "", 0, 0);
        x2s("a*", "a", 0, 1);
        x2s("a*", "aaa", 0, 3);
        x2s("a*", "baaaa", 0, 0);
        ns("a+", "");
        x2s("a+", "a", 0, 1);
        x2s("a+", "aaaa", 0, 4);
        x2s("a+", "aabbb", 0, 2);
        x2s("a+", "baaaa", 1, 5);
        x2s(".?", "", 0, 0);
        x2s(".?", "f", 0, 1);
        x2s(".?", "\n", 0, 0);
        x2s(".*", "", 0, 0);
        x2s(".*", "abcde", 0, 5);
        x2s(".+", "z", 0, 1);
        x2s(".+", "zdswer\n", 0, 6);
        x2s("(.*)a\\1f", "babfbac", 0, 4);
        x2s("(.*)a\\1f", "bacbabf", 3, 7);
        x2s("((.*)a\\2f)", "bacbabf", 3, 7);
        x2s("(.*)a\\1f", "baczzzzzz\nbazz\nzzzzbabf", 19, 23);
        x2s("a|b", "a", 0, 1);
        x2s("a|b", "b", 0, 1);
        x2s("|a", "a", 0, 0);
        x2s("(|a)", "a", 0, 0);
        x2s("ab|bc", "ab", 0, 2);
        x2s("ab|bc", "bc", 0, 2);
        x2s("z(?:ab|bc)", "zbc", 0, 3);
        x2s("a(?:ab|bc)c", "aabc", 0, 4);
        x2s("ab|(?:ac|az)", "az", 0, 2);
        x2s("a|b|c", "dc", 1, 2);
        x2s("a|b|cd|efg|h|ijk|lmn|o|pq|rstuvwx|yz", "pqr", 0, 2);
        ns("a|b|cd|efg|h|ijk|lmn|o|pq|rstuvwx|yz", "mn");
        x2s("a|^z", "ba", 1, 2);
        x2s("a|^z", "za", 0, 1);
        x2s("a|\\Gz", "bza", 2, 3);
        x2s("a|\\Gz", "za", 0, 1);
        x2s("a|\\Az", "bza", 2, 3);
        x2s("a|\\Az", "za", 0, 1);
        x2s("a|b\\Z", "ba", 1, 2);
        x2s("a|b\\Z", "b", 0, 1);
        x2s("a|b\\z", "ba", 1, 2);
        x2s("a|b\\z", "b", 0, 1);
        x2s("\\w|\\s", " ", 0, 1);
        ns("\\w|\\w", " ");
        x2s("\\w|%", "%", 0, 1);
        x2s("\\w|[&$]", "&", 0, 1);
        x2s("[b-d]|[^e-z]", "a", 0, 1);
        x2s("(?:a|[c-f])|bz", "dz", 0, 1);
        x2s("(?:a|[c-f])|bz", "bz", 0, 2);
        x2s("abc|(?=zz)..f", "zzf", 0, 3);
        x2s("abc|(?!zz)..f", "abf", 0, 3);
        x2s("(?=za)..a|(?=zz)..a", "zza", 0, 3);
        ns("(?>a|abd)c", "abdc");
        x2s("(?>abd|a)c", "abdc", 0, 4);
        x2s("a?|b", "a", 0, 1);
        x2s("a?|b", "b", 0, 0);
        x2s("a?|b", "", 0, 0);
        x2s("a*|b", "aa", 0, 2);
        x2s("a*|b*", "ba", 0, 0);
        x2s("a*|b*", "ab", 0, 1);
        x2s("a+|b*", "", 0, 0);
        x2s("a+|b*", "bbb", 0, 3);
        x2s("a+|b*", "abbb", 0, 1);
        ns("a+|b+", "");
        x2s("(a|b)?", "b", 0, 1);
        x2s("(a|b)*", "ba", 0, 2);
        x2s("(a|b)+", "bab", 0, 3);
        x2s("(ab|ca)+", "caabbc", 0, 4);
        x2s("(ab|ca)+", "aabca", 1, 5);
        x2s("(ab|ca)+", "abzca", 0, 2);
        x2s("(a|bab)+", "ababa", 0, 5);
        x2s("(a|bab)+", "ba", 1, 2);
        x2s("(a|bab)+", "baaaba", 1, 4);
        x2s("(?:a|b)(?:a|b)", "ab", 0, 2);
        x2s("(?:a*|b*)(?:a*|b*)", "aaabbb", 0, 3);
        x2s("(?:a*|b*)(?:a+|b+)", "aaabbb", 0, 6);
        x2s("(?:a+|b+){2}", "aaabbb", 0, 6);
        x2s("h{0,}", "hhhh", 0, 4);
        x2s("(?:a+|b+){1,2}", "aaabbb", 0, 6);
        ns("ax{2}*a", "0axxxa1");
        ns("a.{0,2}a", "0aXXXa0");
        ns("a.{0,2}?a", "0aXXXa0");
        ns("a.{0,2}?a", "0aXXXXa0");
        x2s("^a{2,}?a$", "aaa", 0, 3);
        x2s("^[a-z]{2,}?$", "aaa", 0, 3);
        x2s("(?:a+|\\Ab*)cc", "cc", 0, 2);
        ns("(?:a+|\\Ab*)cc", "abcc");
        x2s("(?:^a+|b+)*c", "aabbbabc", 6, 8);
        x2s("(?:^a+|b+)*c", "aabbbbc", 0, 7);
        x2s("a|(?i)c", "C", 0, 1);
        x2s("(?i)c|a", "C", 0, 1);
        x2s("(?i)c|a", "A", 0, 1);
        x2s("(?i:c)|a", "C", 0, 1);
        ns("(?i:c)|a", "A");
        x2s("[abc]?", "abc", 0, 1);
        x2s("[abc]*", "abc", 0, 3);
        x2s("[^abc]*", "abc", 0, 0);
        ns("[^abc]+", "abc");
        x2s("a??", "aaa", 0, 0);
        x2s("ba??b", "bab", 0, 3);
        x2s("a*?", "aaa", 0, 0);
        x2s("ba*?", "baa", 0, 1);
        x2s("ba*?b", "baab", 0, 4);
        x2s("a+?", "aaa", 0, 1);
        x2s("ba+?", "baa", 0, 2);
        x2s("ba+?b", "baab", 0, 4);
        x2s("(?:a?)??", "a", 0, 0);
        x2s("(?:a??)?", "a", 0, 0);
        x2s("(?:a?)+?", "aaa", 0, 1);
        x2s("(?:a+)??", "aaa", 0, 0);
        x2s("(?:a+)??b", "aaab", 0, 4);
        x2s("(?:ab)?{2}", "", 0, 0);
        x2s("(?:ab)?{2}", "ababa", 0, 4);
        x2s("(?:ab)*{0}", "ababa", 0, 0);
        x2s("(?:ab){3,}", "abababab", 0, 8);
        ns("(?:ab){3,}", "abab");
        x2s("(?:ab){2,4}", "ababab", 0, 6);
        x2s("(?:ab){2,4}", "ababababab", 0, 8);
        x2s("(?:ab){2,4}?", "ababababab", 0, 4);
        x2s("(?:ab){,}", "ab{,}", 0, 5);
        x2s("(?:abc)+?{2}", "abcabcabc", 0, 6);
        x2s("(?:X*)(?i:xa)", "XXXa", 0, 4);
        x2s("(d+)([^abc]z)", "dddz", 0, 4);
        x2s("([^abc]*)([^abc]z)", "dddz", 0, 4);
        x2s("(\\w+)(\\wz)", "dddz", 0, 4);
        x3s("(a)", "a", 0, 1, 1);
        x3s("(ab)", "ab", 0, 2, 1);
        x2s("((ab))", "ab", 0, 2);
        x3s("((ab))", "ab", 0, 2, 1);
        x3s("((ab))", "ab", 0, 2, 2);
        x3s("((((((((((((((((((((ab))))))))))))))))))))", "ab", 0, 2, 20);
        x3s("(ab)(cd)", "abcd", 0, 2, 1);
        x3s("(ab)(cd)", "abcd", 2, 4, 2);
        x3s("()(a)bc(def)ghijk", "abcdefghijk", 3, 6, 3);
        x3s("(()(a)bc(def)ghijk)", "abcdefghijk", 3, 6, 4);
        x2s("(^a)", "a", 0, 1);
        x3s("(a)|(a)", "ba", 1, 2, 1);
        x3s("(^a)|(a)", "ba", 1, 2, 2);
        x3s("(a?)", "aaa", 0, 1, 1);
        x3s("(a*)", "aaa", 0, 3, 1);
        x3s("(a*)", "", 0, 0, 1);
        x3s("(a+)", "aaaaaaa", 0, 7, 1);
        x3s("(a+|b*)", "bbbaa", 0, 3, 1);
        x3s("(a+|b?)", "bbbaa", 0, 1, 1);
        x3s("(abc)?", "abc", 0, 3, 1);
        x3s("(abc)*", "abc", 0, 3, 1);
        x3s("(abc)+", "abc", 0, 3, 1);
        x3s("(xyz|abc)+", "abc", 0, 3, 1);
        x3s("([xyz][abc]|abc)+", "abc", 0, 3, 1);
        x3s("((?i:abc))", "AbC", 0, 3, 1);
        x2s("(abc)(?i:\\1)", "abcABC", 0, 6);
        x3s("((?m:a.c))", "a\nc", 0, 3, 1);
        x3s("((?=az)a)", "azb", 0, 1, 1);
        x3s("abc|(.abd)", "zabd", 0, 4, 1);
        x2s("(?:abc)|(ABC)", "abc", 0, 3);
        x3s("(?i:(abc))|(zzz)", "ABC", 0, 3, 1);
        x3s("a*(.)", "aaaaz", 4, 5, 1);
        x3s("a*?(.)", "aaaaz", 0, 1, 1);
        x3s("a*?(c)", "aaaac", 4, 5, 1);
        x3s("[bcd]a*(.)", "caaaaz", 5, 6, 1);
        x3s("(\\Abb)cc", "bbcc", 0, 2, 1);
        ns("(\\Abb)cc", "zbbcc");
        x3s("(^bb)cc", "bbcc", 0, 2, 1);
        ns("(^bb)cc", "zbbcc");
        x3s("cc(bb$)", "ccbb", 2, 4, 1);
        ns("cc(bb$)", "ccbbb");
        ns("(\\1)", "");
        ns("\\1(a)", "aa");
        ns("(a(b)\\1)\\2+", "ababb");
        ns("(?:(?:\\1|z)(a))+$", "zaa");
        x2s("(?:(?:\\1|z)(a))+$", "zaaa", 0, 4);
        x2s("(a)(?=\\1)", "aa", 0, 1);
        ns("(a)$|\\1", "az");
        x2s("(a)\\1", "aa", 0, 2);
        ns("(a)\\1", "ab");
        x2s("(a?)\\1", "aa", 0, 2);
        x2s("(a??)\\1", "aa", 0, 0);
        x2s("(a*)\\1", "aaaaa", 0, 4);
        x3s("(a*)\\1", "aaaaa", 0, 2, 1);
        x2s("a(b*)\\1", "abbbb", 0, 5);
        x2s("a(b*)\\1", "ab", 0, 1);
        x2s("(a*)(b*)\\1\\2", "aaabbaaabb", 0, 10);
        x2s("(a*)(b*)\\2", "aaabbbb", 0, 7);
        x2s("(((((((a*)b))))))c\\7", "aaabcaaa", 0, 8);
        x3s("(((((((a*)b))))))c\\7", "aaabcaaa", 0, 3, 7);
        x2s("(a)(b)(c)\\2\\1\\3", "abcbac", 0, 6);
        x2s("([a-d])\\1", "cc", 0, 2);
        x2s("(\\w\\d\\s)\\1", "f5 f5 ", 0, 6);
        ns("(\\w\\d\\s)\\1", "f5 f5");
        x2s("(who|[a-c]{3})\\1", "whowho", 0, 6);
        x2s("...(who|[a-c]{3})\\1", "abcwhowho", 0, 9);
        x2s("(who|[a-c]{3})\\1", "cbccbc", 0, 6);
        x2s("(^a)\\1", "aa", 0, 2);
        ns("(^a)\\1", "baa");
        ns("(a$)\\1", "aa");
        ns("(ab\\Z)\\1", "ab");
        x2s("(a*\\Z)\\1", "a", 1, 1);
        x2s(".(a*\\Z)\\1", "ba", 1, 2);
        x3s("(.(abc)\\2)", "zabcabc", 0, 7, 1);
        x3s("(.(..\\d.)\\2)", "z12341234", 0, 9, 1);
        x2s("((?i:az))\\1", "AzAz", 0, 4);
        ns("((?i:az))\\1", "Azaz");
        x2s("(?<=a)b", "ab", 1, 2);
        ns("(?<=a)b", "bb");
        x2s("(?<=a|b)b", "bb", 1, 2);
        x2s("(?<=a|bc)b", "bcb", 2, 3);
        x2s("(?<=a|bc)b", "ab", 1, 2);
        x2s("(?<=a|bc||defghij|klmnopq|r)z", "rz", 1, 2);
        x2s("(a)\\g<1>", "aa", 0, 2);
        x2s("(?<!a)b", "cb", 1, 2);
        ns("(?<!a)b", "ab");
        x2s("(?<!a|bc)b", "bbb", 0, 1);
        ns("(?<!a|bc)z", "bcz");
        x2s("(?<name1>a)", "a", 0, 1);
        x2s("(?<name_2>ab)\\g<name_2>", "abab", 0, 4);
        x2s("(?<name_3>.zv.)\\k<name_3>", "azvbazvb", 0, 8);
        x2s("(?<=\\g<ab>)|-\\zEND (?<ab>XyZ)", "XyZ", 3, 3);
        x2s("(?<n>|a\\g<n>)+", "", 0, 0);
        x2s("(?<n>|\\(\\g<n>\\))+$", "()(())", 0, 6);
        x3s("\\g<n>(?<n>.){0}", "X", 0, 1, 1);
        x2s("\\g<n>(abc|df(?<n>.YZ){2,8}){0}", "XYZ", 0, 3);
        x2s("\\A(?<n>(a\\g<n>)|)\\z", "aaaa", 0, 4);
        x2s("(?<n>|\\g<m>\\g<n>)\\z|\\zEND (?<m>a|(b)\\g<m>)", "bbbbabba", 0, 8);
        x2s("(?<name1240>\\w+\\sx)a+\\k<name1240>", "  fg xaaaaaaaafg x", 2, 18);
        x3s("(z)()()(?<_9>a)\\g<_9>", "zaa", 2, 3, 1);
        x2s("(.)(((?<_>a)))\\k<_>", "zaa", 0, 3);
        x2s("((?<name1>\\d)|(?<name2>\\w))(\\k<name1>|\\k<name2>)", "ff", 0, 2);
        x2s("(?:(?<x>)|(?<x>efg))\\k<x>", "", 0, 0);
        x2s("(?:(?<x>abc)|(?<x>efg))\\k<x>", "abcefgefg", 3, 9);
        ns("(?:(?<x>abc)|(?<x>efg))\\k<x>", "abcefg");
        x2s("(?:(?<n1>.)|(?<n1>..)|(?<n1>...)|(?<n1>....)|(?<n1>.....)|(?<n1>......)|(?<n1>.......)|(?<n1>........)|(?<n1>.........)|(?<n1>..........)|(?<n1>...........)|(?<n1>............)|(?<n1>.............)|(?<n1>..............))\\k<n1>$", "a-pyumpyum", 2, 10);
        x3s("(?:(?<n1>.)|(?<n1>..)|(?<n1>...)|(?<n1>....)|(?<n1>.....)|(?<n1>......)|(?<n1>.......)|(?<n1>........)|(?<n1>.........)|(?<n1>..........)|(?<n1>...........)|(?<n1>............)|(?<n1>.............)|(?<n1>..............))\\k<n1>$", "xxxxabcdefghijklmnabcdefghijklmn", 4, 18, 14);
        x3s("(?<name1>)(?<name2>)(?<name3>)(?<name4>)(?<name5>)(?<name6>)(?<name7>)(?<name8>)(?<name9>)(?<name10>)(?<name11>)(?<name12>)(?<name13>)(?<name14>)(?<name15>)(?<name16>aaa)(?<name17>)$", "aaa", 0, 3, 16);
        x2s("(?<foo>a|\\(\\g<foo>\\))", "a", 0, 1);
        x2s("(?<foo>a|\\(\\g<foo>\\))", "((((((a))))))", 0, 13);
        x3s("(?<foo>a|\\(\\g<foo>\\))", "((((((((a))))))))", 0, 17, 1);
        x2s("\\g<bar>|\\zEND(?<bar>.*abc$)", "abcxxxabc", 0, 9);
        x2s("\\g<1>|\\zEND(.a.)", "bac", 0, 3);
        x3s("\\g<_A>\\g<_A>|\\zEND(.a.)(?<_A>.b.)", "xbxyby", 3, 6, 1);
        x2s("\\A(?:\\g<pon>|\\g<pan>|\\zEND  (?<pan>a|c\\g<pon>c)(?<pon>b|d\\g<pan>d))$", "cdcbcdc", 0, 7);
        x2s("\\A(?<n>|a\\g<m>)\\z|\\zEND (?<m>\\g<n>)", "aaaa", 0, 4);
        x2s("(?<n>(a|b\\g<n>c){3,5})", "baaaaca", 1, 5);
        x2s("(?<n>(a|b\\g<n>c){3,5})", "baaaacaaaaa", 0, 10);
        x2s("(?<pare>\\(([^\\(\\)]++|\\g<pare>)*+\\))", "((a))", 0, 5);
        x2s("()*\\1", "", 0, 0);
        x2s("(?:()|())*\\1\\2", "", 0, 0);
        x3s("(?:\\1a|())*", "a", 0, 0, 1);
        x2s("x((.)*)*x", "0x1x2x3", 1, 6);
        x2s("x((.)*)*x(?i:\\1)\\Z", "0x1x2x1X2", 1, 9);
        x2s("(?:()|()|()|()|()|())*\\2\\5", "", 0, 0);
        x2s("(?:()|()|()|(x)|()|())*\\2b\\5", "b", 0, 1);
        
        x3s("\\A(?<a>|.|(?:(?<b>.)\\g<a>\\k<b+0>))\\z", "reer", 0, 4, 1);
        x3s("(?-i:\\g<name>)(?i:(?<name>a)){0}", "A", 0, 1, 1);
        
        String pat = 
            "(?<element> \\g<stag> \\g<content>* \\g<etag> ){0}" +
            "(?<stag> < \\g<name> \\s* > ){0}" +
            "(?<name> [a-zA-Z_:]+ ){0}" +
            "(?<content> [^<&]+ (\\g<element> | [^<&]+)* ){0}" +
            "(?<etag> </ \\k<name+1> >){0}" +
            "\\g<element>";

        String str = "<foo>f<bar>bbb</bar>f</foo>";
        
        x3s(pat, str, 0, 27, 0, Option.EXTEND);
        x3s(pat, str, 0, 27, 1, Option.EXTEND);
        x3s(pat, str, 6, 11, 2, Option.EXTEND);
        x3s(pat, str, 7, 10, 3, Option.EXTEND);
        x3s(pat, str, 5, 21, 4, Option.EXTEND);
        x3s(pat, str, 21, 27, 5, Option.EXTEND);

        x2s("(a)b\\k<1>", "aba", 0, 3);
    }
    
    public static void main(String[] args) throws Throwable{
        new TestA().run();
    }
}
