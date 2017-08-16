package com.dreamland.awesometts;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;

import com.dreamland.awesometts.utils.AbsPollOnceThread;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;


public class TTSEngine<T extends AbsTtsInfo> implements OnCompletionListener, OnErrorListener, AudioManager.OnAudioFocusChangeListener, ITTSInnerListener {

	private final String AUDIO_PATH;

	private static final String TAG = TTSEngine.class.getSimpleName();

	protected List<ITTSListener<T>> mListeners = new ArrayList<>();

	protected Context mContext;

	private AudioManager mAudioManager;

	private TtsCore ttsCore;

	private TtsReaderThread mTtsReaderThread;

	public TTSEngine(Context context,TtsType ttsType){
		mContext = context;
		AUDIO_PATH = context.getObbDir().getAbsolutePath();
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		ttsCore = new TtsCore(context,ttsType,this);
	}

	synchronized public void registerITTSListener(ITTSListener<T> l){
		if(!mListeners.contains(l)){
			mListeners.add(l);
		}
	}

	synchronized public void unregisterITTSListener(ITTSListener<T> l){
		mListeners.remove(l);
	}

	@Override
	public void onTTSEnd(){
		//FIXME 目前的tts end回调比实际来得快
		try {
			Thread.sleep(125);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		T info = mTtsReaderThread.getCurrentInfo();
		if(info != null){
			Log.w(TAG,"onTTSEnd:"+info);
			synchronized(this){
				for(ITTSListener<T> l:mListeners){
					l.onTTSEnd(info);
				}
			}
		}
		mTtsReaderThread.notifyEnd();
		mAudioManager.abandonAudioFocus(this);
	}

	@Override
	public void onTTSPlay(){
		mAudioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		T info = mTtsReaderThread.getCurrentInfo();
		if(info != null){
			Log.w(TAG,"onTTSPlay:"+info);
			synchronized(this){
				for(ITTSListener<T> l:mListeners){
					l.onTTSPlay(info);
				}
			}
		}
	}

	@Override
	public void onAudioFocusChange(int i) {

	}


	private class TtsReaderThread extends AbsPollOnceThread {
		private LinkedBlockingDeque<T> mTtsQueue = new LinkedBlockingDeque<>();
		private final Object _lock = new Object();
		private T currentInfo;
		public void enqueue(T data){
			mTtsQueue.add(data);
			pollOnce();
		}

		public void clearQueue(){
			Log.v(TAG, "clearQueue:"+mTtsQueue.peekLast());
			mTtsQueue.clear();
		}

		public void notifyEnd(){
			synchronized (_lock) {
				_lock.notify();
			}
		}

		@Override
		protected void pollOnceWork() {
			while((currentInfo = mTtsQueue.poll()) != null && isRunning()){
				if(currentInfo.getType() == AbsTtsInfo.TYPE_TTS){
					_play(currentInfo.getContent());
				}else if(currentInfo.getType() == AbsTtsInfo.TYPE_AUDIO){
					playAudio(currentInfo.getContent());
				}

				synchronized (_lock) {
					try {
						Log.w(TAG,"等待ttsEnd:"+currentInfo);
						_lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}

		@Override
		protected void ready() {

		}

		public T getCurrentInfo() {
			return currentInfo;
		}
	}

	public void init(){
		mMediaPlayer = new MediaPlayer();
		mTtsReaderThread = new TtsReaderThread();
		mTtsReaderThread.start();
	}

	public void play(T data){
		if(mTtsReaderThread != null){
			mTtsReaderThread.enqueue(data);
		}
	}



	protected void _play(String content){
		Log.i(TAG,"_play:"+content);
		if(TextUtils.isEmpty(content)){
			onTTSPlay();
			onTTSEnd();
		}else {
			ttsCore.play(content);
		}
	}


	public void stop(){
		T info = mTtsReaderThread.getCurrentInfo();
		if(info != null){
			Log.w(TAG,"stop:"+info);
			switch(info.getType()){
			case AbsTtsInfo.TYPE_TTS:
				ttsCore.stop();
				break;
			case AbsTtsInfo.TYPE_AUDIO:
				stopAudio();
				break;
			}
		}
		mAudioManager.abandonAudioFocus(this);
	}

	public void clear(){
		mTtsReaderThread.clearQueue();
		ttsCore.stop();
		stopAudio();
	}
	
	

	@CallSuper
	public void destroy(){
		if(mTtsReaderThread != null){
			mTtsReaderThread.exit();
		}
		if(mMediaPlayer != null){
			mMediaPlayer.release();
		}

		mContext = null;
		mAudioManager.abandonAudioFocus(this);
	}
	
	

	private MediaPlayer mMediaPlayer;
	
	private void playAudio(String path){
		path = path.replace("default:", AUDIO_PATH+ File.separator);
		try {
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	private void stopAudio(){
		try{
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.stop();
			}
		}catch (IllegalStateException ignored){

		}

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		onTTSEnd();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		onTTSEnd();
		return false;
	}

	public void setPitch(int value){
		ttsCore.setPitch(value);
	}
	public void setVolume(int value){
		ttsCore.setVolume(value);
	}
	public void setSpeed(int value){
		ttsCore.setSpeed(value);
	}
}
