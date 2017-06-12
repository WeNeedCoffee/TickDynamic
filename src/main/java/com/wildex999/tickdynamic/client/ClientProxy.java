package com.wildex999.tickdynamic.client;

import com.wildex999.tickdynamic.CommonProxy;
import net.minecraft.client.resources.I18n;

public class ClientProxy extends CommonProxy {
	@Override
	public String translate(String s, Object... args){
		return I18n.format(s, args);
	}
}
