# avro-json-decoder
JSON decoder for AVRO that infers default values. Based on org.apache.avro.io.JsonDecoder from <a href="https://github.com/apache/avro">AVRO</a> 1.8.2 and org.apache.avro.io.ExtendedJsonDecoder by <a href="https://github.com/zolyfarkas/avro">zolyfarkas</a>.

## why

Given this schema (in AVRO IDL)

```
record User {
  string username;
  union {null, string} name = null;
}
```
this record is valid and will be decoded properly
```json
{"username":"user1","name":null}
```
whereas this record
```json
{"username":"user1"}
```
will produce something like
```
org.apache.avro.AvroTypeException: Expected field name not found: name
	at org.apache.avro.io.JsonDecoder.doAction(JsonDecoder.java:495)
	at org.apache.avro.io.parsing.Parser.advance(Parser.java:88)
	at org.apache.avro.io.JsonDecoder.advance(JsonDecoder.java:157)
	at org.apache.avro.io.JsonDecoder.readIndex(JsonDecoder.java:447)
	...
```
or
```
org.apache.avro.AvroTypeException: Expected start-union. Got END_OBJECT
	at org.apache.avro.io.JsonDecoder.error(JsonDecoder.java:698)
	at org.apache.avro.io.JsonDecoder.readIndex(JsonDecoder.java:441)
	at org.apache.avro.io.ResolvingDecoder.doAction(ResolvingDecoder.java:290)
	at org.apache.avro.io.parsing.Parser.advance(Parser.java:88)
        ...
```

The decoder allows decoding JSON that doesn't specify optional values, provided they have defaults.

## how

Replace

```java
Decoder decoder = DecoderFactory.get().jsonDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```
with
```java
Decoder decoder = new ExtendedJsonDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```
and pass it to your reader, as usual:
```java
SpecificDatumReader<T> reader = new SpecificDatumReader<>(SCHEMA_OR_CLASS);
reader.read(null, decoder);
```
