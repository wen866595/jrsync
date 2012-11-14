package bruce.rsync.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import bruce.rsync.checksum.ChecksumInputStream;
import bruce.rsync.checksum.LocalChecksumOutputStream;
import bruce.rsync.client.Differ;
import bruce.rsync.diffitem.LocalDiffItemOutputStream;
import bruce.rsync.server.RsyncServer;

public class TestDiffer {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		LocalChecksumOutputStream outputStream = new LocalChecksumOutputStream();
		RsyncServer.buildChecksumOutputStream("small_server.txt", outputStream);
		System.out.println(outputStream);
		
		Differ differ = new Differ();
		ChecksumInputStream checksumInputStream = outputStream.toChecksumInputStream();
		FileInputStream clientFileInputStream = new FileInputStream("small_client.txt");
		LocalDiffItemOutputStream diffOutputStream = new LocalDiffItemOutputStream();
		differ.diff(clientFileInputStream, checksumInputStream, diffOutputStream);
		
		System.out.println(diffOutputStream);
	}

}
