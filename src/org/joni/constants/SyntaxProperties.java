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
package org.joni.constants;

public interface SyntaxProperties {
    /* syntax (operators); */
    int OP_VARIABLE_META_CHARACTERS    = (1<<0);
    int OP_DOT_ANYCHAR                 = (1<<1);   /* . */
    int OP_ASTERISK_ZERO_INF           = (1<<2);   /* * */
    int OP_ESC_ASTERISK_ZERO_INF       = (1<<3);
    int OP_PLUS_ONE_INF                = (1<<4);   /* + */
    int OP_ESC_PLUS_ONE_INF            = (1<<5);
    int OP_QMARK_ZERO_ONE              = (1<<6);   /* ? */
    int OP_ESC_QMARK_ZERO_ONE          = (1<<7);
    int OP_BRACE_INTERVAL              = (1<<8);   /* {lower,upper} */
    int OP_ESC_BRACE_INTERVAL          = (1<<9);   /* \{lower,upper\} */
    int OP_VBAR_ALT                    = (1<<10);   /* | */
    int OP_ESC_VBAR_ALT                = (1<<11);  /* \| */
    int OP_LPAREN_SUBEXP               = (1<<12);  /* (...);   */
    int OP_ESC_LPAREN_SUBEXP           = (1<<13);  /* \(...\); */
    int OP_ESC_AZ_BUF_ANCHOR           = (1<<14);  /* \A, \Z, \z */
    int OP_ESC_CAPITAL_G_BEGIN_ANCHOR  = (1<<15);  /* \G     */
    int OP_DECIMAL_BACKREF             = (1<<16);  /* \num   */
    int OP_BRACKET_CC                  = (1<<17);  /* [...]  */
    int OP_ESC_W_WORD                  = (1<<18);  /* \w, \W */
    int OP_ESC_LTGT_WORD_BEGIN_END     = (1<<19);  /* \<. \> */
    int OP_ESC_B_WORD_BOUND            = (1<<20);  /* \b, \B */
    int OP_ESC_S_WHITE_SPACE           = (1<<21);  /* \s, \S */
    int OP_ESC_D_DIGIT                 = (1<<22);  /* \d, \D */
    int OP_LINE_ANCHOR                 = (1<<23);  /* ^, $   */
    int OP_POSIX_BRACKET               = (1<<24);  /* [:xxxx:] */
    int OP_QMARK_NON_GREEDY            = (1<<25);  /* ??,*?,+?,{n,m}? */
    int OP_ESC_CONTROL_CHARS           = (1<<26);  /* \n,\r,\t,\a ... */
    int OP_ESC_C_CONTROL               = (1<<27);  /* \cx  */
    int OP_ESC_OCTAL3                  = (1<<28);  /* \OOO */
    int OP_ESC_X_HEX2                  = (1<<29);  /* \xHH */
    int OP_ESC_X_BRACE_HEX8            = (1<<30);  /* \x{7HHHHHHH} */
    int OP_ESC_O_BRACE_OCTAL           = (1<<31); /* \o{OOO} */

    int OP2_ESC_CAPITAL_Q_QUOTE        = (1<<0);  /* \Q...\E */
    int OP2_QMARK_GROUP_EFFECT         = (1<<1);  /* (?...); */
    int OP2_OPTION_PERL                = (1<<2);  /* (?imsxadlu), (?-imsx), (?^imsxalu) */
    int OP2_OPTION_RUBY                = (1<<3);  /* (?imxadu);, (?-imx);  */
    int OP2_PLUS_POSSESSIVE_REPEAT     = (1<<4);  /* ?+,*+,++ */
    int OP2_PLUS_POSSESSIVE_INTERVAL   = (1<<5);  /* {n,m}+   */
    int OP2_CCLASS_SET_OP              = (1<<6);  /* [...&&..[..]..] */
    int OP2_QMARK_LT_NAMED_GROUP       = (1<<7);  /* (?<name>...); */
    int OP2_ESC_K_NAMED_BACKREF        = (1<<8);  /* \k<name> */
    int OP2_ESC_G_SUBEXP_CALL          = (1<<9);  /* \g<name>, \g<n> */
    int OP2_ATMARK_CAPTURE_HISTORY     = (1<<10); /* (?@..);,(?@<x>..); */
    int OP2_ESC_CAPITAL_C_BAR_CONTROL  = (1<<11); /* \C-x */
    int OP2_ESC_CAPITAL_M_BAR_META     = (1<<12); /* \M-x */
    int OP2_ESC_V_VTAB                 = (1<<13); /* \v as VTAB */
    int OP2_ESC_U_HEX4                 = (1<<14); /* \\uHHHH */
    int OP2_ESC_GNU_BUF_ANCHOR         = (1<<15); /* \`, \' */
    int OP2_ESC_P_BRACE_CHAR_PROPERTY  = (1<<16); /* \p{...}, \P{...} */
    int OP2_ESC_P_BRACE_CIRCUMFLEX_NOT = (1<<17); /* \p{^..}, \P{^..} */
    /* final int OP2_CHAR_PROPERTY_PREFIX_IS = (1<<18); */
    int OP2_ESC_H_XDIGIT               = (1<<19); /* \h, \H */
    int OP2_INEFFECTIVE_ESCAPE         = (1<<20); /* \ */
    int OP2_ESC_CAPITAL_R_LINEBREAK    = (1<<21); /* \R as (?>\x0D\x0A|[\x0A-\x0D\x{85}\x{2028}\x{2029}]) */
    int OP2_ESC_CAPITAL_X_EXTENDED_GRAPHEME_CLUSTER = (1<<22); /* \X as (?:\P{M}\p{M}*) */
    int OP2_ESC_V_VERTICAL_WHITESPACE  = (1<<23); /* \v, \V -- Perl */
    int OP2_ESC_H_HORIZONTAL_WHITESPACE= (1<<24); /* \h, \H -- Perl */
    int OP2_ESC_CAPITAL_K_KEEP         = (1<<25); /* \K */
    int OP2_ESC_G_BRACE_BACKREF        = (1<<26); /* \g{name}, \g{n} */
    int OP2_QMARK_SUBEXP_CALL          = (1<<27); /* (?&name), (?n), (?R), (?0) */
    int OP2_QMARK_BAR_BRANCH_RESET     = (1<<28); /* (?|...) */
    int OP2_QMARK_LPAREN_CONDITION     = (1<<29); /* (?(cond)yes...|no...) */
    int OP2_QMARK_CAPITAL_P_NAMED_GROUP= (1<<30); /* (?P<name>...), (?P=name), (?P>name) -- Python/PCRE */
    int OP2_QMARK_TILDE_ABSENT         = (1<<31); /* (?~...) */

    int OP3_OPTION_JAVA                = (1<<0); /* (?idmsux), (?-idmsux) */
    int OP3_OPTION_ECMASCRIPT          = (1<<1); /* EcmaScript quirks */

    /* syntax (behavior); */
    int CONTEXT_INDEP_ANCHORS           = (1<<31); /* not implemented */
    int CONTEXT_INDEP_REPEAT_OPS        = (1<<0);  /* ?, *, +, {n,m} */
    int CONTEXT_INVALID_REPEAT_OPS      = (1<<1);  /* error or ignore */
    int ALLOW_UNMATCHED_CLOSE_SUBEXP    = (1<<2);  /* ...);... */
    int ALLOW_INVALID_INTERVAL          = (1<<3);  /* {??? */
    int ALLOW_INTERVAL_LOW_ABBREV       = (1<<4);  /* {,n} => {0,n} */
    int STRICT_CHECK_BACKREF            = (1<<5);  /* /(\1);/,/\1();/ ..*/
    int DIFFERENT_LEN_ALT_LOOK_BEHIND   = (1<<6);  /* (?<=a|bc); */
    int CAPTURE_ONLY_NAMED_GROUP        = (1<<7);  /* see doc/RE */
    int ALLOW_MULTIPLEX_DEFINITION_NAME = (1<<8);  /* (?<x>);(?<x>); */
    int FIXED_INTERVAL_IS_GREEDY_ONLY   = (1<<9);  /* a{n}?=(?:a{n});? */
    int ALLOW_MULTIPLEX_DEFINITION_NAME_CALL = (1<<10);  /* (?<x>)(?<x>)(?&x) */

    /* syntax (behavior); in char class [...] */
    int NOT_NEWLINE_IN_NEGATIVE_CC      = (1<<20); /* [^...] */
    int BACKSLASH_ESCAPE_IN_CC          = (1<<21); /* [..\w..] etc.. */
    int ALLOW_EMPTY_RANGE_IN_CC         = (1<<22);
    int ALLOW_DOUBLE_RANGE_OP_IN_CC     = (1<<23); /* [0-9-a]=[0-9\-a] */
    /* syntax (behavior); warning */
    int WARN_CC_OP_NOT_ESCAPED          = (1<<24); /* [,-,] */
    int WARN_REDUNDANT_NESTED_REPEAT    = (1<<25); /* (?:a*);+ */
    int WARN_CC_DUP                     = (1<<26); /* [aa] */

    int POSIX_COMMON_OP =
                            OP_DOT_ANYCHAR | OP_POSIX_BRACKET |
                            OP_DECIMAL_BACKREF |
                            OP_BRACKET_CC | OP_ASTERISK_ZERO_INF |
                            OP_LINE_ANCHOR |
                            OP_ESC_CONTROL_CHARS;

    int GNU_REGEX_OP =
                            OP_DOT_ANYCHAR | OP_BRACKET_CC |
                            OP_POSIX_BRACKET | OP_DECIMAL_BACKREF |
                            OP_BRACE_INTERVAL | OP_LPAREN_SUBEXP |
                            OP_VBAR_ALT |
                            OP_ASTERISK_ZERO_INF | OP_PLUS_ONE_INF |
                            OP_QMARK_ZERO_ONE |
                            OP_ESC_AZ_BUF_ANCHOR | OP_ESC_CAPITAL_G_BEGIN_ANCHOR |
                            OP_ESC_W_WORD |
                            OP_ESC_B_WORD_BOUND | OP_ESC_LTGT_WORD_BEGIN_END |
                            OP_ESC_S_WHITE_SPACE | OP_ESC_D_DIGIT |
                            OP_LINE_ANCHOR;

    int GNU_REGEX_BV =
                            CONTEXT_INDEP_ANCHORS | CONTEXT_INDEP_REPEAT_OPS |
                            CONTEXT_INVALID_REPEAT_OPS | ALLOW_INVALID_INTERVAL |
                            BACKSLASH_ESCAPE_IN_CC | ALLOW_DOUBLE_RANGE_OP_IN_CC;
}
