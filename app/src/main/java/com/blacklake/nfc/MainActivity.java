package com.blacklake.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blacklake.nfc.datastore.DBHelp;
import com.blacklake.nfc.datastore.MCTag;
import com.blacklake.nfc.util.Promise;
import com.wugj.nfc.nfc.util.Constant;
import com.wugj.nfc.nfc.util.MLog;


public class MainActivity extends AppCompatActivity {
    private TextView ifo_NFC;
    private static final String[] strs = new String[]{

            "写MIfareClassic文本数据",

            "读MifareClassic中的文本数据",

//            "写MifareUltraligth中的文本数据",
    };
    private NfcAdapter mNfcAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHelp.cleanDataBase();

        ifo_NFC = (TextView) findViewById(R.id.ifo_NFC);
        ifo_NFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testInsert();
            }
        });


        // NFC适配器，所有的关于NFC的操作从该适配器s进行
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!ifNFCUse()) {
            return;
        }
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchActivity(position);
            }
        });


    }


    private void testInsert(){
        new Promise().then(new Promise.Work<MCTag>() {
            @Override
            public MCTag work() {
                MCTag mcTag = new MCTag();
                mcTag.tagId = "123";
                mcTag.keys = Constant.STAND_KEYS;
                mcTag.lastModify = System.currentTimeMillis();
                DBHelp.insertTag(mcTag);
                MCTag mcTag1 = DBHelp.getTargetTag("123");
                return mcTag1;
            }
        }).schedule(Promise.ThreadType.UIThread)
                .complete(new Promise.Complete<MCTag>() {
            @Override
            public void complete(MCTag mcTag) {
                MLog.i("testTag","mcTag is null = " + (mcTag == null));
            }
        }).start();
    }



    private void switchActivity(int position) {
        switch (position) {

            case 0:
                startActivity(new Intent(this, MCWriteTextActivity.class));
                break;

            case 1:
                startActivity(new Intent(this, MCReadTextActivity.class));
                break;

            case 2:
                startActivity(new Intent(this, MifareUltraligthActivity.class));
                break;

            default:
                break;
        }
    }

    /**
     * 检测工作,判断设备的NFC支持情况
     *
     * @return
     */
    protected Boolean ifNFCUse() {
        if (mNfcAdapter == null) {
            ifo_NFC.setText("设备不支持NFC！");
            return false;
        }
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            ifo_NFC.setText("请在系统设置中先启用NFC功能！");
            return false;
        }
        return true;
    }
}

