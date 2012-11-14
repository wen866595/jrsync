package bruce.rsync.diffitem;

public class DiffItem {

	/**
	 * 是否匹配
	 */
	private boolean isMatch;

	/**
	 * 如果匹配，匹配的块号
	 */
	private int trunkIndex;

	/**
	 * 如果不匹配，此为新增数据
	 */
	private byte[] data;

	public DiffItem(int trunkIndex) {
		isMatch = true;
		this.trunkIndex = trunkIndex;
	}

	public DiffItem(byte[] data) {
		isMatch = false;
		this.data = data;
	}

	public boolean isMatch() {
		return isMatch;
	}

	public int getTrunkIndex() {
		return trunkIndex;
	}

	public byte[] getData() {
		return data;
	}

}
