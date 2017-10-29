package sidechannel.cost.approximate.monitor;

import java.io.OutputStream;

public class FileMonitor extends OutputStreamMonitor {

	@Override
	int getSource() {
		return OutputStream.FILE;
	}

}
