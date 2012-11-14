package bruce.rsync.checksum;

import java.util.Iterator;
import java.util.List;

public class LocalChecksumInputStream implements ChecksumInputStream {
	private boolean isClosed = false;
	private Iterator<Checksum> iterator;

	public LocalChecksumInputStream(List<Checksum> list) {
		iterator = list.iterator();
	}

	@Override
	public void close() {
		isClosed = true;
	}

	@Override
	public Checksum readChecksum() {
		if (isClosed) {
			throw new IllegalStateException(
					"cann't read Checksum from a closed ChecksumInputStream .");
		}

		if (iterator.hasNext()) {
			return iterator.next();
		}

		return null;
	}

}
