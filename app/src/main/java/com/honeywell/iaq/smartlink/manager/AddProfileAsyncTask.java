/*
* Copyright (C) 2015 Texas Instruments Incorporated - http://www.ti.com/
*
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*    Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
*    Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the
*    distribution.
*
*    Neither the name of Texas Instruments Incorporated nor the names of
*    its contributors may be used to endorse or promote products derived
*    from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
*  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
*  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
*  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
*  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
*  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
*  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
*  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
*  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/

package com.honeywell.iaq.smartlink.manager;

import android.os.AsyncTask;
import android.util.Log;

import com.honeywell.iaq.utils.network.DeviceVersion;
import com.honeywell.iaq.utils.network.NetworkUtil;
import com.honeywell.iaq.utils.network.SecurityType;

import java.util.ArrayList;

public class AddProfileAsyncTask extends AsyncTask<ArrayList<Object>, Void, Boolean> {

	private static final String TAG = "AddProfileAsyncTask";
	public static String BASE_URL = "http://mysimplelink.net";
	public static String DEVICE_LIST_FAILED_ADDING_PROFILE = "Failed adding the profile";



	private AddProfileAsyncTaskCallback mAddProfileAsyncTaskCallback;
	public DeviceVersion mDeviceVersion;
	public String mDeviceName;

	public AddProfileAsyncTask(AddProfileAsyncTaskCallback callBack) {
		mAddProfileAsyncTaskCallback = callBack;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mAddProfileAsyncTaskCallback.addProfileCompleted();
		super.onPostExecute(result);
	}

	@Override
	protected Boolean doInBackground(ArrayList<Object>... params) {

		ArrayList<Object> list = params[0];
		String deviceName = (String)list.get(0);
		SecurityType ssidToAddSecurityType = (SecurityType)list.get(1);
		String ssidToAdd = (String)list.get(2);
		String ssidToAddSecurityKey = (String)list.get(3);
		String ssidToAddPriority = (String)list.get(4);
		String iotUuid = (String)list.get(5);

		mDeviceVersion = NetworkUtil.getSLVersion(BASE_URL);

		if (mDeviceVersion == DeviceVersion.UNKNOWN) {
			mAddProfileAsyncTaskCallback.addProfileFailed("Failed to get version of the device");
			return false;
		}

		if ( !iotUuid.equals("") ) {
			if (NetworkUtil.setIotUuid(iotUuid, BASE_URL)) {
				print("Set UUID" + iotUuid);
			}
		}

		mDeviceName = deviceName;

//		if (!deviceName.equals("")) {
//			if (NetworkUtil.setNewDeviceName(deviceName, BASE_URL, mDeviceVersion)) {
//				mDeviceName = deviceName;
//				print("Set a new device name " + deviceName);
//			}
//			else {
//				mAddProfileAsyncTaskCallback.addProfileFailed("Failed to get version of the device");
//				return false;
//			}
//		}
//		else {
//			//read device name from device only if not set by the application
//			mDeviceName = NetworkUtil.getDeviceName(BASE_URL, mDeviceVersion);
//		}
		
		mAddProfileAsyncTaskCallback.addProfileDeviceNameFetched(mDeviceName);

		print("Set a new Wifi configuration");
		if (!NetworkUtil.addProfile(BASE_URL, ssidToAddSecurityType, ssidToAdd, ssidToAddSecurityKey, ssidToAddPriority, mDeviceVersion)) {
			mAddProfileAsyncTaskCallback.addProfileFailed(DEVICE_LIST_FAILED_ADDING_PROFILE);
			return false;
		}

		print("Starting configuration verification");
		NetworkUtil.moveStateMachineAfterProfileAddition(BASE_URL, ssidToAdd, mDeviceVersion);

		return true;
	}

	private void print(String msg) {
		Log.i(TAG, msg);
		mAddProfileAsyncTaskCallback.addProfileMsg(msg);
	}

	public interface AddProfileAsyncTaskCallback {
		void addProfileCompleted();
		void addProfileDeviceNameFetched(String deviceName);
		void addProfileFailed(String errorMessage);
		void addProfileMsg(String errorMessage);
	}
}
