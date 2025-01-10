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

public interface NodeType {
    /* node type */
    int  STR        = 0;
    int  CCLASS     = 1;
    int  CTYPE      = 2;
    int  CANY       = 3;
    int  BREF       = 4;
    int  QTFR       = 5;
    int  ENCLOSE    = 6;
    int  ANCHOR     = 7;
    int  LIST       = 8;
    int  ALT        = 9;
    int  CALL       = 10;

    int BIT_STR        = 1 << STR;
    int BIT_CCLASS     = 1 << CCLASS;
    int BIT_CTYPE      = 1 << CTYPE;
    int BIT_CANY       = 1 << CANY;
    int BIT_BREF       = 1 << BREF;
    int BIT_QTFR       = 1 << QTFR;
    int BIT_ENCLOSE    = 1 << ENCLOSE;
    int BIT_ANCHOR     = 1 << ANCHOR;
    int BIT_LIST       = 1 << LIST;
    int BIT_ALT        = 1 << ALT;
    int BIT_CALL       = 1 << CALL;

    /* allowed node types in look-behind */
    int ALLOWED_IN_LB = ( BIT_LIST |
                                BIT_ALT |
                                BIT_STR |
                                BIT_CCLASS |
                                BIT_CTYPE |
                                BIT_CANY |
                                BIT_ANCHOR |
                                BIT_ENCLOSE |
                                BIT_QTFR |
                                BIT_CALL );

    int SIMPLE =        ( BIT_STR |
                                BIT_CCLASS |
                                BIT_CTYPE |
                                BIT_CANY |
                                BIT_BREF);

}
