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
    /* internal error */
    final String PARSER_BUG = "internal parser error (bug)";
    final String UNDEFINED_BYTECODE = "undefined bytecode (bug)";
    final String UNEXPECTED_BYTECODE = "unexpected bytecode (bug)";
    final String TOO_MANY_CAPTURE_GROUPS = "too many capture groups are specified";

    /* general error */
    final String INVALID_ARGUMENT = "invalid argument";

    /* syntax error */
    final String END_PATTERN_AT_LEFT_BRACE = "end pattern at left brace";
    final String END_PATTERN_AT_LEFT_BRACKET = "end pattern at left bracket";
    final String EMPTY_CHAR_CLASS = "empty char-class";
    final String PREMATURE_END_OF_CHAR_CLASS = "premature end of char-class";
    final String END_PATTERN_AT_ESCAPE = "end pattern at escape";
    final String END_PATTERN_AT_META = "end pattern at meta";
    final String END_PATTERN_AT_CONTROL = "end pattern at control";
    final String META_CODE_SYNTAX = "invalid meta-code syntax";
    final String CONTROL_CODE_SYNTAX = "invalid control-code syntax";
    final String CHAR_CLASS_VALUE_AT_END_OF_RANGE = "char-class value at end of range";
    final String CHAR_CLASS_VALUE_AT_START_OF_RANGE = "char-class value at start of range";
    final String UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS = "unmatched range specifier in char-class";
    final String TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED = "target of repeat operator is not specified";
    final String TARGET_OF_REPEAT_OPERATOR_INVALID = "target of repeat operator is invalid";
    final String NESTED_REPEAT_NOT_ALLOWED = "nested repeat is not allowed";
    final String NESTED_REPEAT_OPERATOR = "nested repeat operator";
    final String UNMATCHED_CLOSE_PARENTHESIS = "unmatched close parenthesis";
    final String END_PATTERN_WITH_UNMATCHED_PARENTHESIS = "end pattern with unmatched parenthesis";
    final String END_PATTERN_IN_GROUP = "end pattern in group";
    final String UNDEFINED_GROUP_OPTION = "undefined group option";
    final String INVALID_POSIX_BRACKET_TYPE = "invalid POSIX bracket type";
    final String INVALID_LOOK_BEHIND_PATTERN = "invalid pattern in look-behind";
    final String INVALID_REPEAT_RANGE_PATTERN = "invalid repeat range {lower,upper}";
    final String INVALID_CONDITION_PATTERN = "invalid conditional pattern";

    /* values error (syntax error) */
    final String TOO_BIG_NUMBER = "too big number";
    final String TOO_BIG_NUMBER_FOR_REPEAT_RANGE = "too big number for repeat range";
    final String UPPER_SMALLER_THAN_LOWER_IN_REPEAT_RANGE = "upper is smaller than lower in repeat range";
    final String EMPTY_RANGE_IN_CHAR_CLASS = "empty range in char class";
    final String MISMATCH_CODE_LENGTH_IN_CLASS_RANGE = "mismatch multibyte code length in char-class range";
    final String TOO_MANY_MULTI_BYTE_RANGES = "too many multibyte code ranges are specified";
    final String TOO_SHORT_MULTI_BYTE_STRING = "too short multibyte code string";
    final String TOO_BIG_BACKREF_NUMBER = "too big backref number";
    final String INVALID_BACKREF = Config.USE_NAMED_GROUP ? "invalid backref number/name" : "invalid backref number";
    final String NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED = "numbered backref/call is not allowed. (use name)";
    final String TOO_SHORT_DIGITS = "too short digits";
    final String INVALID_WIDE_CHAR_VALUE = "invalid wide-char value";
    final String EMPTY_GROUP_NAME = "group name is empty";
    final String INVALID_GROUP_NAME = "invalid group name <%n>";
    final String INVALID_CHAR_IN_GROUP_NAME = Config.USE_NAMED_GROUP ? "invalid char in group name <%n>" : "invalid char in group number <%n>";
    final String UNDEFINED_NAME_REFERENCE = "undefined name <%n> reference";
    final String UNDEFINED_GROUP_REFERENCE = "undefined group <%n> reference";
    final String MULTIPLEX_DEFINED_NAME = "multiplex defined name <%n>";
    final String MULTIPLEX_DEFINITION_NAME_CALL = "multiplex definition name <%n> call";
    final String NEVER_ENDING_RECURSION = "never ending recursion";
    final String GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY = "group number is too big for capture history";
    final String NOT_SUPPORTED_ENCODING_COMBINATION = "not supported encoding combination";
    final String INVALID_COMBINATION_OF_OPTIONS = "invalid combination of options";
    final String OVER_THREAD_PASS_LIMIT_COUNT = "over thread pass limit count";
    final String TOO_BIG_SB_CHAR_VALUE = "too big singlebyte char value";

}
