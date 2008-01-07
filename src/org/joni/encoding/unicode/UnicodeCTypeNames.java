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
package org.joni.encoding.unicode;

import org.joni.Config;
import org.joni.util.BytesHash;

public class UnicodeCTypeNames {

    static void initializeCTypeNameTable() {
        BytesHash<Integer> table = new BytesHash<Integer>();
        
        int limit = Config.USE_UNICODE_PROPERTIES ? CTypeNameTable.length : 15;
        
        for (int i=0; i<limit; i++) 
            table.putDirect(CTypeNameTable[i], i);
        
        CTypeNameHash = table;
    }
    
    static BytesHash<Integer> CTypeNameHash;
    
    private static final byte CTypeNameTable[][] = new byte[][] {
        "NEWLINE".getBytes(),
        "Alpha".getBytes(),
        "Blank".getBytes(),
        "Cntrl".getBytes(),
        "Digit".getBytes(),
        "Graph".getBytes(),
        "Lower".getBytes(),
        "Print".getBytes(),
        "Punct".getBytes(),
        "Space".getBytes(),
        "Upper".getBytes(),
        "XDigit".getBytes(),
        "Word".getBytes(),
        "Alnum".getBytes(),
        "ASCII".getBytes(),
        
        // unicode properties
        "Any".getBytes(),
        "Assigned".getBytes(),
        "C".getBytes(),
        "Cc".getBytes(),
        "Cf".getBytes(),
        "Cn".getBytes(),
        "Co".getBytes(),
        "Cs".getBytes(),
        "L".getBytes(),
        "Ll".getBytes(),
        "Lm".getBytes(),
        "Lo".getBytes(),
        "Lt".getBytes(),
        "Lu".getBytes(),
        "M".getBytes(),
        "Mc".getBytes(),
        "Me".getBytes(),
        "Mn".getBytes(),
        "N".getBytes(),
        "Nd".getBytes(),
        "Nl".getBytes(),
        "No".getBytes(),
        "P".getBytes(),
        "Pc".getBytes(),
        "Pd".getBytes(),
        "Pe".getBytes(),
        "Pf".getBytes(),
        "Pi".getBytes(),
        "Po".getBytes(),
        "Ps".getBytes(),
        "S".getBytes(),
        "Sc".getBytes(),
        "Sk".getBytes(),
        "Sm".getBytes(),
        "So".getBytes(),
        "Z".getBytes(),
        "Zl".getBytes(),
        "Zp".getBytes(),
        "Zs".getBytes(),
        "Arabic".getBytes(),
        "Armenian".getBytes(),
        "Bengali".getBytes(),
        "Bopomofo".getBytes(),
        "Braille".getBytes(),
        "Buginese".getBytes(),
        "Buhid".getBytes(),
        "Canadian_Aboriginal".getBytes(),
        "Cherokee".getBytes(),
        "Common".getBytes(),
        "Coptic".getBytes(),
        "Cypriot".getBytes(),
        "Cyrillic".getBytes(),
        "Deseret".getBytes(),
        "Devanagari".getBytes(),
        "Ethiopic".getBytes(),
        "Georgian".getBytes(),
        "Glagolitic".getBytes(),
        "Gothic".getBytes(),
        "Greek".getBytes(),
        "Gujarati".getBytes(),
        "Gurmukhi".getBytes(),
        "Han".getBytes(),
        "Hangul".getBytes(),
        "Hanunoo".getBytes(),
        "Hebrew".getBytes(),
        "Hiragana".getBytes(),
        "Inherited".getBytes(),
        "Kannada".getBytes(),
        "Katakana".getBytes(),
        "Kharoshthi".getBytes(),
        "Khmer".getBytes(),
        "Lao".getBytes(),
        "Latin".getBytes(),
        "Limbu".getBytes(),
        "Linear_B".getBytes(),
        "Malayalam".getBytes(),
        "Mongolian".getBytes(),
        "Myanmar".getBytes(),
        "New_Tai_Lue".getBytes(),
        "Ogham".getBytes(),
        "Old_Italic".getBytes(),
        "Old_Persian".getBytes(),
        "Oriya".getBytes(),
        "Osmanya".getBytes(),
        "Runic".getBytes(),
        "Shavian".getBytes(),
        "Sinhala".getBytes(),
        "Syloti_Nagri".getBytes(),
        "Syriac".getBytes(),
        "Tagalog".getBytes(),
        "Tagbanwa".getBytes(),
        "Tai_Le".getBytes(),
        "Tamil".getBytes(),
        "Telugu".getBytes(),
        "Thaana".getBytes(),
        "Thai".getBytes(),
        "Tibetan".getBytes(),
        "Tifinagh".getBytes(),
        "Ugaritic".getBytes(),
        "Yi".getBytes()
    };

}
