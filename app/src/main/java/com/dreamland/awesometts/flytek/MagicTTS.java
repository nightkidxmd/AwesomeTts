package com.dreamland.awesometts.flytek;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.dreamland.awesometts.BaseTTS;
import com.dreamland.awesometts.ITTS;
import com.dreamland.awesometts.ITTSInnerListener;
import com.dreamland.awesometts.TtsType;
import com.dreamland.awesometts.utils.FileUtil;
import com.iflytek.tts.TtsService.Tts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * 高德tts
 * @author XMD
 */
public class MagicTTS extends BaseTTS implements ITTS {
	private static final int ISS_TTS_PARAM_SPEAKER = 1280;
	private static final int ISS_TTS_PARAM_VOICE_SPEED = 1282;
	private static final int ISS_TTS_PARAM_VOICE_PITCH = 1283;
	private static final int ISS_TTS_PARAM_VOLUME = 1284;

	private static final int ivTTS_ROLE_USER = 99;
	private static int[][] params = {
//			{ISS_TTS_PARAM_SPEAKER,ivTTS_ROLE_USER},
			{ISS_TTS_PARAM_VOICE_SPEED,5000},
			{ISS_TTS_PARAM_VOICE_PITCH,3000},
			{ISS_TTS_PARAM_VOLUME,Integer.MAX_VALUE},
	};

	public static final int TTS_ROLE_LINZHILING = 1;
	public static final int TTS_ROLE_GUODEGANG  = 2;

	private static final Map<Integer,String> ROLE_MAPS = new HashMap<>();
	static {
		ROLE_MAPS.put(TTS_ROLE_LINZHILING,"linzhiling.irf");
		ROLE_MAPS.put(TTS_ROLE_GUODEGANG,"guodegang.irf");
	}

	private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(@NonNull Runnable runnable) {
					return new Thread(runnable,"MagicTTS");
				}
			});


	public MagicTTS(Context context, ITTSInnerListener listener) {
		super(context,listener, "MagicTTS",null);
	}

	@Override
	protected void onInitializing() {
		if(TextUtils.isEmpty(resPath)){
			resPath = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts/";
		}else if(!resPath.endsWith(File.pathSeparator)){
			resPath = resPath+File.pathSeparator;
		}
		File resDir = new File(resPath);
		if(!resDir.exists()){
			if(!resDir.mkdirs()){
				sendMessage(MESSAGE_INIT_ERROR,-1);
				return;
			}
		}
		createTts();
	}

	private void createTts(){
		String roleResFile = ROLE_MAPS.get(role);
		String targetPath = resPath+roleResFile;
		FileUtil.copyFromAssetsToSdcard(getContext(), false, "tts/iflytek/autonavi/"+roleResFile, targetPath);
		Tts.JniCreate(targetPath);
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
	protected void onSwitchingRole() {
		Tts.JniDestory();
		createTts();
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
	public TtsType getTtsType() {
		return TtsType.MAGIC_TTS;
	}

	@Override
	public int getSpeed() {
		return Tts.JniGetParam(ISS_TTS_PARAM_VOICE_SPEED);
	}

}
