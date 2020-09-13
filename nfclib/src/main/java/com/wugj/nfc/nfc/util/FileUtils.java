package com.wugj.nfc.nfc.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.wugj.nfc.base.BaseNfcApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FileUtils {

    public enum Path{
        MCPath{
            public File getFile1(boolean isUseInternal){
                return getFileFromStorage("MifareClassic/keys-files/std.keys",isUseInternal);
            }

            public File getFile2(boolean isUseInternal){
                return getFileFromStorage("MifareClassic/keys-files/extended-std.keys",isUseInternal);
            }
        };

        public File getFile1(boolean isUseInternal){

            throw new AbstractMethodError();
        }

        public File getFile2(boolean isUseInternal){

            throw  new AbstractMethodError();
        }


    }


    private static String LOG_TAG = FileUtils.class.getSimpleName();


    /**
     * Read a file line by line. The file should be a simple text file.
     * Empty lines and lines STARTING with "#" will not be interpreted.
     * @param file The file to read.
     * @param readComments Whether to read comments or to ignore them.
     * Comments are lines STARTING with "#" (and empty lines).
     * will be shown.
     * @return Array of strings representing the lines of the file.
     * If the file is empty or an error occurs "null" will be returned.
     */
    public static String[] readFileLineByLine(File file, boolean readComments
                                              ) {
        BufferedReader br = null;
        String[] ret = null;
        if (file != null  && isExternalStorageMounted() && file.exists()) {
            try {
                br = new BufferedReader(new FileReader(file));

                String line;
                ArrayList<String> linesArray = new ArrayList<>();
                while ((line = br.readLine()) != null)   {
                    // Ignore empty lines.
                    // Ignore comments if readComments == false.
                    if ( !line.equals("")
                            && (readComments || !line.startsWith("#"))) {
                        try {
                            linesArray.add(line);
                        } catch (OutOfMemoryError e) {
                            // Error. File is too big
                            // (too many lines, out of memory).
                            Log.e(LOG_TAG,"file is too big OutOfMemoryError");
                            return null;
                        }
                    }
                }
                if (linesArray.size() > 0) {
                    ret = linesArray.toArray(new String[linesArray.size()]);
                } else {
                    ret = new String[] {""};
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error while reading from file "
                        + file.getPath() + "." ,e);
                ret = null;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException e) {
                        Log.e(LOG_TAG, "Error while closing file.", e);
                        ret = null;
                    }
                }
            }
        }
        return ret;
    }


    /**
     * Checks if external storage is available for read and write.
     * @return True if external storage is writable. False otherwise.
     */
    public static boolean isExternalStorageMounted() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }



    /**
     * Checks if external storage is available for read and write.
     * If not, show an error Toast.
     * @param context The Context in which the Toast will be shown.
     * @return True if external storage is writable. False otherwise.
     */
    public static boolean isExternalStorageWritableErrorToast(
            Context context) {
        if (!isExternalStorageMounted()) {
            Toast.makeText(context, "外部存储卡不可用",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    /**
     * Check if the user granted read/write permissions to the external storage.
     * @param context The Context to check the permissions for.
     * @return True if granted the permissions. False otherwise.
     */
    public static boolean hasWritePermissionToExternalStorage(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }



    /**
     * Write an array of strings (each field is one line) to a given file.
     * @param file The file to write to.
     * @param lines The lines to save.
     * @param append Append to file (instead of replacing its content).
     * @return True if file writing was successful. False otherwise.
     */
    public static boolean saveFile(File file, String[] lines, boolean append) {
        boolean noError = true;
        if (file != null && lines != null && isExternalStorageMounted()) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(file, append));
                // Add new line before appending.
                if (append) {
                    bw.newLine();
                }
                int i;
                for(i = 0; i < lines.length-1; i++) {
                    bw.write(lines[i]);
                    bw.newLine();
                }
                bw.write(lines[i]);
            } catch (IOException | NullPointerException ex) {
                Log.e(LOG_TAG, "Error while writing to '"
                        + file.getName() + "' file.", ex);
                noError = false;

            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error while closing file.", e);
                        noError = false;
                    }
                }
            }
        } else {
            noError = false;
        }
        return noError;
    }



    /**
     * Create a File object with a path that consists of its storage
     * (internal/external according to its preference) and the relative
     * path.
     * @param relativePath The relative path that gets appended to the
     * internal or external storage path part
     * @param isUseInternalStorage
     * @return A File object with the absolute path of the storage and the
     * relative component given by the parameter.
     */
    public static File getFileFromStorage(String relativePath ,boolean isUseInternalStorage) {
        File file;
        if (isUseInternalStorage) {
            // Use internal storage.
            file = new File(BaseNfcApplication.getApplication().getFilesDir() + relativePath);
        } else {
            // Use external storage (default).
            file = new File(Environment.getExternalStorageDirectory() +
                    relativePath);
        }
        return file;
    }


    /**
     * Append an array of strings (each field is one line) to a given file.
     * @param file The file to write to.
     * @param lines The lines to save.
     * @param comment If true, add a comment before the appended section.
     * @return True if file writing was successful. False otherwise.
     */
    public static boolean saveFileAppend(File file, String[] lines,
                                         boolean comment) {
        if (comment) {
            // Append to a existing file.
            String[] newLines = new String[lines.length + 4];
            System.arraycopy(lines, 0, newLines, 4, lines.length);
            newLines[1] = "";
            newLines[2] = "# Append #######################";
            newLines[3] = "";
            lines = newLines;
        }
        return saveFile(file, lines, true);
    }



    /**
     * Key files are simple text files. Any plain text editor will do the trick.
     * All key and dump data from this App is stored in
     * getExternalStoragePublicDirectory(Common.HOME_DIR) to remain
     * there after App uninstallation.
     */
    public static void copyStdKeysFilesIfNecessary(boolean isUseInternalStorage,String assetsPath,String targetPath) {
        File targetFile = getFileFromStorage(targetPath,isUseInternalStorage);
        AssetManager assetManager = BaseNfcApplication.getApplication().getAssets();

        if (!targetFile.exists()) {
            // Copy std.keys.
            try {
                InputStream in = assetManager.open(assetsPath);
                OutputStream out = new FileOutputStream(targetFile);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch(IOException e) {
                Log.e(LOG_TAG, "Error while copying 'std.keys' from assets "
                        + "to external storage.");
            }
        }
    }

    /**
     * Copy file.
     * @param in Input file (source).
     * @param out Output file (destination).
     * @throws IOException Error upon coping.
     */
    public static void copyFile(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
