open module org.jruby.joni {
    exports org.joni;
    exports org.joni.constants;
    exports org.joni.exception;

    requires transitive org.jruby.jcodings;
}