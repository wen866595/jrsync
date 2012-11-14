package bruce.rsync.diffitem;

import java.util.Iterator;
import java.util.List;

public class LocalDiffItemInputStream implements DiffItemInputStream {
	private boolean isClosed = false;
	private Iterator<DiffItem> iterator;

	public LocalDiffItemInputStream(List<DiffItem> list) {
		iterator = list.iterator();
	}

	@Override
	public void close() {
		isClosed = true;
	}

	@Override
	public DiffItem readDiffItem() {
		if (isClosed) {
			throw new IllegalStateException(
					"cann't read DiffItem from a closed DiffItemInputStream .");
		}

		return iterator.next();
	}

}
