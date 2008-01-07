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
package org.joni.encoding.specific;

import org.joni.CodeRangeBuffer;
import org.joni.IntHolder;
import org.joni.constants.CharacterType;
import org.joni.encoding.MultiByteEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.util.BytesHash;

public final class SJISEncoding extends MultiByteEncoding  {

    protected SJISEncoding() {
        super(SjisEncLen, ASCIIEncoding.AsciiCtypeTable);
    }
    
    @Override
    public String toString() {
        return "Shift_JIS";
    }
    
    @Override
    public int maxLength() {
        return 2;
    }
    
    @Override
    public int minLength() {
        return 1;
    }
    
    @Override
    public boolean isFixedWidth() {
        return false;
    }
    
    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return mbnMbcToCode(bytes, p, end);
    }
    
    @Override
    public int codeToMbcLength(int code) {
        if (code < 256) {
            return SjisEncLen[code] == 1 ? 1 : 0;
        } else if (code <= 0xffff) {
            return 2;
        } else {
            throw new InternalException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);            
        }
    }

    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff00) != 0) bytes[p_++] = (byte)(((code >>  8) & 0xff));
        bytes[p_++] = (byte)(code & 0xff);
        return p_ - p;
    }
    
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return mbnMbcCaseFold(flag, bytes, pp, end, lower);
    }
    
    static final boolean SJIS_CAN_BE_TRAIL_TABLE[] = {
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false
    };
    
    private static boolean isSjisMbFirst(int b) { 
        return SjisEncLen[b] > 1;
    }
    
    private static boolean isSjisMbTrail(int b) {
        return SJIS_CAN_BE_TRAIL_TABLE[b];
    }
    
    @Override
    public int leftAdjustCharHead(byte[]bytes, int p, int end) {
        if (end <= p) return end;
        
        int p_ = end;
        
        if (isSjisMbTrail(bytes[p_] & 0xff)) {
            while (p_ > p) {
                if (!isSjisMbFirst(bytes[--p_] & 0xff)) {
                    p_++;
                    break;
                }
            }
        }
        int len = length(bytes[p_]);
        if (p_ + len > end) return p_;
        p_ += len;
        return p_ + ((end - p_) & ~1);
    }    
    
    @Override    
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        int c = bytes[p] & 0xff;
        return isSjisMbTrail(c);
    }
    
    private static final int CR_Hiragana[] = {
        1,
        0x829f, 0x82f1
    }; /* CR_Hiragana */
    
    private static final int CR_Katakana[] = {
        4,
        0x00a6, 0x00af,
        0x00b1, 0x00dd,
        0x8340, 0x837e,
        0x8380, 0x8396,
    }; /* CR_Katakana */
    
    private static final int PropertyList[][] = new int[][] {
        CR_Hiragana,
        CR_Katakana
    };
    
    private static final BytesHash<Integer> CTypeNameHash = new BytesHash<Integer>();

    static {
        CTypeNameHash.put("Hiragana".getBytes(), 1 + CharacterType.MAX_STD_CTYPE);
        CTypeNameHash.put("Katakana".getBytes(), 2 + CharacterType.MAX_STD_CTYPE);        
    }
    
    @Override
    public int propertyNameToCType(byte[]bytes, int p, int end) {
        Integer ctype;
        if ((ctype = CTypeNameHash.get(bytes, p, end)) == null) {
            return super.propertyNameToCType(bytes, p, end);
        }
        return ctype;
    }
    
    
    @Override
    public boolean isCodeCType(int code, int ctype) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            if (code < 128) { 
                // ctype table is configured with ASCII
                return isCodeCTypeInternal(code, ctype);
            } else {
                if (isWordGraphPrint(ctype)) {
                    return codeToMbcLength(code) > 1;
                }
            }
        } else {
            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return CodeRangeBuffer.isInCodeRange(PropertyList[ctype], code);
        }
        return false;        
    }
    
    @Override 
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            return null;
        } else {
            sbOut.value = 0x80;
            
            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return PropertyList[ctype];
        }
    }    
    
    static final int SjisEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1
    };
    
    public static final SJISEncoding INSTANCE = new SJISEncoding();
}
