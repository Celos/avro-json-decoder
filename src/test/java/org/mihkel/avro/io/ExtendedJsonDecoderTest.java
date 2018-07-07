package org.mihkel.avro.io;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class ExtendedJsonDecoderTest extends TestCase {
	@Test public void testInt() throws Exception {
		checkNumeric("int", 1);
	}
	
	@Test public void testLong() throws Exception {
		checkNumeric("long", 1L);
	}
	
	@Test public void testFloat() throws Exception {
		checkNumeric("float", 1.0F);
	}
	
	@Test public void testDouble() throws Exception {
		checkNumeric("double", 1.0);
	}
	
	private void checkNumeric(String type, Object value) throws Exception {
		String def =
				"{\"type\":\"record\",\"name\":\"X\",\"fields\":"
						+"[{\"type\":\""+type+"\",\"name\":\"n\"}]}";
		Schema schema = Schema.parse(def);
		DatumReader<GenericRecord> reader =
				new GenericDatumReader<GenericRecord>(schema);
		
		String[] records = {"{\"n\":1}", "{\"n\":1.0}"};
		
		for (String record : records) {
			Decoder decoder = new ExtendedJsonDecoder(schema, record);
			GenericRecord r = reader.read(null, decoder);
			Assert.assertEquals(value, r.get("n"));
		}
	}
	
	// Ensure that even if the order of fields in JSON is different from the order in schema,
	// it works.
	@Test public void testReorderFields() throws Exception {
		String w =
				"{\"type\":\"record\",\"name\":\"R\",\"fields\":"
						+"[{\"type\":\"long\",\"name\":\"l\"},"
						+"{\"type\":{\"type\":\"array\",\"items\":\"int\"},\"name\":\"a\"}"
						+"]}";
		Schema ws = Schema.parse(w);
		String data = "{\"a\":[1,2],\"l\":100}{\"l\": 200, \"a\":[1,2]}";
		ExtendedJsonDecoder in = new ExtendedJsonDecoder(ws, data);
		Assert.assertEquals(100, in.readLong());
		in.skipArray();
		Assert.assertEquals(200, in.readLong());
		in.skipArray();
	}
	
	@Test
	public void testNullsAreInferred() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"R\",\"fields\":[{\"type\":[\"null\",\"long\"],\"name\":\"a\",\"default\":null}]}";
		GenericRecord record = readRecord(w, "{}");
		
		Assert.assertNull(record.get("a"));
	}
	
	@Test
	public void testDefaultValuesAreInferred() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"R\",\"fields\":[{\"type\":\"long\",\"name\":\"a\",\"default\":7}]}";
		GenericRecord record = readRecord(w, "{}");
		
		Assert.assertEquals(7L, record.get("a"));
	}
	
	@Test
	public void testNestedNullsAreInferred() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"R\",\"fields\":[{\"name\":\"S\",\"type\":" +
				"{\"type\":\"record\",\"name\":\"S\",\"fields\":[{\"type\":[\"null\",\"long\"],\"name\":\"a\",\"default\":null},{\"type\":\"long\",\"name\":\"b\"}]}}]}";
		String data = "{\"S\": {\"b\":1}}";
		GenericRecord record = ((GenericRecord)readRecord(w, data).get("S"));
		Assert.assertNull(record.get("a"));
	}
	
	@Test
	public void testArraysCanBeNull() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"R\",\"fields\":[{\"type\":[\"null\",{\"type\":\"array\",\"items\":\"long\"}],\"name\":\"A\",\"default\":null}]}";
		String data = "{}";
		GenericRecord record = readRecord(w, data);
		Assert.assertNull(record.get("A"));
	}
	
	@Test
	public void testRecordCanBeNull() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"R\",\"namespace\":\"com.playtech.bex.massupdate.api\",\"fields\":" +
				"[{\"name\":\"S\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"S\",\"fields\":[{\"name\":\"A\",\"type\":\"long\"}]}],\"default\":null}]}";
		String data = "{}";
		GenericRecord record = readRecord(w, data);
		Assert.assertNull(record.get("S"));
	}
	
	@Test
	public void testWtf() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"wrapper\",\"fields\":[{\"name\":\"data\",\"type\":" +
				"{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"r1\",\"fields\":" +
				"[{\"name\":\"r1\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"sr2\",\"fields\":" +
				"[{\"name\":\"sr2\",\"type\":\"string\"}]}}},{\"name\":\"r2\",\"type\":{\"type\":\"array\",\"items\":" +
				"{\"type\":\"record\",\"name\":\"r2\",\"fields\":[{\"name\":\"notfound1\",\"type\":[\"null\"," +
				"{\"type\":\"array\",\"items\":\"string\"}],\"default\":null},{\"name\":\"notfound2\",\"type\":" +
				"[\"null\",{\"type\":\"array\",\"items\":\"string\"}],\"default\":null}]}}}]}}}]}";
		String data = "{\"data\":[{\"r1\":[],\"r2\":[{\"notfound1\":{\"array\":[\"val1\",\"val2\"]}}]}]}";
		GenericRecord record = readRecord(w, data);
		Assert.assertNull(record.get("S"));
	}

	@Test
	public void testNestedOptionals() throws IOException {
		String w = "{\"type\":\"record\",\"name\":\"rootType\",\"fields\":[{\"name\":\"outer\",\"type\":" +
				"[\"null\",{\"type\":\"record\",\"name\":\"outerType\",\"fields\":[{\"name\":\"innerField\",\"type\":[\"null\",\"string\"],\"default\":null}]}" +
				"],\"default\":null}]}";
		String data = "{\"outer\":{\"outerType\":{}}}";
		GenericRecord record = readRecord(w, data);
		Assert.assertNull(record.get("S"));
	}

	public GenericRecord readRecord(String schemaString, String jsonData) throws IOException {
		Schema schema = Schema.parse(schemaString);
		Decoder decoder = new ExtendedJsonDecoder(schema, jsonData);
		DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
		return datumReader.read(null, decoder);
	}
}