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

import static org.joni.constants.MetaChar.INEFFECTIVE_META_CHAR;

import org.joni.constants.SyntaxProperties;

public final class Syntax implements SyntaxProperties {
    public final String name;
    private final int op;
    private final int op2;
    private final int op3;
    private final int behavior;
    public final int options;
    public final MetaCharTable metaCharTable;

    public Syntax(String name, int op, int op2, int op3, int behavior, int options, MetaCharTable metaCharTable) {
        this.name = name;
        this.op = op;
        this.op2 = op2;
        this.op3 = op3;
        this.behavior = behavior;
        this.options = options;
        this.metaCharTable = metaCharTable;
    }

    public static class MetaCharTable {
        public final int esc;
        public final int anyChar;
        public final int anyTime;
        public final int zeroOrOneTime;
        public final int oneOrMoreTime;
        public final int anyCharAnyTime;

        public MetaCharTable(int esc, int anyChar, int anyTime,
                             int zeroOrOneTime, int oneOrMoreTime, int anyCharAnyTime) {
            this.esc = esc;
            this.anyChar = anyChar;
            this.anyTime = anyTime;
            this.zeroOrOneTime = zeroOrOneTime;
            this.oneOrMoreTime = oneOrMoreTime;
            this.anyCharAnyTime = anyCharAnyTime;
        }
    }

    /**
     * OP
     *
     */
    protected boolean isOp(int opm) {
        return (op & opm) != 0;
    }

    public boolean opVariableMetaCharacters() {
        return isOp(OP_VARIABLE_META_CHARACTERS);
    }

    public boolean opDotAnyChar() {
        return isOp(OP_DOT_ANYCHAR);
    }

    public boolean opAsteriskZeroInf() {
        return isOp(OP_ASTERISK_ZERO_INF);
    }

    public boolean opEscAsteriskZeroInf() {
        return isOp(OP_ESC_ASTERISK_ZERO_INF);
    }

    public boolean opPlusOneInf() {
        return isOp(OP_PLUS_ONE_INF);
    }

    public boolean opEscPlusOneInf() {
        return isOp(OP_ESC_PLUS_ONE_INF);
    }

    public boolean opQMarkZeroOne() {
        return isOp(OP_QMARK_ZERO_ONE);
    }

    public boolean opEscQMarkZeroOne() {
        return isOp(OP_ESC_QMARK_ZERO_ONE);
    }

    public boolean opBraceInterval() {
        return isOp(OP_BRACE_INTERVAL);
    }

    public boolean opEscBraceInterval() {
        return isOp(OP_ESC_BRACE_INTERVAL);
    }

    public boolean opVBarAlt() {
        return isOp(OP_VBAR_ALT);
    }

    public boolean opEscVBarAlt() {
        return isOp(OP_ESC_VBAR_ALT);
    }

    public boolean opLParenSubexp() {
        return isOp(OP_LPAREN_SUBEXP);
    }

    public boolean opEscLParenSubexp() {
        return isOp(OP_ESC_LPAREN_SUBEXP);
    }

    public boolean opEscAZBufAnchor() {
        return isOp(OP_ESC_AZ_BUF_ANCHOR);
    }

    public boolean opEscCapitalGBeginAnchor() {
        return isOp(OP_ESC_CAPITAL_G_BEGIN_ANCHOR);
    }

    public boolean opDecimalBackref() {
        return isOp(OP_DECIMAL_BACKREF);
    }

    public boolean opBracketCC() {
        return isOp(OP_BRACKET_CC);
    }

    public boolean opEscWWord() {
        return isOp(OP_ESC_W_WORD);
    }

    public boolean opEscLtGtWordBeginEnd() {
        return isOp(OP_ESC_LTGT_WORD_BEGIN_END);
    }

    public boolean opEscBWordBound() {
        return isOp(OP_ESC_B_WORD_BOUND);
    }

    public boolean opEscSWhiteSpace() {
        return isOp(OP_ESC_S_WHITE_SPACE);
    }

    public boolean opEscDDigit() {
        return isOp(OP_ESC_D_DIGIT);
    }

    public boolean opLineAnchor() {
        return isOp(OP_LINE_ANCHOR);
    }

    public boolean opPosixBracket() {
        return isOp(OP_POSIX_BRACKET);
    }

    public boolean opQMarkNonGreedy() {
        return isOp(OP_QMARK_NON_GREEDY);
    }

    public boolean opEscControlChars() {
        return isOp(OP_ESC_CONTROL_CHARS);
    }

    public boolean opEscCControl() {
        return isOp(OP_ESC_C_CONTROL);
    }

    public boolean opEscOctal3() {
        return isOp(OP_ESC_OCTAL3);
    }

    public boolean opEscXHex2() {
        return isOp(OP_ESC_X_HEX2);
    }

    public boolean opEscXBraceHex8() {
        return isOp(OP_ESC_X_BRACE_HEX8);
    }

    public boolean opEscOBraceOctal() {
        return isOp(OP_ESC_O_BRACE_OCTAL);
    }

    /**
     * OP
     *
     */
    protected boolean isOp2(int opm) {
        return (op2 & opm) != 0;
    }

    public boolean op2EscCapitalQQuote() {
        return isOp2(OP2_ESC_CAPITAL_Q_QUOTE);
    }

    public boolean op2QMarkGroupEffect() {
        return isOp2(OP2_QMARK_GROUP_EFFECT);
    }

    public boolean op2OptionPerl() {
        return isOp2(OP2_OPTION_PERL);
    }

    public boolean op2OptionRuby() {
        return isOp2(OP2_OPTION_RUBY);
    }

    public boolean op2PlusPossessiveRepeat() {
        return isOp2(OP2_PLUS_POSSESSIVE_REPEAT);
    }

    public boolean op2PlusPossessiveInterval() {
        return isOp2(OP2_PLUS_POSSESSIVE_INTERVAL);
    }

    public boolean op2CClassSetOp() {
        return isOp2(OP2_CCLASS_SET_OP);
    }

    public boolean op2QMarkLtNamedGroup() {
        return isOp2(OP2_QMARK_LT_NAMED_GROUP);
    }

    public boolean op2EscKNamedBackref() {
        return isOp2(OP2_ESC_K_NAMED_BACKREF);
    }

    public boolean op2EscGSubexpCall() {
        return isOp2(OP2_ESC_G_SUBEXP_CALL);
    }

    public boolean op2AtMarkCaptureHistory() {
        return isOp2(OP2_ATMARK_CAPTURE_HISTORY);
    }

    public boolean op2EscCapitalCBarControl() {
        return isOp2(OP2_ESC_CAPITAL_C_BAR_CONTROL);
    }

    public boolean op2EscCapitalMBarMeta() {
        return isOp2(OP2_ESC_CAPITAL_M_BAR_META);
    }

    public boolean op2EscVVtab() {
        return isOp2(OP2_ESC_V_VTAB);
    }

    public boolean op2EscUHex4() {
        return isOp2(OP2_ESC_U_HEX4);
    }

    public boolean op2EscGnuBufAnchor() {
        return isOp2(OP2_ESC_GNU_BUF_ANCHOR);
    }

    public boolean op2EscPBraceCharProperty() {
        return isOp2(OP2_ESC_P_BRACE_CHAR_PROPERTY);
    }

    public boolean op2EscPBraceCircumflexNot() {
        return isOp2(OP2_ESC_P_BRACE_CIRCUMFLEX_NOT);
    }

    public boolean op2EscHXDigit() {
        return isOp2(OP2_ESC_H_XDIGIT);
    }

    public boolean op2IneffectiveEscape() {
        return isOp2(OP2_INEFFECTIVE_ESCAPE);
    }

    public boolean op2EscCapitalRLinebreak() {
        return isOp2(OP2_ESC_CAPITAL_R_LINEBREAK);
    }

    public boolean op2EscCapitalXExtendedGraphemeCluster() {
        return isOp2(OP2_ESC_CAPITAL_X_EXTENDED_GRAPHEME_CLUSTER);
    }

    public boolean op2EscVVerticalWhiteSpace() {
        return isOp2(OP2_ESC_V_VERTICAL_WHITESPACE);
    }

    public boolean op2EscHHorizontalWhiteSpace() {
        return isOp2(OP2_ESC_H_HORIZONTAL_WHITESPACE);
    }

    public boolean op2EscCapitalKKeep() {
        return isOp2(OP2_ESC_CAPITAL_K_KEEP);
    }

    public boolean op2QMarkTildeAbsent() {
        return isOp2(OP2_QMARK_TILDE_ABSENT);
    }

    public boolean op2EscGBraceBackref() {
        return isOp2(OP2_ESC_G_BRACE_BACKREF);
    }

    public boolean op2QMarkSubexpCall() {
        return isOp2(OP2_QMARK_SUBEXP_CALL);
    }

    public boolean op2QMarkBarBranchReset() {
        return isOp2(OP2_QMARK_BAR_BRANCH_RESET);
    }

    public boolean op2QMarkLParenCondition() {
        return isOp2(OP2_QMARK_LPAREN_CONDITION);
    }

    public boolean op2QMarkCapitalPNamedGroup() {
        return isOp2(OP2_QMARK_CAPITAL_P_NAMED_GROUP);
    }

    protected boolean isOp3(int opm) {
        return (op3 & opm) != 0;
    }

    public boolean op3OptionJava() {
        return isOp3(OP3_OPTION_JAVA);
    }

    public boolean op3OptionECMAScript() {
        return isOp3(OP3_OPTION_ECMASCRIPT);
    }


    /**
     * BEHAVIOR
     *
     */
    protected boolean isBehavior(int bvm) {
        return (behavior & bvm) != 0;
    }

    public boolean contextIndepRepeatOps() {
        return isBehavior(CONTEXT_INDEP_REPEAT_OPS);
    }

    public boolean contextInvalidRepeatOps() {
        return isBehavior(CONTEXT_INVALID_REPEAT_OPS);
    }

    public boolean allowUnmatchedCloseSubexp() {
        return isBehavior(ALLOW_UNMATCHED_CLOSE_SUBEXP);
    }

    public boolean allowInvalidInterval() {
        return isBehavior(ALLOW_INVALID_INTERVAL);
    }

    public boolean allowIntervalLowAbbrev() {
        return isBehavior(ALLOW_INTERVAL_LOW_ABBREV);
    }

    public boolean strictCheckBackref() {
        return isBehavior(STRICT_CHECK_BACKREF);
    }

    public boolean differentLengthAltLookBehind() {
        return isBehavior(DIFFERENT_LEN_ALT_LOOK_BEHIND);
    }

    public boolean captureOnlyNamedGroup() {
        return isBehavior(CAPTURE_ONLY_NAMED_GROUP);
    }

    public boolean allowMultiplexDefinitionName() {
        return isBehavior(ALLOW_MULTIPLEX_DEFINITION_NAME);
    }

    public boolean fixedIntervalIsGreedyOnly() {
        return isBehavior(FIXED_INTERVAL_IS_GREEDY_ONLY);
    }


    public boolean notNewlineInNegativeCC() {
        return isBehavior(NOT_NEWLINE_IN_NEGATIVE_CC);
    }

    public boolean backSlashEscapeInCC() {
        return isBehavior(BACKSLASH_ESCAPE_IN_CC);
    }

    public boolean allowEmptyRangeInCC() {
        return isBehavior(ALLOW_EMPTY_RANGE_IN_CC);
    }

    public boolean allowDoubleRangeOpInCC() {
        return isBehavior(ALLOW_DOUBLE_RANGE_OP_IN_CC);
    }

    public boolean warnCCOpNotEscaped() {
        return isBehavior(WARN_CC_OP_NOT_ESCAPED);
    }

    public boolean warnCCDup() {
        return isBehavior(WARN_CC_DUP);
    }

    public boolean warnReduntantNestedRepeat() {
        return isBehavior(WARN_REDUNDANT_NESTED_REPEAT);
    }

    public static final Syntax RUBY = new Syntax(
        "RUBY",
        (( GNU_REGEX_OP | OP_QMARK_NON_GREEDY |
        OP_ESC_OCTAL3 | OP_ESC_X_HEX2 |
        OP_ESC_X_BRACE_HEX8 | OP_ESC_CONTROL_CHARS |
        OP_ESC_C_CONTROL )
        & ~OP_ESC_LTGT_WORD_BEGIN_END ),

        ( OP2_QMARK_GROUP_EFFECT |
        OP2_OPTION_RUBY |
        OP2_QMARK_LT_NAMED_GROUP | OP2_ESC_K_NAMED_BACKREF |
        OP2_ESC_G_SUBEXP_CALL |
        OP2_ESC_P_BRACE_CHAR_PROPERTY  |
        OP2_ESC_P_BRACE_CIRCUMFLEX_NOT |
        OP2_PLUS_POSSESSIVE_REPEAT |
        OP2_CCLASS_SET_OP | OP2_ESC_CAPITAL_C_BAR_CONTROL |
        OP2_ESC_CAPITAL_M_BAR_META | OP2_ESC_V_VTAB |
        OP2_ESC_H_XDIGIT |
        OP2_ESC_CAPITAL_X_EXTENDED_GRAPHEME_CLUSTER |
        OP2_QMARK_LPAREN_CONDITION |
        OP2_ESC_CAPITAL_R_LINEBREAK |
        OP2_ESC_CAPITAL_K_KEEP |
        OP2_QMARK_TILDE_ABSENT
        ),

        0,

        ( GNU_REGEX_BV |
        ALLOW_INTERVAL_LOW_ABBREV |
        DIFFERENT_LEN_ALT_LOOK_BEHIND |
        CAPTURE_ONLY_NAMED_GROUP |
        ALLOW_MULTIPLEX_DEFINITION_NAME |
        FIXED_INTERVAL_IS_GREEDY_ONLY |
        WARN_CC_OP_NOT_ESCAPED |
        WARN_CC_DUP |
        WARN_REDUNDANT_NESTED_REPEAT ),

        (Option.ASCII_RANGE | Option.POSIX_BRACKET_ALL_RANGE | Option.WORD_BOUND_ALL_RANGE),

        new MetaCharTable(
            '\\',                           /* esc */
            INEFFECTIVE_META_CHAR,          /* anychar '.' */
            INEFFECTIVE_META_CHAR,          /* anytime '*' */
            INEFFECTIVE_META_CHAR,          /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,          /* one or more time '+' */
            INEFFECTIVE_META_CHAR           /* anychar anytime */
        )
    );

    public static final Syntax DEFAULT = RUBY;

    public static final Syntax TEST = new Syntax("TEST", RUBY.op, RUBY.op2 | OP2_ESC_U_HEX4, RUBY.op3, RUBY.behavior, RUBY.options & ~ Option.ASCII_RANGE, RUBY.metaCharTable);

    public static final Syntax ASIS = new Syntax(
        "ASIS",
        0,

        OP2_INEFFECTIVE_ESCAPE,

        0,

        0,

        Option.NONE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax PosixBasic = new Syntax(
        "PosixBasic",
        (POSIX_COMMON_OP | OP_ESC_LPAREN_SUBEXP |
        OP_ESC_BRACE_INTERVAL ),

        0,

        0,

        0,

        ( Option.SINGLELINE | Option.MULTILINE ),

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax PosixExtended = new Syntax(
        "PosixExtended",
        ( POSIX_COMMON_OP | OP_LPAREN_SUBEXP |
        OP_BRACE_INTERVAL |
        OP_PLUS_ONE_INF | OP_QMARK_ZERO_ONE |OP_VBAR_ALT ),

        0,

        0,

        ( CONTEXT_INDEP_ANCHORS |
        CONTEXT_INDEP_REPEAT_OPS | CONTEXT_INVALID_REPEAT_OPS |
        ALLOW_UNMATCHED_CLOSE_SUBEXP |
        ALLOW_DOUBLE_RANGE_OP_IN_CC ),

        ( Option.SINGLELINE | Option.MULTILINE ),

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax Emacs = new Syntax(
        "Emacs",
        ( OP_DOT_ANYCHAR | OP_BRACKET_CC |
        OP_ESC_BRACE_INTERVAL |
        OP_ESC_LPAREN_SUBEXP | OP_ESC_VBAR_ALT |
        OP_ASTERISK_ZERO_INF | OP_PLUS_ONE_INF |
        OP_QMARK_ZERO_ONE | OP_DECIMAL_BACKREF |
        OP_LINE_ANCHOR | OP_ESC_CONTROL_CHARS ),

        OP2_ESC_GNU_BUF_ANCHOR,

        0,

        ALLOW_EMPTY_RANGE_IN_CC,

        Option.NONE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax Grep = new Syntax(
        "Grep",
        ( OP_DOT_ANYCHAR | OP_BRACKET_CC | OP_POSIX_BRACKET |
        OP_ESC_BRACE_INTERVAL | OP_ESC_LPAREN_SUBEXP |
        OP_ESC_VBAR_ALT |
        OP_ASTERISK_ZERO_INF | OP_ESC_PLUS_ONE_INF |
        OP_ESC_QMARK_ZERO_ONE | OP_LINE_ANCHOR |
        OP_ESC_W_WORD | OP_ESC_B_WORD_BOUND |
        OP_ESC_LTGT_WORD_BEGIN_END | OP_DECIMAL_BACKREF ),

        0,

        0,

        ( ALLOW_EMPTY_RANGE_IN_CC | NOT_NEWLINE_IN_NEGATIVE_CC ),

        Option.NONE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax GnuRegex = new Syntax(
        "GnuRegex",
        GNU_REGEX_OP,

        0,

        0,

        GNU_REGEX_BV,

        Option.NONE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax Java = new Syntax(
        "Java",
        (( GNU_REGEX_OP | OP_QMARK_NON_GREEDY |
        OP_ESC_CONTROL_CHARS | OP_ESC_C_CONTROL |
        OP_ESC_OCTAL3 | OP_ESC_X_HEX2 )
        & ~OP_ESC_LTGT_WORD_BEGIN_END ),

        ( OP2_ESC_CAPITAL_Q_QUOTE | OP2_QMARK_GROUP_EFFECT |
        OP2_OPTION_PERL | OP2_PLUS_POSSESSIVE_REPEAT |
        OP2_PLUS_POSSESSIVE_INTERVAL | OP2_CCLASS_SET_OP |
        OP2_QMARK_LT_NAMED_GROUP | OP2_ESC_K_NAMED_BACKREF |
        OP2_ESC_V_VTAB | OP2_ESC_U_HEX4 |
        OP2_ESC_P_BRACE_CHAR_PROPERTY ),

        0,

        ( GNU_REGEX_BV | DIFFERENT_LEN_ALT_LOOK_BEHIND ),

        (Option.SINGLELINE | Option.WORD_BOUND_ALL_RANGE | Option.WORD_BOUND_ALL_RANGE),

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax Perl = new Syntax(
        "Perl",
        (( GNU_REGEX_OP | OP_QMARK_NON_GREEDY |
        OP_ESC_OCTAL3 | OP_ESC_X_HEX2 |
        OP_ESC_X_BRACE_HEX8 | OP_ESC_CONTROL_CHARS |
        OP_ESC_C_CONTROL )
        & ~OP_ESC_LTGT_WORD_BEGIN_END ),

        ( OP2_ESC_CAPITAL_Q_QUOTE |
        OP2_QMARK_GROUP_EFFECT | OP2_OPTION_PERL |
        OP2_ESC_P_BRACE_CHAR_PROPERTY |
        OP2_ESC_P_BRACE_CIRCUMFLEX_NOT ),

        0,

        GNU_REGEX_BV,

        Option.SINGLELINE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax PerlNG = new Syntax(
        "PerlNG",
        (( GNU_REGEX_OP | OP_QMARK_NON_GREEDY |
        OP_ESC_OCTAL3 | OP_ESC_X_HEX2 |
        OP_ESC_X_BRACE_HEX8 | OP_ESC_CONTROL_CHARS |
        OP_ESC_C_CONTROL )
        & ~OP_ESC_LTGT_WORD_BEGIN_END ),

        ( OP2_ESC_CAPITAL_Q_QUOTE |
        OP2_QMARK_GROUP_EFFECT | OP2_OPTION_PERL |
        OP2_ESC_P_BRACE_CHAR_PROPERTY  |
        OP2_ESC_P_BRACE_CIRCUMFLEX_NOT |
        OP2_QMARK_LT_NAMED_GROUP       |
        OP2_ESC_K_NAMED_BACKREF        |
        OP2_ESC_G_SUBEXP_CALL ),

        0,

        ( GNU_REGEX_BV |
        CAPTURE_ONLY_NAMED_GROUP |
        ALLOW_MULTIPLEX_DEFINITION_NAME ),

        Option.SINGLELINE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );

    public static final Syntax ECMAScript = new Syntax(
        "ECMAScript",
        (( GNU_REGEX_OP | OP_QMARK_NON_GREEDY |
        OP_ESC_OCTAL3 | OP_ESC_X_HEX2 |
        OP_ESC_CONTROL_CHARS | OP_ESC_C_CONTROL |
        OP_DECIMAL_BACKREF | OP_ESC_D_DIGIT |
        OP_ESC_S_WHITE_SPACE | OP_ESC_W_WORD )
        & ~OP_ESC_LTGT_WORD_BEGIN_END ),

        ( OP2_ESC_CAPITAL_Q_QUOTE |
        OP2_QMARK_GROUP_EFFECT | OP2_OPTION_PERL |
        OP2_ESC_P_BRACE_CHAR_PROPERTY |
        OP2_ESC_P_BRACE_CIRCUMFLEX_NOT |
        OP2_ESC_U_HEX4 | OP2_ESC_V_VTAB),

        OP3_OPTION_ECMASCRIPT,

        ( CONTEXT_INDEP_ANCHORS |
        CONTEXT_INDEP_REPEAT_OPS |
        CONTEXT_INVALID_REPEAT_OPS |
        ALLOW_INVALID_INTERVAL |
        BACKSLASH_ESCAPE_IN_CC |
        ALLOW_DOUBLE_RANGE_OP_IN_CC |
        DIFFERENT_LEN_ALT_LOOK_BEHIND ),

        Option.NONE,

        new MetaCharTable(
            '\\',                          /* esc */
            INEFFECTIVE_META_CHAR,         /* anychar '.' */
            INEFFECTIVE_META_CHAR,         /* anytime '*' */
            INEFFECTIVE_META_CHAR,         /* zero or one time '?' */
            INEFFECTIVE_META_CHAR,         /* one or more time '+' */
            INEFFECTIVE_META_CHAR          /* anychar anytime */
        )
    );
}
