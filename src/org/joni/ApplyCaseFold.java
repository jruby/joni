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

import org.jcodings.ApplyAllCaseFoldFunction;
import org.jcodings.Encoding;
import org.joni.ast.CClassNode;
import org.joni.ast.ConsAltNode;
import org.joni.ast.StringNode;

final class ApplyCaseFold implements ApplyAllCaseFoldFunction {

    // i_apply_case_fold
    public void apply(int from, int[]to, int length, Object o) {
        ApplyCaseFoldArg arg = (ApplyCaseFoldArg)o;

        ScanEnvironment env = arg.env;
        Encoding enc = env.enc;
        CClassNode cc = arg.cc;
        BitSet bs = cc.bs;
        
        if (length == 1) {
            boolean inCC = cc.isCodeInCC(enc, from);

            if (Config.CASE_FOLD_IS_APPLIED_INSIDE_NEGATIVE_CCLASS) {
                if ((inCC && !cc.isNot()) || (!inCC && cc.isNot())) {
                    if (enc.minLength() > 1 || to[0] >= BitSet.SINGLE_BYTE_SIZE) {
                        cc.addCodeRange(env, to[0], to[0]);
                    } else {
                        /* /(?i:[^A-C])/.match("a") ==> fail. */
                        bs.set(to[0]);
                    }
                }
            } else {
                if (inCC) {
                    if (enc.minLength() > 1 || to[0] >= BitSet.SINGLE_BYTE_SIZE) {
                        if (cc.isNot()) cc.clearNotFlag(enc);
                        cc.addCodeRange(env, to[0], to[0]);
                    } else {
                        if (cc.isNot()) {
                            bs.clear(to[0]);
                        } else {
                            bs.set(to[0]);
                        }
                    }
                }
            } // CASE_FOLD_IS_APPLIED_INSIDE_NEGATIVE_CCLASS
            
        } else {
            if (cc.isCodeInCC(enc, from) && (!Config.CASE_FOLD_IS_APPLIED_INSIDE_NEGATIVE_CCLASS || !cc.isNot())) {
                StringNode node = null;
                for (int i=0; i<length; i++) {
                    if (i == 0) {
                        node = new StringNode();
                        node.ensure(Config.ENC_CODE_TO_MBC_MAXLEN);
                        node.end += enc.codeToMbc(to[i], node.bytes, node.end);
                        
                        /* char-class expanded multi-char only
                        compare with string folded at match time. */
                        node.setAmbig();
                    } else {
                        node.ensure(Config.ENC_CODE_TO_MBC_MAXLEN);
                        node.end += enc.codeToMbc(to[i], node.bytes, node.end);
                    }
                }

                ConsAltNode alt = ConsAltNode.newAltNode(node, null);

                if (arg.tail == null) {
                    arg.altRoot = alt;
                } else {
                    arg.tail.setCdr(alt);
                }
                arg.tail = alt;
            }

        }

    }
    
    static final ApplyCaseFold INSTANCE = new ApplyCaseFold();   
}
