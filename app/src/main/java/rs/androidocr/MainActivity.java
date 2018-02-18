package rs.androidocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton add, camera, gallery;
    Button button, openContacts;
    ImageView imageView;
    boolean isOpen = false;
    Animation fabOpen, fabClose, fabClockwise, fabAnticlockwise;
    static int flag = 0;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;
    TextView displayName, displayPhone, displayEMail;
    Bitmap bitmap;
    private TessBaseAPI mTess;
    String datapath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = (FloatingActionButton) findViewById(R.id.camera);
        add = (FloatingActionButton) findViewById(R.id.add);
        gallery = (FloatingActionButton) findViewById(R.id.gallery);
        button = (Button) findViewById(R.id.button);
        openContacts = (Button) findViewById(R.id.contacts);
        imageView = (ImageView) findViewById(R.id.imageView);
        displayName = (TextView) findViewById(R.id.textView);
        displayPhone = (TextView) findViewById(R.id.textView2);
        displayEMail = (TextView) findViewById(R.id.textView3);

        fabOpen = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_close);
        fabClockwise = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_clockwise);
        fabAnticlockwise = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_anticlockwise);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image3);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            camera.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        String language = "eng";
        datapath = getFilesDir() + "/tessaract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));
        mTess.init(datapath, language);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage();
            }
        });

        openContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToContacts();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOpen) {
                    add.startAnimation(fabAnticlockwise);
                    camera.startAnimation(fabClose);
                    gallery.startAnimation(fabClose);
                    camera.setEnabled(false);
                    gallery.setEnabled(false);
                    isOpen = false;
                } else {
                    add.startAnimation(fabClockwise);
                    camera.startAnimation(fabOpen);
                    gallery.startAnimation(fabOpen);
                    gallery.setEnabled(true);
                    camera.setEnabled(true);
                    isOpen = true;
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                flag = 0;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);

            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });


    }

    public void processImage() {
        String OCRresult;
        mTess.setImage(bitmap);
        OCRresult = mTess.getUTF8Text();
        extractName(OCRresult);
        extractEmail(OCRresult);
        extractPhone(OCRresult);
    }

    public void extractName(String str) {
        System.out.println("Getting the Name");
        final String NAME_REGEX = "^([A-Z]([a-z]*|\\.) *){1,2}([A-Z][a-z]+-?)+$";
        Pattern p = Pattern.compile(NAME_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            System.out.println(m.group());
            displayName.setText(m.group());
        }
    }

    public void extractEmail(String str) {
        System.out.println("Getting the Email");
        final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern p = Pattern.compile(EMAIL_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            System.out.println(m.group());
            displayEMail.setText(m.group());
        }
    }

    public void extractPhone(String str) {
        System.out.println("Getting the Phone Number");
        final String PHONE_REGEX = "(?:^|\\D)(\\d{3})[)\\-. ]*?(\\d{3})[\\-. ]*?(\\d{4})(?:$|\\D)";
        Pattern p = Pattern.compile(PHONE_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            System.out.println(m.group());
            displayPhone.setText(m.group());
        }
    }

    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToContacts() {


        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        if (displayName.getText().length() > 0 && (displayPhone.getText().length() > 0 || displayEMail.getText().length() > 0))
        {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName.getText());
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, displayEMail.getText());
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, displayPhone.getText());
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
            startActivity(intent);
        } else
        {
            Toast.makeText(getApplicationContext(), "No information to add to contacts!", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CAMERA_REQUEST) {

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bmp);
            } else if (requestCode == GALLERY_REQUEST) {

                Uri selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
            }

        }
    }
}

