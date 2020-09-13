package com.wugj.nfc.nfc.mifare;

import android.nfc.tech.MifareClassic;

import com.wugj.nfc.R;
import com.wugj.nfc.nfc.util.FileUtils;
import com.wugj.nfc.nfc.util.UIRun;
import com.wugj.nfc.nfc.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public interface MCKeysFactory {




     ArrayList<byte[]> getKeys(String... keys);


     ArrayList<byte[]> getKeys(File... files);



     MCKeysFactory DefaultMCKeysFactory = new MCKeysFactory(){

         @Override
         public ArrayList<byte[]> getKeys(String... keys) {

             if(keys == null || keys.length == 0) return new ArrayList<byte[]>(){
                 {
                     add(MifareClassic.KEY_DEFAULT);
                 }
             };

             ArrayList<byte[]> bytes = new ArrayList<>();
             for(String key : keys){

                if(isHexKey(key)){
                    bytes.add(Utils.hexStringToByteArray(key));
                    continue;
                }

                if(is6ByteKey(key)) {
                    bytes.add(key.getBytes());
                    continue;
                }
             }

             if(bytes.isEmpty())
                 throw new IllegalStateException("MCKeysFactory keys is Illegal");

             return bytes;
         }


         @Override
         public ArrayList<byte[]> getKeys(File... files) {

             boolean hasAllZeroKey = false;

             HashSet<byte[]> keys = new HashSet<>();
             for (File file : files) {
                 String[] lines = FileUtils.readFileLineByLine(file, false);
                 if (lines != null) {
                     for (String line : lines) {
                         if (!line.equals("") && line.length() == 12
                                 && line.matches("[0-9A-Fa-f]+")) {
                             if (line.equals("000000000000")) {
                                 hasAllZeroKey = true;
                             }
                             try {
                                 keys.add(Utils.hexStringToByteArray(line));
                             } catch (OutOfMemoryError e) {
                                 // Error. Too many keys (out of memory).
                                 UIRun.toastLength(R.string.info_to_many_keys);
                             }
                         }
                     }
                 }
             }

             ArrayList<byte[]>  mKeysWithOrder =new ArrayList<>(keys);

             if (keys.size() > 0) {
                 mKeysWithOrder = new ArrayList<>(keys);
                 byte[] zeroKey = Utils.hexStringToByteArray("000000000000");
                 if (hasAllZeroKey) {
                     // NOTE: The all-F key has to be tested always first if there
                     // is a all-0 key in the key file, because of a bug in
                     // some tags and/or devices.
                     // https://github.com/ikarus23/MifareClassicTool/iss000000000000ues/66
                     byte[] fKey = Utils.hexStringToByteArray("FFFFFFFFFFFF");
                     mKeysWithOrder.remove(fKey);
                     mKeysWithOrder.add(0, fKey);
                 }
             }


             return mKeysWithOrder;
         }


         /**
          * Check if a (key) string is pure hex (0-9, A-Z, a-z) and 16 byte
          * @param key The string to check.
          * @return True if key is six bytes.
          */
         private  boolean is6ByteKey(String key) {

             if (key.matches("[0-9A-Za-z]{6}")) {
                 // Error, not hex.

                 return true;
             }

//             UIRun.toastLength( R.string.info_not_6_byte_data);

             return false;
         }

         /**
          * Check if a (key) string is pure hex (0-9, A-Z, a-z) and 16 byte
          * @param key The string to check.
          * @return True if key is hexString.
          */
         private  boolean isHexKey(String key) {

             if (key.matches("[0-9A-F]{12}")) {

                 return true;
             }

             // Error, not hex.
//             UIRun.toastLength( R.string.info_not_hex_data);

             return false;
         }

     };


}
