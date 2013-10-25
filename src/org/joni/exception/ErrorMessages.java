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
package org.joni.exception;

import org.joni.Config;

public interface ErrorMessages extends org.jcodings.exception.ErrorMessages {
    final String MISMATCH = "mismatch";
    final String NO_SUPPORT_CONFIG = "no support in this configuration";

    /* internal error */
    final String ERR_MEMORY = "fail to memory allocation";
    final String ERR_MATCH_STACK_LIMIT_OVER = "match-stack limit over";
    final String ERR_TYPE_BUG = "undefined type (bug)";
    final String ERR_PARSER_BUG = "internal parser error (bug)";
    final String ERR_STACK_BUG = "stack error (bug)";
    final String ERR_UNDEFINED_BYTECODE = "undefined bytecode (bug)";
    final String ERR_UNEXPECTED_BYTECODE = "unexpected bytecode (bug)";
    final String ERR_DEFAULT_ENCODING_IS_NOT_SETTED = "default multibyte-encoding is not setted";
    final String ERR_SPECIFIED_ENCODING_CANT_CONVERT_TO_WIDE_CHAR = "can't convert to wide-char on specified multibyte-encoding";

    /* general error */
    final String ERR_INVALID_ARGUMENT = "invalid argument";

    /* syntax error */
    final String ERR_END_PATTERN_AT_LEFT_BRACE = "end pattern at left brace";
    final String ERR_END_PATTERN_AT_LEFT_BRACKET = "end pattern at left bracket";
    final String ERR_EMPTY_CHAR_CLASS = "empty char-class";
    final String ERR_PREMATURE_END_OF_CHAR_CLASS = "premature end of char-class";
    final String ERR_END_PATTERN_AT_ESCAPE = "end pattern at escape";
    final String ERR_END_PATTERN_AT_META = "end pattern at meta";
    final String ERR_END_PATTERN_AT_CONTROL = "end pattern at control";
    final String ERR_META_CODE_SYNTAX = "invalid meta-code syntax";
    final String ERR_CONTROL_CODE_SYNTAX = "invalid control-code syntax";
    final String ERR_CHAR_CLASS_VALUE_AT_END_OF_RANGE = "char-class value at end of range";
    final String ERR_CHAR_CLASS_VALUE_AT_START_OF_RANGE = "char-class value at start of range";
    final String ERR_UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS = "unmatched range specifier in char-class";
    final String ERR_TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED = "target of repeat operator is not specified";
    final String ERR_TARGET_OF_REPEAT_OPERATOR_INVALID = "target of repeat operator is invalid";
    final String ERR_NESTED_REPEAT_NOT_ALLOWED = "nested repeat is not allowed";
    final String ERR_NESTED_REPEAT_OPERATOR = "nested repeat operator";
    final String ERR_UNMATCHED_CLOSE_PARENTHESIS = "unmatched close parenthesis";
    final String ERR_END_PATTERN_WITH_UNMATCHED_PARENTHESIS = "end pattern with unmatched parenthesis";
    final String ERR_END_PATTERN_IN_GROUP = "end pattern in group";
    final String ERR_UNDEFINED_GROUP_OPTION = "undefined group option";
    final String ERR_INVALID_POSIX_BRACKET_TYPE = "invalid POSIX bracket type";
    final String ERR_INVALID_LOOK_BEHIND_PATTERN = "invalid pattern in look-behind";
    final String ERR_INVALID_REPEAT_RANGE_PATTERN = "invalid repeat range {lower,upper}";

    /* values error (syntax error) */
    final String ERR_TOO_BIG_NUMBER = "too big number";
    final String ERR_TOO_BIG_NUMBER_FOR_REPEAT_RANGE = "too big number for repeat range";
    final String ERR_UPPER_SMALLER_THAN_LOWER_IN_REPEAT_RANGE = "upper is smaller than lower in repeat range";
    final String ERR_EMPTY_RANGE_IN_CHAR_CLASS = "empty range in char class";
    final String ERR_MISMATCH_CODE_LENGTH_IN_CLASS_RANGE = "mismatch multibyte code length in char-class range";
    final String ERR_TOO_MANY_MULTI_BYTE_RANGES = "too many multibyte code ranges are specified";
    final String ERR_TOO_SHORT_MULTI_BYTE_STRING = "too short multibyte code string";
    final String ERR_TOO_BIG_BACKREF_NUMBER = "too big backref number";
    final String ERR_INVALID_BACKREF = Config.USE_NAMED_GROUP ? "invalid backref number/name" : "invalid backref number";
    final String ERR_NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED = "numbered backref/call is not allowed. (use name)";
    final String ERR_INVALID_WIDE_CHAR_VALUE = "invalid wide-char value";
    final String ERR_EMPTY_GROUP_NAME = "group name is empty";
    final String ERR_INVALID_GROUP_NAME = "invalid group name <%n>";
    final String ERR_INVALID_CHAR_IN_GROUP_NAME = Config.USE_NAMED_GROUP ? "invalid char in group name <%n>" : "invalid char in group number <%n>";
    final String ERR_UNDEFINED_NAME_REFERENCE = "undefined name <%n> reference";
    final String ERR_UNDEFINED_GROUP_REFERENCE = "undefined group <%n> reference";
    final String ERR_MULTIPLEX_DEFINED_NAME = "multiplex defined name <%n>";
    final String ERR_MULTIPLEX_DEFINITION_NAME_CALL = "multiplex definition name <%n> call";
    final String ERR_NEVER_ENDING_RECURSION = "never ending recursion";
    final String ERR_GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY = "group number is too big for capture history";
    final String ERR_NOT_SUPPORTED_ENCODING_COMBINATION = "not supported encoding combination";
    final String ERR_INVALID_COMBINATION_OF_OPTIONS = "invalid combination of options";
    final String ERR_OVER_THREAD_PASS_LIMIT_COUNT = "over thread pass limit count";
    final String ERR_TOO_BIG_SB_CHAR_VALUE = "too big singlebyte char value";

}
