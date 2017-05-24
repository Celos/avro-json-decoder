package org.mihkel.avro.io;

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
}