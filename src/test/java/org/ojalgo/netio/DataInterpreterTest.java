package org.ojalgo.netio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class DataInterpreterTest extends NetioTests {

    private static void doSequenceTest(final DataInterpreter<String> interpreter) throws IOException {

        InMemoryFile file = new InMemoryFile();

        try (DataWriter<String> writer = new DataWriter<>(file.newOutputStream(), interpreter)) {
            writer.write("apete");
            writer.write("abc");
            writer.write("123");
        }

        try (DataReader<String> reader = new DataReader<>(file.newInputStream(), interpreter)) {
            TestUtils.assertEquals("apete", reader.read());
            TestUtils.assertEquals("abc", reader.read());
            TestUtils.assertEquals("123", reader.read());
        }
    }

    private static void doSingleTest(final DataInterpreter<String> interpreter) throws IOException {

        String data = "Hello, World!";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        interpreter.serialize(data, dos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        String result = interpreter.deserialize(dis);

        TestUtils.assertEquals(data, result);
    }

    @Test
    public void testBytesInterpreter() throws IOException {

        byte[] data = { 1, 2, 3, 4, 5 };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        DataInterpreter.BYTES.serialize(data, dos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        byte[] result = DataInterpreter.BYTES.deserialize(dis);

        TestUtils.assertArrayEquals(data, result);
    }

    @Test
    public void testStringBytesInterpreter() throws IOException {
        DataInterpreterTest.doSingleTest(DataInterpreter.STRING_BYTES);
    }

    @Test
    public void testStringCharsInterpreter() throws IOException {
        DataInterpreterTest.doSingleTest(DataInterpreter.STRING_CHARS);
    }

    @Test
    public void testStringInterpreters() throws IOException {

        DataInterpreterTest.doSequenceTest(DataInterpreter.STRING_UTF);

        DataInterpreterTest.doSequenceTest(DataInterpreter.STRING_CHARS);

        DataInterpreterTest.doSequenceTest(DataInterpreter.STRING_BYTES);
    }

    @Test
    public void testStringUtfInterpreter() throws IOException {
        DataInterpreterTest.doSingleTest(DataInterpreter.STRING_UTF);
    }

}
