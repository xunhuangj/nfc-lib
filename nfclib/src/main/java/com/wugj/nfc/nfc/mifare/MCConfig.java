package com.wugj.nfc.nfc.mifare;

public class MCConfig {

    //
    private boolean isAutoConnected = true;

    private boolean retryAuth = true;

    private int retryAuthCount = 0;
    private boolean useTextForCreateKeys = true;


    public MCConfig(Builder builder){
        if(builder != null){
            //access private for why.
            isAutoConnected = builder.isAutoConnected;
            retryAuth = builder.retryAuth;
            retryAuthCount = builder.retryAuthCount;
            useTextForCreateKeys = builder.useTextForCreateKeys;
        }
    }

    public boolean isUseTextForCreateKeys() {
        return useTextForCreateKeys;
    }

    public void setUseTextForCreateKeys(boolean useTextForCreateKeys) {
        this.useTextForCreateKeys = useTextForCreateKeys;
    }


    public static class Builder{

        private boolean isAutoConnected = false;

        private boolean retryAuth = false;

        private int retryAuthCount = 0;

        //
        private boolean useTextForCreateKeys = true;

        public Builder(MCConfig mcConfig){
            this.isAutoConnected = mcConfig.isAutoConnected;
            this.retryAuth = mcConfig.retryAuth;
            this.retryAuthCount = mcConfig.retryAuthCount;
            this.useTextForCreateKeys = mcConfig.useTextForCreateKeys;
        }

        public Builder(){

        }

        public Builder setAutoConnected(boolean autoConnected) {
            isAutoConnected = autoConnected;
            return this;
        }

        public Builder setRetryAuth(boolean retryAuth) {
            this.retryAuth = retryAuth;
            return  this;
        }

        public Builder setRetryAuthCount(int retryAuthCount) {
            this.retryAuthCount = retryAuthCount;
            return  this;
        }

        public MCConfig build(){
            return new MCConfig(this);
        }

        public Builder setUseTextForCreateKeys(boolean useTextForCreateKeys) {
            this.useTextForCreateKeys = useTextForCreateKeys;
            return this;
        }
    }


    public Builder newBuild(){
        return new Builder(this);
    }

    public void setAutoConnected(boolean autoConnected) {
        isAutoConnected = autoConnected;
    }

    public boolean isAutoConnected() {
        return isAutoConnected;
    }

    public boolean isRetryAuth() {
        return retryAuth;
    }

    public void setRetryAuth(boolean retryAuth) {
        this.retryAuth = retryAuth;
    }

    public int getRetryAuthCount() {
        return retryAuthCount;
    }

    public void setRetryAuthCount(int retryAuthCount) {
        this.retryAuthCount = retryAuthCount;
    }
}
