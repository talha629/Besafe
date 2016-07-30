package com.PencilIT.besafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SafetyObservationFormActivity extends Activity implements View.OnClickListener, OnItemSelectedListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;
    private static final int SELECT_FILE = 1;
    private static final String[] LOCATION_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    static TextView datetext, timetext;
    static String monthArray[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    TextView titletext, addressgpslat, addressgpslon, address;
    Spinner city, unit;
    EditText descriptions, acitionTaken;
    Button upload, submit, map, date, time, draft;
    ImageButton back, user;
    String userid;
    ImageView picture1, picture2, picture3, picture4;
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4;
    int pic = 0;
    SharedPreferences sharedpreferences;
    Location userLocation;
    private Uri fileUri;

    public static void setDate(String s) {
        datetext.setText(s);
    }

    public static void setTime(String s) {
        timetext.setText(s);
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_observation_form);

        Intent i = getIntent();
        String safetyObservationType = i.getStringExtra("SafetyObservation");
        // Toast.makeText(getApplicationContext(),safetyObservationType,Toast.LENGTH_LONG).show();
        sharedpreferences = getSharedPreferences("BeSafe", Context.MODE_PRIVATE);
        userid = sharedpreferences.getString("userid", "");

        titletext = (TextView) findViewById(R.id.titletext);
        titletext.setText(safetyObservationType);

        descriptions = (EditText) findViewById(R.id.description);
        acitionTaken = (EditText) findViewById(R.id.correctiveaction);

        address = (TextView) findViewById(R.id.address);
        addressgpslat = (TextView) findViewById(R.id.addressgpslat);
        addressgpslon = (TextView) findViewById(R.id.addressgpslon);
        datetext = (TextView) findViewById(R.id.datetext);
        timetext = (TextView) findViewById(R.id.timetext);
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        String am_pm;
        if (c.get(Calendar.HOUR_OF_DAY) >= 12)
            am_pm = "PM";
        else
            am_pm = "AM";
        datetext.setText(day + " " + monthArray[month] + " " + year);
        timetext.setText(hour + ":" + minute + " " + am_pm);
        city = (Spinner) findViewById(R.id.selectcity);

        ArrayAdapter<CharSequence> adapterCity = ArrayAdapter.createFromResource(this, R.array.City,
                android.R.layout.simple_spinner_item);
        adapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(adapterCity);

        city.setOnItemSelectedListener(this);

        unit = (Spinner) findViewById(R.id.selectunit);


        //unit.setOnItemSelectedListener(this);

        date = (Button) findViewById(R.id.buttondate);
        date.setOnClickListener(this);
        time = (Button) findViewById(R.id.buttontime);
        time.setOnClickListener(this);
        submit = (Button) findViewById(R.id.buttonSubmit);
        submit.setOnClickListener(this);
        draft = (Button) findViewById(R.id.buttonDraft);
        draft.setOnClickListener(this);
        upload = (Button) findViewById(R.id.buttonupload);
        upload.setOnClickListener(this);
        map = (Button) findViewById(R.id.buttonmap);
        map.setOnClickListener(this);
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(this);
        user = (ImageButton) findViewById(R.id.user);
        user.setOnClickListener(this);

        picture1 = (ImageView) findViewById(R.id.picture1);
        picture2 = (ImageView) findViewById(R.id.picture2);
        picture3 = (ImageView) findViewById(R.id.picture3);
        picture4 = (ImageView) findViewById(R.id.picture4);
        bitmap1 = null;
        bitmap2 = null;
        bitmap3 = null;
        bitmap4 = null;

        locationAccess();
        UpdateLocation();
        StoragePermissionGrant();
    }

    private void UpdateLocation() {
        Location loc = getLastBestLocation();

        if (loc != null) {
            userLocation = loc;
            addressgpslat.setText("Lat:" + loc.getLatitude());
            addressgpslon.setText("Lon:" + loc.getLongitude());


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String area = " ", city = " ";
            if (addresses != null) {
                area = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
            } else
                Toast.makeText(this, "No Address Found", Toast.LENGTH_LONG).show();

            // Toast.makeText(this, address+" "+city ,
            // Toast.LENGTH_LONG).show();
            address.setText(area + "," + city);

            Toast.makeText(this, loc.getLatitude() + "\nLong: " + loc.getLongitude(), Toast.LENGTH_LONG).show();


        }
    }

    @SuppressLint("NewApi")
    public void locationAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canAccessLocation()) {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.safety_observation_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public String getStringTextView(TextView t) {
        if (t != null)
            return t.getText().toString();
        else
            return "-";
    }

    public String getStringEditText(EditText t) {
        if (t != null)
            return t.getText().toString();
        else
            return "-";
    }

    public String getStringSPinner(Spinner t) {
        if (t != null)
            return t.getSelectedItem().toString();
        else
            return "-";
    }

    public String getBitmap(Bitmap t) {
        if (t != null)
            return getStringImage(t);
        else
            return "-";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonSubmit:
                Log.d("button", "clicked");
                if (isOnline()) {
                    /*new SubmitAsync(userid, getStringTextView(titletext),
                            getStringTextView(datetext) + " " + getStringTextView(timetext), getStringEditText(descriptions),
                            getStringEditText(acitionTaken), getStringTextView(addressgpslat) + "" + getStringTextView(addressgpslon), getStringTextView(address), getStringSPinner(city),
                            getStringSPinner(unit), getBitmap(bitmap1), getBitmap(bitmap2), getBitmap(bitmap3),
                            getBitmap(bitmap4)).execute();*/

                    new UploadToServer(userid, getStringTextView(titletext),
                            getStringTextView(datetext) + " " + getStringTextView(timetext), getStringEditText(descriptions),
                            getStringEditText(acitionTaken), getStringTextView(addressgpslat) + "" + getStringTextView(addressgpslon), getStringTextView(address), getStringSPinner(city),
                            getStringSPinner(unit)).execute();
                } else {
                    SaveDraft(userid, getStringTextView(titletext),
                            getStringTextView(datetext) + " " + getStringTextView(timetext), getStringEditText(descriptions),
                            getStringEditText(acitionTaken), getStringTextView(addressgpslat) + "" + getStringTextView(addressgpslon), getStringTextView(address), getStringSPinner(city),
                            getStringSPinner(unit), getBitmap(bitmap1), getBitmap(bitmap2), getBitmap(bitmap3),
                            getBitmap(bitmap4));

                    finish();

                }

                break;
            case R.id.buttonDraft:
                Log.d("button", "clickeddraft");

                SaveDraft(userid, getStringTextView(titletext),
                        getStringTextView(datetext) + " " + getStringTextView(timetext), getStringEditText(descriptions),
                        getStringEditText(acitionTaken), getStringTextView(addressgpslat) + "" + getStringTextView(addressgpslon), getStringTextView(address), getStringSPinner(city),
                        getStringSPinner(unit), getBitmap(bitmap1), getBitmap(bitmap2), getBitmap(bitmap3),
                        getBitmap(bitmap4));

                finish();

                break;
            case R.id.buttonmap:


                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                if (userLocation != null) {
                    intent.putExtra("lat", userLocation.getLatitude());
                    intent.putExtra("lon,", userLocation.getLongitude());
                }
                startActivity(intent);
                UpdateLocation();
                //Toast.makeText(this, "map", Toast.LENGTH_LONG).show();


                break;
            case R.id.buttonupload:
                if (pic < 4) {
                    selectImage();

                } else
                    Toast.makeText(this, "Max pic Limit", Toast.LENGTH_LONG).show();
                break;
            case R.id.buttondate:

                DialogFragment newFragmentDate = new DatePickerFragment();
                newFragmentDate.show(getFragmentManager(), "datePicker");

                break;
            case R.id.buttontime:

                DialogFragment newFragmentTime = new TimePickerFragment();
                newFragmentTime.show(getFragmentManager(), "timePicker");

                break;

            case R.id.back:

                finish();
                break;
            case R.id.user:
                Intent intentUser = new Intent(getApplicationContext(), UserSettingActivity.class);
                startActivity(intentUser);

                break;

        }

    }

    private void SaveDraft(String d_userid, String d_title, String d_datetime, String d_description, String d_action, String d_gps, String d_addrs, String d_city, String d_unit, String d_bitmap1, String d_bitmap2, String d_bitmap3, String d_bitmap4) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        int draft_i = sharedpreferences.getInt("draft_i", 0);

        editor.putString("userid" + draft_i, d_userid);
        editor.putString("title" + draft_i, d_title);
        editor.putString("datetime" + draft_i, d_datetime);
        editor.putString("description" + draft_i, d_description);
        editor.putString("action" + draft_i, d_action);
        editor.putString("gps" + draft_i, d_gps);
        editor.putString("addrs" + draft_i, d_addrs);
        editor.putString("city" + draft_i, d_city);
        editor.putString("unit" + draft_i, d_unit);
        editor.putString("bitmap1" + draft_i, d_bitmap1);
        editor.putString("bitmap2" + draft_i, d_bitmap2);
        editor.putString("bitmap3" + draft_i, d_bitmap3);
        editor.putString("bitmap4" + draft_i, d_bitmap4);


        editor.putInt("draft_i", draft_i + 1);
        editor.apply();


    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                //
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                Bitmap photo = BitmapFactory.decodeFile(fileUri.getPath(), options);
                imagePreview(photo);

            } else if (requestCode == SELECT_FILE) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                //Bitmap photo = BitmapFactory.decodeFile(data.getData().getPath(), options);
                //imagePreview(photo);

                //Uri selectedImage = data.getData();
                Uri targetUri = data.getData();
                File sourceFile = new File(targetUri.getPath());
                String strFileName = sourceFile.getName();
                Toast.makeText(this, strFileName, Toast.LENGTH_LONG).show();


                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri), null, options);
                    //bitmap = FixImage(bitmap);
                    imagePreview(bitmap);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        }
    }

    private Bitmap FixImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        double ratio;
        int newWidth;
        int newHeight;
        int x = 180;
        if (width < height) {
            ratio = (double) x / width;
            newWidth = x;
            newHeight = (int) (height * ratio);
        } else {

            ratio = (double) x / height;
            newHeight = x;
            newWidth = (int) (width * ratio);

        }

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (newWidth), (int) (newHeight), true);
        return bitmap;
    }

    private void imagePreview(Bitmap photo) {
        // photo = Bitmap.createScaledBitmap(photo, (int) (photo.getWidth() *
        // 0.5), (int) (photo.getHeight() * 0.5), true);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x / 4 - 5;
        int screenHeight = size.y;

        double width = photo.getWidth();
        double height = photo.getHeight();

        double ratio = (double) screenWidth / width;

        int newWidth = screenWidth;
        int newHeight = (int) (height * ratio);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(newWidth, newHeight);


        switch (pic) {
            case 0:

                bitmap1 = photo;
                picture1.setImageBitmap(bitmap1);
                pic++;

                picture1.setLayoutParams(parms);

                break;
            case 1:
                bitmap2 = photo;
                picture2.setImageBitmap(bitmap2);

                pic++;
                picture2.setLayoutParams(parms);
                break;
            case 2:
                bitmap3 = photo;
                picture3.setImageBitmap(bitmap3);

                pic++;
                picture3.setLayoutParams(parms);
                break;
            case 3:
                bitmap4 = photo;
                picture4.setImageBitmap(bitmap4);

                pic++;
                picture4.setLayoutParams(parms);
                break;
            default:
                Toast.makeText(this, "Max pic Limit", Toast.LENGTH_LONG).show();
                break;

        }

    }

    private boolean canAccessLocation() {
        int result = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        // int result =
        // checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //Toast.makeText(parent.getContext(),parent.getItemAtPosition(pos).toString(),
        //Toast.LENGTH_LONG).show();
        //unit.setAdapter(null);
        ///*
        if (parent.getItemAtPosition(pos).toString().equals("DHAKA METRO")) {
            ArrayAdapter<CharSequence> adapterUnit_1 = ArrayAdapter.createFromResource(this, R.array.DHAKA_METRO, android.R.layout.simple_spinner_item);
            adapterUnit_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_1);
        } else if (parent.getItemAtPosition(pos).toString().equals("DHAKA OUTER")) {
            ArrayAdapter<CharSequence> adapterUnit_2 = ArrayAdapter.createFromResource(this, R.array.DHAKA_OUTER, android.R.layout.simple_spinner_item);
            adapterUnit_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_2);
        } else if (parent.getItemAtPosition(pos).toString().equals("CHITTAGONG")) {
            ArrayAdapter<CharSequence> adapterUnit_3 = ArrayAdapter.createFromResource(this, R.array.CHITTAGONG, android.R.layout.simple_spinner_item);
            adapterUnit_3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_3);
        } else if (parent.getItemAtPosition(pos).toString().equals("SYLHET")) {
            ArrayAdapter<CharSequence> adapterUnit_4 = ArrayAdapter.createFromResource(this, R.array.SYLHET, android.R.layout.simple_spinner_item);
            adapterUnit_4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_4);
        } else if (parent.getItemAtPosition(pos).toString().equals("BOGRA")) {
            ArrayAdapter<CharSequence> adapterUnit_5 = ArrayAdapter.createFromResource(this, R.array.BOGRA, android.R.layout.simple_spinner_item);
            adapterUnit_5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_5);
        } else if (parent.getItemAtPosition(pos).toString().equals("RAJSHAHI")) {
            ArrayAdapter<CharSequence> adapterUnit_6 = ArrayAdapter.createFromResource(this, R.array.RAJSHAHI, android.R.layout.simple_spinner_item);
            adapterUnit_6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_6);
        } else if (parent.getItemAtPosition(pos).toString().equals("KHULNA")) {
            ArrayAdapter<CharSequence> adapterUnit_7 = ArrayAdapter.createFromResource(this, R.array.KHULNA, android.R.layout.simple_spinner_item);
            adapterUnit_7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_7);
        } else if (parent.getItemAtPosition(pos).toString().equals("WATER")) {
            ArrayAdapter<CharSequence> adapterUnit_8 = ArrayAdapter.createFromResource(this, R.array.Water, android.R.layout.simple_spinner_item);
            adapterUnit_8.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unit.setAdapter(adapterUnit_8);
        }
        //*/

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    private Location getLastBestLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (statusOfGPS) {
            Location locationGPS;
            Location locationNet;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long GPSLocationTime = 0;
            if (null != locationGPS) {
                GPSLocationTime = locationGPS.getTime();
            }

            long NetLocationTime = 0;

            if (null != locationNet) {
                NetLocationTime = locationNet.getTime();
            }

            if (0 < GPSLocationTime - NetLocationTime) {
                return locationGPS;
            } else {
                return locationNet;
            }
        } else {
            Toast.makeText(this, "Please Turn On GPS", Toast.LENGTH_LONG).show();
            return null;
        }

    }


    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void StoragePermissionGrant() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return;
        }


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

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

            setDate(day + " " + monthArray[month] + " " + year);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            String am_pm;
            if (hourOfDay >= 12) {
                if (hourOfDay != 12)
                    hourOfDay -= 12;
                am_pm = "PM";
            } else {

                am_pm = "AM";
            }
            setTime(hourOfDay + ":" + minute + " " + am_pm);
        }
    }

    class SubmitAsync extends AsyncTask<String, String, String> {

        String id, title, date, description, action, gps, addrs, city, unit, img1, img2, img3, img4;
        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

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
            pDialog = new ProgressDialog(SafetyObservationFormActivity.this);
            pDialog.setMessage("Uploading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
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

                String result = jsonParser.makeHttpRequest(Config.SUBMIT_URL, "POST", params);

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
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            String dialogText;
            if (result != null && result.equals("Success!")) {
                dialogText = "Data Uploaded Successfully";
                // Toast.makeText(getApplicationContext(), "Success!",
                // Toast.LENGTH_LONG).show();
            } else {
                dialogText = "Upload unsuccessful";
                // Toast.makeText(getApplicationContext(), "Failure",
                // Toast.LENGTH_LONG).show();
            }
            final Dialog dialog = new Dialog(SafetyObservationFormActivity.this);
            dialog.setContentView(R.layout.dialog_layout);

            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.text);
            text.setText(dialogText);

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    finish();
                }
            });

            dialog.show();

        }

    }

    private class UploadToServer extends AsyncTask<Void, Integer, String> {

        String id, title, date, description, action, gps, addrs, city, unit;
        private ProgressDialog pDialog;

        UploadToServer(String id, String title, String date, String description, String action, String gps, String addrs, String city,
                       String unit) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.description = description;
            this.action = action;
            this.gps = gps;
            this.addrs = addrs;
            this.city = city;
            this.unit = unit;
        }


        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();

            pDialog = new ProgressDialog(SafetyObservationFormActivity.this);
            pDialog.setMessage("Uploading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            //progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            //progressBar.setProgress(progress[0]);

            // updating percentage value
            //txtPercentage.setText(String.valueOf(progress[0]) + "%");
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
                    entity.addPart("img1", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(getStringImage(bitmap1)) + ".jpg"));
                    //entity.addPart("image", new FileBody(sourceFile));
                }
                if (bitmap2 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img2", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(getStringImage(bitmap2)) + ".jpg"));

                }
                if (bitmap3 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap3.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img3", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(getStringImage(bitmap3)) + ".jpg"));

                }
                if (bitmap4 != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap4.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapBytes = baos.toByteArray();
                    entity.addPart("img4", new ByteArrayBody(bitmapBytes, "image/jpeg", md5(getStringImage(bitmap4)) + ".jpg"));

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
        protected void onPostExecute(final String result) {
            Log.e(TAG, "Response from server: " + result);
            pDialog.dismiss();
            // showing the server response in an alert dialog
            //showAlert(result);
            final String dialogText;
            if (result != null && result.equals("Success!")) {
                dialogText = "Data Uploaded Successfully";
                // Toast.makeText(getApplicationContext(), "Success!",
                // Toast.LENGTH_LONG).show();
            } else {
                dialogText = "Upload unsuccessful";
                // Toast.makeText(getApplicationContext(), "Failure",
                // Toast.LENGTH_LONG).show();
            }
            final Dialog dialog = new Dialog(SafetyObservationFormActivity.this);
            dialog.setContentView(R.layout.dialog_layout);

            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.text);
            text.setText(dialogText);

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (dialogText.equals("Data Uploaded Successfully")) {
                        finish();
                    }
                }
            });

            dialog.show();

            super.onPostExecute(result);

        }

    }

}
