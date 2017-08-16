package com.iflytek.tts.TtsService;

public final class Tts
{
  private static int InitializeTTsBOOT = 0;

  static
  {
    System.loadLibrary("Aisound");
  }

  public static native int JniCreate(String paramString);

  public static native int JniDestory();

  public static native int JniGetParam(int paramInt);

  public static native int JniGetVersion();

  public static native boolean JniIsCreated();

  public static native int JniIsPlaying();

  public static native int JniSetParam(int paramInt1, int paramInt2);

  public static native int JniSpeak(String paramString);

  public static native int JniStop();

  public static void TTS_Destory()
  {
    if (InitializeTTsBOOT == 2)
    {
      JniStop();
      JniDestory();
      InitializeTTsBOOT = 0;
    }
  }

  public static void TTS_Stop()
  {
    if (InitializeTTsBOOT == 2)
      JniStop();
  }

  public static int getInitializeType()
  {
    return InitializeTTsBOOT;
  }

  public static void setInitializeType(int paramInt)
  {
    InitializeTTsBOOT = paramInt;
  }
}