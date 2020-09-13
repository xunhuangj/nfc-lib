package com.wugj.nfc.nfc.mifare;

import android.nfc.Tag;

import com.wugj.nfc.nfc.NfcRequest;

public final class MCRequest extends NfcRequest {

    //section from
     private int from ;

    public int getFrom() {
        return from;
    }

    //section to .
    private int to;

    public int getTo() {
        return to;
    }

    private MCKeysFactory keyFactory;

    public MCKeysFactory getKeyFactory() {
        return keyFactory;
    }

    //section keys.
   private   String [] keys;
    public String [] getKeys(){
        return keys;
    }


    /**
     * new keys that replace nfc keys
     * when you want to format with new keys,you set saveKeys
     * @see MCCommon.Operations Format
     */
    private String [] saveKeys;
    public String[] getSaveKeys() {
        return saveKeys;
    }


    private MCConfig mcConfig;
    public MCConfig getMcConfig() {
        return mcConfig;
    }

    private Tag tag;
    public Tag getTag() {
        return tag;
    }

    //do what.
    private MCCommon.Operations operation;
    public MCCommon.Operations getOperation(){
        return operation;
    }

    private String writeText;
    public String getWriteText() {
        return writeText;
    }

    boolean isAppend = false;
    public boolean isAppend(){
        return isAppend;
    }



     MCRequest(Builder builder){
        super(builder);
        this.from = builder.from;
        this.to = builder.to;
        this.keys = builder.keys;
        this.operation = builder.operation;
        this.writeText = builder.writeText;
        this.isAppend = builder.isAppend;
        this.mcConfig = builder.mcConfig;
        this.tag = builder.tag;
        this.keyFactory = builder.keyFactory;
        this.saveKeys = builder.saveKeys;
     }



    public Builder newBuilder(){
        return new Builder(this);
    };



    public final  static class Builder extends NfcRequest.Builder{

         private int from ;
         private int to;
         private String keys [];
        private MCCommon.Operations operation;
        private String writeText;
        private boolean isAppend;
        private MCConfig mcConfig;
        private MCKeysFactory keyFactory;
        private String saveKeys[];

        public Builder setKeyFactory(MCKeysFactory keyFactory) {
            this.keyFactory = keyFactory;
            return this;
        }

        public MCConfig getMcConfig() {
            return mcConfig;
        }

        private Tag tag;

        public Builder setTag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public Builder(){
            keyFactory = MCKeysFactory.DefaultMCKeysFactory;
            from = 0;
            to = -1;
            keys = null;
            operation = null;
            isAppend = false;
        }

        public Builder(MCRequest request){
            super(request);
            this.from = request.from;
            this.to = request.to;
            this.keys = request.keys;
            this.operation = request.operation;
            this.isAppend = request.isAppend;
            this.writeText = request.writeText;
            mcConfig = request.mcConfig;
            this.tag = request.tag;
            this.keyFactory = request.keyFactory;
            this.saveKeys = request.saveKeys;
        }

        public Builder setMcConfig(MCConfig mcConfig) {
            this.mcConfig = mcConfig;
            return this;
        }

        public Builder setSaveKeys(String[] saveKeys) {
            this.saveKeys = saveKeys;
            return this;
        }

        public Builder setWriteText(String writeText) {
            this.writeText = writeText;
            return this;
        }

        public Builder setAppend(boolean append) {
            isAppend = append;
            return this;
        }

        public Builder setFrom(int from) {
            this.from = from;
            return this;
        }

        public Builder setTo(int to) {
            this.to = to;
            return this;
        }

        public Builder setKeys(String[] keys) {
            this.keys = keys;
            return this;
        }

        public Builder setOperation(MCCommon.Operations operation) {
            this.operation = operation;
            return this;
        }

        public MCRequest build(){
            return new MCRequest(this);
        }
    }


}
