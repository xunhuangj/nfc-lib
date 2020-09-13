/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.wugj.nfc.nfc.mifare;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;


import com.wugj.nfc.R;
import com.wugj.nfc.base.BaseNfcApplication;
import com.wugj.nfc.nfc.util.UIRun;
import com.wugj.nfc.nfc.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Common functions and variables for all Activities.
 * @author Gerhard Klostermeier
 */
public class MCCommon {


    /**
     * Possible operations the on a MIFARE Classic Tag.
     */
    public enum Operations {
        Read, Write,Format ,Increment, DecTransRest, ReadKeyA, ReadKeyB, ReadAC,
        WriteKeyA, WriteKeyB, WriteAC
    }

    private static final String LOG_TAG = MCCommon.class.getSimpleName();



    /**
     * Get the shared preferences with application context for saving
     * and loading ("global") values.
     * @return The shared preferences object with application context.
     */
    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(BaseNfcApplication.getApplication());
    }

    /**
     * Enables the NFC foreground dispatch system for the given Activity.
     * @param targetActivity The Activity that is in foreground and wants to
     * have NFC Intents.
     * @see #disableNfcForegroundDispatch(Activity,NfcAdapter)
     */
    public static void enableNfcForegroundDispatch(Activity targetActivity,NfcAdapter mNfcAdapter) {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

            Intent intent = new Intent(targetActivity,
                    targetActivity.getClass()).addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    targetActivity, 0, intent, 0);
            mNfcAdapter.enableForegroundDispatch(
                    targetActivity, pendingIntent, null, new String[][] {
                            new String[] { NfcA.class.getName() } });
        }
    }

    /**
     * Disable the NFC foreground dispatch system for the given Activity.
     * @param targetActivity An Activity that is in foreground and has
     * NFC foreground dispatch system enabled.
     * @see #enableNfcForegroundDispatch(Activity,NfcAdapter)
     */
    public static void disableNfcForegroundDispatch(Activity targetActivity,NfcAdapter mNfcAdapter) {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disableForegroundDispatch(targetActivity);
        }
    }

    /**
     * For Activities which want to treat new Intents as Intents with a new
     * Tag attached. If the given Intent has a Tag extra, it will be patched
     * by {@link MCReader#patchTag(Tag)}
     *  A Toast message will be shown in the
     * Context of the calling Activity. This method will also check if the
     * device/tag supports MIFARE Classic (see return values and
     * {@link #checkMifareClassicSupport(Tag, Context)}).
     * @param intent The Intent which should be checked for a new Tag.
     * @param context The Context in which the Toast will be shown.
     * @return
     * <ul>
     * <li>0 - The device/tag supports MIFARE Classic</li>
     * <li>-1 - Device does not support MIFARE Classic.</li>
     * <li>-2 - Tag does not support MIFARE Classic.</li>
     * <li>-3 - Error (tag or context is null).</li>
     * <li>-4 - Wrong Intent (action is not "ACTION_TECH_DISCOVERED").</li>
     * </ul>
     * @see #checkMifareClassicSupport(Tag, Context)
     */
    public static int treatAsNewTag(Intent intent, Context context) {
        // Check if Intent has a NFC Tag.
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            tag = MCReader.patchTag(tag);
            //setTag(tag);

                // Show Toast message with UID.
                String id = context.getResources().getString(
                        R.string.info_new_tag_found) + " (UID: ";
                id += Utils.byte2HexString(tag.getId());
                id += ")";
                Toast.makeText(context, id, Toast.LENGTH_LONG).show();
            return checkMifareClassicSupport(tag, context);
        }
        return -4;
    }

    /**
     * Check if the device supports the MIFARE Classic technology.
     * In order to do so, there is a first check ensure the device actually has
     * a NFC hardware .
     * After this, this function will check if there are files
     * like "/dev/bcm2079x-i2c" or "/system/lib/libnfc-bcrm*". Files like
     * these are indicators for a NFC controller manufactured by Broadcom.
     * Broadcom chips don't support MIFARE Classic.
     * @return True if the device supports MIFARE Classic. False otherwise.
     */
    public static boolean hasMifareClassicSupport(int mHasMifareClassicSupport) {
        if (mHasMifareClassicSupport != 0) {
            return mHasMifareClassicSupport == 1;
        }

        // Check for the MifareClassic class.
        // It is most likely there on all NFC enabled phones.
        // Therefore this check is not needed.
        /*
        try {
            Class.forName("android.nfc.tech.MifareClassic");
        } catch( ClassNotFoundException e ) {
            // Class not found. Devices does not support MIFARE Classic.
            return false;
        }
        */

        // Check if ther is any NFC hardware at all.
        if (NfcAdapter.getDefaultAdapter(BaseNfcApplication.getApplication()) == null) {
            mHasMifareClassicSupport = -1;
            return false;
        }

        // Check if there is the NFC device "bcm2079x-i2c".
        // Chips by Broadcom don't support MIFARE Classic.
        // This could fail because on a lot of devices apps don't have
        // the sufficient permissions.
        // Another exception:
        // The Lenovo P2 has a device at "/dev/bcm2079x-i2c" but is still
        // able of reading/writing MIFARE Classic tags. I don't know why...
        boolean isLenovoP2 = Build.MANUFACTURER.equals("LENOVO")
                && Build.MODEL.equals("Lenovo P2a42");
        File device = new File("/dev/bcm2079x-i2c");
        if (!isLenovoP2 && device.exists()) {
            mHasMifareClassicSupport = -1;
            return false;
        }

        // Check if there is the NFC device "pn544".
        // The PN544 NFC chip is manufactured by NXP.
        // Chips by NXP support MIFARE Classic.
        device = new File("/dev/pn544");
        if (device.exists()) {
            mHasMifareClassicSupport = 1;
            return true;
        }

        // Check if there are NFC libs with "brcm" in their names.
        // "brcm" libs are for devices with Broadcom chips. Broadcom chips
        // don't support MIFARE Classic.
        File libsFolder = new File("/system/lib");
        File[] libs = libsFolder.listFiles();
        for (File lib : libs) {
            if (lib.isFile()
                    && lib.getName().startsWith("libnfc")
                    && lib.getName().contains("brcm")
                    // Add here other non NXP NFC libraries.
                    ) {
                mHasMifareClassicSupport = -1;
                return false;
            }
        }

        mHasMifareClassicSupport = 1;
        return true;
    }

    /**
     * Check if the tag and the device support the MIFARE Classic technology.
     * @param tag The tag to check.
     * @param context The context of the package manager.
     * @return
     * <ul>
     * <li>0 - Device and tag support MIFARE Classic.</li>
     * <li>-1 - Device does not support MIFARE Classic.</li>
     * <li>-2 - Tag does not support MIFARE Classic.</li>
     * <li>-3 - Error (tag or context is null).</li>
     * </ul>
     */
    public static int checkMifareClassicSupport(Tag tag, Context context) {
        if (tag == null || context == null) {
            // Error.
            return -3;
        }

        if (Arrays.asList(tag.getTechList()).contains(
                MifareClassic.class.getName())) {
            // Device and tag support MIFARE Classic.
            return 0;

        // This is no longer valid. There are some devices (e.g. LG's F60)
        // that have this system feature but no MIFARE Classic support.
        // (The F60 has a Broadcom NFC controller.)
        /*
        } else if (context.getPackageManager().hasSystemFeature(
                "com.nxp.mifare")){
            // Tag does not support MIFARE Classic.
            return -2;
        */

        } else {
            // Check if device does not support MIFARE Classic.
            // For doing so, check if the SAK of the tag indicate that
            // it's a MIFARE Classic tag.
            NfcA nfca = NfcA.get(tag);
            byte sak = (byte)nfca.getSak();
            if ((sak>>1 & 1) == 1) {
                // RFU.
                return -2;
            } else {
                if ((sak>>3 & 1) == 1) { // SAK bit 4 = 1?
                    if((sak>>4 & 1) == 1) { // SAK bit 5 = 1?
                        // MIFARE Classic 4k
                        // MIFARE SmartMX 4K
                        // MIFARE PlusS 4K SL1
                        // MIFARE PlusX 4K SL1
                        return -1;
                    } else {
                        if ((sak & 1) == 1) { // SAK bit 1 = 1?
                            // MIFARE Mini
                            return -1;
                        } else {
                            // MIFARE Classic 1k
                            // MIFARE SmartMX 1k
                            // MIFARE PlusS 2K SL1
                            // MIFARE PlusX 2K SL2
                            return -1;
                        }
                    }
                } else {
                    // Some MIFARE tag, but not Classic or Classic compatible.
                    return -2;
                }
            }

            // Old MIFARE Classic support check. No longer valid.
            // Check if the ATQA + SAK of the tag indicate that it's a MIFARE Classic tag.
            // See: http://www.nxp.com/documents/application_note/AN10833.pdf
            // (Table 5 and 6)
            // 0x28 is for some emulated tags.
            /*
            NfcA nfca = NfcA.get(tag);
            byte[] atqa = nfca.getAtqa();
            if (atqa[1] == 0 &&
                    (atqa[0] == 4 || atqa[0] == (byte)0x44 ||
                     atqa[0] == 2 || atqa[0] == (byte)0x42)) {
                // ATQA says it is most likely a MIFARE Classic tag.
                byte sak = (byte)nfca.getSak();
                if (sak == 8 || sak == 9 || sak == (byte)0x18 ||
                                            sak == (byte)0x88 ||
                                            sak == (byte)0x28) {
                    // SAK says it is most likely a MIFARE Classic tag.
                    // --> Device does not support MIFARE Classic.
                    return -1;
                }
            }
            // Nope, it's not the device (most likely).
            // The tag does not support MIFARE Classic.
            return -2;
            */
        }
    }


    /**
     * is support MifareClassic
     * @param tag
     * @param context
     * @return
     */
    public static boolean isSupportMifareClassic(Tag tag,Context context){
        if(checkMifareClassicSupport(tag,context) == 0) return true;

        return false;
    }



    /**
     * Create a connected {@link MCReader} if there is a present MIFARE Classic
     * tag. If there is no MIFARE Classic tag an error
     * message will be displayed to the user.
     * @return A connected {@link MCReader} or "null" if no tag was present.
     */
    public static MCReader checkForTagAndCreateReader(MCConfig mcConfig,Tag mTag) {
        MCReader reader;
        boolean tagLost = false;
        // Check for tag.
        if (mTag != null && (reader = MCReader.get(mTag,mcConfig)) != null) {
            try {
                reader.connect();
            } catch (Exception e) {
                tagLost = true;
            }
            if (!tagLost && !reader.isConnected()) {
                reader.close();
                tagLost = true;
            }
            if (!tagLost) {
                return reader;
            }
        }

        UIRun.toastLength(R.string.info_no_tag_found);
        return null;
    }

    /**
     * Depending on the provided Access Conditions this method will return
     * with which key you can achieve the operation ({@link Operations})
     * you asked for.<br />
     * This method contains the table from the NXP MIFARE Classic Datasheet.
     * @param c1 Access Condition byte "C!".
     * @param c2 Access Condition byte "C2".
     * @param c3 Access Condition byte "C3".
     * @param op The operation you want to do.
     * @param isSectorTrailer True if it is a Sector Trailer, False otherwise.
     * @param isKeyBReadable True if key B is readable, False otherwise.
     * @return The operation "op" is possible with:<br />
     * <ul>
     * <li>0 - Never.</li>
     * <li>1 - Key A.</li>
     * <li>2 - Key B.</li>
     * <li>3 - Key A or B.</li>
     * <li>-1 - Error.</li>
     * </ul>
     */
    public static int getOperationInfoForBlock(byte c1, byte c2, byte c3,
            Operations op, boolean isSectorTrailer, boolean isKeyBReadable) {
        // Is Sector Trailer?
        if (isSectorTrailer) {
            // Sector Trailer.
            if (op != Operations.ReadKeyA && op != Operations.ReadKeyB
                    && op != Operations.ReadAC
                    && op != Operations.WriteKeyA
                    && op != Operations.WriteKeyB
                    && op != Operations.WriteAC) {
                // Error. Sector Trailer but no Sector Trailer permissions.
                return 4;
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB
                        || op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return 1;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadKeyB
                        || op == Operations.ReadAC) {
                    return 1;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.WriteKeyA
                        || op == Operations.WriteKeyB) {
                    return 2;
                }
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadKeyA) {
                    return 0;
                }
                return 1;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                if (op == Operations.ReadKeyA
                        || op == Operations.ReadKeyB) {
                    return 0;
                }
                return 2;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                if (op == Operations.WriteAC) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                if (op == Operations.ReadAC) {
                    return 3;
                }
                return 0;
            } else {
                return -1;
            }
        } else {
            // Data Block.
            if (op != Operations.Read && op != Operations.Write
                    && op != Operations.Increment
                    && op != Operations.DecTransRest) {
                // Error. Data block but no data block permissions.
                return -1;
            }
            if          (c1 == 0 && c2 == 0 && c3 == 0) {
                return (isKeyBReadable) ? 1 : 3;
            } else if   (c1 == 0 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 0) {
                if (op == Operations.Read) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                if (op == Operations.Write) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 0) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 2;
            } else if   (c1 == 0 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read
                        || op == Operations.DecTransRest) {
                    return (isKeyBReadable) ? 1 : 3;
                }
                return 0;
            } else if   (c1 == 0 && c2 == 1 && c3 == 1) {
                if (op == Operations.Read || op == Operations.Write) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 0 && c3 == 1) {
                if (op == Operations.Read) {
                    return 2;
                }
                return 0;
            } else if   (c1 == 1 && c2 == 1 && c3 == 1) {
                return 0;
            } else {
                // Error.
                return -1;
            }
        }
    }

    /**
     * Check if key B is readable.
     * Key B is readable for the following configurations:
     * <ul>
     * <li>C1 = 0, C2 = 0, C3 = 0</li>
     * <li>C1 = 0, C2 = 0, C3 = 1</li>
     * <li>C1 = 0, C2 = 1, C3 = 0</li>
     * </ul>
     * @param c1 Access Condition byte "C1"
     * @param c2 Access Condition byte "C2"
     * @param c3 Access Condition byte "C3"
     * @return True if key B is readable. False otherwise.
     */
    public static boolean isKeyBReadable(byte c1, byte c2, byte c3) {
        return c1 == 0
                && ((c2 == 0 && c3 == 0)
                || (c2 == 1 && c3 == 0)
                || (c2 == 0 && c3 == 1));
    }

    /**
     * Convert the Access Condition bytes to a matrix containing the
     * resolved C1, C2 and C3 for each block.
     * @param acBytes The Access Condition bytes (3 byte).
     * @return Matrix of access conditions bits (C1-C3) where the first
     * dimension is the "C" parameter (C1-C3, Index 0-2) and the second
     * dimension is the block number (Index 0-3). If the ACs are incorrect
     * null will be returned.
     */
    public static byte[][] acBytesToACMatrix(byte acBytes[]) {
        // ACs correct?
        // C1 (Byte 7, 4-7) == ~C1 (Byte 6, 0-3) and
        // C2 (Byte 8, 0-3) == ~C2 (Byte 6, 4-7) and
        // C3 (Byte 8, 4-7) == ~C3 (Byte 7, 0-3)
        byte[][] acMatrix = new byte[3][4];
        if (acBytes.length > 2 &&
                (byte)((acBytes[1]>>>4)&0x0F)  ==
                        (byte)((acBytes[0]^0xFF)&0x0F) &&
                (byte)(acBytes[2]&0x0F) ==
                        (byte)(((acBytes[0]^0xFF)>>>4)&0x0F) &&
                (byte)((acBytes[2]>>>4)&0x0F)  ==
                        (byte)((acBytes[1]^0xFF)&0x0F)) {
            // C1, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[0][i] = (byte)((acBytes[1]>>>4+i)&0x01);
            }
            // C2, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[1][i] = (byte)((acBytes[2]>>>i)&0x01);
            }
            // C3, Block 0-3
            for (int i = 0; i < 4; i++) {
                acMatrix[2][i] = (byte)((acBytes[2]>>>4+i)&0x01);
            }
            return acMatrix;
        }
        return null;
    }

    /**
     * Convert a matrix with Access Conditions bits into normal 3
     * Access Condition bytes.
     * @param acMatrix Matrix of access conditions bits (C1-C3) where the first
     * dimension is the "C" parameter (C1-C3, Index 0-2) and the second
     * dimension is the block number (Index 0-3).
     * @return The Access Condition bytes (3 byte).
     */
    public static byte[] acMatrixToACBytes(byte acMatrix[][]) {
        if (acMatrix != null && acMatrix.length == 3) {
            for (int i = 0; i < 3; i++) {
                if (acMatrix[i].length != 4)
                    // Error.
                    return null;
            }
        } else {
            // Error.
            return null;
        }
        byte[] acBytes = new byte[3];
        // Byte 6, Bit 0-3.
        acBytes[0] = (byte)((acMatrix[0][0]^0xFF)&0x01);
        acBytes[0] |= (byte)(((acMatrix[0][1]^0xFF)<<1)&0x02);
        acBytes[0] |= (byte)(((acMatrix[0][2]^0xFF)<<2)&0x04);
        acBytes[0] |= (byte)(((acMatrix[0][3]^0xFF)<<3)&0x08);
        // Byte 6, Bit 4-7.
        acBytes[0] |= (byte)(((acMatrix[1][0]^0xFF)<<4)&0x10);
        acBytes[0] |= (byte)(((acMatrix[1][1]^0xFF)<<5)&0x20);
        acBytes[0] |= (byte)(((acMatrix[1][2]^0xFF)<<6)&0x40);
        acBytes[0] |= (byte)(((acMatrix[1][3]^0xFF)<<7)&0x80);
        // Byte 7, Bit 0-3.
        acBytes[1] = (byte)((acMatrix[2][0]^0xFF)&0x01);
        acBytes[1] |= (byte)(((acMatrix[2][1]^0xFF)<<1)&0x02);
        acBytes[1] |= (byte)(((acMatrix[2][2]^0xFF)<<2)&0x04);
        acBytes[1] |= (byte)(((acMatrix[2][3]^0xFF)<<3)&0x08);
        // Byte 7, Bit 4-7.
        acBytes[1] |= (byte)((acMatrix[0][0]<<4)&0x10);
        acBytes[1] |= (byte)((acMatrix[0][1]<<5)&0x20);
        acBytes[1] |= (byte)((acMatrix[0][2]<<6)&0x40);
        acBytes[1] |= (byte)((acMatrix[0][3]<<7)&0x80);
        // Byte 8, Bit 0-3.
        acBytes[2] = (byte)(acMatrix[1][0]&0x01);
        acBytes[2] |= (byte)((acMatrix[1][1]<<1)&0x02);
        acBytes[2] |= (byte)((acMatrix[1][2]<<2)&0x04);
        acBytes[2] |= (byte)((acMatrix[1][3]<<3)&0x08);
        // Byte 8, Bit 4-7.
        acBytes[2] |= (byte)((acMatrix[2][0]<<4)&0x10);
        acBytes[2] |= (byte)((acMatrix[2][1]<<5)&0x20);
        acBytes[2] |= (byte)((acMatrix[2][2]<<6)&0x40);
        acBytes[2] |= (byte)((acMatrix[2][3]<<7)&0x80);

        return acBytes;
    }



    /**
     * Check if the given block (hex string) is a value block.
     * NXP has PDFs describing what value blocks are. Google something
     * like "nxp MIFARE classic value block" if you want to have a
     * closer look.
     * @param hexString Block data as hex string.
     * @return True if it is a value block. False otherwise.
     */
    public static boolean isValueBlock(String hexString) {
        byte[] b = Utils.hexStringToByteArray(hexString);
        if (b.length == 16) {
            // Google some NXP info PDFs about MIFARE Classic to see how
            // Value Blocks are formatted.
            // For better reading (~ = invert operator):
            // if (b0=b8 and b0=~b4) and (b1=b9 and b9=~b5) ...
            // ... and (b12=b14 and b13=b15 and b12=~b13) then
            if (    (b[0] == b[8] && (byte)(b[0]^0xFF) == b[4]) &&
                    (b[1] == b[9] && (byte)(b[1]^0xFF) == b[5]) &&
                    (b[2] == b[10] && (byte)(b[2]^0xFF) == b[6]) &&
                    (b[3] == b[11] && (byte)(b[3]^0xFF) == b[7]) &&
                    (b[12] == b[14] && b[13] == b[15] &&
                    (byte)(b[12]^0xFF) == b[13])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all blocks (lines) contain valid data.
     * @param lines Blocks (incl. their sector header, e.g. "+Sector: 1").
     * @param ignoreAsterisk Ignore lines starting with "*" and move on
     * to the next sector (header).
     * @return <ul>
     * <li>0 - Everything is (most likely) O.K.</li>
     * <li>1 - Found a sector that has not 4 or 16 blocks.</li>
     * <li>2 - Found a block that has invalid characters (not hex or "-" as
     * marker for no key/no data).</li>
     * <li>3 - Found a block that has not 16 bytes (32 chars).</li>
     * <li>4 - A sector index is out of range.</li>
     * <li>5 - Found two times the same sector number (index).
     * Maybe this is a file containing multiple dumps
     * (the dump editor->save->append function was used)</li>
     * <li>6 - There are no lines (lines == null or len(lines) == 0).</li>
     * </ul>
     */
    public static int isValidDump(String[] lines, boolean ignoreAsterisk) {
        ArrayList<Integer> knownSectors = new ArrayList<>();
        int blocksSinceLastSectorHeader = 4;
        boolean is16BlockSector = false;
        if (lines == null || lines.length == 0) {
            // There are no lines.
            return 6;
        }
        for(String line : lines) {
            if ((!is16BlockSector && blocksSinceLastSectorHeader == 4)
                    || (is16BlockSector && blocksSinceLastSectorHeader == 16)) {
                // A sector header is expected.
                if (!line.matches("^\\+Sector: [0-9]{1,2}$")) {
                    // Not a valid sector length or not a valid sector header.
                    return 1;
                }
                int sector;
                try {
                    sector = Integer.parseInt(line.split(": ")[1]);
                } catch (Exception ex) {
                    // Not a valid sector header.
                    // Should not occur due to the previous check (regex).
                    return 1;
                }
                if (sector < 0 || sector > 39) {
                    // Sector out of range.
                    return 4;
                }
                if (knownSectors.contains(sector)) {
                    // Two times the same sector number (index).
                    // Maybe this is a file containing multiple dumps
                    // (the dump editor->save->append function was used).
                    return 5;
                }
                knownSectors.add(sector);
                is16BlockSector = (sector >= 32);
                blocksSinceLastSectorHeader = 0;
                continue;
            }
            if (line.startsWith("*") && ignoreAsterisk) {
                // Ignore line and move to the next sector.
                // (The line was a "No keys found or dead sector" message.)
                is16BlockSector = false;
                blocksSinceLastSectorHeader = 4;
                continue;
            }
            if (!line.matches("[0-9A-Fa-f-]+")) {
                // Not pure hex (or NO_DATA).
                return 2;
            }
            if (line.length() != 32) {
                // Not 32 chars per line.
                return 3;
            }
            blocksSinceLastSectorHeader++;
        }
        return 0;
    }

    /**
     * Show a Toast message with error information according to
     * {@link #isValidDump(String[], boolean)}.
     * @see #isValidDump(String[], boolean)
     */
    public static void isValidDumpErrorToast(int errorCode,
            Context context) {
        switch (errorCode) {
        case 1:
            Toast.makeText(context, R.string.info_valid_dump_not_4_or_16_lines,
                    Toast.LENGTH_LONG).show();
            break;
        case 2:
            Toast.makeText(context, R.string.info_valid_dump_not_hex,
                    Toast.LENGTH_LONG).show();
            break;
        case 3:
            Toast.makeText(context, R.string.info_valid_dump_not_16_bytes,
                    Toast.LENGTH_LONG).show();
            break;
        case 4:
            Toast.makeText(context, R.string.info_valid_dump_sector_range,
                    Toast.LENGTH_LONG).show();
            break;
        case 5:
            Toast.makeText(context, R.string.info_valid_dump_double_sector,
                    Toast.LENGTH_LONG).show();
            break;
        case 6:
            Toast.makeText(context, R.string.info_valid_dump_empty_dump,
                    Toast.LENGTH_LONG).show();
            break;
        }
    }





}
