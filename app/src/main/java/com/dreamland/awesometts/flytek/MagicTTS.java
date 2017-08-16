package com.dreamland.awesometts.flytek;

import android.content.Context;
import android.os.Environment;

import com.dreamland.awesometts.BaseTTS;
import com.dreamland.awesometts.ITTS;
import com.dreamland.awesometts.ITTSInnerListener;
import com.dreamland.awesometts.utils.FileUtil;
import com.iflytek.tts.TtsService.Tts;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 高德tts
 */
public class MagicTTS extends BaseTTS implements ITTS {
	private static final int ISS_TTS_PARAM_SPEAKER = 1280;
	private static final int ISS_TTS_PARAM_VOICE_SPEED = 1282;
	private static final int ISS_TTS_PARAM_VOICE_PITCH = 1283;
	private static final int ISS_TTS_PARAM_VOLUME = 1284;

	private static final int ivTTS_ROLE_USER = 99;
	private static int[][] params = {
//			{ISS_TTS_PARAM_SPEAKER,ivTTS_ROLE_USER},
			{ISS_TTS_PARAM_VOICE_SPEED,8000},
			{ISS_TTS_PARAM_VOICE_PITCH,3000},
			{ISS_TTS_PARAM_VOLUME,32767},
	};

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	public MagicTTS(Context context,ITTSInnerListener listener) {
		super(context,listener, "MagicTTS");
	}

	@Override
	protected void onInitializing() {
		File resDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts");
		if(!resDir.exists()){
			if(!resDir.mkdirs()){
				sendMessage(MESSAGE_INIT_ERROR,-1);
				return;
			}
		}

		FileUtil.copyFromAssetsToSdcard(getContext(), false, "tts/iflytek/autonavi/Resource.irf", Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts/Resource.irf");
		Tts.JniCreate(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts/Resource.irf");

		for(int[] p:params){
			Tts.JniSetParam(p[0],p[1]);
		}
		sendMessage(MESSAGE_READY);
	}

	@Override
	protected void handleTtsPlay(final String content) {
		onTTSPlay();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Tts.JniSpeak(content);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				onTTSEnd();
			}
		});
	}

	@Override
	protected void handleTtsStop() {
		Tts.JniStop();
	}

	@Override
	protected void handleSetRole(int role) {
		Tts.JniSetParam(ISS_TTS_PARAM_SPEAKER,role);
	}

	@Override
	protected void handleSetPitch(int pitch) {
		Tts.JniSetParam(ISS_TTS_PARAM_VOICE_PITCH,pitch);
	}

	@Override
	protected void handleSetVolume(int volume) {
		Tts.JniSetParam(ISS_TTS_PARAM_VOLUME,volume);
	}

	@Override
	protected void handleSetSpeed(int speed) {
		Tts.JniSetParam(ISS_TTS_PARAM_VOICE_SPEED,speed);
	}

	@Override
	protected void onDestroy() {
		Tts.JniDestory();
		executorService.shutdown();
		sendMessage(MESSAGE_DESTROYED);
	}

	@Override
	public int getRole() {
		return Tts.JniGetParam(ISS_TTS_PARAM_SPEAKER);
	}

	@Override
	public int getPitch() {
		return Tts.JniGetParam(ISS_TTS_PARAM_VOICE_PITCH);
	}

	@Override
	public int getVolume() {
		return Tts.JniGetParam(ISS_TTS_PARAM_VOLUME);
	}

	@Override
	public int getSpeed() {
		return Tts.JniGetParam(ISS_TTS_PARAM_VOICE_SPEED);
	}

}
