package link.androidapps.quicknotes;

import android.app.Application;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by plamen on 05.11.15.
 */
public class QuickNotesApplication extends Application {
	private static String md5;
	private static String MD5_DIGEST_STRING = "quick notes backup";

	static {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("MD5");
			byte[] digest = mDigest.digest(MD5_DIGEST_STRING.getBytes("UTF-8"));
			md5 = new String(Hex.encodeHex(digest));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Log.e("APPLICATION_CLASS", e.getMessage());
		}
	}

	public static String getMd5() {
		return md5;
	}
}
