package org.bbottema.javareflection.valueconverter.converters;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bbottema.javareflection.commonslang25.NumberUtils;
import org.bbottema.javareflection.util.Function;
import org.bbottema.javareflection.valueconverter.IncompatibleTypeException;
import org.bbottema.javareflection.valueconverter.ValueFunction;
import org.bbottema.javareflection.valueconverter.ValueFunction.ValueFunctionImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Conversions are as follows:
 * FIXME add non-Number conversions
 * <ol>
 * <li><strong>Integer (or primitive int)</strong>: <code>Integer.parseInt(value)</code></li>
 * <li><strong>Byte (or primitive byte)</strong>: <code>Byte.parseByte(value)</code></li>
 * <li><strong>Short (or primitive short)</strong>: <code>Short.parseShort(value)</code></li>
 * <li><strong>Long (or primitive long)</strong>: <code>Long.parseLong(value)</code></li>
 * <li><strong>Float (or primitive float)</strong>: <code>Float.parseFloat(value)</code></li>
 * <li><strong>Double (or primitive double)</strong>: <code>Double.parseDouble(value)</code></li>
 * <li><strong>BigInteger</strong>: <code>BigInteger.valueOf(Long.parseLong(value))</code></li>
 * <li><strong>BigDecimal</strong>: <code>BigDecimal.valueOf(Long.parseLong(value))</code></li>
 * </ol>
 */
@Nullable
@UtilityClass
public final class StringConverters {
	
	public static final Collection<ValueFunction<String, ?>> STRING_CONVERTERS = produceStringConverters();
	
	private static Collection<ValueFunction<String, ?>> produceStringConverters() {
		ArrayList<ValueFunction<String, ?>> converters = new ArrayList<>();
		converters.add(new ValueFunctionImpl<>(String.class, Character.class, new StringToCharacterFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Boolean.class, new StringToBooleanFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Number.class, new StringToNumberFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Byte.class, new StringToByteFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Short.class, new StringToShortFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Long.class, new StringToLongFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Float.class, new StringToFloatFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, Double.class, new StringToDoubleFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, BigInteger.class, new StringToBigIntegerFunction()));
		converters.add(new ValueFunctionImpl<>(String.class, BigDecimal.class, new StringToBigDecimalFunction()));
		return converters;
	}
	
	/**
	 * Creates a converter to convert a <code>String</code> to an Enum instance, by mapping to the enum's name using
	 * {@link Enum#valueOf(Class, String)}.
	 */
	public static <T extends Enum> ValueFunction<String, T> produceStringToEnumConverter(Class<T> targetEnumClass) {
		return new ValueFunctionImpl<>(String.class, targetEnumClass, new StringToEnumFunction<>(targetEnumClass));
	}
	
	@RequiredArgsConstructor
	private static class StringToEnumFunction<T extends Enum> implements Function<String, T> {
		@Nonnull
		private final Class<T> targetEnumClass;
		
		@Override
		public T apply(String value) {
			// /CLOVER:OFF
			try {
				// /CLOVER:ON
				//noinspection unchecked
				return (T) targetEnumClass.getMethod("valueOf", String.class).invoke(null, value);
				// /CLOVER:OFF
			} catch (final IllegalArgumentException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
				throw new IncompatibleTypeException(value, String.class, targetEnumClass, e);
			}
			// /CLOVER:ON
		}
	}
	
	private static class StringToCharacterFunction implements Function<String, Character> {
		@Override
		public Character apply(String value) {
			if (value.length() == 1) {
				return value.charAt(0);
			}
			throw new IncompatibleTypeException(value, String.class, Character.class);
		}
	}
	
	private static class StringToBooleanFunction implements Function<String, Boolean> {
		@Override
		public Boolean apply(String value) {
			if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")) {
				return false;
			} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
				return true;
			}
			throw new IncompatibleTypeException(value, String.class, Boolean.class);
		}
	}
	
	private static class StringToNumberFunction implements Function<String, Number> {
		@Override
		public Number apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return new BigDecimal(value);
			}
			throw new IncompatibleTypeException(value, String.class, Number.class);
		}
	}
	
	private static class StringToByteFunction implements Function<String, Byte> {
		@Override
		public Byte apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return Byte.parseByte(value);
			}
			throw new IncompatibleTypeException(value, String.class, Byte.class);
		}
	}
	
	private static class StringToShortFunction implements Function<String, Short> {
		@Override
		public Short apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return Short.parseShort(value);
			}
			throw new IncompatibleTypeException(value, String.class, Short.class);
		}
	}
	
	private static class StringToLongFunction implements Function<String, Long> {
		@Override
		public Long apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return Long.parseLong(value);
			}
			throw new IncompatibleTypeException(value, String.class, Long.class);
		}
	}
	
	private static class StringToFloatFunction implements Function<String, Float> {
		@Override
		public Float apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return Float.parseFloat(value);
			}
			throw new IncompatibleTypeException(value, String.class, Float.class);
		}
	}
	
	private static class StringToDoubleFunction implements Function<String, Double> {
		@Override
		public Double apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return Double.parseDouble(value);
			}
			throw new IncompatibleTypeException(value, String.class, Double.class);
		}
	}
	
	private static class StringToBigIntegerFunction implements Function<String, BigInteger> {
		@Override
		public BigInteger apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return BigInteger.valueOf(Long.parseLong(value));
			}
			throw new IncompatibleTypeException(value, String.class, BigInteger.class);
		}
	}
	
	private static class StringToBigDecimalFunction implements Function<String, BigDecimal> {
		@Override
		public BigDecimal apply(String value) {
			if (NumberUtils.isNumber(value)) {
				return BigDecimal.valueOf(Long.parseLong(value));
			}
			throw new IncompatibleTypeException(value, String.class, BigDecimal.class);
		}
	}
}