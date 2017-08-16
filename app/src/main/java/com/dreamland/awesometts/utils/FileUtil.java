package com.dreamland.awesometts.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by XMD on 2017/1/17.
 */

public class FileUtil {
    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    public static String getFolderName(String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    public static void copyFromAssetsDirToSdcard(Context context, String sourceDir, String destDir) {
        try {
            String[] reslist = context.getAssets().list(sourceDir);
            for (int i = 0; i < reslist.length; i++) {
                copyFromAssetsToSdcard(context.getAssets(), false, sourceDir + File.separator + reslist[i], destDir + File.separator + reslist[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param assets
     * @param isCover
     * @param source
     * @param dest
     */
    public static void copyFromAssetsToSdcard(AssetManager assets, boolean isCover, String source, String dest) {
        File file = new File(dest);

        if (isCover || !file.exists() || file.length() == 0) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String temp = dest + ".temp";
                fos = new FileOutputStream(temp);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
                fos.close();
                File tempFile = new File(temp);
                if (file.exists()) {
                    file.delete();
                }
                tempFile.renameTo(file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param context
     * @param isCover
     * @param source
     * @param dest
     */

    public static void copyFromAssetsToSdcard(Context context, boolean isCover, String source, String dest) {
        copyFromAssetsToSdcard(context.getAssets(), isCover, source, dest);
    }

    /**
     * read file
     *
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            return readFile(new FileInputStream(file), charsetName);
        } catch (FileNotFoundException e) {
        }
        return null;
    }


    public static StringBuilder readFile(InputStream fis, String charsetName) {
        StringBuilder fileContent = new StringBuilder("");
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(fis, charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    public static String getFileEncoding(String filePath){
        BufferedInputStream bin = null;
        String code = "GBK";
        try {
            bin = new BufferedInputStream(
                    new FileInputStream(filePath));
            int p = (bin.read() << 8) + bin.read();
            Log.e("DADA",String.format("0x%x",p));
            switch (p) {
                case 0xefbb:
                    code = "UTF-8";
                    break;
                case 0xfffe:
                    code = "Unicode";
                    break;
                case 0xfeff:
                    code = "UTF-16BE";
                    break;
                default:
                    code = "GBK";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bin != null){
                try {
                    bin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return code;
    }
}
