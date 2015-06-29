package com.example.cplusc.scarycamera;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;


public class FullscreenActivity extends Activity implements View.OnClickListener {

    //カメラプレビュー
    private CameraPreview cameraPreview;

    //サウンドプール
    private SoundPool mSoundPool;

    //メモリ上に読み込まれたオーディオ数
    private int audioCounter;

    //怖い声の再生ID
    private int screamSoundID;

    //「はいチーズ」音声の再生ID
    private int poseSoundID;

    //叫び声再生と怖い画像表示用のタイマー
    private Timer screamTimer;

    //連写用タイマー
    private Timer shotTimer;

    //基本画面に戻るためのタイマー
    private Timer returnTimer;

    //最大に引き上げる前の音量を保存する変数
    private int previousRingVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //UI上のビューをカメラプレビューに設定する。
        SurfaceView surface = (SurfaceView) findViewById(R.id.surfaceView);
        surface = cameraPreview = new CameraPreview(this, surface, FullscreenActivity.this.getContentResolver());
        surface.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        //ボタンのリスナーをこのクラスに設定する。
        Button button = (Button) findViewById(R.id.shot_button);
        button.setOnClickListener(this);

        //正常に読み込まれたオーディオの数を集計する。
        audioCounter = 0;

        //サウンドプールを設定する。
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        //音声ファイルがサウンドプールに読み込まれた時のコールバックを設定。
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            //オーディオが一つ読み込まれたら呼び出す関数。
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                //読み込まれたオーディオ数をインクリメント
                ++audioCounter;


                //読み込まれたオーディオ数が2に達したら撮影ボタンを有効化する。
                if (audioCounter == 2) {
                    Button b1 = (Button) findViewById(R.id.shot_button);
                    b1.setEnabled(true);
                }
            }
        });

        //叫び声の音声ファイルを読み込む。
        screamSoundID = mSoundPool.load(getApplicationContext(), R.raw.scream, 0);

        //「はいチーズ」の音声ファイルを読み込む。
        poseSoundID = mSoundPool.load(getApplicationContext(), R.raw.haichizu, 0);
    }

    @Override
    public void onClick(View v) {
        //撮影ボタンが押された時
        if (v.getId() == R.id.shot_button) {
            //撮影ボタンを無効化する。
            Button b1 = (Button) findViewById(R.id.shot_button);
            b1.setEnabled(false);

            //「はいチーズ」を再生。
            mSoundPool.play(poseSoundID, 1.0f, 1.0f, 0, 0, 1.0f);

            //各タイマーを初期化
            screamTimer = new Timer(true);
            returnTimer = new Timer(true);
            shotTimer = new Timer(true);

            //ボタン押下の3秒後に以下のTimerTask.run()の以下の内容を実行する。
            screamTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //怖い画像を表示させる。
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ImageView imageView = (ImageView) findViewById(R.id.scaryImage);
                            imageView.setVisibility(View.VISIBLE);
                        }
                    });

                    //オーディオマネージャを取得
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    // 現在の音量を取得して、保存する。
                    previousRingVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

                    // オーディオマネージャが設定可能な最大音量を取得する
                    int ringMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    // 音量を最大音量に設定する
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, ringMaxVolume, AudioManager.FLAG_PLAY_SOUND);

                    //叫び声を再生する。
                    mSoundPool.play(screamSoundID, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }, 3000);


            //ボタン押下の3秒後に1秒に5回連写する。（4秒間続ける。）
            shotTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                        }

                    });

                    //撮影
                    cameraPreview.TakeShot();
                }
            }, 3000, 200);

            //ボタン押下の9秒後に撮影ボタンの有効化、怖い画像の非表示、オーディオマネージャの音量をデフォルトに戻してカメラプレビューを再開する。（一番最初の状態に戻る。）
            returnTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            ImageView imageView = (ImageView) findViewById(R.id.scaryImage);
                            imageView.setVisibility(View.INVISIBLE);

                            Button b1 = (Button) findViewById(R.id.shot_button);
                            b1.setEnabled(true);
                        }
                    });

                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    // 音量を最大に引き上げる前のものに設定する
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, previousRingVolume, AudioManager.FLAG_PLAY_SOUND);

                    //連写用タイマーを停止
                    shotTimer.cancel();

                    //カメラのプレビューを再開
                    cameraPreview.mCamera.startPreview();
                }
            }, 9000);
        }
    }
}