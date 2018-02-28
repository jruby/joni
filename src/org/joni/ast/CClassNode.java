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

import org.jcodings.CodeRange;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.jcodings.constants.CharacterType;
import org.joni.BitSet;
import org.joni.CodeRangeBuffer;
import org.joni.ScanEnvironment;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.SyntaxException;
import org.joni.exception.ValueException;

public final class CClassNode extends Node {
    private static final int FLAG_NCCLASS_NOT = 1 << 0;

    private int flags;
    public final BitSet bs = new BitSet();  // conditional creation ?
    public CodeRangeBuffer mbuf;            /* multi-byte info or NULL */

    // node_new_cclass
    public CClassNode() {
        super(CCLASS);
    }

    public void clear() {
        bs.clear();
        flags = 0;
        mbuf = null;
    }

    @Override
    public String getName() {
        return "Character Class";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder();
        value.append("\n  flags: " + flagsToString());
        value.append("\n  bs: " + pad(bs, level + 1));
        value.append("\n  mbuf: " + pad(mbuf, level + 1));
        return value.toString();
    }

    public String flagsToString() {
        StringBuilder flags = new StringBuilder();
        if (isNot()) flags.append("NOT ");
        return flags.toString();
    }

    public boolean isEmpty() {
        return mbuf == null && bs.isEmpty();
    }

    void addCodeRangeToBuf(ScanEnvironment env, int from, int to) {
        addCodeRangeToBuf(env, from, to, true);
    }

    void addCodeRangeToBuf(ScanEnvironment env, int from, int to, boolean checkDup) {
        mbuf = CodeRangeBuffer.addCodeRangeToBuff(mbuf, env, from, to, checkDup);
    }

    // add_code_range, be aware of it returning null!
    public void addCodeRange(ScanEnvironment env, int from, int to) {
        addCodeRange(env, from, to, true);
    }

    public void addCodeRange(ScanEnvironment env, int from, int to, boolean checkDup) {
        mbuf = CodeRangeBuffer.addCodeRange(mbuf, env, from, to, checkDup);
    }

    void addAllMultiByteRange(ScanEnvironment env) {
        mbuf = CodeRangeBuffer.addAllMultiByteRange(env, mbuf);
    }

    public void clearNotFlag(ScanEnvironment env) {
        if (isNot()) {
            bs.invert();
            if (!env.enc.isSingleByte()) {
                mbuf = CodeRangeBuffer.notCodeRangeBuff(env, mbuf);
            }
            clearNot();
        }
    }

    public int isOneChar() {
        if (isNot()) return -1;
        int c = -1;
        if (mbuf != null) {
            int[]range = mbuf.getCodeRange();
            c = range[1];
            if (range[0] == 1 && c == range[2]) {
                if (c < BitSet.SINGLE_BYTE_SIZE && bs.at(c)) {
                    c = -1;
                }
            } else {
                return -1;
            }
        }

        for (int i = 0; i < BitSet.BITSET_SIZE; i++) {
            int b1 = bs.bits[i];
            if (b1 != 0) {
                if ((b1 & (b1 - 1)) == 0 && c == -1) {
                    c = BitSet.BITS_IN_ROOM * i + Integer.bitCount(b1 - 1);
                } else {
                    return -1;
                }
            }
        }
        return c;
    }

    // and_cclass
    public void and(CClassNode other, ScanEnvironment env) {
        boolean not1 = isNot();
        BitSet bsr1 = bs;
        CodeRangeBuffer buf1 = mbuf;
        boolean not2 = other.isNot();
        BitSet bsr2 = other.bs;
        CodeRangeBuffer buf2 = other.mbuf;

        if (not1) {
            BitSet bs1 = new BitSet();
            bsr1.invertTo(bs1);
            bsr1 = bs1;
        }

        if (not2) {
            BitSet bs2 = new BitSet();
            bsr2.invertTo(bs2);
            bsr2 = bs2;
        }

        bsr1.and(bsr2);

        if (bsr1 != bs) {
            bs.copy(bsr1);
            bsr1 = bs;
        }

        if (not1) {
            bs.invert();
        }

        CodeRangeBuffer pbuf = null;

        if (!env.enc.isSingleByte()) {
            if (not1 && not2) {
                pbuf = CodeRangeBuffer.orCodeRangeBuff(env, buf1, false, buf2, false);
            } else {
                pbuf = CodeRangeBuffer.andCodeRangeBuff(buf1, not1, buf2, not2, env);

                if (not1) {
                    pbuf = CodeRangeBuffer.notCodeRangeBuff(env, pbuf);
                }
            }
            mbuf = pbuf;
        }

    }

    // or_cclass
    public void or(CClassNode other, ScanEnvironment env) {
        boolean not1 = isNot();
        BitSet bsr1 = bs;
        CodeRangeBuffer buf1 = mbuf;
        boolean not2 = other.isNot();
        BitSet bsr2 = other.bs;
        CodeRangeBuffer buf2 = other.mbuf;

        if (not1) {
            BitSet bs1 = new BitSet();
            bsr1.invertTo(bs1);
            bsr1 = bs1;
        }

        if (not2) {
            BitSet bs2 = new BitSet();
            bsr2.invertTo(bs2);
            bsr2 = bs2;
        }

        bsr1.or(bsr2);

        if (bsr1 != bs) {
            bs.copy(bsr1);
            bsr1 = bs;
        }

        if (not1) {
            bs.invert();
        }

        if (!env.enc.isSingleByte()) {
            CodeRangeBuffer pbuf = null;
            if (not1 && not2) {
                pbuf = CodeRangeBuffer.andCodeRangeBuff(buf1, false, buf2, false, env);
            } else {
                pbuf = CodeRangeBuffer.orCodeRangeBuff(env, buf1, not1, buf2, not2);
                if (not1) {
                    pbuf = CodeRangeBuffer.notCodeRangeBuff(env, pbuf);
                }
            }
            mbuf = pbuf;
        }
    }

    // add_ctype_to_cc_by_range // Encoding out!
    public void addCTypeByRange(int ctype, boolean not, ScanEnvironment env, int sbOut, int mbr[]) {
        int n = mbr[0];
        int i;

        if (!not) {
            for (i=0; i<n; i++) {
                for (int j=CR_FROM(mbr, i); j<=CR_TO(mbr, i); j++) {
                    if (j >= sbOut) {
                        if (j > CR_FROM(mbr, i)) {
                            addCodeRangeToBuf(env, j, CR_TO(mbr, i));
                            i++;
                        }
                        // !goto sb_end!, remove duplication!
                        for (; i<n; i++) {
                            addCodeRangeToBuf(env, CR_FROM(mbr, i), CR_TO(mbr, i));
                        }
                        return;
                    }
                    bs.set(env, j);
                }
            }
            // !sb_end:!
            for (; i<n; i++) {
                addCodeRangeToBuf(env, CR_FROM(mbr, i), CR_TO(mbr, i));
            }

        } else {
            int prev = 0;

            for (i=0; i<n; i++) {
                for (int j=prev; j < CR_FROM(mbr, i); j++) {
                    if (j >= sbOut) {
                        // !goto sb_end2!, remove duplication
                        prev = sbOut;
                        for (i=0; i<n; i++) {
                            if (prev < CR_FROM(mbr, i)) addCodeRangeToBuf(env, prev, CR_FROM(mbr, i) - 1);
                            prev = CR_TO(mbr, i) + 1;
                        }
                        if (prev < 0x7fffffff/*!!!*/) addCodeRangeToBuf(env, prev, 0x7fffffff);
                        return;
                    }
                    bs.set(env, j);
                }
                prev = CR_TO(mbr, i) + 1;
            }

            for (int j=prev; j<sbOut; j++) {
                bs.set(env, j);
            }

            // !sb_end2:!
            prev = sbOut;
            for (i=0; i<n; i++) {
                if (prev < CR_FROM(mbr, i)) addCodeRangeToBuf(env, prev, CR_FROM(mbr, i) - 1);
                prev = CR_TO(mbr, i) + 1;
            }
            if (prev < 0x7fffffff/*!!!*/) addCodeRangeToBuf(env, prev, 0x7fffffff);
        }
    }

    private static int CR_FROM(int[] range, int i) {
        return range[(i * 2) + 1];
    }

    private static int CR_TO(int[] range, int i) {
        return range[(i * 2) + 2];
    }

    // add_ctype_to_cc
    public void addCType(int ctype, boolean not, boolean asciiRange, ScanEnvironment env, IntHolder sbOut) {
        Encoding enc = env.enc;
        int[]ranges = enc.ctypeCodeRange(ctype, sbOut);
        if (ranges != null) {
            if (asciiRange) {
                CClassNode ccWork = new CClassNode();
                ccWork.addCTypeByRange(ctype, not, env, sbOut.value, ranges);
                if (not) {
                    ccWork.addCodeRangeToBuf(env, 0x80, CodeRangeBuffer.LAST_CODE_POINT, false);
                } else {
                    CClassNode ccAscii = new CClassNode();
                    if (enc.minLength() > 1) {
                        ccAscii.addCodeRangeToBuf(env, 0x00, 0x7F);
                    } else {
                        ccAscii.bs.setRange(env, 0x00, 0x7F);
                    }
                    ccWork.and(ccAscii, env);
                }
                or(ccWork, env);
            } else {
                addCTypeByRange(ctype, not, env, sbOut.value, ranges);
            }
            return;
        }

        int maxCode = asciiRange ? 0x80 : BitSet.SINGLE_BYTE_SIZE;
        switch(ctype) {
        case CharacterType.ALPHA:
        case CharacterType.BLANK:
        case CharacterType.CNTRL:
        case CharacterType.DIGIT:
        case CharacterType.LOWER:
        case CharacterType.PUNCT:
        case CharacterType.SPACE:
        case CharacterType.UPPER:
        case CharacterType.XDIGIT:
        case CharacterType.ASCII:
        case CharacterType.ALNUM:
            if (not) {
                for (int c=0; c<BitSet.SINGLE_BYTE_SIZE; c++) {
                    if (!enc.isCodeCType(c, ctype)) bs.set(env, c);
                }
                addAllMultiByteRange(env);
            } else {
                for (int c=0; c<BitSet.SINGLE_BYTE_SIZE; c++) {
                    if (enc.isCodeCType(c, ctype)) bs.set(env, c);
                }
            }
            break;

        case CharacterType.GRAPH:
        case CharacterType.PRINT:
            if (not) {
                for (int c=0; c<BitSet.SINGLE_BYTE_SIZE; c++) {
                    if (!enc.isCodeCType(c, ctype) || c >= maxCode) bs.set(env, c);
                }
                if (asciiRange) addAllMultiByteRange(env);
            } else {
                for (int c=0; c<maxCode; c++) {
                    if (enc.isCodeCType(c, ctype)) bs.set(env, c);
                }
                if (!asciiRange) addAllMultiByteRange(env);
            }
            break;

        case CharacterType.WORD:
            if (!not) {
                for (int c=0; c<maxCode; c++) {
                    if (enc.isSbWord(c)) bs.set(env, c);
                }
                if (!asciiRange) addAllMultiByteRange(env);
            } else {
                for (int c=0; c<BitSet.SINGLE_BYTE_SIZE; c++) {
                    if (enc.codeToMbcLength(c) > 0 && /* check invalid code point */
                            !(enc.isWord(c) || c >= maxCode)) bs.set(env, c);
                }
                if (asciiRange) addAllMultiByteRange(env);
            }
            break;

        default:
            throw new InternalException(ErrorMessages.PARSER_BUG);
        } // switch
    }

    public static enum CCVALTYPE {
        SB,
        CODE_POINT,
        CLASS
    }

    public static enum CCSTATE {
        VALUE,
        RANGE,
        COMPLETE,
        START
    }

    public static final class CCStateArg {
        public int from;
        public int to;
        public boolean fromIsRaw;
        public boolean toIsRaw;
        public CCVALTYPE inType;
        public CCVALTYPE type;
        public CCSTATE state;
    }

    public void nextStateClass(CCStateArg arg, CClassNode ascCC, ScanEnvironment env) {
        if (arg.state == CCSTATE.RANGE) throw new SyntaxException(ErrorMessages.CHAR_CLASS_VALUE_AT_END_OF_RANGE);

        if (arg.state == CCSTATE.VALUE && arg.type != CCVALTYPE.CLASS) {
            if (arg.type == CCVALTYPE.SB) {
                bs.set(env, arg.from);
                if (ascCC != null) ascCC.bs.set(arg.from);
            } else if (arg.type == CCVALTYPE.CODE_POINT) {
                addCodeRange(env, arg.from, arg.from);
                if (ascCC != null) ascCC.addCodeRange(env, arg.from, arg.from, false);
            }
        }
        arg.state = CCSTATE.VALUE;
        arg.type = CCVALTYPE.CLASS;
    }

    public void nextStateValue(CCStateArg arg, CClassNode ascCc, ScanEnvironment env) {
        switch(arg.state) {
        case VALUE:
            if (arg.type == CCVALTYPE.SB) {
                bs.set(env, arg.from);
                if (ascCc != null) ascCc.bs.set(arg.from);
            } else if (arg.type == CCVALTYPE.CODE_POINT) {
                addCodeRange(env, arg.from, arg.from);
                if (ascCc != null) ascCc.addCodeRange(env, arg.from, arg.from, false);
            }
            break;

        case RANGE:
            if (arg.inType == arg.type) {
                if (arg.inType == CCVALTYPE.SB) {
                    if (arg.from > 0xff || arg.to > 0xff) throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);

                    if (arg.from > arg.to) {
                        if (env.syntax.allowEmptyRangeInCC()) {
                            // goto ccs_range_end
                            arg.state = CCSTATE.COMPLETE;
                            break;
                        } else {
                            throw new ValueException(ErrorMessages.EMPTY_RANGE_IN_CHAR_CLASS);
                        }
                    }
                    bs.setRange(env, arg.from, arg.to);
                    if (ascCc != null) ascCc.bs.setRange(null, arg.from, arg.to);
                } else {
                    addCodeRange(env, arg.from, arg.to);
                    if (ascCc != null) ascCc.addCodeRange(env, arg.from, arg.to, false);
                }
            } else {
                if (arg.from > arg.to) {
                    if (env.syntax.allowEmptyRangeInCC()) {
                        // goto ccs_range_end
                        arg.state = CCSTATE.COMPLETE;
                        break;
                    } else {
                        throw new ValueException(ErrorMessages.EMPTY_RANGE_IN_CHAR_CLASS);
                    }
                }
                bs.setRange(env, arg.from, arg.to < 0xff ? arg.to : 0xff);
                addCodeRange(env, arg.from, arg.to);
                if (ascCc != null) {
                    ascCc.bs.setRange(null, arg.from, arg.to < 0xff ? arg.to : 0xff);
                    ascCc.addCodeRange(env, arg.from, arg.to, false);
                }
            }
            // ccs_range_end:
            arg.state = CCSTATE.COMPLETE;
            break;

        case COMPLETE:
        case START:
            arg.state = CCSTATE.VALUE;
            break;

        default:
            break;

        } // switch

        arg.fromIsRaw = arg.toIsRaw;
        arg.from = arg.to;
        arg.type = arg.inType;
    }

    // onig_is_code_in_cc_len
    boolean isCodeInCCLength(int encLength, int code) {
        boolean found;

        if (encLength > 1 || code >= BitSet.SINGLE_BYTE_SIZE) {
            if (mbuf == null) {
                found = false;
            } else {
                found = CodeRange.isInCodeRange(mbuf.getCodeRange(), code);
            }
        } else {
            found = bs.at(code);
        }

        if (isNot()) {
            return !found;
        } else {
            return found;
        }
    }

    // onig_is_code_in_cc
    public boolean isCodeInCC(Encoding enc, int code) {
        int len;
        if (enc.minLength() > 1) {
            len = 2;
        } else {
            len = enc.codeToMbcLength(code);
        }
        return isCodeInCCLength(len, code);
    }

    public void setNot() {
        flags |= FLAG_NCCLASS_NOT;
    }

    public void clearNot() {
        flags &= ~FLAG_NCCLASS_NOT;
    }

    public boolean isNot() {
        return (flags & FLAG_NCCLASS_NOT) != 0;
    }
}
