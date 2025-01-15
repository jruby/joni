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
    String PARSER_BUG = "internal parser error (bug)";
    String UNDEFINED_BYTECODE = "undefined bytecode (bug)";
    String UNEXPECTED_BYTECODE = "unexpected bytecode (bug)";
    String TOO_MANY_CAPTURE_GROUPS = "too many capture groups are specified";

    /* general error */
    String INVALID_ARGUMENT = "invalid argument";

    /* syntax error */
    String REGEX_TOO_LONG = "regex length too long";
    String END_PATTERN_AT_LEFT_BRACE = "end pattern at left brace";
    String END_PATTERN_AT_LEFT_BRACKET = "end pattern at left bracket";
    String EMPTY_CHAR_CLASS = "empty char-class";
    String PREMATURE_END_OF_CHAR_CLASS = "premature end of char-class";
    String END_PATTERN_AT_ESCAPE = "end pattern at escape";
    String END_PATTERN_AT_META = "end pattern at meta";
    String END_PATTERN_AT_CONTROL = "end pattern at control";
    String META_CODE_SYNTAX = "invalid meta-code syntax";
    String CONTROL_CODE_SYNTAX = "invalid control-code syntax";
    String CHAR_CLASS_VALUE_AT_END_OF_RANGE = "char-class value at end of range";
    String CHAR_CLASS_VALUE_AT_START_OF_RANGE = "char-class value at start of range";
    String UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS = "unmatched range specifier in char-class";
    String TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED = "target of repeat operator is not specified";
    String TARGET_OF_REPEAT_OPERATOR_INVALID = "target of repeat operator is invalid";
    String NESTED_REPEAT_NOT_ALLOWED = "nested repeat is not allowed";
    String NESTED_REPEAT_OPERATOR = "nested repeat operator";
    String UNMATCHED_CLOSE_PARENTHESIS = "unmatched close parenthesis";
    String END_PATTERN_WITH_UNMATCHED_PARENTHESIS = "end pattern with unmatched parenthesis";
    String END_PATTERN_IN_GROUP = "end pattern in group";
    String UNDEFINED_GROUP_OPTION = "undefined group option";
    String INVALID_POSIX_BRACKET_TYPE = "invalid POSIX bracket type";
    String INVALID_LOOK_BEHIND_PATTERN = "invalid pattern in look-behind";
    String INVALID_REPEAT_RANGE_PATTERN = "invalid repeat range {lower,upper}";
    String INVALID_CONDITION_PATTERN = "invalid conditional pattern";

    /* values error (syntax error) */
    String TOO_BIG_NUMBER = "too big number";
    String TOO_BIG_NUMBER_FOR_REPEAT_RANGE = "too big number for repeat range";
    String UPPER_SMALLER_THAN_LOWER_IN_REPEAT_RANGE = "upper is smaller than lower in repeat range";
    String EMPTY_RANGE_IN_CHAR_CLASS = "empty range in char class";
    String MISMATCH_CODE_LENGTH_IN_CLASS_RANGE = "mismatch multibyte code length in char-class range";
    String TOO_MANY_MULTI_BYTE_RANGES = "too many multibyte code ranges are specified";
    String TOO_SHORT_MULTI_BYTE_STRING = "too short multibyte code string";
    String TOO_BIG_BACKREF_NUMBER = "too big backref number";
    String INVALID_BACKREF = Config.USE_NAMED_GROUP ? "invalid backref number/name" : "invalid backref number";
    String NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED = "numbered backref/call is not allowed. (use name)";
    String TOO_SHORT_DIGITS = "too short digits";
    String INVALID_WIDE_CHAR_VALUE = "invalid wide-char value";
    String EMPTY_GROUP_NAME = "group name is empty";
    String INVALID_GROUP_NAME = "invalid group name <%n>";
    String INVALID_CHAR_IN_GROUP_NAME = Config.USE_NAMED_GROUP ? "invalid char in group name <%n>" : "invalid char in group number <%n>";
    String UNDEFINED_NAME_REFERENCE = "undefined name <%n> reference";
    String UNDEFINED_GROUP_REFERENCE = "undefined group <%n> reference";
    String MULTIPLEX_DEFINED_NAME = "multiplex defined name <%n>";
    String MULTIPLEX_DEFINITION_NAME_CALL = "multiplex definition name <%n> call";
    String PROPERTY_NAME_NEVER_TERMINATED = "property name never terminated \\p{%n";
    String NEVER_ENDING_RECURSION = "never ending recursion";
    String GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY = "group number is too big for capture history";
    String NOT_SUPPORTED_ENCODING_COMBINATION = "not supported encoding combination";
    String INVALID_COMBINATION_OF_OPTIONS = "invalid combination of options";
    String OVER_THREAD_PASS_LIMIT_COUNT = "over thread pass limit count";
    String TOO_BIG_SB_CHAR_VALUE = "too big singlebyte char value";

}
