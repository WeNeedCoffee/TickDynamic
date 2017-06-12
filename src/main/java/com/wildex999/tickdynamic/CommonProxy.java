package com.wildex999.tickdynamic;

import net.minecraft.util.text.translation.I18n;

public class CommonProxy {
	public String translate(String s, Object... args){
		return I18n.translateToLocalFormatted(s, args);
	}
}
