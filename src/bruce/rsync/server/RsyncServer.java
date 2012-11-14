package bruce.rsync.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import bruce.rsync.Constant;
import bruce.rsync.Rsync;
import bruce.rsync.checksum.Checksum;
import bruce.rsync.checksum.ChecksumOutputStream;
import bruce.rsync.checksum.LocalChecksumOutputStream;

public class RsyncServer {

	public static void buildChecksumOutputStream(String fileName,
			ChecksumOutputStream outputStream) throws NoSuchAlgorithmException,
			IOException {

		FileInputStream fin = new FileInputStream(fileName);
		try {
			checksumList(fin, outputStream);

		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	/**
	 * 根据给定的输入流计算checksum列表
	 * 
	 * @param input
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void checksumList(InputStream input,
			ChecksumOutputStream outputStream) throws IOException,
			NoSuchAlgorithmException {

		if (input == null) {
			throw new NullPointerException();
		}

		byte[] buffer = new byte[Constant.TRUNCK_SIZE];
		int readSize = -1;

		int trunkIndex = 0;

		do {
			readSize = input.read(buffer);

			if (readSize > 0) {
				Checksum checksum = calcChecksum(buffer, readSize);
				checksum.setIndex(trunkIndex++);
				outputStream.writeChecksum(checksum);
			} else {
				break;
			}
		} while (true);
	}

	private static Checksum calcChecksum(byte[] buffer, int length)
			throws NoSuchAlgorithmException {

		int adler32 = Rsync.adler32(buffer, length);

		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(buffer);
		byte[] result = digest.digest();

		return new Checksum(adler32, result);
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException {
		LocalChecksumOutputStream outputStream = new LocalChecksumOutputStream();
		buildChecksumOutputStream("client_test.txt", outputStream);

	}

}
