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

public class JOniException extends RuntimeException{
    private static final long serialVersionUID = -6027192180014164667L;

    public JOniException(String message) {
        super(message);
    }

    // The stacktrace is not very useful and is very expensive,
    // for example for Regexp#to_s on a Regexp like /(?i:a)|(?i:b)/,
    // so it is omitted unless -Dorg.joni.enableStackTrace=true
    @Override
    public final Throwable fillInStackTrace() {
        if (Config.ENABLE_STACK_TRACE) {
            return super.fillInStackTrace();
        } else {
            return this;
        }
    }
}
