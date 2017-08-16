package com.iflytek.tts.TtsService;

import android.media.AudioTrack;
import android.util.Log;

public class AudioData
{
  private static TtsPlayer mTtsPlayer = new TtsPlayer();

  public static void close()
  {
    if (mTtsPlayer != null)
      mTtsPlayer.close();
  }

  public static void onJniOutData(int paramInt, byte[] paramArrayOfByte)
  {
    if (mTtsPlayer == null)
      mTtsPlayer = new TtsPlayer();
    mTtsPlayer.play(paramInt, paramArrayOfByte);
  }

  public static void onJniWatchCB(int paramInt)
  {
  }

  public static void release()
  {

    if (mTtsPlayer != null)
      mTtsPlayer.close();
  }

  static class TtsPlayer
  {
    private static int mBuffSize = 8000;
    private static int mSampleRate;
    private static int mStreamType = 3;
    private volatile AudioTrack mAudio = null;
    private final Object mLock = new Object();

    static
    {
      mSampleRate = 16000;
    }

    public void close()
    {
      try
      {
        synchronized (this.mLock)
        {
          if (this.mAudio == null)
            return;
          if (this.mAudio.getState() != 1)
            return;
        }
      }
      catch (Exception localException)
      {
       
        return;
      }
      this.mAudio.release();
      this.mAudio = null;
    }

    public void play(int paramInt, byte[] paramArrayOfByte)
    {

      try
      {
        if (this.mAudio == null)
          this.mAudio = new AudioTrack(mStreamType, mSampleRate, 2, 2, mBuffSize, 1);
          if (this.mAudio.getPlayState() != 3)
            this.mAudio.play();
          this.mAudio.write(paramArrayOfByte, 0, paramInt);


        
      }
      catch (Exception localException)
      {

          Log.e("AudioData", Log.getStackTraceString(localException));
      }
      finally
      {
      }
    }
  }
}
