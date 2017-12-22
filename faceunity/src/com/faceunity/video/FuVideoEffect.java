package com.faceunity.video;

import android.content.Context;
import android.util.Log;

import com.faceunity.auth.authpack;
import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;

/** 基于FU SDK的第三方滤镜
 * Created by hzzhujinbo on 2017/3/21.
 */

public class FuVideoEffect {

    private final String TAG = "FuVideoEffect";
    private static final String[] EFFECT_ITEM_FILE_NAME = {"none", "tiara.mp3", "item0208.mp3",
            "YellowEar.mp3", "PrincessCrown.mp3", "Mood.mp3", "Deer.mp3", "BeagleDog.mp3", "item0501.mp3",
            "ColorCrown.mp3", "item0210.mp3",  "HappyRabbi.mp3", "item0204.mp3", "hartshorn.mp3"};
    private final static String[] FILTERS_NAME = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};
    private String mFilterName = FILTERS_NAME[0];
    private String mEffectFileName = EFFECT_ITEM_FILE_NAME[1];
    private int mFacebeautyItem = 0;
    private int mEffectItem = 0;
    private int mFrameId;

    private float mFacebeautyColorLevel = 0.5f; // 美白 （0 - 1）
    private float mFacebeautyBlurLevel = 6.0f;  //磨皮 （1-6）
    private float mFacebeautyCheeckThin = 1.0f; // 瘦脸 (0 - 2)
    private float mFacebeautyEnlargeEye = 1.0f; //大眼 (0 -4 )
    private int mFaceShape = 0;
    private float mFaceShapeLevel = 0.5f;


    private Context mContext;
    private boolean mIsCreatedEGLContext = false;
    private boolean effectChange = false;


    public boolean filterInit(Context context) {
        Log.i(TAG,"filterInit");
        return fuInit(context);
    }

    public boolean filterInitWithoutGLContext(Context context) {
        Log.i(TAG,"filterInitWithoutGLContext");
        faceunity.fuCreateEGLContext();
        mIsCreatedEGLContext = true;
        return fuInit(context);
    }

    public void filterUnInit() {
        Log.i(TAG,"filterUnInit");
        fuUnInit();
    }

    public int ifilterNV21Image(byte[] data, int width, int height) {
        addEffectItem();
        int fuTex = faceunity.fuRenderToNV21Image(data,
                width, height, mFrameId++, new int[] {mEffectItem, mFacebeautyItem});
        return fuTex;
    }


    public int filterNV21AndTexture(byte[] data, int texture, int width, int height, boolean readBack) {
        addEffectItem();
//        boolean isOESTexture = true; //camera默认的是OES的
//        int flags = isOESTexture ? faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE : 0;
        int flags = faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;
        if(readBack){
            flags |= faceunity.FU_ADM_FLAG_ENABLE_READBACK;
        }
        int fuTex = faceunity.fuDualInputToTexture(data, texture, flags,
                width, height, mFrameId++, new
                        int[] {mEffectItem, mFacebeautyItem});
        return fuTex;
    }

    private boolean fuInit(Context context){
        boolean ret = true;
        try {
            InputStream is = context.getAssets().open("v3.mp3");
            byte[] v3data = new byte[is.available()];
            is.read(v3data);
            is.close();
            // 如果要使用faceu美颜，请联系faceu获取证书authpack，并放到com.netease.fulive目录下。
            // 可能需要替换authpack.A()的对象名，跟你的证书名一致
            faceunity.fuSetup(v3data, null, authpack.A());
//            faceunity.fuSetup(v3data, null, null);
            faceunity.fuSetMaxFaces(1);
            Log.i(TAG, "fuSetup");

            is = context.getAssets().open("face_beautification.mp3");
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            mFacebeautyItem = faceunity.fuCreateItemFromPackage(itemData);
            mFrameId = 0;
            mContext = context;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    private void fuUnInit(){
        faceunity.fuDestroyItem(mEffectItem);
        mEffectItem = 0;
        faceunity.fuDestroyItem(mFacebeautyItem);
        mFacebeautyItem = 0;
        faceunity.fuOnDeviceLost();
        if(mIsCreatedEGLContext){
            faceunity.fuReleaseEGLContext();
            mIsCreatedEGLContext = false;
        }
        mFrameId = 0;
        mContext = null;
    }


    private void addEffectItem(){
        if (mEffectItem == 0 || effectChange) {
            effectChange = false;

            try {
                if (mEffectFileName.equals("none")) {
                    mEffectItem = 0;
                } else {
                    InputStream is = mContext.getAssets().open(mEffectFileName);
                    byte[] itemData = new byte[is.available()];
                    is.read(itemData);
                    is.close();
                    mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        faceunity.fuItemSetParam(mFacebeautyItem, "color_level", mFacebeautyColorLevel);
        faceunity.fuItemSetParam(mFacebeautyItem, "blur_level", mFacebeautyBlurLevel);
        faceunity.fuItemSetParam(mFacebeautyItem, "filter_name", mFilterName);
        faceunity.fuItemSetParam(mFacebeautyItem, "cheek_thinning", mFacebeautyCheeckThin);
        faceunity.fuItemSetParam(mFacebeautyItem, "eye_enlarging", mFacebeautyEnlargeEye);
        faceunity.fuItemSetParam(mFacebeautyItem, "face_shape", mFaceShape);
        faceunity.fuItemSetParam(mFacebeautyItem, "face_shape_level", mFaceShapeLevel);
    }

    public void onEffectItemSelected(int position) {
        if (mEffectFileName == EFFECT_ITEM_FILE_NAME[position]) {
            return;
        }

        mEffectFileName = EFFECT_ITEM_FILE_NAME[position];
        effectChange = true;
    }
}
