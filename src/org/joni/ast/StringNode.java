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
package org.joni.ast;

import org.jcodings.Encoding;
import org.joni.Config;
import org.joni.constants.internal.StringType;

public final class StringNode extends Node implements StringType {
    private static final int NODE_STR_MARGIN = 16;
    private static final int NODE_STR_BUF_SIZE = 24;
    public static final StringNode EMPTY = new StringNode(null, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public byte[]bytes;
    public int p;
    public int end;
    public int flag;

    public StringNode(int size) {
        super(STR);
        this.bytes = new byte[size];
    }

    public StringNode() {
        this(NODE_STR_BUF_SIZE);
    }

    public static StringNode fromCodePoint(int code, Encoding enc) {
        StringNode str = new StringNode(Config.ENC_CODE_TO_MBC_MAXLEN);
        str.end = enc.codeToMbc(code, str.bytes, str.p);
        return str;
    }

    public StringNode(byte[]bytes, int p, int end) {
        super(STR);
        this.bytes = bytes;
        this.p = p;
        this.end = end;
        setShared();
    }

    /* Ensure there is ahead bytes available in node's buffer
     * (assumes that the node is not shared)
     */
    private void ensure(int ahead) {
        int len = (end - p) + ahead;
        if (len >= bytes.length) {
            byte[]tmp = new byte[len + NODE_STR_MARGIN];
            System.arraycopy(bytes, p, tmp, 0, end - p);
            bytes = tmp;
        }
    }

    /* COW and/or ensure there is ahead bytes available in node's buffer
     */
    private void modifyEnsure(int ahead) {
        if (isShared()) {
            int len = (end - p) + ahead;
            byte[]tmp = new byte[len + NODE_STR_MARGIN];
            System.arraycopy(bytes, p, tmp, 0, end - p);
            bytes = tmp;
            end = end - p;
            p = 0;
            clearShared();
        } else {
            ensure(ahead);
        }
    }

    @Override
    public String getName() {
        return "String";
    }

    public int length() {
        return end - p;
    }

    public int length(Encoding enc) {
        return enc.strLength(bytes, p, end);
    }

    public StringNode splitLastChar(Encoding enc) {
        StringNode n = null;
        if (end > p) {
            int prev = enc.prevCharHead(bytes, p, end, end);
            if (prev != -1 && prev > p) { /* can be split */
                n = new StringNode(bytes, prev, end);
                if (isRaw()) n.setRaw();
                end = prev;
            }
        }
        return n;
    }

    public boolean canBeSplit(Encoding enc) {
        if (end > p) {
            return enc.length(bytes, p, end) < (end - p);
        }
        return false;
    }

    public void set(byte[]bytes, int p, int end) {
        this.bytes = bytes;
        this.p = p;
        this.end = end;
        setShared();
    }

    public void catBytes(byte[]cat, int catP, int catEnd) {
        int len = catEnd - catP;
        modifyEnsure(len);
        System.arraycopy(cat, catP, bytes, end, len);
        end += len;
    }

    public void catByte(byte c) {
        modifyEnsure(1);
        bytes[end++] = c;
    }

    public void catCode(int code, Encoding enc) {
        modifyEnsure(Config.ENC_CODE_TO_MBC_MAXLEN);
        end += enc.codeToMbc(code, bytes, end);
    }

    public void setRaw() {
        flag |= NSTR_RAW;
    }

    public void clearRaw() {
        flag &= ~NSTR_RAW;
    }

    public boolean isRaw() {
        return (flag & NSTR_RAW) != 0;
    }

    public void setAmbig() {
        flag |= NSTR_AMBIG;
    }

    public void clearAmbig() {
        flag &= ~NSTR_AMBIG;
    }

    public boolean isAmbig() {
        return (flag & NSTR_AMBIG) != 0;
    }

    public void setDontGetOptInfo() {
        flag |= NSTR_DONT_GET_OPT_INFO;
    }

    public void clearDontGetOptInfo() {
        flag &= ~NSTR_DONT_GET_OPT_INFO;
    }

    public boolean isDontGetOptInfo() {
        return (flag & NSTR_DONT_GET_OPT_INFO) != 0;
    }

    public void setShared() {
        flag |= NSTR_SHARED;
    }

    public void clearShared() {
        flag &= ~NSTR_SHARED;
    }

    public boolean isShared() {
        return (flag & NSTR_SHARED) != 0;
    }

    public String flagsToString() {
        StringBuilder flags = new StringBuilder();
        if (isRaw()) flags.append("RAW ");
        if (isAmbig()) flags.append("AMBIG ");
        if (isDontGetOptInfo()) flags.append("DONT_GET_OPT_INFO ");
        if (isShared()) flags.append("SHARED ");
        return flags.toString();
    }

    @Override
    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  flags: " + flagsToString());
        sb.append("\n  bytes: '");
        for (int i=p; i<end; i++) {
            if ((bytes[i] & 0xff) >= 0x20 && (bytes[i] & 0xff) < 0x7f) {
                sb.append((char)bytes[i]);
            } else {
                sb.append(String.format("[0x%02x]", bytes[i]));
            }
        }
        sb.append("'");
        return sb.toString();
    }
}
