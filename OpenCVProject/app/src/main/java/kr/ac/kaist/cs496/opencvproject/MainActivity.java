package kr.ac.kaist.cs496.opencvproject;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "OCVSample::Activity";

    String[] items = {"Bottom","Knit","Outer","Shirts","Top"};

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101;

    private ImageView imgMain;
    private Button btnCamera, btnAlbum, btnSearch;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private Uri photoUri;
    private String mCurrentPhotoPath;

    static TextView text2;
    static TextView yusado;
    static String category = "";

    static Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();

        //Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.g22);
        //Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.near);

        //Spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){


            @Override
            public void onNothingSelected(AdapterView<?> adapterView){
                category ="";
            }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                category = items[position].toLowerCase();
            }

        });


        //yusado = (TextView) findViewById(textView);
        //text2 = (TextView) findViewById(R.id.textView2);
        //ImageView img1 = (ImageView) findViewById(R.id.imageView);
        //ImageView img2 = (ImageView) findViewById(R.id.imageView2);

        //img1.setImageBitmap(bmp1);
        //img2.setImageBitmap(bmp2);

        //yusado.setText(Integer.toString(compareFeature(bmp1, bmp2)));

    }

    private void initView() {
        imgMain = (ImageView) findViewById(R.id.img_test);
        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnAlbum = (Button) findViewById(R.id.btn_album);
        btnSearch = (Button) findViewById(R.id.search);

        btnCamera.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                takePhoto();
                break;
            case R.id.btn_album:
                goToAlbum();
                break;
            case R.id.search:
                Intent searchIntent = new Intent (MainActivity.this, GridActivity.class);
                startActivity(searchIntent);
                break;
        }
    }


    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //사진을 찍기 위하여 설정합니다.
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();              finish();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(MainActivity.this, "kr.ac.kaist.cs496.opencvproject.provider", photoFile); //FileProvider의 경우 이전 포스트를 참고하세요.
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); //사진을 찍어 해당 Content uri를 photoUri에 적용시키기 위함
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    // Android M에서는 Uri.fromFile 함수를 사용하였으나 7.0부터는 이 함수를 사용할 시 FileUriExposedException이
    // 발생하므로 아래와 같이 함수를 작성합니다. 이전 포스트에 참고한 영문 사이트를 들어가시면 자세한 설명을 볼 수 있습니다.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "IP" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/test/"); //test라는 경로에 이미지를 저장하기 위함
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK); //ACTION_PICK 즉 사진을 고르겠다!
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            cropImage();
            // 갤러리에 나타나게
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else if (requestCode == CROP_FROM_CAMERA) {
            imgMain.setImageURI(null);
            imgMain.setImageURI(photoUri);
            try{
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }catch (IOException e){
                Toast.makeText(MainActivity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
            }
            //yusado.setText(Integer.toString(compareFeature(mBitmap, mBitmap)));

            //Toast.makeText(this, "적용되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

//모든 작업에 있어 사전에 FALG_GRANT_WRITE_URI_PERMISSION과 READ 퍼미션을 줘야 uri를 활용한 작업에 지장을 받지 않는다는 것이 핵심입니다.
    public void cropImage() {
        this.grantUriPermission("com.android.camera", photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {

            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            //intent.putExtra("aspectX", 4);
            //intent.putExtra("aspectY", 3);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/test/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(MainActivity.this,
                    "kr.ac.kaist.cs496.opencvproject.provider", tempFile);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);


        }

    }



    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }

    }

    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
