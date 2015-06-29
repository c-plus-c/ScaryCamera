package com.example.cplusc.scarycamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    //サーフェスのインタフェース用ホルダー
    SurfaceHolder mSurfaceHolder;

    //カメラクラス
    Camera mCamera;

    //コンテンツブロバイダ（データアクセス用）
    ContentResolver contentResolver;

    public CameraPreview(Context context,SurfaceView sv,ContentResolver contentResolver)
    {
        super(context);
        mSurfaceHolder=sv.getHolder();
        mSurfaceHolder.addCallback(this);
        this.contentResolver=contentResolver;
    }

    /*
    サーフェイスを生成するときの処理
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            int openCameraType = Camera.CameraInfo.CAMERA_FACING_BACK;

            if(openCameraType <= Camera.getNumberOfCameras())
            {
                //カメラを初期化して、プレビューディスプレイに設定し、ビューを90度回転させる。
                mCamera = Camera.open(openCameraType);
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
            }
            else
            {
                Log.d("CameraSample", "cannot bind camera.");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
        サーフェイスを変更するときの処理
    */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setPreviewSize(width,height);
        mCamera.startPreview();
    }

    /*
    プレビュー画面のサイズを設定する。
     */
    protected void setPreviewSize(int width, int height)
    {
        //カメラのプレビューを作成。
        Camera.Parameters params = mCamera.getParameters();

        //プレビューサイズがサポートしている縦横サイズ一覧を取得。
        List<Camera.Size> supported = params.getSupportedPreviewSizes();
        if(supported != null)
        {
            //サポートされた縦横サイズ一覧の中から設定サイズよりも小さくてかつ最大のサイズをカメラプレビューのサイズにする。
            for(Camera.Size size : supported)
            {
                if(size.width <= width && size.height <= height)
                {
                    params.setPreviewSize(size.width,size.height);
                    mCamera.setParameters(params);
                    break;
                }
            }
        }
    }

    /*
    スクリーンショットを撮影する。
    */
    public void TakeShot()
    {
        mCamera.takePicture(null, null, mPicJpgListener);
    }

    /*
       サーフェイスを破棄するときの処理
   */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Debug","surfaceDestroyed");

        //カメラのプレビューを停止して開放する。
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

    }

    /*
    カメラプレビューに写った内容を画像として保存するコールバックを定義する。
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            //画像を保存するディレクトリパスを設定する。
            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/ScaryCamera";

            //ファイルハンドラを作成する。
            File file = new File(saveDir);

            //ディレクトリが存在しない場合は作成する。
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }

            //現在時を取得し、現在時からファイルの保存名を決め、保存先パスを設定する。（西暦+月+日+"_"+時間+分+秒+ミリ秒）
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmssSS");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            FileOutputStream fos;
            try {
                //ファイルを書き出す。
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                //Android上の画像一覧データベースを更新する。
                registAndroidDB(imgPath);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }

            fos = null;
        }
    };

    /*
    Android上の画像一覧データベースを更新
     */
    private void registAndroidDB(String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
