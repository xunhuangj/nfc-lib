package com.blacklake.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcB;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.wugj.nfc.base.BaseNfcActivity;

import java.io.IOException;
import java.nio.charset.Charset;

public class MifareUltraligthActivity extends BaseNfcActivity {
    private TextView mNfcText;

    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_read_isodep);
        mNfcText = (TextView) findViewById(R.id.tv_nfctext);
        checkBox = findViewById(R.id.cb_checkbox);

    }


    NfcB nfcbTag;

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //得到nfc标签.
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        //得到nfc标签内支持的数据格式,如ndef,MifareUltralight
        String[] techList = tag.getTechList();

        //判断是否支持MifareUltralight数据格式
        boolean haveMifareUltralight = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareUltralight") >= 0) {
                haveMifareUltralight = true;
                break;
            }
        }
        if (!haveMifareUltralight) {//标签不支持MifareUltralight数据格式
            Toast.makeText(this, "本标签不支持MifareUltralight数据格式", Toast.LENGTH_LONG).show();
            return;
        }
        if (checkBox.isChecked()) {
            //3,写数据
            writeTag(tag);
        } else {
            //4,读数据
            String data = readTag(tag);
            if (data != null) {
                mNfcText.setText(data);
//                Toast.makeText(this, data, Toast.LENGTH_LONG).show();
            }
        }
    }





    /*
 87      * 向nfc标签写入数据
 88      * 将NFC标签的存储区域分为16个页，每一个页可以存储4个字节，一个可存储64个字节（512位）。
 89      * 页码从0开始（0至15）。前4页（0至3）存储了NFC标签相关的信息（如NFC标签的序列号、控制位等）。
 90      * 从第5页开始存储实际的数据（4至15页）。
 91      */
      public void writeTag(Tag tag) {
                //向nfc标签写数据第1步,从标签中得到MifareUltralight
                 MifareUltralight ultralight = MifareUltralight.get(tag);
                 try {
                         //向nfc标签写数据第2步, connect
                         ultralight.connect();

                         /*
100              * 向nfc标签写数据第3步, 正式写数据.前4页（0至3）存储了NFC标签相关的信息
101              *
102              * 注意 Charset.forName("GB2312")),
103              * 不用utf-8因为一个汉字有可能用3个字节编码汉字,那么2个汉字有可能是6个字节.
104              * 而GB2312始终用2个字节.而每页最多4个字节,
105              */
                         ultralight.writePage(4, "中国".getBytes(Charset.forName("GB2312")));//第4页,页从0开始.
                         ultralight.writePage(5, "美国".getBytes(Charset.forName("GB2312")));
                         ultralight.writePage(6, "英国".getBytes(Charset.forName("GB2312")));
                         ultralight.writePage(7, "德国".getBytes(Charset.forName("GB2312")));

                         Toast.makeText(this, "成功写入MifareUltralight格式数据",Toast.LENGTH_LONG).show();
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         try {
                                 ultralight.close();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                     }
             }
            /*
            * 读取MifareUltralight格式数据
            */
             public String readTag(Tag tag) {
                 //读数据 第1步,从nfc标签中得到MifareUltralight
                 MifareUltralight ultralight = MifareUltralight.get(tag);

                 try {
                         //读数据 第2步,接连
                         ultralight.connect();
                         //读数据 第3步,从ultralight数据中的下标为4的位开始读数据.
                         byte[] data = ultralight.readPages(4);
                         //读数据 第4步,把读出的数据存到一个string中.注意语言编码
                         return new String(data, Charset.forName("GB2312"));
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         try {
                                 ultralight.close();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                     }
                 return null;
             }






    class CommandAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            byte[] search = new byte[]{0x05, 0x00, 0x00};
            search = new byte[]{0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x60,
                    0x02};
            search = new byte[]{0x1D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
                    0x01, 0x08};
            byte[] result = new byte[]{};
            StringBuffer sb = new StringBuffer();
            try {
                byte[] cmd = new byte[]{0x05, 0x00, 0x00};
                ;
                result = nfcbTag.transceive(cmd);
                sb.append("寻卡指令:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                cmd = new byte[]{0x1D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
                        0x01, 0x08};
                result = nfcbTag.transceive(cmd);
                sb.append("选卡指令:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                sb.append("读固定信息指令\n");

                cmd = new byte[]{0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x60,
                        0x02};
                result = nfcbTag.transceive(cmd);
                sb.append("发:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                cmd = new byte[]{(byte) 0x80, (byte) 0xB0, 0x00, 0x00, 0x20};
                result = nfcbTag.transceive(cmd);
                sb.append("发:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                cmd = new byte[]{0x00, (byte) 0x88, 0x00, 0x52, 0x0A,
                        (byte) 0xF0, 0x00, 0x0E, 0x0C, (byte) 0x89, 0x53,
                        (byte) 0xC3, 0x09, (byte) 0xD7, 0x3D};
                result = nfcbTag.transceive(cmd);
                sb.append("发:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                cmd = new byte[]{0x00, (byte) 0x88, 0x00, 0x52, 0x0A,
                        (byte) 0xF0, 0x00,};
                result = nfcbTag.transceive(cmd);
                sb.append("发:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
                cmd = new byte[]{0x00, (byte) 0x84, 0x00, 0x00, 0x08};
                result = nfcbTag.transceive(cmd);
                sb.append("发:" + ByteArrayToHexString(cmd) + "\n");
                sb.append("收:" + ByteArrayToHexString(result) + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mNfcText.setText(result);
            try {
                nfcbTag.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private String ByteArrayToHexString(byte[] inarray) { // converts byte
        // arrays to string
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


}
