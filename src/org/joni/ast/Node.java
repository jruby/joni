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
package org.joni.ast;

import org.joni.constants.internal.NodeType;

public abstract class Node implements NodeType {
    public Node parent;
    protected int type;

    Node(int type) {
        this.type = type;
    }

    public final int getType() {
        return type;
    }

    public final int getType2Bit() {
        return 1 << getType();
    }

    protected void setChild(Node tgt){
        // default definition
    }

    protected Node getChild(){
        // default definition
        return null;
    };

    public void replaceWith(Node with) {
        with.parent = parent;
        parent.setChild(with);
        parent = null;
    }

    public abstract String getName();
    protected abstract String toString(int level);

    public String getAddressName() {
        return getName() + ":0x" + Integer.toHexString(System.identityHashCode(this));
    }

    public final String toString() {
        StringBuilder s = new StringBuilder();
        s.append("<" + getAddressName() + " (" + (parent == null ? "NULL" : parent.getAddressName())  + ")>");
        return s + toString(0);
    }

    protected static String pad(Object value, int level) {
        if (value == null) return "NULL";

        StringBuilder pad = new StringBuilder("  ");
        for (int i=0; i<level; i++) pad.append(pad);

        return value.toString().replace("\n",  "\n" + pad);
    }

    public final boolean isSimple() {
        return (getType2Bit() & SIMPLE) != 0;
    }

    public static TopNode newTop(Node root) {
        return new TopNode(root);
    }

    public static final class TopNode extends Node {
        private Node root;

        TopNode(Node root) {
            super(-1);
            root.parent = this;
            setChild(root);
        }

        public Node getRoot() {
            return root;
        }

        @Override
        public void setChild(Node node) {
            node.parent = this;
            root = node;
        }

        @Override
        public Node getChild() {
            return root;
        }

        @Override
        public String getName() {
            return "ROOT";
        }

        @Override
        public String toString(int level) {
            return "\n" + pad(root, level + 1);
        }
    }
}
