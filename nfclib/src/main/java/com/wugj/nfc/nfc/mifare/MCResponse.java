package com.wugj.nfc.nfc.mifare;

import android.text.TextUtils;
import android.util.SparseArray;

import com.wugj.nfc.nfc.util.MLog;
import com.wugj.nfc.nfc.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

final public class MCResponse {

    final String TAG = MCResponse.class.getSimpleName();

    private SparseArray<String []> hexMap;


    private String content;

    void setContent(String content) {
        this.content = content;
    }

    //user data.

    //some section is broken.
    private List<HashMap<String,String>> errorMap;


    public MCResponse(SparseArray<String []> hexMap){
        this.hexMap= hexMap;
        this.content = getTextFromBytes();
    }

    public MCResponse(){

    }

    private String getContent() {
        return content == null ? content: content.trim();
    }

    public String toString(){
        return getContent();
    }


    /**
     * from to ,maping
     * @return
     */
    private String getTextFromBytes(){

        if(hexMap == null || hexMap.size() == 0) return null;

        byte [] bytesContent = getBytesFromHexMap(hexMap);
        if(bytesContent == null || bytesContent.length == 0){
            MLog.w(TAG,"bytesContent  is empty");
            return null;
        }
        try {
            return new String(bytesContent,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            MLog.e(TAG,"UnsupportedEncodingException");
        }
        return null;
    }


    /**
     * decode hex to bytes from section1 to lastSection
     * @param hexMap
     * @return
     */
    private byte [] getBytesFromHexMap(SparseArray<String []> hexMap){

        List<byte []> bytes = new ArrayList<>();
        for(int i = 0 ; i < hexMap.size() ;i++){
            int sectionIndex = hexMap.keyAt(i);
            String [] hexArray = hexMap.get(sectionIndex);
            //只取数据区
            for(int j = sectionIndex == 0 ? 1 : 0; j < hexArray.length - 1 ; j++){
                byte [] blockBytes = Utils.hexStringToByteArray(hexArray[j]);
                bytes.add(blockBytes);
            }
        }

        return Utils.byteMergerAll(bytes);
    }


    // get hexString
    public String getHexString(){

        if(hexMap == null) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0 ;i < hexMap.size(); i++){

            String [] hexArray = hexMap.get(hexMap.keyAt(i));
            for(int j = 0 ; j < hexArray.length ; j++){
                stringBuilder.append(hexArray[j]).append("\n");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }


    public List<HashMap<String, String>> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(List<HashMap<String, String>> errorMap) {
        this.errorMap = errorMap;
    }

    /**
     * if you write some items , we get a list. split "\n"
     * one item this list size is 1
     */
    public List<String> getData(){
        if(TextUtils.isEmpty(content)) return null;
        return Arrays.asList( content.split("\n"));
    }


}
