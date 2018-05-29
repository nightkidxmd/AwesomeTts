package com.iflytek.speech.tts;

import android.media.AudioTrack;
import android.util.Log;

import com.iflytek.ITTSListener;
import com.iflytek.speech.ITtsListener;
import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libisstts;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TtsPlayer implements ITtsListener {
  private static final int M_AUDIO_FORMAT = 2;
  private static final int M_CHANNEL_CONFIG = 4;
  private static final int M_SAMPLE_RATE_IN_HZ = 16000;
  private static final String TAG ="TtsPlayer";
  private static int cnt = 0;
  private final Lock mAudioTrackLock = new ReentrantLock();
  private final AudioWriteWorkingFunc mAudioWriteWorkingFunc = new AudioWriteWorkingFunc();
  private final NativeHandle mNativeHandle = new NativeHandle();
  private final Object mWorkingThreadSyncObj = new Object();
  private AudioTrack mAudioTrack = null;
  private int mAudioTrackSteamState = 0;
  private ITTSListener mITTSListener = null;
  private int mMinBufferSizeInBytes = 0;
  private boolean mOnDataReadyFlag = false;
  private Thread mThreadAudioWrite = null;
  private int nPreTextIndex = -1;

  public int Init(int paramInt, String paramString) {
    cnt = 1 + cnt;
    Log.d(TAG,"Init cnt: " + cnt);
    try {
      libisstts.destroy(mNativeHandle);
      libisstts.configCreate(mNativeHandle, paramString, this);
      if (mNativeHandle.err_ret != 0) {
        return mNativeHandle.err_ret;
      }

      mMinBufferSizeInBytes = AudioTrack.getMinBufferSize(M_SAMPLE_RATE_IN_HZ, M_CHANNEL_CONFIG, M_AUDIO_FORMAT);
      Log.d(TAG,"mMinBufferSizeInBytes=" + mMinBufferSizeInBytes + ".");
      if (mMinBufferSizeInBytes <= 0) {
        Log.e(TAG,"Error: AudioTrack.getMinBufferSize(16000, 4, 2) ret " + mMinBufferSizeInBytes);
        return 10106;
      }
      if (mAudioTrack == null) {
        mAudioTrack = new AudioTrack(paramInt, M_SAMPLE_RATE_IN_HZ, M_CHANNEL_CONFIG, M_AUDIO_FORMAT, 3 * mMinBufferSizeInBytes, 1);
        if (mAudioTrack.getState() != 1) {
          Log.e(TAG,"Error: Can't init AudioRecord!");
          return -1;
        }
        Log.d(TAG,"new AudioTrack(streamType=" + paramInt + ")");
      }
      mAudioTrackSteamState = 0;
      if (mThreadAudioWrite == null) {
        mAudioWriteWorkingFunc.clearExitFlag();
        mThreadAudioWrite = new Thread(mAudioWriteWorkingFunc, "mThreadAudioWrite");
        mThreadAudioWrite.start();
      }
      return 0;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return 10106;
  }

  public int Pause() {
    Log.d(TAG,"Pause");
    if ((mAudioTrack == null) || (mThreadAudioWrite == null) || (mNativeHandle.native_point == 0)) {
      if ((mAudioTrackSteamState == AudioTrackSteamState.STREAM_RELEASED) || (mAudioTrackSteamState == AudioTrackSteamState.STREAM_STOPPED)) {
        return 10000;
      } else if (mAudioTrackSteamState == AudioTrackSteamState.STREAM_PAUSED) {
        return 0;
      }
    }

    mAudioTrackSteamState = AudioTrackSteamState.STREAM_PAUSED;
    mAudioTrackLock.lock();
    if (mAudioTrack != null) {
      mAudioTrack.pause();
    }
    mAudioTrackLock.unlock();
    synchronized (mWorkingThreadSyncObj) {
      mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int Release() {
    Log.d(TAG,"Release");
    mAudioTrackSteamState = AudioTrackSteamState.STREAM_RELEASED;
    mAudioWriteWorkingFunc.setExitFlag();
    if (mThreadAudioWrite != null) {
      try {
        mThreadAudioWrite.join();
        mThreadAudioWrite = null;
        libisstts.destroy(mNativeHandle);
        return 0;
      } catch (InterruptedException localInterruptedException) {
        localInterruptedException.printStackTrace();
      }
    }
    return 0;
  }

  public int Resume() {
    Log.d(TAG,"Resume");
    if ((mAudioTrack == null) || (mThreadAudioWrite == null) || (mNativeHandle.native_point == 0)) {
      if ((mAudioTrackSteamState == AudioTrackSteamState.STREAM_RELEASED) || (mAudioTrackSteamState == AudioTrackSteamState.STREAM_STOPPED)) {
        return 10000;
      } else if (mAudioTrackSteamState == AudioTrackSteamState.STREAM_RUNNING) {
        return 0;
      }
    }

    mAudioTrackSteamState = AudioTrackSteamState.STREAM_RUNNING;
    mAudioTrackLock.lock();
    if (mAudioTrack != null) {
      mAudioTrack.play();
    }
    mAudioTrackLock.unlock();
    synchronized (mWorkingThreadSyncObj) {
      mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int SetParam(int paramInt1, int paramInt2) {
    Log.d(TAG,"SetParam");
    if (mNativeHandle.native_point == 0) {
      return 10000;
    }
    libisstts.setParam(mNativeHandle, paramInt1, paramInt2);
    return mNativeHandle.err_ret;
  }

  public int Start(String paramString) {
    Log.d(TAG,"Start");
    if ((mAudioTrack == null) || (mThreadAudioWrite == null) || (mNativeHandle.native_point == 0)) {
      Stop();
      return 10000;
    }
    synchronized (mWorkingThreadSyncObj) {
      if (mAudioTrackSteamState == AudioTrackSteamState.STREAM_RELEASED) {
        nPreTextIndex = -1;
        mOnDataReadyFlag = false;
      }

      Log.d(TAG,"start text : " + paramString);
      libisstts.start(mNativeHandle, paramString);
      if (mNativeHandle.err_ret != 0) {
        return mNativeHandle.err_ret;
      }
      mAudioTrackSteamState = AudioTrackSteamState.STREAM_RUNNING;
      mAudioTrackLock.lock();
      if (mAudioTrack != null) {
        mAudioTrack.play();
      }
      mAudioTrackLock.unlock();

      mWorkingThreadSyncObj.notifyAll();
      return 0;
    }
  }

  public int Stop() {
    Log.d(TAG,"Stop " + mAudioTrackSteamState);
    synchronized (mWorkingThreadSyncObj) {
      if (mAudioTrackSteamState == AudioTrackSteamState.STREAM_RELEASED) {
        return 10000;
      } else if (mAudioTrackSteamState == AudioTrackSteamState.STREAM_STOPPED) {
        return 0;
      }
    }
    libisstts.stop(mNativeHandle);
    mAudioTrackLock.lock();
    if (mAudioTrack != null) {
      mAudioTrack.pause();
      mAudioTrack.flush();
    }
    mAudioTrackLock.unlock();
    if (mITTSListener != null) {
      mITTSListener.onTTSPlayInterrupted();
    }

    synchronized (mWorkingThreadSyncObj){
      mWorkingThreadSyncObj.notifyAll();
      mAudioTrackSteamState = AudioTrackSteamState.STREAM_STOPPED;
    }

    return 0;
  }

  @Override
  public void onDataReady() {
    Log.d(TAG,"onDataReady");
    synchronized (mWorkingThreadSyncObj) {
      mOnDataReadyFlag = true;
      mWorkingThreadSyncObj.notifyAll();
    }

    if (mITTSListener != null) {
      mITTSListener.onTTSPlayBegin();
    }
  }

  @Override
  public void onProgress(int paramInt1, int paramInt2) {
    if (nPreTextIndex < paramInt1) {
      Log.d(TAG,"onProgress(" + paramInt1 + ", " + paramInt2 + ")");

    }

    if (mITTSListener != null) {
      mITTSListener.onTTSProgressReturn(paramInt1, paramInt2);
      nPreTextIndex = paramInt1;
    }
  }

  public void setListener(ITTSListener paramITTSListener) {
    mITTSListener = paramITTSListener;
  }

  private class AudioTrackSteamState {
    public static final int STREAM_PAUSED = 2;
    public static final int STREAM_RELEASED = 3;
    public static final int STREAM_RUNNING = 1;
    public static final int STREAM_STOPPED = 0;

    private AudioTrackSteamState() {
    }
  }

  private class AudioWriteWorkingFunc
          implements Runnable {
    private boolean mExitFlag = false;

    private AudioWriteWorkingFunc() {
    }

    public void clearExitFlag() {
      mExitFlag = false;
    }

    @Override
    public void run() {
      Log.d(TAG,"AudioWriteWorkingFunc In.");
      if ((mAudioTrack == null) || (mNativeHandle.native_point == 0) || (mMinBufferSizeInBytes == 0)) {
        Log.e(TAG,"mAudioTrack==null || mNativeHandle.native_point == 0 || mMinBufferSizeInBytes==0, should never happen.");
        mAudioTrackLock.lock();
        if (mAudioTrack != null) {
          mAudioTrack.release();
          mAudioTrack = null;
        }
        mAudioTrackLock.unlock();
        Log.d(TAG,"AudioWriteWorkingFunc Out.");
        return;
      }
      byte[] arrayOfByte = new byte[mMinBufferSizeInBytes];
      Log.d(TAG,"mBufferOnceSizeInBytes is " + mMinBufferSizeInBytes);
      while (!mExitFlag) {
        if (mAudioTrackSteamState != AudioTrackSteamState.STREAM_RUNNING || !mOnDataReadyFlag) {
          synchronized (mWorkingThreadSyncObj) {
            if (mAudioTrackSteamState != AudioTrackSteamState.STREAM_RUNNING || !mOnDataReadyFlag) {
              try {
                mWorkingThreadSyncObj.wait();
              } catch (InterruptedException e) {
                e.printStackTrace();
              }

              Log.i(TAG,"run play");

              if (mExitFlag) {
                break;
              }
            }
          }
        }

        int[] arrayOfInt = new int[1];
        libisstts.getAudioData(mNativeHandle, arrayOfByte, mMinBufferSizeInBytes, arrayOfInt);
        if (mNativeHandle.err_ret == 10004) {
          Log.d(TAG,"libisstts.getAudioData Completed.");
          mAudioTrackSteamState = AudioTrackSteamState.STREAM_STOPPED;
          if (mITTSListener != null) {
            mITTSListener.onTTSPlayCompleted();
          }
          continue;
        }
        if (arrayOfInt[0] <= 0) {
          try {
            synchronized (mWorkingThreadSyncObj) {
              mWorkingThreadSyncObj.wait(5L);
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          continue;
        }
        mAudioTrackLock.lock();
        int i = mAudioTrack.write(arrayOfByte, 0, arrayOfInt[0]);
        mAudioTrackLock.unlock();
        if (i >= 0) {
          continue;
        }
        Log.e(TAG,"mAudioTrack.write(size=" + arrayOfInt[0] + ") ret " + i);
        synchronized (mWorkingThreadSyncObj) {
          mAudioTrackSteamState = AudioTrackSteamState.STREAM_STOPPED;
        }
        Thread.yield();

      }
    }

    public void setExitFlag() {
      mExitFlag = true;
      synchronized (mWorkingThreadSyncObj) {
        mWorkingThreadSyncObj.notifyAll();
      }
    }
  }
}
