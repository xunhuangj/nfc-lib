package com.wugj.nfc.nfc.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.wugj.nfc.R;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

public class Utils {


    /**
     * Convert Dips to pixels.
     * @param dp Dips.
     * @return Dips as px.
     */
    public static int dpToPx(int dp,float mScale) {
        return (int) (dp * mScale + 0.5f);
    }


    /**
     * Share a file from the "tmp" directory as attachment.
     * @param context The context the FileProvider and the share intent.
     * @param file The file to share (from the "tmp" directory).
     */
    public static void shareTmpFile(Context context, File file) {
        // Share file.
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri;
        try {
            uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
        } catch (IllegalArgumentException ex) {
            UIRun.toastLength(R.string.info_share_error);
            return;
        }
        intent.setDataAndType(uri, "text/plain");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent,
                context.getText(R.string.dialog_share_title)));
    }


    /**
     * Get the content of the Android clipboard (if it is plain text).
     * @param context Context of the SystemService
     * @return The content of the Android clipboard. On error
     * (clipboard empty, clipboard content not plain text, etc.) null will
     * be returned.
     */
    public static String getFromClipboard(Context context) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)
                        context.getSystemService(
                                Context.CLIPBOARD_SERVICE);
        if (clipboard.getPrimaryClip() != null
                && clipboard.getPrimaryClip().getItemCount() > 0
                && clipboard.getPrimaryClipDescription().hasMimeType(
                android.content.ClipDescription.MIMETYPE_TEXT_PLAIN)
                && clipboard.getPrimaryClip().getItemAt(0) != null
                && clipboard.getPrimaryClip().getItemAt(0)
                .getText() != null) {
            return clipboard.getPrimaryClip().getItemAt(0)
                    .getText().toString();
        }

        // Error.
        return null;
    }

    /**
     * Copy a text to the Android clipboard.
     * @param text The text that should by stored on the clipboard.
     * @param context Context of the SystemService
     * (and the Toast message that will by shown).
     * @param showMsg Show a "Copied to clipboard" message.
     */
    public static void copyToClipboard(String text, Context context,
                                       boolean showMsg) {
        if (!text.equals("")) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager)
                            context.getSystemService(
                                    Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText(
                            "MIFARE classic tool data", text);
            clipboard.setPrimaryClip(clip);
            if (showMsg) {
                Toast.makeText(context, R.string.info_copied_to_clipboard,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Check if a (hex) string is pure hex (0-9, A-F, a-f) and 16 byte
     * (32 chars) long. If not show an error Toast in the context.
     * @param hexString The string to check.
     * @param context The Context in which the Toast will be shown.
     * @return True if sting is hex an 16 Bytes long, False otherwise.
     */
    public static boolean isHexAnd16Byte(String hexString, Context context) {
        if (!hexString.matches("[0-9A-Fa-f]+")) {
            // Error, not hex.
            UIRun.toastLength( R.string.info_not_hex_data);
            return false;
        }

        if (hexString.length() != 32) {
            // Error, not 16 byte (32 chars).
            UIRun.toastLength( R.string.info_not_16_byte);
            return false;
        }
        return true;
    }



    private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


    public static byte[] toBytes(int a) {
        return new byte[] { (byte) (0x000000ff & (a >>> 24)),
                (byte) (0x000000ff & (a >>> 16)),
                (byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
    }

    public static int toInt(byte[] b, int s, int n) {
        int ret = 0;

        final int e = s + n;
        for (int i = s; i < e; ++i) {
            ret <<= 8;
            ret |= b[i] & 0xFF;
        }
        return ret;
    }

    public static int toIntR(byte[] b, int s, int n) {
        int ret = 0;

        for (int i = s; (i >= 0 && n > 0); --i, --n) {
            ret <<= 8;
            ret |= b[i] & 0xFF;
        }
        return ret;
    }

    public static int toInt(byte... b) {
        int ret = 0;
        for (final byte a : b) {
            ret <<= 8;
            ret |= a & 0xFF;
        }
        return ret;
    }

    public static String toHexString(byte[] d, int s, int n) {
        final char[] ret = new char[n * 2];
        final int e = s + n;

        int x = 0;
        for (int i = s; i < e; ++i) {
            final byte v = d[i];
            ret[x++] = HEX[0x0F & (v >> 4)];
            ret[x++] = HEX[0x0F & v];
        }
        return new String(ret);
    }

    public static String toHexStringR(byte[] d, int s, int n) {
        final char[] ret = new char[n * 2];

        int x = 0;
        for (int i = s + n - 1; i >= s; --i) {
            final byte v = d[i];
            ret[x++] = HEX[0x0F & (v >> 4)];
            ret[x++] = HEX[0x0F & v];
        }
        return new String(ret);
    }

    public static String toHexStringR(byte[] d) {

        return toHexString(d,0,0);
    }

    public static int parseInt(String txt, int radix, int def) {
        int ret;
        try {
            ret = Integer.valueOf(txt, radix);
        } catch (Exception e) {
            ret = def;
        }

        return ret;
    }

    public static String toAmountString(float value) {
        return String.format("%.2f", value);
    }




    /**
     * Convert an array of bytes into a string of hex values.
     * @param bytes Bytes to convert.
     * @return The bytes in hex string format.
     */
    public static String byte2HexString(byte[] bytes) {
        StringBuilder ret = new StringBuilder();
        if (bytes != null) {
            for (Byte b : bytes) {
                ret.append(String.format("%02X", b.intValue() & 0xFF));
            }
        }
        return ret.toString();
    }

    /**
     * Convert a string of hex data into a byte array.
     * @param s The hex string to convert
     * @return An array of bytes with the values of the string.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {
            Log.d("Utils", "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }


    /**
     * Reverse a byte Array (e.g. Little Endian -> Big Endian).
     * Hmpf! Java has no Array.reverse(). And I don't want to use
     * Commons.Lang (ArrayUtils) from Apache....
     * @param array The array to reverse (in-place).
     */
    public static void reverseByteArrayInPlace(byte[] array) {
        for(int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }


    /**
     * Convert byte array to a string of the specified format.
     * Format value corresponds to the pref radio button sequence.
     * @param bytes Bytes to convert.
     * @param fmt Format (0=Hex; 1=DecBE; 2=DecLE).
     * @return The bytes in the specified format.
     */
    public static String byte2FmtString(byte[] bytes, int fmt) {
        switch(fmt) {
            case 2:
                byte[] revBytes = bytes.clone();
                reverseByteArrayInPlace(revBytes);
                return hex2Dec(byte2HexString(revBytes));
            case 1:
                return hex2Dec(byte2HexString(bytes));
        }
        return byte2HexString(bytes);
    }

    /**
     * Convert a hexadecimal string to a decimal string.
     * Uses BigInteger only if the hexadecimal string is longer than 7 bytes.
     * @param hexString The hexadecimal value to convert.
     * @return String representation of the decimal value of hexString.
     */
    public static String hex2Dec(String hexString) {
        String ret;
        if (hexString == null || hexString.isEmpty()) {
            ret = "0";
        } else if (hexString.length() <= 14) {
            ret = Long.toString(Long.parseLong(hexString, 16));
        } else {
            BigInteger bigInteger = new BigInteger(hexString , 16);
            ret = bigInteger.toString();
        }
        return ret;
    }




    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    public static byte[] byteMergerAll(List<byte []> values) {
        int length_byte = 0;
        for (int i = 0; i < values.size(); i++) {
            length_byte += values.get(i).length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.size(); i++) {
            byte[] b = values.get(i);
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }



}
