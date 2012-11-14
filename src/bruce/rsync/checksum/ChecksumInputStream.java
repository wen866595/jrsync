package bruce.rsync.checksum;

import java.io.Closeable;

public interface ChecksumInputStream extends Closeable {
	Checksum readChecksum();
}
