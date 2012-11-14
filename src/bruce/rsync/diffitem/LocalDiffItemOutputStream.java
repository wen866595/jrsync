package bruce.rsync.diffitem;

import java.util.LinkedList;
import java.util.List;

public class LocalDiffItemOutputStream implements DiffItemOutputStream {
	private boolean isClosed = false;
	private List<DiffItem> list = new LinkedList<DiffItem>();

	@Override
	public void close() {
		isClosed = true;
	}

	@Override
	public void writeDiffItem(DiffItem item) {
		if (isClosed) {
			throw new IllegalStateException(
					"cann't write DiffItem to a closed DiffItemOutputStream .");
		}

		list.add(item);
	}

	public DiffItemInputStream toDiffItemInputStream() {
		return new LocalDiffItemInputStream(list);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(512);
		for (DiffItem item : list) {
			if (item.isMatch()) {
				sb.append("match server trunk index :" + item.getTrunkIndex()
						+ "\n");
			} else {
				sb.append("new trunk. data is :[" + new String(item.getData()))
						.append("]\n");
			}
		}
		return sb.toString();
	}
}
