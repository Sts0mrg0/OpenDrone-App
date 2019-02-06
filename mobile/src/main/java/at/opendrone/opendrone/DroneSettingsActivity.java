/*
 * Last modified: 09.09.18 18:56
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class DroneSettingsActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE = 69;
    private static final int CAMERA_CODE = 88;
    private static final int GALLERY_CODE = 18;
    private static final int RESULT_CODE = 3;
    private static final int MODE_NEW = 7;
    private static final int MODE_EDIT = 27;
    private ConstraintLayout parent;
    private TextView txtView_Calibration;
    private TextView txtView_PinConfiguration;
    private TextView txtView_DroneName;
    private TextView txtView_DroneDescription;
    private EditText txt_DroneDescription;
    private EditText txt_DroneName;
    private Spinner spinner_DroneTyp;
    private ImageView dronePicture;
    private AppCompatImageView cameraOverlay;
    private int position;
    private boolean fromIntent = false;

    private Uri imgUri;
    private String picturePath;

    private Drone selectedDrone;
    private String mode = "";

    private SharedPreferences sp;

    public void setDrone(Drone drone) {
        this.selectedDrone = drone;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RESULT_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length < 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DroneSettings", "permissions not granted");
                }
                return;
            }
            default:
                return;
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    RESULT_CODE);
            // Permission is not granted
        }
    }

    private void takePhoto(View view) {
        showInfo();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case CAMERA_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("picky", "camera");
                    cameraToFile();
                    //setUri(imageReturnedIntent);
                }

                break;
            case GALLERY_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("picky", "gallery");
                    galleryToFile(imageReturnedIntent);
                }
                break;
            default:
                break;
        }
    }

    private void cameraToFile() {
        imgUri = Uri.fromFile(new File(picturePath));
        Log.i("Picky", picturePath);
        Picasso.get().load(imgUri).into(dronePicture);

    }

    private void galleryToFile(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        picturePath = cursor.getString(columnIndex);
        Log.i("Picky", "test: " + picturePath);
        //sp.edit().putString("imagepath",picturePath);
        cursor.close();

        imgUri = Uri.fromFile(new File(picturePath));
        Picasso.get().load(imgUri).into(dronePicture);
    }

    private void showInfo() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.alertDialog_Picture_Message)
                .setTitle(R.string.alertDialog_Picture_Title);

        builder.setPositiveButton(getResources().getString(R.string.alertDialog_Picture_Positive), (dialogInterface, i) -> openCameraIntent());
        builder.setNegativeButton(getResources().getString(R.string.alertDialog_Picture_Negative), (dialogInterface, i) -> openGalleryIntent());
// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_CODE);//one can be replaced with any action code
    }

    private void openCameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        picturePath = getExternalMediaDirs()[0] + "/" + System.currentTimeMillis() + ".jpg";
        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".my.package.name.provider", new File(picturePath));
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(takePicture, CAMERA_CODE);
    }

    public void setValuesForDrone(Drone drone) {
        txt_DroneDescription.setText(drone.description);
        txt_DroneName.setText(drone.name);

        String[] values = getResources().getStringArray(R.array.array_DroneTyp);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(drone.type)) {
                spinner_DroneTyp.setSelection(i);
                break;
            }
        }
    }

    private void addTextWatcher(EditText src, final TextView target) {
        src.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                target.setText(editable.toString());
            }
        });
    }

    public void startCalibration() {

        Intent i = new Intent(getApplicationContext(), DroneCalibrationActivity.class);
        startActivity(i);
    }

    private void startPinConfig() {
        Intent i = new Intent(getApplicationContext(), PinConfigurationActivity.class);
        startActivity(i);

    }

    private void initViews() {
        dronePicture = findViewById(R.id.imageView_DronePicture);
        cameraOverlay = findViewById(R.id.imgView_CameraOverlay);
        parent = findViewById(R.id.parent);

        cameraOverlay.setOnClickListener(this::takePhoto);
        initButtons();
    }

    private void initImgView() {
        dronePicture = findViewById(R.id.imageView_DronePicture);
        Log.i("Loady123","DroneImg" + position);
        String uriStr = sp.getString("DroneImg" + position, "");
        if (!uriStr.equals("")) {
            Uri imgUri = Uri.parse(uriStr);
            Picasso.get().load(imgUri).into(dronePicture);
        }
    }

    public void initButtons() {
        txtView_Calibration = findViewById(R.id.txtView_Calibration);
        txtView_PinConfiguration = findViewById(R.id.txtView_PinConfiguration);
        txt_DroneName = findViewById(R.id.txt_DroneName);

        txtView_DroneName = findViewById(R.id.txtView_DroneName);
        txtView_DroneDescription = findViewById(R.id.txtView_DroneDescription);

        spinner_DroneTyp = findViewById(R.id.spinner_DroneTyp);
        txt_DroneDescription = findViewById(R.id.txt_Drone_description);

        addTextWatcher(txt_DroneName, txtView_DroneName);
        addTextWatcher(txt_DroneDescription, txtView_DroneDescription);

        txtView_Calibration.setOnClickListener(v -> startCalibration());

        txtView_PinConfiguration.setOnClickListener(v -> startPinConfig());

        switch (this.mode) {
            case ("edit"): {
                editDrone(MODE_EDIT);
                break;
            }
            case ("new"): {
                editDrone(MODE_NEW);
                break;
            }
            default:
                break;
        }

    }


    private Drone readData() {
        String droneName = String.valueOf(txt_DroneName.getText());
        String droneDescription = String.valueOf(txt_DroneDescription.getText());
        String droneType = String.valueOf(spinner_DroneTyp.getSelectedItem().toString());

        return new Drone(droneName, droneDescription, droneType);
    }


    private boolean isNullOrEmpty(Object object) {
        return object == null || object.toString().equals("");
    }

    private void editDrone(final int mode) {
        Button btn_SaveSettings = findViewById(R.id.btn_saveChanges);
        btn_SaveSettings.setOnClickListener(v -> handleEditDrone(mode));
    }

    private void handleEditDrone(int mode) {
        if (isNullOrEmpty(txt_DroneName.getText())) {
            txt_DroneName.setError(getResources().getString(R.string.editTxt_DroneName_Error));
            return;
        }
        Drone drone = readData();
        checkMode(mode, drone);
        saveInSP();
        finish();
    }

    private void checkMode(int mode, Drone drone) {
        switch (mode) {
            case MODE_EDIT:
                DroneCardListRecyclerFragment.drones.set(position, drone);
                break;
            case MODE_NEW:
                DroneCardListRecyclerFragment.drones.add(drone);
                break;
            default:
                break;
        }
    }

    private void saveInSP() {
        Log.i("picky", "SaveInSP()");
        Gson gson = new Gson();
        String serialized = gson.toJson(DroneCardListRecyclerFragment.drones.toArray());
        sp.edit().putString("DroneList", serialized).apply();
        if (imgUri != null && !imgUri.toString().equals("")) {
            Log.i("Loady123", "DroneImg" + position);
            sp.edit().putString("DroneImg" + position, picturePath).apply();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onResume() {

        int position = sp.getInt("CurrentDronePosition", -1);
        if (position != -1 && !fromIntent) {
            initFromIntent(position);
        }
        fromIntent = false;
        initViews();
        checkPermission();

        AndroidUtils.hideKeyboard(txt_DroneName, this);
        AndroidUtils.hideKeyboard(txt_DroneDescription, this);

        if (selectedDrone != null) {
            setValuesForDrone(this.selectedDrone);
        }

        super.onResume();
    }

    private void initFromIntent(int position) {
        String mode = sp.getString("CurrentDroneMode", "");
        String serializedDrone = sp.getString("CurrentDrone", "");

        this.mode = mode;
        Gson gson = new Gson();
        selectedDrone = gson.fromJson(serializedDrone, Drone.class);
        this.position = position;

        sp.edit().remove("CurrentDronePosition").apply();
        sp.edit().remove("CurrentDroneMode").apply();
        sp.edit().remove("CurrentDrone").apply();
    }

    @Override
    protected void onPause() {
        Gson gson = new Gson();
        String serializedDrone = gson.toJson(selectedDrone);
        sp.edit().putInt("CurrentDronePosition", position).apply();
        sp.edit().putString("CurrentDrone", serializedDrone).apply();
        sp.edit().putString("CurrentDroneMode", mode).apply();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_settings);
        //addTouchListeners(findViewById(R.id.ConstraintLayout));

        Intent i = getIntent();
        sp = getSharedPreferences("at.opendrone.opendrone", MODE_PRIVATE);
        fromIntent = false;
        if (i != null && i.getExtras() != null) {
            fromIntent = true;
            String mode = i.getStringExtra("Mode");
            setMode(mode);
            Drone drone = (Drone) i.getSerializableExtra("Drone");
            setDrone(drone);
            int position = i.getIntExtra("Position", -1);
            setPosition(position);
            dronePicture = findViewById(R.id.imageView_DronePicture);
            String uriStr = i.getStringExtra("Img");
            if (uriStr != null && !uriStr.equals("")) {
                Uri uri = Uri.fromFile(new File(uriStr));
                Picasso.get().load(uri).into(dronePicture);
            }
            //initImgView();
            //String defFragment.setMode("edit");
        }


    }
}
