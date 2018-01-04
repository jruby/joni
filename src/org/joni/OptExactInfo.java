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

final class OptExactInfo {
    static final int OPT_EXACT_MAXLEN = 24;

    final MinMaxLen mmd = new MinMaxLen();
    final OptAnchorInfo anchor = new OptAnchorInfo();
    boolean reachEnd;
    int ignoreCase; /* -1: unset, 0: case sensitive, 1: ignore case */
    final byte bytes[] = new byte[OPT_EXACT_MAXLEN];
    int length;

    boolean isFull() {
        return length >= OPT_EXACT_MAXLEN;
    }

    void clear() {
        mmd.clear();
        anchor.clear();
        reachEnd = false;
        ignoreCase = -1;
        length = 0;
    }

    void copy(OptExactInfo other) {
        mmd.copy(other.mmd);
        anchor.copy(other.anchor);
        reachEnd = other.reachEnd;
        ignoreCase = other.ignoreCase;
        length = other.length;

        System.arraycopy(other.bytes, 0, bytes, 0, OPT_EXACT_MAXLEN);
    }

    void concat(OptExactInfo other, Encoding enc) {
        if (ignoreCase < 0) {
            ignoreCase = other.ignoreCase;
        } else if (ignoreCase != other.ignoreCase) {
            return;
        }

        int p = 0; // add->s;
        int end = p + other.length;

        int i;
        for (i = length; p < end;) {
            int len = enc.length(other.bytes, p, end);
            if (i + len > OPT_EXACT_MAXLEN) break;
            for (int j = 0; j < len && p < end; j++) {
                bytes[i++] = other.bytes[p++]; // arraycopy or even don't copy anything ??
            }
        }

        length = i;
        reachEnd = (p == end ? other.reachEnd : false);

        OptAnchorInfo tmp = new OptAnchorInfo();
        tmp.concat(anchor, other.anchor, 1, 1);
        if (!reachEnd) tmp.rightAnchor = 0;
        anchor.copy(tmp);
    }

    void concatStr(byte[]lbytes, int p, int end, boolean raw, Encoding enc) {
        int i;
        for (i = length; p < end && i < OPT_EXACT_MAXLEN;) {
            int len = enc.length(lbytes, p, end);
            if (i + len > OPT_EXACT_MAXLEN) break;
            for (int j = 0; j < len && p < end; j++) {
                bytes[i++] = lbytes[p++];
            }
        }
        length = i;
    }

    void altMerge(OptExactInfo other, OptEnvironment env) {
        if (other.length == 0 || length == 0) {
            clear();
            return;
        }

        if (!mmd.equal(other.mmd)) {
            clear();
            return;
        }

        int i;
        for (i=0; i<length && i<other.length;) {
            if (bytes[i] != other.bytes[i]) break;
            int len = env.enc.length(bytes, i, length);

            int j;
            for (j=1; j<len; j++) {
                if (bytes[i+j] != other.bytes[i+j]) break;
            }

            if (j < len) break;
            i += len;
        }

        if (!other.reachEnd || i<other.length || i<length) reachEnd = false;

        length = i;
        if (ignoreCase < 0) {
            ignoreCase = other.ignoreCase;
        } else if (other.ignoreCase >= 0) {
            ignoreCase |= other.ignoreCase;
        }

        anchor.altMerge(other.anchor);

        if (!reachEnd) anchor.rightAnchor = 0;
    }


    void select(OptExactInfo alt, Encoding enc) {
        int v1 = length;
        int v2 = alt.length;

        if (v2 == 0) {
            return;
        } else if (v1 == 0) {
            copy(alt);
            return;
        } else if (v1 <= 2 && v2 <= 2) {
            /* ByteValTable[x] is big value --> low price */
            v2 = OptMapInfo.positionValue(enc, bytes[0] & 0xff);
            v1 = OptMapInfo.positionValue(enc, alt.bytes[0] & 0xff);

            if (length > 1) v1 += 5;
            if (alt.length > 1) v2 += 5;
        }

        if (ignoreCase <= 0) v1 *= 2;
        if (alt.ignoreCase <= 0) v2 *= 2;

        if (mmd.compareDistanceValue(alt.mmd, v1, v2) > 0) copy(alt);
    }

    // comp_opt_exact_or_map_info
    private static final int COMP_EM_BASE   = 20;
    int compare(OptMapInfo m) {
        if (m.value <= 0) return -1;

        int ve = COMP_EM_BASE * length * (ignoreCase > 0 ? 1 : 2);
        int vm = COMP_EM_BASE * 5 * 2 / m.value;

        return mmd.compareDistanceValue(m.mmd, ve, vm);
    }
}
