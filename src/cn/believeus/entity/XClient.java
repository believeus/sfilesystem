package cn.believeus.entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XClient {
	public static void main(String[] args) {
		try {
			final Scanner scanner = new Scanner(System.in);
			System.out.print("�������ϴ��ļ���ַ:");
			String filename = "d:/usr/local/jdk-6u45-windows-x64.exe";
			final File file = new File(filename);
			if(!file.exists()){
				System.out.println("�ļ�������!�����ļ�·���Ƿ���ȷ!");
				return;
			}
			final long filesize = file.length();
			final int block = 1024 * 1024 * 10;
			final int times = (int)Math.ceil(filesize / (float) block);
			//��������һ����ļ���С (1024*1024*10)x3+10
			final int endsize=(int)(filesize-((times-1)*block));
			final String fileId = UUID.randomUUID().toString();
			ExecutorService pool = Executors.newFixedThreadPool(3);
			for (int i = 0; i < times; i++) {
				final int j=i;
				pool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							Socket socket = new Socket("192.168.3.20", 9999);
							OutputStream o = socket.getOutputStream();
							InputStream in = socket.getInputStream();
							DataInputStream datain = new DataInputStream(in);
							// ������
							DataOutputStream dataout = new DataOutputStream(o);
							// ��ȡ�ļ���׺
							String stuffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);// /
							// 0.���ļ��������ȥ
							dataout.writeUTF(fileId + "-" + j);
							// 1.���ļ���׺д������� // pp.txt
							dataout.writeUTF(stuffix);
							// 2.��ÿ�ζ��Ĵ�Сд�������,���һ���ļ���С<=block
							int blocksize=(j==(times-1))?endsize:block;
							dataout.writeInt(blocksize);
							RandomAccessFile rFile = new RandomAccessFile(file, "r");
							rFile.seek(j * block);
							byte[] buf = new byte[block];
							int len = rFile.read(buf);
							// ����ÿ�η��͵�����˵�������block��С
							//����:���û�д�����ɣ�Ҳ����˵��Ϊ���������
							//�����ݲ���һ���ԣ������ȥ�ģ����ߴ����ʱ���ǲ����δ���
							dataout.write(buf, 0, len);
							dataout.flush();
							scanner.close();
							//����ȴ�������������ϣ����е����Źر�
							datain.readUTF();
							dataout.close();
							socket.close();
							rFile.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			pool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
