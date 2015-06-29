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

    //�T�[�t�F�X�̃C���^�t�F�[�X�p�z���_�[
    SurfaceHolder mSurfaceHolder;

    //�J�����N���X
    Camera mCamera;

    //�R���e���c�u���o�C�_�i�f�[�^�A�N�Z�X�p�j
    ContentResolver contentResolver;

    public CameraPreview(Context context,SurfaceView sv,ContentResolver contentResolver)
    {
        super(context);
        mSurfaceHolder=sv.getHolder();
        mSurfaceHolder.addCallback(this);
        this.contentResolver=contentResolver;
    }

    /*
    �T�[�t�F�C�X�𐶐�����Ƃ��̏���
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            int openCameraType = Camera.CameraInfo.CAMERA_FACING_BACK;

            if(openCameraType <= Camera.getNumberOfCameras())
            {
                //�J���������������āA�v���r���[�f�B�X�v���C�ɐݒ肵�A�r���[��90�x��]������B
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
        �T�[�t�F�C�X��ύX����Ƃ��̏���
    */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setPreviewSize(width,height);
        mCamera.startPreview();
    }

    /*
    �v���r���[��ʂ̃T�C�Y��ݒ肷��B
     */
    protected void setPreviewSize(int width, int height)
    {
        //�J�����̃v���r���[���쐬�B
        Camera.Parameters params = mCamera.getParameters();

        //�v���r���[�T�C�Y���T�|�[�g���Ă���c���T�C�Y�ꗗ���擾�B
        List<Camera.Size> supported = params.getSupportedPreviewSizes();
        if(supported != null)
        {
            //�T�|�[�g���ꂽ�c���T�C�Y�ꗗ�̒�����ݒ�T�C�Y�����������Ă��ő�̃T�C�Y���J�����v���r���[�̃T�C�Y�ɂ���B
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
    �X�N���[���V���b�g���B�e����B
    */
    public void TakeShot()
    {
        mCamera.takePicture(null, null, mPicJpgListener);
    }

    /*
       �T�[�t�F�C�X��j������Ƃ��̏���
   */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Debug","surfaceDestroyed");

        //�J�����̃v���r���[���~���ĊJ������B
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

    }

    /*
    �J�����v���r���[�Ɏʂ������e���摜�Ƃ��ĕۑ�����R�[���o�b�N���`����B
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            //�摜��ۑ�����f�B���N�g���p�X��ݒ肷��B
            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/ScaryCamera";

            //�t�@�C���n���h�����쐬����B
            File file = new File(saveDir);

            //�f�B���N�g�������݂��Ȃ��ꍇ�͍쐬����B
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }

            //���ݎ����擾���A���ݎ�����t�@�C���̕ۑ��������߁A�ۑ���p�X��ݒ肷��B�i����+��+��+"_"+����+��+�b+�~���b�j
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmssSS");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            FileOutputStream fos;
            try {
                //�t�@�C���������o���B
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                //Android��̉摜�ꗗ�f�[�^�x�[�X���X�V����B
                registAndroidDB(imgPath);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }

            fos = null;
        }
    };

    /*
    Android��̉摜�ꗗ�f�[�^�x�[�X���X�V
     */
    private void registAndroidDB(String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
