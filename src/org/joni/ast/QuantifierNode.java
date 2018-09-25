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

import static org.joni.ast.QuantifierNode.ReduceType.A;
import static org.joni.ast.QuantifierNode.ReduceType.AQ;
import static org.joni.ast.QuantifierNode.ReduceType.ASIS;
import static org.joni.ast.QuantifierNode.ReduceType.DEL;
import static org.joni.ast.QuantifierNode.ReduceType.PQ_Q;
import static org.joni.ast.QuantifierNode.ReduceType.P_QQ;
import static org.joni.ast.QuantifierNode.ReduceType.QQ;

import org.joni.Config;
import org.joni.ScanEnvironment;
import org.joni.constants.internal.TargetInfo;

public final class QuantifierNode extends StateNode {
    public static final int REPEAT_INFINITE = -1;
    public Node target;
    public int lower;
    public int upper;
    public boolean greedy;

    public int targetEmptyInfo;

    public Node headExact;
    public Node nextHeadExact;
    public boolean isRefered;           /* include called node. don't eliminate even if {0} */

    // USE_COMBINATION_EXPLOSION_CHECK
    public int  combExpCheckNum;        /* 1,2,3...: check,  0: no check  */

    public QuantifierNode(int lower, int upper, boolean byNumber) {
        super(QTFR);
        this.lower = lower;
        this.upper = upper;
        greedy = true;
        targetEmptyInfo = TargetInfo.ISNOT_EMPTY;

        if (byNumber) setByNumber();
    }

    @Override
    protected void setChild(Node child) {
        target = child;
    }

    @Override
    protected Node getChild() {
        return target;
    }

    public void setTarget(Node tgt) {
        target = tgt;
        tgt.parent = this;
    }

    @Override
    public String getName() {
        return "Quantifier";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder(super.toString(level));
        value.append("\n  lower: " + lower);
        value.append(", upper: " + upper);
        value.append(", greedy: " + greedy);
        value.append(", isRefered: " + isRefered);
        value.append(", targetEmptyInfo: " + targetEmptyInfo);
        value.append(", combExpCheckNum: " + combExpCheckNum);
        value.append("\n  headExact: " + pad(headExact, level + 1));
        value.append("\n  nextHeadExact: " + pad(nextHeadExact, level + 1));
        value.append("\n  target: " + pad(target, level + 1));
        return value.toString();
    }

    public boolean isAnyCharStar() {
        return greedy && isRepeatInfinite(upper) && target.getType() == CANY;
    }

    /* ?:0, *:1, +:2, ??:3, *?:4, +?:5 */
    protected int popularNum() {
        if (greedy) {
            if (lower == 0) {
                if (upper == 1) return 0;
                else if (isRepeatInfinite(upper)) return 1;
            } else if (lower == 1) {
                if (isRepeatInfinite(upper)) return 2;
            }
        } else {
            if (lower == 0) {
                if (upper == 1) return 3;
                else if (isRepeatInfinite(upper)) return 4;
            } else if (lower == 1) {
                if (isRepeatInfinite(upper)) return 5;
            }
        }
        return -1;
    }

    protected void copy(QuantifierNode other) {
        state = other.state;
        setTarget(other.target);
        other.target = null;
        lower = other.lower;
        upper = other.upper;
        greedy = other.greedy;
        targetEmptyInfo = other.targetEmptyInfo;
        headExact = other.headExact;
        nextHeadExact = other.nextHeadExact;
        isRefered = other.isRefered;
        combExpCheckNum = other.combExpCheckNum;
    }

    static enum ReduceType {
        ASIS,       /* as is */
        DEL,        /* delete parent */
        A,          /* to '*'    */
        AQ,         /* to '*?'   */
        QQ,         /* to '??'   */
        P_QQ,       /* to '+)??' */
        PQ_Q,       /* to '+?)?' */
    }

    static final ReduceType[][]REDUCE_TABLE = {
      {DEL,     A,      A,      QQ,     AQ,     ASIS}, /* '?'  */
      {DEL,     DEL,    DEL,    P_QQ,   P_QQ,   DEL},  /* '*'  */
      {A,       A,      DEL,    ASIS,   P_QQ,   DEL},  /* '+'  */
      {DEL,     AQ,     AQ,     DEL,    AQ,     AQ},   /* '??' */
      {DEL,     DEL,    DEL,    DEL,    DEL,    DEL},  /* '*?' */
      {ASIS,    PQ_Q,   DEL,    AQ,     AQ,     DEL}   /* '+?' */
    };


    public void reduceNestedQuantifier(QuantifierNode other) {
        int pnum = popularNum();
        int cnum = other.popularNum();

        if (pnum < 0 || cnum < 0) return;

        switch(REDUCE_TABLE[cnum][pnum]) {
        case DEL:
            // no need to set the parent here...
            copy(other);
            break;

        case A:
            setTarget(other.target);
            lower = 0;
            upper = REPEAT_INFINITE;
            greedy = true;
            break;

        case AQ:
            setTarget(other.target);
            lower = 0;
            upper = REPEAT_INFINITE;
            greedy = false;
            break;

        case QQ:
            setTarget(other.target);
            lower = 0;
            upper = 1;
            greedy = false;
            break;

        case P_QQ:
            setTarget(other);
            lower = 0;
            upper = 1;
            greedy = false;
            other.lower = 1;
            other.upper = REPEAT_INFINITE;
            other.greedy = true;
            return;

        case PQ_Q:
            setTarget(other);
            lower = 0;
            upper = 1;
            greedy = true;
            other.lower = 1;
            other.upper = REPEAT_INFINITE;
            other.greedy = false;
            return;

        case ASIS:
            setTarget(other);
            return;
        }
        // ??? remove the parent from target ???
        other.target = null; // remove target from reduced quantifier
    }

    static final String PopularQStr[] = new String[] {"?", "*", "+", "??", "*?", "+?"};
    static final String ReduceQStr[] = new String[] {"", "", "*", "*?", "??", "+ and ??", "+? and ?"};

    public int setQuantifier(Node tgt, boolean group, ScanEnvironment env, byte[]bytes, int p, int end) {
        if (lower == 1 && upper == 1) {
            if (env.syntax.op3OptionECMAScript()) {
                setTarget(tgt);
            }
            return 1;
        }

        switch(tgt.getType()) {

        case STR:
            if (!group) {
                StringNode sn = (StringNode)tgt;
                if (sn.canBeSplit(env.enc)) {
                    StringNode n = sn.splitLastChar(env.enc);
                    if (n != null) {
                        setTarget(n);
                        return 2;
                    }
                }
            }
            break;

        case QTFR:
            /* check redundant double repeat. */
            /* verbose warn (?:.?)? etc... but not warn (.?)? etc... */
            QuantifierNode qnt = (QuantifierNode)tgt;
            int nestQNum = popularNum();
            int targetQNum = qnt.popularNum();

            if (Config.USE_WARNING_REDUNDANT_NESTED_REPEAT_OPERATOR) {
                if (nestQNum >= 0 && targetQNum >= 0 && env.syntax.warnReduntantNestedRepeat()) {
                    switch(REDUCE_TABLE[targetQNum][nestQNum]) {
                    case ASIS:
                        break;
                    case DEL:
                        env.warnings.warn("regular expression has redundant nested repeat operator " + PopularQStr[targetQNum] + " /" + new String(bytes, p, end) + "/");
                        break;
                    default:
                        env.warnings.warn("nested repeat operator '" + PopularQStr[targetQNum] + "' and '" + PopularQStr[nestQNum] +
                                "' was replaced with '" + ReduceQStr[REDUCE_TABLE[targetQNum][nestQNum].ordinal()] + "' in regular expression " + "/" + new String(bytes, p, end) + "/");
                    }
                }
            } // USE_WARNING_REDUNDANT_NESTED_REPEAT_OPERATOR

            if (targetQNum >= 0) {
                if (nestQNum >= 0) {
                    reduceNestedQuantifier(qnt);
                    return 0;
                } else if (targetQNum == 1 || targetQNum == 2) { /* * or + */
                    /* (?:a*){n,m}, (?:a+){n,m} => (?:a*){n,n}, (?:a+){n,n} */
                    if (!isRepeatInfinite(upper) && upper > 1 && greedy) {
                        upper = lower == 0 ? 1 : lower;
                    }
                }
            }

        default:
            break;
        }

        setTarget(tgt);
        return 0;
    }

    public static boolean isRepeatInfinite(int n) {
        return n == REPEAT_INFINITE;
    }
}
