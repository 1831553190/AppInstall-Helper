package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final int INSTALL_PACKAGES_REQUESTCODE = 100;
    private static final int GET_UNKNOWN_APP_SOURCES = 3;
    private static final String TAG = "MainActivity";

    File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        TextView txv=findViewById(R.id.filePath);
        Intent i=getIntent();
        Uri d=i.getData();
        if (d!=null&&d.getPath()!=null){
            System.out.println(getRealPath(d));
            if (d.getScheme().equals("file")){
                file = new File(d.getPath());
            }else {
                try {

                    file=new File(getRealPath(d));
                }catch (Exception e){
                    Toast.makeText(this,"路径获取失败！",Toast.LENGTH_SHORT).show();
                }
            }
            checkIsAndroidO();

        }
    }

    private String getRealPath(Uri d) {
        if (DocumentsContract.isDocumentUri(this,d)){
            String p;
            try{
                p = FileUtils.getFilePathByUri(this,d);
            }catch (Exception e){
               p=null;
            }
            if (p==null){
                String[] pt=d.getPath().split(":");
                return pt[1];
            }
        }else{
            try {
                return FileUtils.getFilePathByUri(this,d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return FileUtils.getFilePathByUri(this,d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPath(Uri uri) {
            ContentResolver contentResolver=getContentResolver();
            String filePath;
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};

            Cursor cursor = contentResolver.query(uri, filePathColumn, null, null, null);
//	    也可用下面的方法拿到cursor
//	    Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;

    }
    private void checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                installApk();//安装应用的逻辑(写自己的就可以)
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
            }
        } else {
            installApk();
        }

    }

    private void installApk() {
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        if (Build.VERSION.SDK_INT>=24){
            uri = FileProvider.getUriForFile(getApplicationContext(),getPackageName()+".fileProvider",file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            uri=Uri.fromFile(file);
        }
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGES_REQUESTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    installApk();
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_UNKNOWN_APP_SOURCES:
                checkIsAndroidO();
                break;

            default:
                break;
        }
    }

}
