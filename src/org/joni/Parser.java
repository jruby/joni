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

import static org.joni.BitStatus.bsOnAtSimple;
import static org.joni.BitStatus.bsOnOff;
import static org.joni.Option.isAsciiRange;
import static org.joni.Option.isDontCaptureGroup;
import static org.joni.Option.isIgnoreCase;
import static org.joni.Option.isPosixBracketAllRange;

import org.jcodings.ObjPtr;
import org.jcodings.Ptr;
import org.jcodings.constants.CharacterType;
import org.jcodings.constants.PosixBracket;
import org.jcodings.exception.InternalException;
import org.joni.ast.AnchorNode;
import org.joni.ast.AnyCharNode;
import org.joni.ast.BackRefNode;
import org.joni.ast.CClassNode;
import org.joni.ast.CClassNode.CCSTATE;
import org.joni.ast.CClassNode.CCStateArg;
import org.joni.ast.CClassNode.CCVALTYPE;
import org.joni.ast.CTypeNode;
import org.joni.ast.CallNode;
import org.joni.ast.EncloseNode;
import org.joni.ast.ListNode;
import org.joni.ast.Node;
import org.joni.ast.QuantifierNode;
import org.joni.ast.StringNode;
import org.joni.constants.internal.AnchorType;
import org.joni.constants.internal.EncloseType;
import org.joni.constants.internal.NodeType;
import org.joni.constants.internal.TokenType;
import org.joni.exception.ErrorMessages;

class Parser extends Lexer {
    protected int returnCode; // return code used by parser methods (they itself return parsed nodes)
                              // this approach will not affect recursive calls

    protected Parser(Regex regex, Syntax syntax, byte[]bytes, int p, int end, WarnCallback warnings) {
        super(regex, syntax, bytes, p, end, warnings);
    }

    private static final int POSIX_BRACKET_NAME_MIN_LEN            = 4;
    private static final int POSIX_BRACKET_CHECK_LIMIT_LENGTH      = 20;
    private static final byte BRACKET_END[]                        = ":]".getBytes();
    private boolean parsePosixBracket(CClassNode cc, CClassNode ascCc) {
        mark();

        boolean not;
        if (peekIs('^')) {
            inc();
            not = true;
        } else {
            not = false;
        }
        if (enc.strLength(bytes, p, stop) >= POSIX_BRACKET_NAME_MIN_LEN + 3) { // else goto not_posix_bracket
            boolean asciiRange = isAsciiRange(env.option) && !isPosixBracketAllRange(env.option);

            for (int i=0; i<PosixBracket.PBSNamesLower.length; i++) {
                byte[]name = PosixBracket.PBSNamesLower[i];
                // hash lookup here ?
                if (enc.strNCmp(bytes, p, stop, name, 0, name.length) == 0) {
                    p = enc.step(bytes, p, stop, name.length);
                    if (enc.strNCmp(bytes, p, stop, BRACKET_END, 0, BRACKET_END.length) != 0) {
                        newSyntaxException(INVALID_POSIX_BRACKET_TYPE);
                    }
                    int ctype = PosixBracket.PBSValues[i];
                    cc.addCType(ctype, not, asciiRange, env, this);
                    if (ascCc != null) {
                        if (ctype != CharacterType.WORD && ctype != CharacterType.ASCII && !asciiRange) {
                            ascCc.addCType(ctype, not, asciiRange, env, this);
                        }
                    }
                    inc();
                    inc();
                    return false;
                }
            }

        }

        // not_posix_bracket:
        c = 0;
        int i= 0;
        while (left() && ((c=peek()) != ':') && c != ']') {
            inc();
            if (++i > POSIX_BRACKET_CHECK_LIMIT_LENGTH) break;
        }

        if (c == ':' && left()) {
            inc();
            if (left()) {
                fetch();
                if (c == ']') newSyntaxException(INVALID_POSIX_BRACKET_TYPE);
            }
        }
        restore();
        return true; /* 1: is not POSIX bracket, but no error. */
    }

    private boolean codeExistCheck(int code, boolean ignoreEscaped) {
        mark();

        boolean inEsc = false;
        while (left()) {
            if (ignoreEscaped && inEsc) {
                inEsc = false;
            } else {
                fetch();
                if (c == code) {
                    restore();
                    return true;
                }
                if (c == syntax.metaCharTable.esc) inEsc = true;
            }
        }

        restore();
        return false;
    }

    private CClassNode parseCharClass(ObjPtr<CClassNode> ascNode) {
        final boolean neg;
        CClassNode cc, prevCc = null, ascCc = null, ascPrevCc = null, workCc = null, ascWorkCc = null;
        CCStateArg arg = new CCStateArg();

        fetchTokenInCC();
        if (token.type == TokenType.CHAR && token.getC() == '^' && !token.escaped) {
            neg = true;
            fetchTokenInCC();
        } else {
            neg = false;
        }

        if (token.type == TokenType.CC_CLOSE && !syntax.op3OptionECMAScript()) {
            if (!codeExistCheck(']', true)) newSyntaxException(EMPTY_CHAR_CLASS);
            env.ccEscWarn("]");
            token.type = TokenType.CHAR; /* allow []...] */
        }

        cc = new CClassNode();
        if (isIgnoreCase(env.option)) {
            ascCc = ascNode.p = new CClassNode();
        }

        boolean andStart = false;
        arg.state = CCSTATE.START;
        while (token.type != TokenType.CC_CLOSE) {
            boolean fetched = false;

            switch (token.type) {
            case CHAR:
                final int len;
                if (token.getCode() >= BitSet.SINGLE_BYTE_SIZE || (len = enc.codeToMbcLength(token.getC())) > 1) {
                    arg.inType = CCVALTYPE.CODE_POINT;
                } else {
                    arg.inType = CCVALTYPE.SB; // sb_char:
                }
                arg.to = token.getC();
                arg.toIsRaw = false;
                parseCharClassValEntry2(cc, ascCc, arg); // goto val_entry2
                break;

            case RAW_BYTE:
                if (!enc.isSingleByte() && token.base != 0) { /* tok->base != 0 : octal or hexadec. */
                    byte[]buf = new byte[Config.ENC_MBC_CASE_FOLD_MAXLEN];
                    int psave = p;
                    int base = token.base;
                    buf[0] = (byte)token.getC();
                    int i;
                    for (i=1; i<enc.maxLength(); i++) {
                        fetchTokenInCC();
                        if (token.type != TokenType.RAW_BYTE || token.base != base) {
                            fetched = true;
                            break;
                        }
                        buf[i] = (byte)token.getC();
                    }
                    if (i < enc.minLength()) newValueException(TOO_SHORT_MULTI_BYTE_STRING);

                    len = enc.length(buf, 0, i);
                    if (i < len) {
                        newValueException(TOO_SHORT_MULTI_BYTE_STRING);
                    } else if (i > len) { /* fetch back */
                        p = psave;
                        for (i=1; i<len; i++) fetchTokenInCC();
                        fetched = false;
                    }
                    if (i == 1) {
                        arg.to = buf[0] & 0xff;
                        arg.inType = CCVALTYPE.SB; // goto raw_single
                    } else {
                        arg.to = enc.mbcToCode(buf, 0, buf.length);
                        arg.inType = CCVALTYPE.CODE_POINT;
                    }
                } else {
                    arg.to = token.getC();
                    arg.inType = CCVALTYPE.SB; // raw_single:
                }
                arg.toIsRaw = true;
                parseCharClassValEntry2(cc, ascCc, arg); // goto val_entry2
                break;

            case CODE_POINT:
                arg.to = token.getCode();
                arg.toIsRaw = true;
                parseCharClassValEntry(cc, ascCc, arg); // val_entry:, val_entry2
                break;

            case POSIX_BRACKET_OPEN:
                if (parsePosixBracket(cc, ascCc)) { /* true: is not POSIX bracket */
                    env.ccEscWarn("[");
                    p = token.backP;
                    arg.to = token.getC();
                    arg.toIsRaw = false;
                    parseCharClassValEntry(cc, ascCc, arg); // goto val_entry
                    break;
                }
                cc.nextStateClass(arg, ascCc, env); // goto next_class
                break;

            case CHAR_TYPE:
                cc.addCType(token.getPropCType(), token.getPropNot(), isAsciiRange(env.option), env, this);
                if (ascCc != null) {
                    if (token.getPropCType() != CharacterType.WORD) {
                        ascCc.addCType(token.getPropCType(), token.getPropNot(), isAsciiRange(env.option), env, this);
                    }
                }
                cc.nextStateClass(arg, ascCc, env); // next_class:
                break;

            case CHAR_PROPERTY:
                int ctype = fetchCharPropertyToCType();
                cc.addCType(ctype, token.getPropNot(), false, env, this);
                if (ascCc != null) {
                    if (ctype != CharacterType.ASCII) {
                        ascCc.addCType(ctype, token.getPropNot(), false, env, this);
                    }
                }
                cc.nextStateClass(arg, ascCc, env); // goto next_class
                break;

            case CC_RANGE:
                if (arg.state == CCSTATE.VALUE) {
                    fetchTokenInCC();
                    fetched = true;
                    if (token.type == TokenType.CC_CLOSE) { /* allow [x-] */
                        parseCharClassRangeEndVal(cc, ascCc, arg); // range_end_val:, goto val_entry;
                        break;
                    } else if (token.type == TokenType.CC_AND) {
                        env.ccEscWarn("-");
                        parseCharClassRangeEndVal(cc, ascCc, arg); // goto range_end_val
                        break;
                    }
                    if (arg.type == CCVALTYPE.CLASS) newValueException(UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS);
                    arg.state = CCSTATE.RANGE;
                } else if (arg.state == CCSTATE.START) {
                    arg.to = token.getC(); /* [-xa] is allowed */
                    arg.toIsRaw = false;
                    fetchTokenInCC();
                    fetched = true;
                    if (token.type == TokenType.CC_RANGE || andStart) env.ccEscWarn("-"); /* [--x] or [a&&-x] is warned. */
                    parseCharClassValEntry(cc, ascCc, arg); // goto val_entry
                    break;
                } else if (arg.state == CCSTATE.RANGE) {
                    env.ccEscWarn("-");
                    parseCharClassSbChar(cc, ascCc, arg); // goto sb_char /* [!--x] is allowed */
                    break;
                } else { /* CCS_COMPLETE */
                    fetchTokenInCC();
                    fetched = true;
                    if (token.type == TokenType.CC_CLOSE) { /* allow [a-b-] */
                        parseCharClassRangeEndVal(cc, ascCc, arg); // goto range_end_val
                        break;
                    } else if (token.type == TokenType.CC_AND) {
                        env.ccEscWarn("-");
                        parseCharClassRangeEndVal(cc, ascCc, arg); // goto range_end_val
                        break;
                    }

                    if (syntax.allowDoubleRangeOpInCC()) {
                        env.ccEscWarn("-");
                        // parseCharClassSbChar(cc, ascCc, arg); // goto sb_char /* [0-9-a] is allowed as [0-9\-a] */
                        parseCharClassRangeEndVal(cc, ascCc, arg); // goto range_end_val
                        break;
                    }
                    newSyntaxException(UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS);
                }
                break;

            case CC_CC_OPEN: /* [ */
                ObjPtr<CClassNode> ascPtr = new ObjPtr<CClassNode>();
                CClassNode acc = parseCharClass(ascPtr);
                cc.or(acc, env);
                if (ascPtr.p != null) {
                    ascCc.or(ascPtr.p, env);
                }
                break;

            case CC_AND:     /* && */
                if (arg.state == CCSTATE.VALUE) {
                    arg.to = 0;
                    arg.toIsRaw = false;
                    cc.nextStateValue(arg, ascCc, env);
                }
                /* initialize local variables */
                andStart = true;
                arg.state = CCSTATE.START;
                if (prevCc != null) {
                    prevCc.and(cc, env);
                    if (ascCc != null) {
                        ascPrevCc.and(ascCc, env);
                    }
                } else {
                    prevCc = cc;
                    if (workCc == null) workCc = new CClassNode();
                    cc = workCc;
                    if (ascCc != null) {
                        ascPrevCc = ascCc;
                        if (ascWorkCc == null) ascWorkCc = new CClassNode();
                        ascCc = ascWorkCc;
                    }
                }
                cc.clear();
                if (ascCc != null) ascCc.clear();
                break;

            case EOT:
                newSyntaxException(PREMATURE_END_OF_CHAR_CLASS);

            default:
                newInternalException(PARSER_BUG);
            } // switch

            if (!fetched) fetchTokenInCC();

        } // while

        if (arg.state == CCSTATE.VALUE) {
            arg.to = 0;
            arg.toIsRaw = false;
            cc.nextStateValue(arg, ascCc, env);
        }

        if (prevCc != null) {
            prevCc.and(cc, env);
            cc = prevCc;
            if (ascCc != null) {
                ascPrevCc.and(ascCc, env);
                ascCc = ascPrevCc;
            }
        }

        if (neg) {
            cc.setNot();
            if (ascCc != null) ascCc.setNot();
        } else {
            cc.clearNot();
            if (ascCc != null) ascCc.clearNot();
        }

        if (cc.isNot() && syntax.notNewlineInNegativeCC()) {
            if (!cc.isEmpty()) { // ???
                final int NEW_LINE = 0x0a;
                if (enc.isNewLine(NEW_LINE)) {
                    if (enc.codeToMbcLength(NEW_LINE) == 1) {
                        cc.bs.set(env, NEW_LINE);
                    } else {
                        cc.addCodeRange(env, NEW_LINE, NEW_LINE);
                    }
                }
            }
        }

        return cc;
    }

    private void parseCharClassSbChar(CClassNode cc, CClassNode ascCc, CCStateArg arg) {
        arg.inType = CCVALTYPE.SB;
        arg.to = token.getC();
        arg.toIsRaw = false;
        parseCharClassValEntry2(cc, ascCc, arg); // goto val_entry2
    }

    private void parseCharClassRangeEndVal(CClassNode cc, CClassNode ascCc, CCStateArg arg) {
        arg.to = '-';
        arg.toIsRaw = false;
        parseCharClassValEntry(cc, ascCc, arg); // goto val_entry
    }

    private void parseCharClassValEntry(CClassNode cc, CClassNode ascCc, CCStateArg arg) {
        int len = enc.codeToMbcLength(arg.to);
        arg.inType = len == 1 ? CCVALTYPE.SB : CCVALTYPE.CODE_POINT;
        parseCharClassValEntry2(cc, ascCc, arg); // val_entry2:
    }

    private void parseCharClassValEntry2(CClassNode cc, CClassNode ascCc, CCStateArg arg) {
        cc.nextStateValue(arg, ascCc, env);
    }

    private Node parseEnclose(TokenType term) {
        Node node = null;

        if (!left()) newSyntaxException(END_PATTERN_WITH_UNMATCHED_PARENTHESIS);

        int option = env.option;

        if (peekIs('?') && syntax.op2QMarkGroupEffect()) {
            inc();
            if (!left()) newSyntaxException(END_PATTERN_IN_GROUP);

            boolean listCapture = false;

            fetch();
            switch(c) {
            case ':':  /* (?:...) grouping only */
                fetchToken(); // group:
                node = parseSubExp(term);
                returnCode = 1; /* group */
                return node;
            case '=':
                node = new AnchorNode(AnchorType.PREC_READ);
                break;
            case '!':  /*         preceding read */
                node = new AnchorNode(AnchorType.PREC_READ_NOT);
                if (syntax.op3OptionECMAScript()) {
                    env.pushPrecReadNotNode(node);
                }
                break;
            case '>':  /* (?>...) stop backtrack */
                node = new EncloseNode(EncloseType.STOP_BACKTRACK); // node_new_enclose
                break;
            case '~': /* (?~...) absent operator */
                if (syntax.op2QMarkTildeAbsent()) {
                    node = new EncloseNode(EncloseType.ABSENT);
                    break;
                } else {
                    newSyntaxException(UNDEFINED_GROUP_OPTION);
                }
            case '\'':
                if (Config.USE_NAMED_GROUP) {
                    if (syntax.op2QMarkLtNamedGroup()) {
                        listCapture = false; // goto named_group1
                        node = parseEncloseNamedGroup2(listCapture);
                        break;
                    } else {
                        newSyntaxException(UNDEFINED_GROUP_OPTION);
                    }
                } // USE_NAMED_GROUP
                break;
            case '<':  /* look behind (?<=...), (?<!...) */
                fetch();
                if (c == '=') {
                    node = new AnchorNode(AnchorType.LOOK_BEHIND);
                } else if (c == '!') {
                    node = new AnchorNode(AnchorType.LOOK_BEHIND_NOT);
                } else {
                    if (Config.USE_NAMED_GROUP) {
                        if (syntax.op2QMarkLtNamedGroup()) {
                            unfetch();
                            c = '<';

                            listCapture = false; // named_group1:
                            node = parseEncloseNamedGroup2(listCapture); // named_group2:
                            break;
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }

                    } else { // USE_NAMED_GROUP
                        newSyntaxException(UNDEFINED_GROUP_OPTION);
                    } // USE_NAMED_GROUP
                }
                break;
            case '@':
                if (syntax.op2AtMarkCaptureHistory()) {
                    if (Config.USE_NAMED_GROUP) {
                        if (syntax.op2QMarkLtNamedGroup()) {
                            fetch();
                            if (c == '<' || c == '\'') {
                                listCapture = true;
                                node = parseEncloseNamedGroup2(listCapture); // goto named_group2 /* (?@<name>...) */
                            }
                            unfetch();
                        }
                    } // USE_NAMED_GROUP
                    EncloseNode en = EncloseNode.newMemory(env.option, false);
                    int num = env.addMemEntry();
                    if (num >= BitStatus.BIT_STATUS_BITS_NUM) newValueException(GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY);
                    en.regNum = num;
                    node = en;
                } else {
                    newSyntaxException(UNDEFINED_GROUP_OPTION);
                }
                break;

            case '(':   /* conditional expression: (?(cond)yes), (?(cond)yes|no) */
                if (syntax.op2QMarkLParenCondition()) {
                    int num = -1;
                    int name = -1;
                    fetch();
                    if (enc.isDigit(c)) { /* (n) */
                        unfetch();
                        num = fetchName('(', true);
                        if (syntax.strictCheckBackref()) {
                            if (num > env.numMem || env.memNodes == null || env.memNodes[num] == null) newValueException(INVALID_BACKREF);
                        }
                    } else {
                        if (Config.USE_NAMED_GROUP) {
                            if (c == '<' || c == '\'') {    /* (<name>), ('name') */
                                name = p;
                                fetchNamedBackrefToken();
                                inc();
                                num = token.getBackrefNum() > 1 ? token.getBackrefRefs()[0] : token.getBackrefRef1();
                            }
                        } else { // USE_NAMED_GROUP
                            newSyntaxException(INVALID_CONDITION_PATTERN);
                        }
                    }
                    EncloseNode en = new EncloseNode(EncloseType.CONDITION);
                    en.regNum = num;
                    if (name != -1) en.setNameRef();
                    node = en;
                } else {
                    newSyntaxException(UNDEFINED_GROUP_OPTION);
                }
                break;

            case '^': /* loads default options */
                if (left() && syntax.op2OptionPerl()) {
                    /* d-imsx */
                    option = bsOnOff(option, Option.ASCII_RANGE, true);
                    option = bsOnOff(option, Option.IGNORECASE, true);
                    option = bsOnOff(option, Option.SINGLELINE, false);
                    option = bsOnOff(option, Option.MULTILINE, true);
                    option = bsOnOff(option, Option.EXTEND, true);
                    fetch();
                } else {
                    newSyntaxException(UNDEFINED_GROUP_OPTION);
                }

            // case 'p': #ifdef USE_POSIXLINE_OPTION
            case '-':
            case 'i':
            case 'm':
            case 's':
            case 'x':
            case 'a':
            case 'd':
            case 'l':
            case 'u':
                boolean neg = false;
                while (true) {
                    switch(c) {
                    case ':':
                    case ')':
                        break;
                    case '-':
                        neg = true;
                        break;
                    case 'x':
                        option = bsOnOff(option, Option.EXTEND, neg);
                        break;
                    case 'i':
                        option = bsOnOff(option, Option.IGNORECASE, neg);
                        break;
                    case 's':
                        if (syntax.op2OptionPerl()) {
                            option = bsOnOff(option, Option.MULTILINE, neg);
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }
                        break;
                    case 'm':
                        if (syntax.op2OptionPerl()) {
                            option = bsOnOff(option, Option.SINGLELINE, !neg);
                        } else if (syntax.op2OptionRuby()) {
                            option = bsOnOff(option, Option.MULTILINE, neg);
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }
                        break;
                    // case 'p': #ifdef USE_POSIXLINE_OPTION // not defined
                    // option = bsOnOff(option, Option.MULTILINE|Option.SINGLELINE, neg);
                    // break;

                    case 'a':     /* limits \d, \s, \w and POSIX brackets to ASCII range */
                        if ((syntax.op2OptionPerl() || syntax.op2OptionRuby()) && !neg) {
                            option = bsOnOff(option, Option.ASCII_RANGE, false);
                            option = bsOnOff(option, Option.POSIX_BRACKET_ALL_RANGE, true);
                            option = bsOnOff(option, Option.WORD_BOUND_ALL_RANGE, true);
                            break;
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }
                    case 'u':
                        if ((syntax.op2OptionPerl() || syntax.op2OptionRuby()) && !neg) {
                            option = bsOnOff(option, Option.ASCII_RANGE, true);
                            option = bsOnOff(option, Option.POSIX_BRACKET_ALL_RANGE, true);
                            option = bsOnOff(option, Option.WORD_BOUND_ALL_RANGE, true);
                            break;
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }

                    case 'd':
                        if (syntax.op2OptionPerl() && !neg) {
                            option = bsOnOff(option, Option.ASCII_RANGE, true);
                        } else if (syntax.op2OptionRuby() && !neg) {
                            option = bsOnOff(option, Option.ASCII_RANGE, false);
                            option = bsOnOff(option, Option.POSIX_BRACKET_ALL_RANGE, false);
                            option = bsOnOff(option, Option.WORD_BOUND_ALL_RANGE, false);
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }
                        break;

                    case 'l':
                        if (syntax.op2OptionPerl() && !neg) {
                            option = bsOnOff(option, Option.ASCII_RANGE, true);
                        } else {
                            newSyntaxException(UNDEFINED_GROUP_OPTION);
                        }
                        break;
                    default:
                        newSyntaxException(UNDEFINED_GROUP_OPTION);
                    } // switch

                    if (c == ')') {
                        EncloseNode en = EncloseNode.newOption(option);
                        node = en;
                        returnCode = 2; /* option only */
                        return node;
                    } else if (c == ':') {
                        int prev = env.option;
                        env.option = option;
                        fetchToken();
                        Node target = parseSubExp(term);
                        env.option = prev;
                        EncloseNode en = EncloseNode.newOption(option);
                        en.setTarget(target);
                        node = en;
                        returnCode = 0;
                        return node;
                    }
                    if (!left()) newSyntaxException(END_PATTERN_IN_GROUP);
                    fetch();
                } // while

            default:
                newSyntaxException(UNDEFINED_GROUP_OPTION);
            } // switch

        } else {
            if (isDontCaptureGroup(env.option)) {
                fetchToken(); // goto group
                node = parseSubExp(term);
                returnCode = 1; /* group */
                return node;
            }
            EncloseNode en = EncloseNode.newMemory(env.option, false);
            int num = env.addMemEntry();
            en.regNum = num;
            node = en;
        }

        fetchToken();
        Node target = parseSubExp(term);

        if (node.getType() == NodeType.ANCHOR) {
            AnchorNode an = (AnchorNode)node;
            an.setTarget(target);
            if (syntax.op3OptionECMAScript() && an.type == AnchorType.PREC_READ_NOT) {
                env.popPrecReadNotNode(an);
            }
        } else {
            EncloseNode en = (EncloseNode)node;
            en.setTarget(target);
            if (en.type == EncloseType.MEMORY) {
                if (syntax.op3OptionECMAScript()) {
                    en.containingAnchor = env.currentPrecReadNotNode();
                }
                /* Don't move this to previous of parse_subexp() */
                env.setMemNode(en.regNum, en);
            } else if (en.type == EncloseType.CONDITION) {
                if (target.getType() != NodeType.ALT) { /* convert (?(cond)yes) to (?(cond)yes|empty) */
                    en.setTarget(ListNode.newAlt(target, ListNode.newAlt(StringNode.EMPTY, null)));
                }
            }
        }
        returnCode = 0;
        return node; // ??
    }

    private Node parseEncloseNamedGroup2(boolean listCapture) {
        int nm = p;
        int num = fetchName(c, false);
        int nameEnd = value;
        num = env.addMemEntry();
        if (listCapture && num >= BitStatus.BIT_STATUS_BITS_NUM) newValueException(GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY);

        regex.nameAdd(bytes, nm, nameEnd, num, syntax);
        EncloseNode en = EncloseNode.newMemory(env.option, true);
        en.regNum = num;

        Node node = en;

        if (listCapture) env.captureHistory = bsOnAtSimple(env.captureHistory, num);
        env.numNamed++;
        return node;
    }

    private int findStrPosition(int[]s, int n, int from, int to, Ptr nextChar) {
        int x;
        int q;
        int p = from;
        int i = 0;
        while (p < to) {
            x = enc.mbcToCode(bytes, p, to);
            q = p + enc.length(bytes, p, to);
            if (x == s[0]) {
                for (i=1; i<n && q<to; i++) {
                    x = enc.mbcToCode(bytes, q, to);
                    if (x != s[i]) break;
                    q += enc.length(bytes, q, to);
                }
                if (i >= n) {
                    if (bytes[nextChar.p] != 0) nextChar.p = q; // we may need zero term semantics...
                    return p;
                }
            }
            p = q;
        }
        return -1;
    }

    private Node parseExp(TokenType term) {
        if (token.type == term) return StringNode.EMPTY; // goto end_of_token
        Node node = null;
        boolean group = false;

        switch(token.type) {
        case ALT:
        case EOT:
            return StringNode.EMPTY; // end_of_token:, node_new_empty

        case SUBEXP_OPEN:
            node = parseEnclose(TokenType.SUBEXP_CLOSE);
            if (returnCode == 1) {
                group = true;
            } else if (returnCode == 2) { /* option only */
                int prev = env.option;
                EncloseNode en = (EncloseNode)node;
                env.option = en.option;
                fetchToken();
                Node target = parseSubExp(term);
                env.option = prev;
                en.setTarget(target);
                return node;
            }
            break;
        case SUBEXP_CLOSE:
            if (!syntax.allowUnmatchedCloseSubexp()) newSyntaxException(UNMATCHED_CLOSE_PARENTHESIS);
            if (token.escaped) {
                return parseExpTkRawByte(group); // goto tk_raw_byte
            } else {
                return parseExpTkByte(group); // goto tk_byte
            }
        case LINEBREAK:
            node = parseLineBreak();
            break;

        case EXTENDED_GRAPHEME_CLUSTER:
            node = parseExtendedGraphemeCluster();
            break;

        case KEEP:
            node = new AnchorNode(AnchorType.KEEP);
            break;

        case STRING:
            return parseExpTkByte(group); // tk_byte:

        case RAW_BYTE:
            return parseExpTkRawByte(group); // tk_raw_byte:

        case CODE_POINT:
            return parseStringLoop(StringNode.fromCodePoint(token.getCode(), enc), group);

        case QUOTE_OPEN:
            node = parseQuoteOpen();
            break;

        case CHAR_TYPE:
            node = parseCharType(node);
            break;

        case CHAR_PROPERTY:
            node = parseCharProperty();
            break;

        case CC_OPEN: {
            ObjPtr<CClassNode> ascPtr = new ObjPtr<CClassNode>();
            CClassNode cc = parseCharClass(ascPtr);
            int code = cc.isOneChar();
            if (code != -1) return parseStringLoop(StringNode.fromCodePoint(code, enc), group);

            node = cc;
            if (isIgnoreCase(env.option)) node = cClassCaseFold(node, cc, ascPtr.p);
            break;
            }

        case ANYCHAR:
            node = new AnyCharNode();
            break;

        case ANYCHAR_ANYTIME:
            node = parseAnycharAnytime();
            break;

        case BACKREF:
            node = parseBackref();
            break;

        case CALL:
            if (Config.USE_SUBEXP_CALL) node = parseCall();
            break;

        case ANCHOR:
            node = new AnchorNode(token.getAnchorSubtype(), token.getAnchorASCIIRange());
            break;

        case OP_REPEAT:
        case INTERVAL:
            if (syntax.contextIndepRepeatOps()) {
                if (syntax.contextInvalidRepeatOps()) {
                    newSyntaxException(TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED);
                } else {
                    node = StringNode.EMPTY; // node_new_empty
                }
            } else {
                return parseExpTkByte(group); // goto tk_byte
            }
            break;

        default:
            newInternalException(PARSER_BUG);
        } //switch

        //targetp = node;

        fetchToken(); // re_entry:

        return parseExpRepeat(node, group); // repeat:
    }

    private Node parseLineBreak() {
        byte[]buflb = new byte[Config.ENC_CODE_TO_MBC_MAXLEN * 2];
        int len1 = enc.codeToMbc(0x0D, buflb, 0);
        int len2 = enc.codeToMbc(0x0A, buflb, len1);
        StringNode left = new StringNode(buflb, 0, len1 + len2);
        left.setRaw();
        /* [\x0A-\x0D] or [\x0A-\x0D\x{85}\x{2028}\x{2029}] */
        CClassNode right = new CClassNode();
        if (enc.minLength() > 1) {
            right.addCodeRange(env, 0x0A, 0x0D);
        } else {
            right.bs.setRange(env, 0x0A, 0x0D);
        }

        if (enc.isUnicode()) {
            /* UTF-8, UTF-16BE/LE, UTF-32BE/LE */
            right.addCodeRange(env, 0x85, 0x85);
            right.addCodeRange(env, 0x2028, 0x2029);
        }
        /* (?>...) */
        EncloseNode en = new EncloseNode(EncloseType.STOP_BACKTRACK);
        en.setTarget(ListNode.newAlt(left, ListNode.newAlt(right, null)));
        return en;
    }

    private static class GraphemeNames {
        static final byte[] Grapheme_Cluster_Break_Extend = "Grapheme_Cluster_Break=Extend".getBytes();
        static final byte[] Grapheme_Cluster_Break_Control = "Grapheme_Cluster_Break=Control".getBytes();
        static final byte[] Grapheme_Cluster_Break_Prepend = "Grapheme_Cluster_Break=Prepend".getBytes();
        static final byte[] Grapheme_Cluster_Break_L = "Grapheme_Cluster_Break=L".getBytes();
        static final byte[] Grapheme_Cluster_Break_V = "Grapheme_Cluster_Break=V".getBytes();
        static final byte[] Grapheme_Cluster_Break_LV = "Grapheme_Cluster_Break=LV".getBytes();
        static final byte[] Grapheme_Cluster_Break_LVT = "Grapheme_Cluster_Break=LVT".getBytes();
        static final byte[] Grapheme_Cluster_Break_T = "Grapheme_Cluster_Break=T".getBytes();
        static final byte[] Regional_Indicator = "Regional_Indicator".getBytes();
        static final byte[] Extended_Pictographic = "Extended_Pictographic".getBytes();
        static final byte[] Grapheme_Cluster_Break_SpacingMark = "Grapheme_Cluster_Break=SpacingMark".getBytes();
    }

    private void addPropertyToCC(CClassNode cc, byte[] propName, boolean not) {
        int ctype = enc.propertyNameToCType(propName, 0, propName.length);
        cc.addCType(ctype, not, false, env, this);
    }

    private void createPropertyNode(Node[]nodes, int np, byte[] propName) {
        CClassNode cc = new CClassNode();
        addPropertyToCC(cc, propName, false);
        nodes[np] = cc;
    }

    private void quantifierNode(Node[]nodes, int np, int lower, int upper) {
        QuantifierNode qnf = new QuantifierNode(lower, upper, false);
        qnf.setTarget(nodes[np]);
        nodes[np] = qnf;
    }

    private void quantifierPropertyNode(Node[]nodes, int np, byte[] propName, char repetitions) {
        int lower = 0;
        int upper = QuantifierNode.REPEAT_INFINITE;

        createPropertyNode(nodes, np, propName);
        switch (repetitions) {
            case '?':  upper = 1;          break;
            case '+':  lower = 1;          break;
            case '*':                      break;
            case '2':  lower = upper = 2;  break;
            default :  new InternalException(ErrorMessages.PARSER_BUG);
        }

        quantifierNode(nodes, np, lower, upper);
    }

    private void createNodeFromArray(boolean list, Node[] nodes, int np, int nodeArray) {
        int i = 0;
        ListNode tmp = null;
        while (nodes[nodeArray + i] != null) i++;
        while (--i >= 0) {
            nodes[np] = list ? ListNode.newList(nodes[nodeArray + i], tmp) : ListNode.newAlt(nodes[nodeArray + i], tmp);
            nodes[nodeArray + i] = null;
            tmp = (ListNode)nodes[np];
        }
    }

    private ListNode createNodeFromArray(Node[]nodes, int nodeArray) {
        int i = 0;
        ListNode np = null, tmp = null;
        while (nodes[nodeArray + i] != null) i++;
        while (--i >= 0) {
            np = ListNode.newAlt(nodes[nodeArray + i], tmp);
            nodes[nodeArray + i] = null;
            tmp = np;
        }
        return np;
    }

    private static int NODE_COMMON_SIZE = 16;
    private Node parseExtendedGraphemeCluster() {
        final Node[] nodes = new Node[NODE_COMMON_SIZE];
        final int anyTargetPosition;
        int alts = 0;

        StringNode strNode = new StringNode(Config.ENC_CODE_TO_MBC_MAXLEN * 2);
        strNode.setRaw();
        strNode.catCode(0x0D, enc);
        strNode.catCode(0x0A, enc);
        nodes[alts] = strNode;

        if (Config.USE_UNICODE_PROPERTIES && enc.isUnicode()) {
            CClassNode cc;
            cc = new CClassNode();
            nodes[alts + 1] = cc;
            addPropertyToCC(cc, GraphemeNames.Grapheme_Cluster_Break_Control, false);
            if (enc.minLength() > 1) {
                cc.addCodeRange(env, 0x000A, 0x000A);
                cc.addCodeRange(env, 0x000D, 0x000D);
            } else {
                cc.bs.set(0x0A);
                cc.bs.set(0x0D);
            }

            {
                int list = alts + 3;
                quantifierPropertyNode(nodes, list + 0, GraphemeNames.Grapheme_Cluster_Break_Prepend, '*');
                {
                    int coreAlts = list + 2;
                    {
                        int HList = coreAlts + 1;
                        quantifierPropertyNode(nodes, HList + 0, GraphemeNames.Grapheme_Cluster_Break_L, '*');
                        {
                            int HAlt2 = HList + 2;
                            quantifierPropertyNode(nodes, HAlt2 + 0, GraphemeNames.Grapheme_Cluster_Break_V, '+');
                            {
                                int HList2 = HAlt2 + 2;
                                createPropertyNode(nodes, HList2 + 0, GraphemeNames.Grapheme_Cluster_Break_LV);
                                quantifierPropertyNode(nodes, HList2 + 1, GraphemeNames.Grapheme_Cluster_Break_V, '*');
                                createNodeFromArray(true, nodes, HAlt2 + 1, HList2);
                            }
                            createPropertyNode(nodes, HAlt2 + 2, GraphemeNames.Grapheme_Cluster_Break_LVT);
                            createNodeFromArray(false, nodes, HList + 1, HAlt2);
                        }
                        quantifierPropertyNode(nodes, HList + 2, GraphemeNames.Grapheme_Cluster_Break_T, '*');
                        createNodeFromArray(true, nodes, coreAlts + 0, HList);
                    }
                    quantifierPropertyNode(nodes, coreAlts + 1, GraphemeNames.Grapheme_Cluster_Break_L, '+');
                    quantifierPropertyNode(nodes, coreAlts + 2, GraphemeNames.Grapheme_Cluster_Break_T, '+');
                    quantifierPropertyNode(nodes, coreAlts + 3, GraphemeNames.Regional_Indicator, '2');
                    {
                        int XPList = coreAlts + 5;
                        createPropertyNode(nodes, XPList + 0, GraphemeNames.Extended_Pictographic);
                        {
                            int ExList = XPList + 2;
                            quantifierPropertyNode(nodes, ExList + 0, GraphemeNames.Grapheme_Cluster_Break_Extend, '*');
                            strNode = new StringNode(Config.ENC_CODE_TO_MBC_MAXLEN);
                            strNode.setRaw();
                            strNode.catCode(0x200D, enc);
                            nodes[ExList + 1] = strNode;
                            createPropertyNode(nodes, ExList + 2, GraphemeNames.Extended_Pictographic);
                            createNodeFromArray(true, nodes, XPList + 1, ExList);
                        }
                        quantifierNode(nodes, XPList + 1, 0, QuantifierNode.REPEAT_INFINITE);
                        createNodeFromArray(true, nodes, coreAlts + 4, XPList);
                    }
                    cc = new CClassNode();
                    nodes[coreAlts + 5] = cc;
                    if (enc.minLength() > 1) {
                        addPropertyToCC(cc, GraphemeNames.Grapheme_Cluster_Break_Control, false);
                        cc.addCodeRange(env, 0x000A, 0x000A);
                        cc.addCodeRange(env, 0x000D, 0x000D);
                        cc.mbuf = CodeRangeBuffer.notCodeRangeBuff(env, cc.mbuf);
                    } else {
                        addPropertyToCC(cc, GraphemeNames.Grapheme_Cluster_Break_Control, true);
                        cc.bs.clear(0x0A);
                        cc.bs.clear(0x0D);
                    }
                    createNodeFromArray(false, nodes, list + 1, coreAlts);
                }
                createPropertyNode(nodes, list + 2, GraphemeNames.Grapheme_Cluster_Break_Extend);
                cc = (CClassNode)nodes[list + 2];
                addPropertyToCC(cc, GraphemeNames.Grapheme_Cluster_Break_SpacingMark, false);
                cc.addCodeRange(env, 0x200D, 0x200D);
                quantifierNode(nodes, list + 2, 0, QuantifierNode.REPEAT_INFINITE);
                createNodeFromArray(true, nodes, alts + 2, list);

            }
            anyTargetPosition = 3;
        } else { // enc.isUnicode()
            anyTargetPosition = 1;
        }

        Node any = new AnyCharNode();
        EncloseNode option = EncloseNode.newOption(bsOnOff(env.option, Option.MULTILINE, false));
        option.setTarget(any);
        nodes[anyTargetPosition] = option;

        Node topAlt = createNodeFromArray(nodes, alts);
        EncloseNode enclose = new EncloseNode(EncloseType.STOP_BACKTRACK);
        enclose.setTarget(topAlt);

        if (Config.USE_UNICODE_PROPERTIES && enc.isUnicode()) {
            option = EncloseNode.newOption(bsOnOff(env.option, Option.IGNORECASE, true));
            option.setTarget(enclose);
            return option;
        } else {
            return enclose;
        }
    }

    private Node parseExpTkByte(boolean group) {
        StringNode node = new StringNode(bytes, token.backP, p); // tk_byte:
        return parseStringLoop(node, group);
    }

    private Node parseStringLoop(StringNode node, boolean group) {
        while (true) {
            fetchToken();
            if (token.type == TokenType.STRING) {
                if (token.backP == node.end) {
                    node.end = p; // non escaped character, remain shared, just increase shared range
                } else {
                    node.catBytes(bytes, token.backP, p); // non continuous string stream, need to COW
                }
            } else if (token.type == TokenType.CODE_POINT) {
                node.catCode(token.getCode(), enc);
            } else {
                break;
            }
        }
        // targetp = node;
        return parseExpRepeat(node, group); // string_end:, goto repeat
    }

    private Node parseExpTkRawByte(boolean group) {
        // tk_raw_byte:
        StringNode node = new StringNode();
        node.setRaw();
        node.catByte((byte)token.getC());

        int len = 1;
        while (true) {
            if (len >= enc.minLength()) {
                if (len == enc.length(node.bytes, node.p, node.end)) {
                    fetchToken();
                    node.clearRaw();
                    // !goto string_end;!
                    return parseExpRepeat(node, group);
                }
            }

            fetchToken();
            if (token.type != TokenType.RAW_BYTE) {
                /* Don't use this, it is wrong for little endian encodings. */
                // USE_PAD_TO_SHORT_BYTE_CHAR ...
                newValueException(TOO_SHORT_MULTI_BYTE_STRING);
            }
            node.catByte((byte)token.getC());
            len++;
        } // while
    }

    private Node parseExpRepeat(Node target, boolean group) {
        while (token.type == TokenType.OP_REPEAT || token.type == TokenType.INTERVAL) { // repeat:
            if (isInvalidQuantifier(target)) newSyntaxException(TARGET_OF_REPEAT_OPERATOR_INVALID);

            if (!group && syntax.op3OptionECMAScript() && target.getType() == NodeType.QTFR) {
                newSyntaxException(NESTED_REPEAT_NOT_ALLOWED);
            }
            QuantifierNode qtfr = new QuantifierNode(token.getRepeatLower(),
                                                     token.getRepeatUpper(),
                                                     token.type == TokenType.INTERVAL);

            qtfr.greedy = token.getRepeatGreedy();
            int ret = qtfr.setQuantifier(target, group, env, bytes, getBegin(), getEnd());
            Node qn = qtfr;

            if (token.getRepeatPossessive()) {
                EncloseNode en = new EncloseNode(EncloseType.STOP_BACKTRACK); // node_new_enclose
                en.setTarget(qn);
                qn = en;
            }

            if (ret == 0 || (syntax.op3OptionECMAScript() && ret == 1)) {
                target = qn;
            } else if (ret == 2) { /* split case: /abc+/ */
                target = ListNode.newList(target, null);
                ListNode tmp = ListNode.newList(qn, null);
                ((ListNode)target).setTail(tmp);

                fetchToken();
                return parseExpRepeatForCar(target, tmp, group);
            }
            fetchToken(); // goto re_entry
        }
        return target;
    }

    private Node parseExpRepeatForCar(Node top, ListNode target, boolean group) {
        while (token.type == TokenType.OP_REPEAT || token.type == TokenType.INTERVAL) { // repeat:
            if (isInvalidQuantifier(target.value)) newSyntaxException(TARGET_OF_REPEAT_OPERATOR_INVALID);

            QuantifierNode qtfr = new QuantifierNode(token.getRepeatLower(),
                                                     token.getRepeatUpper(),
                                                     token.type == TokenType.INTERVAL);

            qtfr.greedy = token.getRepeatGreedy();
            int ret = qtfr.setQuantifier(target.value, group, env, bytes, getBegin(), getEnd());
            Node qn = qtfr;

            if (token.getRepeatPossessive()) {
                EncloseNode en = new EncloseNode(EncloseType.STOP_BACKTRACK); // node_new_enclose
                en.setTarget(qn);
                qn = en;
            }

            if (ret == 0) {
                target.setValue(qn);
            } else if (ret == 2) { /* split case: /abc+/ */
                assert false;
            }
            fetchToken(); // goto re_entry
        }
        return top;
    }

    private boolean isInvalidQuantifier(Node node) {
        if (Config.USE_NO_INVALID_QUANTIFIER) return false;

        ListNode consAlt;
        switch(node.getType()) {
        case NodeType.ANCHOR:
            return true;

        case NodeType.ENCLOSE:
            /* allow enclosed elements */
            /* return is_invalid_quantifier_target(NENCLOSE(node)->target); */
            break;

        case NodeType.LIST:
            consAlt = (ListNode)node;
            do {
                if (!isInvalidQuantifier(consAlt.value)) return false;
            } while ((consAlt = consAlt.tail) != null);
            return false;

        case NodeType.ALT:
            consAlt = (ListNode)node;
            do {
                if (isInvalidQuantifier(consAlt.value)) return true;
            } while ((consAlt = consAlt.tail) != null);
            break;

        default:
            break;
        }
        return false;
    }

    private Node parseQuoteOpen() {
        int[]endOp = new int[]{syntax.metaCharTable.esc, 'E'};
        int qstart = p;
        Ptr nextChar = new Ptr();
        int qend = findStrPosition(endOp, endOp.length, qstart, stop, nextChar);
        if (qend == -1) nextChar.p = qend = stop;
        Node node = new StringNode(bytes, qstart, qend);
        p = nextChar.p;
        return node;
    }

    private Node parseCharType(Node node) {
        switch(token.getPropCType()) {
        case CharacterType.WORD:
            node = new CTypeNode(token.getPropCType(), token.getPropNot(), isAsciiRange(env.option));
            break;

        case CharacterType.SPACE:
        case CharacterType.DIGIT:
        case CharacterType.XDIGIT:
            CClassNode ccn = new CClassNode();
            ccn.addCType(token.getPropCType(), false, isAsciiRange(env.option), env, this);
            if (token.getPropNot()) ccn.setNot();
            node = ccn;
            break;

        default:
            newInternalException(PARSER_BUG);
        } // inner switch
        return node;
    }

    private Node cClassCaseFold(Node node, CClassNode cc, CClassNode ascCc) {
        ApplyCaseFoldArg arg = new ApplyCaseFoldArg(env, cc, ascCc);
        enc.applyAllCaseFold(env.caseFoldFlag, ApplyCaseFold.INSTANCE, arg);
        if (arg.altRoot != null) {
            node = ListNode.newAlt(node, arg.altRoot);
        }
        return node;
    }

    private Node parseCharProperty() {
        int ctype = fetchCharPropertyToCType();
        CClassNode cc = new CClassNode();
        Node node = cc;
        cc.addCType(ctype, false, false, env, this);
        if (token.getPropNot()) cc.setNot();

        if (isIgnoreCase(env.option)) {
            if (ctype != CharacterType.ASCII) {
                node = cClassCaseFold(node, cc, cc);
            }
        }
        return node;
    }

    private Node parseAnycharAnytime() {
        Node node = new AnyCharNode();
        QuantifierNode qn = new QuantifierNode(0, QuantifierNode.REPEAT_INFINITE, false);
        qn.setTarget(node);
        return qn;
    }

    private Node parseBackref() {
        final Node node;
        if (syntax.op3OptionECMAScript() && token.getBackrefNum() == 1 && env.memNodes != null) {
            EncloseNode encloseNode = env.memNodes[token.getBackrefRef1()];
            boolean shouldIgnore = false;
            if (encloseNode != null && encloseNode.containingAnchor != null) {
                shouldIgnore = true;
                for (Node anchorNode : env.precReadNotNodes) {
                    if (anchorNode == encloseNode.containingAnchor) {
                        shouldIgnore = false;
                        break;
                    }
                }
            }
            if (shouldIgnore) {
                node = StringNode.EMPTY;
            } else {
                node = newBackRef(new int[]{token.getBackrefRef1()});
            }
        } else {
            node = newBackRef(token.getBackrefNum() > 1 ? token.getBackrefRefs() : new int[]{token.getBackrefRef1()});
        }
        return node;
    }

    private BackRefNode newBackRef(int[]backRefs) {
        return new BackRefNode(token.getBackrefNum(),
            backRefs,
            token.getBackrefByName(),
            token.getBackrefExistLevel(),
            token.getBackrefLevel(),
            env);
    }

    private Node parseCall() {
        int gNum = token.getCallGNum();
        if (gNum < 0 || token.getCallRel()) {
            if (gNum > 0) gNum--;
            gNum = backrefRelToAbs(gNum);
            if (gNum <= 0) newValueException(INVALID_BACKREF);
        }
        Node node = new CallNode(bytes, token.getCallNameP(), token.getCallNameEnd(), gNum);
        env.numCall++;
        return node;
    }

    private Node parseBranch(TokenType term) {
        Node node = parseExp(term);

        if (token.type == TokenType.EOT || token.type == term || token.type == TokenType.ALT) {
            return node;
        } else {
            ListNode top = ListNode.newList(node, null);
            ListNode t = top;

            while (token.type != TokenType.EOT && token.type != term && token.type != TokenType.ALT) {
                node = parseExp(term);
                if (node.getType() == NodeType.LIST) {
                    t.setTail((ListNode)node);
                    while (((ListNode)node).tail != null ) node = ((ListNode)node).tail;

                    t = ((ListNode)node);
                } else {
                    t.setTail(ListNode.newList(node, null));
                    t = t.tail;
                }
            }
            return top;
        }
    }

    /* term_tok: TK_EOT or TK_SUBEXP_CLOSE */
    private Node parseSubExp(TokenType term) {
        Node node = parseBranch(term);

        if (token.type == term) {
            return node;
        } else if (token.type == TokenType.ALT) {
            ListNode top = ListNode.newAlt(node, null);
            ListNode t = top;
            while (token.type == TokenType.ALT) {
                fetchToken();
                node = parseBranch(term);

                t.setTail(ListNode.newAlt(node, null));
                t = t.tail;
            }

            if (token.type != term) parseSubExpError(term);
            return top;
        } else {
            parseSubExpError(term);
            return null; //not reached
        }
    }

    private void parseSubExpError(TokenType term) {
        if (term == TokenType.SUBEXP_CLOSE) {
            newSyntaxException(END_PATTERN_WITH_UNMATCHED_PARENTHESIS);
        } else {
            newInternalException(PARSER_BUG);
        }
    }

    protected final Node parseRegexp() {
        fetchToken();
        Node top = parseSubExp(TokenType.EOT);
        if (Config.USE_SUBEXP_CALL) {
            if (env.numCall > 0) {
                /* Capture the pattern itself. It is used for (?R), (?0) and \g<0>. */
                EncloseNode np = EncloseNode.newMemory(env.option, false);
                np.regNum = 0;
                np.setTarget(top);
                if (env.memNodes ==  null) env.memNodes = new EncloseNode[Config.SCANENV_MEMNODES_SIZE];
                env.memNodes[0] = np;
                top = np;
            }
        }
        return top;
    }
}
