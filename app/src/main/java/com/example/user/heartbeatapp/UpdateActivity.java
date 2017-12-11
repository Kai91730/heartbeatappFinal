package com.example.user.heartbeatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class UpdateActivity extends AppCompatActivity {
    private ImageView ivSpot;
    private EditText etName;
    private EditText etWeb;
    private EditText etPhone;
    private EditText etAddress;
    private MySQLiteOpenHelper sqliteHelper;
    private Spot spot;
    private static final int REQUEST_TAKE_PICTURE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        if (sqliteHelper == null) {
            sqliteHelper = new MySQLiteOpenHelper(this);
        }
        findViews();
    }

    private void findViews() {
        ivSpot = (ImageView) findViewById(R.id.ivSpot);
        etName = (EditText) findViewById(R.id.etName);
        etWeb = (EditText) findViewById(R.id.etWeb);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);
        //依據前頁傳來的景點ID，呼叫MySQLiteOpenHelper.findById()將對應的景點資料取得
        int id = getIntent().getExtras().getInt("id");
        spot = sqliteHelper.findById(id);
        if (spot == null) {
            Toast.makeText(this, R.string.msg_NoDataFound,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //將取得的景點資訊顯示在各個UI元件上
        Bitmap bitmap = BitmapFactory.decodeByteArray(spot.getImage(), 0,
                spot.getImage().length);
        ivSpot.setImageBitmap(bitmap);
        etName.setText(spot.getName());
        etWeb.setText(spot.getWeb());
        etPhone.setText(spot.getPhone());
        etAddress.setText(spot.getAddress());
    }
    //以下onTakePictureClick()及其相關方法的功能同InsertActivity.java
    public void onTakePictureClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (isIntentAvailable(this, intent)) {
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        } else {
            Toast.makeText(this, R.string.msg_NoCameraAppsFound,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //當初請求的是拍照
                case REQUEST_TAKE_PICTURE:
                    //拍照完畢會將縮圖存在Bundle內，以減少占用記憶體空間
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                    if (bitmap != null) {
                        ivSpot.setImageBitmap(bitmap);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //轉成png不會失真，所以quality參數值100會被忽略
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        spot.setImage(baos.toByteArray());
                    }
                    break;
                default:
                    break;
            }
        }
    }
    //點擊Update按鈕擷取輸入資料並設定給spot物件
    public void onUpdateClick(View view) {
        //擷取輸入資料
        String name = etName.getText().toString();
        String web = etWeb.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();
        if (name.length() <= 0) {
            Toast.makeText(this, R.string.msg_NameIsInvalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //設定給spot物件
        spot.setName(name);
        spot.setWeb(web);
        spot.setPhone(phone);
        spot.setAddress(address);
        //呼叫MySQLiteOpenHelper.update()並將輸入資料轉成的Spot物件傳遞進去完成景點資料的修改
        int count = sqliteHelper.update(spot);
        Toast.makeText(this,getString(R.string.msg_RowUpdated),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onCancelClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sqliteHelper != null) {
            sqliteHelper.close();
        }
    }

    //主程式離開確認
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(UpdateActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("務必確認資料修改存取 確定要返回？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        return true;
    }
}
