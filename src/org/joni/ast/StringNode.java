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
import org.joni.constants.StringType;

public final class StringNode extends Node implements StringType {
    
    private static final int NODE_STR_MARGIN = 16;
    private static final int NODE_STR_BUF_SIZE = 24;
    
    public byte[]bytes;
    public int p;
    public int end;
    
    int flag;
    
    public StringNode() {
        this.bytes = new byte[NODE_STR_BUF_SIZE];
    }

    public StringNode(byte[]bytes, int p, int end) {
        this.bytes = bytes;
        this.p = p;
        this.end = end;
        setShared();
    }
    
    public StringNode(byte c) {
        this();
        bytes[end++] = c;
    }

    /* Ensure there is ahead bytes available in node's buffer
     * (assumes that the node is not shared)
     */
    public void ensure(int ahead) {
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
        int len = (end - p) + ahead;
        if (isShared()) {
            byte[]tmp = new byte[len + NODE_STR_MARGIN];
            System.arraycopy(bytes, p, tmp, 0, end - p);
            bytes = tmp;
            end = end - p;
            p = 0;
            clearShared();
        } else {
            if (len >= bytes.length) {
                byte[]tmp = new byte[len + NODE_STR_MARGIN];
                System.arraycopy(bytes, p, tmp, 0, end - p);
                bytes = tmp;
            }
        }
    }
    
    @Override
    public int getType() {
        return STR;
    }
    
    @Override
    public String getName() {
        return "String";
    }   
    
    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder();
        value.append("\n  bytes: ");
        for (int i=p; i<end; i++) {
            if ((bytes[i] & 0xff) >= 0x20 && (bytes[i] & 0xff) < 0x7f) {
                value.append((char)bytes[i]);
            } else {
                value.append(String.format(" 0x%02x", bytes[i]));
            }
        }
        return value.toString();
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
            if (prev != -1 && prev > p) { /* can be splitted. */
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
    
    public void cat(byte[]cat, int catP, int catEnd) {
        int len = catEnd - catP;
        modifyEnsure(len);
        System.arraycopy(cat, catP, bytes, end, len);
        end += len;
    }
    
    public void cat(byte c) {
        modifyEnsure(1);
        bytes[end++] = c;
    }
    
    public void clear() {
        if (bytes.length > NODE_STR_BUF_SIZE) bytes = new byte[NODE_STR_BUF_SIZE];
        flag = 0;
        p = end = 0;
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
}
