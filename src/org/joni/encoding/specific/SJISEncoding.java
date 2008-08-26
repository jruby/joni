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
import org.joni.Config;
import org.joni.IntHolder;
import org.joni.constants.CharacterType;
import org.joni.encoding.CanBeTrailTableEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.ValueException;
import org.joni.util.BytesHash;

public final class SJISEncoding extends CanBeTrailTableEncoding {

    protected SJISEncoding() {
        super(1, 2, SjisEncLen, SjisTrans, ASCIIEncoding.AsciiCtypeTable, SJIS_CAN_BE_TRAIL_TABLE);
    }

    @Override
    public String toString() {
        return "Shift_JIS";
    }

    @Override
    public int length(byte[]bytes, int p, int end) {
        if (Config.VANILLA) {
            return length(bytes[p]);
        } else {
            return safeLengthForUptoTwo(bytes, p, end);
        }
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
            throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);            
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
                    if (Config.VANILLA) {
                        return codeToMbcLength(code) > 1;
                    } else {
                        return true;
                    }
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
    
    private static final int SjisTrans[][] = Config.VANILLA ? null : new int[][]{
        { /* S0   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 1 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 2 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 3 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 4 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 5 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 6 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 7 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 8 */ F, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* 9 */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* a */ F, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* b */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* c */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* d */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* e */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* f */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, F, F, F
        },
        { /* S1   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 5 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 6 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 7 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, F,
          /* 8 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 9 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* a */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* b */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* c */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* d */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* e */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* f */ A, A, A, A, A, A, A, A, A, A, A, A, A, F, F, F
        }
    };
    
    public static final SJISEncoding INSTANCE = new SJISEncoding();
}
