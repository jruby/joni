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

public interface NodeStatus {
    /* status bits */
    int NST_MIN_FIXED            = (1<<0);
    int NST_MAX_FIXED            = (1<<1);
    int NST_CLEN_FIXED           = (1<<2);
    int NST_MARK1                = (1<<3);
    int NST_MARK2                = (1<<4);
    int NST_MEM_BACKREFED        = (1<<5);
    int NST_STOP_BT_SIMPLE_REPEAT= (1<<6);
    int NST_RECURSION            = (1<<7);
    int NST_CALLED               = (1<<8);
    int NST_ADDR_FIXED           = (1<<9);
    int NST_NAMED_GROUP          = (1<<10);
    int NST_NAME_REF             = (1<<11);
    int NST_IN_REPEAT            = (1<<12);   /* STK_REPEAT is nested in stack. */
    int NST_NEST_LEVEL           = (1<<13);
    int NST_BY_NUMBER            = (1<<14);   /* {n,m} */
}
