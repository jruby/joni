joni
====

[![Maven Central](https://img.shields.io/maven-central/v/org.jruby.joni/joni)](https://central.sonatype.com/artifact/org.jruby.joni/joni)
[![Build Status](https://github.com/jruby/joni/actions/workflows/maven.yml/badge.svg)](https://github.com/jruby/joni/actions/workflows/maven.yml)

Java port of [Oniguruma](https://github.com/kkos/oniguruma) regexp library

## Usage

### Imports

```java
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
```

### Matching

```java
byte[] pattern = "a*".getBytes();
byte[] str = "aaa".getBytes();

Regex regex = new Regex(pattern, 0, pattern.length, Option.NONE, UTF8Encoding.INSTANCE);
Matcher matcher = regex.matcher(str);
int result = matcher.search(0, str.length, Option.DEFAULT);
```

### Using captures

```java
byte[] pattern = "(a*)".getBytes();
byte[] str = "aaa".getBytes();

Regex regex = new Regex(pattern, 0, pattern.length, Option.NONE, UTF8Encoding.INSTANCE);
Matcher matcher = regex.matcher(str);
int result = matcher.search(0, str.length, Option.DEFAULT);
if (result != -1) {
    Region region = matcher.getEagerRegion();
}
```

### Using named captures

```java
byte[] pattern = "(?<name>a*)".getBytes();
byte[] str = "aaa".getBytes();

Regex regex = new Regex(pattern, 0, pattern.length, Option.NONE, UTF8Encoding.INSTANCE);
Matcher matcher = regex.matcher(str);
int result = matcher.search(0, str.length, Option.DEFAULT);
if (result != -1) {
    Region region = matcher.getEagerRegion();
    for (Iterator<NameEntry> entry = regex.namedBackrefIterator(); entry.hasNext();) {
        NameEntry e = entry.next();
        int number = e.getBackRefs()[0]; // can have many refs per name
        // int begin = region.beg[number];
        // int end = region.end[number];
    }
}
```

## License

Joni is released under the [MIT License](http://www.opensource.org/licenses/MIT).
