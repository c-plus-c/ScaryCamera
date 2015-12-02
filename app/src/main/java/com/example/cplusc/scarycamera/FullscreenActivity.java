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

    //�J�����v���r���[
    private CameraPreview cameraPreview;

    //�T�E���h�v�[��
    private SoundPool mSoundPool;

    //��������ɓǂݍ��܂ꂽ�I�[�f�B�I��
    private int audioCounter;

    //�|�����̍Đ�ID
    private int screamSoundID;

    //�u�͂��`�[�Y�v�����̍Đ�ID
    private int poseSoundID;

    //���ѐ��Đ��ƕ|���摜�\���p�̃^�C�}�[
    private Timer screamTimer;

    //�A�ʗp�^�C�}�[
    private Timer shotTimer;

    //��{��ʂɖ߂邽�߂̃^�C�}�[
    private Timer returnTimer;

    //�ő�Ɉ�グ��O�̉��ʂ�ۑ�����ϐ�
    private int previousRingVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //UI��̃r���[���J�����v���r���[�ɐݒ肷��B
        SurfaceView surface = (SurfaceView) findViewById(R.id.surfaceView);
        surface = cameraPreview = new CameraPreview(this, surface, FullscreenActivity.this.getContentResolver());
        surface.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        //�{�^���̃��X�i�[�����̃N���X�ɐݒ肷��B
        Button button = (Button) findViewById(R.id.shot_button);
        button.setOnClickListener(this);

        //����ɓǂݍ��܂ꂽ�I�[�f�B�I�̐����W�v����B
        audioCounter = 0;

        //�T�E���h�v�[����ݒ肷��B
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        //�����t�@�C�����T�E���h�v�[���ɓǂݍ��܂ꂽ���̃R�[���o�b�N��ݒ�B
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            //�I�[�f�B�I����ǂݍ��܂ꂽ��Ăяo���֐��B
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                //�ǂݍ��܂ꂽ�I�[�f�B�I�����C���N�������g
                ++audioCounter;


                //�ǂݍ��܂ꂽ�I�[�f�B�I����2�ɒB������B�e�{�^����L����B
                if (audioCounter == 2) {
                    Button b1 = (Button) findViewById(R.id.shot_button);
                    b1.setEnabled(true);
                }
            }
        });

        //���ѐ��̉����t�@�C����ǂݍ��ށB
        screamSoundID = mSoundPool.load(getApplicationContext(), R.raw.scream, 0);

        //�u�͂��`�[�Y�v�̉����t�@�C����ǂݍ��ށB
        poseSoundID = mSoundPool.load(getApplicationContext(), R.raw.haichizu, 0);
    }

    @Override
    public void onClick(View v) {
        //�B�e�{�^���������ꂽ��
        if (v.getId() == R.id.shot_button) {
            //�B�e�{�^���𖳌���B
            Button b1 = (Button) findViewById(R.id.shot_button);
            b1.setEnabled(false);

            //�u�͂��`�[�Y�v���Đ��B
            mSoundPool.play(poseSoundID, 1.0f, 1.0f, 0, 0, 1.0f);

            //�e�^�C�}�[������
            screamTimer = new Timer(true);
            returnTimer = new Timer(true);
            shotTimer = new Timer(true);

            //�{�^��������3�b��Ɉȉ���TimerTask.run()�̈ȉ��̓��e�����s����B
            screamTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //�|���摜��\��������B
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ImageView imageView = (ImageView) findViewById(R.id.scaryImage);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.bringToFront();
                        }
                    });

                    //�I�[�f�B�I�}�l�[�W�����擾
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    // ���݂̉��ʂ��擾���āA�ۑ�����B
                    previousRingVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

                    // �I�[�f�B�I�}�l�[�W�����ݒ�\�ȍő剹�ʂ��擾����
                    int ringMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    // ���ʂ��ő剹�ʂɐݒ肷��
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, ringMaxVolume, AudioManager.FLAG_PLAY_SOUND);

                    //���ѐ����Đ�����B
                    mSoundPool.play(screamSoundID, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }, 3000);


            //�{�^��������3�b���1�b��5��A�ʂ���B�i4�b�ԑ�����B�j
            shotTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                        }

                    });

                    //�B�e
                    cameraPreview.TakeShot();
                }
            }, 3000, 200);

            //�{�^��������9�b��ɎB�e�{�^���̗L��A�|���摜�̔�\���A�I�[�f�B�I�}�l�[�W���̉��ʂ��f�t�H���g�ɖ߂��ăJ�����v���r���[���ĊJ����B�i��ԍŏ��̏�Ԃɖ߂�B�j
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

                    // ���ʂ��ő�Ɉ�グ��O�̂��̂ɐݒ肷��
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, previousRingVolume, AudioManager.FLAG_PLAY_SOUND);

                    //�A�ʗp�^�C�}�[���~
                    shotTimer.cancel();

                    //�J�����̃v���r���[���ĊJ
                    cameraPreview.mCamera.startPreview();
                }
            }, 9000);
        }
    }
}