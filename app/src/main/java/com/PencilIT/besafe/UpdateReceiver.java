package com.PencilIT.besafe;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class UpdateReceiver extends BroadcastReceiver {

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Context ctx;
    int draft_i;
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4;

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        sharedpreferences = context.getSharedPreferences("BeSafe", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        if (isConnectingToInternet(context)) {
            UpdateNext();
        }


    }

    private void UpdateNext() {

        draft_i = sharedpreferences.getInt("draft_i", 0);
        if (draft_i > 0) {

            String id = sharedpreferences.getString("userid" + (draft_i - 1), " ");
            String title = sharedpreferences.getString("title" + (draft_i - 1), " ");
            String date = sharedpreferences.getString("datetime" + (draft_i - 1), " ");
            String description = sharedpreferences.getString("description" + (draft_i - 1), " ");
            String action = sharedpreferences.getString("action" + (draft_i - 1), " ");
            String gps = sharedpreferences.getString("gps" + (draft_i - 1), " ");
            String addrs = sharedpreferences.getString("addrs" + (draft_i - 1), " ");
            String city = sharedpreferences.getString("city" + (draft_i - 1), " ");
            String unit = sharedpreferences.getString("unit" + (draft_i - 1), " ");
            String img1 = sharedpreferences.getString("bitmap1" + (draft_i - 1), " ");
            String img2 = sharedpreferences.getString("bitmap2" + (draft_i - 1), " ");
            String img3 = sharedpreferences.getString("bitmap3" + (draft_i - 1), " ");
            String img4 = sharedpreferences.getString("bitmap4" + (draft_i - 1), " ");

            bitmap1 = decodeBase64(img1);
            bitmap2 = decodeBase64(img2);
            bitmap3 = decodeBase64(img3);
            bitmap4 = decodeBase64(img4);


            //new SubmitAsync(id, title, date, description, action, gps, addrs, city, unit, img1, img2, img3, img4).execute();
            new UploadToServer(id, title, date, description, action, gps, addrs, city, unit, img1, img2, img3, img4).execute();


        }

    }

    public boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            //LogUtils.d("Network","NETWORKNAME: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }

    public void Notification(Context context, String message) {
        // Set Notification Title
        String strtitle = "Besafe";
        // Open NotificationView Class on Notification Click

        Intent intent = new Intent(context, DraftActivity.class);
        // Send data to NotificationView Class
        intent.putExtra("title", strtitle);
        intent.putExtra("text", message);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.drawable.ic_launcher)
                // Set Title
                .setContentTitle("Besafe")
                // Set Text
                .setContentText(message)
                // Add an Action Button below Notification
                //.addAction(R.drawable.ic_launcher, "Action Button", pIntent)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true);


        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    class SubmitAsync extends AsyncTask<String, String, String> {

        String id, title, date, description, action, gps, addrs, city, unit, img1, img2, img3, img4;
        JSONParser jsonParser = new JSONParser();


        SubmitAsync(String id, String title, String date, String description, String action, String gps, String addrs, String city,
                    String unit, String img1, String img2, String img3, String img4) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.description = description;
            this.action = action;
            this.gps = gps;
            this.addrs = addrs;
            this.city = city;
            this.unit = unit;
            this.img1 = img1;
            this.img2 = img2;
            this.img3 = img3;
            this.img4 = img4;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("title", title);
                params.put("date", date);
                params.put("description", description);
                params.put("action", action);
                params.put("gps", gps);
                params.put("addrs", addrs);
                params.put("city", city);
                params.put("unit", unit);
                //if(!img1.equals(" "))
                params.put("img1", img1);
                //if(!img2.equals(" "))
                params.put("img2", img2);
                //if(!img3.equals(" "))
                params.put("img3", img3);
                //if(!img4.equals(" "))
                params.put("img4", img4);

                Log.d("request", "starting");

                String result = jsonParser.makeHttpRequest(Config.DRAFT_URL, "POST", params);

                if (result != null) {
                    Log.d("JSON result", result);

                    return result;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {

            //String dialogText = "";
            if (result != null && result.equals("Success!")) {
                //dialogText = "Data Uploaded Successfully";

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putInt("draft_i", draft_i - 1);
                editor.remove("draft_" + (draft_i - 1));

                editor.remove("userid" + (draft_i - 1));
                editor.remove("title" + (draft_i - 1));
                editor.remove("datetime" + (draft_i - 1));
                editor.remove("description" + (draft_i - 1));
                editor.remove("action" + (draft_i - 1));
                editor.remove("gps" + (draft_i - 1));
                editor.remove("addrs" + (draft_i - 1));
                editor.remove("city" + (draft_i - 1));
                editor.remove("unit" + (draft_i - 1));
                editor.remove("bitmap1" + (draft_i - 1));
                editor.remove("bitmap2" + (draft_i - 1));
                editor.remove("bitmap3" + (draft_i - 1));
                editor.remove("bitmap4" + (draft_i - 1));

                editor.apply();
                UpdateNext();

                //DraftActivity inst = DraftActivity.instance();
                //inst.setNotification();
                Notification(ctx, "Draft Uploaded");

            } else {
                //dialogText = "Upload unsuccessful";

                //UpdateNext();
            }


        }

    }

    private class UploadToServer extends AsyncTask<Void, Integer, String> {

        String id, title, date, description, action, gps, addrs, city, unit, img1, img2, img3, img4;

        UploadToServer(String id, String title, String date, String description, String action, String gps, String addrs, String city,
                       String unit, String img1, String img2, String img3, String img4) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.description = description;
            this.action = action;
            this.gps = gps;
            this.addrs = addrs;
            this.city = city;
            this.unit = unit;
            this.img1 = img1;
            this.img2 = img2;
            this.img3 = img3;
            this.img4 = img4;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.SUBMIT_URL);

            try {

                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                //publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 4;

                //Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                if (bitmap1 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();

                    //File sourceFile = new File(filePath);
                    //String strFileName = sourceFile.getName();
                    //String strFileName = "asfs";

                    // Adding file data to http body
                    entity.addPart("img1", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(img1) + ".jpg"));
                    //entity.addPart("image", new FileBody(sourceFile));
                }
                if (bitmap2 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img2", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(img2) + ".jpg"));

                }
                if (bitmap3 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap3.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img3", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(img3) + ".jpg"));

                }
                if (bitmap4 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap4.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img4", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(img4) + ".jpg"));

                }

                // Extra parameters if you want to pass to server
                entity.addPart("id", new StringBody(id));
                entity.addPart("title", new StringBody(title));
                entity.addPart("date", new StringBody(date));

                entity.addPart("description", new StringBody(description));
                entity.addPart("action", new StringBody(action));
                entity.addPart("gps", new StringBody(gps));
                entity.addPart("city", new StringBody(city));
                entity.addPart("unit", new StringBody(unit));
                entity.addPart("addrs", new StringBody(addrs));

                //totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.equals("Success!")) {
                //dialogText = "Data Uploaded Successfully";

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putInt("draft_i", draft_i - 1);
                editor.remove("draft_" + (draft_i - 1));

                editor.remove("userid" + (draft_i - 1));
                editor.remove("title" + (draft_i - 1));
                editor.remove("datetime" + (draft_i - 1));
                editor.remove("description" + (draft_i - 1));
                editor.remove("action" + (draft_i - 1));
                editor.remove("gps" + (draft_i - 1));
                editor.remove("addrs" + (draft_i - 1));
                editor.remove("city" + (draft_i - 1));
                editor.remove("unit" + (draft_i - 1));
                editor.remove("bitmap1" + (draft_i - 1));
                editor.remove("bitmap2" + (draft_i - 1));
                editor.remove("bitmap3" + (draft_i - 1));
                editor.remove("bitmap4" + (draft_i - 1));

                editor.apply();
                UpdateNext();

                //DraftActivity inst = DraftActivity.instance();
                //inst.setNotification();
                Notification(ctx, "Draft Uploaded");

            } else {
                //dialogText = "Upload unsuccessful";

                //UpdateNext();
            }

            super.onPostExecute(result);

        }

    }

}