package com.dreamland.awesometts;

import java.io.Serializable;

abstract public class AbsTtsInfo implements Serializable {
	private String content;
	private int id;
	private int type;
	private Serializable serializable;
	public static final int TYPE_TTS   = 0;
	public static final int TYPE_AUDIO = 1;
	
	public AbsTtsInfo(String content,int id,Serializable serializable) {
		this.content = content;
		this.type = TYPE_TTS;
		this.id = id;
		this.serializable = serializable;
	}
	
	public AbsTtsInfo(String content, int type,int id,Serializable serializable) {
		this.content = content;
		this.type = type;
		this.id = id;
		this.serializable = serializable;
	}

	public String getContent(){
		return content;
	}
	
	public int getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public Serializable getSerializable() {
		return serializable;
	}

	@Override
	public String toString() {
		return "AbsTtsInfo{" +
				"content='" + content + '\'' +
				", id=" + String.format("0x%x",id) +
				", type=" + type +
				", serializable=" + serializable +
				'}';
	}
}

