package org.bbottema.javareflection;

import org.bbottema.javareflection.model.InvokableObject;
import org.bbottema.javareflection.model.LookupMode;
import org.bbottema.javareflection.testmodel.A;
import org.bbottema.javareflection.testmodel.B;
import org.bbottema.javareflection.testmodel.C;
import org.bbottema.javareflection.testmodel.Foo;
import org.bbottema.javareflection.testmodel.Fruit;
import org.bbottema.javareflection.testmodel.Meta;
import org.bbottema.javareflection.testmodel.Moo;
import org.bbottema.javareflection.testmodel.Pear;
import org.bbottema.javareflection.util.MetaAnnotationExtractor;
import org.bbottema.javareflection.util.MethodCallingExtractor;
import org.bbottema.javareflection.valueconverter.ValueConversionHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MethodUtilsTest {
	
	@Before
	public void resetStaticCaches() {
		MethodUtils.resetCache();
		ClassUtils.resetCache();
		ValueConversionHelper.resetDefaultConverters();
	}
	
	@Test
	public void testFindCompatibleMethod()
			throws NoSuchMethodException {
		// find method through interface on superclass, using autoboxing, class casting and an auto convert
		final EnumSet<LookupMode> allButSmartLookup = EnumSet.allOf(LookupMode.class);
		allButSmartLookup.remove(LookupMode.SMART_CONVERT);
		
		InvokableObject<Method> m = MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(Foo.class.getMethod("foo", Double.class, Fruit.class, char.class));
		InvokableObject<Method> m2 = MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m2).isNotNull();
		assertThat(m2).isEqualTo(m);
		// find the same method, but now the first implementation on C should be returned
		m = MethodUtils.findCompatibleMethod(C.class, "foo", allButSmartLookup, double.class, Pear.class, String.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(C.class.getMethod("foo", Double.class, Fruit.class, char.class));
		// find a String method
		m = MethodUtils.findCompatibleMethod(String.class, "concat", EnumSet.noneOf(LookupMode.class), String.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(String.class.getMethod("concat", String.class));
		// shouldn't be able to find the following methods
		try {
			MethodUtils.findCompatibleMethod(B.class, "foos", allButSmartLookup, double.class, Pear.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, String.class, String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleMethod(B.class, "foo", allButSmartLookup, double.class, Fruit.class, Math.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		InvokableObject<Method> result = MethodUtils.findCompatibleMethod(B.class, "foo", EnumSet.allOf(LookupMode.class), double.class, Fruit.class, Math.class);
		assertThat(result).isNotNull();
	}
	
	@Test
	public void testFindCompatibleConstructor()
			throws NoSuchMethodException {
		// find constructor on superclass, using autoboxing
		InvokableObject<Constructor> m = MethodUtils.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(B.class.getConstructor(Fruit.class));
		InvokableObject<Constructor> m2 = MethodUtils.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertThat(m2).isNotNull();
		assertThat(m2).isEqualTo(m);
		// find constructor on superclass, using autoboxing and class casting
		m = MethodUtils.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(B.class.getConstructor(Fruit.class));
		// still find constructor on superclass
		m = MethodUtils.findCompatibleConstructor(C.class, EnumSet.allOf(LookupMode.class), Fruit.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(C.class.getConstructor(Fruit.class));
		// still find constructor on subclass
		m = MethodUtils.findCompatibleConstructor(C.class, EnumSet.allOf(LookupMode.class), Pear.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(C.class.getConstructor(Pear.class));
		// find a String constructor
		m = MethodUtils.findCompatibleConstructor(String.class, EnumSet.noneOf(LookupMode.class), String.class);
		assertThat(m).isNotNull();
		assertThat(m.getMethod()).isEqualTo(String.class.getConstructor(String.class));
		// shouldn't be able to find the following methods
		try {
			MethodUtils.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), double.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.findCompatibleConstructor(B.class, EnumSet.allOf(LookupMode.class), String.class);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testInvokeCompatibleMethod()
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), B.class, "foo", 50d, new Pear(), "g");
		MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), "g");
		MethodUtils.invokeCompatibleMethod("", String.class, "concat", String.class);
		
		// shouldn't be able to find the following methods
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foos", 50d, new Pear(), "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, "foobar", "g");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleMethod(new C(new Pear()), C.class, "foo", 50d, new Pear(), Calendar.getInstance());
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testInvokeCompatibleConstructor()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, new Pear());
		} catch (InstantiationException e) {
			// Ok
		}
		MethodUtils.invokeCompatibleConstructor(C.class, new Pear());
		MethodUtils.invokeCompatibleConstructor(String.class, "test string");
		MethodUtils.invokeCompatibleConstructor(String.class, 1234567);
		// shouldn't be able to find the following methods
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, 50d);
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
		try {
			MethodUtils.invokeCompatibleConstructor(B.class, "foobar");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException e) {
			// OK
		}
	}
	
	@Test
	public void testIsMethodCompatible_Simple() throws NoSuchMethodException {
		EnumSet<LookupMode> lookupModes = EnumSet.noneOf(LookupMode.class);
		
		Method m = Math.class.getMethod("min", int.class, int.class);
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, int.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(m, lookupModes, int.class, A.class)).isFalse();
		
		Method stringConcat = String.class.getMethod("concat", String.class);
		assertThat(MethodUtils.isMethodCompatible(stringConcat, lookupModes, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, lookupModes, Calendar.class)).isFalse();
	}
	
	@Test
	public void testIsMethodCompatible_TestLookupModes() throws NoSuchMethodException {
		EnumSet<LookupMode> noConversions = EnumSet.noneOf(LookupMode.class);
		EnumSet<LookupMode> commonConversions = EnumSet.of(LookupMode.COMMON_CONVERT);
		EnumSet<LookupMode> castConvert = EnumSet.of(LookupMode.CAST_TO_SUPER, LookupMode.CAST_TO_INTERFACE);
		EnumSet<LookupMode> castThenCommonsConvert = EnumSet.of(LookupMode.CAST_TO_SUPER, LookupMode.COMMON_CONVERT);
		EnumSet<LookupMode> smartConversions = EnumSet.of(LookupMode.SMART_CONVERT);
		
		Method m = Math.class.getMethod("min", int.class, int.class);
		assertThat(MethodUtils.isMethodCompatible(m, noConversions, int.class, boolean.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(m, commonConversions, int.class, boolean.class)).isTrue();
		
		Method cFoo = C.class.getMethod("foo", Double.class, Fruit.class, char.class);
		assertThat(MethodUtils.isMethodCompatible(cFoo, castConvert, Double.class, Pear.class, char.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(cFoo, noConversions, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, commonConversions, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, castConvert, double.class, Pear.class, String.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(cFoo, castThenCommonsConvert, double.class, Pear.class, String.class)).isTrue();
		
		Method stringConcat = String.class.getMethod("concat", String.class);
		assertThat(MethodUtils.isMethodCompatible(stringConcat, noConversions, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, noConversions, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, commonConversions, String.class)).isTrue();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, commonConversions, Calendar.class)).isFalse();
		assertThat(MethodUtils.isMethodCompatible(stringConcat, smartConversions, Calendar.class)).isTrue();
	}
	
	@SuppressWarnings("ConstantConditions")
	@Test
	public void testInvokeCompatibleMethod_VariousAccessLevels() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final C c = new C(new Pear());
		assertThat(MethodUtils.invokeCompatibleMethod(c, A.class, "privateMethod")).isEqualTo("private 1");
		assertThat(MethodUtils.invokeCompatibleMethod(c, B.class, "protectedMethod")).isEqualTo("protected 2");
		assertThat(MethodUtils.invokeCompatibleMethod(c, C.class, "privateMethod")).isEqualTo("private 2");
		assertThat(MethodUtils.invokeCompatibleMethod(c, C.class, "protectedMethod")).isEqualTo("protected 2");
		
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 1, true)).intValue()).isEqualTo(1);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 1, false)).intValue()).isEqualTo(0);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", 0, true)).intValue()).isEqualTo(0);
		assertThat(((Number) MethodUtils.invokeCompatibleMethod(null, Math.class, "min", "d", 1000)).intValue()).isEqualTo(100); // d -> 100
	}
	
	@Test
	public void testFindMatchingMethods() {
		assertThat(MethodUtils.findMatchingMethods(Moo.class, "method1", "Integer"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extracting(new MethodCallingExtractor<String>("value"))
				.containsExactlyInAnyOrder("Moo.method1-A", "Shmoo.method1-A");
		
		assertThat(MethodUtils.findMatchingMethods(Moo.class, "method1", "Object", "java.lang.Integer"))
				.extracting(new MetaAnnotationExtractor<>(Meta.class))
				.extracting(new MethodCallingExtractor<String>("value"))
				.containsExactlyInAnyOrder("Moo.method1-C");
	}
	
}