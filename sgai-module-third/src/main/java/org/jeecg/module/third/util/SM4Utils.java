package org.jeecg.module.third.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;

public class SM4Utils {
    private static final String sm4Key = "cp1qp+SmFcSSHw==";

    private static final byte[] KEY = sm4Key.getBytes(CharsetUtil.CHARSET_UTF_8);

    /**
     * sm4 加密
     *
     * @param content 需要解密的内容
     * @return
     */
    public static String sm4EncryptHex(String content) {
        try {
            SM4 sm4 = SmUtil.sm4(KEY);
            return sm4.encryptBase64(content, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * @param content
     * @return
     */
    public static String sm4decrypt(String content) {
        try {
            SM4 sm4 = SmUtil.sm4(KEY);
            return sm4.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * @param content
     * @return
     */
    public static String sm4decryptReturnNull(String content) {
        try {
            SM4 sm4 = SmUtil.sm4(KEY);
            return sm4.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String str = "username=ceshi&time="+System.currentTimeMillis();
        String enc = sm4EncryptHex(str);
        System.out.println(enc);
        System.out.println(sm4decryptReturnNull(enc));
    }
}