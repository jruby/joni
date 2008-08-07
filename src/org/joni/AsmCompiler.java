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

import org.joni.ast.AnchorNode;
import org.joni.ast.BackRefNode;
import org.joni.ast.CClassNode;
import org.joni.ast.CTypeNode;
import org.joni.ast.CallNode;
import org.joni.ast.ConsAltNode;
import org.joni.ast.EncloseNode;
import org.joni.ast.QuantifierNode;

final class AsmCompiler extends AsmCompilerSupport {

    public AsmCompiler(Analyser analyser) {
        super(analyser);
    }
    
    @Override
    protected void prepare() {
        REG_NUM++;
        prepareMachine();
        prepareMachineInit();
        prepareMachineMatch();

        prepareFactory();
        prepareFactoryInit();
    }
    
    @Override
    protected void finish() {
        setupFactoryInit();

        setupMachineInit();
        setupMachineMatch();

        setupClasses();
    }

    @Override
    protected void compileAltNode(ConsAltNode node) {
    }

    @Override
    protected void addCompileString(byte[]bytes, int p, int mbLength, int strLength, boolean ignoreCase) {
        String template = installTemplate(bytes, p, strLength);
    }

    @Override
    protected void compileCClassNode(CClassNode node) {
        if (node.bs != null) {
            String bitsetName = installBitSet(node.bs.bits);
        }
    }

    @Override
    protected void compileCTypeNode(CTypeNode node) {
    }

    @Override
    protected void compileAnyCharNode() {
    }

    @Override
    protected void compileBackrefNode(BackRefNode node) {
    }

    @Override
    protected void compileCallNode(CallNode node) {
    }

    @Override
    protected void compileCECQuantifierNode(QuantifierNode node) {
    }

    @Override
    protected void compileNonCECQuantifierNode(QuantifierNode node) {
    }

    @Override
    protected void compileOptionNode(EncloseNode node) {
    }

    @Override
    protected void compileEncloseNode(EncloseNode node) {
    }

    @Override
    protected void compileAnchorNode(AnchorNode node) {
    }
}
