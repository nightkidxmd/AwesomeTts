package com.iflytek.speech;

public class libisstts
{
  static
  {
    System.loadLibrary("ichip-jni");
    System.loadLibrary("itts-jni");
  }

  public static native void configCreate(NativeHandle paramNativeHandle, String paramString, ITtsListener paramITtsListener);

  public static native void create(NativeHandle paramNativeHandle, ITtsListener paramITtsListener);

  public static native void destroy(NativeHandle paramNativeHandle);

  public static native void getAudioData(NativeHandle paramNativeHandle, byte[] paramArrayOfByte, int paramInt, int[] paramArrayOfInt);

  public static native int initRes(String paramString, int paramInt);

  public static native void setParam(NativeHandle paramNativeHandle, int paramInt1, int paramInt2);

  public static native void start(NativeHandle paramNativeHandle, String paramString);

  public static native void stop(NativeHandle paramNativeHandle);

  public static native int unInitRes();
}

/* Location:           D:\002.WORKSPACE\SpeechJNIFJDX_dex2jar.jar
 * Qualified Name:     com.iflytek.speech.libisstts
 * JD-Core Version:    0.6.2
 */