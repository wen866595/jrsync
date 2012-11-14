package bruce.rsync.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import bruce.rsync.Constant;
import bruce.rsync.Rsync;
import bruce.rsync.checksum.Checksum;
import bruce.rsync.checksum.ChecksumInputStream;
import bruce.rsync.diffitem.DiffItem;
import bruce.rsync.diffitem.DiffItemOutputStream;

public class Differ {
	private final int limit = Constant.TRUNCK_SIZE * 4;
	private byte[] buffer = new byte[limit];

	/**
	 * 用于缓存两个匹配块之间的新增数据
	 */
	private ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(limit);

	/**
	 * 指向块的首字节
	 */
	private int rollIndex = 0;

	/**
	 * 指向块后的第一个字节
	 */
	private int currentIndex = 0;

	/**
	 * 指向不属于块后的第一个空闲位置
	 */
	private int validLimit = 0;

	/**
	 * 当前块 [rollIndex, currentIndex) 的adler32值
	 */
	private int adler32 = 0;

	/**
	 * 上一个匹配块的结束位置。-1表示无意义 或两次匹配间的新增数据已有部分写入outBuffer里
	 */
	private int preMatchEnd = -1;

	/**
	 * 是否处于滚动查找匹配状态
	 */
	private boolean rolling = false;
	private InputStream inStream;
	private MessageDigest digest;
	private Map<Integer, Checksum> map = new HashMap<Integer, Checksum>();

	public Differ() throws NoSuchAlgorithmException {
		digest = MessageDigest.getInstance(Constant.STRONG_CHECKSUM_METHOD);
	}

	public void diff(InputStream inputStream,
			ChecksumInputStream checksumInputStream,
			DiffItemOutputStream diffOutputStream) throws IOException {

		inStream = inputStream;
		convert2map(checksumInputStream);

		out: do {
			if (!hasTrunkInBuffer()) {
				boolean isStreamEnd = fillBuffer();
				if (isStreamEnd) {
					doStreamEnd(diffOutputStream);
					break;
				}
			}

			calcTrunkAdler32();

			inner: do {
				int matchTrunkIndex = checkMatchIndex();

				if (matchTrunkIndex != -1) {
					doExactMatch(diffOutputStream, matchTrunkIndex);
					rolling = false;
					break inner;

				} else {
					rolling = true;
					boolean isStreamEnd = ensureValidData();
					if (isStreamEnd) {
						doStreamEnd(diffOutputStream);
						break out;
					}

					rollAdler32();
				}
			} while (true);
		} while (true);
	}

	private void rollAdler32() {
		byte oldByte = buffer[rollIndex++];
		byte nextByte = buffer[currentIndex++];
		adler32 = Rsync.nextAdler32(adler32, oldByte, nextByte);
	}

	private boolean hasTrunkInBuffer() {
		return (validLimit - currentIndex) >= Constant.TRUNCK_SIZE;
	}

	private void doExactMatch(DiffItemOutputStream diffOutputStream,
			int matchIndex) {

		wirteRolledValidData2newTrunkBuffer();

		writeNewTrunkIfHas(diffOutputStream);

		// 写块匹配
		DiffItem item = new DiffItem(matchIndex);
		diffOutputStream.writeDiffItem(item);

		rollIndex += Constant.TRUNCK_SIZE;
		preMatchEnd = currentIndex;
	}

	private void doStreamEnd(DiffItemOutputStream diffOutputStream) {
		if (rolling) {
			if (preMatchEnd == -1) {
				outBuffer.write(buffer, 0, validLimit);
			} else {
				outBuffer.write(buffer, preMatchEnd, validLimit - preMatchEnd);
			}
		} else {
			// 刚块匹配之后，缓存还有数据，需要继续检测匹配
			checkLastTrunk(diffOutputStream);
		}

		writeNewTrunkIfHas(diffOutputStream);
	}

	/**
	 * 检测缓存，直到缓存的所有数据被检测完
	 * 
	 * @param diffOutputStream
	 */
	private void checkLastTrunk(DiffItemOutputStream diffOutputStream) {
		out: do {
			if (!hasTrunkInBuffer()) {
				doLastTrunkEnd();
				break;
			}
			calcTrunkAdler32();

			inner: do {
				int matchTrunkIndex = checkMatchIndex();

				if (matchTrunkIndex != -1) {
					doExactMatch(diffOutputStream, matchTrunkIndex);
					break inner;

				} else {
					if (currentIndex < validLimit) {
						rollAdler32();
					} else {
						doLastTrunkEnd();
						break out;
					}
				}
			} while (true);
		} while (true);
	}

	private void doLastTrunkEnd() {
		outBuffer.write(buffer, rollIndex, validLimit - rollIndex);
	}

	/**
	 * 用于滚动查找过程中确保缓存还有有效数据。如果没有则填充，返回流是否结束
	 * 
	 * @return 流是否结束
	 * @throws IOException
	 */
	private boolean ensureValidData() throws IOException {
		if (validLimit <= currentIndex) {
			// 没有空闲空间
			return fillBuffer();
		}

		return false;
	}

	private int checkMatchIndex() {
		Checksum checksum = map.get(adler32);
		if (checksum != null) {
			digest.update(buffer, rollIndex, Constant.TRUNCK_SIZE);
			byte[] bs = digest.digest();
			boolean eq = Arrays.equals(bs, checksum.getStrongChecksum());
			return eq ? checksum.getIndex() : -1;
		}

		return -1;
	}

	/**
	 * 计算从rollIndex开始，Constant.TRUNCK_SIZE长的块的adler32值
	 */
	private void calcTrunkAdler32() {
		adler32 = Rsync.adler32(buffer, rollIndex, Constant.TRUNCK_SIZE);
		currentIndex = rollIndex + Constant.TRUNCK_SIZE;
	}

	/**
	 * 填充缓存
	 * 
	 * @return 流是否结束
	 * @throws IOException
	 */
	private boolean fillBuffer() throws IOException {
		wirteRolledValidData2newTrunkBuffer();
		preMatchEnd = -1;

		int remain = validLimit - rollIndex;
		System.arraycopy(buffer, rollIndex, buffer, 0, remain);
		rollIndex = 0;
		currentIndex = remain;
		validLimit = remain;

		int toReadLen = limit - validLimit;
		int readed = inStream.read(buffer, validLimit, toReadLen);
		if (readed >= 0) {
			validLimit += readed;
			return false;
		}

		return true;
	}

	private void writeNewTrunkIfHas(DiffItemOutputStream diffOutputStream) {
		if (outBuffer.size() > 0) {
			DiffItem item = new DiffItem(outBuffer.toByteArray());
			diffOutputStream.writeDiffItem(item);
			outBuffer.reset();
		}
	}

	/**
	 * 把buffer里滚动过的有效数据写入新增数据块缓存
	 */
	private void wirteRolledValidData2newTrunkBuffer() {
		if (preMatchEnd != -1) {
			outBuffer.write(buffer, preMatchEnd, rollIndex - preMatchEnd);
		} else {
			outBuffer.write(buffer, 0, rollIndex);
		}
	}

	private void convert2map(ChecksumInputStream checksumInputStream) {
		Checksum checksum = null;
		while ((checksum = checksumInputStream.readChecksum()) != null) {
			map.put(checksum.getWeakChecksum(), checksum);
		}
	}
}
