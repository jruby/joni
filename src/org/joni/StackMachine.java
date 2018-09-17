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
import static org.joni.Config.USE_CEC;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.joni.constants.internal.StackPopLevel;
import org.joni.constants.internal.StackType;

abstract class StackMachine extends Matcher implements StackType {
    protected static final int INVALID_INDEX = -1;

    protected StackEntry[]stack;
    protected int stk;  // stkEnd
    protected final int[]repeatStk;
    protected final int memStartStk, memEndStk;
    protected byte[] stateCheckBuff; // CEC, move to int[] ?
    protected int stateCheckBuffSize;

    protected StackMachine(Regex regex, Region region, byte[]bytes, int p , int end) {
        super(regex, region, bytes, p, end);
        stack = regex.requireStack ? fetchStack() : null;
        final int n;
        if (Config.USE_SUBEXP_CALL) {
            n = regex.numRepeat + ((regex.numMem + 1) << 1);
            memStartStk = regex.numRepeat;
            memEndStk   = memStartStk + regex.numMem + 1;
        } else {
            n = regex.numRepeat + (regex.numMem << 1);
            memStartStk = regex.numRepeat - 1;
            memEndStk   = memStartStk + regex.numMem;
            /* for index start from 1, mem_start_stk[1]..mem_start_stk[num_mem] */
            /* for index start from 1, mem_end_stk[1]..mem_end_stk[num_mem] */
        }
        repeatStk = n > 0 ? new int[n] : null;
    }

    protected final void stackInit() {
        if (stack != null) pushEnsured(ALT, regex.codeLength - 1); /* bottom stack */
        if (repeatStk != null) {
            for (int i = (Config.USE_SUBEXP_CALL ? 0 : 1); i <= regex.numMem; i++) {
                repeatStk[i + memStartStk] = repeatStk[i + memEndStk] = INVALID_INDEX;
            }
        }
    }

    private static StackEntry[] allocateStack() {
        StackEntry[]stack = new StackEntry[Config.INIT_MATCH_STACK_SIZE];
        stack[0] = USE_CEC ? new SCStackEntry() : new StackEntry();
        return stack;
    }

    private void doubleStack() {
        StackEntry[] newStack = new StackEntry[stack.length << 1];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }

    static final ThreadLocal<WeakReference<StackEntry[]>> stacks
            = new ThreadLocal<WeakReference<StackEntry[]>>();

    private static StackEntry[] fetchStack() {
        WeakReference<StackEntry[]> ref = stacks.get();
        StackEntry[] stack;
        if (ref == null) {
            stacks.set( new WeakReference<StackEntry[]>(stack = allocateStack()) );
        }
        else {
            stack = ref.get();
            if (stack == null) {
                stacks.set( new WeakReference<StackEntry[]>(stack = allocateStack()) );
            }
        }
        return stack;
    }

    private final StackEntry ensure1() {
        if (stk >= stack.length) doubleStack();
        StackEntry e = stack[stk];
        if (e == null) stack[stk] = e = USE_CEC ? new SCStackEntry() : new StackEntry();
        return e;
    }

    private final void pushType(int type) {
        ensure1().type = type;
        stk++;
    }

    // CEC

    // STATE_CHECK_POS
    private int stateCheckPos(int s, int snum) {
        return (s - str) * regex.numCombExpCheck + (snum - 1);
    }

    // STATE_CHECK_VAL
    protected final boolean stateCheckVal(int s, int snum) {
        if (stateCheckBuff != null) {
            int x = stateCheckPos(s, snum);
            return (stateCheckBuff[x / 8] & (1 << (x % 8))) != 0;
        }
        return false;
    }

    // ELSE_IF_STATE_CHECK_MARK
    private void stateCheckMark() {
        StackEntry e = stack[stk];
        int x = stateCheckPos(e.getStatePStr(), ((SCStackEntry)e).getStateCheck());
        stateCheckBuff[x / 8] |= (1 << (x % 8));
    }

    // STATE_CHECK_BUFF_INIT
    private static final int STATE_CHECK_BUFF_MALLOC_THRESHOLD_SIZE = 16;
    protected final void stateCheckBuffInit(int strLength, int offset, int stateNum) {
        if (stateNum > 0 && strLength >= Config.CHECK_STRING_THRESHOLD_LEN) {
            int size = ((strLength + 1) * stateNum + 7) >>> 3;
            offset = (offset * stateNum) >>> 3;

            if (size > 0 && offset < size && size < Config.CHECK_BUFF_MAX_SIZE) {
                if (size >= STATE_CHECK_BUFF_MALLOC_THRESHOLD_SIZE) {
                    stateCheckBuff = new byte[size];
                } else {
                    // same impl, reduce...
                    stateCheckBuff = new byte[size];
                }
                Arrays.fill(stateCheckBuff, offset, (size - offset), (byte)0);
                stateCheckBuffSize = size;
            } else {
                stateCheckBuff = null; // reduce
                stateCheckBuffSize = 0;
            }
        } else {
            stateCheckBuff = null; // reduce
            stateCheckBuffSize = 0;
        }
    }

    protected final void stateCheckBuffClear() {
        stateCheckBuff = null;
        stateCheckBuffSize = 0;
    }

    private void push(int type, int pat, int s, int prev, int pkeep) {
        StackEntry e = ensure1();
        e.type = type;
        e.setStatePCode(pat);
        e.setStatePStr(s);
        e.setStatePStrPrev(prev);
        if (USE_CEC) ((SCStackEntry)e).setStateCheck(0);
        e.setPKeep(pkeep);
        stk++;
    }

    private final void pushEnsured(int type, int pat) {
        StackEntry e = stack[stk];
        e.type = type;
        e.setStatePCode(pat);
        if (USE_CEC) ((SCStackEntry)e).setStateCheck(0);
        stk++;
    }

    protected final void pushAltWithStateCheck(int pat, int s, int sprev, int snum, int pkeep) {
        StackEntry e = ensure1();
        e.type = ALT;
        e.setStatePCode(pat);
        e.setStatePStr(s);
        e.setStatePStrPrev(sprev);
        if (USE_CEC) ((SCStackEntry)e).setStateCheck(stateCheckBuff != null ? snum : 0);
        e.setPKeep(pkeep);
        stk++;
    }

    protected final void pushStateCheck(int s, int snum) {
        if (stateCheckBuff != null) {
            StackEntry e = ensure1();
            e.type = STATE_CHECK_MARK;
            e.setStatePStr(s);
            ((SCStackEntry)e).setStateCheck(snum);
            stk++;
        }
    }

    protected final void pushAlt(int pat, int s, int prev, int pkeep) {
        push(ALT, pat, s, prev, pkeep);
    }

    protected final void pushPos(int s, int prev, int pkeep) {
        push(POS, -1 /*NULL_UCHARP*/, s, prev, pkeep);
    }

    protected final void pushPosNot(int pat, int s, int prev, int pkeep) {
        push(POS_NOT, pat, s, prev, pkeep);
    }

    protected final void pushStopBT() {
        pushType(STOP_BT);
    }

    protected final void pushLookBehindNot(int pat, int s, int sprev, int pkeep) {
        push(LOOK_BEHIND_NOT, pat, s, sprev, pkeep);
    }

    protected final void pushRepeat(int id, int pat) {
        StackEntry e = ensure1();
        e.type = REPEAT;
        e.setRepeatNum(id);
        e.setRepeatPCode(pat);
        e.setRepeatCount(0);
        stk++;
    }

    protected final void pushRepeatInc(int sindex) {
        StackEntry e = ensure1();
        e.type = REPEAT_INC;
        e.setSi(sindex);
        stk++;
    }

    protected final void pushMemStart(int mnum, int s) {
        StackEntry e = ensure1();
        e.type = MEM_START;
        e.setMemNum(mnum);
        e.setMemPstr(s);
        e.setMemStart(repeatStk[memStartStk + mnum]);
        e.setMemEnd(repeatStk[memEndStk + mnum]);
        repeatStk[memStartStk + mnum] = stk;
        repeatStk[memEndStk + mnum] = INVALID_INDEX;
        stk++;
    }

    protected final void pushMemEnd(int mnum, int s) {
        StackEntry e = ensure1();
        e.type = MEM_END;
        e.setMemNum(mnum);
        e.setMemPstr(s);
        e.setMemStart(repeatStk[memStartStk + mnum]);
        e.setMemEnd(repeatStk[memEndStk + mnum]);
        repeatStk[memEndStk + mnum] = stk;
        stk++;
    }

    protected final void pushMemEndMark(int mnum) {
        StackEntry e = ensure1();
        e.type = MEM_END_MARK;
        e.setMemNum(mnum);
        stk++;
    }

    protected final int getMemStart(int mnum) {
        int level = 0;
        int stkp = stk;

        while (stkp > 0) {
            stkp--;
            StackEntry e = stack[stkp];
            if ((e.type & MASK_MEM_END_OR_MARK) != 0 && e.getMemNum() == mnum) {
                level++;
            } else if (e.type == MEM_START && e.getMemNum() == mnum) {
                if (level == 0) break;
                level--;
            }
        }
        return stkp;
    }

    protected final void pushNullCheckStart(int cnum, int s) {
        StackEntry e = ensure1();
        e.type = NULL_CHECK_START;
        e.setNullCheckNum(cnum);
        e.setNullCheckPStr(s);
        stk++;
    }

    protected final void pushNullCheckEnd(int cnum) {
        StackEntry e = ensure1();
        e.type = NULL_CHECK_END;
        e.setNullCheckNum(cnum);
        stk++;
    }

    protected final void pushCallFrame(int pat) {
        StackEntry e = ensure1();
        e.type = CALL_FRAME;
        e.setCallFrameRetAddr(pat);
        stk++;
    }

    protected final void pushReturn() {
        StackEntry e = ensure1();
        e.type = RETURN;
        stk++;
    }

    protected final void pushAbsent() {
        StackEntry e = ensure1();
        e.type = ABSENT;
        stk++;
    }

    protected final void pushAbsentPos(int start, int end) {
        StackEntry e = ensure1();
        e.type = ABSENT_POS;
        e.setAbsentStr(start);
        e.setAbsentEndStr(end);
        stk++;
    }

    protected final void popOne() {
        stk--;
    }

    protected final StackEntry pop() {
        switch (regex.stackPopLevel) {
        case StackPopLevel.FREE:
            return popFree();
        case StackPopLevel.MEM_START:
            return popMemStart();
        default:
            return popDefault();
        }
    }

    private StackEntry popFree() {
        while (true) {
            StackEntry e = stack[--stk];
            if ((e.type & MASK_POP_USED) != 0) {
                return e;
            } else if (USE_CEC) {
                if (e.type == STATE_CHECK_MARK) stateCheckMark();
            }
        }
    }

    private StackEntry popMemStart() {
        while (true) {
            StackEntry e = stack[--stk];
            if ((e.type & MASK_POP_USED) != 0) {
                return e;
            } else if (e.type == MEM_START) {
                repeatStk[memStartStk + e.getMemNum()] = e.getMemStart();
                repeatStk[memEndStk + e.getMemNum()] = e.getMemEnd();
            } else if (USE_CEC) {
                if (e.type == STATE_CHECK_MARK) stateCheckMark();
            }
        }
    }

    private void popRewrite(StackEntry e) {
        if (e.type == MEM_START) {
            repeatStk[memStartStk + e.getMemNum()] = e.getMemStart();
            repeatStk[memEndStk + e.getMemNum()] = e.getMemEnd();
        } else if (e.type == REPEAT_INC) {
            stack[e.getSi()].decreaseRepeatCount();
        } else if (e.type == MEM_END) {
            repeatStk[memStartStk + e.getMemNum()] = e.getMemStart();
            repeatStk[memEndStk + e.getMemNum()] = e.getMemEnd();
        } else if (USE_CEC) {
            if (e.type == STATE_CHECK_MARK) stateCheckMark();
        }
    }

    private StackEntry popDefault() {
        while (true) {
            StackEntry e = stack[--stk];
            if ((e.type & MASK_POP_USED) != 0) return e; else popRewrite(e);
        }
    }

    protected final void popTilPosNot() {
        while (true) {
            StackEntry e = stack[--stk];
            if (e.type == POS_NOT) break; else popRewrite(e);
        }
    }

    protected final void popTilLookBehindNot() {
        while (true) {
            StackEntry e = stack[--stk];
            if (e.type == LOOK_BEHIND_NOT) break; else popRewrite(e);
        }
    }

    protected final void popTilAbsent() {
        while (true) {
            StackEntry e = stack[--stk];
            if (e.type == ABSENT) break; else popRewrite(e);
        }
    }

    protected final int posEnd() {
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];
            if ((e.type & MASK_TO_VOID_TARGET) != 0) {
                e.type = VOID;
            } else if (e.type == POS) {
                e.type = VOID;
                break;
            }
        }
        return k;
    }

    protected final void stopBtEnd() {
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if ((e.type & MASK_TO_VOID_TARGET) != 0) {
                e.type = VOID;
            } else if (e.type == STOP_BT) {
                e.type = VOID;
                break;
            }
        }
    }

    // int for consistency with other null check routines
    protected final int nullCheck(int id, int s) {
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == NULL_CHECK_START) {
                if (e.getNullCheckNum() == id) {
                    return e.getNullCheckPStr() == s ? 1 : 0;
                }
            }
        }
    }

    protected final int nullCheckRec(int id, int s) {
        int level = 0;
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == NULL_CHECK_START) {
                if (e.getNullCheckNum() == id) {
                    if (level == 0) {
                        return e.getNullCheckPStr() == s ? 1 : 0;
                    } else {
                        level--;
                    }
                }
            } else if (e.type == NULL_CHECK_END) {
                level++;
            }
        }
    }

    protected final int nullCheckMemSt(int id, int s) {
        int k = stk;
        int isNull;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == NULL_CHECK_START) {
                if (e.getNullCheckNum() == id) {
                    if (e.getNullCheckPStr() != s) {
                        isNull = 0;
                        break;
                    } else {
                        int endp;
                        isNull = 1;
                        while (k < stk) {
                            e = stack[k++];
                            if (e.type == MEM_START) {
                                if (e.getMemEnd() == INVALID_INDEX) {
                                    isNull = 0;
                                    break;
                                }
                                if (bsAt(regex.btMemEnd, e.getMemNum())) {
                                    endp = stack[e.getMemEnd()].getMemPStr();
                                } else {
                                    endp = e.getMemEnd();
                                }
                                if (stack[e.getMemStart()].getMemPStr() != endp) {
                                    isNull = 0;
                                    break;
                                } else if (endp != s) {
                                    isNull = -1; /* empty, but position changed */
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return isNull;
    }

    protected final int nullCheckMemStRec(int id, int s) {
        int level = 0;
        int k = stk;
        int isNull;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == NULL_CHECK_START) {
                if (e.getNullCheckNum() == id) {
                    if (level == 0) {
                        if (e.getNullCheckPStr() != s) {
                            isNull = 0;
                            break;
                        } else {
                            int endp;
                            isNull = 1;
                            while (k < stk) {
                                if (e.type == MEM_START) {
                                    if (e.getMemEnd() == INVALID_INDEX) {
                                        isNull = 0;
                                        break;
                                    }
                                    if (bsAt(regex.btMemEnd, e.getMemNum())) {
                                        endp = stack[e.getMemEnd()].getMemPStr();
                                    } else {
                                        endp = e.getMemEnd();
                                    }
                                    if (stack[e.getMemStart()].getMemPStr() != endp) {
                                        isNull = 0;
                                        break;
                                    } else if (endp != s) {
                                        isNull = -1;; /* empty, but position changed */
                                    }
                                }
                                k++;
                                e = stack[k];
                            }
                            break;
                        }
                    } else {
                        level--;
                    }
                }
            } else if (e.type == NULL_CHECK_END) {
                if (e.getNullCheckNum() == id) level++;
            }
        }
        return isNull;
    }

    protected final int getRepeat(int id) {
        int level = 0;
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == REPEAT) {
                if (level == 0) {
                    if (e.getRepeatNum() == id) return k;
                }
            } else if (e.type == CALL_FRAME) {
                level--;
            } else if (e.type == RETURN) {
                level++;
            }
        }
    }

    protected final int sreturn() {
        int level = 0;
        int k = stk;
        while (true) {
            k--;
            StackEntry e = stack[k];

            if (e.type == CALL_FRAME) {
                if (level == 0) {
                    return e.getCallFrameRetAddr();
                } else {
                    level--;
                }
            } else if (e.type == RETURN) {
                level++;
            }
        }
    }
}
