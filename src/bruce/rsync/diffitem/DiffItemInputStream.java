package bruce.rsync.diffitem;

import java.io.Closeable;

public interface DiffItemInputStream extends Closeable {
	DiffItem readDiffItem();
}
