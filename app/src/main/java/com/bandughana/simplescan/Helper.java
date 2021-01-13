package com.bandughana.simplescan;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class Helper {
    private static final int RC_CODE = 100;

    static void openSearch(String mUrl, Context context) {
        String url = "https://www.google.com/search?q=" + mUrl;
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    static void openUrl(String url, Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }


    static void shareContent(String content, Context context){
        if (!content.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);

            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            context.startActivity(shareIntent);
        }
    }

    public static void shareApp(Context context, String title){
        String message;
        if (title != null){
            message = title +
                    "\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName();
        } else {
            message = "Hello! Please download the Simple Scan app for all your " +
                    " QR Code/Barcode scanning tasks. It's free and a modern way of finding " +
                    " your best products online. Get it here:\n" +
                    "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, message)
                .setType("text/plain");
        PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(), RC_CODE, new Intent(context.getApplicationContext(),
                ShareBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent share = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            share = Intent.createChooser(intent, null, pi.getIntentSender());
        } else {
            share = Intent.createChooser(intent, null);
        }
        context.startActivity(share);
    }

    static private boolean verifySignatureOnServer(String data, String signature) {
        String retFromServer = "";
        URL url;
        HttpsURLConnection urlConnection = null;
        try {
            String urlStr = "https://www.example.com/verify.php?data=" + URLEncoder.encode(data, "UTF-8") + "&signature=" + URLEncoder.encode(signature, "UTF-8");

            url = new URL(urlStr);
            urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader inRead = new InputStreamReader(in);
            retFromServer = convertStreamToString(inRead);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return retFromServer.equals("good");
    }

    private static String convertStreamToString(java.io.InputStreamReader is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    static void rateUs(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent playIntent = new Intent(Intent.ACTION_VIEW, uri);

        playIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(playIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }
    static void copyText(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Scanned Text", text);
        Objects.requireNonNull(clipboardManager).setPrimaryClip(clip);
    }
    
    static boolean supportsFlashLight(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
