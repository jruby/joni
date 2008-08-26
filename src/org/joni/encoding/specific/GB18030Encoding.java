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
import org.joni.encoding.MultiByteEncoding;
import org.joni.exception.IllegalCharacterException;

public final class GB18030Encoding extends MultiByteEncoding  {

    protected GB18030Encoding() {
        super(1, 4, null, GB18030Trans, ASCIIEncoding.AsciiCtypeTable);
    }

    @Override
    public String toString() {
        return "GB18030";
    }

    @Override
    public int length(byte[]bytes, int p, int end) {
        if (Config.VANILLA) {
            if (GB18030_MAP[bytes[p] & 0xff] != CM) return 1;
            int c = GB18030_MAP[bytes[p + 1] & 0xff];
            if (c == C4) return 4;
            if (c == C1) return 1; /* illegal sequence */
            return 2;
        } else {
            int s = TransZero[bytes[p] & 0xff];
            if (s < 0) {
                if (s == A) return 1;
                throw IllegalCharacterException.INSTANCE;
            }
            return lengthForTwoUptoFour(bytes, p, end, s);
        }
    }

    private int lengthForTwoUptoFour(byte[]bytes, int p, int end, int s) {
        if (++p == end) return -1;
        s = Trans[s][bytes[p] & 0xff];
        if (s < 0) {
            if (s == A) return 2;
            throw IllegalCharacterException.INSTANCE;
        }
        return lengthForThreeUptoFour(bytes, p, end, s);
    }

    private int lengthForThreeUptoFour(byte[]bytes, int p, int end, int s) {
        if (++p == end) return -2;
        s = Trans[s][bytes[p] & 0xff];
        if (s < 0) {
            if (s == A) return 3;
            throw IllegalCharacterException.INSTANCE;
        }
        if (++p == end) return -1;
        s = Trans[s][bytes[p] & 0xff];
        if (s == A) return 4;
        throw IllegalCharacterException.INSTANCE;  
    }    

    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        if (Config.VANILLA) {
            return mbnMbcToCode(bytes, p, end);
        } else {
            return mbnMbcToCode(bytes, p, end) & 0x7FFFFFFF;
        }
    }
    
    @Override
    public int codeToMbcLength(int code) {
        return mb4CodeToMbcLength(code);
    }

    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        if (Config.VANILLA) {
            return mb4CodeToMbc(code, bytes, p);
        } else {
            if ((code & 0xff000000) != 0) code |= 0x80000000;
            return mb4CodeToMbc(code, bytes, p);
        }
    }
    
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return mbnMbcCaseFold(flag, bytes, pp, end, lower);
    }
    
    @Override
    public boolean isCodeCType(int code, int ctype) {
        return mb4IsCodeCType(code, ctype);
    }
    
    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        return null;
    }

    private enum State {
        START,
        One_C2,
        One_C4,
        One_CM,

        Odd_CM_One_CX,
        Even_CM_One_CX,

        /* CMC4 : pair of "CM C4" */
        One_CMC4,
        Odd_CMC4,
        One_C4_Odd_CMC4,
        Even_CMC4,
        One_C4_Even_CMC4,

        Odd_CM_Odd_CMC4,
        Even_CM_Odd_CMC4,

        Odd_CM_Even_CMC4,
        Even_CM_Even_CMC4,

        /* C4CM : pair of "C4 CM" */
        Odd_C4CM,
        One_CM_Odd_C4CM,
        Even_C4CM,
        One_CM_Even_C4CM,

        Even_CM_Odd_C4CM,
        Odd_CM_Odd_C4CM,
        Even_CM_Even_C4CM,
        Odd_CM_Even_C4CM
    };

    @Override
    public int leftAdjustCharHead(byte[]bytes, int start, int s) {
        State state = State.START;
        
        for (int p = s; p >= start; p--) {
            switch (state) {
            case START:
                switch (GB18030_MAP[bytes[p] & 0xff]) {
                case C1:    return s;
                case C2:    state = State.One_C2; /* C2 */
                    break;
                case C4:    state = State.One_C4; /* C4 */
                    break;
                case CM:    state = State.One_CM; /* CM */
                    break;
                }
                break;
                case One_C2: /* C2 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return s;
                    case CM:    state = State.Odd_CM_One_CX; /* CM C2 */
                        break;
                    }
                    break;
                case One_C4: /* C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return s;
                    case CM:    state = State.One_CMC4;
                        break;
                    }
                    break;
                case One_CM: /* CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return s;
                    case C4:    state = State.Odd_C4CM;
                        break;
                    case CM:    state = State.Odd_CM_One_CX; /* CM CM */
                        break;
                    }
                    break;
                case Odd_CM_One_CX: /* CM C2 */ /* CM CM */ /* CM CM CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 1);
                    case CM:    state = State.Even_CM_One_CX;
                        break;
                    }
                    break;
                case Even_CM_One_CX: /* CM CM C2 */ /* CM CM CM */ /* CM CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return s;
                    case CM:    state = State.Odd_CM_One_CX;
                        break;
                    }
                    break;
                case One_CMC4: /* CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return (s - 1);
                    case C4:    state = State.One_C4_Odd_CMC4; /* C4 CM C4 */
                        break;
                    case CM:    state = State.Even_CM_One_CX; /* CM CM C4 */
                        break;
                    }
                    break;
                case Odd_CMC4: /* CM C4 CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return (s - 1);
                    case C4:    state = State.One_C4_Odd_CMC4;
                        break;
                    case CM:    state = State.Odd_CM_Odd_CMC4;
                        break;
                    }
                    break;
                case One_C4_Odd_CMC4: /* C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 1);
                    case CM:    state = State.Even_CMC4; /* CM C4 CM C4 */
                        break;
                    }
                    break;
                case Even_CMC4: /* CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return (s - 3);
                    case C4:    state = State.One_C4_Even_CMC4;
                        break;
                    case CM:    state = State.Odd_CM_Even_CMC4;
                        break;
                    }
                    break;
                case One_C4_Even_CMC4: /* C4 CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 3);
                    case CM:    state = State.Odd_CMC4;
                        break;
                    }
                    break;
                case Odd_CM_Odd_CMC4: /* CM CM C4 CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 3);
                    case CM:    state = State.Even_CM_Odd_CMC4;
                        break;
                    }
                    break;
                case Even_CM_Odd_CMC4: /* CM CM CM C4 CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 1);
                    case CM:    state = State.Odd_CM_Odd_CMC4;
                        break;
                    }
                    break;
                case Odd_CM_Even_CMC4: /* CM CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 1);
                    case CM:    state = State.Even_CM_Even_CMC4;
                        break;
                    }
                    break;
                case Even_CM_Even_CMC4: /* CM CM CM C4 CM C4 */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 3);
                    case CM:    state = State.Odd_CM_Even_CMC4;
                        break;
                    }
                    break;
                case Odd_C4CM: /* C4 CM */  /* C4 CM C4 CM C4 CM*/
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return s;
                    case CM:    state = State.One_CM_Odd_C4CM; /* CM C4 CM */
                        break;
                    }
                    break;
                case One_CM_Odd_C4CM: /* CM C4 CM */ /* CM C4 CM C4 CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return (s - 2); /* |CM C4 CM */
                    case C4:    state = State.Even_C4CM;
                        break;
                    case CM:    state = State.Even_CM_Odd_C4CM;
                        break;
                    }
                    break;
                case Even_C4CM: /* C4 CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 2);  /* C4|CM C4 CM */
                    case CM:    state = State.One_CM_Even_C4CM;
                        break;
                    }
                    break;
                case One_CM_Even_C4CM: /* CM C4 CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:    return (s - 0);  /*|CM C4 CM C4|CM */
                    case C4:    state = State.Odd_C4CM;
                        break;
                    case CM:    state = State.Even_CM_Even_C4CM;
                        break;
                    }
                    break;
                case Even_CM_Odd_C4CM: /* CM CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 0); /* |CM CM|C4|CM */
                    case CM:    state = State.Odd_CM_Odd_C4CM;
                        break;
                    }
                    break;
                case Odd_CM_Odd_C4CM: /* CM CM CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 2); /* |CM CM|CM C4 CM */
                    case CM:    state = State.Even_CM_Odd_C4CM;
                        break;
                    }
                    break;
                case Even_CM_Even_C4CM: /* CM CM C4 CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 2); /* |CM CM|C4|CM C4 CM */
                    case CM:    state = State.Odd_CM_Even_C4CM;
                        break;
                    }
                    break;
                case Odd_CM_Even_C4CM: /* CM CM CM C4 CM C4 CM */
                    switch (GB18030_MAP[bytes[p] & 0xff]) {
                    case C1:
                    case C2:
                    case C4:    return (s - 0);  /* |CM CM|CM C4 CM C4|CM */
                    case CM:    state = State.Even_CM_Even_C4CM;
                        break;
                    }
                    break;
            }
        }
        
        switch (state) {
        case START:             return (s - 0);
        case One_C2:            return (s - 0);
        case One_C4:            return (s - 0);
        case One_CM:            return (s - 0);

        case Odd_CM_One_CX:     return (s - 1);
        case Even_CM_One_CX:    return (s - 0);

        case One_CMC4:          return (s - 1);
        case Odd_CMC4:          return (s - 1);
        case One_C4_Odd_CMC4:   return (s - 1);
        case Even_CMC4:         return (s - 3);
        case One_C4_Even_CMC4:  return (s - 3);

        case Odd_CM_Odd_CMC4:   return (s - 3);
        case Even_CM_Odd_CMC4:  return (s - 1);

        case Odd_CM_Even_CMC4:  return (s - 1);
        case Even_CM_Even_CMC4: return (s - 3);

        case Odd_C4CM:          return (s - 0);
        case One_CM_Odd_C4CM:   return (s - 2);
        case Even_C4CM:         return (s - 2);
        case One_CM_Even_C4CM:  return (s - 0);

        case Even_CM_Odd_C4CM:  return (s - 0);
        case Odd_CM_Odd_C4CM:   return (s - 2);
        case Even_CM_Even_C4CM: return (s - 2);
        case Odd_CM_Even_C4CM:  return (s - 0);
        }

        return s;
    }
    
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return GB18030_MAP[bytes[p] & 0xff] == C1;
    }

    private static final int C1 = 0; /* one-byte char */
    private static final int C2 = 1; /* one-byte or second of two-byte char */
    private static final int C4 = 2; /* one-byte or second or fourth of four-byte char */
    private static final int CM = 3; /* first of two- or four-byte char or second of two-byte char */

    private static final int GB18030_MAP[] = {
        C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1,
        C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1,
        C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1, C1,
        C4, C4, C4, C4, C4, C4, C4, C4, C4, C4, C1, C1, C1, C1, C1, C1,
        C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2,
        C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2,
        C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2,
        C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C2, C1,
        C2, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM,
        CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, CM, C1
    };

    private static final int GB18030Trans[][] = Config.VANILLA ? null : new int[][]{
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
          /* a */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
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
          /* 3 */ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, F, F, F, F, F, F,
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
          /* f */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, F 
        },
        { /* S2   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* 9 */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* a */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* b */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* c */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* d */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* e */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          /* f */ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, F 
        },
        { /* S3   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ A, A, A, A, A, A, A, A, A, A, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F 
        }
    };

    public static final GB18030Encoding INSTANCE = new GB18030Encoding();
}
