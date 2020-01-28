package cn.innc11.updater.server.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5
{
	public static String getMD5(File File)
	{
		String md5 = "";
		try {
			FileInputStream fileInputStream = null;
			DigestInputStream digestInputStream = null;

			MessageDigest digest = MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(File);
			digestInputStream = new DigestInputStream(fileInputStream, digest);

			byte[] buffer =new byte[8*1024];
			while (digestInputStream.read(buffer) > 0);

			digest = digestInputStream.getMessageDigest();

			fileInputStream.close();
			digestInputStream.close();

			md5 = byteArrayToHex(digest.digest());
		}
		catch (NoSuchAlgorithmException | IOException e) {e.printStackTrace();}
		return md5;
	}

	private static String byteArrayToHex(byte[] byteArray) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < byteArray.length; n++) {
			stmp = (Integer.toHexString(byteArray[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
			if (n < byteArray.length - 1) {
				hs = hs + "";
			}
		}
		// return hs.toUpperCase();
		return hs;

		// 首先初始化一个字符数组，用来存放每个16进制字符

      /*char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'A','B','C','D','E','F' };



      // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））

      char[] resultCharArray =new char[byteArray.length * 2];

      // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去

      int index = 0;

      for (byte b : byteArray) {

         resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];

         resultCharArray[index++] = hexDigits[b& 0xf];

      }

      // 字符数组组合成字符串返回

      return new String(resultCharArray);*/

	}
}
