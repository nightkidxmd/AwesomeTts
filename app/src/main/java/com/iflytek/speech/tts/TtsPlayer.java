package com.iflytek.speech.tts;

import android.media.AudioTrack;
import android.util.Log;

import com.iflytek.ITTSListener;
import com.iflytek.speech.ITtsListener;
import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libisstts;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TtsPlayer
  implements ITtsListener
{
  private static int cnt = 0;
  private static final int mAudioFormat = 2;
  private static final int mChannelConfig = 4;
  private static final int mSampleRateInHz = 16000;
  private AudioTrack mAudioTrack = null;
  private final Lock mAudioTrackLock = new ReentrantLock();
  private int mAudioTrackSteamState = 0;
  private final AudioWriteWorkingFunc mAudioWriteWorkingFunc = new AudioWriteWorkingFunc();
  private ITTSListener mITTSListener = null;
  private int mMinBufferSizeInBytes = 0;
  private final NativeHandle mNativeHandle = new NativeHandle();
  private boolean mOnDataReadyFlag = false;
  private Thread mThreadAudioWrite = null;
  private final Object mWorkingThreadSyncObj = new Object();
  private int nPreTextIndex = -1;
  private String tag = "TtsPlayer_" + cnt;

  public int Init(int paramInt, String paramString)
  {
    cnt = 1 + cnt;
    Log.d("ttsplayer", "Init cnt: " + cnt);
    try
    {
      libisstts.destroy(this.mNativeHandle);
      libisstts.configCreate(this.mNativeHandle, paramString, this);
      if (this.mNativeHandle.err_ret != 0){
        return this.mNativeHandle.err_ret;
      }

      this.mMinBufferSizeInBytes = AudioTrack.getMinBufferSize(16000, 4, 2);
      Log.d(this.tag, "mMinBufferSizeInBytes=" + this.mMinBufferSizeInBytes + ".");
      if (this.mMinBufferSizeInBytes <= 0)
      {
        Log.e(this.tag, "Error: AudioTrack.getMinBufferSize(16000, 4, 2) ret " + this.mMinBufferSizeInBytes);
        return 10106;
      }
      if (this.mAudioTrack == null)
      {
        this.mAudioTrack = new AudioTrack(paramInt, 16000, 4, 2, 3 * this.mMinBufferSizeInBytes, 1);
        if (this.mAudioTrack.getState() != 1)
        {
          Log.e(this.tag, "Error: Can't init AudioRecord!");
          return -1;
        }
        Log.d(this.tag, "new AudioTrack(streamType=" + paramInt + ")");
      }
      this.mAudioTrackSteamState = 0;
      if (this.mThreadAudioWrite == null)
      {
        this.mAudioWriteWorkingFunc.clearExitFlag();
        this.mThreadAudioWrite = new Thread(this.mAudioWriteWorkingFunc, "mThreadAudioWrite");
        this.mThreadAudioWrite.start();
      }
      return 0;
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    return 10106;
  }

  public int Pause()
  {
    Log.d(this.tag, "Pause");
    if ((this.mAudioTrack == null) || (this.mThreadAudioWrite == null) || (this.mNativeHandle.native_point == 0)){
      if ((this.mAudioTrackSteamState == 3) || (this.mAudioTrackSteamState == 0)) {
        return 10000;
      }else if (this.mAudioTrackSteamState == 2){
        return 0;
      }
    }

    this.mAudioTrackSteamState = 2;
    this.mAudioTrackLock.lock();
    if (this.mAudioTrack != null) {
      this.mAudioTrack.pause();
    }
    this.mAudioTrackLock.unlock();
    synchronized (this.mWorkingThreadSyncObj)
    {
      this.mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int Release()
  {
    Log.d(this.tag, "Release");
    this.mAudioTrackSteamState = 3;
    this.mAudioWriteWorkingFunc.setExitFlag();
    if (this.mThreadAudioWrite != null){
      try {
        this.mThreadAudioWrite.join();
        this.mThreadAudioWrite = null;
        libisstts.destroy(this.mNativeHandle);
        return 0;
      } catch (InterruptedException localInterruptedException) {
        localInterruptedException.printStackTrace();
      }
    }
    return 0;
  }

  public int Resume()
  {
    Log.d(this.tag, "Resume");
    if ((this.mAudioTrack == null) || (this.mThreadAudioWrite == null) || (this.mNativeHandle.native_point == 0)){
      if ((this.mAudioTrackSteamState == 3) || (this.mAudioTrackSteamState == 0)){
        return 10000;
      }else if (this.mAudioTrackSteamState == 1)
        return 0;
    }

    this.mAudioTrackSteamState = 1;
    this.mAudioTrackLock.lock();
    if (this.mAudioTrack != null) {
      this.mAudioTrack.play();
    }
    this.mAudioTrackLock.unlock();
    synchronized (this.mWorkingThreadSyncObj)
    {
      this.mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int SetParam(int paramInt1, int paramInt2)
  {
    Log.d(this.tag, "SetParam");
    if (this.mNativeHandle.native_point == 0) {
      return 10000;
    }
    libisstts.setParam(this.mNativeHandle, paramInt1, paramInt2);
    return this.mNativeHandle.err_ret;
  }

  public int Start(String paramString)
  {
    Log.d(this.tag, "Start");
    if ((this.mAudioTrack == null) || (this.mThreadAudioWrite == null) || (this.mNativeHandle.native_point == 0)){
      Stop();
      return 10000;
    }
    if (this.mAudioTrackSteamState == 3){
      this.nPreTextIndex = -1;
      this.mOnDataReadyFlag = false;
    }

    Log.d(this.tag, "start text : " + paramString);
    libisstts.start(this.mNativeHandle, paramString);
    if (this.mNativeHandle.err_ret != 0) {
      return this.mNativeHandle.err_ret;
    }
    this.mAudioTrackSteamState = 1;
    this.mAudioTrackLock.lock();
    if (this.mAudioTrack != null) {
      this.mAudioTrack.play();
    }
    this.mAudioTrackLock.unlock();
    synchronized (this.mWorkingThreadSyncObj)
    {
      this.mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int Stop()
  {
    Log.d(this.tag, "Stop");
    if ((this.mAudioTrack == null) || (this.mThreadAudioWrite == null) || (this.mNativeHandle.native_point == 0)){
      if(mAudioTrackSteamState == 3) {
        return 10000;
      }else if (this.mAudioTrackSteamState == 0)
        return 0;
    }

    this.mAudioTrackSteamState = 0;
    libisstts.stop(this.mNativeHandle);
    this.mAudioTrackLock.lock();
    if (this.mAudioTrack != null)
    {
      this.mAudioTrack.pause();
      this.mAudioTrack.flush();
    }
    this.mAudioTrackLock.unlock();
    synchronized (this.mWorkingThreadSyncObj)
    {
      this.mWorkingThreadSyncObj.notifyAll();
    }
    if (this.mITTSListener != null){
      this.mITTSListener.onTTSPlayInterrupted();
    }

    return 0;
  }

  public void onDataReady()
  {
    Log.d(this.tag, "onDataReady");
    this.mOnDataReadyFlag = true;
    if (this.mITTSListener != null) {
      this.mITTSListener.onTTSPlayBegin();
    }
  }

  public void onProgress(int paramInt1, int paramInt2)
  {
    if (this.nPreTextIndex < paramInt1)
    {
      Log.d(this.tag, "onProgress(" + paramInt1 + ", " + paramInt2 + ")");

    }

    if (this.mITTSListener != null){
      this.mITTSListener.onTTSProgressReturn(paramInt1, paramInt2);
      this.nPreTextIndex = paramInt1;
    }
  }

  public void setListener(ITTSListener paramITTSListener)
  {
    this.mITTSListener = paramITTSListener;
  }

  private class AudioTrackSteamState
  {
    public static final int STREAM_PAUSED = 2;
    public static final int STREAM_RELEASED = 3;
    public static final int STREAM_RUNNING = 1;
    public static final int STREAM_STOPPED = 4;

    private AudioTrackSteamState()
    {
    }
  }

  private class AudioWriteWorkingFunc
    implements Runnable
  {
    private static final String tag = "AudioWriteWorkingFunc";
    private boolean mExitFlag = false;

    private AudioWriteWorkingFunc()
    {
    }

    public void clearExitFlag()
    {
      this.mExitFlag = false;
    }

    public void run()
    {
      Log.d("AudioWriteWorkingFunc", "AudioWriteWorkingFunc In.");
      if ((TtsPlayer.this.mAudioTrack == null) || (TtsPlayer.this.mNativeHandle.native_point == 0) || (TtsPlayer.this.mMinBufferSizeInBytes == 0))
      {
        Log.e("AudioWriteWorkingFunc", "mAudioTrack==null || mNativeHandle.native_point == 0 || mMinBufferSizeInBytes==0, this should never happen.");
        TtsPlayer.this.mAudioTrackLock.lock();
        if (TtsPlayer.this.mAudioTrack != null)
        {
          TtsPlayer.this.mAudioTrack.release();
          TtsPlayer.this.mAudioTrack = null;
        }
        TtsPlayer.this.mAudioTrackLock.unlock();
        Log.d("AudioWriteWorkingFunc", "AudioWriteWorkingFunc Out.");
        return;
      }
      byte[] arrayOfByte = new byte[TtsPlayer.this.mMinBufferSizeInBytes];
      Log.d("AudioWriteWorkingFunc", "mBufferOnceSizeInBytes is " + TtsPlayer.this.mMinBufferSizeInBytes);
      while (!mExitFlag) {
         synchronized (mWorkingThreadSyncObj){
           if (TtsPlayer.this.mAudioTrackSteamState != 1 && TtsPlayer.this.mOnDataReadyFlag){
             try {
               mWorkingThreadSyncObj.wait();
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
           }
         }


          int[] arrayOfInt = new int[1];
          libisstts.getAudioData(TtsPlayer.this.mNativeHandle, arrayOfByte, TtsPlayer.this.mMinBufferSizeInBytes, arrayOfInt);
          if (TtsPlayer.this.mNativeHandle.err_ret == 10004)
          {
            Log.d("AudioWriteWorkingFunc", "libisstts.getAudioData Completed.");
            TtsPlayer.this.mAudioTrackSteamState = 0;
            if (TtsPlayer.this.mITTSListener != null){
              TtsPlayer.this.mITTSListener.onTTSPlayCompleted();
            }
            continue;
          }
          if (arrayOfInt[0] <= 0){
            try{
              synchronized (TtsPlayer.this.mWorkingThreadSyncObj)
              {
//                Log.d("AudioWriteWorkingFunc", "Before wait(5)");
                TtsPlayer.this.mWorkingThreadSyncObj.wait(5L);
//                Log.d("AudioWriteWorkingFunc", "After wait(5)");
              }
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          TtsPlayer.this.mAudioTrackLock.lock();
          int i = TtsPlayer.this.mAudioTrack.write(arrayOfByte, 0, arrayOfInt[0]);
          TtsPlayer.this.mAudioTrackLock.unlock();
          if (i >= 0)
            continue;
//          Log.e("AudioWriteWorkingFunc", "mAudioTrack.write(size=" + arrayOfInt[0] + ") ret " + i);
          TtsPlayer.this.mAudioTrackSteamState = 0;
          Thread.yield();
        }
    }

//        try
//        {
//          label374: synchronized (TtsPlayer.this.mWorkingThreadSyncObj)
//          {
//            Log.d("AudioWriteWorkingFunc", "Before wait(5)");
//            TtsPlayer.this.mWorkingThreadSyncObj.wait(5L);
//            Log.d("AudioWriteWorkingFunc", "After wait(5)");
//          }
//        }
//        catch (InterruptedException localInterruptedException2)
//        {
//          while (true)
//            localInterruptedException2.printStackTrace();
//        }
//
//
//    }

    public void setExitFlag()
    {
      this.mExitFlag = true;
      synchronized (TtsPlayer.this.mWorkingThreadSyncObj)
      {
        TtsPlayer.this.mWorkingThreadSyncObj.notifyAll();
        return;
      }
    }
  }
}
