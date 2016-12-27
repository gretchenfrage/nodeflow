package com.phoenixkahlo.ptest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Static utility class for testing.
 */
public class Testing {

	private Testing() {
	}

	public static final Random RANDOM = new Random();

	/**
	 * Execute all {@link com.phoenixkahlo.ptest.Test tests} in the given class.
	 */
	public static void test(Class<?> clazz, boolean crash) {
		boolean assertEnabled = false;
		assert assertEnabled = true;
		if (!assertEnabled) {
			System.err.println("enable assertions with the -ea VM argument");
			System.exit(1);
		}

		Random seeder = new Random();
		System.out.println("### testing class \"" + clazz.getSimpleName() + "\" ###");
		Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(Test.class)).forEach(method -> {
			Test annotation = method.getAnnotation(Test.class);
			if (annotation.name().equals("$unnamed"))
				System.out.print("running \"" + method.getName() + "\"");
			else
				System.out.print("running \"" + annotation.name() + "\"");
			long seed = seeder.nextLong();
			System.out.println(" (seed=" + seed + ")");
			RANDOM.setSeed(seed);
			try {
				method.invoke(null);
			} catch (InvocationTargetException e) {
				System.err.println("test failed with exception:");
				if (crash)
					throw new RuntimeException(e.getTargetException());
				else
					e.getTargetException().printStackTrace();
			} catch (Throwable e) {
				System.err.println("testing error");
				e.printStackTrace();
			}
		});
	}

	public static void test(Class<?> clazz) {
		test(clazz, false);
	}

	/**
	 * Create a Mockery of the given interface.
	 */
	@SuppressWarnings("unchecked")
	public static <E> E mock(Class<E> intrface) {
		Map<Method, MethodMocker> mockers = new HashMap<>();
		for (Method method : intrface.getMethods()) {
			mockers.put(method, new MethodMocker(method.toString()));
		}
		InvocationHandler handler = (proxy, method, args) -> {
			if (method.equals(Mockery.class.getMethod("method", String.class, Class[].class))) {
				for (Map.Entry<Method, MethodMocker> entry : mockers.entrySet()) {
					if (entry.getKey().getName().equals(args[0])
							&& Arrays.equals(entry.getKey().getParameterTypes(), (Object[]) args[1])) {
						return entry.getValue();
					}
				}
				throw new NoSuchElementException("MethodMocker does not exist, name=\"" + args[0] + "\" paramTypes="
						+ Arrays.toString((Object[]) args[1]));
			} else {
				return mockers.get(method).handle(args);
			}
		};
		return (E) Proxy.newProxyInstance(Testing.class.getClassLoader(), new Class<?>[] { intrface, Mockery.class },
				handler);
	}

}
