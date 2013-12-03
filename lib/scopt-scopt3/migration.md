Migration from scopt 2.x
========================

Here's an example of scopt 2.x immutable parser:

```scala
case class Config(foo: Int = 0)
val parser = new scopt.immutable.OptionParser[Config]("scopt", "2.x") { def options = Seq(
  intOpt("f", "foo", "foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) },
  help(None, "version", "display this message"),
  arglist("<schema_file>...", "input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
) }
// parser.parse returns Option[C]
parser.parse(args, Config()) map { config =>
  println(config.foo)
} getOrElse {
  // arguments are bad, usage message will have been displayed
}
```

### step 0

The case class and `parser.parse` part stay the same. So we'll look at the parser part.

### step 1

Create `new scopt.OptionParser[Config]` instead of `immutable` or `mutable`.

### step 2

`def options = Seq()` is no longer needed.

```scala
val parser = new scopt.OptionParser[Config]("scopt", "2.x") {
  intOpt("f", "foo", "foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) }
  help(None, "version", "display this message")
  arglist("<schema_file>...", "input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 3

Move out version number into `head` along with the product name.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  intOpt("f", "foo", "foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) }
  help(None, "version", "display this message")
  arglist("<schema_file>...", "input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 4

Change `opt`, `intOpt`, `keyValueOpt` to `opt[String]`, `opt[Int]`, and `opt[(String, String)]` respectively.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo", "foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) }
  help(None, "version", "display this message")
  arglist("<schema_file>...", "input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 5

Change `shortOpt` to a `Char`, or omit the parameter instead of passing `None`.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo", "foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) }
  help("version", "display this message")
  arglist("<schema_file>...", "input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 6

Move descriptions out to `text` method.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo") text("foo is an integer property") { (v: Int, c: Config) =>
    c.copy(foo = v) }
  help("version") text("display this message")
  arglist("<schema_file>...") text("input schema to be converted") { (x: String, c: Config) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 7

Place callback closure in `action` method for immutable parsing. `foreach` for mutable parsing. Type annotations shouldn't be needed.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo") text("foo is an integer property") action { (x, c) =>
    c.copy(foo = x) }
  help("version") text("display this message")
  arglist("<schema_file>...") text("input schema to be converted") action { (x, c) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 8

There's now `version` method that prints out only the header text. Consider providing both `--help` and `--version`.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo") text("foo is an integer property") action { (x, c) =>
    c.copy(foo = x) }
  help("help") text("display this message")
  version("version") text("display version info")
  arglist("<schema_file>...") text("input schema to be converted") action { (x, c) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 9

Change both `arg` and `arglist` to `arg[String]`. Call `unbounded()` for `arglist`.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo") text("foo is an integer property") action { (x, c) =>
    c.copy(foo = x) }
  help("help") text("display this message")
  version("version") text("display version info")
  arg[String]("<schema_file>...") unbounded() text("input schema to be converted") action { (x, c) =>
    c.copy(files = c.files :+ (new File(x))) }
}
```

### step 10

scopt now supports `java.io.File`, `java.util.Calendar`, `java.net.URI`, etc.

```scala
val parser = new scopt.OptionParser[Config]("scopt") {
  head("Very scopt", "3.x")
  opt[Int]('f', "foo") text("foo is an integer property") action { (x, c) =>
    c.copy(foo = x) }
  help("help") text("display this message")
  version("version") text("display version info")
  arg[File]("<schema_file>...") unbounded() text("input schema to be converted") action { (x, c) =>
    c.copy(files = c.files :+ x) }
}
```
