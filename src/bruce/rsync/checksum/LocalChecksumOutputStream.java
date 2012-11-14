package bruce.rsync.checksum;

import java.util.LinkedList;
import java.util.List;

public class LocalChecksumOutputStream implements ChecksumOutputStream {
	private List<Checksum> list = new LinkedList<Checksum>();
	private boolean isClosed = false;

	@Override
	public void writeChecksum(Checksum checksum) {
		if (isClosed) {
			throw new IllegalArgumentException(
					"can't write to a closed ChecksumOutputStream .");
		}

		list.add(checksum);
	}

	@Override
	public void close() {
		isClosed = true;
	}

	public ChecksumInputStream toChecksumInputStream() {
		return new LocalChecksumInputStream(list);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		for (Checksum item : list) {
			sb.append("trunk " + item.getIndex() + ", weakchaekSum :"
					+ (item.getWeakChecksum() & 0xff) + "\n");
			// ", strong checksum :"+ Arrays.toString(item.getStrongChecksum())
		}

		return sb.toString();
	}

}
