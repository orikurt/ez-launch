package com.sadna.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class serviceBootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, StatisticsService.class);
        context.startService(startServiceIntent);
    }
}