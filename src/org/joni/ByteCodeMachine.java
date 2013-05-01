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

import static org.joni.BitStatus.bsAt;
import static org.joni.Option.isFindCondition;
import static org.joni.Option.isFindLongest;
import static org.joni.Option.isFindNotEmpty;
import static org.joni.Option.isNotBol;
import static org.joni.Option.isNotEol;
import static org.joni.Option.isPosixRegion;

import org.jcodings.CodeRange;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.joni.ast.CClassNode;
import org.joni.constants.OPCode;
import org.joni.constants.OPSize;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;

class ByteCodeMachine extends StackMachine {
    private int bestLen;          // return value
    private int s = 0;            // current char

    private int range;            // right range
    private int sprev;
    private int sstart;
    private int sbegin;

    private final int[]code;        // byte code
    private int ip;                 // instruction pointer

    ByteCodeMachine(Regex regex, byte[]bytes, int p, int end) {
        super(regex, bytes, p, end);
        this.code = regex.code;
    }

    protected int stkp; // a temporary
    private boolean makeCaptureHistoryTree(CaptureTreeNode node) {
        //CaptureTreeNode child;
        int k = stkp;
        //int k = kp;

        while (k < stk) {
            StackEntry e = stack[k];
            if (e.type == MEM_START) {
                int n = e.getMemNum();
                if (n <= Config.MAX_CAPTURE_HISTORY_GROUP && bsAt(regex.captureHistory, n)) {
                    CaptureTreeNode child = new CaptureTreeNode();
                    child.group = n;
                    child.beg = e.getMemPStr() - str;
                    node.addChild(child);
                    stkp = k + 1;
                    if (makeCaptureHistoryTree(child)) return true;

                    k = stkp;
                    child.end = e.getMemPStr() - str;
                }
            } else if (e.type == MEM_END) {
                if (e.getMemNum() == node.group) {
                    node.end = e.getMemPStr() - str;
                    stkp = k;
                    return false;
                }
            }
        }
        return true; /* 1: root node ending. */
    }

    private void checkCaptureHistory(Region region) {
        CaptureTreeNode node;
        if (region.historyRoot == null) {
            node = region.historyRoot = new CaptureTreeNode();
        } else {
            node = region.historyRoot;
            node.clear();
        }

        // was clear ???
        node.group = 0;
        node.beg = sstart - str;
        node.end = s      - str;

        stkp = 0;
        makeCaptureHistoryTree(region.historyRoot);
    }

    private byte[]cfbuf;
    private byte[]cfbuf2;

    protected final byte[]cfbuf() {
        return cfbuf == null ? cfbuf = new byte[Config.ENC_MBC_CASE_FOLD_MAXLEN] : cfbuf;
    }

    protected final byte[]cfbuf2() {
        return cfbuf2 == null ? cfbuf2 = new byte[Config.ENC_MBC_CASE_FOLD_MAXLEN] : cfbuf2;
    }

    private boolean stringCmpIC(int caseFlodFlag, int s1, IntHolder ps2, int mbLen, int textEnd) {
        byte[]buf1 = cfbuf();
        byte[]buf2 = cfbuf2();

        int s2 = ps2.value;
        int end1 = s1 + mbLen;

        while (s1 < end1) {
            value = s1;
            int len1 = enc.mbcCaseFold(caseFlodFlag, bytes, this, textEnd, buf1);
            s1 = value;
            value = s2;
            int len2 = enc.mbcCaseFold(caseFlodFlag, bytes, this, textEnd, buf2);
            s2 = value;

            if (len1 != len2) return false;
            int p1 = 0;
            int p2 = 0;

            while (len1-- > 0) {
                if (buf1[p1] != buf2[p2]) return false;
                p1++; p2++;
            }
        }
        ps2.value = s2;
        return true;
    }

    private void debugMatchBegin() {
        Config.log.println("match_at: " +
                "str: " + str +
                ", end: " + end +
                ", start: " + this.sstart +
                ", sprev: " + this.sprev);
        Config.log.println("size: " + (end - str) + ", start offset: " + (this.sstart - str));
    }

    private void debugMatchLoop() {
        if (Config.DEBUG_MATCH) {
            Config.log.printf("%4d", (s - str)).print("> \"");
            int q, i;
            for (i=0, q=s; i<7 && q<end && s>=0; i++) {
                int len = enc.length(bytes, q, end);
                while (len-- > 0) if (q < end) Config.log.print(new String(new byte[]{bytes[q++]}));
            }
            String str = q < end ? "...\"" : "\"";
            q += str.length();
            Config.log.print(str);
            for (i=0; i<20-(q-s);i++) Config.log.print(" ");
            StringBuilder sb = new StringBuilder();
            new ByteCodePrinter(regex).compiledByteCodeToString(sb, ip);
            Config.log.println(sb.toString());
        }
    }

    protected final int matchAt(int range, int sstart, int sprev) throws InterruptedException {
        this.range = range;
        this.sstart = sstart;
        this.sprev = sprev;

        stk = 0;
        ip = 0;

        if (Config.DEBUG_MATCH) debugMatchBegin();

        init();

        bestLen = -1;
        s = sstart;

        final int[]code = this.code;
        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (Config.DEBUG_MATCH) debugMatchLoop();

            sbegin = s;
            switch (code[ip++]) {
                case OPCode.END:    if (opEnd()) return finish();                  break;
                case OPCode.EXACT1:                     opExact1();                break;
                case OPCode.EXACT2:                     opExact2();                continue;
                case OPCode.EXACT3:                     opExact3();                continue;
                case OPCode.EXACT4:                     opExact4();                continue;
                case OPCode.EXACT5:                     opExact5();                continue;
                case OPCode.EXACTN:                     opExactN();                continue;

                case OPCode.EXACTMB2N1:                 opExactMB2N1();            break;
                case OPCode.EXACTMB2N2:                 opExactMB2N2();            continue;
                case OPCode.EXACTMB2N3:                 opExactMB2N3();            continue;
                case OPCode.EXACTMB2N:                  opExactMB2N();             continue;
                case OPCode.EXACTMB3N:                  opExactMB3N();             continue;
                case OPCode.EXACTMBN:                   opExactMBN();              continue;

                case OPCode.EXACT1_IC:                  opExact1IC();              break;
                case OPCode.EXACTN_IC:                  opExactNIC();              continue;

                case OPCode.CCLASS:                     opCClass();                break;
                case OPCode.CCLASS_MB:                  opCClassMB();              break;
                case OPCode.CCLASS_MIX:                 opCClassMIX();             break;
                case OPCode.CCLASS_NOT:                 opCClassNot();             break;
                case OPCode.CCLASS_MB_NOT:              opCClassMBNot();           break;
                case OPCode.CCLASS_MIX_NOT:             opCClassMIXNot();          break;
                case OPCode.CCLASS_NODE:                opCClassNode();            break;

                case OPCode.ANYCHAR:                    opAnyChar();               break;
                case OPCode.ANYCHAR_ML:                 opAnyCharML();             break;
                case OPCode.ANYCHAR_STAR:               opAnyCharStar();           break;
                case OPCode.ANYCHAR_ML_STAR:            opAnyCharMLStar();         break;
                case OPCode.ANYCHAR_STAR_PEEK_NEXT:     opAnyCharStarPeekNext();   break;
                case OPCode.ANYCHAR_ML_STAR_PEEK_NEXT:  opAnyCharMLStarPeekNext(); break;
                case OPCode.STATE_CHECK_ANYCHAR_STAR:   opStateCheckAnyCharStar(); break;
                case OPCode.STATE_CHECK_ANYCHAR_ML_STAR:opStateCheckAnyCharMLStar();break;

                case OPCode.WORD:                       opWord();                  break;
                case OPCode.NOT_WORD:                   opNotWord();               break;
                case OPCode.WORD_BOUND:                 opWordBound();             continue;
                case OPCode.NOT_WORD_BOUND:             opNotWordBound();          continue;
                case OPCode.WORD_BEGIN:                 opWordBegin();             continue;
                case OPCode.WORD_END:                   opWordEnd();               continue;

                case OPCode.BEGIN_BUF:                  opBeginBuf();              continue;
                case OPCode.END_BUF:                    opEndBuf();                continue;
                case OPCode.BEGIN_LINE:                 opBeginLine();             continue;
                case OPCode.END_LINE:                   opEndLine();               continue;
                case OPCode.SEMI_END_BUF:               opSemiEndBuf();            continue;
                case OPCode.BEGIN_POSITION:             opBeginPosition();         continue;

                case OPCode.MEMORY_START_PUSH:          opMemoryStartPush();       continue;
                case OPCode.MEMORY_START:               opMemoryStart();           continue;
                case OPCode.MEMORY_END_PUSH:            opMemoryEndPush();         continue;
                case OPCode.MEMORY_END:                 opMemoryEnd();             continue;
                case OPCode.MEMORY_END_PUSH_REC:        opMemoryEndPushRec();      continue;
                case OPCode.MEMORY_END_REC:             opMemoryEndRec();          continue;

                case OPCode.BACKREF1:                   opBackRef1();              continue;
                case OPCode.BACKREF2:                   opBackRef2();              continue;
                case OPCode.BACKREFN:                   opBackRefN();              continue;
                case OPCode.BACKREFN_IC:                opBackRefNIC();            continue;
                case OPCode.BACKREF_MULTI:              opBackRefMulti();          continue;
                case OPCode.BACKREF_MULTI_IC:           opBackRefMultiIC();        continue;
                case OPCode.BACKREF_WITH_LEVEL:         opBackRefAtLevel();        continue;

                case OPCode.NULL_CHECK_START:           opNullCheckStart();        continue;
                case OPCode.NULL_CHECK_END:             opNullCheckEnd();          continue;
                case OPCode.NULL_CHECK_END_MEMST:       opNullCheckEndMemST();     continue;
                case OPCode.NULL_CHECK_END_MEMST_PUSH:  opNullCheckEndMemSTPush(); continue;

                case OPCode.JUMP:                       opJump();                  continue;
                case OPCode.PUSH:                       opPush();                  continue;

                // CEC
                case OPCode.STATE_CHECK_PUSH:           opStateCheckPush();        continue;
                case OPCode.STATE_CHECK_PUSH_OR_JUMP:   opStateCheckPushOrJump();  continue;
                case OPCode.STATE_CHECK:                opStateCheck();            continue;

                case OPCode.POP:                        opPop();                   continue;
                case OPCode.PUSH_OR_JUMP_EXACT1:        opPushOrJumpExact1();      continue;
                case OPCode.PUSH_IF_PEEK_NEXT:          opPushIfPeekNext();        continue;

                case OPCode.REPEAT:                     opRepeat();                continue;
                case OPCode.REPEAT_NG:                  opRepeatNG();              continue;
                case OPCode.REPEAT_INC:                 opRepeatInc();             continue;
                case OPCode.REPEAT_INC_SG:              opRepeatIncSG();           continue;
                case OPCode.REPEAT_INC_NG:              opRepeatIncNG();           continue;
                case OPCode.REPEAT_INC_NG_SG:           opRepeatIncNGSG();         continue;

                case OPCode.PUSH_POS:                   opPushPos();               continue;
                case OPCode.POP_POS:                    opPopPos();                continue;
                case OPCode.PUSH_POS_NOT:               opPushPosNot();            continue;
                case OPCode.FAIL_POS:                   opFailPos();               continue;
                case OPCode.PUSH_STOP_BT:               opPushStopBT();            continue;
                case OPCode.POP_STOP_BT:                opPopStopBT();             continue;

                case OPCode.LOOK_BEHIND:                opLookBehind();            continue;
                case OPCode.PUSH_LOOK_BEHIND_NOT:       opPushLookBehindNot();     continue;
                case OPCode.FAIL_LOOK_BEHIND_NOT:       opFailLookBehindNot();     continue;

                // USE_SUBEXP_CALL
                case OPCode.CALL:                       opCall();                  continue;
                case OPCode.RETURN:                     opReturn();                continue;

                // single byte implementations
                case OPCode.CCLASS_SB:                      opCClassSb();                break;
                case OPCode.CCLASS_NOT_SB:                  opCClassNotSb();             break;

                case OPCode.ANYCHAR_SB:                     opAnyCharSb();               break;
                case OPCode.ANYCHAR_ML_SB:                  opAnyCharMLSb();             break;
                case OPCode.ANYCHAR_STAR_SB:                opAnyCharStarSb();           break;
                case OPCode.ANYCHAR_ML_STAR_SB:             opAnyCharMLStarSb();         break;
                case OPCode.ANYCHAR_STAR_PEEK_NEXT_SB:      opAnyCharStarPeekNextSb();   break;
                case OPCode.ANYCHAR_ML_STAR_PEEK_NEXT_SB:   opAnyCharMLStarPeekNextSb(); break;
                case OPCode.STATE_CHECK_ANYCHAR_STAR_SB:    opStateCheckAnyCharStarSb(); break;
                case OPCode.STATE_CHECK_ANYCHAR_ML_STAR_SB: opStateCheckAnyCharMLStarSb();break;

                case OPCode.WORD_SB:                        opWordSb();                  break;
                case OPCode.NOT_WORD_SB:                    opNotWordSb();               break;
                case OPCode.WORD_BOUND_SB:                  opWordBoundSb();             continue;
                case OPCode.NOT_WORD_BOUND_SB:              opNotWordBoundSb();          continue;
                case OPCode.WORD_BEGIN_SB:                  opWordBeginSb();             continue;
                case OPCode.WORD_END_SB:                    opWordEndSb();               continue;

                case OPCode.LOOK_BEHIND_SB:                 opLookBehindSb();            continue;

                case OPCode.EXACT1_IC_SB:                   opExact1ICSb();              break;
                case OPCode.EXACTN_IC_SB:                   opExactNICSb();              continue;

                case OPCode.FINISH:
                    return finish();

                case OPCode.FAIL:                       opFail();                  continue;

                default:
                    throw new InternalException(ErrorMessages.ERR_UNDEFINED_BYTECODE);

            } // main switch
        } // main while
    }

    private boolean opEnd() {
        int n = s - sstart;

        if (n > bestLen) {
            if (Config.USE_FIND_LONGEST_SEARCH_ALL_OF_RANGE) {
                if (isFindLongest(regex.options)) {
                    if (n > msaBestLen) {
                        msaBestLen = n;
                        msaBestS = sstart;
                    } else {
                        // goto end_best_len;
                        return endBestLength();
                    }
                }
            } // USE_FIND_LONGEST_SEARCH_ALL_OF_RANGE

            bestLen = n;
            final Region region = msaRegion;
            if (region != null) {
                // USE_POSIX_REGION_OPTION ... else ...
                region.beg[0] = msaBegin = sstart - str;
                region.end[0] = msaEnd   = s      - str;
                for (int i = 1; i <= regex.numMem; i++) {
                    // opt!
                    if (repeatStk[memEndStk + i] != INVALID_INDEX) {
                        region.beg[i] = bsAt(regex.btMemStart, i) ?
                                        stack[repeatStk[memStartStk + i]].getMemPStr() - str :
                                        repeatStk[memStartStk + i] - str;


                        region.end[i] = bsAt(regex.btMemEnd, i) ?
                                        stack[repeatStk[memEndStk + i]].getMemPStr() :
                                        repeatStk[memEndStk + i] - str;

                    } else {
                        region.beg[i] = region.end[i] = Region.REGION_NOTPOS;
                    }

                }

                if (Config.USE_CAPTURE_HISTORY) {
                    if (regex.captureHistory != 0) checkCaptureHistory(region);
                }
            } else {
                msaBegin = sstart - str;
                msaEnd   = s      - str;
            }
        } else {
            Region region = msaRegion;
            if (Config.USE_POSIX_API_REGION_OPTION) {
                if (!isPosixRegion(regex.options)) {
                    if (region != null) {
                        region.clear();
                    } else {
                        msaBegin = msaEnd = 0;
                    }
                }
            } else {
                if (region != null) {
                    region.clear();
                } else {
                    msaBegin = msaEnd = 0;
                }
            } // USE_POSIX_REGION_OPTION
        }
        // end_best_len:
        /* default behavior: return first-matching result. */
        return endBestLength();
    }

    private boolean endBestLength() {
        if (isFindCondition(regex.options)) {
            if (isFindNotEmpty(regex.options) && s == sstart) {
                bestLen = -1;
                {opFail(); return false;} /* for retry */
            }
            if (isFindLongest(regex.options) && s < range) {
                {opFail(); return false;} /* for retry */
            }
        }
        // goto finish;
        return true;
    }

    private void opExact1() {
        if (s >= range || code[ip] != bytes[s++]) {opFail(); return;}
        //if (s > range) {opFail(); return;}
        ip++;
        sprev = sbegin; // break;
    }

    private void opExact2() {
        if (s + 2 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        sprev = s;
        ip++; s++;
    }

    private void opExact3() {
        if (s + 3 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        sprev = s;
        ip++; s++;
    }

    private void opExact4() {
        if (s + 4 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        sprev = s;
        ip++; s++;
    }

    private void opExact5() {
        if (s + 5 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        sprev = s;
        ip++; s++;
    }

    private void opExactN() {
        int tlen = code[ip++];
        if (s + tlen > range) {opFail(); return;}

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];

            while (tlen-- > 0) if (bs[ps++] != bytes[s++]) {opFail(); return;}

        } else {
            while (tlen-- > 0) if (code[ip++] != bytes[s++]) {opFail(); return;}
        }
        sprev = s - 1;
    }

    private void opExactMB2N1() {
        if (s + 2 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        sprev = sbegin; // break;
    }

    private void opExactMB2N2() {
        if (s + 4 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        sprev = s;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
   }

    private void opExactMB2N3() {
        if (s + 6 > range) {opFail(); return;}
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        sprev = s;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
        if (code[ip] != bytes[s]) {opFail(); return;}
        ip++; s++;
    }

    private void opExactMB2N() {
        int tlen = code[ip++];
        if (s + tlen * 2 > range) {opFail(); return;}

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];

            while(tlen-- > 0) {
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
            }
        } else {
            while(tlen-- > 0) {
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
            }
        }
        sprev = s - 2;
    }

    private void opExactMB3N() {
        int tlen = code[ip++];
        if (s + tlen * 3 > range) {opFail(); return;}

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];

            while (tlen-- > 0) {
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
            }
        } else {
            while (tlen-- > 0) {
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
            }
        }

        sprev = s - 3;
    }

    private void opExactMBN() {
        int tlen = code[ip++];   /* mb-len */
        int tlen2= code[ip++];   /* string len */

        tlen2 *= tlen;
        if (s + tlen2 > range) {opFail(); return;}

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];

            while (tlen2-- > 0) {
                if (bs[ps] != bytes[s]) {opFail(); return;}
                ps++; s++;
            }
        } else {
            while (tlen2-- > 0) {
                if (code[ip] != bytes[s]) {opFail(); return;}
                ip++; s++;
            }
        }

        sprev = s - tlen;
    }

    private void opExact1IC() {
        if (s >= range) {opFail(); return;}

        byte[]lowbuf = cfbuf();

        value = s;
        int len = enc.mbcCaseFold(regex.caseFoldFlag, bytes, this, end, lowbuf);
        s = value;

        if (s > range) {opFail(); return;}

        int q = 0;
        while (len-- > 0) {
            if (code[ip] != lowbuf[q]) {opFail(); return;}
            ip++; q++;
        }
        sprev = sbegin; // break;
    }

    private void opExact1ICSb() {
        if (s >= range || code[ip] != enc.toLowerCaseTable()[bytes[s++] & 0xff]) {opFail(); return;}
        ip++;
        sprev = sbegin; // break;
    }

    private void opExactNIC() {
        int tlen = code[ip++];
        byte[]lowbuf = cfbuf();

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];
            int endp = ps + tlen;

            while (ps < endp) {
                sprev = s;
                if (s >= range) {opFail(); return;}

                value = s;
                int len = enc.mbcCaseFold(regex.caseFoldFlag, bytes, this, end, lowbuf);
                s = value;

                if (s > range) {opFail(); return;}
                int q = 0;
                while (len-- > 0) {
                    if (bs[ps] != lowbuf[q]) {opFail(); return;}
                    ps++; q++;
                }
            }
        } else {
            int endp = ip + tlen;

            while (ip < endp) {
                sprev = s;
                if (s >= range) {opFail(); return;}

                value = s;
                int len = enc.mbcCaseFold(regex.caseFoldFlag, bytes, this, end, lowbuf);
                s = value;

                if (s > range) {opFail(); return;}
                int q = 0;
                while (len-- > 0) {
                    if (code[ip] != lowbuf[q]) {opFail(); return;}
                    ip++; q++;
                }
            }
        }

    }

    private void opExactNICSb() {
        int tlen = code[ip++];
        if (s + tlen > range) {opFail(); return;}

        if (Config.USE_STRING_TEMPLATES) {
            byte[]bs = regex.templates[code[ip++]];
            int ps = code[ip++];
            byte[]toLowerTable = enc.toLowerCaseTable();

            while (tlen-- > 0) if (bs[ps++] != toLowerTable[bytes[s++] & 0xff]) {opFail(); return;}
        } else {
            byte[]toLowerTable = enc.toLowerCaseTable();

            while (tlen-- > 0) if (code[ip++] != toLowerTable[bytes[s++] & 0xff]) {opFail(); return;}
        }
        sprev = s - 1;
    }

    private boolean isInBitSet() {
        int c = bytes[s] & 0xff;
        return ((code[ip + (c >>> BitSet.ROOM_SHIFT)] & (1 << c)) != 0);
    }

    private void opCClass() {
        if (s >= range || !isInBitSet()) {opFail(); return;}
        ip += BitSet.BITSET_SIZE;
        s += enc.length(bytes, s, end); /* OP_CCLASS can match mb-code. \D, \S */
        if (s > end) s = end;
        sprev = sbegin; // break;
    }

    private void opCClassSb() {
        if (s >= range || !isInBitSet()) {opFail(); return;}
        ip += BitSet.BITSET_SIZE;
        s++;
        sprev = sbegin; // break;
    }

    private boolean isInClassMB() {
        int tlen = code[ip++];
        if (s >= range) return false;
        int mbLen = enc.length(bytes, s, end);
        if (s + mbLen > range) return false;
        int ss = s;
        s += mbLen;
        int c = enc.mbcToCode(bytes, ss, s);
        if (!CodeRange.isInCodeRange(code, ip, c)) return false;
        ip += tlen;
        return true;
    }

    private void opCClassMB() {
        // beyond string check
        if (s >= range || !enc.isMbcHead(bytes, s, end)) {opFail(); return;}
        if (!isInClassMB()) {opFail(); return;} // not!!!
        sprev = sbegin; // break;
    }

    private void opCClassMIX() {
        if (s >= range) {opFail(); return;}
        if (enc.isMbcHead(bytes, s, end)) {
            ip += BitSet.BITSET_SIZE;
            if (!isInClassMB()) {opFail(); return;}
        } else {
            if (!isInBitSet()) {opFail(); return;}
            ip += BitSet.BITSET_SIZE;
            int tlen = code[ip++]; // by code range length
            ip += tlen;
            s++;
        }
        sprev = sbegin; // break;
    }

    private void opCClassNot() {
        if (s >= range || isInBitSet()) {opFail(); return;}
        ip += BitSet.BITSET_SIZE;
        s += enc.length(bytes, s, end);
        if (s > end) s = end;
        sprev = sbegin; // break;
    }

    private void opCClassNotSb() {
        if (s >= range || isInBitSet()) {opFail(); return;}
        ip += BitSet.BITSET_SIZE;
        s++;
        sprev = sbegin; // break;
    }

    private boolean isNotInClassMB() {
        int tlen = code[ip++];
        int mbLen = enc.length(bytes, s, end);

        if (!(s + mbLen <= range)) {
            if (s >= range) return false;
            s = end;
            ip += tlen;
            return true;
        }

        int ss = s;
        s += mbLen;
        int c = enc.mbcToCode(bytes, ss, s);

        if (CodeRange.isInCodeRange(code, ip, c)) return false;
        ip += tlen;
        return true;
    }

    private void opCClassMBNot() {
        if (s >= range) {opFail(); return;}
        if (!enc.isMbcHead(bytes, s, end)) {
            s++;
            int tlen = code[ip++];
            ip += tlen;
            sprev = sbegin; // break;
            return;
        }
        if (!isNotInClassMB()) {opFail(); return;}
        sprev = sbegin; // break;
    }

    private void opCClassMIXNot() {
        if (s >= range) {opFail(); return;}
        if (enc.isMbcHead(bytes, s, end)) {
            ip += BitSet.BITSET_SIZE;
            if (!isNotInClassMB()) {opFail(); return;}
        } else {
            if (isInBitSet()) {opFail(); return;}
            ip += BitSet.BITSET_SIZE;
            int tlen = code[ip++];
            ip += tlen;
            s++;
        }
        sprev = sbegin; // break;
    }

    private void opCClassNode() {
        if (s >= range) {opFail(); return;}
        CClassNode cc = (CClassNode)regex.operands[code[ip++]];
        int mbLen = enc.length(bytes, s, end);
        int ss = s;
        s += mbLen;
        if (s > range) {opFail(); return;}
        int c = enc.mbcToCode(bytes, ss, s);
        if (!cc.isCodeInCCLength(mbLen, c)) {opFail(); return;}
        sprev = sbegin; // break;
    }

    private void opAnyChar() {
        if (s >= range) {opFail(); return;}
        int n = enc.length(bytes, s, end);
        if (s + n > range) {opFail(); return;}
        if (enc.isNewLine(bytes, s, end)) {opFail(); return;}
        s += n;
        sprev = sbegin; // break;
    }

    private void opAnyCharSb() {
        if (s >= range) {opFail(); return;}
        if (bytes[s] == Encoding.NEW_LINE) {opFail(); return;}
        s++;
        sprev = sbegin; // break;
    }

    private void opAnyCharML() {
        if (s >= range) {opFail(); return;}
        int n = enc.length(bytes, s, end);
        if (s + n > range) {opFail(); return;}
        s += n;
        sprev = sbegin; // break;
    }

    private void opAnyCharMLSb() {
        if (s >= range) {opFail(); return;}
        s++;
        sprev = sbegin; // break;
    }

    private void opAnyCharStar() {
        final byte[]bytes = this.bytes;
        while (s < range) {
            pushAlt(ip, s, sprev);
            int n = enc.length(bytes, s, end);
            if (s + n > range) {opFail(); return;}
            if (enc.isNewLine(bytes, s, end)) {opFail(); return;}
            sprev = s;
            s += n;
        }
        sprev = sbegin; // break;
    }

    private void opAnyCharStarSb() {
        final byte[]bytes = this.bytes;
        while (s < range) {
            pushAlt(ip, s, sprev);
            if (bytes[s] == Encoding.NEW_LINE) {opFail(); return;}
            sprev = s;
            s++;
        }
        sprev = sbegin; // break;
    }

    private void opAnyCharMLStar() {
        final byte[]bytes = this.bytes;
        while (s < range) {
            pushAlt(ip, s, sprev);
            int n = enc.length(bytes, s, end);
            if (s + n > range) {opFail(); return;}
            sprev = s;
            s += n;
        }
        sprev = sbegin; // break;
    }

    private void opAnyCharMLStarSb() {
        while (s < range) {
            pushAlt(ip, s, sprev);
            sprev = s;
            s++;
        }
        sprev = sbegin; // break;
    }

    private void opAnyCharStarPeekNext() {
        final byte c = (byte)code[ip];
        final byte[]bytes = this.bytes;

        while (s < range) {
            if (c == bytes[s]) pushAlt(ip + 1, s, sprev);
            int n = enc.length(bytes, s, end);
            if (s + n > range || enc.isNewLine(bytes, s, end)) {opFail(); return;}
            sprev = s;
            s += n;
        }
        ip++;
        sprev = sbegin; // break;
    }

    private void opAnyCharStarPeekNextSb() {
        final byte c = (byte)code[ip];
        final byte[]bytes = this.bytes;

        while (s < range) {
            byte b = bytes[s];
            if (c == b) pushAlt(ip + 1, s, sprev);
            if (b == Encoding.NEW_LINE) {opFail(); return;}
            sprev = s;
            s++;
        }
        ip++;
        sprev = sbegin; // break;
    }

    private void opAnyCharMLStarPeekNext() {
        final byte c = (byte)code[ip];
        final byte[]bytes = this.bytes;

        while (s < range) {
            if (c == bytes[s]) pushAlt(ip + 1, s, sprev);
            int n = enc.length(bytes, s, end);
            if (s + n > range) {opFail(); return;}
            sprev = s;
            s += n;
        }
        ip++;
        sprev = sbegin; // break;
    }

    private void opAnyCharMLStarPeekNextSb() {
        final byte c = (byte)code[ip];
        final byte[]bytes = this.bytes;

        while (s < range) {
            if (c == bytes[s]) pushAlt(ip + 1, s, sprev);
            sprev = s;
            s++;
        }
        ip++;
        sprev = sbegin; // break;
    }

    // CEC
    private void opStateCheckAnyCharStar() {
        int mem = code[ip++];
        final byte[]bytes = this.bytes;

        while (s < range) {
            if (stateCheckVal(s, mem)) {opFail(); return;}
            pushAltWithStateCheck(ip, s, sprev, mem);
            int n = enc.length(bytes, s, end);
            if (s + n > range || enc.isNewLine(bytes, s, end)) {opFail(); return;}
            sprev = s;
            s += n;
        }
        sprev = sbegin; // break;
    }

    private void opStateCheckAnyCharStarSb() {
        int mem = code[ip++];
        final byte[]bytes = this.bytes;

        while (s < range) {
            if (stateCheckVal(s, mem)) {opFail(); return;}
            pushAltWithStateCheck(ip, s, sprev, mem);
            if (bytes[s] == Encoding.NEW_LINE) {opFail(); return;}
            sprev = s;
            s++;
        }
        sprev = sbegin; // break;
    }

    // CEC
    private void opStateCheckAnyCharMLStar() {
        int mem = code[ip++];

        final byte[]bytes = this.bytes;
        while (s < range) {
            if (stateCheckVal(s, mem)) {opFail(); return;}
            pushAltWithStateCheck(ip, s, sprev, mem);
            int n = enc.length(bytes, s, end);
            if (s + n > range) {opFail(); return;}
            sprev = s;
            s += n;
        }
        sprev = sbegin; // break;
    }

    private void opStateCheckAnyCharMLStarSb() {
        int mem = code[ip++];

        while (s < range) {
            if (stateCheckVal(s, mem)) {opFail(); return;}
            pushAltWithStateCheck(ip, s, sprev, mem);
            sprev = s;
            s++;
        }
        sprev = sbegin; // break;
    }

    private void opWord() {
        if (s >= range || !enc.isMbcWord(bytes, s, end)) {opFail(); return;}
        s += enc.length(bytes, s, end);
        sprev = sbegin; // break;
    }

    private void opWordSb() {
        if (s >= range || !enc.isWord(bytes[s] & 0xff)) {opFail(); return;}
        s++;
        sprev = sbegin; // break;
    }

    private void opNotWord() {
        if (s >= range || enc.isMbcWord(bytes, s, end)) {opFail(); return;}
        s += enc.length(bytes, s, end);
        sprev = sbegin; // break;
    }

    private void opNotWordSb() {
        if (s >= range || enc.isWord(bytes[s] & 0xff)) {opFail(); return;}
        s++;
        sprev = sbegin; // break;
    }

    private void opWordBound() {
        if (s == str) {
            if (s >= range || !enc.isMbcWord(bytes, s, end)) {opFail(); return;}
        } else if (s == end) {
            if (!enc.isMbcWord(bytes, sprev, end)) {opFail(); return;}
        } else {
            if (enc.isMbcWord(bytes, s, end) == enc.isMbcWord(bytes, sprev, end)) {opFail(); return;}
        }
    }

    private void opWordBoundSb() {
        if (s == str) {
            if (s >= range || !enc.isWord(bytes[s] & 0xff)) {opFail(); return;}
        } else if (s == end) {
            if (sprev >= end || !enc.isWord(bytes[sprev] & 0xff)) {opFail(); return;}
        } else {
            if (enc.isWord(bytes[s] & 0xff) == enc.isWord(bytes[sprev] & 0xff)) {opFail(); return;}
        }
    }

    private void opNotWordBound() {
        if (s == str) {
            if (s < range && enc.isMbcWord(bytes, s, end)) {opFail(); return;}
        } else if (s == end) {
            if (enc.isMbcWord(bytes, sprev, end)) {opFail(); return;}
        } else {
            if (enc.isMbcWord(bytes, s, end) != enc.isMbcWord(bytes, sprev, end)) {opFail(); return;}
        }
    }

    private void opNotWordBoundSb() {
        if (s == str) {
            if (s < range && enc.isWord(bytes[s] & 0xff)) {opFail(); return;}
        } else if (s == end) {
            if (sprev < end && enc.isWord(bytes[sprev] & 0xff)) {opFail(); return;}
        } else {
            if (enc.isWord(bytes[s] & 0xff) != enc.isWord(bytes[sprev] & 0xff)) {opFail(); return;}
        }
    }

    private void opWordBegin() {
        if (s < range && enc.isMbcWord(bytes, s, end)) {
            if (s == str || !enc.isMbcWord(bytes, sprev, end)) return;
        }
        opFail();
    }

    private void opWordBeginSb() {
        if (s < range && enc.isWord(bytes[s] & 0xff)) {
            if (s == str || !enc.isWord(bytes[sprev] & 0xff)) return;
        }
        opFail();
    }

    private void opWordEnd() {
        if (s != str && enc.isMbcWord(bytes, sprev, end)) {
            if (s == end || !enc.isMbcWord(bytes, s, end)) return;
        }
        opFail();
    }

    private void opWordEndSb() {
        if (s != str && enc.isWord(bytes[sprev] & 0xff)) {
            if (s == end || !enc.isWord(bytes[s] & 0xff)) return;
        }
        opFail();
    }

    private void opBeginBuf() {
        if (s != str) opFail();
    }

    private void opEndBuf() {
        if (s != end) opFail();
    }

    private void opBeginLine() {
        if (s == str) {
            if (isNotBol(msaOptions)) opFail();
            return;
        } else if (enc.isNewLine(bytes, sprev, end) && s != end) {
            return;
        }
        opFail();
    }

    private void opEndLine()  {
        if (s == end) {
            if (Config.USE_NEWLINE_AT_END_OF_STRING_HAS_EMPTY_LINE) {
                if (str == end || !enc.isNewLine(bytes, sprev, end)) {
                    if (isNotEol(msaOptions)) opFail();
                }
                return;
            } else {
                if (isNotEol(msaOptions)) opFail();
                return;
            }
        } else if (enc.isNewLine(bytes, s, end) || (Config.USE_CRNL_AS_LINE_TERMINATOR && enc.isMbcCrnl(bytes, s, end))) {
            return;
        }
        opFail();
    }

    private void opSemiEndBuf() {
        if (s == end) {
            if (Config.USE_NEWLINE_AT_END_OF_STRING_HAS_EMPTY_LINE) {
                if (str == end || !enc.isNewLine(bytes, sprev, end)) {
                    if (isNotEol(msaOptions)) opFail();
                }
                return;
            } else {
                if (isNotEol(msaOptions)) opFail();
                return;
            }
        } else if (enc.isNewLine(bytes, s, end) && (s + enc.length(bytes, s, end)) == end) {
            return;
        } else if (Config.USE_CRNL_AS_LINE_TERMINATOR && enc.isMbcCrnl(bytes, s, end)) {
            int ss = s + enc.length(bytes, s, end);
            ss += enc.length(bytes, ss, end);
            if (ss == end) return;
        }
        opFail();
    }

    private void opBeginPosition() {
        if (s != msaStart) opFail();
    }

    private void opMemoryStartPush() {
        int mem = code[ip++];
        pushMemStart(mem, s);
    }

    private void opMemoryStart() {
        int mem = code[ip++];
        repeatStk[memStartStk + mem] = s;
    }

    private void opMemoryEndPush() {
        int mem = code[ip++];
        pushMemEnd(mem, s);
    }

    private void opMemoryEnd() {
        int mem = code[ip++];
        repeatStk[memEndStk + mem] = s;
    }

    private void opMemoryEndPushRec() {
        int mem = code[ip++];
        int stkp = getMemStart(mem); /* should be before push mem-end. */
        pushMemEnd(mem, s);
        repeatStk[memStartStk + mem] = stkp;
    }

    private void opMemoryEndRec() {
        int mem = code[ip++];
        repeatStk[memEndStk + mem] = s;
        int stkp = getMemStart(mem);

        if (BitStatus.bsAt(regex.btMemStart, mem)) {
            repeatStk[memStartStk + mem] = stkp;
        } else {
            repeatStk[memStartStk + mem] = stack[stkp].getMemPStr();
        }

        pushMemEndMark(mem);
    }

    private boolean backrefInvalid(int mem) {
        return repeatStk[memEndStk + mem] == INVALID_INDEX || repeatStk[memStartStk + mem] == INVALID_INDEX;
    }

    private int backrefStart(int mem) {
        return bsAt(regex.btMemStart, mem) ? stack[repeatStk[memStartStk + mem]].getMemPStr() : repeatStk[memStartStk + mem];
    }

    private int backrefEnd(int mem) {
        return bsAt(regex.btMemEnd, mem) ? stack[repeatStk[memEndStk + mem]].getMemPStr() : repeatStk[memEndStk + mem];
    }

    private void backref(int mem) {
        /* if you want to remove following line,
        you should check in parse and compile time. (numMem) */
        if (mem > regex.numMem || backrefInvalid(mem)) {opFail(); return;}

        int pstart = backrefStart(mem);
        int pend = backrefEnd(mem);

        int n = pend - pstart;
        if (s + n > range) {opFail(); return;}
        sprev = s;

        // STRING_CMP
        while(n-- > 0) if (bytes[pstart++] != bytes[s++]) {opFail(); return;}

        int len;

        // beyond string check
        if (sprev < range) {
            while (sprev + (len = enc.length(bytes, sprev, end)) < s) sprev += len;
        }
    }

    private void opBackRef1() {
        backref(1);
    }

    private void opBackRef2() {
        backref(2);
    }

    private void opBackRefN() {
        backref(code[ip++]);
    }

    private void opBackRefNIC() {
        int mem = code[ip++];
        /* if you want to remove following line,
        you should check in parse and compile time. (numMem) */
        if (mem > regex.numMem || backrefInvalid(mem)) {opFail(); return;}

        int pstart = backrefStart(mem);
        int pend = backrefEnd(mem);

        int n = pend - pstart;
        if (s + n > range) {opFail(); return;}
        sprev = s;

        value = s;
        if (!stringCmpIC(regex.caseFoldFlag, pstart, this, n, end)) {opFail(); return;}
        s = value;

        int len;
        // if (sprev < bytes.length)
        while (sprev + (len = enc.length(bytes, sprev, end)) < s) sprev += len;
    }

    private void opBackRefMulti() {
        int tlen = code[ip++];

        int i;
        loop:for (i=0; i<tlen; i++) {
            int mem = code[ip++];
            if (backrefInvalid(mem)) continue;

            int pstart = backrefStart(mem);
            int pend = backrefEnd(mem);

            int n = pend - pstart;
            if (s + n > range) {opFail(); return;}

            sprev = s;
            int swork = s;

            while (n-- > 0) {
                if (bytes[pstart++] != bytes[swork++]) continue loop;
            }

            s = swork;

            int len;

            // beyond string check
            if (sprev < range) {
                while (sprev + (len = enc.length(bytes, sprev, end)) < s) sprev += len;
            }

            ip += tlen - i  - 1; // * SIZE_MEMNUM (1)
            break; /* success */
        }
        if (i == tlen) {opFail(); return;}
    }

    private void opBackRefMultiIC() {
        int tlen = code[ip++];

        int i;
        loop:for (i=0; i<tlen; i++) {
            int mem = code[ip++];
            if (backrefInvalid(mem)) continue;

            int pstart = backrefStart(mem);
            int pend = backrefEnd(mem);

            int n = pend - pstart;
            if (s + n > range) {opFail(); return;}

            sprev = s;

            value = s;
            if (!stringCmpIC(regex.caseFoldFlag, pstart, this, n, end)) continue loop; // STRING_CMP_VALUE_IC
            s = value;

            int len;
            // if (sprev < bytes.length)
            while (sprev + (len = enc.length(bytes, sprev, end)) < s) sprev += len;

            ip += tlen - i  - 1; // * SIZE_MEMNUM (1)
            break;  /* success */
        }
        if (i == tlen) {opFail(); return;}
    }

    private boolean memIsInMemp(int mem, int num, int memp) {
        for (int i=0; i<num; i++) {
            int m = code[memp++];
            if (mem == m) return true;
        }
        return false;
    }

    // USE_BACKREF_AT_LEVEL // (s) and (end) implicit
    private boolean backrefMatchAtNestedLevel(boolean ignoreCase, int caseFoldFlag,
                                              int nest, int memNum, int memp) {
        int pend = -1;
        int level = 0;
        int k = stk - 1;

        while (k >= 0) {
            StackEntry e = stack[k];

            if (e.type == CALL_FRAME) {
                level--;
            } else if (e.type == RETURN) {
                level++;
            } else if (level == nest) {
                if (e.type == MEM_START) {
                    if (memIsInMemp(e.getMemNum(), memNum, memp)) {
                        int pstart = e.getMemPStr();
                        if (pend != -1) {
                            if (pend - pstart > end - s) return false; /* or goto next_mem; */
                            int p = pstart;

                            value = s;
                            if (ignoreCase) {
                                if (!stringCmpIC(caseFoldFlag, pstart, this, pend - pstart, end)) {
                                    return false; /* or goto next_mem; */
                                }
                            } else {
                                while (p < pend) {
                                    if (bytes[p++] != bytes[value++]) return false; /* or goto next_mem; */
                                }
                            }
                            s = value;

                            return true;
                        }
                    }
                } else if (e.type == MEM_END) {
                    if (memIsInMemp(e.getMemNum(), memNum, memp)) {
                        pend = e.getMemPStr();
                    }
                }
            }
            k--;
        }
        return false;
    }

    private void opBackRefAtLevel() {
        int ic      = code[ip++];
        int level   = code[ip++];
        int tlen    = code[ip++];

        sprev = s;
        if (backrefMatchAtNestedLevel(ic != 0, regex.caseFoldFlag, level, tlen, ip)) { // (s) and (end) implicit
            int len;
            while (sprev + (len = enc.length(bytes, sprev, end)) < s) sprev += len;
            ip += tlen; // * SIZE_MEMNUM
        } else {
            {opFail(); return;}
        }
    }

    /* no need: IS_DYNAMIC_OPTION() == 0 */
    private void opSetOptionPush() {
        // option = code[ip++]; // final for now
        pushAlt(ip, s, sprev);
        ip += OPSize.SET_OPTION + OPSize.FAIL;
    }

    private void opSetOption() {
        // option = code[ip++]; // final for now
    }

    private void opNullCheckStart() {
        int mem = code[ip++];
        pushNullCheckStart(mem, s);
    }

    private void nullCheckFound() {
        // null_check_found:
        /* empty loop founded, skip next instruction */
        switch(code[ip++]) {
        case OPCode.JUMP:
        case OPCode.PUSH:
            ip++;       // p += SIZE_RELADDR;
            break;
        case OPCode.REPEAT_INC:
        case OPCode.REPEAT_INC_NG:
        case OPCode.REPEAT_INC_SG:
        case OPCode.REPEAT_INC_NG_SG:
            ip++;        // p += SIZE_MEMNUM;
            break;
        default:
            throw new InternalException(ErrorMessages.ERR_UNEXPECTED_BYTECODE);
        } // switch
    }

    private void opNullCheckEnd() {
        int mem = code[ip++];
        int isNull = nullCheck(mem, s); /* mem: null check id */

        if (isNull != 0) {
            if (Config.DEBUG_MATCH) {
                Config.log.println("NULL_CHECK_END: skip  id:" + mem + ", s:" + s);
            }

            nullCheckFound();
        }
    }

    // USE_INFINITE_REPEAT_MONOMANIAC_MEM_STATUS_CHECK
    private void opNullCheckEndMemST() {
        int mem = code[ip++];   /* mem: null check id */
        int isNull = nullCheckMemSt(mem, s);

        if (isNull != 0) {
            if (Config.DEBUG_MATCH) {
                Config.log.println("NULL_CHECK_END_MEMST: skip  id:" + mem + ", s:" + s);
            }

            if (isNull == -1) {opFail(); return;}
            nullCheckFound();
        }
    }

    // USE_SUBEXP_CALL
    private void opNullCheckEndMemSTPush() {
        int mem = code[ip++];   /* mem: null check id */

        int isNull;
        if (Config.USE_MONOMANIAC_CHECK_CAPTURES_IN_ENDLESS_REPEAT) {
            isNull = nullCheckMemStRec(mem, s);
        } else {
            isNull = nullCheckRec(mem, s);
        }

        if (isNull != 0) {
            if (Config.DEBUG_MATCH) {
                Config.log.println("NULL_CHECK_END_MEMST_PUSH: skip  id:" + mem + ", s:" + s);
            }

            if (isNull == -1) {opFail(); return;}
            nullCheckFound();
        } else {
            pushNullCheckEnd(mem);
        }
    }

    private void opJump() {
        ip += code[ip] + 1;
    }

    private void opPush() {
        int addr = code[ip++];
        pushAlt(ip + addr, s, sprev);
    }

    // CEC
    private void opStateCheckPush() {
        int mem = code[ip++];
        if (stateCheckVal(s, mem)) {opFail(); return;}
        int addr = code[ip++];
        pushAltWithStateCheck(ip + addr, s, sprev, mem);
    }

    // CEC
    private void opStateCheckPushOrJump() {
        int mem = code[ip++];
        int addr= code[ip++];

        if (stateCheckVal(s, mem)) {
            ip += addr;
        } else {
            pushAltWithStateCheck(ip + addr, s, sprev, mem);
        }
    }

    // CEC
    private void opStateCheck() {
        int mem = code[ip++];
        if (stateCheckVal(s, mem)) {opFail(); return;}
        pushStateCheck(s, mem);
    }

    private void opPop() {
        popOne();
    }

    private void opPushOrJumpExact1() {
        int addr = code[ip++];
        // beyond string check
        if (s < range && code[ip] == bytes[s]) {
            ip++;
            pushAlt(ip + addr, s, sprev);
            return;
        }
        ip += addr + 1;
    }

    private void opPushIfPeekNext() {
        int addr = code[ip++];
        // beyond string check
        if (s < range && code[ip] == bytes[s]) {
            ip++;
            pushAlt(ip + addr, s, sprev);
            return;
        }
        ip++;
    }

    private void opRepeat() {
        int mem = code[ip++];   /* mem: OP_REPEAT ID */
        int addr= code[ip++];

        // ensure1();
        repeatStk[mem] = stk;
        pushRepeat(mem, ip);

        if (regex.repeatRangeLo[mem] == 0) { // lower
            pushAlt(ip + addr, s, sprev);
        }
    }

    private void opRepeatNG() {
        int mem = code[ip++];   /* mem: OP_REPEAT ID */
        int addr= code[ip++];

        // ensure1();
        repeatStk[mem] = stk;
        pushRepeat(mem, ip);

        if (regex.repeatRangeLo[mem] == 0) {
            pushAlt(ip, s, sprev);
            ip += addr;
        }
    }

    private void repeatInc(int mem, int si) {
        StackEntry e = stack[si];

        e.increaseRepeatCount();

        if (e.getRepeatCount() >= regex.repeatRangeHi[mem]) {
            /* end of repeat. Nothing to do. */
        } else if (e.getRepeatCount() >= regex.repeatRangeLo[mem]) {
            pushAlt(ip, s, sprev);
            ip = e.getRepeatPCode(); /* Don't use stkp after PUSH. */
        } else {
            ip = e.getRepeatPCode();
        }
        pushRepeatInc(si);
    }

    private void opRepeatInc() {
        int mem = code[ip++];   /* mem: OP_REPEAT ID */
        int si = repeatStk[mem];
        repeatInc(mem, si);
    }

    private void opRepeatIncSG() {
        int mem = code[ip++];   /* mem: OP_REPEAT ID */
        int si = getRepeat(mem);
        repeatInc(mem, si);
    }

    private void repeatIncNG(int mem, int si) {
        StackEntry e = stack[si];

        e.increaseRepeatCount();

        if (e.getRepeatCount() < regex.repeatRangeHi[mem]) {
            if (e.getRepeatCount() >= regex.repeatRangeLo[mem]) {
                int pcode = e.getRepeatPCode();
                pushRepeatInc(si);
                pushAlt(pcode, s, sprev);
            } else {
                ip = e.getRepeatPCode();
                pushRepeatInc(si);
            }
        } else if (e.getRepeatCount() == regex.repeatRangeHi[mem]) {
            pushRepeatInc(si);
        }
    }

    private void opRepeatIncNG() {
        int mem = code[ip++];
        int si = repeatStk[mem];
        repeatIncNG(mem, si);
    }

    private void opRepeatIncNGSG() {
        int mem = code[ip++];
        int si = getRepeat(mem);
        repeatIncNG(mem, si);
    }

    private void opPushPos() {
        pushPos(s, sprev);
    }

    private void opPopPos() {
        StackEntry e = stack[posEnd()];
        s    = e.getStatePStr();
        sprev= e.getStatePStrPrev();
    }

    private void opPushPosNot() {
        int addr = code[ip++];
        pushPosNot(ip + addr, s, sprev);
    }

    private void opFailPos() {
        popTilPosNot();
        opFail();
    }

    private void opPushStopBT() {
        pushStopBT();
    }

    private void opPopStopBT() {
        stopBtEnd();
    }

    private void opLookBehind() {
        int tlen = code[ip++];
        s = enc.stepBack(bytes, str, s, end, tlen);
        if (s == -1) {opFail(); return;}
        sprev = enc.prevCharHead(bytes, str, s, end);
    }

    private void opLookBehindSb() {
        int tlen = code[ip++];
        s -= tlen;
        if (s < str) {opFail(); return;}
        sprev = s == str ? -1 : s - 1;
    }

    private void opPushLookBehindNot() {
        int addr = code[ip++];
        int tlen = code[ip++];
        int q = enc.stepBack(bytes, str, s, end, tlen);
        if (q == -1) {
            /* too short case -> success. ex. /(?<!XXX)a/.match("a")
            If you want to change to fail, replace following line. */
            ip += addr;
            // return FAIL;
        } else {
            pushLookBehindNot(ip + addr, s, sprev);
            s = q;
            sprev = enc.prevCharHead(bytes, str, s, end);
        }
    }

    private void opFailLookBehindNot() {
        popTilLookBehindNot();
        opFail();
    }

    private void opCall() {
        int addr = code[ip++];
        pushCallFrame(ip);
        ip = addr; // absolute address
    }

    private void opReturn() {
        ip = sreturn();
        pushReturn();
    }

    private void opFail() {
        if (stack == null) {
            ip = regex.codeLength - 1;
            return;
        }


        StackEntry e = pop();
        ip    = e.getStatePCode();
        s     = e.getStatePStr();
        sprev = e.getStatePStrPrev();

        if (Config.USE_COMBINATION_EXPLOSION_CHECK) {
            if (e.getStateCheck() != 0) {
                e.type = STATE_CHECK_MARK;
                stk++;
            }
        }
    }

    private int finish() {
        return bestLen;
    }
}
