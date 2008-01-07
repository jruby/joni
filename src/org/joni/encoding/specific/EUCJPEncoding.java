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
import org.joni.encoding.EucEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.ValueException;
import org.joni.util.BytesHash;

public final class EUCJPEncoding extends EucEncoding  {

    protected EUCJPEncoding() {
        super(EUCJPEncLen, ASCIIEncoding.AsciiCtypeTable);
    }
    
    @Override
    public String toString() {
        return "EUC-JP";
    }
    
    @Override
    public int maxLength() {
        return 3;
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
    
    @Override
    public int codeToMbcLength(int code) {
        if (isAscii(code)) return 1;
        if ((code & 0xff0000) != 0) return 3;
        if ((code &   0xff00) != 0) return 2;
        throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
    }
    
    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff0000) != 0) bytes[p_++] = (byte)((code >> 16) & 0xff); // need mask here ??
        if ((code &   0xff00) != 0) bytes[p_++] = (byte)((code >>  8) & 0xff);
        bytes[p_++] = (byte)(code & 0xff);
        
        if (length(bytes[p_]) != p_ - p) throw new InternalException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);        
        return p_ - p;
    }
    
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        int p = pp.value;        
        int lowerP = 0;
        
        if (isMbcAscii(bytes[p])) {
            lower[lowerP] = ASCIIEncoding.AsciiToLowerCaseTable[bytes[p] & 0xff];
            pp.value++;
            return 1;
        } else {
            int len = length(bytes[p]);
            for (int i=0; i<len; i++) {
                lower[lowerP++] = bytes[p++];
            }
            pp.value += len;
            return len; /* return byte length of converted char to lower */
        }
    }
    
    protected boolean isLead(int c) {
        return ((c - 0xa1) & 0xff) > 0xfe - 0xa1;
    }
    
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        int c = bytes[p] & 0xff;
        return c <= 0x7e || c == 0x8e || c == 0x8f;
    }
    
    private static final int CR_Hiragana[] = {
        1,
        0xa4a1, 0xa4f3
    }; /* CR_Hiragana */
    
    private static final int CR_Katakana[] = {
        3,
        0xa5a1, 0xa5f6,
        0xaaa6, 0xaaaf,
        0xaab1, 0xaadd
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
    
    static final int EUCJPEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1
    };
    
    public static final EUCJPEncoding INSTANCE = new EUCJPEncoding();
}
