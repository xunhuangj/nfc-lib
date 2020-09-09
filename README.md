# nfc-lib
nfc-lib 格式化，写入数据，读出数据.目前支持M1卡...你的赞力是我前进的动力...持续更新，兼容更多格式的读写期待后续

## 背景  
因入职一家做工厂携程的公司，工厂可能用到nfc 打卡，签到，或者一些贴在物料后面的电子标签，所以需要做一份Nfc开发调研报告，需要写一个nfcTool 来实现读写格式化，而自己起初也是一个小白，以前也没做过硬件方面的开发，所以先得搞明白Nfc 是什么样的技术，和rfid的区别，nfc的一些常见用途，短板，支持一些什么样的格式。对于普通开发者能做到什么程度，这方面必须搞清楚啊，不然你都不知道自己在玩蛇皮哈哈！花了两周先做了一份调研，写了一篇报告[初识NFC](http), 才恍然大悟原来如此，当时为了看无限电波是啥，又要去补哈物理，找到这个家伙 [詹姆斯·克拉克·麦克斯韦](https://baike.baidu.com/item/%E8%A9%B9%E5%A7%86%E6%96%AF%C2%B7%E5%85%8B%E6%8B%89%E5%85%8B%C2%B7%E9%BA%A6%E5%85%8B%E6%96%AF%E9%9F%A6/314955?fromtitle=%E9%BA%A6%E5%85%8B%E6%96%AF%E9%9F%A6&fromid=161423&fr=aladdin), 原来是这样啊，wifi ，蓝牙都是无线电波技术，我勒个去。搞清楚了十个啥子技术，那咋开发呢，后面碰到的一个雷就是，一直在找如何读NfcA ....NfcB...NfcF ,发现找遍所有文档，塔喵的没有卵用，都是告诉我咋读公交，身份证...之流的云云，拜托我要的是一个能自己开发这种格式的文档详解，不是你喵的给我提供各种已有的指令去操作，后面终于了解到他呀的这些个格式根本没法开发的，除非知道**APDU**(application data unit)，我去搞半天这都不能开发的，于是继续找找到一个M1卡的才知道原来这种格式的卡可以自己定制开发，而且有秘钥和访问控制的，终于发现苗头了，但是市面上的东西写的有点拉稀了，终于自己又钻研了一番，而且分析了[ikarus23](https://github.com/ikarus23/MifareClassicTool)大神的东东，获益匪浅（主要是访问控制哈）, 于是决定自己整一个库希望能对MifareClassic,Ndef,Ndeformatable 能开发的格式进行开发，能写入数据，读取数据满足基本需求，毕竟这才是我的宗旨。

## 使用
**MifareClassic格式化<br />**
<img src="https://github.com/xunhuangj/images/blob/master/nfcimg/nfc_format.jpg" width="35%" height="35%"><br/>
**MifareClassic写入数据**<br/>
<img src="https://github.com/xunhuangj/images/blob/master/nfcimg/nfc_write.jpg" width="35%" height="35%"><br/>
__MifareClassic读出数据__<br/>
<img src="https://github.com/xunhuangj/images/blob/master/nfcimg/nfc_read.jpg" width="35%" height="35%"><br/>
## code
**格式mifareClassic**
```
mcSimpleRW.setCurKeys(curKeys);
mcSimpleRW.setNewKeys(newKeys);
mcSimpleRW.nfcrw.format(intent,new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
                UIRun.toastLength(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse mcResponse = nfcResponse.getResponse();
                UIRun.toastLength(mcResponse.toString());

            }
        });
```
**写入数据**
```
mcSimpleRW.setCurKeys(curKeys);
   mcSimpleRW.nfcrw.write(intent, mText, new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
                UIRun.toastLength(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse response = nfcResponse.getResponse();
                UIRun.toastLength("写入成功");
            }
        });
```
**读出数据**
```
mcSimpleRW.setCurKeys(curKeys);
    mcSimpleRW.nfcrw.read(intent, new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
//                    UIRun.toastLength(ex.getMessage());
                mNfcText.setText(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse mcResponse = nfcResponse.getResponse();
                mNfcText.setText(mcResponse.toString() + "\n" + mcResponse.getHexString());
            }
        });
```





