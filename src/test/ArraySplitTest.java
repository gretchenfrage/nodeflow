package test;

import java.util.Arrays;

import com.phoenixkahlo.pnet.serialization.SerializationUtils;
import com.phoenixkahlo.ptest.Test;

public class ArraySplitTest {

	@Test
	public static void test1() {
		byte[] arr = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
		byte[][] arrs = SerializationUtils.split(arr, 5);
		assert Arrays.equals(arrs[0], new byte[] {0, 1, 2, 3, 4});
		assert Arrays.equals(arrs[1], new byte[] {5, 6, 7, 8, 9});
		assert Arrays.equals(arrs[2], new byte[] {10, 11, 12, 13, 14});
		assert Arrays.equals(arrs[3], new byte[] {15, 16, 17, 18, 19});
		assert arrs.length == 4;
	}
	
	@Test
	public static void test2() {
		byte[] arr = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
		byte[][] arrs = SerializationUtils.split(arr, 7);
		assert Arrays.equals(arrs[0], new byte[] {0, 1, 2, 3, 4, 5, 6});
		assert Arrays.equals(arrs[1], new byte[] {7, 8, 9, 10, 11, 12, 13});
		assert Arrays.equals(arrs[2], new byte[] {14, 15, 16, 17, 18, 19});
		assert arrs.length == 3;
	}
	
}
