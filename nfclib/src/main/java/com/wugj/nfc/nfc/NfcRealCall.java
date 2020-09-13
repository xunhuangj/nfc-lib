package com.wugj.nfc.nfc;


import com.wugj.nfc.nfc.mifare.MCConfig;
import com.wugj.nfc.nfc.mifare.MCRequest;
import com.wugj.nfc.nfc.mifare.MifareClssicImp;
import com.wugj.nfc.nfc.util.MLog;
import com.wugj.nfc.nfc.util.UIRun;

public class NfcRealCall implements NfcCall {


    private NfcRequest request;

    private NfcClient client;
    private INfc iNfc;

    private boolean isExcute = false;

    public NfcRealCall(NfcRequest request, NfcClient mcClient) {
        this.client = mcClient;
        this.request = request;
    }

    public static NfcRealCall newRealCall(NfcRequest mcRequest, NfcClient mcClient) {

        NfcRealCall call = new NfcRealCall(mcRequest, mcClient);
        return call;
    }

    private Callback callback;
    @Override
    public void enqueue(Callback callback) {
        try {
            if(callback == null)
                throw new NullPointerException("callback is null");
            this.callback = callback;
            synchronized (this) {
                if (isExcute)
                    throw new IllegalStateException("this call is running");
                isExcute = true;
            }

            if (request instanceof MCRequest) {
                MCRequest.Builder newRequestBuilder = (MCRequest.Builder) request.newBuilder().setNfcType(NfcType.MifareClassic);
                if (newRequestBuilder.getMcConfig() == null) {
                    newRequestBuilder.setMcConfig(new MCConfig.Builder()
                            .setRetryAuthCount(1)
                            .setAutoConnected(true)
                            .setRetryAuth(true)
                            .build());
                }

                if(((MCRequest) request).getFrom() < 0){
                    newRequestBuilder.setFrom(0);
                }
                if(((MCRequest) request).getTo() < 0){
                    newRequestBuilder.setTo(255);
                }

                iNfc = MifareClssicImp.newRealCall(newRequestBuilder.build(), client);
            }


            if(iNfc == null){
                throw new IllegalArgumentException("request must extends NfcRequest that the frame relalize it");
            }

            client.getDispatcher().equeue(new AsyncCall(callback));
        } catch (Exception e) {
            MLog.e("callback",e.getMessage());
            callback.failedCall(request, e);
        }
    }

    class AsyncCall implements Runnable {

        Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                switch (request.getOperation()) {
                    case Read:
                        iNfc.read(callback);
                        break;

                    case Write:
                        iNfc.write(callback);
                        break;
                    case Format:
                        iNfc.format(callback);
                        break;
                }
            } catch (final Exception e) {
                MLog.e("callback",e.getMessage());
                setFailedCall(e);
            }finally {
//                try {
//                    iNfc.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    setFailedCall(new IOException("closing tag IOException"));
//                }
            }
        }
    }

    private void setFailedCall(final Exception e){
        UIRun.runOnUI(new Runnable() {
            @Override
            public void run() {
                callback.failedCall(request,e);
            }
        });
    }


}
