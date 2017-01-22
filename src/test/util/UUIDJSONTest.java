package test.util;

import org.json.JSONException;

import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.util.UUID;

public class UUIDJSONTest {

	public static void main(String[] args) throws JSONException {
		test1();
	}
	
	@Test
	public static void test1() throws JSONException {
		UUID id = new UUID();
		String serialized = id.toString();
		UUID id2 = new UUID(serialized);
		assert id.equals(id2);
	}
	
}
