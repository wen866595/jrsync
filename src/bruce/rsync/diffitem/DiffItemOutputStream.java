package bruce.rsync.diffitem;

import java.io.Closeable;

public interface DiffItemOutputStream extends Closeable {
	void writeDiffItem(DiffItem item);
}
