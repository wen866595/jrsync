package bruce.rsync;


public class Rsync {

	public static int adler32(byte[] data, int offset, int length) {
		int a = 0;
		int b = 0;

		for (int i = offset, limit = i + length; i < limit; i++) {
			a += data[i] & 0xff;
			if (a >= Constant.MOD_ADLER) {
				a -= Constant.MOD_ADLER;
			}

			b += a;
			if (b >= Constant.MOD_ADLER) {
				b -= Constant.MOD_ADLER;
			}
		}

		return b << 16 | a;
	}

	public static int adler32(byte[] data, int length) {
		return adler32(data, 0, length);
	}

	/**
	 * @param data
	 * @return
	 */
	public static int adler32(byte[] data) {
		return adler32(data, 0, data.length);
	}

	/**
	 * @param oldAdler32
	 * @param preByte
	 * @param nextByte
	 * @return
	 */
	public static int nextAdler32(int oldAdler32, byte preByte, byte nextByte) {
		int a = oldAdler32 & 0xffff;
		int b = (oldAdler32 >>> 16) & 0xffff;

		int an = a - preByte + nextByte;

		int bn = b - (preByte) * Constant.TRUNCK_SIZE + an;

		return (bn << 16) + (an & 0xffff);
	}

	public static void main(String[] args) {
		byte[] data = ",456".getBytes();

		int adler32 = adler32(data); // 32506060
		System.out.println(adler32);

		java.util.zip.Adler32 javaAdler32 = new java.util.zip.Adler32();
		javaAdler32.update(",456".getBytes());
		System.out.println(javaAdler32.getValue());

		System.out.println(adler32("4567".getBytes())); // 34996439

		int nextAdler32 = nextAdler32(32506060, (byte) 44, (byte) 55);
		System.out.println(nextAdler32);
		
		System.out.println(adler32("5678".getBytes()));		// 35651803
		System.out.println(nextAdler32(34996439, (byte) 52, (byte) 56));
	}
}
