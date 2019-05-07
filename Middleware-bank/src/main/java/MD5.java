import org.apache.commons.codec.digest.DigestUtils;

public class MD5 {
    public static String md5Hash(String s){
        return DigestUtils.md5Hex(s).toUpperCase();
    }
}
