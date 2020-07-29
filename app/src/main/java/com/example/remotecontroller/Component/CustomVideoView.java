package com.example.remotecontroller.Component;

import android.icu.text.Collator;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.remotecontroller.ExtraTools;
import com.example.remotecontroller.R;
import com.example.remotecontroller.Resource;

import java.util.Timer;
import java.util.TimerTask;

public class CustomVideoView implements MediaPlayer.OnCompletionListener {


    private String TAG = "Video view";
    private String url;
    private VideoView videoView;
    private ImageView imageView;

    private int currentSession= 0;
    private int currnetPosition=0;

    private boolean isSingle =false;
    private boolean isLoop =false;
    private boolean isVideoReady =false;
    private AlexaFinishCallback alexaFinishCallback;


    private int [] currentCheckPoint;
    private int [] s2CheckPoint= new int []{-1,4000,-1};
    private int [] s3CheckPoint =new int []{5700,-1,-1};
    private int [] s4CheckPoint=new int []{-1,-1,-1,-1,-1,-1};
    private int [] s5CheckPoint =new int []{-1,-1,-1};
    private int [] checkPoint  =new int []{-1,2000,3000,4000,5000};
    private int currentCheckPointIndex =0 ;
    private LottieAnimationView lottieAnimationView;
    public CustomVideoView (VideoView videoView, ImageView imageView,AlexaFinishCallback alexaFinishCallback)
    {

        this.videoView=videoView;
        this.imageView=imageView;
        this.alexaFinishCallback=alexaFinishCallback;

        videoView.setOnCompletionListener(this);
        detectorVideoFrame();
    }

    public void setUrl(String url)
    {
        this.url=url;

        videoView.setVideoURI(Uri.parse(url));
    }
    public void playVideo ()
    {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                videoView.start();
                isVideoReady =true;
            }
        });

    }
    public void s5PauseVideo ()
    {
        videoView.pause();
    }
    public void s5PlayVideo(){

        videoView.start();

    }
    public void nextClick ()
    {
        Log.i(TAG,"nextClick");
        if (currentSession ==ExtraTools.S1&& currentCheckPointIndex ==0)
        {
            lottieAnimationView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(Resource.s5_2VideoPath));
            Log.e(TAG,"HHHHHHHHHHHHHHHHHHH");
            videoView.start();
            currentCheckPointIndex++;


        }
        else if (currentSession ==ExtraTools.S2  && currentCheckPointIndex ==0)
        {
            videoView.setVideoURI(Uri.parse(Resource.s2_2VideoPath));
            currentCheckPointIndex++;
            isLoop=true;
        }
        else if (currentSession==ExtraTools.S4)
        {
            Log.e(TAG,"currentCheckPointIndex  "+currentCheckPointIndex);
            currentCheckPointIndex++;
            currentCheckPointIndex%=4;
            imageView.setVisibility(View.VISIBLE);
            videoView.pause();
            imageView.setImageResource(Resource.s4ImagePath[currentCheckPointIndex * 2 + (isSingle ? 0 : 1)]);

        }
        else if (currentSession != ExtraTools.S4)
        {
            videoView.start();
            currentCheckPointIndex++;
            currentCheckPointIndex%=checkPoint.length;
        }
//        if (currentSession==ExtraTools.S5 && currentCheckPointIndex ==0)
//        {
//            lottieAnimationView.setVisibility(View.INVISIBLE);
//            imageView.setVisibility(View.INVISIBLE);
//            videoView.setVideoURI(Uri.parse(Resource.s5_2VideoPath));
//
//        }





    }

    public void changeSession (int session)
    {
        /*
            change video and image source by session

            argv :
                session : index from session
        */

        Log.i(TAG,"Change Session");
        currentSession=session;
        currentCheckPointIndex= 0;
        String videoReource="";
        imageView.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.INVISIBLE);
        isVideoReady =false;
        switch (session)
        {
            case ExtraTools.S1:
                videoView.pause();
                videoView.setVisibility(View.INVISIBLE);
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();
                checkPoint=s5CheckPoint;
                imageView.setImageResource(Resource.homepageImageId);
                imageView.setVisibility(View.VISIBLE);
                //videoReource=Resource.s1VideoPath;
                isLoop=true;
                break;
            case ExtraTools.S2:
                videoView.setVisibility(View.VISIBLE);
                videoReource=Resource.s2VideoPath;
                checkPoint =s2CheckPoint;
                isLoop=true;
                break;
            case ExtraTools.S3:
                imageView.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoReource=Resource.s3VideoPath;
                checkPoint =s3CheckPoint;
                isLoop =false;
                break;
            case ExtraTools.S4:
                videoView.setVisibility(View.VISIBLE);
                videoReource=Resource.s4VideoPath;
                checkPoint =s4CheckPoint;
                currentCheckPointIndex=-1;
                isLoop=true;
                break;
//            case ExtraTools.S5:
//                videoView.setVisibility(View.VISIBLE);
//                checkPoint=s5CheckPoint;
//                videoReource=Resource.s5VideoPath;
//
//                lottieAnimationView.setVisibility(View.VISIBLE);
//                lottieAnimationView.playAnimation();
//                imageView.setImageResource(Resource.homepageImageId);
//                imageView.setVisibility(View.VISIBLE);
//                isLoop=true;
//
//                break;
            default:
                break;

        }
        if (!videoReource.equals(""))
        {
            setUrl(videoReource);
            playVideo();
        }

    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        /*
            check video is finish and loop or not
        */
        Log.e(TAG,"Video finish" +currentSession);
        if (isLoop)
        {
            //videoView.seekTo(0);
            videoView.start();
            if (currentSession==ExtraTools.S2)
            {
                Log.i(TAG,"Video finish");
                videoView.seekTo(6000);

            }
            else  currentCheckPointIndex = 0;
        }
        else if ((currentSession==ExtraTools.S2 || currentSession ==ExtraTools.S4)   && currentCheckPointIndex ==0)
        {
            alexaFinishCallback.onCompletion();

        }

    }

    private void detectorVideoFrame ()
    {
        /*
            make a timer to detect frame position

        */
        Timer clockTimer =new Timer(true);
        clockTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (isVideoReady ) {
                    int position = videoView.getCurrentPosition();
                   // Log.i(TAG,"position"+position);
                    if(currentCheckPointIndex != -1) {
                        if (checkPoint[currentCheckPointIndex] != -1 && position > checkPoint[currentCheckPointIndex]) {
                            Log.e(TAG,"Video pause");
                            videoView.pause();
                        }
                    }
                }
//                Message msg = handler.obtainMessage();
//                msg.what = TIME_UPDATE_MESSAGE;
//                msg.obj=ExtraTools.getCurrentTime();
//                msg.sendToTarget();
                //Log.i(TAG,"Video position "+videoView.getCurrentPosition());

            }
        },0,100);
    }
    public void setIsSingle (boolean isSingle)
    {
        /*
           set mode  single or multi

           argv:
            isSingle : single or multi
         */
        this.isSingle=isSingle;
        if (currentCheckPointIndex ==-1)return ;

        int index = currentCheckPointIndex ;
        imageView.setImageResource(Resource.s4ImagePath[index * 2 + (isSingle ? 0 : 1)]);
        if (isSingle) {
            Log.i(TAG, "Change mode : Single mode");
        }
        else
        {
            Log.i(TAG,"Change mode : multi mode");

        }
    }
    public void setLottieAnimationView (LottieAnimationView lottieAnimationView)
    {
        this.lottieAnimationView=lottieAnimationView;
        this.lottieAnimationView.addAnimatorUpdateListener((animation -> {
            float test = (float) animation.getAnimatedValue();
            Log.i(TAG,"test"+test);

        }));
    }
    public void playLottieAnimation ()
    {
        if (!lottieAnimationView.isAnimating()) lottieAnimationView.playAnimation();

    }
    public int getCurrentCheckPointIndex ()
    {
        return currentCheckPointIndex;
    }

    public void jumpToClass ()
    {
        currentSession =ExtraTools.S2;
        lottieAnimationView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        setUrl(Resource.s2_2VideoPath);
        videoView.start();
        videoView.seekTo(6000);
        checkPoint =s2CheckPoint;
        isLoop=true;
    }
    public interface AlexaFinishCallback{
        void onCompletion ();


    }











}
