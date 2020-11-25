package com.loror.lororUtil.dataBus;

import android.content.Intent;

public interface DataBusReceiver {
    void receiveData(String name, Intent data);
}
