package bruce.rsync;

public class Constant {
	/**
	 * trunck块大小，byte
	 */
	public final static int TRUNCK_SIZE = 4;

	/**
	 * 小于1<<16 (65536) 的最小素数
	 */
	public final static int MOD_ADLER = 65521;

	/**
	 * 用于计算强校验和的方法
	 */
	public final static String STRONG_CHECKSUM_METHOD = "MD5";
}
