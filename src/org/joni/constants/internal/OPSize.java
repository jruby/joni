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
package org.joni.constants.internal;

public interface OPSize {

    // this might be helpful for potential byte[] migration
    int OPCODE                = 1;
    int RELADDR               = 1;
    int ABSADDR               = 1;
    int LENGTH                = 1;
    int MEMNUM                = 1;
    int STATE_CHECK_NUM       = 1;
    int REPEATNUM             = 1;
    int OPTION                = 1;
    int CODE_POINT            = 1;
    int POINTER               = 1;
    int INDEX                 = 1;

    /* op-code + arg size */

    int ANYCHAR_STAR                  = OPCODE;
    int ANYCHAR_STAR_PEEK_NEXT        = (OPCODE + 1);
    int JUMP                          = (OPCODE + RELADDR);
    int PUSH                          = (OPCODE + RELADDR);
    int POP                           = OPCODE;
    int PUSH_OR_JUMP_EXACT1           = (OPCODE + RELADDR + 1);
    int PUSH_IF_PEEK_NEXT             = (OPCODE + RELADDR + 1);
    int REPEAT_INC                    = (OPCODE + MEMNUM);
    int REPEAT_INC_NG                 = (OPCODE + MEMNUM);
    int PUSH_POS                      = OPCODE;
    int PUSH_POS_NOT                  = (OPCODE + RELADDR);
    int POP_POS                       = OPCODE;
    int FAIL_POS                      = OPCODE;
    int SET_OPTION                    = (OPCODE + OPTION);
    int SET_OPTION_PUSH               = (OPCODE + OPTION);
    int FAIL                          = OPCODE;
    int MEMORY_START                  = (OPCODE + MEMNUM);
    int MEMORY_START_PUSH             = (OPCODE + MEMNUM);
    int MEMORY_END_PUSH               = (OPCODE + MEMNUM);
    int MEMORY_END_PUSH_REC           = (OPCODE + MEMNUM);
    int MEMORY_END                    = (OPCODE + MEMNUM);
    int MEMORY_END_REC                = (OPCODE + MEMNUM);
    int PUSH_STOP_BT                  = OPCODE;
    int POP_STOP_BT                   = OPCODE;
    int NULL_CHECK_START              = (OPCODE + MEMNUM);
    int NULL_CHECK_END                = (OPCODE + MEMNUM);
    int LOOK_BEHIND                   = (OPCODE + LENGTH);
    int PUSH_LOOK_BEHIND_NOT          = (OPCODE + RELADDR + LENGTH);
    int FAIL_LOOK_BEHIND_NOT          = OPCODE;
    int CALL                          = (OPCODE + ABSADDR);
    int RETURN                        = OPCODE;
    int CONDITION                     = (OPCODE + MEMNUM + RELADDR);
    int PUSH_ABSENT_POS               = OPCODE;
    int ABSENT                        = (OPCODE + RELADDR);
    int ABSENT_END                    = OPCODE;

    // #ifdef USE_COMBINATION_EXPLOSION_CHECK
    int STATE_CHECK                   = (OPCODE + STATE_CHECK_NUM);
    int STATE_CHECK_PUSH              = (OPCODE + STATE_CHECK_NUM + RELADDR);
    int STATE_CHECK_PUSH_OR_JUMP      = (OPCODE + STATE_CHECK_NUM + RELADDR);
    int STATE_CHECK_ANYCHAR_STAR      = (OPCODE + STATE_CHECK_NUM);
}
