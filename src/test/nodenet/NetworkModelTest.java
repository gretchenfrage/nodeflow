package test.nodenet;

import java.util.Arrays;

import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.ptest.Test;


public class NetworkModelTest {

	@Test
	public static void test1() {
		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(3));
		model.connect(new NodeAddress(1), new NodeAddress(5));
		model.connect(new NodeAddress(2), new NodeAddress(3));
		model.connect(new NodeAddress(2), new NodeAddress(5));
		model.connect(new NodeAddress(4), new NodeAddress(5));

		model.connect(new NodeAddress(4), new NodeAddress(2));
		model.disconnect(new NodeAddress(2), new NodeAddress(4));

		model.connect(new NodeAddress(6), new NodeAddress(7));

		model.disconnect(new NodeAddress(3245867), new NodeAddress(918723));

		assert model.connected(new NodeAddress(1), new NodeAddress(3));
		assert model.connected(new NodeAddress(4), new NodeAddress(3));
		assert model.connected(new NodeAddress(6), new NodeAddress(7));
		assert !model.connected(new NodeAddress(1), new NodeAddress(6));

		assert model.distance(new NodeAddress(4), new NodeAddress(3)).getAsInt() == 3;
		assert model.distance(new NodeAddress(1), new NodeAddress(1)).getAsInt() == 0;
		assert !model.distance(new NodeAddress(5), new NodeAddress(6)).isPresent();
		
		model.disconnect(new NodeAddress(3), new NodeAddress(1));
		
		assert model.connected(new NodeAddress(4), new NodeAddress(3));
		assert model.path(new NodeAddress(4), new NodeAddress(3)).equals(Arrays.asList(
				new NodeAddress[] { new NodeAddress(4), new NodeAddress(5), new NodeAddress(2), new NodeAddress(3) }));
	}

}
