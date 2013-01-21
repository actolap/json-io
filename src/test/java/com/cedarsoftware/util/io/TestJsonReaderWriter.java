package com.cedarsoftware.util.io;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.cedarsoftware.util.SafeSimpleDateFormat;

/**
 * Test cases for JsonReader / JsonWriter
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) John DeRegnaucourt
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License. */
public class TestJsonReaderWriter extends TestCase
{
    public static boolean _debug = false;
    public static Date _testDate = new Date();
    public static Character _CONST_CHAR = new Character('j');
    public static Byte _CONST_BYTE = new Byte((byte) 16);
    public static Short _CONST_SHORT = new Short((short) 26);
    public static Integer _CONST_INT = new Integer(36);
    public static Long _CONST_LONG = new Long(46);
    public static Float _CONST_FLOAT = new Float(56.56);
    public static Double _CONST_DOUBLE = new Double(66.66);
    private long _startWrite;
    private long _endWrite;
    private long _startRead;
    private long _endRead;

    public static class TestObject implements Comparable, Serializable
    {
        protected String _name;
        private TestObject _other;

        public TestObject(String name)
        {
            _name = name;
        }

        public int compareTo(Object that)
        {
            if (!(that instanceof TestObject))
            {
                return 1;
            }
            return _name.compareTo(((TestObject) that)._name);
        }

        public int hashCode()
        {
            return _name == null ? 0 : _name.hashCode();
        }

        public boolean equals(Object that)
        {
            if (that == null)
            {
                return false;
            }
            return that instanceof TestObject && _name.equals(((TestObject) that)._name);
        }

        public String toString()
        {
            return "name=" + _name;
        }

        public String getName()
        {
            return _name;
        }
    }

    private static class TestObjectKid extends TestObject implements Serializable
    {
        private String _email;

        TestObjectKid(String name, String email)
        {
            super(name);
            _email = email;
        }
    }

    private static class TestJsonNoDefaultOrPublicConstructor
    {
        private final String _str;
        private final Date _date;
        private final byte _byte;
        private final Byte _Byte;
        private final short _short;
        private final Short _Short;
        private final int _int;
        private final Integer _Integer;
        private final long _long;
        private final Long _Long;
        private final float _float;
        private final Float _Float;
        private final double _double;
        private final Double _Double;
        private final boolean _boolean;
        private final Boolean _Boolean;
        private final char _char;
        private final Character _Char;
        private final String[] _strings;
        private final int[] _ints;
        private final BigDecimal _bigD;

        private TestJsonNoDefaultOrPublicConstructor(String string, Date date, byte b, Byte B, short s, Short S, int i, Integer I,
                                                     long l, Long L, float f, Float F, double d, Double D, boolean bool, Boolean Bool,
                                                     char c, Character C, String[] strings, int[] ints, BigDecimal bigD)
        {
            _str = string;
            _date = date;
            _byte = b;
            _Byte = B;
            _short = s;
            _Short = S;
            _int = i;
            _Integer = I;
            _long = l;
            _Long = L;
            _float = f;
            _Float = F;
            _double = d;
            _Double = D;
            _boolean = bool;
            _Boolean = Bool;
            _char = c;
            _Char = C;
            _strings = strings;
            _ints = ints;
            _bigD = bigD;
        }

        public String getString()
        {
            return _str;
        }

        public Date getDate()
        {
            return _date;
        }

        public byte getByte()
        {
            return _byte;
        }

        public short getShort()
        {
            return _short;
        }

        public int getInt()
        {
            return _int;
        }

        public long getLong()
        {
            return _long;
        }

        public float getFloat()
        {
            return _float;
        }

        public double getDouble()
        {
            return _double;
        }

        public boolean getBoolean()
        {
            return _boolean;
        }

        public char getChar()
        {
            return _char;
        }

        public String[] getStrings()
        {
            return _strings;
        }

        public int[] getInts()
        {
            return _ints;
        }
    }

    private static class Empty implements Serializable
    {
        public static double multiple(double x, double y)
        {
            return x * y;
        }

        public boolean equals(Object other)
        {
            return other != null && other instanceof Empty;
        }
    }

    public static class TestArray implements Serializable
    {
        private Empty _empty_a;
        private Empty _empty_b;
        private Empty[] _empty_c;
        private Empty[] _empty_d;
        private Empty[][] _empty_e;
        private Empty[][] _empty_f;

        private boolean[] _booleans_a;
        private boolean[] _booleans_b;
        private boolean[] _booleans_c;
        private Boolean[] _booleans_d;
        private Boolean[] _booleans_e;
        private Boolean[] _booleans_f;
        private Boolean[] _booleans_g;
        private boolean[][] _booleans_h;
        private boolean[][] _booleans_i;
        private boolean[][] _booleans_j;

        private char[] _chars_a;
        private char[] _chars_b;
        private char[] _chars_c;
        private Character[] _chars_d;
        private Character[] _chars_e;
        private Character[] _chars_f;
        private char[][] _chars_g;
        private Character[][] _chars_h;

        private byte[] _bytes_a;
        private byte[] _bytes_b;
        private byte[] _bytes_c;
        private Byte[] _bytes_d;
        private Byte[] _bytes_e;
        private Byte[] _bytes_f;
        private byte[][] _bytes_g;
        private Byte[][] _bytes_h;
        private byte[] _bytes_i;

        private short[] _shorts_a;
        private short[] _shorts_b;
        private short[] _shorts_c;
        private Short[] _shorts_d;
        private Short[] _shorts_e;
        private Short[] _shorts_f;
        private short[][] _shorts_g;
        private Short[][] _shorts_h;

        private int[] _ints_a;
        private int[] _ints_b;
        private int[] _ints_c;
        private int[][] _int_1;
        private Integer[] _ints_d;
        private Integer[] _ints_e;
        private Integer[] _ints_f;
        private Integer[][] _ints_g;

        private long[] _longs_a;
        private long[] _longs_b;
        private long[] _longs_c;
        private long[][][] _longs_1;
        private Long[] _longs_d;
        private Long[] _longs_e;
        private Long[] _longs_f;
        private Long[][] _longs_g;

        private float[] _floats_a;
        private float[] _floats_b;
        private float[] _floats_c;
        private Float[] _floats_d;
        private Float[] _floats_e;
        private Float[] _floats_f;
        private float[][] _floats_g;
        private Float[][] _floats_h;

        private double[] _doubles_a;
        private double[] _doubles_b;
        private double[] _doubles_c;
        private Double[] _doubles_d;
        private Double[] _doubles_e;
        private Double[] _doubles_f;
        private double[][] _doubles_g;
        private Double[][] _doubles_h;

        private String[] _strings_a;
        private String[][] _strings_b;

        private Date[] _dates_a;
        private Date[][] _dates_b;

        private Class[] _classes_a;
        private Class[][] _classes_b;

        private StringBuffer _stringbuffer_a;
        private StringBuffer[] _stringbuffer_b;
        private StringBuffer[][] _stringbuffer_c;

        private Object _oStringBuffer_a;
        private Object [] _oStringBuffer_b;

        private TestObject _testobj_a;
        private TestObject[] _testobj_b;
        private TestObject[][] _testobj_c;

        private Object[] _test_a;
        private Object[] _test_b;
        private Object[] _hetero_a;
        private Object[] _testRefs0;
        private Object[] _testRefs1;

        private BigInteger[] _bigInts;
        private Object[] _oBigInts;
        private BigDecimal[] _bigDecs;
        private Object[] _oBigDecs;

        private Object _arrayO;
        private Object _arrayS;
        private Object _arrayArrayO;

        public TestArray() { }

        public void init()
        {
            _empty_a = new Empty();
            _empty_b = null;
            _empty_c = new Empty[]{};
            _empty_d = new Empty[]{new Empty(), null};
            _empty_e = new Empty[][]{{}};
            _empty_f = new Empty[][]{{new Empty(), null}, null, {}, {new Empty()}};

            _booleans_a = new boolean[]{true, false, true};
            _booleans_b = new boolean[]{};
            _booleans_c = null;
            _booleans_d = new Boolean[]{Boolean.TRUE, Boolean.FALSE, null};
            _booleans_e = new Boolean[]{};
            _booleans_f = null;
            _booleans_g = new Boolean[]{null};
            _booleans_h = new boolean[][]{{true}, {true, false}, {true, false, true}, null, {}};
            _booleans_i = null;
            _booleans_j = new boolean[][]{null};

            _chars_a = new char[]{'a', '\t', '\u0005'};
            _chars_b = new char[]{};
            _chars_c = null;
            _chars_d = new Character[]{new Character('a'), new Character('\t'), new Character('\u0006')};
            _chars_e = new Character[]{};
            _chars_f = null;
            _chars_g = new char[][]{{'a', '\t', '\u0004'}, null, {}};
            _chars_h = new Character[][]{{new Character('a'), new Character('\t'), new Character('\u0004')}, null, {}};

            _bytes_a = new byte[]{Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE};
            _bytes_b = new byte[]{};
            _bytes_c = null;
            _bytes_d = new Byte[]{new Byte(Byte.MIN_VALUE), new Byte((byte) -1), new Byte((byte) 0), new Byte((byte) 1), new Byte(Byte.MAX_VALUE)};
            _bytes_e = new Byte[]{};
            _bytes_f = null;
            _bytes_g = new byte[][]{null, {}, {Byte.MAX_VALUE}};
            _bytes_h = new Byte[][]{null, {}, {new Byte(Byte.MAX_VALUE)}};
            _bytes_i = new byte[]{16};

            _shorts_a = new short[]{Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE};
            _shorts_b = new short[]{};
            _shorts_c = null;
            _shorts_d = new Short[]{new Short(Short.MIN_VALUE), new Short((short) -1), new Short((short) 0), new Short((short) 1), new Short(Short.MAX_VALUE)};
            _shorts_e = new Short[]{};
            _shorts_f = null;
            _shorts_g = new short[][]{null, {}, {Short.MAX_VALUE}};
            _shorts_h = new Short[][]{null, {}, {new Short(Short.MAX_VALUE)}};

            _ints_a = new int[]{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
            _ints_b = new int[]{};
            _ints_c = null;
            _int_1 = new int[][]{{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}, null, {-1, 0, 1, 2}};
            _ints_d = new Integer[]{new Integer(Integer.MIN_VALUE), new Integer(-1), new Integer(0), new Integer(1), new Integer(Integer.MAX_VALUE)};
            _ints_e = new Integer[]{};
            _ints_f = null;
            _ints_g = new Integer[][]{null, {}, {new Integer(Integer.MAX_VALUE)}};

            _longs_a = new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
            _longs_b = new long[]{};
            _longs_c = null;
            _longs_1 = new long[][][]{{{-1}, {0, 1}, {1, 2, 3}}, {}, {{1, 2}}, {{}}};
            _longs_d = new Long[]{new Long(Long.MIN_VALUE), new Long(-1), new Long(0), new Long(1), new Long(Long.MAX_VALUE)};
            _longs_e = new Long[]{};
            _longs_f = null;
            _longs_g = new Long[][]{null, {}, {new Long(Long.MAX_VALUE)}};

            _floats_a = new float[]{0.0f, Float.MIN_VALUE, Float.MAX_VALUE, -1.0f};
            _floats_b = new float[]{};
            _floats_c = null;
            _floats_d = new Float[]{new Float(0.0f), new Float(Float.MIN_VALUE), new Float(Float.MAX_VALUE), new Float(-1.0f), null};
            _floats_e = new Float[]{};
            _floats_f = null;
            _floats_g = new float[][]{null, {}, {Float.MAX_VALUE}};
            _floats_h = new Float[][]{null, {}, {new Float(Float.MAX_VALUE)}};

            _doubles_a = new double[]{0.0, Double.MIN_VALUE, Double.MAX_VALUE, -1.0};
            _doubles_b = new double[]{};
            _doubles_c = null;
            _doubles_d = new Double[]{new Double(0.0), new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE), new Double(-1.0), null};
            _doubles_e = new Double[]{};
            _doubles_f = null;
            _doubles_g = new double[][]{null, {}, {Double.MAX_VALUE}};
            _doubles_h = new Double[][]{null, {}, {new Double(Double.MAX_VALUE)}};

            _strings_a = new String[]{null, "\u0007", "\t\rfood\n\f", "null"};
            _strings_b = new String[][]{{"alpha", "bravo", "charlie"}, {null, "\u0007", "\t", "null"}, null, {}};

            _dates_a = new Date[]{new Date(0), _testDate, null};
            _dates_b = new Date[][]{null, {}, {_testDate}};

            _classes_a = new Class[]{boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, null, String.class};
            _classes_b = new Class[][]{null, {}, {Date.class}};

            _stringbuffer_a = new StringBuffer("food");
            _stringbuffer_b = new StringBuffer[3];
            _stringbuffer_b[0] = new StringBuffer("first");
            _stringbuffer_b[1] = new StringBuffer("second");
            _stringbuffer_b[2] = null;
            _stringbuffer_c = new StringBuffer[][]{null, {}, {new StringBuffer("sham-wow")}};
            _oStringBuffer_a = new StringBuffer("murder my days one at a time");
            _oStringBuffer_b = new Object[] {new StringBuffer("chaiyya chaiyya")};

            _testobj_a = new TestObject("food");
            _testobj_b = new TestObject[]{new TestObject("ten"), new TestObject("hut")};
            _testobj_c = new TestObject[][]{null, {}, {new TestObject("mighty-mend")}};

            _test_a = new Object[1];
            _test_b = new Object[1];
            _test_a[0] = _test_b;
            _test_b[0] = _test_a;

            _hetero_a = new Object[]{new Character('a'), Boolean.TRUE, new Byte((byte) 9), new Short((short) 9), new Integer(9), new Long(9), new Float(9.9), new Double(9.9), "getStartupInfo", _testDate, boolean.class, null, "null", _CONST_INT, Class.class};
            _testRefs0 = new Object[]{_testDate, Boolean.FALSE, _CONST_CHAR, _CONST_BYTE, _CONST_SHORT, _CONST_INT, _CONST_LONG, _CONST_FLOAT, _CONST_DOUBLE, "Happy"};
            _testRefs1 = new Object[]{_testDate, Boolean.FALSE, _CONST_CHAR, _CONST_BYTE, _CONST_SHORT, _CONST_INT, _CONST_LONG, _CONST_FLOAT, _CONST_DOUBLE, "Happy"};
            _arrayO = new Object[] { "foo", true, null, 16L, 3.14};
            _arrayS = new String[] {"fingers", "toes"};
            _arrayArrayO = new Object[][] {{"true", "false"}, {1L, 2L, 3L}, null, {1.1, 2.2}, {true, false}};

            _bigInts = new BigInteger[] { new BigInteger("-123456789012345678901234567890"), new BigInteger("0"), new BigInteger("123456789012345678901234567890")};
            _oBigInts = new Object[] { new BigInteger("-123456789012345678901234567890"), new BigInteger("0"), new BigInteger("123456789012345678901234567890")};
            _bigDecs = new BigDecimal[] { new BigDecimal("-123456789012345678901234567890.123456789012345678901234567890"), new BigDecimal("0.0"), new BigDecimal("123456789012345678901234567890.123456789012345678901234567890")};
            _oBigDecs = new Object[] { new BigDecimal("-123456789012345678901234567890.123456789012345678901234567890"), new BigDecimal("0.0"), new BigDecimal("123456789012345678901234567890.123456789012345678901234567890")};
        }
    }

    public void testArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testArray()");
        TestArray obj = new TestArray();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        TestArray root = (TestArray) readJsonObject(jsonOut);
        assertArray(root);
        time(root);
    }

    private void assertArray(TestArray root)
    {
        assertTrue(root._empty_a != null);
        assertNull(root._empty_b);
        assertTrue(root._empty_c.length == 0);
        assertTrue(root._empty_d.length == 2);
        assertTrue(root._empty_d[0].equals(new Empty()));
        assertNull(root._empty_d[1]);
        assertTrue(root._empty_e.length == 1);
        assertTrue(root._empty_e[0].length == 0);
        assertTrue(root._empty_f.length == 4);
        assertTrue(root._empty_f[0].length == 2);
        assertTrue(root._empty_f[0][0].equals(new Empty()));
        assertNull(root._empty_f[0][1]);
        assertNull(root._empty_f[1]);
        assertTrue(root._empty_f[2].length == 0);
        assertTrue(root._empty_f[3].length == 1);

        assertTrue(root._booleans_a.getClass().equals(boolean[].class));
        assertTrue(root._booleans_a.length == 3);
        assertTrue(root._booleans_a[0]);
        assertFalse(root._booleans_a[1]);
        assertTrue(root._booleans_a[2]);
        assertTrue(root._booleans_b.length == 0);
        assertNull(root._booleans_c);
        assertTrue(root._booleans_d.length == 3);
        assertTrue(root._booleans_d[0].booleanValue());
        assertFalse(root._booleans_d[1].booleanValue());
        assertNull(root._booleans_d[2]);
        assertTrue(root._booleans_e.length == 0);
        assertNull(root._booleans_f);
        assertTrue(root._booleans_g.length == 1);
        assertNull(root._booleans_g[0]);
        assertTrue(root._booleans_h.length == 5);
        assertTrue(root._booleans_h[0].length == 1);
        assertTrue(root._booleans_h[1].length == 2);
        assertTrue(root._booleans_h[2].length == 3);
        assertTrue(root._booleans_h[0][0]);
        assertTrue(root._booleans_h[1][0]);
        assertFalse(root._booleans_h[1][1]);
        assertTrue(root._booleans_h[2][0]);
        assertFalse(root._booleans_h[2][1]);
        assertTrue(root._booleans_h[2][2]);
        assertNull(root._booleans_h[3]);
        assertTrue(root._booleans_h[4].length == 0);
        assertNull(root._booleans_i);
        assertTrue(root._booleans_j.length == 1);
        assertNull(root._booleans_j[0]);

        assertTrue(root._chars_a[0] == 'a');
        assertTrue(root._chars_a[1] == '\t');
        assertTrue(root._chars_a[2] == '\u0005');
        assertTrue(root._chars_b.length == 0);
        assertNull(root._chars_c);
        assertTrue(root._chars_d[0].charValue() == 'a');
        assertTrue(root._chars_d[1].charValue() == '\t');
        assertTrue(root._chars_d[2].charValue() == '\u0006');
        assertTrue(root._chars_e.length == 0);
        assertNull(root._chars_f);

        assertTrue(root._bytes_a[0] == Byte.MIN_VALUE);
        assertTrue(root._bytes_a[1] == -1);
        assertTrue(root._bytes_a[2] == 0);
        assertTrue(root._bytes_a[3] == 1);
        assertTrue(root._bytes_a[4] == Byte.MAX_VALUE);
        assertTrue(root._bytes_b.length == 0);
        assertNull(root._bytes_c);
        assertTrue(root._bytes_d[0].byteValue() == Byte.MIN_VALUE);
        assertTrue(root._bytes_d[1].byteValue() == -1);
        assertTrue(root._bytes_d[2].byteValue() == 0);
        assertTrue(root._bytes_d[3].byteValue() == 1);
        assertTrue(root._bytes_d[4].byteValue() == Byte.MAX_VALUE);
        assertTrue(root._bytes_e.length == 0);
        assertNull(root._bytes_f);
        assertTrue(root._bytes_g.length == 3);
        assertNull(root._bytes_g[0]);
        assertTrue(root._bytes_g[1].length == 0);
        assertTrue(root._bytes_g[2].length == 1);
        assertTrue(root._bytes_g[2][0] == Byte.MAX_VALUE);
        assertTrue(root._bytes_h.length == 3);
        assertNull(root._bytes_h[0]);
        assertTrue(root._bytes_h[1].length == 0);
        assertTrue(root._bytes_h[2].length == 1);
        assertTrue(root._bytes_h[2][0].byteValue() == Byte.MAX_VALUE);
        assertTrue(root._bytes_i[0] == 16);

        assertTrue(root._chars_g.length == 3);
        assertTrue(root._chars_g[0].length == 3);
        assertTrue(root._chars_g[0][0] == 'a');
        assertTrue(root._chars_g[0][1] == '\t');
        assertTrue(root._chars_g[0][2] == '\u0004');
        assertNull(root._chars_g[1]);
        assertTrue(root._chars_g[2].length == 0);
        assertTrue(root._chars_h.length == 3);
        assertTrue(root._chars_h[0].length == 3);
        assertTrue(root._chars_h[0][0].equals(new Character('a')));
        assertTrue(root._chars_h[0][1].equals(new Character('\t')));
        assertTrue(root._chars_h[0][2].equals(new Character('\u0004')));
        assertNull(root._chars_h[1]);
        assertTrue(root._chars_h[2].length == 0);

        assertTrue(root._shorts_a[0] == Short.MIN_VALUE);
        assertTrue(root._shorts_a[1] == -1);
        assertTrue(root._shorts_a[2] == 0);
        assertTrue(root._shorts_a[3] == 1);
        assertTrue(root._shorts_a[4] == Short.MAX_VALUE);
        assertTrue(root._shorts_b.length == 0);
        assertNull(root._shorts_c);
        assertTrue(root._shorts_d[0].shortValue() == Short.MIN_VALUE);
        assertTrue(root._shorts_d[1].shortValue() == -1);
        assertTrue(root._shorts_d[2].shortValue() == 0);
        assertTrue(root._shorts_d[3].shortValue() == 1);
        assertTrue(root._shorts_d[4].shortValue() == Short.MAX_VALUE);
        assertTrue(root._shorts_e.length == 0);
        assertNull(root._shorts_f);
        assertTrue(root._shorts_g.length == 3);
        assertNull(root._shorts_g[0]);
        assertTrue(root._shorts_g[1].length == 0);
        assertTrue(root._shorts_g[2].length == 1);
        assertTrue(root._shorts_g[2][0] == Short.MAX_VALUE);
        assertTrue(root._shorts_h.length == 3);
        assertNull(root._shorts_h[0]);
        assertTrue(root._shorts_h[1].length == 0);
        assertTrue(root._shorts_h[2].length == 1);
        assertTrue(root._shorts_h[2][0].shortValue() == Short.MAX_VALUE);

        assertTrue(root._ints_a[0] == Integer.MIN_VALUE);
        assertTrue(root._ints_a[1] == -1);
        assertTrue(root._ints_a[2] == 0);
        assertTrue(root._ints_a[3] == 1);
        assertTrue(root._ints_a[4] == Integer.MAX_VALUE);
        assertTrue(root._ints_b.length == 0);
        assertNull(root._ints_c);
        assertTrue(root._ints_d[0].intValue() == Integer.MIN_VALUE);
        assertTrue(root._ints_d[1].intValue() == -1);
        assertTrue(root._ints_d[2].intValue() == 0);
        assertTrue(root._ints_d[3].intValue() == 1);
        assertTrue(root._ints_d[4].intValue() == Integer.MAX_VALUE);
        assertTrue(root._ints_e.length == 0);
        assertNull(root._ints_f);
        assertTrue(root._int_1.length == 3);
        assertTrue(root._int_1[0].length == 5);
        assertTrue(root._int_1[0][0] == Integer.MIN_VALUE);
        assertTrue(root._int_1[0][1] == -1);
        assertTrue(root._int_1[0][2] == 0);
        assertTrue(root._int_1[0][3] == 1);
        assertTrue(root._int_1[0][4] == Integer.MAX_VALUE);
        assertNull(root._int_1[1]);
        assertTrue(root._int_1[2].length == 4);
        assertTrue(root._int_1[2][0] == -1);
        assertTrue(root._int_1[2][1] == 0);
        assertTrue(root._int_1[2][2] == 1);
        assertTrue(root._int_1[2][3] == 2);
        assertTrue(root._ints_g.length == 3);
        assertNull(root._ints_g[0]);
        assertTrue(root._ints_g[1].length == 0);
        assertTrue(root._ints_g[2].length == 1);
        assertTrue(root._ints_g[2][0].intValue() == Integer.MAX_VALUE);

        assertTrue(root._longs_a[0] == Long.MIN_VALUE);
        assertTrue(root._longs_a[1] == -1);
        assertTrue(root._longs_a[2] == 0);
        assertTrue(root._longs_a[3] == 1);
        assertTrue(root._longs_a[4] == Long.MAX_VALUE);
        assertTrue(root._longs_b.length == 0);
        assertNull(root._longs_c);
        assertTrue(root._longs_d[0].longValue() == Long.MIN_VALUE);
        assertTrue(root._longs_d[1].longValue() == -1);
        assertTrue(root._longs_d[2].longValue() == 0);
        assertTrue(root._longs_d[3].longValue() == 1);
        assertTrue(root._longs_d[4].longValue() == Long.MAX_VALUE);
        assertTrue(root._longs_e.length == 0);
        assertNull(root._longs_f);
        assertTrue(root._longs_1.length == 4);
        assertTrue(root._longs_1[0].length == 3);
        assertTrue(root._longs_1[1].length == 0);
        assertTrue(root._longs_1[2].length == 1);
        assertTrue(root._longs_1[3].length == 1);
        assertTrue(root._ints_g.length == 3);
        assertNull(root._longs_g[0]);
        assertTrue(root._longs_g[1].length == 0);
        assertTrue(root._longs_g[2].length == 1);
        assertTrue(root._longs_g[2][0].longValue() == Long.MAX_VALUE);

        assertTrue(root._floats_a.length == 4);
        assertTrue(root._floats_a[0] == 0.0f);
        assertTrue(root._floats_a[1] == Float.MIN_VALUE);
        assertTrue(root._floats_a[2] == Float.MAX_VALUE);
        assertTrue(root._floats_a[3] == -1.0f);
        assertTrue(root._floats_b.length == 0);
        assertNull(root._floats_c);
        assertTrue(root._floats_d.length == 5);
        assertTrue(root._floats_d[0].equals(new Float(0.0f)));
        assertTrue(root._floats_d[1].equals(new Float(Float.MIN_VALUE)));
        assertTrue(root._floats_d[2].equals(new Float(Float.MAX_VALUE)));
        assertTrue(root._floats_d[3].equals(new Float(-1.0f)));
        assertNull(root._floats_d[4]);
        assertTrue(root._floats_e.length == 0);
        assertNull(root._floats_f);
        assertNull(root._floats_g[0]);
        assertTrue(root._floats_g[1].length == 0);
        assertTrue(root._floats_g[2].length == 1);
        assertTrue(root._floats_g[2][0] == Float.MAX_VALUE);
        assertNull(root._floats_h[0]);
        assertTrue(root._floats_h[1].length == 0);
        assertTrue(root._floats_h[2].length == 1);
        assertTrue(root._floats_h[2][0].floatValue() == Float.MAX_VALUE);

        assertTrue(root._doubles_a.length == 4);
        assertTrue(root._doubles_a[0] == 0.0);
        assertTrue(root._doubles_a[1] == Double.MIN_VALUE);
        assertTrue(root._doubles_a[2] == Double.MAX_VALUE);
        assertTrue(root._doubles_a[3] == -1.0);
        assertTrue(root._doubles_b.length == 0);
        assertNull(root._doubles_c);
        assertTrue(root._doubles_d.length == 5);
        assertTrue(root._doubles_d[0].equals(new Double(0.0)));
        assertTrue(root._doubles_d[1].equals(new Double(Double.MIN_VALUE)));
        assertTrue(root._doubles_d[2].equals(new Double(Double.MAX_VALUE)));
        assertTrue(root._doubles_d[3].equals(new Double(-1.0)));
        assertNull(root._doubles_d[4]);
        assertTrue(root._doubles_e.length == 0);
        assertNull(root._doubles_f);
        assertNull(root._doubles_g[0]);
        assertTrue(root._doubles_g[1].length == 0);
        assertTrue(root._doubles_g[2].length == 1);
        assertTrue(root._doubles_g[2][0] == Double.MAX_VALUE);
        assertNull(root._doubles_h[0]);
        assertTrue(root._doubles_h[1].length == 0);
        assertTrue(root._doubles_h[2].length == 1);
        assertTrue(root._doubles_h[2][0].doubleValue() == Double.MAX_VALUE);

        assertNull(root._strings_a[0]);
        assertTrue(root._strings_a[1].equals("\u0007"));
        assertTrue(root._strings_a[2].equals("\t\rfood\n\f"));
        assertTrue(root._strings_a[3].equals("null"));
        assertTrue(root._strings_b.length == 4);
        assertTrue(root._strings_b[0].length == 3);
        assertTrue(root._strings_b[0][0].equals("alpha"));
        assertTrue(root._strings_b[0][1].equals("bravo"));
        assertTrue(root._strings_b[0][2].equals("charlie"));
        assertTrue(root._strings_b[1].length == 4);
        assertNull(root._strings_b[1][0]);
        assertTrue(root._strings_b[1][1].equals("\u0007"));
        assertTrue(root._strings_b[1][2].equals("\t"));
        assertTrue(root._strings_b[1][3].equals("null"));
        assertNull(root._strings_b[2]);
        assertTrue(root._strings_b[3].length == 0);

        assertTrue(root._dates_a[0].equals(new Date(0)));
        assertTrue(root._dates_a[1].equals(_testDate));
        assertNull(root._dates_a[2]);
        assertNull(root._dates_b[0]);
        assertTrue(root._dates_b[1].length == 0);
        assertTrue(root._dates_b[2].length == 1);
        assertTrue(root._dates_b[2][0].equals(_testDate));

        assertTrue(root._classes_a.length == 10);
        assertTrue(root._classes_a[0].equals(boolean.class));
        assertTrue(root._classes_a[1].equals(char.class));
        assertTrue(root._classes_a[2].equals(byte.class));
        assertTrue(root._classes_a[3].equals(short.class));
        assertTrue(root._classes_a[4].equals(int.class));
        assertTrue(root._classes_a[5].equals(long.class));
        assertTrue(root._classes_a[6].equals(float.class));
        assertTrue(root._classes_a[7].equals(double.class));
        assertNull(root._classes_a[8]);
        assertTrue(root._classes_a[9].equals(String.class));
        assertNull(root._classes_b[0]);
        assertTrue(root._classes_b[1].length == 0);
        assertTrue(root._classes_b[2].length == 1);
        assertTrue(root._classes_b[2][0].equals(Date.class));

        assertTrue(root._stringbuffer_a.toString().equals("food"));
        assertTrue(root._stringbuffer_b.length == 3);
        assertTrue(root._stringbuffer_b[0].toString().equals("first"));
        assertTrue(root._stringbuffer_b[1].toString().equals("second"));
        assertNull(root._stringbuffer_b[2]);
        assertTrue(root._stringbuffer_c.length == 3);
        assertNull(root._stringbuffer_c[0]);
        assertTrue(root._stringbuffer_c[1].length == 0);
        assertTrue(root._stringbuffer_c[2].length == 1);
        assertTrue(root._stringbuffer_c[2][0].toString().equals("sham-wow"));

        assertTrue("murder my days one at a time".equals(root._oStringBuffer_a.toString()));
        assertTrue(root._oStringBuffer_b.length == 1);
        assertTrue("chaiyya chaiyya".equals(root._oStringBuffer_b[0].toString()));

        assertTrue(root._testobj_a.equals(new TestObject("food")));
        assertTrue(root._testobj_b.length == 2);
        assertTrue(root._testobj_b[0].getName().equals("ten"));
        assertTrue(root._testobj_b[1].getName().equals("hut"));
        assertTrue(root._testobj_c.length == 3);
        assertNull(root._testobj_c[0]);
        assertTrue(root._testobj_c[1].length == 0);
        assertTrue(root._testobj_c[2].length == 1);
        assertTrue(root._testobj_c[2][0].equals(new TestObject("mighty-mend")));

        assertTrue(root._test_a.length == 1);
        assertTrue(root._test_b.length == 1);
        assertTrue(root._test_a[0] == root._test_b);
        assertTrue(root._test_b[0] == root._test_a);

        assertTrue(root._hetero_a.length == 15);
        assertTrue(root._hetero_a[0].equals(new Character('a')));
        assertTrue(root._hetero_a[1].equals(Boolean.TRUE));
        assertTrue(root._hetero_a[2].equals(new Byte((byte) 9)));
        assertTrue(root._hetero_a[3].equals(new Short((short) 9)));
        assertTrue(root._hetero_a[4].equals(new Integer(9)));
        assertTrue(root._hetero_a[5].equals(new Long(9)));
        assertTrue(root._hetero_a[6].equals(new Float(9.9)));
        assertTrue(root._hetero_a[7].equals(new Double(9.9)));
        assertTrue(root._hetero_a[8].equals("getStartupInfo"));
        assertTrue(root._hetero_a[9].equals(_testDate));
        assertTrue(root._hetero_a[10].equals(boolean.class));
        assertNull(root._hetero_a[11]);
        assertTrue(root._hetero_a[12].equals("null"));
        assertTrue(root._hetero_a[13].equals(_CONST_INT));
        assertTrue(root._hetero_a[14].equals(Class.class));

        assertTrue(root._testRefs0.length == 10);
        assertTrue(root._testRefs0[0].equals(_testDate));
        assertTrue(root._testRefs0[1].equals(Boolean.FALSE));
        assertTrue(root._testRefs0[2].equals(_CONST_CHAR));
        assertTrue(root._testRefs0[3].equals(_CONST_BYTE));
        assertTrue(root._testRefs0[4].equals(_CONST_SHORT));
        assertTrue(root._testRefs0[5].equals(_CONST_INT));
        assertTrue(root._testRefs0[6].equals(_CONST_LONG));
        assertTrue(root._testRefs0[7].equals(_CONST_FLOAT));
        assertTrue(root._testRefs0[8].equals(_CONST_DOUBLE));
        assertTrue(root._testRefs0[9].equals("Happy"));

        assertTrue(root._testRefs1.length == 10);
        assertTrue(root._testRefs1[0] ==  root._testRefs0[0]);
        assertTrue(root._testRefs1[1] == root._testRefs0[1]);    // Works because we only read in Boolean.TRUE, Boolean.FALSE, or null
        assertTrue(root._testRefs1[2] == root._testRefs0[2]);
        assertTrue(root._testRefs1[3] == root._testRefs0[3]);
        assertTrue(root._testRefs1[4] == root._testRefs0[4]);
        assertTrue(root._testRefs1[5] == root._testRefs0[5]);
        assertTrue(root._testRefs1[6].equals(root._testRefs0[6]));
        assertTrue(root._testRefs1[7] != root._testRefs0[7]);       // Primitive Wrappers are treated like primitives
        assertTrue(root._testRefs1[8].equals(root._testRefs0[8]));
        assertTrue(root._testRefs1[9].equals(root._testRefs0[9]));

        assertTrue(root._arrayO instanceof Object[]);
        Object[] items = (Object[]) root._arrayO;
        assertTrue(items.length == 5);
        assertTrue("foo".equals(items[0]));
        assertTrue(Boolean.TRUE.equals(items[1]));
        assertNull(items[2]);
        assertTrue(((Long)16L).equals(items[3]));
        assertTrue(((Double)3.14).equals(items[4]));

        assertTrue(root._arrayS instanceof String[]);
        String[] strItems = (String[]) root._arrayS;
        assertTrue(strItems.length == 2);
        assertTrue("fingers".equals(strItems[0]));
        assertTrue("toes".equals(strItems[1]));

        assertTrue(root._arrayArrayO instanceof Object[]);
        assertTrue(root._arrayArrayO instanceof Object[][]);
        assertFalse(root._arrayArrayO instanceof Object[][][]);

        assertTrue(root._bigInts[0].equals(new BigInteger("-123456789012345678901234567890")));
        assertTrue(root._bigInts[1].equals(new BigInteger("0")));
        assertTrue(root._bigInts[2].equals(new BigInteger("123456789012345678901234567890")));

        assertTrue(root._oBigInts[0].equals(new BigInteger("-123456789012345678901234567890")));
        assertTrue(root._oBigInts[1].equals(new BigInteger("0")));
        assertTrue(root._oBigInts[2].equals(new BigInteger("123456789012345678901234567890")));

        assertTrue(root._bigDecs[0].equals(new BigDecimal("-123456789012345678901234567890.123456789012345678901234567890")));
        assertTrue(root._bigDecs[1].equals(new BigDecimal("0.0")));
        assertTrue(root._bigDecs[2].equals(new BigDecimal("123456789012345678901234567890.123456789012345678901234567890")));

        assertTrue(root._oBigDecs[0].equals(new BigDecimal("-123456789012345678901234567890.123456789012345678901234567890")));
        assertTrue(root._oBigDecs[1].equals(new BigDecimal("0.0")));
        assertTrue(root._oBigDecs[2].equals(new BigDecimal("123456789012345678901234567890.123456789012345678901234567890")));
    }

    private static class TestCollection implements Serializable
    {
        private Collection[] _cols;
        private List _strings_a;
        private List _strings_b;
        private List _strings_c;
        private List _dates_a;
        private List _dates_b;
        private List _dates_c;
        private List _classes_a;
        private List _classes_b;
        private List _classes_c;
        private List _sb_a;
        private List _sb_b;
        private List _sb_c;
        private List _poly_a;
        private ArrayList _typedCol;
        private Set _strs_a;
        private Set _strs_b;
        private Set _strs_c;
        private Set _strs_d;
        private HashSet _typedSet;

        private void init()
        {
            Collection array = new ArrayList();
            array.add(_testDate);
            array.add("Hello");
            array.add(new TestObject("fudge"));
            array.add(_CONST_INT);

            Collection set = new HashSet();
            set.add(Map.class);
            set.add(Boolean.TRUE);
            set.add(null);
            set.add(_CONST_INT);

            Collection tree = new TreeSet();
            tree.add(new Integer(Integer.MIN_VALUE));
            tree.add(new Integer(1));
            tree.add(new Integer(Integer.MAX_VALUE));
            tree.add(_CONST_INT);

            _cols = new Collection[] {array, set, tree};

            _strings_a = new LinkedList();
            _strings_a.add("Alpha");
            _strings_a.add("Bravo");
            _strings_a.add("Charlie");
            _strings_a.add("Delta");
            _strings_b = new LinkedList();
            _strings_c = null;

            _dates_a = new ArrayList();
            _dates_a.add(new Date(0));
            _dates_a.add(_testDate);
            _dates_a.add(new Date(Long.MAX_VALUE));
            _dates_a.add(null);
            _dates_b = new ArrayList();
            _dates_c = null;

            _classes_a = new ArrayList();
            _classes_a.add(boolean.class);
            _classes_a.add(char.class);
            _classes_a.add(byte.class);
            _classes_a.add(short.class);
            _classes_a.add(int.class);
            _classes_a.add(long.class);
            _classes_a.add(float.class);
            _classes_a.add(double.class);
            _classes_a.add(String.class);
            _classes_a.add(Date.class);
            _classes_a.add(null);
            _classes_a.add(Class.class);
            _classes_b = new ArrayList();
            _classes_c = null;

            _sb_a = new LinkedList();
            _sb_a.add(new StringBuffer("one"));
            _sb_a.add(new StringBuffer("two"));
            _sb_b = new LinkedList();
            _sb_c = null;

            _poly_a = new ArrayList();
            _poly_a.add(Boolean.TRUE);
            _poly_a.add(new Character('a'));
            _poly_a.add(new Byte((byte) 16));
            _poly_a.add(new Short((short) 69));
            _poly_a.add(new Integer(714));
            _poly_a.add(new Long(420));
            _poly_a.add(new Float(0.4));
            _poly_a.add(new Double(3.14));
            _poly_a.add("Jones'in\tfor\u0019a\ncoke");
            _poly_a.add(null);
            _poly_a.add(new StringBuffer("eddie"));
            _poly_a.add(_testDate);
            _poly_a.add(Long.class);
            _poly_a.add(new String[]{"beatles", "stones"});
            _poly_a.add(new TestObject[]{new TestObject("flint"), new TestObject("stone")});
            _poly_a.add(new Object[]{"fox", "wolf", "dog", "hound"});

            Set colors = new TreeSet();
            colors.add(new TestObject("red"));
            colors.add(new TestObject("green"));
            colors.add(new TestObject("blue"));
            _poly_a.add(colors);

            _strs_a = new HashSet();
            _strs_a.add("Dog");
            _strs_a.add("Cat");
            _strs_a.add("Cow");
            _strs_a.add("Horse");
            _strs_a.add("Duck");
            _strs_a.add("Bird");
            _strs_a.add("Goose");
            _strs_b = new HashSet();
            _strs_c = null;
            _strs_d = new TreeSet();
            _strs_d.addAll(_strs_a);

            _typedCol = new ArrayList();
            _typedCol.add("string");
            _typedCol.add(null);
            _typedCol.add(new Date(19));
            _typedCol.add(true);
            _typedCol.add(17.76);
            _typedCol.add(TimeZone.getTimeZone("PST"));

            _typedSet = new HashSet();
            _typedSet.add("string");
            _typedSet.add(null);
            _typedSet.add(new Date(19));
            _typedSet.add(true);
            _typedSet.add(17.76);
            _typedSet.add(TimeZone.getTimeZone("PST"));
        }
    }

    public void testCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testCollection()");
        TestCollection obj = new TestCollection();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        TestCollection root = (TestCollection) readJsonObject(jsonOut);

        assertCollection(root);

        time(root);
    }

    private void assertCollection(TestCollection root)
    {
        assertTrue(root._cols.length == 3);
        assertTrue(root._cols[0].getClass().equals(ArrayList.class));
        assertTrue(root._cols[1].getClass().equals(HashSet.class));
        assertTrue(root._cols[2].getClass().equals(TreeSet.class));

        Collection array = root._cols[0];
        assertTrue(array.size() == 4);
        assertTrue(array.getClass().equals(ArrayList.class));
        List alist = (List) array;
        assertTrue(alist.get(0).equals(_testDate));
        assertTrue(alist.get(1).equals("Hello"));
        assertTrue(alist.get(2).equals(new TestObject("fudge")));
        assertTrue(alist.get(3).equals(_CONST_INT));

        Collection set = root._cols[1];
        assertTrue(set.size() == 4);
        assertTrue(set.getClass().equals(HashSet.class));
        assertTrue(set.contains(Map.class));
        assertTrue(set.contains(Boolean.TRUE));
        assertTrue(set.contains(null));
        assertTrue(set.contains(_CONST_INT));

        set = root._cols[2];
        assertTrue(set.size() == 4);
        assertTrue(set.getClass().equals(TreeSet.class));
        assertTrue(set.contains(new Integer(Integer.MIN_VALUE)));
        assertTrue(set.contains(new Integer(1)));
        assertTrue(set.contains(new Integer(Integer.MAX_VALUE)));
        assertTrue(set.contains(_CONST_INT));

        assertTrue(root._strings_a.size() == 4);
        assertTrue(root._strings_a.get(0).equals("Alpha"));
        assertTrue(root._strings_a.get(1).equals("Bravo"));
        assertTrue(root._strings_a.get(2).equals("Charlie"));
        assertTrue(root._strings_a.get(3).equals("Delta"));
        assertTrue(root._strings_b.isEmpty());
        assertNull(root._strings_c);

        assertTrue(root._dates_a.size() == 4);
        assertTrue(root._dates_a.get(0).equals(new Date(0)));
        assertTrue(root._dates_a.get(1).equals(_testDate));
        assertTrue(root._dates_a.get(2).equals(new Date(Long.MAX_VALUE)));
        assertNull(root._dates_a.get(3));
        assertTrue(root._dates_b.isEmpty());
        assertNull(root._dates_c);

        assertTrue(root._classes_a.size() == 12);
        assertTrue(root._classes_a.get(0) == boolean.class);
        assertTrue(root._classes_a.get(1).equals(char.class));
        assertTrue(root._classes_a.get(2).equals(byte.class));
        assertTrue(root._classes_a.get(3).equals(short.class));
        assertTrue(root._classes_a.get(4).equals(int.class));
        assertTrue(root._classes_a.get(5).equals(long.class));
        assertTrue(root._classes_a.get(6).equals(float.class));
        assertTrue(root._classes_a.get(7).equals(double.class));
        assertTrue(root._classes_a.get(8).equals(String.class));
        assertTrue(root._classes_a.get(9).equals(Date.class));
        assertNull(root._classes_a.get(10));
        assertTrue(root._classes_a.get(11).equals(Class.class));
        assertTrue(root._classes_b.isEmpty());
        assertNull(root._classes_c);

        assertTrue(root._sb_a.size() == 2);
        assertTrue(root._sb_a.get(0).toString().equals("one"));
        assertTrue(root._sb_a.get(1).toString().equals("two"));
        assertTrue(root._sb_b.isEmpty());
        assertNull(root._sb_c);

        assertTrue(root._poly_a.size() == 17);
        assertTrue(root._poly_a.get(0).equals(Boolean.TRUE));
        assertTrue(root._poly_a.get(1).equals(new Character('a')));
        assertTrue(root._poly_a.get(2).equals(new Byte((byte) 16)));
        assertTrue(root._poly_a.get(3).equals(new Short((byte) 69)));
        assertTrue(root._poly_a.get(4).equals(new Integer(714)));
        assertTrue(root._poly_a.get(5).equals(new Long(420)));
        assertTrue(root._poly_a.get(6).equals(new Float(0.4)));
        assertTrue(root._poly_a.get(7).equals(new Double(3.14)));
        assertTrue(root._poly_a.get(8).equals("Jones'in\tfor\u0019a\ncoke"));
        assertNull(root._poly_a.get(9));
        assertTrue(root._poly_a.get(10).toString().equals("eddie"));
        assertTrue(root._poly_a.get(11).equals(_testDate));
        assertTrue(root._poly_a.get(12).equals(Long.class));

        String[] sa = (String[]) root._poly_a.get(13);
        assertTrue(sa[0].equals("beatles"));
        assertTrue(sa[1].equals("stones"));
        TestObject[] to = (TestObject[]) root._poly_a.get(14);
        assertTrue(to[0].getName().equals("flint"));
        assertTrue(to[1].getName().equals("stone"));
        Object[] arrayInCol = (Object[]) root._poly_a.get(15);
        assertTrue(arrayInCol[0].equals("fox"));
        assertTrue(arrayInCol[1].equals("wolf"));
        assertTrue(arrayInCol[2].equals("dog"));
        assertTrue(arrayInCol[3].equals("hound"));

        Set colors = (Set) root._poly_a.get(16);
        assertTrue(colors.size() == 3);
        assertTrue(colors.contains(new TestObject("red")));
        assertTrue(colors.contains(new TestObject("green")));
        assertTrue(colors.contains(new TestObject("blue")));

        assertTrue(root._strs_a.size() == 7);
        assertTrue(root._strs_a.contains("Dog"));
        assertTrue(root._strs_a.contains("Cat"));
        assertTrue(root._strs_a.contains("Cow"));
        assertTrue(root._strs_a.contains("Horse"));
        assertTrue(root._strs_a.contains("Duck"));
        assertTrue(root._strs_a.contains("Bird"));
        assertTrue(root._strs_a.contains("Goose"));
        assertTrue(root._strs_b.isEmpty());
        assertNull(root._strs_c);
        assertTrue(root._strs_d.size() == 7);
        assertTrue(root._strs_d instanceof TreeSet);

        assertTrue(root._typedCol != null);
        assertTrue(root._typedCol .size() == 6);
        assertTrue("string".equals(root._typedCol.get(0)));
        assertTrue(null == root._typedCol.get(1));
        assertTrue((new Date(19)).equals(root._typedCol.get(2)));
        assertTrue((Boolean)root._typedCol.get(3));
        assertTrue(17.76 == (Double) root._typedCol.get(4));
        assertTrue(TimeZone.getTimeZone("PST").equals(root._typedCol.get(5)));

        assertTrue(root._typedSet != null);
        assertTrue(root._typedSet .size() == 6);
        assertTrue(root._typedSet.contains("string"));
        assertTrue(root._typedCol.contains(null));
        assertTrue(root._typedCol.contains(new Date(19)));
        assertTrue(root._typedCol.contains(true));
        assertTrue(root._typedCol.contains(17.76));
        assertTrue(root._typedCol.contains(TimeZone.getTimeZone("PST")));
    }

    private static class TestMap implements Serializable
    {
        private HashMap _strings_a;
        private Map _strings_b;
        private Map _strings_c;
        private Map _testobjs_a;
        private Map _map_col;
        private Map _map_col_2;
        private Map _map_col_3;
        private Map _map_obj;
        private Map _map_con;
        private TreeMap _typedMap;

        private void init()
        {
            _strings_a = new HashMap();
            _strings_b = new HashMap();
            _strings_c = null;
            _testobjs_a = new TreeMap();
            _map_col = new HashMap();
            _map_col_2 = new TreeMap();
            _map_col_3 = new HashMap();
            _map_obj = new HashMap();
            _map_con = new ConcurrentHashMap();

            _strings_a.put("woods", "tiger");
            _strings_a.put("mickleson", "phil");
            _strings_a.put("garcia", "sergio");

            _testobjs_a.put(new TestObject("one"), new TestObject("alpha"));
            _testobjs_a.put(new TestObject("two"), new TestObject("bravo"));

            List l = new LinkedList();
            l.add("andromeda");
            _map_col.put(new TestObject[]{new TestObject("earth"), new TestObject("jupiter")}, l);
            _map_col_2.put("cat", new Object[]{"tiger", "lion", "cheetah", "jaguar"});
            _map_col_3.put(new Object[]{"composite", "key"}, "value");

            _map_obj.put(new Integer(99), new Double(.123));
            _map_obj.put(null, null);

            _map_con.put(new TestObject("alpha"), new TestObject("one"));
            _map_con.put(new TestObject("bravo"), new TestObject("two"));

            _typedMap = new TreeMap();
            _typedMap.put("a", "alpha");
            _typedMap.put("b", "bravo");
        }
    }

    public void testMap() throws Exception
    {
        println("\nTestJsonReaderWriter.testMap()");
        TestMap obj = new TestMap();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        TestMap root = (TestMap) readJsonObject(jsonOut);
        assertMap(root);
        time(root);
    }

    private void assertMap(TestMap root)
    {
        assertTrue(root._strings_a.size() == 3);
        assertTrue(root._strings_a.get("woods").equals("tiger"));
        assertTrue(root._strings_a.get("mickleson").equals("phil"));
        assertTrue(root._strings_a.get("garcia").equals("sergio"));
        assertTrue(root._strings_b.isEmpty());
        assertNull(root._strings_c);

        assertTrue(root._testobjs_a.size() == 2);
        assertTrue(root._testobjs_a.get(new TestObject("one")).equals(new TestObject("alpha")));
        assertTrue(root._testobjs_a.get(new TestObject("two")).equals(new TestObject("bravo")));

        assertTrue(root._map_col.size() == 1);
        Iterator i = root._map_col.keySet().iterator();
        TestObject[] key = (TestObject[]) i.next();
        key[0]._name.equals("earth");
        key[1]._name.equals("jupiter");
        i = root._map_col.values().iterator();
        Collection list = (Collection) i.next();
        list.contains("andromeda");

        // Check value-side of Map with Object[] (special case because Object[]'s @type is never written)
        Object[] catTypes = (Object[]) root._map_col_2.get("cat");
        assertTrue(catTypes[0].equals("tiger"));
        assertTrue(catTypes[1].equals("lion"));
        assertTrue(catTypes[2].equals("cheetah"));
        assertTrue(catTypes[3].equals("jaguar"));

        assertTrue(root._map_col_3.size() == 1);
        i = root._map_col_3.keySet().iterator();
        Object[] key_a = (Object[]) i.next();
        key_a[0].equals("composite");
        key_a[1].equals("key");
        String value = (String) root._map_col_3.get(key_a);
        assertTrue("value".equals(value));

        assertTrue(root._map_obj.size() == 2);
        assertTrue(root._map_obj.get(new Integer(99)).equals(new Double(.123)));
        assertNull(root._map_obj.get(null));

        assertTrue(root._map_con.size() == 2);
        assertTrue(root._map_con instanceof ConcurrentHashMap);
        i = root._map_con.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry e = (Map.Entry) i.next();
            TestObject key1 = (TestObject) e.getKey();
            TestObject value1 = (TestObject) e.getValue();
            if (key1.equals(new TestObject("alpha")))
            {
                assertTrue(value1.getName().equals("one"));
            }
            else if (key1.equals(new TestObject("bravo")))
            {
                assertTrue(value1.getName().equals("two"));
            }
            else
            {
                assertTrue("Unknown key", false);
            }
        }

        assertTrue(root._typedMap != null);
        assertTrue(root._typedMap.size() == 2);
        assertTrue(root._typedMap.get("a").equals("alpha"));
        assertTrue(root._typedMap.get("b").equals("bravo"));
    }

    /**
     * Test direct fields for all types, primitives, special handled fields
     * like Date, String, and Class, plus regular objects, and circular
     * references.
     */
    private static class TestFields implements Serializable
    {
        private boolean _boolean_a;
        private boolean _boolean_b;
        private Boolean _boolean_c;
        private Boolean _boolean_d;
        private Boolean _boolean_e;

        private char _char_a;
        private char _char_b;
        private char _char_c;
        private Character _char_d;
        private Character _char_e;
        private Character _char_f;
        private Character _char_g;

        private byte _byte_a;
        private byte _byte_b;
        private Byte _byte_c;
        private Byte _byte_d;
        private Byte _byte_e;

        private short _short_a;
        private short _short_b;
        private Short _short_c;
        private Short _short_d;
        private Short _short_e;

        private int _int_a;
        private int _int_b;
        private Integer _int_c;
        private Integer _int_d;
        private Integer _int_e;

        private long _long_a;
        private long _long_b;
        private Long _long_c;
        private Long _long_d;
        private Long _long_e;

        private float _float_a;
        private float _float_b;
        private Float _float_c;
        private Float _float_d;
        private Float _float_e;

        private double _double_a;
        private double _double_b;
        private Double _double_c;
        private Double _double_d;
        private Double _double_e;

        private String _string_a;
        private String _string_b;
        private String _string_c;

        private Date _date_a;
        private Date _date_b;
        private Date _date_c;

        private Class _class_a;
        private Class _class_b;
        private Class _class_c;
        private Class _class_d;
        private Class _class_e;
        private Class _class_f;
        private Class _class_g;
        private Class _class_h;
        private Class _class_i;
        private Class _class_j;

        private StringBuffer _sb_a;
        private StringBuffer _sb_b;
        private StringBuffer _sb_c;
        private Object _sb_d;

        private StringBuilder _sbld_a;
        private StringBuilder _sbld_b;
        private StringBuilder _sbld_c;
        private Object _sbld_d;

        private BigInteger _bigInt;
        private Object _bigIntO;
        private BigDecimal _bigDec;
        private Object _bigDecO;

        // Cycle test
        private TestObject _cycleTest;

        // Ensure @type is dropped when Collection field type matches instance type
        // Normally, this is poor coding style, however, the @type field can be dropped
        // in these cases, making the JSON output smaller.
        private ArrayList _arrayList_empty;
        private ArrayList _arrayList_1;
        private List _arrayList_2;
        private List _arrayList_3;
        private List _arrayList_4;
        private ArrayList _arrayList_5;
        private List _arrayList_6;

        private HashMap _hashMap_empty;
        private HashMap _hashMap_1;
        private Map _hashMap_2;
        private Map _hashMap_3;
        private Map _hashMap_4;
        private HashMap _hashMap_5;
        private Map _hashMap_6;

        private String[] _stringArray_empty;
        private String[] _stringArray_1;
        private Object[] _stringArray_2;
        private Object[] _stringArray_3;
        private Object[] _stringArray_4;
        private String[] _stringArray_5;
        private Object[] _stringArray_6;

        private void init()
        {
            _boolean_a = true;
            _boolean_b = false;
            _boolean_c = new Boolean(true);
            _boolean_d = new Boolean(false);
            _boolean_e = null;

            _char_a = 'a';
            _char_b = '\t';
            _char_c = '\u0004';
            _char_d = new Character('a');
            _char_e = new Character('\t');
            _char_f = new Character('\u0002');
            _char_g = null;

            _byte_a = -128;
            _byte_b = 127;
            _byte_c = new Byte((byte) -128);
            _byte_d = new Byte((byte) 127);
            _byte_e = null;

            _short_a = Short.MIN_VALUE;
            _short_b = Short.MAX_VALUE;
            _short_c = new Short(Short.MIN_VALUE);
            _short_d = new Short(Short.MAX_VALUE);
            _short_e = null;

            _int_a = Integer.MIN_VALUE;
            _int_b = Integer.MAX_VALUE;
            _int_c = new Integer(Integer.MIN_VALUE);
            _int_d = new Integer(Integer.MAX_VALUE);
            _int_e = null;

            _long_a = Long.MIN_VALUE;
            _long_b = Long.MAX_VALUE;
            _long_c = new Long(Long.MIN_VALUE);
            _long_d = new Long(Long.MAX_VALUE);
            _long_e = null;

            _float_a = Float.MIN_VALUE;
            _float_b = Float.MAX_VALUE;
            _float_c = new Float(Float.MIN_VALUE);
            _float_d = new Float(Float.MAX_VALUE);
            _float_e = null;

            _double_a = Double.MIN_VALUE;
            _double_b = Double.MAX_VALUE;
            _double_c = new Double(Double.MIN_VALUE);
            _double_d = new Double(Double.MAX_VALUE);
            _double_e = null;

            _string_a = "Hello";
            _string_b = "";
            _string_c = null;

            _date_a = _testDate;
            _date_b = new Date(0);
            _date_c = null;

            _class_a = boolean.class;
            _class_b = char.class;
            _class_c = byte.class;
            _class_d = short.class;
            _class_e = int.class;
            _class_f = long.class;
            _class_g = float.class;
            _class_h = double.class;
            _class_i = String.class;
            _class_j = null;

            _sb_a = new StringBuffer("holstein");
            _sb_b = new StringBuffer();
            _sb_c = null;
            _sb_d = new StringBuffer("viper");

            _sbld_a = new StringBuilder("holstein");
            _sbld_b = new StringBuilder();
            _sbld_c = null;
            _sbld_d = new StringBuilder("viper");

            _bigInt = new BigInteger("25");
            _bigIntO = new BigInteger("25");
            _bigDec = new BigDecimal("25.0");
            _bigDecO = new BigDecimal("25.0");

            TestObject a = new TestObject("A");
            TestObject b = new TestObject("B");
            TestObject c = new TestObject("C");
            a._other = b;
            b._other = c;
            c._other = a;
            _cycleTest = a;

            _arrayList_empty = new ArrayList();
            _arrayList_1 = new ArrayList();
            _arrayList_1.add("should be no id, no type");
            _arrayList_2 = new ArrayList();
            _arrayList_2.add("should have type, but no id");
            _arrayList_3 = new ArrayList();
            _arrayList_3.add("should have id and type");
            _arrayList_4 = _arrayList_3;
            _arrayList_5 = new ArrayList();
            _arrayList_5.add("should have id, but no type");
            _arrayList_6 = _arrayList_5;

            _hashMap_empty = new HashMap();
            _hashMap_1 = new HashMap();
            _hashMap_1.put("mapkey", "should have no id or type");
            _hashMap_2 = new HashMap();
            _hashMap_2.put("mapkey", "should have type, but no id");
            _hashMap_3 = new HashMap();
            _hashMap_3.put("mapkey", "should have id and type");
            _hashMap_4 = _hashMap_3;
            _hashMap_5 = new HashMap();
            _hashMap_5.put("mapkey", "should have id, but no type");
            _hashMap_6 = _hashMap_5;

            _stringArray_empty = new String[]{};
            _stringArray_1 = new String[]{"should have no id, no type"};
            _stringArray_2 = new String[]{"should have type, but no id"};
            _stringArray_3 = new String[]{"should have id and type"};
            _stringArray_4 = _stringArray_3;
            _stringArray_5 = new String[]{"should have id, but not type"};
            _stringArray_6 = _stringArray_5;
        }
    }

    public void testFields() throws Exception
    {
        println("\nTestJsonReaderWriter.testFields()");
        TestFields obj = new TestFields();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        TestFields root = (TestFields) readJsonObject(jsonOut);
        assertFields(root);
        time(root);
    }

    private void assertFields(TestFields root)
    {
        assertTrue(root._boolean_a);
        assertFalse(root._boolean_b);
        assertTrue(root._boolean_c.booleanValue());
        assertFalse(root._boolean_d.booleanValue());
        assertNull(root._boolean_e);

        assertTrue(root._char_a == 'a');
        assertTrue(root._char_b == '\t');
        assertTrue(root._char_c == '\u0004');
        assertTrue(root._char_d.equals(new Character('a')));
        assertTrue(root._char_e.equals(new Character('\t')));
        assertTrue(root._char_f.equals(new Character('\u0002')));
        assertNull(root._char_g);

        assertTrue(root._byte_a == Byte.MIN_VALUE);
        assertTrue(root._byte_b == Byte.MAX_VALUE);
        assertTrue(root._byte_c.equals(new Byte(Byte.MIN_VALUE)));
        assertTrue(root._byte_d.equals(new Byte(Byte.MAX_VALUE)));
        assertNull(root._byte_e);

        assertTrue(root._short_a == Short.MIN_VALUE);
        assertTrue(root._short_b == Short.MAX_VALUE);
        assertTrue(root._short_c.equals(new Short(Short.MIN_VALUE)));
        assertTrue(root._short_d.equals(new Short(Short.MAX_VALUE)));
        assertNull(root._short_e);

        assertTrue(root._int_a == Integer.MIN_VALUE);
        assertTrue(root._int_b == Integer.MAX_VALUE);
        assertTrue(root._int_c.equals(new Integer(Integer.MIN_VALUE)));
        assertTrue(root._int_d.equals(new Integer(Integer.MAX_VALUE)));
        assertNull(root._int_e);

        assertTrue(root._long_a == Long.MIN_VALUE);
        assertTrue(root._long_b == Long.MAX_VALUE);
        assertTrue(root._long_c.equals(new Long(Long.MIN_VALUE)));
        assertTrue(root._long_d.equals(new Long(Long.MAX_VALUE)));
        assertNull(root._long_e);

        assertTrue(root._float_a == Float.MIN_VALUE);
        assertTrue(root._float_b == Float.MAX_VALUE);
        assertTrue(root._float_c.equals(new Float(Float.MIN_VALUE)));
        assertTrue(root._float_d.equals(new Float(Float.MAX_VALUE)));
        assertNull(root._float_e);

        assertTrue(root._double_a == Double.MIN_VALUE);
        assertTrue(root._double_b == Double.MAX_VALUE);
        assertTrue(root._double_c.equals(new Double(Double.MIN_VALUE)));
        assertTrue(root._double_d.equals(new Double(Double.MAX_VALUE)));
        assertNull(root._double_e);

        assertTrue(root._string_a.equals("Hello"));
        assertTrue(root._string_b.equals(""));
        assertNull(root._string_c);

        assertTrue(root._date_a.equals(_testDate));
        assertTrue(root._date_b.equals(new Date(0)));
        assertNull(root._date_c);

        assertTrue(root._class_a.equals(boolean.class));
        assertTrue(root._class_b.equals(char.class));
        assertTrue(root._class_c.equals(byte.class));
        assertTrue(root._class_d.equals(short.class));
        assertTrue(root._class_e.equals(int.class));
        assertTrue(root._class_f.equals(long.class));
        assertTrue(root._class_g.equals(float.class));
        assertTrue(root._class_h.equals(double.class));
        assertTrue(root._class_i.equals(String.class));
        assertNull(root._class_j);

        assertTrue("holstein".equals(root._sb_a.toString()));
        assertTrue(root._sb_b.toString().equals(""));
        assertNull(root._sb_c);
        assertTrue("viper".equals(root._sb_d.toString()));

        assertTrue("holstein".equals(root._sb_a.toString()));
        assertTrue(root._sb_b.toString().equals(""));
        assertNull(root._sb_c);
        assertTrue("viper".equals(root._sb_d.toString()));

        assertTrue(root._bigInt.equals(new BigInteger("25")));
        assertTrue(root._bigIntO.equals(new BigInteger("25")));
        assertTrue(root._bigDec.equals(new BigDecimal("25.0")));
        assertTrue(root._bigDec.equals(new BigDecimal("25.0")));

        assertTrue(root._cycleTest._name.equals("A"));
        assertTrue(root._cycleTest._other._name.equals("B"));
        assertTrue(root._cycleTest._other._other._name.equals("C"));
        assertTrue(root._cycleTest._other._other._other._name.equals("A"));
        assertTrue(root._cycleTest == root._cycleTest._other._other._other);

        assertTrue(root._arrayList_empty.isEmpty());
        assertTrue(root._arrayList_1.size() == 1);
        assertTrue(root._arrayList_2.size() == 1);
        assertTrue(root._arrayList_3.size() == 1);
        assertTrue(root._arrayList_4 == root._arrayList_3);
        assertTrue(root._arrayList_5.size() == 1);
        assertTrue(root._arrayList_6 == root._arrayList_5);

        assertTrue(root._hashMap_empty.isEmpty());
        assertTrue(root._hashMap_1.size() == 1);
        assertTrue(root._hashMap_2.size() == 1);
        assertTrue(root._hashMap_3.size() == 1);
        assertTrue(root._hashMap_4 == root._hashMap_3);
        assertTrue(root._hashMap_5.size() == 1);
        assertTrue(root._hashMap_6 == root._hashMap_5);

        assertTrue(root._stringArray_empty.length == 0);
        assertTrue(root._stringArray_1.length == 1);
        assertTrue(root._stringArray_2.length == 1);
        assertTrue(root._stringArray_3.length == 1);
        assertTrue(root._stringArray_4 == root._stringArray_3);
        assertTrue(root._stringArray_5.length == 1);
        assertTrue(root._stringArray_6 == root._stringArray_5);
    }

    private static class TestReferences implements Serializable
    {
        // Field ordering below is vital (b, a, c).  We treat JSON keys in alphabetical
        // order, whereas Java Field walking is in declaration order.
        private TestObject _b;
        private TestObject[] _a;
        private TestObject[] _c;

        private TestObject[][] _foo;

        private TestObject _back_a;
        private TestObject _back_b;
        private TestObject _back_c;

        private TestObject _cycle_a;
        private TestObject _cycle_b;

        private TestObject _polymorphic;
        private TestObject[] _polymorphics;

        private char _big;

        private void init()
        {
            _big = '\ufbfc';
            _b = new TestObject("B");
            _a = new TestObject[]{_b};
            _c = new TestObject[]{_b};
            _foo = new TestObject[][]{null, {}, {new TestObject("alpha"), new TestObject("beta")}};
            _back_a = new TestObject("back test");
            _back_b = _back_a;
            _back_c = _back_b;

            _cycle_a = new TestObject("a");
            _cycle_b = new TestObject("b");
            _cycle_a._other = _cycle_b;
            _cycle_b._other = _cycle_a;

            _polymorphic = new TestObjectKid("dilbert", "dilbert@myotherdrive.com");
            _polymorphics = new TestObject[]{new TestObjectKid("dog", "dog@house.com"), new TestObjectKid("cat", "cat@house.com"), new TestObject("shortie")};
        }
    }

    public void testReferences() throws Exception
    {
        println("\nTestJsonReaderWriter.testReferences()");
        TestReferences obj = new TestReferences();
        obj.init();
        String jsonOut = getJsonString(obj);
        println(jsonOut);

        TestReferences root = (TestReferences) readJsonObject(jsonOut);

        assertTrue(root._a.length == 1);
        assertTrue(root._b != null);
        assertTrue(root._a[0] == root._b);
        assertTrue(root._c.length == 1);
        assertTrue(root._c[0] == root._b);

        assertTrue(root._foo.length == 3);
        assertNull(root._foo[0]);
        assertTrue(root._foo[1].length == 0);
        assertTrue(root._foo[2].length == 2);

        assertTrue(root._back_b == root._back_a);
        assertTrue(root._back_c == root._back_a);

        assertTrue(root._cycle_a._name.equals("a"));
        assertTrue(root._cycle_b._name.equals("b"));
        assertTrue(root._cycle_a._other == root._cycle_b);
        assertTrue(root._cycle_b._other == root._cycle_a);

        assertTrue(root._polymorphic.getClass().equals(TestObjectKid.class));
        TestObjectKid kid = (TestObjectKid) root._polymorphic;
        assertTrue(kid._name.equals("dilbert"));
        assertTrue(kid._email.equals("dilbert@myotherdrive.com"));

        assertTrue(root._polymorphics.length == 3);
        TestObjectKid kid1 = (TestObjectKid) root._polymorphics[0];
        TestObjectKid kid2 = (TestObjectKid) root._polymorphics[1];
        TestObject kid3 = root._polymorphics[2];
        assertTrue(kid1.getClass().equals(TestObjectKid.class));
        assertTrue(kid1.getClass().equals(kid2.getClass()));
        assertTrue(kid3.getClass().equals(TestObject.class));
        assertTrue(kid1._name.equals("dog"));
        assertTrue(kid1._email.equals("dog@house.com"));
        assertTrue(kid2._name.equals("cat"));
        assertTrue(kid2._email.equals("cat@house.com"));
        assertTrue(kid3._name.equals("shortie"));
        assertTrue(root._big == '\ufbfc');

        time(root);
    }

    public void testPerformance() throws Exception
    {
        println("\nTestJsonReaderWriter.testPerformance()    128K byte[]");
        byte[] bytes = new byte[128 * 1024];
        Random r = new Random();
        r.nextBytes(bytes);
        String json = getJsonString(bytes);

        byte[] bytes2 = (byte[]) readJsonObject(json);

        for (int i = 0; i < bytes.length; i++)
        {
            assertTrue(bytes[i] == bytes2[i]);
        }

        time(bytes);
    }

    public static class DateTrick
    {
        private Date _userDate;
    }

    public static class LongTrick
    {
        private long _userDate;
    }

    /**
     * Instantiate off of each others JSON String, proving Date for long substitution works.  This will work on any
     * field that is of type Date or Long.  It will not work when the Dates are inside a Collection, for example.
     * <p/>
     * This substitution trick allows Date fields to be converted to long in order to save memory 16 bytes of memory
     * per date.  (Date's are more than 8 bytes, longs are 8).
     */
    public void testDateLongSubstitution() throws Exception
    {
        println("\nTestJsonReaderWriter.testDateLongSubstitution()");
        long now = System.currentTimeMillis();
        DateTrick d = new DateTrick();
        d._userDate = new Date(now);
        LongTrick l = new LongTrick();
        l._userDate = now;
        String jsonOut1 = getJsonString(d);
        println(jsonOut1);
        String jsonOut2 = getJsonString(l);
        println(jsonOut2);
        jsonOut1 = jsonOut1.replace("$Date", "$Long");
        jsonOut2 = jsonOut2.replace("$Long", "$Date");
        l = (LongTrick) readJsonObject(jsonOut1);
        d = (DateTrick) readJsonObject(jsonOut2);
        assertTrue(d._userDate.getTime() == l._userDate);
    }

    public void testRoots() throws Exception
    {
        println("\nTestJsonReaderWriter.testRoots()");
        // Test Object[] as root element passed in
        Object[] foo = {new TestObject("alpha"), new TestObject("beta")};

        String jsonOut = getJsonString(foo);
        println(jsonOut);

        Object[] bar = (Object[]) JsonReader.toJava(jsonOut);
        assertTrue(bar.length == 2);
        assertTrue(bar[0].equals(new TestObject("alpha")));
        assertTrue(bar[1].equals(new TestObject("beta")));

        String json = "[\"getStartupInfo\",[\"890.022905.16112006.00024.0067ur\",\"machine info\"]]";
        Object[] baz = (Object[]) JsonReader.toJava(json);
        assertTrue(baz.length == 2);
        assertTrue("getStartupInfo".equals(baz[0]));
        Object[] args = (Object[]) baz[1];
        assertTrue(args.length == 2);
        assertTrue("890.022905.16112006.00024.0067ur".equals(args[0]));
        assertTrue("machine info".equals(args[1]));

        String hw = "[\"Hello, World\"]";
        Object[] qux = (Object[]) JsonReader.toJava(hw);
        assertTrue(qux != null);
        assertTrue("Hello, World".equals(qux[0]));

        // Whitespace
        String pkg = TestObject.class.getName();
        Object[] fred = (Object[]) JsonReader.toJava(" [  {  \"@type\"  :  \"" + pkg + "\"  ,  \"_name\"  :  \"alpha\"  ,  \"_other\"  :  null  }  ,  {  \"@type\"  :  \"" + pkg + "\"  ,  \"_name\"  :  \"beta\"  ,  \"_other\" : null  }  ]  ");
        assertTrue(fred != null);
        assertTrue(fred.length == 2);
        assertTrue(fred[0].equals(new TestObject("alpha")));
        assertTrue(fred[1].equals(new TestObject("beta")));

        Object[] wilma = (Object[]) JsonReader.toJava("[{\"@type\":\"" + pkg + "\",\"_name\" : \"alpha\" , \"_other\":null,\"fake\":\"_typeArray\"},{\"@type\": \"" + pkg + "\",\"_name\":\"beta\",\"_other\":null}]");
        assertTrue(wilma != null);
        assertTrue(wilma.length == 2);
        assertTrue(wilma[0].equals(new TestObject("alpha")));
        assertTrue(wilma[1].equals(new TestObject("beta")));
    }

    public void testRoots2() throws Exception
    {
        println("\nTestJsonReaderWriter.testRoots2()");
        // Test root JSON type as [ ] 
        Object array = new Object[] {"Hello"};
        String json = getJsonString(array);
        Object oa = JsonReader.toJava(json);
        assertTrue(oa.getClass().isArray());
        assertTrue(((Object[])oa)[0].equals("Hello"));

        // Test root JSON type as { }
        Calendar cal = Calendar.getInstance();
        cal.set(1965, 11, 17);
        json = getJsonString(cal);
        println("json = " + json);
        Object obj = JsonReader.toJava(json);
        assertTrue(!obj.getClass().isArray());
        Calendar date = (Calendar) obj;
        assertTrue(date.get(Calendar.YEAR) == 1965);
        assertTrue(date.get(Calendar.MONTH) == 11);
        assertTrue(date.get(Calendar.DAY_OF_MONTH) == 17);
    }

    public void testNoDefaultConstructor() throws Exception
    {
        println("\nTestJsonReaderWriter.testNoDefaultConstructor()");
        Calendar c = Calendar.getInstance();
        c.set(2010, 5, 5, 5, 5, 5);
        String[] strings = new String[] { "C", "C++", "Java"};
        int[] ints = new int[] {1, 2, 4, 8, 16, 32, 64, 128};
        Object foo = new TestJsonNoDefaultOrPublicConstructor("Hello, World.", c.getTime(), (byte) 1, new Byte((byte)11), (short) 2, new Short((short)22), 3, new Integer(33), 4L, new Long(44L), 5.0f, new Float(55.0f), 6.0, new Double(66.0), true, Boolean.TRUE,'J', new Character('K'), strings, ints, new BigDecimal(1.1));
        String jsonOut = getJsonString(foo);
        println(jsonOut);

        TestJsonNoDefaultOrPublicConstructor bar = (TestJsonNoDefaultOrPublicConstructor) readJsonObject(jsonOut);

        assertTrue("Hello, World.".equals(bar.getString()));
        assertTrue(bar.getDate().equals(c.getTime()));
        assertTrue(bar.getByte() == 1);
        assertTrue(bar.getShort() == 2);
        assertTrue(bar.getInt() == 3);
        assertTrue(bar.getLong() == 4);
        assertTrue(bar.getFloat() == 5.0f);
        assertTrue(bar.getDouble() == 6.0);
        assertTrue(bar.getBoolean());
        assertTrue(bar.getChar() == 'J');
        assertTrue(bar.getStrings() != null);
        assertTrue(bar.getStrings().length == strings.length);
        assertTrue(bar.getInts() != null);
        assertTrue(bar.getInts().length == ints.length);
        assertTrue(bar._bigD.equals(new BigDecimal(1.1)));
    }

    private static class TestByte implements Serializable
    {
        private final Byte _arrayElement;
        private final Byte[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Byte _min;
        private final Byte _max;
        private final Byte _null;

        private TestByte()
        {
            _arrayElement = new Byte((byte) -1);
            _polyRefTarget = new Byte((byte)71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Byte((byte) 71);
            Byte local = new Byte((byte)75);
            _null  = null;
            _typeArray = new Byte[] {_arrayElement, (byte) 44, local, _null, null, new Byte((byte)44)};
            _objArray = new Object[] {_arrayElement, (byte) 69, local, _null, null, new Byte((byte)69)};
            _min = Byte.MIN_VALUE;
            _max = Byte.MAX_VALUE;
        }
    }

    public void testByte() throws Exception
    {
        println("\nTestJsonReaderWriter.testByte()");
        TestByte test = new TestByte();
        String json = getJsonString(test);
        println("json = " + json);

        TestByte that = (TestByte) readJsonObject(json);

        assertTrue(that._arrayElement.equals((byte) -1));
        assertTrue(that._polyRefTarget.equals((byte) 71));
        assertTrue(that._polyRef.equals((byte) 71));
        assertTrue(that._polyNotRef.equals((byte) 71));
        assertTrue(that._polyRef == that._polyRefTarget);
        assertTrue(that._polyNotRef == that._polyRef);             // byte cache is working

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[0] == that._arrayElement);  // byte cache is working
        assertTrue(that._typeArray[1] instanceof Byte);
        assertTrue(that._typeArray[1] instanceof Byte);
        assertTrue(that._typeArray[1].equals((byte) 44));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[0] == that._arrayElement);   // byte cache is working
        assertTrue(that._objArray[1] instanceof Byte);
        assertTrue(that._objArray[1].equals((byte) 69));
        assertTrue(that._polyRefTarget instanceof Byte);
        assertTrue(that._polyNotRef instanceof Byte);
        assertTrue(that._objArray[2].equals((byte) 75));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Byte.MIN_VALUE));
        assertTrue(that._max.equals(Byte.MAX_VALUE));
        assertTrue(that._min == Byte.MIN_VALUE);   // Verifies non-referenced byte caching is working
        assertTrue(that._max == Byte.MAX_VALUE); // Verifies non-referenced byte caching is working

        time(test);
    }

    private static class TestShort implements Serializable
    {
        private final Short _arrayElement;
        private final Short[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Short _min;
        private final Short _max;
        private final Short _null;

        private TestShort()
        {
            _arrayElement = new Short((short) -1);
            _polyRefTarget = new Short((short)710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Short((short) 710);
            Short local = new Short((short)75);
            _null  = null;
            _typeArray = new Short[] {_arrayElement, (short) 44, local, _null, null, new Short((short)44)};
            _objArray = new Object[] {_arrayElement, (short) 69, local, _null, null, new Short((short)69)};
            _min = Short.MIN_VALUE;
            _max = Short.MAX_VALUE;
        }
    }

    public void testShort() throws Exception
    {
        println("\nTestJsonReaderWriter.testShort()");
        TestShort test = new TestShort();
        String json = getJsonString(test);
        println("json = " + json);
        TestShort that = (TestShort) readJsonObject(json);

        assertTrue(that._arrayElement.equals((short) -1));
        assertTrue(that._polyRefTarget.equals((short) 710));
        assertTrue(that._polyRef.equals((short) 710));
        assertTrue(that._polyNotRef.equals((short) 710));
        assertTrue(that._polyRef != that._polyRefTarget);   // Primitive wrappers are treated like primitives (no ref)
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Short);
        assertTrue(that._typeArray[1] instanceof Short);
        assertTrue(that._typeArray[1].equals((short) 44));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Short);
        assertTrue(that._objArray[1].equals((short) 69));
        assertTrue(that._polyRefTarget instanceof Short);
        assertTrue(that._polyNotRef instanceof Short);

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._objArray[2].equals((short) 75));

        // Because of cache in Short.valueOf(), these values between -128 and 127 will have same address.
        assertTrue(that._typeArray[1] == that._typeArray[5]);
        assertTrue(that._objArray[1] == that._objArray[5]);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Short.MIN_VALUE));
        assertTrue(that._max.equals(Short.MAX_VALUE));
        assertTrue(that._min == Short.MIN_VALUE);
        assertTrue(that._max == Short.MAX_VALUE);
        time(test);
    }

    private static class TestInteger implements Serializable
    {
        private final Integer _arrayElement;
        private final Integer[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Integer _min;
        private final Integer _max;
        private final Integer _null;

        private TestInteger()
        {
            _arrayElement = -1;
            _polyRefTarget = new Integer(710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Integer(710);
            Integer local = new Integer(75);
            _null  = null;
            _typeArray = new Integer[] {_arrayElement, 44, local, _null, null, new Integer(44), 0, new Integer(0)};
            _objArray = new Object[] {_arrayElement, 69, local, _null, null, new Integer(69), 0, new Integer(0)};
            _min = Integer.MIN_VALUE;
            _max = Integer.MAX_VALUE;
        }
    }

    public void testInteger() throws Exception
    {
        println("\nTestJsonReaderWriter.testInteger()");
        TestInteger test = new TestInteger();
        String json = getJsonString(test);
        println("json = " + json);
        TestInteger that = (TestInteger) readJsonObject(json);

        assertTrue(that._arrayElement.equals(-1));
        assertTrue(that._polyRefTarget.equals(710));
        assertTrue(that._polyRef.equals(710));
        assertTrue(that._polyNotRef.equals(710));
        assertTrue(that._polyRef != that._polyRefTarget); // Primitive wrappers are treated like primitives (no ref)
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 8);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Integer);
        assertTrue(that._typeArray[1] instanceof Integer);
        assertTrue(that._typeArray[1].equals(44));
        assertTrue(that._objArray.length == 8);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Integer);
        assertTrue(that._objArray[1].equals(69));
        assertTrue(that._polyRefTarget instanceof Integer);
        assertTrue(that._polyNotRef instanceof Integer);
        assertTrue(that._objArray[2].equals(75));

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._typeArray[1] == that._typeArray[5]);
        assertTrue(that._objArray[1] == that._objArray[5]);
        // an unreferenced 0 is cached
        assertTrue(that._typeArray[6] == that._typeArray[7]);
        assertTrue(that._objArray[6] == that._objArray[7]);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Integer.MIN_VALUE));
        assertTrue(that._max.equals(Integer.MAX_VALUE));
        assertTrue(that._min == Integer.MIN_VALUE);
        assertTrue(that._max == Integer.MAX_VALUE);
        time(test);
    }

    private static class TestLong implements Serializable
    {
        private final Long _arrayElement;
        private final Long[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Long _min;
        private final Long _max;
        private final Long _null;

        private TestLong()
        {
            _arrayElement = new Long(-1);
            _polyRefTarget = new Long(710);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Long(710);
            Long local = new Long(75);
            _null = null;
            // 44 below is between -128 and 127, values cached by     Long Long.valueOf(long l)
            _typeArray = new Long[] {_arrayElement, 44L, local, _null, null, new Long(44)};
            _objArray = new Object[] {_arrayElement, 69L, local, _null, null, new Long(69)};
            _min = Long.MIN_VALUE;
            _max = Long.MAX_VALUE;
        }
    }

    public void testLong() throws Exception
    {
        println("\nTestJsonReaderWriter.testLong()");
        TestLong test = new TestLong();
        String json = getJsonString(test);
        println("json = " + json);
        TestLong that = (TestLong) readJsonObject(json);

        assertTrue(that._arrayElement.equals(-1L));
        assertTrue(that._polyRefTarget.equals(710L));
        assertTrue(that._polyRef.equals(710L));
        assertTrue(that._polyNotRef.equals(710L));
        assertTrue(that._polyRef != that._polyRefTarget);   // Primitive wrappers are treated like primitives (no ref)
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Long);
        assertTrue(that._typeArray[1] instanceof Long);
        assertTrue(that._typeArray[1].equals(44L));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Long);
        assertTrue(that._objArray[1].equals(69L));
        assertTrue(that._polyRefTarget instanceof Long);
        assertTrue(that._polyNotRef instanceof Long);

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._typeArray[1] == that._typeArray[5]);
        assertTrue(that._objArray[1] == that._objArray[5]);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Long.MIN_VALUE));
        assertTrue(that._max.equals(Long.MAX_VALUE));
        assertTrue(that._min == Long.MIN_VALUE);
        assertTrue(that._max == Long.MAX_VALUE);
        time(test);
    }

    private static class TestDouble implements Serializable
    {
        private final Double _arrayElement;
        private final Double[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Double _min;
        private final Double _max;
        private final Double _null;

        private TestDouble()
        {
            _arrayElement = new Double(-1);
            _polyRefTarget = new Double(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Double(71);
            Double local = new Double(75);
            _null = null;
            _typeArray = new Double[] {_arrayElement, 44.0, local, _null, null, new Double(44)};
            _objArray = new Object[] {_arrayElement, 69.0, local, _null, null, new Double(69)};
            _min = Double.MIN_VALUE;
            _max = Double.MAX_VALUE;
        }
    }

    public void testDouble() throws Exception
    {
        println("\nTestJsonReaderWriter.testDouble()");
        TestDouble test = new TestDouble();
        String json = getJsonString(test);
        println("json = " + json);
        TestDouble that = (TestDouble) readJsonObject(json);

        assertTrue(that._arrayElement.equals(-1.0));
        assertTrue(that._polyRefTarget.equals(71.0));
        assertTrue(that._polyRef.equals(71.0));
        assertTrue(that._polyNotRef.equals(71.0));
        assertTrue(that._polyRef != that._polyRefTarget);   // Primitive wrappers are treated like primitives (no ref)
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[1] instanceof Double);
        assertTrue(that._typeArray[1] instanceof Double);
        assertTrue(that._typeArray[1].equals(44.0));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[1] instanceof Double);
        assertTrue(that._objArray[1].equals(69.0));
        assertTrue(that._polyRefTarget instanceof Double);
        assertTrue(that._polyNotRef instanceof Double);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Double.MIN_VALUE));
        assertTrue(that._max.equals(Double.MAX_VALUE));
        assertTrue(that._min == Double.MIN_VALUE);
        assertTrue(that._max == Double.MAX_VALUE);
        time(test);
    }

    private static class TestFloat implements Serializable
    {
        private final Float _arrayElement;
        private final Float[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Float _min;
        private final Float _max;
        private final Float _null;

        private TestFloat()
        {
            _arrayElement = new Float(-1);
            _polyRefTarget = new Float(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Float(71);
            Float local = new Float(75);
            _null = null;
            _typeArray = new Float[] {_arrayElement, 44f, local, _null, null, new Float(44f)};
            _objArray = new Object[] {_arrayElement, 69f, local, _null, null, new Float(69f)};
            _min = Float.MIN_VALUE;
            _max = Float.MAX_VALUE;
        }
    }

    public void testFloat() throws Exception
    {
        println("\nTestJsonReaderWriter.testFloat()");
        TestFloat test = new TestFloat();
        String json = getJsonString(test);
        println("json = " + json);
        TestFloat that = (TestFloat) readJsonObject(json);

        assertTrue(that._arrayElement.equals(-1.0f));
        assertTrue(that._polyRefTarget.equals(71.0f));
        assertTrue(that._polyRef.equals(71.0f));
        assertTrue(that._polyNotRef.equals(71.0f));
        assertTrue(that._polyRef != that._polyRefTarget);   // Primitive wrappers are treated like primitives (no ref)
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[1] instanceof Float);
        assertTrue(that._typeArray[1] instanceof Float);
        assertTrue(that._typeArray[1].equals(44.0f));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[1] instanceof Float);
        assertTrue(that._objArray[1].equals(69.0f));
        assertTrue(that._polyRefTarget instanceof Float);
        assertTrue(that._polyNotRef instanceof Float);

        assertTrue(that._objArray[2].equals(that._typeArray[2]));
        assertFalse(that._typeArray[1] == that._typeArray[5]);
        assertFalse(that._objArray[1] == that._objArray[5]);
        assertFalse(that._typeArray[1] == that._objArray[1]);
        assertFalse(that._typeArray[5] == that._objArray[5]);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Float.MIN_VALUE));
        assertTrue(that._max.equals(Float.MAX_VALUE));
        assertTrue(that._min == Float.MIN_VALUE);
        assertTrue(that._max == Float.MAX_VALUE);
        time(test);
    }

    private static class TestBoolean implements Serializable
    {
        private final Boolean _arrayElement;
        private final Boolean[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Boolean _null;

        private TestBoolean()
        {
            _arrayElement = new Boolean(true);
            _polyRefTarget = new Boolean(true);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Boolean(true);
            Boolean local = new Boolean(true);
            _null = null;
            _typeArray = new Boolean[] {_arrayElement, true, local, _null, null, Boolean.FALSE, new Boolean(false)};
            _objArray = new Object[] {_arrayElement, true, local, _null, null, Boolean.FALSE, new Boolean(false) };
        }
    }

    public void testBoolean() throws Exception
    {
        println("\nTestJsonReaderWriter.testBoolean()");
        TestBoolean test = new TestBoolean();
        String json = getJsonString(test);
        println("json = " + json);
        TestBoolean that = (TestBoolean) readJsonObject(json);

        assertTrue(that._arrayElement.equals(true));
        assertTrue(that._polyRefTarget.equals(true));
        assertTrue(that._polyRef.equals(true));
        assertTrue(that._polyNotRef.equals(true));
        assertTrue(that._polyRef == that._polyRefTarget);
        assertTrue(that._polyNotRef == that._polyRef); // because only Boolean.TRUE or Boolean.FALSE used

        assertTrue(that._typeArray.length == 7);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Boolean);
        assertTrue(that._typeArray[1] instanceof Boolean);
        assertTrue(that._typeArray[1].equals(true));
        assertTrue(that._objArray.length == 7);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Boolean);
        assertTrue(that._objArray[1].equals(true));
        assertTrue(that._polyRefTarget instanceof Boolean);
        assertTrue(that._polyNotRef instanceof Boolean);

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._typeArray[5] == that._objArray[5]);
        assertTrue(that._typeArray[6] == that._objArray[6]);
        assertTrue(that._typeArray[5] == that._typeArray[6]);
        assertTrue(that._objArray[5] == that._objArray[6]);

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);
        time(test);
    }

    private static class TestCharacter implements Serializable
    {
        private final Character _arrayElement;
        private final Character[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Character _min;
        private final Character _max;
        private final Character _null;

        private TestCharacter()
        {
            _arrayElement = new Character((char) 1);
            _polyRefTarget = new Character((char)71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Character((char) 71);
            Character local = new Character((char)75);
            _null  = null;
            _typeArray = new Character[] {_arrayElement, 'a', local, _null, null};
            _objArray = new Object[] {_arrayElement, 'b', local, _null, null};
            _min = Character.MIN_VALUE;
            _max = Character.MAX_VALUE;
        }
    }

    public void testCharacter() throws Exception
    {
        println("\nTestJsonReaderWriter.testCharacter()");
        TestCharacter test = new TestCharacter();
        String json = getJsonString(test);
        println("json = " + json);
        TestCharacter that = (TestCharacter) readJsonObject(json);

        assertTrue(that._arrayElement.equals((char) 1));
        assertTrue(that._polyRefTarget.equals((char) 71));
        assertTrue(that._polyRef.equals((char) 71));
        assertTrue(that._polyNotRef.equals((char) 71));
        assertTrue(that._polyRef == that._polyRefTarget);
        assertTrue (that._polyNotRef == that._polyRef);    // Character cache working

        assertTrue(that._typeArray.length == 5);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Character);
        assertTrue(that._typeArray[1] instanceof Character);
        assertTrue(that._typeArray[1].equals('a'));
        assertTrue(that._objArray.length == 5);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Character);
        assertTrue(that._objArray[1].equals('b'));
        assertTrue(that._polyRefTarget instanceof Character);
        assertTrue(that._polyNotRef instanceof Character);

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._objArray[2].equals((char) 75));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(Character.MIN_VALUE));
        assertTrue(that._max.equals(Character.MAX_VALUE));
        assertTrue(that._min == Character.MIN_VALUE);
        assertTrue(that._max == Character.MAX_VALUE);
        time(test);
    }

    private static class TestString implements Serializable
    {
        private static final int MAX_UTF8_CHAR = 100;
        // Foreign characters test (UTF8 multi-byte chars)
        private final String _range;
        private String _utf8HandBuilt;
        private final String[] _strArray;
        private final Object[] _objArray;
        private final Object[] _objStrArray;
        private final Object[] _cache;
        private final Object _poly;
        private final String _null;

        private TestString()
        {
            _null = null;
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < MAX_UTF8_CHAR; i++)
            {
                s.append((char) i);
            }
            _range = s.toString();

            // BYZANTINE MUSICAL SYMBOL PSILI
            try
            {
                byte[] symbol = {(byte) 0xf0, (byte) 0x9d, (byte) 0x80, (byte) 0x80};
                _utf8HandBuilt = new String(symbol, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                System.out.println("Get a new JVM that supports UTF-8");
            }

            _strArray = new String[] {"1st", "2nd", _null, null, new String("3rd")};
            _objArray = new Object[] {"1st", "2nd", _null, null, new String("3rd")};
            _objStrArray = new String[] {"1st", "2nd", _null, null, new String("3rd")};
            _cache = new Object[] {"true", "true", "golf", "golf"};
            _poly = "Poly";
        }
    }

    public void testString() throws Exception
    {
        println("\nTestJsonReaderWriter.testString()");
        TestString test = new TestString();
        String jsonOut = getJsonString(test);
        println("json=" + jsonOut);
        TestString that = (TestString) readJsonObject(jsonOut);

        for (int i = 0; i < TestString.MAX_UTF8_CHAR; i++)
        {
            assertTrue(that._range.charAt(i) == (char) i);
        }

        // UTF-8 serialization makes it through clean.
        byte[] bytes = that._utf8HandBuilt.getBytes("UTF-8");
        assertTrue(bytes[0] == (byte) 0xf0);
        assertTrue(bytes[1] == (byte) 0x9d);
        assertTrue(bytes[2] == (byte) 0x80);
        assertTrue(bytes[3] == (byte) 0x80);

        assertTrue(that._strArray.length == 5);
        assertTrue(that._objArray.length == 5);
        assertTrue(that._objStrArray.length == 5);
        assertTrue(that._strArray[2] == null);
        assertTrue(that._objArray[2] == null);
        assertTrue(that._objStrArray[2] == null);
        assertTrue(that._strArray[3] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objStrArray[3] == null);
        assertTrue("Poly".equals(that._poly));

        assertTrue(that._cache[0] == that._cache[1]);       // "true' is part of the reusable cache.

        time(test);
    }

    public void testRootString() throws Exception
    {
        println("\nTestJsonReaderWriter.testRootString()");
        String s = "root string";
        String json = JsonWriter.objectToJson(s);
        println("json = " + json);
        Object o = JsonReader.jsonToJava(json);
        assertTrue(o instanceof String);
        assertTrue("root string".equals(o));
    }

    public void testObjectArrayStringReference() throws Exception
    {
        println("\nTestJsonReaderWriter.testObjectArrayStringReference()");
        String s = "dogs";
        String json = JsonWriter.objectToJson(new Object[] {s, s});
        println("json = " + json);
        Object[] o = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(o.length == 2);
        assertTrue("dogs".equals(o[0]));
    }

    public void testStringArrayStringReference() throws Exception
    {
        println("\nTestJsonReaderWriter.testStringArrayStringReference()");
        String s = "dogs";
        String json = JsonWriter.objectToJson(new String[] {s, s});
        println("json = " + json);
        String[] o = (String[]) JsonReader.jsonToJava(json);
        assertTrue(o.length == 2);
        assertTrue("dogs".equals(o[0]));
        assertTrue(o[0] != o[1]);   // Change this to == if we decide to collapse identical String instances
    }

    private static class TestDate implements Serializable
    {
        private final Date _arrayElement;
        private final Date[] _typeArray;
        private final Object[] _objArray;
        private final Object _polyRefTarget;
        private final Object _polyRef;
        private final Object _polyNotRef;
        private final Date _min;
        private final Date _max;
        private final Date _null;

        private TestDate()
        {
            _arrayElement = new Date(-1);
            _polyRefTarget = new Date(71);
            _polyRef = _polyRefTarget;
            _polyNotRef = new Date(71);
            Date local = new Date(75);
            _null  = null;
            _typeArray = new Date[] {_arrayElement, new Date(69), local, _null, null, new Date(69)};
            _objArray = new Object[] {_arrayElement, new Date(69), local, _null, null, new Date(69)};
            _min = new Date(Long.MIN_VALUE);
            _max = new Date(Long.MAX_VALUE);
        }
    }

    public void testDate() throws Exception
    {
        println("\nTestJsonReaderWriter.testDate()");
        TestDate test = new TestDate();
        String json = getJsonString(test);
        println("json = " + json);
        TestDate that = (TestDate) JsonReader.jsonToJava(json);

        assertTrue(that._arrayElement.equals(new Date(-1)));
        assertTrue(that._polyRefTarget.equals(new Date(71)));
        assertTrue(that._polyRef.equals(new Date(71)));
        assertTrue(that._polyNotRef.equals(new Date(71)));
        assertTrue(that._polyRef == that._polyRefTarget);
        assertFalse(that._polyNotRef == that._polyRef);

        assertTrue(that._typeArray.length == 6);
        assertTrue(that._typeArray[0] == that._arrayElement);
        assertTrue(that._typeArray[1] instanceof Date);
        assertTrue(that._typeArray[1] instanceof Date);
        assertTrue(that._typeArray[1].equals(new Date(69)));
        assertTrue(that._objArray.length == 6);
        assertTrue(that._objArray[0] == that._arrayElement);
        assertTrue(that._objArray[1] instanceof Date);
        assertTrue(that._objArray[1].equals(new Date(69)));
        assertTrue(that._polyRefTarget instanceof Date);
        assertTrue(that._polyNotRef instanceof Date);
        assertTrue(that._typeArray[1] != that._typeArray[5]);
        assertTrue(that._objArray[1] != that._objArray[5]);
        assertTrue(that._typeArray[1] != that._objArray[1]);
        assertTrue(that._typeArray[5] != that._objArray[5]);

        assertTrue(that._objArray[2] == that._typeArray[2]);
        assertTrue(that._objArray[2].equals(new Date(75)));

        assertTrue(that._null == null);
        assertTrue(that._typeArray[3] == null);
        assertTrue(that._typeArray[4] == null);
        assertTrue(that._objArray[3] == null);
        assertTrue(that._objArray[4] == null);

        assertTrue(that._min.equals(new Date(Long.MIN_VALUE)));
        assertTrue(that._max.equals(new Date(Long.MAX_VALUE)));
        time(test);
    }

    private static class TestClass implements Serializable
    {
        private List _classes_a;

        private final Class _booleanClass;
        private final Class _BooleanClass;
        private final Object _booleanClassO;
        private final Object _BooleanClassO;
        private final Class[] _booleanClassArray;
        private final Class[] _BooleanClassArray;
        private final Object[] _booleanClassArrayO;
        private final Object[] _BooleanClassArrayO;

        private final Class _charClass;
        private final Class _CharacterClass;
        private final Object _charClassO;
        private final Object _CharacterClassO;
        private final Class[] _charClassArray;
        private final Class[] _CharacterClassArray;
        private final Object[] _charClassArrayO;
        private final Object[] _CharacterClassArrayO;

        private TestClass()
        {
            _classes_a = new ArrayList();
            _classes_a.add(char.class);
            _booleanClass = boolean.class;
            _BooleanClass = Boolean.class;
            _booleanClassO = boolean.class;
            _BooleanClassO = Boolean.class;
            _booleanClassArray = new Class[] { boolean.class };
            _BooleanClassArray = new Class[] { Boolean.class };
            _booleanClassArrayO = new Object[] { boolean.class };
            _BooleanClassArrayO = new Object[] { Boolean.class };

            _charClass = char.class;
            _CharacterClass = Character.class;
            _charClassO = char.class;
            _CharacterClassO = Character.class;
            _charClassArray = new Class[] { char.class };
            _CharacterClassArray = new Class[] { Character.class };
            _charClassArrayO = new Object[] { char.class };
            _CharacterClassArrayO = new Object[] { Character.class };
        }
    }

    public void testClassAtRoot() throws Exception
    {
        Class c = Double.class;
        String json = getJsonString(c);
        println("json=" + json);
        Class r = (Class) readJsonObject(json);
        assertTrue(c.getName().equals(r.getName()));
    }

    public void testClass() throws Exception
    {
        println("\nTestJsonReaderWriter.testClass()");
        TestClass test = new TestClass();
        String json = getJsonString(test);
        println("json = " + json);
        TestClass that = (TestClass) readJsonObject(json);

        assertTrue(that._classes_a.get(0) == char.class);

        assertTrue(boolean.class == that._booleanClass);
        assertTrue(Boolean.class == that._BooleanClass);
        assertTrue(boolean.class == that._booleanClassO);
        assertTrue(Boolean.class == that._BooleanClassO);
        assertTrue(boolean.class == that._booleanClassArray[0]);
        assertTrue(Boolean.class == that._BooleanClassArray[0]);
        assertTrue(boolean.class == that._booleanClassArrayO[0]);
        assertTrue(Boolean.class == that._BooleanClassArrayO[0]);

        assertTrue(char.class == that._charClass);
        assertTrue(Character.class == that._CharacterClass);
        assertTrue(char.class == that._charClassO);
        assertTrue(Character.class == that._CharacterClassO);
        assertTrue(char.class == that._charClassArray[0]);
        assertTrue(Character.class == that._CharacterClassArray[0]);
        assertTrue(char.class == that._charClassArrayO[0]);
        assertTrue(Character.class == that._CharacterClassArrayO[0]);
        time(test);
    }

    private static class TestSet implements Serializable
    {
        private Set _hashSet;
        private Set _treeSet;

        private void init()
        {
            _hashSet = new HashSet();
            _hashSet.add("alpha");
            _hashSet.add("bravo");
            _hashSet.add("charlie");
            _hashSet.add("delta");
            _hashSet.add("echo");
            _hashSet.add("foxtrot");
            _hashSet.add("golf");
            _hashSet.add("hotel");
            _hashSet.add("indigo");
            _hashSet.add("juliet");
            _hashSet.add("kilo");
            _hashSet.add("lima");
            _hashSet.add("mike");
            _hashSet.add("november");
            _hashSet.add("oscar");
            _hashSet.add("papa");
            _hashSet.add("quebec");
            _hashSet.add("romeo");
            _hashSet.add("sierra");
            _hashSet.add("tango");
            _hashSet.add("uniform");
            _hashSet.add("victor");
            _hashSet.add("whiskey");
            _hashSet.add("xray");
            _hashSet.add("yankee");
            _hashSet.add("zulu");

            _treeSet = new TreeSet();
            _treeSet.addAll(_hashSet);
        }

        private TestSet()
        {
        }
    }

    public void testSet() throws Exception
    {
        println("\nTestJsonReaderWriter.testSet()");
        TestSet set = new TestSet();
        set.init();
        String json = getJsonString(set);

        TestSet testSet = (TestSet) readJsonObject(json);
        println("json = " + json);

        assertTrue(testSet._treeSet.size() == 26);
        assertTrue(testSet._hashSet.size() == 26);
        assertTrue(testSet._treeSet.containsAll(testSet._hashSet));
        assertTrue(testSet._hashSet.containsAll(testSet._treeSet));

        time(testSet);
    }

    public void testMap2() throws Exception
    {
        println("\nTestJsonReaderWriter.testMap2()");
        TestObject a = new TestObject("A");
        TestObject b = new TestObject("B");
        TestObject c = new TestObject("C");
        a._other = b;
        b._other = c;
        c._other = a;

        Map map = new HashMap();
        map.put(a, b);
        String json = getJsonString(map);
        println("json = " + json);
        map = (Map) readJsonObject(json);
        assertTrue(map != null);
        assertTrue(map.size() == 1);
        TestObject bb = (TestObject) map.get(new TestObject("A"));
        assertTrue(bb._other.equals(new TestObject("C")));
        TestObject aa = (TestObject) map.keySet().toArray()[0];
        assertTrue(aa._other == bb);
    }

    public void testMap3() throws Exception
    {
        println("\nTestJsonReaderWriter.testMap3()");
        Map map = new HashMap();
        map.put("a", "b");
        String json = getJsonString(map);
        println("json = " + json);
        map = (Map) readJsonObject(json);
        assertTrue(map != null);
        assertTrue(map.size() == 1);
    }

    public void testCustom() throws Exception
    {

    }

    public class A
    {
        public String a;

        class B
        {

            public String b;

            public B()
            {
                // No args constructor for B
            }
        }
    }

    public void testInner() throws Exception
    {
        println("\nTestJsonReaderWriter.testInner()");
        A a = new A();
        a.a = "aaa";

        String json = getJsonString(a);
        println("json = " + json);
        A o1 = (A) readJsonObject(json);
        assertTrue(o1.a.equals("aaa"));

        TestJsonReaderWriter.A.B b = a.new B();
        b.b = "bbb";
        json = getJsonString(b);
        println("json = " + json);
        TestJsonReaderWriter.A.B o2 = (TestJsonReaderWriter.A.B) readJsonObject(json);
        assertTrue(o2.b.equals("bbb"));
    }

    public static class TestCalendar implements Serializable
    {
        private Calendar _cal;
        private GregorianCalendar _greg;
    }

    public void testCalendarAsField() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarAsField()");
        Calendar greg = new GregorianCalendar();
        greg.setTimeZone(TimeZone.getTimeZone("PST"));
        greg.set(1965, 11, 17, 14, 30, 16);
        TestCalendar tc = new TestCalendar();
        tc._greg = (GregorianCalendar) greg;
        Calendar now = Calendar.getInstance();
        tc._cal = now;
        String json = JsonWriter.toJson(tc);
        println("json=" + json);

        tc = (TestCalendar) JsonReader.jsonToJava(json);
        assertTrue(now.equals(tc._cal));
        assertTrue(greg.equals(tc._greg));
    }

    public void testCalendarTypedArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarTypedArray()");
        GregorianCalendar[] gregs = new GregorianCalendar[] {new GregorianCalendar()};
        String json = getJsonString(gregs);
        println("json=" + json);
        GregorianCalendar[] gregs2 = (GregorianCalendar[]) JsonReader.jsonToJava(json);
        assertTrue(gregs2[0].equals(gregs[0]));
    }

    public void testCalendarUntypedArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarUntypedArray()");
        Calendar estCal = (Calendar) JsonReader.jsonToJava("{\"@type\":\"java.util.GregorianCalendar\",\"time\":\"1965-12-17T09:30:16.623-0500\",\"zone\":\"EST\"}");
        Calendar utcCal = (Calendar) JsonReader.jsonToJava("{\"@type\":\"java.util.GregorianCalendar\",\"time\":\"1965-12-17T14:30:16.623-0000\"}");
        String json = getJsonString(new Object[]{estCal, utcCal});
        println("json=" + json);
        Object[] oa = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oa.length == 2);
        assertTrue((oa[0]).equals(estCal));
        assertTrue((oa[1]).equals(utcCal));
    }

    public void testCalendarCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarCollection()");
        List gregs = new ArrayList();
        gregs.add(new GregorianCalendar());
        String json = getJsonString(gregs);
        println("json=" + json);
        List gregs2 = (List) JsonReader.jsonToJava(json);
        assertTrue(gregs2.size() == 1);
        assertTrue(gregs2.get(0).equals(gregs.get(0)));
    }

    public void testCalendarInMapValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarInMapValue()");
        Calendar now = Calendar.getInstance();
        Map map = new HashMap();
        map.put("c", now);
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);

        Calendar cal = (Calendar) map.get("c");
        assertTrue(cal.equals(now));
    }

    public void testCalendarInMapKey() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarInMapKey()");
        Calendar now = Calendar.getInstance();
        Map map = new HashMap();
        map.put(now, "c");
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);

        Iterator i = map.keySet().iterator();
        Calendar cal = (Calendar) i.next();
        assertTrue(cal.equals(now));
    }

    public void testCalendarInMapofMaps() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarInMapOfMaps()");
        Calendar now = Calendar.getInstance();
        String json = JsonWriter.objectToJson(new Object[] {now});
        println("json=" + json);

        Map map = JsonReader.toMaps(json);
        Object[] items = (Object[]) map.get("@items");
        Map item = (Map) items[0];
        assertTrue(item.containsKey("time"));
        assertTrue(item.containsKey("zone"));
    }

    public void testBadCalendar() throws Exception
    {
        try
        {
            JsonReader.jsonToJava("{\"@type\":\"java.util.GregorianCalendar\",\"time\":\"2011-12-08X13:29:58.822-0500\",\"zone\":\"bad zone\"}");
            assertTrue("should not make it here.", false);
        }
        catch(Exception e) { }
    }

    public void testCalendar() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendar()");
        Calendar greg = new GregorianCalendar();
        greg.setTimeZone(TimeZone.getTimeZone("PST"));
        greg.set(1965, 11, 17, 14, 30, 16);
        String json = getJsonString(greg);
        println("json = " + json);

        Calendar cal = (Calendar) readJsonObject(json);
        assertTrue(cal.equals(greg));

        time(greg);

        greg = new GregorianCalendar();
        greg.setTimeZone(TimeZone.getTimeZone("EST"));
        greg.set(2011, 11, 8, 13, 29, 48);
        json = getJsonString(greg);
        println("json=" + json);

        Calendar[] cals = new Calendar[] { greg } ;
        json = getJsonString(cals);
        println("json=" + json);
        cals = (Calendar[]) readJsonObject(json);
        assertTrue(cals[0].equals(greg));
        println("json=" + json);

        TestCalendar testCal = new TestCalendar();
        testCal._cal = cal;
        testCal._greg = (GregorianCalendar) greg;
        json = getJsonString(testCal);
        println("json=" + json);

        testCal = (TestCalendar) readJsonObject(json);
        assertTrue(testCal._cal.equals(cal));
        assertTrue(testCal._greg.equals(greg));

        Calendar estCal = (Calendar) readJsonObject("{\"@type\":\"java.util.GregorianCalendar\",\"time\":\"1965-12-17T09:30:16.623-0500\"}");
        Calendar utcCal = (Calendar) readJsonObject("{\"@type\":\"java.util.GregorianCalendar\",\"time\":\"1965-12-17T14:30:16.623-0000\"}");
        assertTrue(estCal.equals(utcCal));

        json = getJsonString(new Object[]{estCal, utcCal});
        Object[] oa = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oa.length == 2);
        assertTrue((oa[0]).equals(estCal));
        assertTrue((oa[1]).equals(utcCal));
    }

    public static class TestTimeZone implements Serializable
    {
        private TimeZone _zone;
    }

    public void testTimeZoneAsField() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneAsField()");
        TimeZone zone = TimeZone.getDefault();
        TestTimeZone tz = new TestTimeZone();
        tz._zone = zone;
        String json = JsonWriter.toJson(tz);
        println("json=" + json);

        tz = (TestTimeZone) JsonReader.jsonToJava(json);
        assertTrue(zone.equals(tz._zone));
    }

    public void testTimeZone() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZone()");
        TimeZone est = TimeZone.getTimeZone("EST");
        String json = JsonWriter.objectToJson(est);
        println("json=" + json);
        TimeZone tz = (TimeZone) JsonReader.jsonToJava(json);
        assertTrue(tz.equals(est));

        TimeZone pst = TimeZone.getTimeZone("PST");
        json = JsonWriter.objectToJson(pst);
        println("json=" + json);
        tz = (TimeZone) JsonReader.jsonToJava(json);
        assertTrue(tz.equals(pst));

        try
        {
            String noZone = "{\"@type\":\"sun.util.calendar.ZoneInfo\"}";
            JsonReader.jsonToJava(noZone);
            assertTrue("Should not reach this point.", false);
        }
        catch(Exception e) {}
    }

    public void testTimeZoneInArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneInArray()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        String json = JsonWriter.objectToJson(new Object[] {pst});
        println("json=" + json);

        Object[] oArray = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oArray.length == 1);
        TimeZone tz = (TimeZone)oArray[0];
        assertTrue(tz.equals(pst));

        json = JsonWriter.objectToJson(new TimeZone[] {pst});
        println("json=" + json);

        Object[] tzArray = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(tzArray.length == 1);
        tz = (TimeZone)tzArray[0];
        assertTrue(tz.equals(pst));
    }

    public void testTimeZoneInCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneInCollection()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        List col = new ArrayList();
        col.add(pst);
        String json = JsonWriter.objectToJson(col);
        println("json=" + json);

        col = (List) JsonReader.jsonToJava(json);
        assertTrue(col.size() == 1);
        TimeZone tz = (TimeZone) col.get(0);
        assertTrue(tz.equals(pst));
    }

    public void testTimeZoneInMapValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneInMapValue()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        Map map = new HashMap();
        map.put("p", pst);
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);

        TimeZone tz = (TimeZone) map.get("p");
        assertTrue(tz.equals(pst));
    }

    public void testTimeZoneInMapKey() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneInMapKey()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        Map map = new HashMap();
        map.put(pst, "p");
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);

        Iterator i = map.keySet().iterator();
        TimeZone tz = (TimeZone) i.next();
        assertTrue(tz.equals(pst));
    }

    public void testTimeZoneInMapofMaps() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneInMapOfMaps()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        String json = JsonWriter.objectToJson(new Object[] {pst});
        println("json=" + json);

        Map map = JsonReader.toMaps(json);
        Object[] items = (Object[]) map.get("@items");
        Map item = (Map) items[0];
        assertTrue(item.containsKey("zone"));
        assertTrue("PST".equals(item.get("zone")));
    }

    public void testTimeZoneRef() throws Exception
    {
        println("\nTestJsonReaderWriter.testTimeZoneRef()");
        TimeZone pst = TimeZone.getTimeZone("PST");
        String json = JsonWriter.objectToJson(new Object[] {pst, pst});
        println("json=" + json);

        Object[] oArray = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oArray.length == 2);
        TimeZone tz = (TimeZone)oArray[0];
        assertTrue(tz.equals(pst));
        assertTrue(oArray[0] == oArray[1]);
    }

    public void testForwardRefs() throws Exception
    {
        println("\nTestJsonReaderWriter.testForwardRefs()");
        TestObject one = new TestObject("One");
        TestObject two = new TestObject("Two");
        one._other = two;
        two._other = one;
        String pkg = TestObject.class.getName();
        String fwdRef = "[[{\"@id\":2,\"@type\":\"" + pkg + "\",\"_name\":\"Two\",\"_other\":{\"@ref\":1}}],[{\n" + "\"@id\":1,\"@type\":\"" + pkg + "\",\"_name\":\"One\",\"_other\":{\"@ref\":2}}]]";
        Object[] foo = (Object[]) readJsonObject(fwdRef);
        Object[] first = (Object[]) foo[0];
        Object[] second = (Object[]) foo[1];
        assertTrue(one.equals(second[0]));
        assertTrue(two.equals(first[0]));

        String json = "[{\"@ref\":2},{\"@id\":2,\"@type\":\"int\",\"value\":5}]";
        Object[] ints = (Object[]) readJsonObject(json);
        assertTrue((Integer)ints[0] == 5);
        assertTrue((Integer) ints[1] == 5);

        json ="{\"@type\":\"java.util.ArrayList\",\"@items\":[{\"@ref\":2},{\"@id\":2,\"@type\":\"int\",\"value\":5}]}";
        List list = (List) JsonReader.toJava(json);
        assertTrue((Integer)list.get(0) == 5);
        assertTrue((Integer)list.get(1) == 5);

        json = "{\"@type\":\"java.util.TreeSet\",\"@items\":[{\"@type\":\"int\",\"value\":9},{\"@ref\":16},{\"@type\":\"int\",\"value\":4},{\"@id\":16,\"@type\":\"int\",\"value\":5}]}";
        Set set = (Set) readJsonObject(json);
        assertTrue(set.size() == 3);
        Iterator i = set.iterator();
        assertTrue((Integer)i.next() == 4);
        assertTrue((Integer)i.next() == 5);
        assertTrue((Integer)i.next() == 9);

        json = "{\"@type\":\"java.util.HashMap\",\"@keys\":[1,2,3,4],\n" + "\"@items\":[{\"@type\":\"int\",\"value\":9},{\"@ref\":16},{\"@type\":\"int\",\"value\":4},{\"@id\":16,\"@type\":\"int\",\"value\":5}]}";
        Map map = (Map) readJsonObject(json);
        assertTrue(map.size() == 4);
        assertTrue((Integer)map.get(1L) == 9);
        assertTrue((Integer)map.get(2L) == 5);
        assertTrue((Integer)map.get(3L) == 4);
        assertTrue((Integer)map.get(4L) == 5);
    }

    public void testNull() throws Exception
    {
        println("\nTestJsonReaderWriter.testNull()");
        String json = JsonWriter.toJson(null);
        println("json=" + json);
        assertTrue("null".equals(json));
    }

    /**
     * Although a String cannot really be a root in JSON, 
     * json-io returns the JSON utf8 string.  This test
     * exists to catch if this decision ever changes.
     */
    public void testStringRoot() throws Exception
    {
        println("\nTestJsonReaderWriter.testStringRoot()");
        String json = JsonWriter.toJson("This is a string");
        println("json=" + json);
        assertTrue("{\"@type\":\"string\",\"value\":\"This is a string\"}".equals(json));
        Object o = JsonReader.toJava(json);
        assertTrue("This is a string".equals(o));

        o = JsonReader.jsonToJava('[' + json + ']');
        assertTrue(o.getClass().equals(Object[].class));
        Object[] oa = (Object[]) o;
        assertTrue(oa.length == 1);
        assertTrue(oa[0].equals("This is a string"));
    }

    public void testReferencedEmptyArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testReferencedEmptyArray()");
        String[] array = new String[]{};
        Object[] refArray = new Object[]{array};
        String json = JsonWriter.toJson(refArray);
        println("json=" + json);
        Object[] oa = (Object[]) JsonReader.toJava(json);
        assertTrue(oa[0].getClass().equals(String[].class));
        assertTrue(((String[])oa[0]).length == 0);
    }

    public void testBadJson() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadJson()");
        Object o = JsonReader.toJava("[\"bad JSON input\"");
        assertTrue(o == null);

        try
        {
            o = JsonReader.jsonToJava("[\"bad JSON input\"");
            assertTrue("Should have received exception", false);
        }
        catch(Exception ignored)
        { }
    }

    public void testToMaps() throws Exception
    {
        println("\nTestJsonReaderWriter.testToMaps()");
        Map map = JsonReader.jsonToMaps("{\"num\":0,\"nullValue\":null,\"string\":\"yo\"}");
        assertTrue(map != null);
        assertTrue(map.size() == 3);
        assertTrue(map.get("num").equals(0L));
        assertTrue(map.get("nullValue") == null);
        assertTrue(map.get("string").equals("yo"));
    }

    public void testCollectionWithEmptyElement() throws Exception
    {
        println("\nTestJsonReaderWriter.testCollectionWithEmptyElement()");
        List list = new ArrayList();
        list.add("a");
        list.add(null);
        list.add("b");
        String json = getJsonString(list);
        List list2 = (List) JsonReader.toJava(json);
        assertTrue(list.equals(list2));

        json = "{\"@type\":\"java.util.ArrayList\",\"@items\":[\"a\",{},\"b\"]}";
        list2 = (List) JsonReader.toJava(json);
        assertTrue(list2.size() == 3);
        assertTrue(list2.get(0).equals("a"));
        assertTrue(list2.get(1).getClass().equals(JsonObject.class));
        assertTrue(list2.get(2).equals("b"));
    }

    public void testCollectionWithReferences() throws Exception
    {
        println("\nTestJsonReaderWriter.testCollectionWithReferences()");
        TestObject o = new TestObject("JSON");
        List list = new ArrayList();
        list.add(o);
        list.add(o);

        // Backward reference
        String json = getJsonString(list);
        println("json=" + json);
        List list2 = (List) JsonReader.toJava(json);
        assertTrue(list.equals(list2));

        // Forward reference
        String pkg = TestObject.class.getName();
        json = "{\"@type\":\"java.util.ArrayList\",\"@items\":[{\"@ref\":3},{\"@id\":3,\"@type\":\"" + pkg + "\",\"_name\":\"JSON\",\"_other\":null}]}";
        list2 = (List) JsonReader.toJava(json);
        assertTrue(list.equals(list2));
    }

    public void testEmptyArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testEmptyArray()");
        String json = "{\"@type\":\"[Ljava.lang.String;\"}";
        String[] s = (String[])JsonReader.toJava(json);
        assertTrue(s != null);
        assertTrue(s.length == 0);
    }

    public static class TestLocale implements Serializable
    {
        private Locale _loc;
    }

    public void testLocaleAsField() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleAsField()");
        Locale locale = Locale.getDefault();
        TestLocale tl = new TestLocale();
        tl._loc = locale;
        String json = JsonWriter.toJson(tl);
        println("json=" + json);

        tl = (TestLocale) JsonReader.jsonToJava(json);
        assertTrue(locale.equals(tl._loc));
    }

    public void testLocale() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocale()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        String json = JsonWriter.objectToJson(locale);
        println("json=" + json);
        Locale us = (Locale) JsonReader.jsonToJava(json);
        assertTrue(locale.equals(us));

        locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry(), "johnson");
        json = JsonWriter.objectToJson(locale);
        println("json=" + json);
        us = (Locale) JsonReader.jsonToJava(json);
        assertTrue(locale.equals(us));

        try
        {
            String noProps = "{\"@type\":\"java.util.Locale\"}";
            JsonReader.jsonToJava(noProps);
            assertTrue("Should never get here.", false);
        }
        catch(Exception e) {}

        json = "{\"@type\":\"java.util.Locale\",\"language\":\"en\"}";
        locale = (Locale) JsonReader.jsonToJava(json);
        assertTrue("en".equals(locale.getLanguage()));
        assertTrue("".equals(locale.getCountry()));
        assertTrue("".equals(locale.getVariant()));

        json = "{\"@type\":\"java.util.Locale\",\"language\":\"en\",\"country\":\"US\"}";
        locale = (Locale) JsonReader.jsonToJava(json);
        assertTrue("en".equals(locale.getLanguage()));
        assertTrue("US".equals(locale.getCountry()));
        assertTrue("".equals(locale.getVariant()));
    }

    public void testLocaleArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleArray()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        String json = JsonWriter.objectToJson(new Object[] {locale});
        println("json=" + json);
        Object[] oArray = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oArray.length == 1);
        Locale us = (Locale) oArray[0];
        assertTrue(locale.equals(us));

        json = JsonWriter.objectToJson(new Locale[] {locale});
        println("json=" + json);
        Locale[] lArray = (Locale[]) JsonReader.jsonToJava(json);
        assertTrue(lArray.length == 1);
        us = lArray[0];
        assertTrue(locale.equals(us));
    }

    public void testLocaleInCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleInCollection()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        List list = new ArrayList();
        list.add(locale);
        String json = JsonWriter.objectToJson(list);
        println("json=" + json);
        list = (List) JsonReader.jsonToJava(json);
        assertTrue(list.size() == 1);
        assertTrue(list.get(0).equals(locale));
    }

    public void testLocaleInMapValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleInMapValue()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        Map map = new HashMap();
        map.put("us", locale);
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);
        map = (Map) JsonReader.jsonToJava(json);
        assertTrue(map.size() == 1);
        assertTrue(map.get("us").equals(locale));
    }

    public void testLocaleInMapKey() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleInMapKey()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        Map map = new HashMap();
        map.put(locale, "us");
        String json = JsonWriter.objectToJson(map);
        println("json=" + json);
        map = (Map) JsonReader.jsonToJava(json);
        assertTrue(map.size() == 1);
        Iterator i = map.keySet().iterator();
        assertTrue(i.next().equals(locale));
    }

    public void testLocaleInMapOfMaps() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleInMapOfMaps()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        String json = JsonWriter.objectToJson(locale);
        println("json=" + json);
        Map map = JsonReader.toMaps(json);
        assertTrue("en".equals(map.get("language")));
        assertTrue("US".equals(map.get("country")));
    }

    public void testLocaleRef() throws Exception
    {
        println("\nTestJsonReaderWriter.testLocaleForwardRef()");
        Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.US.getCountry());
        String json = JsonWriter.objectToJson(new Object[] {locale, locale});
        println("json=" + json);
        Object[] oArray = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(oArray.length == 2);
        Locale us = (Locale) oArray[0];
        assertTrue(locale.equals(us));
        assertTrue(oArray[0] == oArray[1]);
    }

    public void testEmptyPrimitives() throws Exception
    {
        println("\nTestJsonReaderWriter.testEmptyPrimitives()");
        String json = "{\"@type\":\"byte\"}";
        Byte b = (Byte) JsonReader.toJava(json);
        assertTrue(b.getClass().equals(Byte.class));
        assertTrue(b == 0);

        json = "{\"@type\":\"short\"}";
        Short s = (Short) JsonReader.toJava(json);
        assertTrue(s.getClass().equals(Short.class));
        assertTrue(s == 0);

        json = "{\"@type\":\"int\"}";
        Integer i = (Integer) JsonReader.toJava(json);
        assertTrue(i.getClass().equals(Integer.class));
        assertTrue(i == 0);

        json = "{\"@type\":\"long\"}";
        Long l = (Long) JsonReader.toJava(json);
        assertTrue(l.getClass().equals(Long.class));
        assertTrue(l == 0);

        json = "{\"@type\":\"float\"}";
        Float f = (Float) JsonReader.toJava(json);
        assertTrue(f.getClass().equals(Float.class));
        assertTrue(f == 0.0f);

        json = "{\"@type\":\"double\"}";
        Double d = (Double) JsonReader.toJava(json);
        assertTrue(d.getClass().equals(Double.class));
        assertTrue(d == 0.0d);

        json = "{\"@type\":\"char\"}";
        Character c = (Character) JsonReader.toJava(json);
        assertTrue(c.getClass().equals(Character.class));
        assertTrue(c == '\u0000');

        json = "{\"@type\":\"boolean\"}";
        Boolean bool = (Boolean) JsonReader.toJava(json);
        assertTrue(bool == Boolean.FALSE);

        json = "{\"@type\":\"string\"}";
        String str = (String) JsonReader.toJava(json);
        assertTrue(str == null);
    }

    public void testMalformedJson() throws Exception
    {
        println("\nTestJsonReaderWriter.testMalformedJson()");
        String json = "\"Invalid JSON\"";          // Valid JSON must start with { or [
        try
        {
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 1", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\";0}";  // colon expected between fields
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 2", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{field:0}";  // not quoted field name
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 3", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":0";  // object not terminated correctly (ending in number)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 4", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":true";  // object not terminated correctly (ending in token)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 5", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":\"test\"";  // object not terminated correctly (ending in string)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 6", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":{}";  // object not terminated correctly (ending in another object)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 7", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":[]";  // object not terminated correctly (ending in an array)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 8", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"field\":3.14";  // object not terminated correctly (ending in double precision number)
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 9", false);
        }
        catch (IOException e) { }

        try
        {
            json = "[1,2,3";
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 10", false);
        }
        catch (IOException e)  { }

        try
        {
            json = "[false,true,false";
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 11", false);
        }
        catch (IOException e) { }

        try
        {
            json = "[\"unclosed string]";
            JsonReader.jsonToMaps(json);
            assertTrue("malformed JSON should not parse 12", false);
        }
        catch (IOException e) { }
    }

    public void testBadType() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadType()");
        try
        {
            String json = "{\"@type\":\"non.existent.class.Non\"}";
            JsonReader.jsonToJava(json);
            assertTrue("should not parse", false);
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("instance"));
            assertTrue(e.getMessage().contains("not"));
            assertTrue(e.getMessage().contains("created"));
        }

        // Bad class inside a Collection
        try
        {
            String json = "{\"@type\":\"java.util.ArrayList\",\"@items\":[null, true, {\"@type\":\"bogus.class.Name\"}]}";
            JsonReader.jsonToJava(json);
            assertTrue("should not parse", false);
        }
        catch (IOException e)
        {
            assertTrue(e instanceof IOException);
        }
    }

    public void testOddMaps() throws Exception
    {
        println("\nTestJsonReaderWriter.testOddMaps()");
        String json = "{\"@type\":\"java.util.HashMap\",\"@keys\":null,\"@items\":null}";
        Map map = (Map)JsonReader.jsonToJava(json);
        assertTrue(map instanceof HashMap);
        assertTrue(map.isEmpty());

        json = "{\"@type\":\"java.util.HashMap\"}";
        map = (Map)JsonReader.jsonToJava(json);
        assertTrue(map instanceof HashMap);
        assertTrue(map.isEmpty());

        try
        {
            json = "{\"@type\":\"java.util.HashMap\",\"@keys\":null,\"@items\":[]}";
            map = (Map)JsonReader.jsonToJava(json);
            assertTrue("Should not parse into Java", false);
        }
        catch (IOException e) { }

        try
        {
            json = "{\"@type\":\"java.util.HashMap\",\"@keys\":[1,2],\"@items\":[true]}";
            map = (Map)JsonReader.jsonToJava(json);
            assertTrue("Should not parse into Java", false);
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("different size"));
        }
    }

    public void testBadHexNumber() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadHexNumber()");
        StringBuilder str = new StringBuilder();
        str.append("[\"\\");
        str.append("u000r\"]");
        try
        {
            JsonReader.jsonToJava(str.toString());
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }
    }

    public void testBadValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadValue()");
        try
        {
            String json = "{\"field\":19;}";
            JsonReader.jsonToJava(json);
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }

        try
        {
            String json = "{\"field\":";
            JsonReader.jsonToJava(json);
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }

        try
        {
            String json = "{\"field\":joe";
            JsonReader.jsonToJava(json);
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }

        try
        {
            String json = "{\"field\":trux}";
            JsonReader.jsonToJava(json);
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }

        try
        {
            String json = "{\"field\":tru";
            JsonReader.jsonToJava(json);
            assertTrue("Should not make it here", false);
        }
        catch (IOException e) { }
    }

    public void testStringEscape() throws Exception
    {
        println("\nTestJsonReaderWriter.testStringEscape()");
        String json = "[\"escaped slash \\' should result in a single /\"]";
        JsonReader.jsonToJava(json);

        json = "[\"escaped slash \\/ should result in a single /\"]";
        JsonReader.jsonToJava(json);

        try
        {
            json = "[\"escaped slash \\x should result in a single /\"]";
            JsonReader.jsonToJava(json);
            assertTrue("Should not reach this point", false);
        }
        catch (IOException e) { }
    }

    public void testDateMissingValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testDateMissingValue()");
        try
        {
            JsonReader.jsonToJava("[{\"@type\":\"date\"}]");
            assertTrue("Should not reach this line", false);
        }
        catch (IOException e) { }
    }

    public void testClassMissingValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testClassMissingValue()");
        try
        {
            JsonReader.jsonToJava("[{\"@type\":\"class\"}]");
            assertTrue("Should not reach this line", false);
        }
        catch (IOException e) { }
    }

    public void testCalendarMissingValue() throws Exception
    {
        println("\nTestJsonReaderWriter.testCalendarMissingValue()");
        try
        {
            JsonReader.jsonToJava("[{\"@type\":\"java.util.Calendar\"}]");
            assertTrue("Should not reach this line", false);
        }
        catch (IOException e) { }
    }

    public void testBadFormattedCalendar() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadFormattedCalendar()");
        try
        {
            JsonReader.jsonToJava("[{\"@type\":\"java.util.GregorianCalendar\",\"value\":\"2012-05-03T12:39:45.1X5-0400\"}]");
            assertTrue("Should not reach this line", false);
        }
        catch (IOException e) { }
    }

    public void testEmptyClassName() throws Exception
    {
        println("\nTestJsonReaderWriter.testEmptyClassName()");
        try
        {
            JsonReader.jsonToJava("{\"@type\":\"\"}");
        }
        catch(Exception e) { }
    }

    public void testBadBackRef() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadBackRef()");
        try
        {
            JsonReader.jsonToJava("{\"@type\":\"java.util.ArrayList\",\"@items\":[{\"@ref\":1}]}");
            assertTrue("Should not reach this point", false);
        }
        catch(Exception e) { }
    }

    public void testBadInputForMapAPI() throws Exception
    {
        println("\nTestJsonReaderWriter.testBadInputForMapAPI()");
        Object o = JsonReader.toMaps("[This is not quoted]");
        assertTrue(o == null);
    }

    public void testCollectionWithNonJsonPrimitives() throws Exception
    {
        println("\nTestJsonReaderWriter.testCollectionWithNonJsonPrimitives()");
        Collection col = new ArrayList();
        col.add(new Integer(7));
        col.add(new Short((short) 9));
        col.add(new Float(3.14));
        String json = JsonWriter.objectToJson(col);
        Collection col1 = (Collection) JsonReader.jsonToJava(json);
        assertTrue(col.equals(col1));
    }

    public void testWriterObjectAPI() throws Exception
    {
        println("\nTestJsonReaderWriter.testWriterObjectAPI()");
        String json = "[1,true,null,3.14,[]]";
        Object o = JsonReader.toJava(json);
        assertTrue(JsonWriter.objectToJson(o).equals(json));

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(ba);
        writer.write(o);
        writer.close();
        String s = new String(ba.toByteArray(), "UTF-8");
        assertTrue(json.equals(s));
    }

    public void testUntyped() throws Exception
    {
        println("\nTestJsonReaderWriter.testUntyped()");
        String json ="{\"age\":46,\"name\":\"jack\",\"married\":false,\"salary\":125000.07,\"notes\":null,\"address1\":{\"@ref\":77},\"address2\":{\"@id\":77,\"street\":\"1212 Pennsylvania ave\",\"city\":\"Washington\"}}";
        Map map = JsonReader.jsonToMaps(json);
        println("map=" + map);
        assertTrue(map.get("age").equals(46L));
        assertTrue(map.get("name").equals("jack"));
        assertTrue(map.get("married") == Boolean.FALSE);
        assertTrue(map.get("salary").equals(125000.07d));
        assertTrue(map.get("notes") == null);
        Map address1 = (Map) map.get("address1");
        Map address2 = (Map) map.get("address2");
        assertTrue(address1 == address2);
        assertTrue(address2.get("street").equals("1212 Pennsylvania ave"));
        assertTrue(address2.get("city").equals("Washington"));
    }

    public void testUntypedArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testUntypedArray()");
        Object[] args = (Object[]) readJsonObject("[\"string\",17, null, true, false, [], -1273123,32131, 1e6, 3.14159, -9223372036854775808, 9223372036854775807]");

        for (int i=0; i < args.length; i++)
        {
            println("args[" + i + "]=" + args[i]);
            if (args[i] != null)
            {
                println("args[" + i + "]=" + args[i].getClass().getName());
            }
        }

        assertTrue(args[0].equals("string"));
        assertTrue(args[1].equals(17L));
        assertTrue(args[2] == null);
        assertTrue(args[3].equals(Boolean.TRUE));
        assertTrue(args[4].equals(Boolean.FALSE));
        assertTrue(args[5].getClass().isArray());
        assertTrue(args[6].equals(-1273123L));
        assertTrue(args[7].equals(32131L));
        assertTrue(args[8].equals(new Double(1000000)));
        assertTrue(args[9].equals(new Double(3.14159)));
        assertTrue(args[10].equals(Long.MIN_VALUE));
        assertTrue(args[11].equals(Long.MAX_VALUE));
    }

    public void testUntypedCollections() throws Exception
    {
        println("\nTestJsonReaderWriter.testUntypedCollections()");
        Object[] poly = new Object[] {"Road Runner", 16L, 3.1415, true, false, null, 7, "Coyote", "Coyote"};
        String json = getJsonString(poly);
        println("json=" + json);
        assertTrue("[\"Road Runner\",16,3.1415,true,false,null,{\"@type\":\"int\",\"value\":7},\"Coyote\",\"Coyote\"]".equals(json));
        Collection col = new ArrayList();
        col.add("string");
        col.add(new Long(16));
        col.add(new Double(3.14159));
        col.add(Boolean.TRUE);
        col.add(Boolean.FALSE);
        col.add(null);
        col.add(new Integer(7));
        json = getJsonString(col);
        println("json=" + json);
        assertTrue("{\"@type\":\"java.util.ArrayList\",\"@items\":[\"string\",16,3.14159,true,false,null,{\"@type\":\"int\",\"value\":7}]}".equals(json));
    }

    public void testMapOfMapsSimpleArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testMapOfMapsSimpleArray()");
        String s = "[{\"@ref\":1},{\"name\":\"Jack\",\"age\":21,\"@id\":1}]";
        Map map = JsonReader.jsonToMaps(s);
        Object[] list = (Object[]) map.get("@items");
        assertTrue(list[0] == list[1]);
    }

    public void testMapOfMapsWithFieldAndArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testMapOfMapsWithFieldAndArray()");
        String s = "[\n" +
                " {\"name\":\"Jack\",\"age\":21,\"@id\":1},\n" +
                " {\"@ref\":1},\n" +
                " {\"@ref\":2},\n" +
                " {\"husband\":{\"@ref\":1}},\n" +
                " {\"wife\":{\"@ref\":2}},\n" +
                " {\"attendees\":[{\"@ref\":1},{\"@ref\":2}]},\n" +
                " {\"name\":\"Jill\",\"age\":18,\"@id\":2},\n" +
                " {\"witnesses\":[{\"@ref\":1},{\"@ref\":2}]}\n" +
                "]";

        println("json=" + s);
        Map map = JsonReader.jsonToMaps(s);
        println("map=" + map);
        Object[] items = (Object[]) map.get("@items");
        assertTrue(items.length == 8);
        Map husband = (Map) items[0];
        Map wife = (Map) items[6];
        assertTrue(items[1] == husband);
        assertTrue(items[2] == wife);
        map = (Map) items[3];
        assertTrue(map.get("husband") == husband);
        map = (Map) items[4];
        assertTrue(map.get("wife") == wife);
        map = (Map) items[5];
        map = (Map) map.get("attendees");
        Object[] attendees = (Object[]) map.get("@items");
        assertTrue(attendees.length == 2);
        assertTrue(attendees[0] == husband);
        assertTrue(attendees[1] == wife);
        map = (Map) items[7];
        map = (Map) map.get("witnesses");
        Object[] witnesses = (Object[]) map.get("@items");
        assertTrue(witnesses.length == 2);
        assertTrue(witnesses[0] == husband);
        assertTrue(witnesses[1] == wife);
    }

    public void testMapOfMapsCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testMapOfMapsCollection()");
        List stuff = new ArrayList();
        stuff.add("Hello");
        Object testObj = new TestObject("test object");
        stuff.add(testObj);
        stuff.add(testObj);
        stuff.add(new Date());
        String json = JsonWriter.objectToJson(stuff);
        println("json=" + json);

        Map map = JsonReader.jsonToMaps(json);
        println("map=" + map);
        Object[] items = (Object[]) map.get("@items");
        assertTrue(items.length == 4);
        assertTrue("Hello".equals(items[0]));
        assertTrue(items[1] == items[2]);

        List list = new ArrayList();
        list.add(new Object[] {123L, null, true, "Hello"});
        json = JsonWriter.objectToJson(list);
        println("json=" + json);
        map = JsonReader.jsonToMaps(json);
        items = (Object[]) map.get("@items");
        assertTrue(items.length == 1);
        Object[] oa = (Object[]) items[0];
        assertTrue(oa.length == 4);
        assertTrue(oa[0].equals(123L));
        assertTrue(oa[1] == null);
        assertTrue(oa[2] == Boolean.TRUE);
        assertTrue("Hello".equals(oa[3]));
    }

    public void testMapOfMapsMap() throws Exception
    {
        println("\nTestJsonReaderWriter.testMapOfMapsMap()");
        Map stuff = new TreeMap();
        stuff.put("a", "alpha");
        Object testObj = new TestObject("test object");
        stuff.put("b", testObj);
        stuff.put("c", testObj);
        stuff.put(testObj, 1.0f);
        String json = JsonWriter.objectToJson(stuff);
        println("json=" + json);

        Map map = JsonReader.jsonToMaps(json);
        println("map=" + map);
        Object aa = map.get("a");
        Map bb = (Map) map.get("b");
        Map cc = (Map) map.get("c");

        assertTrue(aa.equals("alpha"));
        assertTrue(bb.get("_name").equals("test object"));
        assertTrue(bb.get("_other") == null);
        assertTrue(bb == cc);
        assertTrue(map.size() == 4);    // contains @type entry
    }

    public void testMapOfMapsPrimitivesInArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testMapOfMapPrimitivesInArray()");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        println("cal=" + cal);
        Class strClass = String.class;
        Object[] prims = new Object[] { true, Boolean.TRUE, (byte)8, (short)1024, 131072, 16777216, 3.14, 3.14f, 'x', "hello", date, cal, strClass};
        String json = JsonWriter.objectToJson(prims);
        println("json=" + json);
        Object[] javaObjs = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(prims.length == javaObjs.length);

        for (int i=0; i < javaObjs.length; i ++)
        {
            assertTrue(javaObjs[i].equals(prims[i]));
        }
    }

    private enum TestEnum1 { A, B, C }

    private enum TestEnum2
    {
        A() {},
        B() {},
        C() {}
    }

    private enum TestEnum3
    {
        A("Foo")
                {
                    void doXX() {}
                },
        B("Bar")
                {
                    void doXX() {}
                },
        C(null)
                {
                    void doXX() {}
                };

        private final String val;

        private TestEnum3(String val)
        {
            this.val = val;
        }

        abstract void doXX();
    }

    public void testEnums() throws Exception
    {
        println("\nTestJsonReaderWriter.testEnums()");

        Collection<Object> collection = new LinkedList<Object>();
        collection.addAll(Arrays.asList(TestEnum1.values()));
        collection.addAll(Arrays.asList(TestEnum2.values()));
        collection.addAll(Arrays.asList(TestEnum3.values()));

        for (Object o : collection)
        {
            String json = JsonWriter.toJson(o);
            println("json=" + json);
            Object read = JsonReader.jsonToJava(json);
            assertTrue(o == read);
        }

        String json = JsonWriter.toJson(collection);
        Collection<Object> collection2 = (Collection<Object>) JsonReader.jsonToJava(json);
        assertTrue(collection.equals(collection2));

        TestEnum1[] array11 = TestEnum1.values();
        json = JsonWriter.toJson(array11);
        println("json=" + json);
        TestEnum1[] array12 = (TestEnum1[]) JsonReader.jsonToJava(json);
        assertTrue(Arrays.equals(array11, array12));

        TestEnum2[] array21 = TestEnum2.values();
        json = JsonWriter.toJson(array21);
        println("json=" + json);
        TestEnum2[] array22 = (TestEnum2[]) JsonReader.jsonToJava(json);
        assertTrue(Arrays.equals(array21, array22));

        TestEnum3[] array31 = TestEnum3.values();
        json = JsonWriter.toJson(array31);
        println("json=" + json);
        TestEnum3[] array32 = (TestEnum3[]) JsonReader.jsonToJava(json);
        assertTrue(Arrays.equals(array31, array32));
    }

    public void testEmptyObject() throws Exception
    {
        println("\nTestJsonReaderWriter.testEmptyObject()");

        Object o = JsonReader.jsonToJava("{}");
        assertTrue(JsonObject.class.equals(o.getClass()));

        Object[] oa = (Object[]) JsonReader.jsonToJava("[{},{}]");
        assertTrue(oa.length == 2);
        assertTrue(Object.class.equals(oa[0].getClass()));
        assertTrue(Object.class.equals(oa[1].getClass()));
    }

    public void testBigInteger() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigInteger()");
        String s = "123456789012345678901234567890";
        BigInteger bigInt = new BigInteger(s);
        String json = JsonWriter.objectToJson(bigInt);
        println("json=" + json);
        bigInt = (BigInteger) JsonReader.jsonToJava(json);
        assertTrue(bigInt.equals(new BigInteger(s)));
    }

    public void testBigIntegerInArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigIntegerInArray()");
        String s = "123456789012345678901234567890";
        BigInteger bigInt = new BigInteger(s);
        Object[] bigInts = new Object[] { bigInt, bigInt };
        BigInteger[] typedBigInts = new BigInteger[] { bigInt, bigInt };
        String json = JsonWriter.objectToJson(bigInts);
        println("json=" + json);
        bigInts = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(bigInts.length == 2);
        assertTrue(bigInts[0] == bigInts[1]);
        assertTrue(new BigInteger(s).equals(bigInts[0]));
        json = JsonWriter.objectToJson(typedBigInts);
        println("json=" + json);
        assertTrue(typedBigInts.length == 2);
        assertTrue(typedBigInts[0] == typedBigInts[1]);
        assertTrue(new BigInteger(s).equals(typedBigInts[0]));
    }

    public void testBigIntegerInCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigIntegerInCollection()");
        String s = "123456789012345678901234567890";
        BigInteger bigInt = new BigInteger(s);
        List list = new ArrayList();
        list.add(bigInt);
        list.add(bigInt);
        String json = JsonWriter.objectToJson(list);
        println("json=" + json);
        list = (List) JsonReader.jsonToJava(json);
        assertTrue(list.size() == 2);
        assertTrue(list.get(0).equals(new BigInteger(s)));
        assertTrue(list.get(0) == list.get(1));
    }

    public void testBigDecimal() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigDecimal()");
        String s = "123456789012345678901234567890.123456789012345678901234567890";
        BigDecimal bigDec = new BigDecimal(s);
        String json = JsonWriter.objectToJson(bigDec);
        println("json=" + json);
        bigDec = (BigDecimal) JsonReader.jsonToJava(json);
        assertTrue(bigDec.equals(new BigDecimal(s)));
    }

    public void testBigDecimalInArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigDecimalInArray()");
        String s = "123456789012345678901234567890.123456789012345678901234567890";
        BigDecimal bigDec = new BigDecimal(s);
        Object[] bigDecs = new Object[] { bigDec, bigDec };
        BigDecimal[] typedBigDecs = new BigDecimal[] { bigDec, bigDec };
        String json = JsonWriter.objectToJson(bigDecs);
        println("json=" + json);

        bigDecs = (Object[]) JsonReader.jsonToJava(json);
        assertTrue(bigDecs.length == 2);
        assertTrue(bigDecs[0] == bigDecs[1]);
        assertTrue(new BigDecimal(s).equals(bigDecs[0]));
        json = JsonWriter.objectToJson(typedBigDecs);
        println("json=" + json);
        assertTrue(typedBigDecs.length == 2);
        assertTrue(typedBigDecs[0] == typedBigDecs[1]);
        assertTrue(new BigDecimal(s).equals(typedBigDecs[0]));
    }

    public void testBigDecimalInCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testBigDecimalInCollection()");
        String s = "-123456789012345678901234567890.123456789012345678901234567890";
        BigDecimal bigDec = new BigDecimal(s);
        List list = new ArrayList();
        list.add(bigDec);
        list.add(bigDec);
        String json = JsonWriter.objectToJson(list);
        println("json=" + json);
        list = (List) JsonReader.jsonToJava(json);
        assertTrue(list.size() == 2);
        assertTrue(list.get(0).equals(new BigDecimal(s)));
        assertTrue(list.get(0) == list.get(1));
    }

    public void testSqlDate() throws Exception
    {
        println("\nTestJsonReaderWriter.testSqlDate()");
        long now = System.currentTimeMillis();
        Date[] dates = new Date[] { new Date(now), new java.sql.Date(now), new Timestamp(now) };
        String json = JsonWriter.objectToJson(dates);
        println("json=" + json);
        Date[] dates2 = (Date[]) JsonReader.jsonToJava(json);
        assertTrue(dates2.length == 3);
        assertTrue(dates2[0].equals(new Date(now)));
        assertTrue(dates2[1].equals(new java.sql.Date(now)));
        Timestamp stamp = (Timestamp) dates2[2];
        assertTrue(stamp.getTime() == dates[0].getTime());
        assertTrue(stamp.getTime() == now);
    }

    public void testJsonReaderConstructor() throws Exception
    {
        println("\nTestJsonReaderWriter.testJsonReaderConstructor()");
        String json = "{\"@type\":\"sun.util.calendar.ZoneInfo\",\"zone\":\"EST\"}";
        JsonReader jr = new JsonReader(new ByteArrayInputStream(json.getBytes()));
        TimeZone tz = (TimeZone) jr.readObject();
        assertTrue(tz != null);
        assertTrue("EST".equals(tz.getID()));
    }

    public class WeirdDate extends Date
    {
        public WeirdDate(Date date) { super(date.getTime()); }
        public WeirdDate(long millis) { super(millis); }
    }

    public class WeirdDateWriter implements JsonWriter.JsonClassWriter
    {
        private SafeSimpleDateFormat dateFormat = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        public void write(Object o, boolean showType, Writer out) throws IOException
        {
            String value = dateFormat.format((Date)o);
            out.write("\"value\":\"");
            out.write(value);
            out.write('"');
        }

        public boolean hasPrimitiveForm()
        {
            return true;
        }

        public void writePrimitiveForm(Object o, Writer out) throws IOException
        {
            String value = dateFormat.format((Date)o);
            out.write('"');
            out.write(value);
            out.write('"');
        }
    }

    public class WeirdDateReader implements JsonReader.JsonClassReader
    {
        private SafeSimpleDateFormat dateFormat = new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        public Object read(Object o, LinkedList<JsonObject<String, Object>> stack) throws IOException
        {
            if (o instanceof String)
            {
                try
                {
                    return new WeirdDate(dateFormat.parse((String) o));
                }
                catch (ParseException e)
                {
                    throw new IOException("Date format incorrect");
                }
            }

            JsonObject jObj = (JsonObject) o;
            if (jObj.containsKey("value"))
            {
                try
                {
                    return jObj.target = new WeirdDate(dateFormat.parse((String) jObj.get("value")));
                }
                catch (ParseException e)
                {
                    throw new IOException("Date format incorrect");
                }
            }
            throw new IOException("Date missing 'value' field");

        }
    }

    public void testCustomClassReaderWriter() throws Exception
    {
        println("\nTestJsonReaderWriter.testCustomClassReaderWriter()");
        JsonWriter.addWriter(WeirdDate.class, new WeirdDateWriter());
        JsonReader.addReader(WeirdDate.class, new WeirdDateReader());

        WeirdDate now = new WeirdDate(System.currentTimeMillis());
        String json = JsonWriter.objectToJson(now);
        println("json=" + json);
        WeirdDate date = (WeirdDate)JsonReader.jsonToJava(json);
        assertTrue(now.equals(date));

        JsonWriter.addNotCustomWriter(WeirdDate.class);
        JsonReader.addNotCustomReader(WeirdDate.class);
        json = JsonWriter.objectToJson(now);
        println("json=" + json);
        assertTrue(now.equals(date));
    }

    public void testReconstituteObjectArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteObjectArray()");
        Date now = new Date();
        TestObject to = new TestObject("football");
        TimeZone tz = TimeZone.getTimeZone("EST");
        Collection col = new ArrayList();
        col.add("Collection inside array");
        col.add(tz);
        col.add(now);
        Object[] objs = new Object[] { now, 123.45, 0.04f, "This is a string", null,  true, to, tz, col, (short) 7, (byte) -127};
        Object[] two = new Object[] {objs, "bella", objs};
        String json0 = JsonWriter.objectToJson(two);
        println("json0=" + json0);
        Map map = JsonReader.jsonToMaps(json0);
        map.toString(); // Necessary
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        // Read back into typed Java objects, the Maps of Maps versus that was dumped out
        Object[] result = (Object[]) JsonReader.jsonToJava(json1);
        assertTrue(result.length == 3);
        Object[] arr1 = (Object[]) result[0];
        assertTrue(arr1.length == 11);
        String bella = (String) result[1];
        Object[] arr2 = (Object[]) result[2];
        assertTrue(arr1 == arr2);
        assertTrue("bella".equals(bella));
        assertTrue(now.equals(arr1[0]));
        assertTrue(arr1[1].equals(123.45));
        assertTrue(arr1[2].equals(0.04f));
        assertTrue("This is a string".equals(arr1[3]));
        assertTrue(arr1[4] == null);
        assertTrue(arr1[5] == Boolean.TRUE);
        assertTrue(to.equals(arr1[6]));
        assertTrue(tz.equals(arr1[7]));
        List c = (List) arr1[8];
        assertTrue("Collection inside array".equals(c.get(0)));
        assertTrue(tz.equals(c.get(1)));
        assertTrue(now.equals(c.get(2)));
        assertTrue(7== (Short)arr1[9]);
        assertTrue(-127 == (Byte) arr1[10]);
        assertTrue(json0.equals(json1));

        TestArray ta = new TestArray();
        ta.init();
        json0 = JsonWriter.objectToJson(ta);
        println("json0=" + json0);

        map = JsonReader.jsonToMaps(json0);
        map.toString();
        json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        assertTrue(json0.equals(json1));
    }

    public void testReconstituteObjectArrayTypes() throws Exception
    {
        println("testReconstituteObjectArrayTypes");
        Object[] bytes = new Object[]{_CONST_BYTE,  _CONST_BYTE};
        testReconstituteArrayHelper(bytes);
        Byte[] Bytes = new Byte[]{_CONST_BYTE,  _CONST_BYTE};
        testReconstituteArrayHelper(Bytes);
        byte[] bites = new byte[]{_CONST_BYTE,  _CONST_BYTE};
        testReconstituteArrayHelper(bites);

        Object[] shorts = new Object[]{_CONST_SHORT,  _CONST_SHORT};
        testReconstituteArrayHelper(shorts);
        Short[] Shorts = new Short[]{_CONST_SHORT,  _CONST_SHORT};
        testReconstituteArrayHelper(Shorts);
        short[] shortz = new short[]{_CONST_SHORT,  _CONST_SHORT};
        testReconstituteArrayHelper(shortz);

        Object[] ints = new Object[]{_CONST_INT,  _CONST_INT};
        testReconstituteArrayHelper(ints);
        Integer[] Ints = new Integer[]{_CONST_INT,  _CONST_INT};
        testReconstituteArrayHelper(Ints);
        int[] intz = new int[]{_CONST_INT,  _CONST_INT};
        testReconstituteArrayHelper(intz);

        Object[] longs = new Object[]{_CONST_LONG,  _CONST_LONG};
        testReconstituteArrayHelper(longs);
        Long[] Longs = new Long[]{_CONST_LONG,  _CONST_LONG};
        testReconstituteArrayHelper(Longs);
        long[] longz = new long[]{_CONST_LONG,  _CONST_LONG};
        testReconstituteArrayHelper(longz);

        Object[] floats = new Object[]{_CONST_FLOAT,  _CONST_FLOAT};
        testReconstituteArrayHelper(floats);
        Float[] Floats = new Float[]{_CONST_FLOAT,  _CONST_FLOAT};
        testReconstituteArrayHelper(Floats);
        float[] floatz = new float[]{_CONST_FLOAT,  _CONST_FLOAT};
        testReconstituteArrayHelper(floatz);

        Object[] doubles = new Object[]{_CONST_DOUBLE,  _CONST_DOUBLE};
        testReconstituteArrayHelper(doubles);
        Double[] Doubles = new Double[]{_CONST_DOUBLE,  _CONST_DOUBLE};
        testReconstituteArrayHelper(Doubles);
        double[] doublez = new double[]{_CONST_DOUBLE,  _CONST_DOUBLE};
        testReconstituteArrayHelper(doublez);

        Object[] booleans = new Object[]{Boolean.TRUE, Boolean.TRUE};
        testReconstituteArrayHelper(booleans);
        Boolean[] Booleans = new Boolean[]{Boolean.TRUE,  Boolean.TRUE};
        testReconstituteArrayHelper(Booleans);
        boolean[] booleanz = new boolean[]{true, true};
        testReconstituteArrayHelper(booleanz);

        Object[] chars = new Object[]{'J', 'J'};
        testReconstituteArrayHelper(chars);
        Character[] Chars = new Character[]{'S', 'S'};
        testReconstituteArrayHelper(Chars);
        char[] charz = new char[]{'R', 'R'};
        testReconstituteArrayHelper(charz);

        Object[] classes = new Object[]{LinkedList.class, LinkedList.class};
        testReconstituteArrayHelper(classes);
        Class[] Classes = new Class[]{LinkedList.class, LinkedList.class};
        testReconstituteArrayHelper(Classes);

        Date now = new Date();
        Object[] dates = new Object[]{now, now};
        testReconstituteArrayHelper(dates);
        Date[] Dates = new Date[]{now, now};
        testReconstituteArrayHelper(Dates);

        BigDecimal pi = new BigDecimal(3.1415926535897932384626433);
        Object[] bigDecimals = new Object[]{pi, pi};
        testReconstituteArrayHelper(bigDecimals);
        BigDecimal[] BigDecimals = new BigDecimal[]{pi, pi};
        testReconstituteArrayHelper(BigDecimals);

        String s = "json-io";
        Object[] strings = new Object[]{s, s};
        testReconstituteArrayHelper(strings);
        String[] Strings = new String[]{s, s};
        testReconstituteArrayHelper(Strings);

        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(TimeZone.getTimeZone("EST"));
        Object[] calendars = new Object[]{cal, cal};
        testReconstituteArrayHelper(calendars);
        Calendar[] Calendars = new Calendar[]{cal, cal};
        testReconstituteArrayHelper(Calendars);
        GregorianCalendar[] calendarz = new GregorianCalendar[]{cal, cal};
        testReconstituteArrayHelper(calendarz);
    }

    private void testReconstituteArrayHelper(Object foo) throws IOException
    {
        assertTrue(Array.getLength(foo) == 2);
        String json0 = JsonWriter.objectToJson(foo);
        println("json0=" + json0);

        Map map = JsonReader.jsonToMaps(json0);
        map.toString(); // called to prevent compiler optimization that could eliminate map local variable.
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteEmptyArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteEmptyArray()");
        Object[] empty = new Object[]{};
        String json0 = getJsonString(empty);
        println("json0=" + json0);

        Map map = JsonReader.jsonToMaps(json0);
        assertTrue(map != null);
        empty = (Object[]) map.get("@items");
        assertTrue(empty != null);
        assertTrue(empty.length == 0);
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        assertTrue(json0.equals(json1));

        Object[] list = new Object[] {empty, empty};
        json0 = getJsonString(list);
        println("json0=" + json0);

        map = JsonReader.jsonToMaps(json0);
        assertTrue(map != null);
        list = (Object[]) map.get("@items");
        assertTrue(list.length == 2);
        Map e1 = (Map) list[0];
        Map e2 = (Map) list[1];
        assertTrue(e1.get("@items") == e2.get("@items"));
        assertTrue(((Object[])e1.get("@items")).length == 0);

        json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteTypedArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteTypedArray()");
        String[] strs = new String[] {"tom", "dick", "harry"};
        Object[] objs = new Object[] {strs, "a", strs};
        String json0 = JsonWriter.objectToJson(objs);
        println("json0=" + json0);
        Map map = JsonReader.jsonToMaps(json0);
        map.toString();     // Necessary
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        Object[] result = (Object[]) JsonReader.jsonToJava(json1);
        assertTrue(result.length == 3);
        assertTrue(result[0] == result[2]);
        assertTrue("a".equals(result[1]));
        String[] sa = (String[]) result[0];
        assertTrue(sa.length == 3);
        assertTrue("tom".equals(sa[0]));
        assertTrue("dick".equals(sa[1]));
        assertTrue("harry".equals(sa[2]));
        String json2 = JsonWriter.objectToJson(result);
        println("json2=" + json2);
        assertTrue(json0.equals(json1));
        assertTrue(json1.equals(json2));
    }

    public void testReconstituteArray() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteArray()");
        TestArray testArray = new TestArray();
        testArray.init();
        String json0 = JsonWriter.objectToJson(testArray);
        println("json0=" + json0);
        Map testArray2 = JsonReader.jsonToMaps(json0);

        String json1 = JsonWriter.objectToJson(testArray2);
        println("json1=" + json1);

        TestArray testArray3 = (TestArray) JsonReader.jsonToJava(json1);
        assertArray(testArray3);    // Re-written from Map of Maps and re-loaded correctly
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteCollection()");
        TestObject to = new TestObject("football");
        Collection objs = new ArrayList();
        Date now = new Date();
        objs.add(now);
        objs.add(123.45);
        objs.add("This is a string");
        objs.add(null);
        objs.add(to);
        objs.add(new Object[] {"dog", new String[] {"a","b","c"}});
        Collection two = new ArrayList();
        two.add(objs);
        two.add("bella");
        two.add(objs);

        String json0 = JsonWriter.objectToJson(two);
        println("json0=" + json0);
        Map map = JsonReader.jsonToMaps(json0);
        map.hashCode();
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        List colOuter = (List) JsonReader.jsonToJava(json1);
        assertTrue(colOuter.get(0) == colOuter.get(2));
        assertTrue("bella".equals(colOuter.get(1)));
        List col1 = (List) colOuter.get(0);
        assertTrue(col1.get(0).equals(now));
        assertTrue(col1.get(1).equals(123.45));
        assertTrue("This is a string".equals(col1.get(2)));
        assertTrue(col1.get(3) == null);
        assertTrue(col1.get(4).equals(to));
        assertTrue(col1.get(5) instanceof Object[]);
        Object[] oa = (Object[]) col1.get(5);
        assertTrue("dog".equals(oa[0]));
        assertTrue(oa[1] instanceof String[]);
        String[] sa = (String[]) oa[1];
        assertTrue("a".equals(sa[0]));
        assertTrue("b".equals(sa[1]));
        assertTrue("c".equals(sa[2]));

        assertTrue(json0.equals(json1));
    }

    public void testReconstituteEmptyCollection() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteEmptyCollection()");
        Collection empty = new ArrayList();
        String json0 = getJsonString(empty);
        println("json0=" + json0);

        Map map = JsonReader.jsonToMaps(json0);
        assertTrue(map != null);
        assertTrue(map.isEmpty());
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        assertTrue(json0.equals(json1));

        Object[] list = new Object[] {empty, empty};
        json0 = getJsonString(list);
        println("json=" + json0);

        map = JsonReader.jsonToMaps(json0);
        assertTrue(map != null);
        list = (Object[]) map.get("@items");
        assertTrue(list.length == 2);
        Map e1 = (Map) list[0];
        Map e2 = (Map) list[1];
        assertTrue(e1.isEmpty());
        assertTrue(e2.isEmpty());

        json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteEmptyObject() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteEmptyObject()");
        Empty empty = new Empty();
        String json0 = getJsonString(empty);
        println("json0=" + json0);

        Map m = JsonReader.jsonToMaps(json0);
        assertTrue(m.isEmpty());

        String json1 = JsonWriter.objectToJson(m);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteMap() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteMap()");
        TestMap testMap = new TestMap();
        testMap.init();
        String json0 = JsonWriter.objectToJson(testMap);
        println("json0=" + json0);
        Map testMap2 = JsonReader.jsonToMaps(json0);

        String json1 = JsonWriter.objectToJson(testMap2);
        println("json1=" + json1);

        TestMap testMap3 = (TestMap) JsonReader.jsonToJava(json1);
        assertMap(testMap3);   // Re-written from Map of Maps and re-loaded correctly
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteCollection2() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteCollection2()");
        TestCollection testCol = new TestCollection();
        testCol.init();
        String json0 = JsonWriter.objectToJson(testCol);
        println("json0=" + json0);
        Map testCol2 = JsonReader.jsonToMaps(json0);

        String json1 = JsonWriter.objectToJson(testCol2);
        println("json1=" + json1);

        TestCollection testCol3 = (TestCollection) JsonReader.jsonToJava(json1);
        assertCollection(testCol3);   // Re-written from Map of Maps and re-loaded correctly
        assertTrue(json0.equals(json1));
    }

    private static class SimpleMapTest
    {
        HashMap map = new HashMap();
    }

    public void testReconstituteMapSimple() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteMapSimple()");
        SimpleMapTest smt = new SimpleMapTest();
        smt.map.put("a", "alpha");
        String json0 = getJsonString(smt);
        println("json0=" + json0);

        Map result = JsonReader.jsonToMaps(json0);
        String json1 = JsonWriter.objectToJson(result);
        println("json1=" + json1);

        SimpleMapTest mapTest = (SimpleMapTest) readJsonObject(json1);
        assertTrue(mapTest.map.equals(smt.map));
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteMapEmpty() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteMapEmpty()");
        Map map = new LinkedHashMap();
        String json0 = getJsonString(map);
        println("json0=" + json0);

        map = JsonReader.jsonToMaps(json0);
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        map = (Map) readJsonObject(json1);
        assertTrue(map instanceof LinkedHashMap);
        assertTrue(map.isEmpty());
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteRefMap() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteEmptyMap()");
        Map m1 = new HashMap();
        Object[] root = new Object[]{ m1, m1};
        String json0 = getJsonString(root);
        println("json0=" + json0);

        Map map = JsonReader.jsonToMaps(json0);
        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);

        root = (Object[]) readJsonObject(json1);
        assertTrue(root.length == 2);
        assertTrue(root[0] instanceof Map);
        assertTrue(((Map)root[0]).isEmpty());
        assertTrue(root[1] instanceof Map);
        assertTrue(((Map) root[1]).isEmpty());
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteFields() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteFields()");
        TestFields testFields = new TestFields();
        testFields.init();
        String json0 = JsonWriter.objectToJson(testFields);
        println("json0=" + json0);
        Map testFields2 = JsonReader.jsonToMaps(json0);

        String json1 = JsonWriter.objectToJson(testFields2);
        println("json1=" + json1);

        TestFields testFields3 = (TestFields) JsonReader.jsonToJava(json1);
        assertFields(testFields3);   // Re-written from Map of Maps and re-loaded correctly
        assertTrue(json0.equals(json1));
    }

    public void testReconstitutePrimitives() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstitutePrimitives()");
        Object foo = new TestJsonNoDefaultOrPublicConstructor("Hello, World.", new Date(), (byte) 1, new Byte((byte)11), (short) 2, new Short((short)22), 3, new Integer(33), 4L, new Long(44L), 5.0f, new Float(55.0f), 6.0, new Double(66.0), true, Boolean.TRUE,'J', new Character('K'), new String[] {"john","adams"}, new int[] {2,6}, new BigDecimal(2.71828));
        String json0 = getJsonString(foo);
        println("json0=" + json0);

        Map map = JsonReader.jsonToMaps(json0);
        assertTrue((Byte)map.get("_byte") == 1);
        assertTrue((Short) map.get("_short") == 2);
        assertTrue((Integer) map.get("_int") == 3);
        assertTrue((Long) map.get("_long") == 4);
        assertTrue((Float) map.get("_float") == 5.0f);
        assertTrue((Double) map.get("_double") == 6.0);
        assertTrue(map.get("_boolean") == Boolean.TRUE);
        assertTrue((Character) map.get("_char") == 'J');

        assertTrue((Byte) map.get("_Byte") == 11);
        assertTrue((Short) map.get("_Short") == 22);
        assertTrue((Integer) map.get("_Integer") == 33);
        assertTrue((Long) map.get("_Long") == 44L);
        assertTrue((Float) map.get("_Float") == 55.0f);
        assertTrue((Double) map.get("_Double") == 66.0);
        assertTrue((Boolean) map.get("_Boolean") == Boolean.TRUE);
        assertTrue((Character) map.get("_Char") == 'K');
        assertTrue(map.get("_bigD").equals(new BigDecimal(2.71828)));

        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testReconstituteNullablePrimitives() throws Exception
    {
        println("\nTestJsonReaderWriter.testReconstituteNullablePrimitives()");
        Object foo = new TestJsonNoDefaultOrPublicConstructor("Hello, World.", new Date(), (byte) 1, null, (short) 2, null, 3, null, 4L, null, 5.0f, null, 6.0, null, true, null,'J', null, new String[] {"john","adams"}, new int[] {2,6}, null);
        String json = getJsonString(foo);
        println("json0=" + json);

        Map map = JsonReader.jsonToMaps(json);
        assertTrue(map.get("_byte") instanceof Byte);
        assertTrue(map.get("_short") instanceof Short);
        assertTrue(map.get("_int") instanceof Integer);
        assertTrue(map.get("_long") instanceof Long);
        assertTrue(map.get("_float") instanceof Float);
        assertTrue(map.get("_double") instanceof Double);
        assertTrue(map.get("_boolean") instanceof Boolean);
        assertTrue(map.get("_char") instanceof Character);

        Map prim = (Map) map.get("_Byte");
        assertNull(prim);
        prim = (Map) map.get("_Short");
        assertNull(prim);
        prim = (Map) map.get("_Integer");
        assertNull(prim);
        prim = (Map) map.get("_Long");
        assertNull(prim);
        prim = (Map) map.get("_Float");
        assertNull(prim);
        prim = (Map) map.get("_Double");
        assertNull(prim);
        prim = (Map) map.get("_Boolean");
        assertNull(prim);
        prim = (Map) map.get("_Char");
        assertNull(prim);
        prim = (Map) map.get("_bigD");
        assertNull(prim);

        String json1 = JsonWriter.objectToJson(map);
        println("json1=" + json1);
        assertTrue(json.equals(json1));

        map = JsonReader.jsonToMaps(json1);
        json = JsonWriter.objectToJson(map);
        println("json2=" + json);
        assertTrue(json.equals(json1));
    }

    public void testArraysAsList() throws Exception
    {
    	Object[] strings = new Object[] { "alpha", "bravo", "charlie" };
    	List strs = Arrays.asList(strings);
    	List foo = (List) JsonReader.jsonToJava(JsonWriter.objectToJson(strs));
		assertTrue(foo.size() == 3);
		assertTrue("alpha".equals(foo.get(0)));
		assertTrue("charlie".equals(foo.get(2)));
    }
    
    public void testLocaleInMap() throws Exception
    {
    	Map<Locale, String> map = new HashMap<Locale, String>();
    	map.put(Locale.US, "United States of America");
    	map.put(Locale.CANADA, "Canada");
    	map.put(Locale.UK, "United Kingdom");
    	
    	String json = JsonWriter.objectToJson(map);
    	Map map2 = (Map) JsonReader.jsonToJava(json);
    	assertTrue(map.equals(map2));
    }

    public void testMultiDimensionalArrays() throws Exception
    {
        int[][][][] x = new int[][][][]{{{{0,1},{0,1}},{{0,1},{0,1}}},{{{0,1},{0,1}},{{0,1},{0,1}}}};
        for (int a=0; a < 2; a++)
        {
            for (int b=0; b < 2; b++)
            {
                for (int c=0; c < 2; c++)
                {
                    for (int d=0; d < 2; d++)
                    {
                        x[a][b][c][d] = a + b + c + d;
                    }
                }
            }
        }

        String json = JsonWriter.objectToJson(x);
        int[][][][] y = (int[][][][]) JsonReader.jsonToJava(json);


        for (int a=0; a < 2; a++)
        {
            for (int b=0; b < 2; b++)
            {
                for (int c=0; c < 2; c++)
                {
                    for (int d=0; d < 2; d++)
                    {
                        assertTrue(y[a][b][c][d] == a + b + c + d);
                    }
                }
            }
        }

        Integer[][][][] xx = new Integer[][][][]{{{{0,1},{0,1}},{{0,1},{0,1}}},{{{0,1},{0,1}},{{0,1},{0,1}}}};
        for (int a=0; a < 2; a++)
        {
            for (int b=0; b < 2; b++)
            {
                for (int c=0; c < 2; c++)
                {
                    for (int d=0; d < 2; d++)
                    {
                        xx[a][b][c][d] = a + b + c + d;
                    }
                }
            }
        }

        json = JsonWriter.objectToJson(xx);
        Integer[][][][] yy = (Integer[][][][]) JsonReader.jsonToJava(json);


        for (int a=0; a < 2; a++)
        {
            for (int b=0; b < 2; b++)
            {
                for (int c=0; c < 2; c++)
                {
                    for (int d=0; d < 2; d++)
                    {
                        assertTrue(yy[a][b][c][d] == a + b + c + d);
                    }
                }
            }
        }
    }

    public void testBooleanCompatibility() throws IOException
    {
        class MyBooleanTesting
        {
            private boolean myBoolean = false;
        }

        class MyBoolean2Testing
        {
            private Boolean myBoolean = false;
        }

        MyBooleanTesting testObject = new MyBooleanTesting();
        MyBoolean2Testing testObject2 = new MyBoolean2Testing();
        String json0 = JsonWriter.objectToJson(testObject);
        String json1 = JsonWriter.objectToJson(testObject2);

        println("json0=" + json0);
        println("json1=" + json1);

        assertTrue(json0.contains("\"myBoolean\":false"));
        assertTrue(json1.contains("\"myBoolean\":false"));
    }

    public void testMapToMapCompatibility() throws Exception
    {
        String json0 = "{\"rows\":[{\"columns\":[{\"name\":\"ZYKLUS\",\"value\":\"9000\"},{\"name\":\"VON\",\"value\":\"0001-01-01\"},{\"name\":\"BIS\",\"value\":\"0001-01-01\"}]},{\"columns\":[{\"name\":\"ZYKLUS\",\"value\":\"9713\"},{\"name\":\"VON\",\"value\":\"0001-01-01\"},{\"name\":\"BIS\",\"value\":\"0001-01-01\"}]}],\"selectedRows\":\"110\"}";
        JsonObject root = (JsonObject) JsonReader.jsonToMaps(json0);
        String json1 = JsonWriter.objectToJson(root);
        println("json0=" + json0);
        println("json1=" + json1);
        assertTrue(json0.equals(json1));
    }

    public void testJsonObjectToJava() throws Exception
    {
        TestObject test = new TestObject("T.O.");
        TestObject child = new TestObject("child");
        test._other = child;
        String json = JsonWriter.objectToJson(test);
        println("json=" + json);
        JsonObject root = (JsonObject) JsonReader.jsonToMaps(json);
        JsonReader reader = new JsonReader();
        TestObject test2 = (TestObject) reader.jsonObjectsToJava(root);
        assertTrue(test2.equals(test));
        assertTrue(test2._other.equals(child));
    }

    public void testInnerInstance() throws Exception
    {
        Dog dog = new Dog();
        dog.x = 10;
        Dog.Leg leg = dog.new Leg();
        leg.y = 20;
        String json0 = JsonWriter.objectToJson(dog);
        println("json0=" + json0);

        String json1 = JsonWriter.objectToJson(leg);
        println("json1=" + json1);
        Dog.Leg go = (Dog.Leg) JsonReader.jsonToJava(json1);
        assertTrue(go.y == 20);
        assertTrue(go.getParentX() == 10);
    }

    private static void println(Object ... args)
    {
        if (_debug)
        {
            for (Object arg : args)
            {
                System.out.println(arg);
            }
        }
    }

    private String getJsonString(Object obj) throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JsonWriter jsonWriter = new JsonWriter(bout);
        _startWrite = System.nanoTime();
        jsonWriter.write(obj);
        _endWrite = System.nanoTime();
        return new String(bout.toByteArray(), "UTF-8");
    }

    private Object readJsonObject(String json) throws Exception
    {    
        _startRead = System.nanoTime();
        Object o = JsonReader.jsonToJava(json);
        _endRead = System.nanoTime();
        return o;
    }

    private void time(Object root) throws Exception
    {
        long startWrite = System.nanoTime();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bo);
        out.writeObject(root);
        out.flush();
        long endWrite = System.nanoTime();
        out.close();

        long startRead = System.nanoTime();
        ByteArrayInputStream bin = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        in.readObject();
        long endRead = System.nanoTime();
        in.close();

        println("JSON write time = " + (_endWrite - _startWrite) / 1000000 + " ms");
        println("ObjectOutputStream time = " + (endWrite - startWrite) / 1000000 + " ms");
        println("JSON  read time  = " + (_endRead - _startRead) / 1000000 + " ms");
        println("ObjectInputStream time = " + (endRead - startRead) / 1000000 + " ms");
    }
}
