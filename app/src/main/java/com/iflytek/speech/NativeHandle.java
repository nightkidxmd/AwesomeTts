package com.iflytek.speech;

public class NativeHandle
{
  public int err_ret;
  public int native_point;

  public int getErr_ret()
  {
    return this.err_ret;
  }

  public int getNative_point()
  {
    return this.native_point;
  }

  public void reSet()
  {
    this.err_ret = 0;
    this.native_point = 0;
  }

  public void setErr_ret(int paramInt)
  {
    this.err_ret = paramInt;
  }

  public void setNative_point(int paramInt)
  {
    this.native_point = paramInt;
  }
}
