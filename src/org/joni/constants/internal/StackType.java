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

public interface StackType {
    /** stack **/
    int INVALID_STACK_INDEX           = -1;

    /* stack type */
    /* used by normal-POP */
    int ALT                           = 0x0001;
    int LOOK_BEHIND_NOT               = 0x0002;
    int POS_NOT                       = 0x0003;
    /* handled by normal-POP */
    int MEM_START                     = 0x0100;
    int MEM_END                       = 0x8200;
    int REPEAT_INC                    = 0x0300;
    int STATE_CHECK_MARK              = 0x1000;
    /* avoided by normal-POP */
    int NULL_CHECK_START              = 0x3000;
    int NULL_CHECK_END                = 0x5000;  /* for recursive call */
    int MEM_END_MARK                  = 0x8400;
    int POS                           = 0x0500;  /* used when POP-POS */
    int STOP_BT                       = 0x0600;  /* mark for "(?>...)" */
    int REPEAT                        = 0x0700;
    int CALL_FRAME                    = 0x0800;
    int RETURN                        = 0x0900;
    int VOID                          = 0x0a00;  /* for fill a blank */
    int ABSENT_POS                    = 0x0b00;  /* for absent */
    int ABSENT                        = 0x0c00;  /* absent inner loop marker */

    /* stack type check mask */
    int MASK_POP_USED                 = 0x00ff;
    int MASK_TO_VOID_TARGET           = 0x10ff;
    int MASK_MEM_END_OR_MARK          = 0x8000;  /* MEM_END or MEM_END_MARK */
}
