/*
 * Copyright 1997-2026 Optimatika (www.optimatika.se)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.netio;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class BasicJsonTest extends NetioTests {

    private static void assertInvalid(final String json) {
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parse(json));
    }

    @Test
    public void testParseArrayOfEachType() {

        List<?> array = BasicJson.parseArray(" [ \"text\" , 42 , 1.5 , true , false , null , { \"k\" : 1 } , [ 1 , 2 ] ] ");

        TestUtils.assertEquals(8, array.size());
        TestUtils.assertEquals("text", array.get(0));
        TestUtils.assertEquals(BigInteger.valueOf(42L), array.get(1));
        TestUtils.assertEquals(new BigDecimal("1.5"), array.get(2));
        TestUtils.assertEquals(Boolean.TRUE, array.get(3));
        TestUtils.assertEquals(Boolean.FALSE, array.get(4));
        TestUtils.assertNull(array.get(5));

        TestUtils.assertTrue(array.get(6) instanceof Map<?, ?>);
        TestUtils.assertEquals(BigInteger.ONE, ((Map<?, ?>) array.get(6)).get("k"));

        TestUtils.assertTrue(array.get(7) instanceof List<?>);
        TestUtils.assertEquals(2, ((List<?>) array.get(7)).size());
    }

    @Test
    public void testParseBoolean() {
        TestUtils.assertEquals(Boolean.TRUE, BasicJson.parseBoolean("true"));
        TestUtils.assertEquals(Boolean.FALSE, BasicJson.parseBoolean(" false "));
        TestUtils.assertNull(BasicJson.parseBoolean("null"));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseBoolean("\"true\""));
        BasicJsonTest.assertInvalid("tru");
        BasicJsonTest.assertInvalid("TRUE");
    }

    @Test
    public void testParseDeeplyNested() {

        String json = "{\"a\":[{\"b\":[[null,{\"c\":[1,\"two\",false]}]]}]}";

        Map<String, ?> level0 = BasicJson.parseObject(json);
        List<?> a = (List<?>) level0.get("a");
        Map<?, ?> level1 = (Map<?, ?>) a.get(0);
        List<?> b = (List<?>) level1.get("b");
        List<?> inner = (List<?>) b.get(0);

        TestUtils.assertNull(inner.get(0));

        Map<?, ?> level2 = (Map<?, ?>) inner.get(1);
        List<?> c = (List<?>) level2.get("c");

        TestUtils.assertEquals(BigInteger.ONE, c.get(0));
        TestUtils.assertEquals("two", c.get(1));
        TestUtils.assertEquals(Boolean.FALSE, c.get(2));

        TestUtils.assertEquals(json, BasicJson.write(level0));
    }

    @Test
    public void testParseEmptyContainers() {
        TestUtils.assertEquals(0, BasicJson.parseArray("[]").size());
        TestUtils.assertEquals(0, BasicJson.parseArray(" [ ] ").size());
        TestUtils.assertEquals(0, BasicJson.parseObject("{}").size());
        TestUtils.assertEquals(0, BasicJson.parseObject(" { } ").size());
    }

    @Test
    public void testParseNull() {
        TestUtils.assertNull(BasicJson.parseNull("null"));
        TestUtils.assertNull(BasicJson.parseNull(" null "));
        TestUtils.assertNull(BasicJson.parse(null));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseNull("0"));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseNull("\"null\""));
        BasicJsonTest.assertInvalid("nul");
    }

    @Test
    public void testParseNumber() {

        TestUtils.assertEquals(BigInteger.ZERO, BasicJson.parseNumber("0"));
        TestUtils.assertEquals(BigInteger.valueOf(-17L), BasicJson.parseNumber("-17"));
        TestUtils.assertEquals(BigInteger.valueOf(Long.MAX_VALUE), BasicJson.parseNumber("9223372036854775807"));
        TestUtils.assertEquals(new BigInteger("123456789012345678901234567890"), BasicJson.parseNumber("123456789012345678901234567890"));

        TestUtils.assertEquals(new BigDecimal("1.5"), BasicJson.parseNumber("1.5"));
        TestUtils.assertEquals(new BigDecimal("-0.125"), BasicJson.parseNumber("-0.125"));
        TestUtils.assertEquals(new BigDecimal("1e3"), BasicJson.parseNumber("1e3"));
        TestUtils.assertEquals(new BigDecimal("1.2E-5"), BasicJson.parseNumber("1.2E-5"));

        TestUtils.assertNull(BasicJson.parseNumber("null"));

        BasicJsonTest.assertInvalid("+1");
        BasicJsonTest.assertInvalid("-");
        BasicJsonTest.assertInvalid("1.");
        BasicJsonTest.assertInvalid(".5");
        BasicJsonTest.assertInvalid("1e");
        BasicJsonTest.assertInvalid("1e+");
        BasicJsonTest.assertInvalid("01");
        BasicJsonTest.assertInvalid("NaN");
    }

    /**
     * The point of parsing to {@link BigInteger} and {@link BigDecimal} - literals that {@code double} and
     * {@code long} cannot hold survive a round trip digit for digit.
     */
    @Test
    public void testParseNumberIsExact() {

        String[] literals = { "0.1", "9223372036854775808", "-9223372036854775809", "123456789012345678901234567890", "0.12345678901234567890123456789",
                "1.0000000000000002", "1E+400", "2.50" };

        for (String literal : literals) {
            TestUtils.assertEquals(literal, BasicJson.write(BasicJson.parseNumber(literal)));
        }

        TestUtils.assertEquals(new BigDecimal("0.1"), BasicJson.parseNumber("0.1"));
        TestUtils.assertNotEquals(BigDecimal.valueOf(0.1), BasicJson.parseNumber("0.10"));
    }

    @Test
    public void testParseObjectOfEachType() {

        Map<String, ?> object = BasicJson.parseObject(
                "{ \"string\" : \"text\" , \"int\" : 42 , \"double\" : 1.5 , \"true\" : true , \"false\" : false , \"null\" : null , \"object\" : { \"k\" : \"v\" } , \"array\" : [ 1 , null , [] ] }");

        TestUtils.assertEquals(8, object.size());
        TestUtils.assertEquals("text", object.get("string"));
        TestUtils.assertEquals(BigInteger.valueOf(42L), object.get("int"));
        TestUtils.assertEquals(new BigDecimal("1.5"), object.get("double"));
        TestUtils.assertEquals(Boolean.TRUE, object.get("true"));
        TestUtils.assertEquals(Boolean.FALSE, object.get("false"));
        TestUtils.assertNull(object.get("null"));
        TestUtils.assertTrue(object.containsKey("null"));

        TestUtils.assertEquals("v", ((Map<?, ?>) object.get("object")).get("k"));

        List<?> array = (List<?>) object.get("array");
        TestUtils.assertEquals(BigInteger.ONE, array.get(0));
        TestUtils.assertNull(array.get(1));
        TestUtils.assertEquals(0, ((List<?>) array.get(2)).size());

        TestUtils.assertEquals("string", new ArrayList<>(object.keySet()).get(0));
        TestUtils.assertEquals("array", new ArrayList<>(object.keySet()).get(7));
    }

    @Test
    public void testParseString() {

        TestUtils.assertEquals("", BasicJson.parseString("\"\""));
        TestUtils.assertEquals("text", BasicJson.parseString(" \"text\" "));
        TestUtils.assertEquals("\"\\/\b\f\n\r\t", BasicJson.parseString("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\""));
        TestUtils.assertEquals("A\u00e5", BasicJson.parseString("\"\\u0041\\u00e5\""));
        TestUtils.assertEquals("\uD83D\uDE00", BasicJson.parseString("\"\\ud83d\\ude00\""));
        TestUtils.assertEquals("a:b", BasicJson.parseString("\"a\\u003ab\""));

        TestUtils.assertNull(BasicJson.parseString("null"));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseString("42"));
        BasicJsonTest.assertInvalid("\"unterminated");
        BasicJsonTest.assertInvalid("\"\\q\"");
        BasicJsonTest.assertInvalid("\"\\u00g0\"");
        BasicJsonTest.assertInvalid("\"\\u00\"");
        BasicJsonTest.assertInvalid("\"raw\tcontrol\"");
    }

    @Test
    public void testParseTypeMismatch() {
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseObject("[]"));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseArray("{}"));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseNumber("true"));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.parseBoolean("1"));
    }

    @Test
    public void testRejectMalformed() {

        BasicJsonTest.assertInvalid("");
        BasicJsonTest.assertInvalid("   ");
        BasicJsonTest.assertInvalid("{");
        BasicJsonTest.assertInvalid("}");
        BasicJsonTest.assertInvalid("[");
        BasicJsonTest.assertInvalid("[1,]");
        BasicJsonTest.assertInvalid("[1 2]");
        BasicJsonTest.assertInvalid("{\"a\":1,}");
        BasicJsonTest.assertInvalid("{\"a\" 1}");
        BasicJsonTest.assertInvalid("{a:1}");
        BasicJsonTest.assertInvalid("{'a':1}");
        BasicJsonTest.assertInvalid("{\"a\":}");
        BasicJsonTest.assertInvalid("[1,2] [3]");
        BasicJsonTest.assertInvalid("{} junk");
        BasicJsonTest.assertInvalid("[1,2]]");
    }

    @Test
    public void testWriteArrays() {

        TestUtils.assertEquals("[1,2,3]", BasicJson.write(new int[] { 1, 2, 3 }));
        TestUtils.assertEquals("[1,2,3]", BasicJson.write(new long[] { 1L, 2L, 3L }));
        TestUtils.assertEquals("[1.0,2.5]", BasicJson.write(new double[] { 1.0, 2.5 }));
        TestUtils.assertEquals("[1.0,2.5]", BasicJson.write(new float[] { 1.0F, 2.5F }));
        TestUtils.assertEquals("[1,2]", BasicJson.write(new short[] { 1, 2 }));
        TestUtils.assertEquals("[1,2]", BasicJson.write(new byte[] { 1, 2 }));
        TestUtils.assertEquals("[true,false]", BasicJson.write(new boolean[] { true, false }));
        TestUtils.assertEquals("[\"a\",\"b\"]", BasicJson.write(new String[] { "a", "b" }));
        TestUtils.assertEquals("[1,\"a\",true,null]", BasicJson.write(new Object[] { Integer.valueOf(1), "a", Boolean.TRUE, null }));
        TestUtils.assertEquals("[]", BasicJson.write(new int[0]));

        TestUtils.assertEquals("[[1,2],[3]]", BasicJson.write(new int[][] { { 1, 2 }, { 3 } }));
    }

    @Test
    public void testWriteArraysMixedWithContainers() {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("primitives", new double[] { 1.5, 2.5 });
        map.put("nested", new Object[] { new int[] { 1 }, Arrays.asList("a", "b") });
        map.put("list", Arrays.asList(new int[] { 7 }, "x"));

        String json = BasicJson.write(map);

        TestUtils.assertEquals("{\"primitives\":[1.5,2.5],\"nested\":[[1],[\"a\",\"b\"]],\"list\":[[7],\"x\"]}", json);

        Map<String, ?> reparsed = BasicJson.parseObject(json);
        TestUtils.assertEquals(new BigDecimal("1.5"), ((List<?>) reparsed.get("primitives")).get(0));
        TestUtils.assertEquals(BigInteger.valueOf(7L), ((List<?>) ((List<?>) reparsed.get("list")).get(0)).get(0));
    }

    @Test
    public void testWriteLargeNumbers() {

        TestUtils.assertEquals("1.0E20", BasicJson.write(Double.valueOf(1E20)));
        TestUtils.assertEquals("3.0", BasicJson.write(Double.valueOf(3.0)));
        TestUtils.assertEquals("-3.0", BasicJson.write(Double.valueOf(-3.0)));
        TestUtils.assertEquals("0.001", BasicJson.write(Double.valueOf(0.001)));
        TestUtils.assertEquals("2.5", BasicJson.write(Float.valueOf(2.5F)));

        TestUtils.assertEquals(String.valueOf(Long.MAX_VALUE), BasicJson.write(Long.valueOf(Long.MAX_VALUE)));
        TestUtils.assertEquals(BigInteger.valueOf(Long.MAX_VALUE), BasicJson.parseNumber(BasicJson.write(Long.valueOf(Long.MAX_VALUE))));

        BigInteger huge = BigInteger.TEN.pow(400);
        TestUtils.assertEquals(huge, BasicJson.parseNumber(BasicJson.write(huge)));

        TestUtils.assertEquals("2.50", BasicJson.write(new BigDecimal("2.50")));
    }

    /**
     * Whatever a {@link Number} prints as is what gets written, so the value read back is the value written.
     */
    @Test
    public void testWriteNumbersUnconverted() {

        Number[] numbers = { Double.valueOf(1E20), Double.valueOf(3.0), Double.valueOf(0.1), Double.valueOf(-0.0), Double.valueOf(Double.MIN_VALUE),
                Double.valueOf(Double.MAX_VALUE), Float.valueOf(1.0F), Long.valueOf(Long.MAX_VALUE), Long.valueOf(Long.MIN_VALUE), Integer.valueOf(-7),
                Byte.valueOf((byte) 3), Short.valueOf((short) 3), BigInteger.TEN.pow(400), new BigDecimal("2.50") };

        for (Number number : numbers) {
            String written = BasicJson.write(number);
            TestUtils.assertEquals(String.valueOf(number), written);
            TestUtils.assertEquals(number.doubleValue(), BasicJson.parseNumber(written).doubleValue());
        }
    }

    @Test
    public void testWriteRejectsNonFinite() {

        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Double.valueOf(Double.NaN)));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Double.valueOf(Double.POSITIVE_INFINITY)));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Double.valueOf(Double.NEGATIVE_INFINITY)));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Float.valueOf(Float.NaN)));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(new double[] { 1.0, Double.NaN }));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Arrays.asList(Double.valueOf(1.0), Double.valueOf(Double.NaN))));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> BasicJson.write(Collections.singletonMap("x", Double.valueOf(Double.NaN))));
    }

    @Test
    public void testWriteThenParse() {

        String json = BasicJson.write(BasicJson.parse("{\"a\":[1,2.5,\"x\\ny\",true,null,{}],\"b\":{}}"));

        Map<String, ?> reparsed = BasicJson.parseObject(json);
        List<?> a = (List<?>) reparsed.get("a");

        TestUtils.assertEquals(BigInteger.ONE, a.get(0));
        TestUtils.assertEquals(new BigDecimal("2.5"), a.get(1));
        TestUtils.assertEquals("x\ny", a.get(2));
        TestUtils.assertEquals(Boolean.TRUE, a.get(3));
        TestUtils.assertNull(a.get(4));
        TestUtils.assertEquals(0, ((Map<?, ?>) a.get(5)).size());
        TestUtils.assertEquals(0, ((Map<?, ?>) reparsed.get("b")).size());
    }
}
