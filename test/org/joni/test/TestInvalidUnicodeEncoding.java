/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.jcodings.specific.UTF16LEEncoding;
import org.jcodings.specific.UTF32BEEncoding;
import org.jcodings.specific.UTF32LEEncoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.JOniException;
import org.junit.Test;

public class TestInvalidUnicodeEncoding {
    private static final Encoding[] ENCODINGS = {
        UTF16LEEncoding.INSTANCE,
        UTF16BEEncoding.INSTANCE,
        UTF32LEEncoding.INSTANCE,
        UTF32BEEncoding.INSTANCE
    };

    private static final String[] CHARSETS = {
        "UTF-16LE",
        "UTF-16BE",
        "UTF-32LE",
        "UTF-32BE"
    };

    @Test
    public void invalidEscapedCodepointsAreRejected() throws Exception {
        String[] invalidPatterns = {
            "\\x{D801}",
            "a\\x{D801}",
            "a\\x{110000}",
            "[\\x{D801}]",
            "\\x{110000}"
        };

        for (int i = 0; i < ENCODINGS.length; i++) {
            for (String source : invalidPatterns) {
                assertInvalidPattern(source, CHARSETS[i], ENCODINGS[i]);
            }
            assertValidSupplementaryCodepoint(CHARSETS[i], ENCODINGS[i]);
        }
    }

    private static void assertInvalidPattern(String source, String charset, Encoding encoding)
            throws UnsupportedEncodingException {
        byte[] pattern = source.getBytes(charset);
        try {
            new Regex(pattern, 0, pattern.length, Option.DEFAULT, encoding, Syntax.RUBY);
            fail("Expected invalid Unicode code point to be rejected in " + charset + ": " + source);
        } catch (JOniException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("invalid unicode:"));
        }
    }

    private static void assertValidSupplementaryCodepoint(String charset, Encoding encoding)
            throws Exception {
        byte[] pattern = "\\x{1F600}".getBytes(charset);
        byte[] subject = "\uD83D\uDE00".getBytes(charset);
        Regex regex = new Regex(pattern, 0, pattern.length, Option.DEFAULT, encoding, Syntax.RUBY);
        Matcher matcher = regex.matcher(subject, 0, subject.length);
        assertEquals(0, matcher.search(0, subject.length, Option.DEFAULT));
    }
}
