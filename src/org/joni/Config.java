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

import java.io.PrintStream;

public interface Config extends org.jcodings.Config {
    int REGEX_MAX_LENGTH = ConfigSupport.getInt("joni.regex_max_length", -1);
    int CHAR_TABLE_SIZE = ConfigSupport.getInt("joni.char_table_size", 256);
    boolean USE_NO_INVALID_QUANTIFIER = ConfigSupport.getBoolean("joni.use_no_invalid_quantifier", true);
    int SCANENV_MEMNODES_SIZE = ConfigSupport.getInt("joni.scanenv_memnodes_size", 8);

    boolean USE_NAMED_GROUP = ConfigSupport.getBoolean("joni.use_named_group", true);
    boolean USE_SUBEXP_CALL = ConfigSupport.getBoolean("joni.use_subexp_call", true);
    boolean USE_PERL_SUBEXP_CALL = ConfigSupport.getBoolean("joni.use_perl_subexp_call", true);
    boolean USE_BACKREF_WITH_LEVEL = ConfigSupport.getBoolean("joni.use_backref_with_level", true); /* \k<name+n>, \k<name-n> */

    boolean USE_MONOMANIAC_CHECK_CAPTURES_IN_ENDLESS_REPEAT = ConfigSupport.getBoolean("joni.use_monomaniac_check_captures_in_endless_repeat", true); /* /(?:()|())*\2/ */
    boolean USE_NEWLINE_AT_END_OF_STRING_HAS_EMPTY_LINE = ConfigSupport.getBoolean("joni.use_newline_at_end_of_string_has_empty_line", true); /* /\n$/ =~ "\n" */
    boolean USE_WARNING_REDUNDANT_NESTED_REPEAT_OPERATOR = ConfigSupport.getBoolean("joni.use_warning_redundant_nested_repeat_operator", true);

    boolean CASE_FOLD_IS_APPLIED_INSIDE_NEGATIVE_CCLASS = ConfigSupport.getBoolean("joni.case_fold_is_applied_inside_negative_cclass", true);

    boolean USE_MATCH_RANGE_MUST_BE_INSIDE_OF_SPECIFIED_RANGE = ConfigSupport.getBoolean("joni.use_match_range_must_be_inside_of_specified_range", false);
    boolean USE_CAPTURE_HISTORY = ConfigSupport.getBoolean("joni.use_capture_history", false);
    boolean USE_VARIABLE_META_CHARS = ConfigSupport.getBoolean("joni.use_variable_meta_chars", true);
    boolean USE_WORD_BEGIN_END = ConfigSupport.getBoolean("joni.use_word_begin_end", true); /* "\<": word-begin, "\>": word-end */
    boolean USE_FIND_LONGEST_SEARCH_ALL_OF_RANGE = ConfigSupport.getBoolean("joni.use_find_longest_search_all_of_range", true);
    boolean USE_SUNDAY_QUICK_SEARCH = ConfigSupport.getBoolean("joni.use_sunday_quick_search", true);
    boolean USE_CEC = ConfigSupport.getBoolean("joni.use_cec", false);
    boolean USE_DYNAMIC_OPTION = ConfigSupport.getBoolean("joni.use_dynamic_option", false);
    boolean USE_BYTE_MAP = ConfigSupport.getBoolean("joni.use_byte_map", OptExactInfo.OPT_EXACT_MAXLEN <= CHAR_TABLE_SIZE);
    boolean USE_INT_MAP_BACKWARD = ConfigSupport.getBoolean("joni.use_int_map_backward", false);

    int NREGION                   = ConfigSupport.getInt("joni.nregion", 10);
    int MAX_BACKREF_NUM           = ConfigSupport.getInt("joni.max_backref_num", 1000);
    int MAX_CAPTURE_GROUP_NUM     = ConfigSupport.getInt("joni.max_capture_group_num", 32767);
    int MAX_REPEAT_NUM            = ConfigSupport.getInt("joni.max_multi_byte_ranges_num", 100000);
    int MAX_MULTI_BYTE_RANGES_NUM = ConfigSupport.getInt("joni.max_multi_byte_ranges_num", 10000);

    // internal config
    boolean USE_OP_PUSH_OR_JUMP_EXACT         = ConfigSupport.getBoolean("joni.use_op_push_or_jump_exact", true);
    boolean USE_QTFR_PEEK_NEXT                = ConfigSupport.getBoolean("joni.use_qtfr_peek_next", true);

    int INIT_MATCH_STACK_SIZE                 = ConfigSupport.getInt("joni.init_match_stack_size", 64);

    boolean OPTIMIZE                          = ConfigSupport.getBoolean("joni.optimize", true);
    @Deprecated boolean DONT_OPTIMIZE                     = !OPTIMIZE;

    // use embedded string templates in Regex object as byte arrays instead of compiling them into int bytecode array
    boolean USE_STRING_TEMPLATES              = ConfigSupport.getBoolean("joni.use_string_templates", true);


    int MAX_CAPTURE_HISTORY_GROUP             = ConfigSupport.getInt("joni.max_capture_history_group", 31);


    int CHECK_STRING_THRESHOLD_LEN            = ConfigSupport.getInt("joni.check_string_threshold_len", 7);
    int CHECK_BUFF_MAX_SIZE                   = ConfigSupport.getInt("joni.check_buff_max_size", 0x4000);

    PrintStream log = System.out;
    PrintStream err = System.err;

    boolean DEBUG_ALL                         = ConfigSupport.getBoolean("joni.debug.all", false);

    boolean DEBUG                             = ConfigSupport.getBoolean("joni.debug", false) || DEBUG_ALL;
    boolean DEBUG_PARSE_TREE                  = ConfigSupport.getBoolean("joni.debug.parse.tree", false) || DEBUG_ALL;
    boolean DEBUG_PARSE_TREE_RAW              = ConfigSupport.getBoolean("joni.debug.parse.tree.raw", true) || DEBUG_ALL;
    boolean DEBUG_COMPILE                     = ConfigSupport.getBoolean("joni.debug.compile", false) || DEBUG_ALL;
    boolean DEBUG_COMPILE_BYTE_CODE_INFO      = ConfigSupport.getBoolean("joni.debug.compile.bytecode.info", false) || DEBUG_ALL;
    boolean DEBUG_SEARCH                      = ConfigSupport.getBoolean("joni.debug.search", false) || DEBUG_ALL;
    boolean DEBUG_MATCH                       = ConfigSupport.getBoolean("joni.debug.match", false) || DEBUG_ALL;
}
