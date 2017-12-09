package com.honeywell.iaq.interfaces;

import java.util.ArrayList;


public interface IResponse<T extends IData> {
    
	void response(ArrayList<T> resultList, int resultCode, Object... objects) throws Exception;


}
