open module org.jruby.joni {
    exports org.joni;
    exports org.joni.constants;
    exports org.joni.exception;

    requires org.jruby.jcodings;
    requires org.objectweb.asm;
}