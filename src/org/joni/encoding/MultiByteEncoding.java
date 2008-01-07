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
package org.joni.encoding;

import org.joni.IntHolder;
import org.joni.encoding.specific.ASCIIEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;

public abstract class MultiByteEncoding extends AbstractEncoding {
    
    protected final int EncLen[];
    
    protected MultiByteEncoding(int[]EncLen, short[]CTypeTable) {
        super(CTypeTable);
        this.EncLen = EncLen;
    }
    
    @Override
    public int length(byte c) { 
        return EncLen[c & 0xff];       
    }
    
    @Override
    public boolean isSingleByte() {
        return false;
    }
    
    protected final int mbnMbcToCode(byte[]bytes, int p, int end) {
        int len = length(bytes[p]);
        int n = bytes[p++] & 0xff;
        if (len == 1) return n;
        
        for (int i=1; i<len; i++) {
            if (p >= end) break;
            int c = bytes[p++] & 0xff;
            n <<= 8;
            n += c;
        }
        return n;
    }
    
    protected final int mbnMbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        int p = pp.value;
        int lowerP = 0;
        
        if (isAscii(bytes[p] & 0xff)) {
            lower[lowerP] = ASCIIEncoding.AsciiToLowerCaseTable[bytes[p] & 0xff];
            pp.value++;
            return 1;
        } else {
            int len = length(bytes[p]);
            for (int i=0; i<len; i++) {
                lower[lowerP++] = bytes[p++];
            }
            pp.value += len;
            return len; /* return byte length of converted to lower char */
        }
    }

    protected final int mb2CodeToMbcLength(int code) {
        return ((code & 0xff00) != 0) ? 2 : 1;
    }
    
    protected final int mb4CodeToMbcLength(int code) {
        if ((code & 0xff000000) != 0) {
            return 4;
        } else if ((code & 0xff0000) != 0) {
            return 3;
        } else if ((code & 0xff00) != 0) {
            return 2;
        } else { 
            return 1;
        }
    }
    
    protected final int mb2CodeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff00) != 0) {
            bytes[p_++] = (byte)((code >>> 8) & 0xff);
        }
        bytes[p_++] = (byte)(code & 0xff);
        
        if (length(bytes[p]) != (p_ - p)) throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
        return p_ - p;
    }
    
    protected final int mb4CodeToMbc(int code, byte[]bytes, int p) {        
        int p_ = p;        
        if ((code & 0xff000000) != 0)           bytes[p_++] = (byte)((code >>> 24) & 0xff);
        if ((code & 0xff0000) != 0 || p_ != p)  bytes[p_++] = (byte)((code >>> 16) & 0xff);
        if ((code & 0xff00) != 0 || p_ != p)    bytes[p_++] = (byte)((code >>> 8) & 0xff);
        bytes[p_++] = (byte)(code & 0xff);
        
        if (length(bytes[p]) != (p_ - p)) throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
        return p_ - p;
    }
    
    protected final boolean mb2IsCodeCType(int code, int ctype) {
        if (code < 128) {            
            return isCodeCTypeInternal(code, ctype); // configured with ascii
        } else {
            if (isWordGraphPrint(ctype)) {
                return codeToMbcLength(code) > 1;
            }
        }
        return false;
    }
    
    protected final boolean mb4IsCodeCType(int code, int ctype) {
        return mb2IsCodeCType(code, ctype);
    }
    
}
