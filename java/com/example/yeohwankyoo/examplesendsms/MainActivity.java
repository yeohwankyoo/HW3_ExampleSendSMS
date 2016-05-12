package com.example.yeohwankyoo.examplesendsms;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    Context mContext;
    WebView browser;
    SendClass sendClass =  new SendClass();
    String phoneNumber = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //smsNumber = (EditText) findViewById(R.id.smsNumber);
        //smsTextContext = (EditText) findViewById(R.id.smsText);

        browser = (WebView)findViewById(R.id.webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(sendClass, "Android");
        browser.loadUrl("file:///android_asset/sendSMS.html");

        //Processing permission for SDK 23 Version(Android 6.0)
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "SMS 발신 권한 있음.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "SMS 발신 권한 없음.", Toast.LENGTH_SHORT).show();
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                Toast.makeText(this, "SMS 발신 권한 설명필요함.", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, 1);
            }
        }
    }

    //Processing permission for SDK 23 Version(Android 6.0)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode)
        {
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED){
                    Toast.makeText(this, "SMS 권한을 사용자가 승인함.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "SMS 권한 거부됨.", Toast.LENGTH_SHORT).show();
                }

            }
            break;

        }
    }


    /*
    When HTML code send message, then JavaScript Interface process data,
    and call sendSMS method to send SMS
    */
    public void sendSMS(String smsNumber, String smsText){

        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        /**
         * SMS가 발송될때 실행
         * When the SMS massage has been sent
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        /**
         * SMS가 도착했을때 실행
         * When the SMS massage has been delivered
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    /*JavaScript Interface

    When HTML send message to java code,
    process data and return data.
    */
 public class SendClass{
     private String commonData = "";

        // When HTML send phone number
     @JavascriptInterface
     public String setPhoneNumber(String num){

         //Sava data
         phoneNumber = num;

         //return phoneNumber to HTML
         return phoneNumber;
     }

        /*
        When HTML send message process data
        if it doesn't have phone number or message, it can't send SMS
        But it has two data, it can send SMS
        it method call sendSMS method.
         */
     @JavascriptInterface
     public void MESSAGE(String num, String msg){
         phoneNumber = num;
         if(msg.length()>0 && phoneNumber.length()>0){
             commonData = msg;
             sendSMS(phoneNumber, commonData);
         }
         else
            Toast.makeText(getApplicationContext(), "Enter Number or MSG", Toast.LENGTH_SHORT).show();
         Toast.makeText(getApplicationContext(), msg , Toast.LENGTH_SHORT).show();
     }
 }
}

