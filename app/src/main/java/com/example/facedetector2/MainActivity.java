package com.example.facedetector2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private MyImageView mIV;
    private Bitmap mFaceBitmap;
    private int mFaceWidth = 200;
    private int mFaceHeight = 200;
    private static final int MAX_FACES = 10;
    private static String TAG = "TutorialOnFaceDetect";
    private static boolean DEBUG = false;

    protected static final int GUIUPDATE_SETFACE = 999;
    protected Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
        mIV.invalidate();
        super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIV = new MyImageView(this);
        setContentView(mIV, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.faces);
        mFaceBitmap = b.copy(Bitmap.Config.RGB_565,true);
        b.recycle();
        
        mFaceWidth = mFaceBitmap.getWidth();
        mFaceHeight = mFaceBitmap.getHeight();
        mIV.setImageBitmap(mFaceBitmap);
        mIV.invalidate();
        doLengthyCalc();//
    }

    public  void setFace(){
        FaceDetector fd;
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        PointF eyescenter = new PointF();
        float eyesdist = 0.0f;
        int[] fpx = null;
        int[] fpy = null;
        int count = 0;

        try {
            fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);
            count = fd.findFaces(mFaceBitmap, faces);
        }catch (Exception e){
            Log.e(TAG, "setFace(): " + e.toString());
            return;
        }
        if (count > 0) {
            fpx = new int[count * 2];
            fpy = new int[count * 2];

            for(int i = 0; i < count; i++){
                try{
                    faces[i].getMidPoint(eyescenter);
                    eyesdist = faces[i].eyesDistance();
                    fpx[2 * i] = (int) (eyescenter.x -eyesdist / 2);
                    fpy[2 * i] = (int) eyescenter.y;
                    if(DEBUG)
                        Log.e(TAG, "setFace(): face " + i +
                                ": confidence = " + faces[i].confidence() + ", eyes distance = "
                                + faces[i].eyesDistance() +  ", pose = (" + faces[i].pose(FaceDetector.Face.EULER_X)
                                + "," + faces[i].pose(FaceDetector.Face.EULER_Y) + ","
                                + faces[i].pose(FaceDetector.Face.EULER_Z) + ")" + ", eyes midpoint = (" + eyescenter.x
                                + "," + eyescenter.y + ")");
                }catch (Exception e){
                    Log.e(TAG, "setFace():face " + i + e.toString());
                }
            }
        }
        mIV.setmDisplayPoints(fpx, fpy, count * 2, 1);
    }

    private void doLengthyCalc() {
        Thread t = new Thread(){
            Message m = new Message();

            public void run(){
                try{
                    setFace();
                    m.what = MainActivity.GUIUPDATE_SETFACE;
                    MainActivity.this.mHandler.sendMessage(m);
                }catch (Exception e){
                    Log.e(TAG, "doLengthyCalc(): " + e.toString());
                }
            }
        };
        t.start();
    }

}
