package bruce.rsync.checksum;

public class Checksum {
	private int index;
	private int weakChecksum;
	private byte[] strongChecksum;

	public Checksum(int weakChecksum, byte[] strongChecksum) {
		this.weakChecksum = weakChecksum;
		this.strongChecksum = strongChecksum;
	}

	/**
	 * 弱校验和
	 */
	public int getWeakChecksum() {
		return weakChecksum;
	}

	/**
	 * 强校验和
	 */
	public byte[] getStrongChecksum() {
		return strongChecksum;
	}

	/**
	 * 块号
	 */
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(200);

		sb.append("trunk index: ").append(index).append(", weacChecksum: ")
				.append(Integer.toHexString(weakChecksum))
				.append(", strongChecksum: ");

		for (byte b : strongChecksum) {
			String hex = Integer.toHexString(b & 0xff);
			if(hex.length() < 2) {
				sb.append('0');
			}
			sb.append(hex);
		}

		return sb.toString();
	}
}
