package com.wugj.nfc.nfc.mifare;

import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.text.TextUtils;
import android.util.SparseArray;

import com.wugj.nfc.R;
import com.wugj.nfc.nfc.INfc;
import com.wugj.nfc.nfc.NfcCall;
import com.wugj.nfc.nfc.NfcClient;
import com.wugj.nfc.nfc.NfcResponse;
import com.wugj.nfc.nfc.util.MLog;
import com.wugj.nfc.nfc.util.UIRun;
import com.wugj.nfc.nfc.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MifareClssicImp implements INfc<MCResponse> {

    private String TAG = MifareClssicImp.class.getSimpleName();

    private MCRequest mcRequest;
    private NfcClient mcClient;

    private MCResponse mcResponse;

    private int mProgressStatus = 0;
    private MCReader reader;
    private NfcCall.Callback<MCResponse> callback;

    public MifareClssicImp(MCRequest mcRequest, NfcClient mcClient) {
        this.mcClient = mcClient;
        this.mcRequest = mcRequest;
        mcResponse = new MCResponse();
    }

    public static MifareClssicImp newRealCall(MCRequest mcRequest, NfcClient mcClient) {

        MifareClssicImp mifareClssicImp = new MifareClssicImp(mcRequest, mcClient);

        return mifareClssicImp;

    }


    @Override
    public void read(NfcCall.Callback<MCResponse> readCallback) {
        callback = readCallback;
        createKeyMap();

        readData(readCallback);
    }


    private int getSectorSize(){

        try {
            reader.setmKeysWithOrder(mcRequest.getKeyFactory().getKeys(mcRequest.getKeys()));
           String [] hexArray =  reader.readSector(0,1,1,reader.createAuthKeys(0));
           if(hexArray != null && hexArray.length == 1 && hexArray[0] != null){
                if(hexArray[0].equals("00000000000000000000000000000000")){
                    return -1;
                }

               try {
                   MLog.i(TAG,"hexArray = " + hexArray[0]);
                   byte[] bytes = Utils.hexStringToByteArray(hexArray[0]);
                   String a = new String(bytes,"UTF-8").trim();
                   return Integer.parseInt(a);
               } catch (UnsupportedEncodingException e) {
                   e.printStackTrace();

                   throw new IllegalStateException("UnsupportedEncodingException : " + e.getMessage());
               }
           }else
           throw new IllegalArgumentException("sectorIndex == 0 , block = 1 hexArray = null");

        } catch (TagLostException e) {
            e.printStackTrace();
            throw new IllegalStateException("create reader failed maybe tag lost");
        }

    }




    /**
     * method starts a worker thread that first creates a key map and then
     */
    private void createKeyMap() {
        try {
            // Create reader.
            reader = MCCommon.checkForTagAndCreateReader(mcRequest.getMcConfig(), mcRequest.getTag());
            if (reader == null) {
                MLog.w(TAG, "reader == null");
                throw new IllegalStateException("create reader failed maybe tag lost");
            }

            if (mcRequest.getOperation() == MCCommon.Operations.Format) {
                MCRequest.Builder builder = null;
                if (mcRequest.getFrom() < 0) {
                    builder = mcRequest.newBuilder().setFrom(0);
                }
                if (mcRequest.getTo() + 1 >= reader.getSectorCount() || mcRequest.getTo() < 0) {
                    builder = builder == null ?
                            mcRequest.newBuilder().setTo(reader.getSectorCount() - 1) : builder.setTo(reader.getSectorCount() - 1);
                };
                mcRequest = builder.build();
            }else if(mcRequest.getOperation() == MCCommon.Operations.Read){
                int to = getSectorSize();
                if(to >= 0){
                    mcRequest = mcRequest.newBuilder().setTo(to).build();
                }
               else
                    throw new IllegalArgumentException("-1 sector 0 no data");
            }
//            else if(mcRequest.getOperation() == Common.Operations.)

            mProgressStatus = mcRequest.getFrom();
            if(reader.getmKeysWithOrder() == null)
                reader.setmKeysWithOrder(mcRequest.getKeyFactory().getKeys(mcRequest.getKeys()));
            if (!reader.setMappingRange(mcRequest.getFrom(), mcRequest.getTo())) {
                //UIRun.toastLength(R.string.info_mapping_sector_out_of_range);
                throw new IndexOutOfBoundsException("createKeyMap:" + UIRun.getString(R.string.info_mapping_sector_out_of_range));
            }

            // Build key map parts and update the progress bar.
            while (true) {
                mProgressStatus = reader.buildNextKeyMapPart(); //mProgeressStatus get cur section.
                MLog.i(TAG, "mProgressStatus = " + mProgressStatus);
                if (mProgressStatus == -1 || mProgressStatus >= reader.getmLastSector()) {
                    // Error while building next key map part.
                    break;
                }
            }

            if(reader.getKeyMap() == null || reader.getKeyMap().size() == 0){
                throw new NullPointerException("createKeyMap: keymap is empty,maybe all keys are wrong");
            }


        } catch (Exception e) {
            MLog.e(TAG, e.getMessage());
            throw e;
        }

    }


    private void readData(final NfcCall.Callback readCallback) {
        MLog.i(TAG, "stop while and mProgressStatus = " + mProgressStatus);
        UIRun.runOnUI(new Runnable() {
            @Override
            public void run() {
                if (mProgressStatus != -1) {
                    SparseArray<String[]> datas = reader.readAsMuchAsPossible(false);
                    MLog.i(TAG, "sparse array datas = " + datas);
                    if (readCallback != null) {
                        readCallback.successCall(mcRequest, new NfcResponse<>(new MCResponse(datas)));
                    }
                } else {
                    readCallback.failedCall(mcRequest,
                            new IllegalStateException(UIRun.getString(R.string.info_key_map_error) + ":error = " + mProgressStatus));
                }
                close();
            }
        });
    }




    @Override
    public void write(NfcCall.Callback<MCResponse> writeCallback) {


        if (TextUtils.isEmpty(mcRequest.getWriteText())) {
            throw new NullPointerException("write text exception");
        }

        try {
            byte[] textBytes = mcRequest.getWriteText().getBytes("UTF-8");

            int outByteSize = textBytes.length & 0xf;

            if(textBytes.length > (1024 >> 1 )){
                throw new IllegalStateException("you can not write more than 512 bytes");
            }

            // blocks num.
            int index = outByteSize == 0 ? textBytes.length >> 4 : (textBytes.length >> 4) + 1;

            //every section we can write 3 * 16 bytes, for 1KB M1.
            int to = index % 3 == 0 ? index / 3 :index /3 + 1;
            mcRequest = mcRequest.newBuilder().setTo(to).build();

            createKeyMap();

            SparseArray<byte[]> bytes = new SparseArray<>(index);

            for (int i = 0; i < index; i++) {
                byte[] emptyBytes = new byte[16];

                int copyLength = 16;

                //last index
                if (index - 1 == i && outByteSize != 0) {
                    for (int j = outByteSize; j < 16; j++) {
                        emptyBytes[j] = (byte) 0x00;
                    }
                    copyLength = outByteSize;
                }

                System.arraycopy(textBytes, i * 16, emptyBytes, 0, copyLength);
                bytes.put(i, emptyBytes);
            }

            byte[] emptyBlock = new byte[]
                    {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
            int writeIndex = 0;
            int lastSector = 0;
            breakWrite:
            for (int i = mcRequest.getFrom(); i <= mcRequest.getTo(); i++) {
                int blockCount = reader.getBlockCountInSector(i) -1; //sub tail
                for (int j = i == 0 ? 1 : 0; j < blockCount; ++j) {

                    if (writeIndex < index) {
                        lastSector = i;
                        writeBlock(i,reader.getKeyMap().get(i), j, bytes.get(bytes.keyAt(writeIndex)));
                    }else{
                        if(j <= blockCount - 1){
                            writeBlock(i,reader.getKeyMap().get(i),j,emptyBlock);
                            if(j == blockCount - 1)
                                break breakWrite;
                        }
                    }
                    writeIndex++;
                }
            }

            byte [] nums = String.valueOf(lastSector).getBytes("UTF-8");
            int i = 0;
            while (i < nums.length && i < emptyBlock.length){
                emptyBlock[i] = nums[i];
                i++;
            }
            writeBlock(0,reader.createAuthKeys(0), 1,emptyBlock);
            close();
            mcResponse.setContent("success");
            writeCallback.successCall(mcRequest, new NfcResponse<>(mcResponse));


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalStateException("write dump UnsupportedEncodingException");
        }


    }


    /**
     * after a key map was created, this method tries to write the given
     * data to the tag. Possible errors are displayed to the user via Toast.
     *
     * @see MCReader#writeBlock(int, int, byte[], byte[], boolean)
     */
    private void writeBlock(int sector,byte[][] keys, int block, byte[] writeTextBytes) {

        //int key save index.  reader.getKeyMap().keyAt(sector) render.getKeyMap().
        int result = -1;

        if (keys[1] != null) {
            result = reader.writeBlock(sector, block,
                    writeTextBytes,
                    keys[1], true);
        }
        // Error while writing? Maybe tag has default factory settings ->
        // try to write with key a (if there is one).
        if (result == -1 && keys[0] != null) {
            result = reader.writeBlock(sector, block,
                    writeTextBytes,
                    keys[0], false);
        }
        // Error handling.
        switch (result) {
            case 2:
                throw new IllegalStateException(UIRun.getString(R.string.info_block_not_in_sector));
            case -1:
                throw new IllegalStateException(UIRun.getString(R.string.info_error_writing_block));

        }

        MLog.i(TAG, "section = " + sector + ",block = " + block
                + UIRun.getString(R.string.info_write_successful));

    }



    @Override
    public void format(NfcCall.Callback<MCResponse> formatCallback) {
        this.callback = formatCallback;
        createKeyMap();
        createFactoryFormattedDump();
    }


    private HashMap<Integer, HashMap<Integer, byte[]>> mDumpWithPos;

    /**
     * Create an factory formatted, empty dump with a size matching
     * the current tag size and then call {@link #checkTag()}.
     * Factory (default) MIFARE Classic Access Conditions are: 0xFF0780XX
     * XX = General purpose byte (GPB): Most of the time 0x69. At the end of
     * an Tag XX = 0xBC.
     *
     * @see #checkTag()
     */
    private void createFactoryFormattedDump() {
        // This function is directly called after a key map was created.
        // So Common.getTag() will return den current present tag
        // (and its size/sector count).
        mDumpWithPos = new HashMap<>();
        int sectors = MifareClassic.get(mcRequest.getTag()).getSectorCount();
        byte[] emptyBlock = new byte[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};


        byte[] normalSectorTrailer = new byte[]{-1, -1, -1, -1, -1, -1,
                -1, 7, -128, 105, -1, -1, -1, -1, -1, -1};

        byte[] lastSectorTrailer = new byte[]{-1, -1, -1, -1, -1, -1,
                -1, 7, -128, -68, -1, -1, -1, -1, -1, -1};


        if (mcRequest.getSaveKeys() != null && mcRequest.getSaveKeys().length != 0) {
            List<byte[]> keyBytes = mcRequest.getKeyFactory().getKeys(mcRequest.getSaveKeys());

            int size = keyBytes.size();
            for (int i = 0; i < size; i++) {
                byte[] key = keyBytes.get(i);
                if (key.length != 6)
                    throw new IllegalArgumentException("new key is not six byte");
                if (i == 0)
                    for (int j = 0; j < 6; j++) {
                        normalSectorTrailer[j] = key[j];
                        lastSectorTrailer[j] = key[j];
                    }
                if (i == 1 || size == 1)
                    for (int j = 0; j < 6; j++) {
                        normalSectorTrailer[j + 10] = key[j];
                        lastSectorTrailer[j + 10] = key[j];
                    }
            }
        }

        // Empty 4 block sector.
        HashMap<Integer, byte[]> empty4BlockSector =
                new HashMap<>(4);
        for (int i = 0; i < 3; i++) {
            empty4BlockSector.put(i, emptyBlock);
        }
        empty4BlockSector.put(3, normalSectorTrailer);
        // Empty 16 block sector.
        HashMap<Integer, byte[]> empty16BlockSector =
                new HashMap<>(16);
        for (int i = 0; i < 15; i++) {
            empty16BlockSector.put(i, emptyBlock);
        }
        empty16BlockSector.put(15, normalSectorTrailer);
        // Last sector.
        HashMap<Integer, byte[]> lastSector;

        // Sector 0.
        HashMap<Integer, byte[]> firstSector =
                new HashMap<>(4);
        firstSector.put(1, emptyBlock);
        firstSector.put(2, emptyBlock);
        firstSector.put(3, normalSectorTrailer);
        mDumpWithPos.put(0, firstSector);
        // Sector 1 - (max.) 31.
        for (int i = 1; i < sectors && i < 32; i++) {
            mDumpWithPos.put(i, empty4BlockSector);
        }
        // Sector 32 - 39.
        if (sectors == 40) {
            // Add the large sectors (containing 16 blocks)
            // of a MIFARE Classic 4k tag.
            for (int i = 32; i < sectors && i < 39; i++) {
                mDumpWithPos.put(i, empty16BlockSector);
            }
            // In the last sector the Sector Trailer is different.
            lastSector = new HashMap<>(empty16BlockSector);
            lastSector.put(15, lastSectorTrailer);
        } else {
            // In the last sector the Sector Trailer is different.
            lastSector = new HashMap<>(empty4BlockSector);
            lastSector.put(3, lastSectorTrailer);
        }
        mDumpWithPos.put(sectors - 1, lastSector);
        checkTag();
    }


    /**
     * Check if the tag is suitable for the dump ({@link #mDumpWithPos}).
     * This is done in three steps. The first check determines if the dump
     * fits on the tag (size check). The second check determines if the keys for
     * relevant sectors are known (key check). At last this method will check
     * whether the keys with write privileges are known and if some blocks
     * are read-only (write check).<br />
     * If some of these checks "fail", the user will get a report dialog
     * with the two options to cancel the whole write process or to
     * write as much as possible(call {@link #writeDump(HashMap,
     * SparseArray)}).
     *
     * @see MCReader#isWritableOnPositions(HashMap, SparseArray)
     * @see #writeDump(HashMap, SparseArray)
     */
    private void checkTag() {

        // Check if tag is correct size for dump.
        if (reader.getSectorCount() - 1 < Collections.max(
                mDumpWithPos.keySet())) {
            // Error. Tag too small for dump.
            UIRun.toastLength(R.string.info_tag_too_small);
            close();
            throw new IllegalStateException("tag sections size is smaller than normal");
        }

        // Check if tag is writable on needed blocks.
        // Reformat for reader.isWritableOnPosition(...).
        final SparseArray<byte[][]> keyMap = reader.getKeyMap();
        //save block counts each section.
        HashMap<Integer, int[]> dataPos =
                new HashMap<>(mDumpWithPos.size());
        for (int sector : mDumpWithPos.keySet()) {
            int i = 0;
            int[] blocks = new int[mDumpWithPos.get(sector).size()];
            for (int block : mDumpWithPos.get(sector).keySet()) {
                blocks[i++] = block;
            }
            dataPos.put(sector, blocks);
        }
        HashMap<Integer, HashMap<Integer, Integer>> writeOnPos =
                reader.isWritableOnPositions(dataPos, keyMap);

        if (writeOnPos == null || writeOnPos.isEmpty()) {
            // Error while checking for keys with write privileges.

//            UIRun.toastLength(R.string.info_check_ac_error);
            throw new IllegalAccessError("access control exception");
        }

        // Skip dialog:
        // Build a dialog showing all sectors and blocks containing data
        // that can not be overwritten with the reason why they are not
        // writable. The user can chose to skip all these blocks/sectors
        // or to cancel the whole write procedure.
        List<HashMap<String, String>> list = new
                ArrayList<>();
        final HashMap<Integer, HashMap<Integer, Integer>> writeOnPosSafe =
                new HashMap<>(
                        mDumpWithPos.size());
        // Keys that are missing completely (mDumpWithPos vs. keyMap).
        HashSet<Integer> sectors = new HashSet<>();
        for (int sector : mDumpWithPos.keySet()) {
            if (keyMap.indexOfKey(sector) < 0) {
                // Problem. Keys for sector not found.
                addToList(list, UIRun.getString(R.string.text_sector) + ": " + sector,
                        UIRun.getString(R.string.text_keys_not_known));
            } else {
                sectors.add(sector);
            }
        }
        // Keys with write privileges that are missing or some
        // blocks (block-parts) are read-only (writeOnPos vs. keyMap).
        for (int sector : sectors) {
            if (writeOnPos.get(sector) == null) {
                // Error. Sector is dead (IO Error) or ACs are invalid.
                addToList(list, UIRun.getString(R.string.text_sector) + ": " + sector,
                        UIRun.getString(R.string.text_invalid_ac_or_sector_dead));
                continue;
            }
            byte[][] keys = keyMap.get(sector);
            Set<Integer> blocks = mDumpWithPos.get(sector).keySet();
            for (int block : blocks) {
                boolean isSafeForWriting = true;
                if (sector == 0 && block == 0) {
                    // Block 0 is read-only. This is normal.
                    // Do not add an entry to the dialog and skip the
                    // "write info" check (except for some
                    // special (non-original) MIFARE tags).
                    continue;
                }
                String position = "区" + ": "
                        + sector + ", " + "块"
                        + ": " + block;
                int writeInfo = writeOnPos.get(sector).get(block);
                switch (writeInfo) {
                    case 0:
                        // Problem. Block is read-only.
                        addToList(list, position, UIRun.getString(
                                R.string.text_block_read_only));
                        isSafeForWriting = false;
                        break;
                    case 1:
                        if (keys[0] == null) {
                            // Problem. Key with write privileges (A) not known.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_write_key_a_not_known));
                            isSafeForWriting = false;
                        }
                        break;
                    case 2:
                        if (keys[1] == null) {
                            // Problem. Key with write privileges (B) not known.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_write_key_b_not_known));
                            isSafeForWriting = false;
                        }
                        break;
                    case 3:
                        // No Problem. Both keys have write privileges.
                        // Set to key A or B depending on which one is available.
                        writeInfo = (keys[0] != null) ? 1 : 2;
                        break;
                    case 4:
                        if (keys[0] == null) {
                            // Problem. Key with write privileges (A) not known.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_write_key_a_not_known));
                            isSafeForWriting = false;
                        } else {
                            // Problem. ACs are read-only.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_ac_read_only));
                        }
                        break;
                    case 5:
                        if (keys[1] == null) {
                            // Problem. Key with write privileges (B) not known.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_write_key_b_not_known));
                            isSafeForWriting = false;
                        } else {
                            // Problem. ACs are read-only.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_ac_read_only));
                        }
                        break;
                    case 6:
                        if (keys[1] == null) {
                            // Problem. Key with write privileges (B) not known.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_write_key_b_not_known));
                            isSafeForWriting = false;
                        } else {
                            // Problem. Keys are read-only.
                            addToList(list, position, UIRun.getString(
                                    R.string.text_keys_read_only));
                        }
                        break;
                    case -1:
                        // Error. Some strange error occurred. Maybe due to some
                        // corrupted ACs...
                        addToList(list, position, UIRun.getString(
                                R.string.text_strange_error));
                        isSafeForWriting = false;
                }
                // Add if safe for writing.
                if (isSafeForWriting) {
                    if (writeOnPosSafe.get(sector) == null) {
                        // Create sector.
                        HashMap<Integer, Integer> blockInfo =
                                new HashMap<>();
                        blockInfo.put(block, writeInfo);
                        writeOnPosSafe.put(sector, blockInfo);
                    } else {
                        // Add to sector.
                        writeOnPosSafe.get(sector).put(block, writeInfo);
                    }
                }
            }
        }

        mcResponse.setErrorMap(list);
        // Write.
        writeDump(writeOnPosSafe, keyMap);
    }

    /**
     * A helper function for {@link #checkTag()} adding an item to
     * the list of all blocks with write issues.
     * This list will be displayed to the user in a dialog before writing.
     *
     * @param list     The list in which to add the key-value-pair.
     * @param position The key (position) for the list item
     *                 (e.g. "Sector 2, Block 3").
     * @param reason   The value (reason) for the list item
     *                 (e.g. "Block is read-only").
     */
    private void addToList(List<HashMap<String, String>> list,
                           String position, String reason) {
        HashMap<String, String> item = new HashMap<>();
        item.put("position", position);
        item.put("reason", reason);
        list.add(item);
    }


    /**
     * This method is triggered by {@link #checkTag()} and writes a dump
     * to a tag.
     *
     * @param writeOnPos A map within a map (all with type = Integer).
     *                   The key of the outer map is the sector number and the value is another
     *                   map with key = block number and value = write information. The write
     *                   information must be filtered (by {@link #checkTag()}) return values
     *                   of {@link MCReader#isWritableOnPositions(HashMap, SparseArray)}.<br />
     *                   Attention: This method does not any checking. The position and write
     *                   information must be checked by {@link #checkTag()}.
     * @param keyMap     A key map generated by {@link MCReader}.
     */
    private void writeDump(
            final HashMap<Integer, HashMap<Integer, Integer>> writeOnPos,
            final SparseArray<byte[][]> keyMap) {
        // Check for write data.
        if (writeOnPos.size() == 0) {
            // Nothing to write. Exit.
//            UIRun.toastLength(R.string.info_nothing_to_write);
            throw new IllegalStateException(UIRun.getString(R.string.info_nothing_to_write));
        }
        // Write dump to tag.
        for (int sector : writeOnPos.keySet()) {
            byte[][] keys = keyMap.get(sector);
            for (int block : writeOnPos.get(sector).keySet()) {
                // Select key with write privileges.
                byte writeKey[] = null;
                boolean useAsKeyB = true;
                int wi = writeOnPos.get(sector).get(block);
                if (wi == 1 || wi == 4) {
                    writeKey = keys[0]; // Write with key A.
                    useAsKeyB = false;
                } else if (wi == 2 || wi == 5 || wi == 6) {
                    writeKey = keys[1]; // Write with key B.
                }

                // Write block.
                int result = reader.writeBlock(sector, block,
                        mDumpWithPos.get(sector).get(block),
                        writeKey, useAsKeyB);

                if (result != 0) {
                    close();
                    // Error. Some error while writing.
                    throw new IllegalStateException(UIRun.getString(R.string.info_write_error));
//                    UIRun.toastLength(R.string.info_write_error);
                }
            }
        }
        // Finished writing.
        close();
        MCResponse mcResponse = new MCResponse();
        mcResponse.setContent(UIRun.getString(R.string.tag_format_successful));
        callback.successCall(mcRequest,new NfcResponse<MCResponse>(mcResponse));
    }

    @Override
    public void close() {
        reader.close();

    }
}
