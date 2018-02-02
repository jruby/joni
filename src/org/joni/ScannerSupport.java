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
package org.joni;

import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.SyntaxException;
import org.joni.exception.ValueException;

abstract class ScannerSupport extends IntHolder implements ErrorMessages {
    protected final Encoding enc;       // fast access to encoding
    protected final byte[]bytes;        // pattern
    protected int p;                    // current scanner position
    protected int stop;                 // pattern end (mutable)
    private int lastFetched;            // last fetched value for unfetch support
    protected int c;                    // current code point

    private final int begin;            // pattern begin position for reset() support
    private final int end;              // pattern end position for reset() support
    protected int _p;                   // used by mark()/restore() to mark positions

    protected ScannerSupport(Encoding enc, byte[]bytes, int p, int end) {
        this.enc = enc;
        this.bytes = bytes;
        this.begin = p;
        this.end = end;
    }

    protected final int getBegin() {
        return begin;
    }

    protected final int getEnd() {
        return end;
    }

    private static final int INT_SIGN_BIT = 1 << 31;
    protected final int scanUnsignedNumber() {
        int last = c;
        int num = 0; // long ???
        while(left()) {
            fetch();
            if (enc.isDigit(c)) {
                int onum = num;
                num = num * 10 + Encoding.digitVal(c);
                if (((onum ^ num) & INT_SIGN_BIT) != 0) return -1;
            } else {
                unfetch();
                break;
            }
        }
        c = last;
        return num;
    }

    protected final int scanUnsignedHexadecimalNumber(int minLength, int maxLength) {
        int last = c;
        int num = 0;
        int restLen = maxLength - minLength;
        while(left() && maxLength-- != 0) {
            fetch();
            if (enc.isXDigit(c)) {
                int val = enc.xdigitVal(c);
                if ((Integer.MAX_VALUE - val) / 16 < num) return -1;
                num = (num << 4) + val;
            } else {
                unfetch();
                maxLength++;
                break;
            }
        }
        if (maxLength > restLen) return -2;
        c = last;
        return num;
    }

    protected final int scanUnsignedOctalNumber(int maxLength) {
        int last = c;
        int num = 0;
        while(left() && maxLength-- != 0) {
            fetch();
            if (enc.isDigit(c) && c < '8') {
                int onum = num;
                int val = Encoding.odigitVal(c);
                num = (num << 3) + val;
                if (((onum ^ num) & INT_SIGN_BIT) != 0) return -1;
            } else {
                unfetch();
                break;
            }
        }
        c = last;
        return num;
    }

    protected final void reset() {
        p = begin;
        stop = end;
    }

    protected final void mark() {
        _p = p;
    }

    protected final void restore() {
        p = _p;
    }

    protected final void inc() {
        lastFetched = p;
        p += enc.length(bytes, p, stop);
    }

    protected final void fetch() {
        c = enc.mbcToCode(bytes, p, stop);
        lastFetched = p;
        p += enc.length(bytes, p, stop);
    }

    protected int fetchTo() {
        int to = enc.mbcToCode(bytes, p, stop);
        lastFetched = p;
        p += enc.length(bytes, p, stop);
        return to;
    }

    protected final void unfetch() {
        p = lastFetched;
    }

    protected final int peek() {
        return p < stop ? enc.mbcToCode(bytes, p, stop) : 0;
    }

    protected final boolean peekIs(int c) {
        return peek() == c;
    }

    protected final boolean left() {
        return p < stop;
    }

    protected void newSyntaxException(String message) {
        throw new SyntaxException(message);
    }

    protected void newValueException(String message) {
        throw new ValueException(message);
    }

    protected void newValueException(String message, String str) {
        throw new ValueException(message, str);
    }

    protected void newValueException(String message, int p, int end) {
        throw new ValueException(message, new String(bytes, p, end - p));
    }

    protected void newInternalException(String message) {
        throw new InternalException(message);
    }

}
