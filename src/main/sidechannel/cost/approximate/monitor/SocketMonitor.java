package sidechannel.cost.approximate.monitor;

import org.omg.CORBA_2_3.portable.OutputStream;

public class SocketMonitor extends OutputStreamMonitor {

	@Override
	int getSource() {
		return OutputStream.SOCKET;
	}

}
