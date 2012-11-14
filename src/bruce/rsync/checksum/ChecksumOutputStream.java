package bruce.rsync.checksum;

import java.io.Closeable;

public interface ChecksumOutputStream extends Closeable {
	void writeChecksum(Checksum checksum);
}
