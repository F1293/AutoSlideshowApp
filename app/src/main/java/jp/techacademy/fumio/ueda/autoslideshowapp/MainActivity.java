package jp.techacademy.fumio.ueda.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
   // Timer mTimer;
    TextView mTimerText;

    // タイマー用の時間のための変数
    double mTimerSec = 0.0;

    int kirikae = 1;

    Handler mHandler = new Handler();

    Button mStartButton;
    Button mNextButton;
    Button mPreviousButton;
    Timer mTimer;
    Cursor mCursor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartButton = (Button) findViewById(R.id.start_button);
        mNextButton = (Button) findViewById(R.id.next_button);
        mPreviousButton = (Button) findViewById(R.id.previous_button);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                SlideContents();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            SlideContents();
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SlideContents();
                } else {
                    //パーミッション許可されなかったときエラー表示して閉じる
                    Toast.makeText(this, "アプリを終了しました。\nアプリの権限を変更してください。", Toast.LENGTH_LONG).show();
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

    public void SlideContents() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {
            //画像があるとき
            // do {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            setImageView(mCursor);
            //  }
            // while (mCursor.moveToNext()) ;




            //スタートが押されたとき
            mStartButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    switch (kirikae) {
                        case 1:
                        mStartButton.setText("停止");
                            mNextButton.setEnabled(false);
                            mPreviousButton.setEnabled(false);
                        kirikae = 2;

                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCursor.moveToNext()) {
                                            setImageView(mCursor);
                                        } else {
                                            //次の画像がないときは初めに戻る
                                            mCursor.moveToFirst();
                                            setImageView(mCursor);
                                        }
                                        Log.d("Test", "OK");
                                        // ここにタイマー処理を記述する
                                    }
                                });
                            }
                        }, 2000, 2000);
                            break;
                        case  2:
                            mStartButton.setText("START");
                            mNextButton.setEnabled(true);
                            mPreviousButton.setEnabled(true);
                            mTimer.cancel();
                            kirikae = 1;
                            break;
                    }


                }
            });
            //NEXTボタンが押されたとき
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if (mCursor.moveToNext()) {
                            // mCursor.moveToNext();
                            setImageView(mCursor);
                        } else {
                            //次の画像がないときは初めに戻る
                            mCursor.moveToFirst();
                            setImageView(mCursor);
                        }
                    }

            });
            //PREVIOUSボタンが押されたとき
            mPreviousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCursor.moveToPrevious()) {
                        setImageView(mCursor);
                    } else {
                        //前の画像がないときは最後に
                        mCursor.moveToLast();
                        setImageView(mCursor);
                    }
                }
            });
        } else {
            //画像が１つも取得できなかった場合
            Toast.makeText(this, "表示できる画像がありません。", Toast.LENGTH_LONG).show();
            mCursor.close();
        }
    }

    private void setImageView(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        //取得した画像を表示する
        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}