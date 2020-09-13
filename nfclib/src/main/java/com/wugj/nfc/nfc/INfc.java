package com.wugj.nfc.nfc;

import java.io.Closeable;

// real operation
public interface INfc<T> extends Closeable {

      void write(NfcCall.Callback<T> writeCallback);
      void read(NfcCall.Callback<T> readCallback);
      void format(NfcCall.Callback<T> formatCallback);

}
