package com.wugj.nfc.nfc;

import android.nfc.Tag;

import com.wugj.nfc.nfc.mifare.MCCommon;

//different nfc card the request is different ,we must extends the common request
public  class NfcRequest {


    protected Tag tag;

    public Tag getTag() {
        return tag;
    }

    private NfcType nfcType;

    public NfcType getNfcType() {
        return nfcType;
    }

    //do what.
    private MCCommon.Operations operation;

    public MCCommon.Operations getOperation() {
        return operation;
    }

    protected String writeText;

    public String getWriteText() {
        return writeText;
    }

    protected boolean isAppend = false;

    public boolean isAppend() {
        return isAppend;
    }


    protected NfcRequest(Builder builder) {
        this.operation = builder.operation;
        this.writeText = builder.writeText;
        this.isAppend = builder.isAppend;
        this.tag = builder.tag;
        this.nfcType = builder.nfcType;
    }


    public Builder newBuilder() {
        return new Builder(this);
    }


    public  static class Builder {


        private MCCommon.Operations operation;
        private String writeText;
        private boolean isAppend;
        private Tag tag;
        private NfcType nfcType;

        public Builder(){

        };

        public Builder setTag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public Builder(NfcRequest request) {
            this.operation = request.operation;
            this.isAppend = request.isAppend;
            this.writeText = request.writeText;
            this.tag = request.tag;
        }


        public Builder setNfcType(NfcType nfcType) {
            this.nfcType = nfcType;
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


        public Builder setOperation(MCCommon.Operations operation) {
            this.operation = operation;
            return this;
        }

        public NfcRequest build() {
            return new NfcRequest(this);
        }
    }

}