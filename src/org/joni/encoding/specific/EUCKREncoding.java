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

import org.joni.Config;
import org.joni.IntHolder;
import org.joni.encoding.EucEncoding;

public final class EUCKREncoding extends EucEncoding {

    protected EUCKREncoding() {
        super(1, 2, EUCKREncLen, EUCKRTrans, ASCIIEncoding.AsciiCtypeTable);
    }
    
    @Override
    public String toString() {
        return "EUC-KR";
    }

    @Override
    public int length(byte[]bytes, int p, int end) {
        if (Config.VANILLA){
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
        return mb2CodeToMbcLength(code);
    }
    
    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        return mb2CodeToMbc(code, bytes, p);
    }
    
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return mbnMbcCaseFold(flag, bytes, pp, end, lower);
    }
    
    @Override
    public boolean isCodeCType(int code, int ctype) {
        return mb2IsCodeCType(code, ctype);
    }
    
    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        return null;
    }
    
    // euckr_islead
    protected boolean isLead(int c) {
        return ((c) < 0xa1 || (c) == 0xff);
    }
    
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        int c = bytes[p] & 0xff;
        return c <= 0x7e;
    }
    
    static final int EUCKREncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
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
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1
    };

    private static final int EUCKRTrans[][] = Config.VANILLA ? null : new int[][]{
        { /* S0   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 1 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 2 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 3 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 4 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 5 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 6 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 7 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* d */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* e */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* f */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, F 
        },
        { /* S1   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* b */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* c */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* d */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* e */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* f */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, F 
        }
    };
    
    public static final EUCKREncoding INSTANCE = new EUCKREncoding();
}
