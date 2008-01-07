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

public interface CharacterType {
    final int NEWLINE   = 0;
    final int ALPHA     = 1;
    final int BLANK     = 2;
    final int CNTRL     = 3;
    final int DIGIT     = 4;
    final int GRAPH     = 5;
    final int LOWER     = 6;
    final int PRINT     = 7;
    final int PUNCT     = 8;
    final int SPACE     = 9;
    final int UPPER     = 10;
    final int XDIGIT    = 11;
    final int WORD      = 12;
    final int ALNUM     = 13;      /* alpha || digit */
    final int ASCII     = 14;
    
    final int MAX_STD_CTYPE = 14;
    
    final int BIT_NEWLINE  = (1<< NEWLINE);
    final int BIT_ALPHA    = (1<< ALPHA);
    final int BIT_BLANK    = (1<< BLANK);
    final int BIT_CNTRL    = (1<< CNTRL);
    final int BIT_DIGIT    = (1<< DIGIT);
    final int BIT_GRAPH    = (1<< GRAPH);
    final int BIT_LOWER    = (1<< LOWER);
    final int BIT_PRINT    = (1<< PRINT);
    final int BIT_PUNCT    = (1<< PUNCT);
    final int BIT_SPACE    = (1<< SPACE);
    final int BIT_UPPER    = (1<< UPPER);
    final int BIT_XDIGIT   = (1<< XDIGIT);
    final int BIT_WORD     = (1<< WORD);
    final int BIT_ALNUM    = (1<< ALNUM);
    final int BIT_ASCII    = (1<< ASCII);
    
}
