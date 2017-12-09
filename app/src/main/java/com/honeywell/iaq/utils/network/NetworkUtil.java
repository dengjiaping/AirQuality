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


package com.honeywell.iaq.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.utils.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NetworkUtil {

	public static String DEVICE_PREFIX = "mysimplelink";

	private static final String TAG = "NetworkUtil";

	public static int NOT_CONNECTED = 0;
	public static int WIFI = 1;
	public static int MOBILE = 2;


	public static int getConnectionStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return WIFI;
			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return MOBILE;
		}
		return NOT_CONNECTED;
	}

	public static String getConnectedSSID(Context context) {

		if (context == null)
			return null;
		String networkName = null;
		int networkState = getConnectionStatus(context);
		Log.i(TAG, "Network State:" + networkState);
		if (networkState == NetworkUtil.WIFI) { //no wifi connection and alert dialog allowed
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager != null) {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
					networkName = wifiInfo.getSSID().replaceAll("\"", "");
				}
			}
		}
		if (networkName == null || networkName.equals("<unknown ssid>") || networkName.equals("0x") || networkName.equals("")) {
			networkName = null;
		}

		return networkName;
	}

	public static String getConnectionStatusString (Context context) {
		int connectionStatus = NetworkUtil.getConnectionStatus(context);
		if (connectionStatus == NetworkUtil.WIFI)
			return "Connected to Wifi";
		if (connectionStatus == NetworkUtil.MOBILE)
			return "Connected to Mobile Data";
		return "No internet connection";	
	}

	public static List<ScanResult> getWifiScanResults(Boolean sorted, Context context) {
		WifiManager wifiManager = NetworkUtil.getWifiManager(context);
		List<ScanResult> wifiList = wifiManager.getScanResults();

		//Remove results with empty ssid
		List<ScanResult> wifiListNew = new ArrayList<ScanResult>();
		for (ScanResult scanResult : wifiList) {
			if (!scanResult.SSID.equals(""))
				wifiListNew.add(scanResult);
		}
		wifiList.clear();
		wifiList.addAll(wifiListNew);

		if (sorted == false)
			return wifiList;

		ArrayList<ScanResult> wifiWithAPPrefix = new ArrayList<ScanResult>();
		ArrayList<ScanResult> rest = new ArrayList<ScanResult>();
		for (ScanResult scanResult : wifiList) {
			if (scanResult.SSID.contains(DEVICE_PREFIX))
				wifiWithAPPrefix.add(scanResult);
			else 
				rest.add(scanResult);
		}


		wifiWithAPPrefix = 	removeMultipleSSIDsWithRSSI(wifiWithAPPrefix);
		rest = removeMultipleSSIDsWithRSSI(rest);


		wifiWithAPPrefix.addAll(rest);
		wifiList = wifiWithAPPrefix;

		return wifiList;
	}


	/**
	 * The removeMultipleSSIDsWithRSSI method is used to remove multiple appearances of identical SSIDs from
	 * the list of APs obtained from the wifiManager, and displayed on the smartConfig mode configuration page as WiFi networks.
	 * This is due to the possible presence of several APs possessing the same SSID but different BSSIDs, and thus causing
	 * the same AP to appear several times on the list.
	 */
	public static ArrayList<ScanResult> removeMultipleSSIDsWithRSSI(ArrayList<ScanResult> list) {

		ArrayList<ScanResult> newList = new ArrayList<>();
		boolean contains;
		for (ScanResult ap : list) {
			contains = false;
			for (ScanResult mp : newList) {
				if ((mp.SSID).equals(ap.SSID)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				newList.add(ap);
			}
		}

		Collections.sort(newList, new Comparator<ScanResult>() {
			@Override
			public int compare(ScanResult lhs, ScanResult rhs) {
				return (lhs.level < rhs.level ? 1 : (lhs.level == rhs.level ? 0 : -1));
			}

		});

		return newList;

	}

	public static String getWifiName (Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String wifiName = wifiManager.getConnectionInfo().getSSID();
		if (wifiName != null){
			if (!wifiName.contains("unknown ssid") && wifiName.length() > 2){
				if (wifiName.startsWith("\"") && wifiName.endsWith("\""))
					wifiName = wifiName.subSequence(1, wifiName.length() - 1).toString();
				return wifiName;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	public static String getGateway (Context context) {
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return NetworkUtil.intToIp(wm.getDhcpInfo().gateway);
	}

	public static String intToIp(int i) {
		return ((i >> 24 ) & 0xFF ) + "." +
				((i >> 16 ) & 0xFF) + "." +
				((i >> 8 ) & 0xFF) + "." +
				( i & 0xFF) ;
	}

	public static void startScan(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
	}

	public static WifiManager getWifiManager(Context context) {
		return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	public static void connectToKnownWifi(Context context, String ssid) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				wifiManager.disconnect();
				wifiManager.enableNetwork(i.networkId, true);
				wifiManager.reconnect();
			}
		}
	}

	public static Boolean connectToWifiAfterDisconnecting(Context context, String ssid) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration wc = new WifiConfiguration();
		wc = new WifiConfiguration();
		wc.SSID = "\"" + ssid + "\"";
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		wifiManager.addNetwork(wc);

		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				wifiManager.enableNetwork(i.networkId, true);
				Boolean flag = wifiManager.reconnect(); 
				return flag;
			}
		}

		return false;
	}

	public static void removeSSIDFromConfiguredNetwork(Context context, String ssid) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);

		List<WifiConfiguration> configuredWifiList = wifiManager.getConfiguredNetworks();
		for (int x = 0; x < configuredWifiList.size(); x++) {
			WifiConfiguration i = configuredWifiList.get(x);
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				Log.w(TAG, "Removing network: " + i.SSID);
				wifiManager.removeNetwork(i.networkId);
				return;
			}
		}
	}

	public static WifiConfiguration getWifiConfigurationWithInfo(Context context, String ssid, SecurityType securityType, String password) {

		WifiManager wifiManager = getWifiManager(context);
		List<WifiConfiguration> configuredWifiList = wifiManager.getConfiguredNetworks();

		if (configuredWifiList == null) {
		return null;

		}
	else {

			for (WifiConfiguration i : configuredWifiList) {
				if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
					Log.i(TAG, "Wifi configuration for " + ssid + " already exist, so we will use it");
					return i;
				}
			}

			Log.i(TAG, "Wifi configuration for " + ssid + " doesn't exist, so we will create new one");
			Log.i(TAG, "SSID: " + ssid);
			Log.i(TAG, "Security: " + securityType);
			WifiConfiguration wc = new WifiConfiguration();

			wc.SSID = "\"" + ssid + "\"";
			wc.status = WifiConfiguration.Status.ENABLED;
			wc.hiddenSSID = false;

			switch (securityType) {
				case OPEN:
					wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
					break;
				case WEP:
					wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
					wc.preSharedKey = "\"" + password + "\"";
					break;
				case WPA1:
					wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
					wc.preSharedKey = "\"" + password + "\"";
					break;
				case WPA2:
					wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
					wc.preSharedKey = "\"" + password + "\"";
					break;
				case UNKNOWN:
					if (password == null) {
						wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
					} else {
						wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
						wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
						wc.preSharedKey = "\"" + password + "\"";
					}
					break;
				default:
					break;
			}

			Log.i(TAG, "New wifi configuration with id " + wifiManager.addNetwork(wc));
			Log.i(TAG, "Saving configuration " + wifiManager.saveConfiguration());
			Log.i(TAG, "wc.networkId " + wc.networkId);

			configuredWifiList = wifiManager.getConfiguredNetworks();
			for (WifiConfiguration i : configuredWifiList) {
				if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
					Log.i(TAG, "Returning wifiConfiguration with id " + i.networkId);
					return i;
				}
			}
		}
		return null;
	}

	public static void connectToWifiWithInfo(Context context, String ssid, SecurityType securityType, String password) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);

		int numberOfOcc = 0;

		List<WifiConfiguration> configuredWifiList = wifiManager.getConfiguredNetworks();
		for (int x = 0; x < configuredWifiList.size(); x++) {
			WifiConfiguration i = configuredWifiList.get(x);
			//System.out.println(i.SSID);
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				numberOfOcc++;
			}
		}

		System.out.println("Done checking doubles: " + numberOfOcc);

		for (WifiConfiguration i : configuredWifiList) {
			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
				Log.i(TAG, "Trying to disconnect (success = " + wifiManager.disconnect() + ")");
				Log.i(TAG, "Trying to connect to " + i.SSID + " (success = " + wifiManager.enableNetwork(i.networkId, true) + ")");
				return;
			}
		}

		WifiConfiguration wc = new WifiConfiguration();

		wc.SSID = "\"" + ssid + "\"";
		wc.status = WifiConfiguration.Status.ENABLED;
		wc.hiddenSSID = false;

		switch (securityType) {
		case OPEN:
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			break;
		case WEP:
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			wc.preSharedKey = "\"" + password + "\"";
			break;
		case WPA1:
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wc.preSharedKey = "\"" + password + "\"";
			break;
		case WPA2:
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wc.preSharedKey = "\"" + password + "\"";
			break;
		case UNKNOWN:
			if (password == null) {
				wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			}
			else {
				wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				wc.preSharedKey = "\"" + password + "\"";
			}
			break;
		default:
			break;
		}

		int res = wifiManager.addNetwork(wc);
		Log.i(TAG, "addnetwork :" +res);
		wifiManager.disconnect();
		wifiManager.enableNetwork(res, true);
		wifiManager.saveConfiguration();

		/*
		if (isLollipopAndUp()) {
			enableNework(ssid, context);
		}
		else {
			wifiManager.enableNetwork(res, true);
		}
		 */

		/*
		if (wifiManager.saveConfiguration() == false) {
			Log.w(TAG, "Failed to save wifi configuration");
		}
		 */
	}

	public static Boolean isLollipopAndUp() {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		return currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP;
	}

	public static SecurityType getScanResultSecurity(ScanResult scanResult) {
		String cap = scanResult != null ? scanResult.capabilities : "";
		SecurityType newState = scanResult != null ? SecurityType.OPEN : SecurityType.UNKNOWN;

		if (cap.contains("WEP"))
			newState = SecurityType.WEP;
		else if (cap.contains("WPA2"))
			newState = SecurityType.WPA2;
		else if (cap.contains("WPA"))
			newState = SecurityType.WPA1;

		return newState;
	}

	public static Boolean addProfile(String baseUrl, SecurityType securityType, String ssid, String password, String priorityString, DeviceVersion version) {

		String url = baseUrl;

		switch (version) {
		case R1:
			url += "/profiles_add.html";
			break;
		case R2:
			url += "/api/1/wlan/profile_add";
			break;
		case UNKNOWN:
			break;
		}

		Boolean flag = false;
		if (securityType == SecurityType.UNKNOWN) {
			if (password.matches("")) {
				flag = NetworkUtil.addProfile(baseUrl, SecurityType.OPEN, ssid, password, priorityString, version);
			}
			else {
				flag = NetworkUtil.addProfile(baseUrl, SecurityType.WEP, ssid, password, priorityString, version);
				flag = flag && NetworkUtil.addProfile(baseUrl, SecurityType.WPA1, ssid, password, priorityString, version);
			}
		}
		else {
			try {
				HttpClient client = new DefaultHttpClient();
				String addProfileUrl = url;
				HttpPost addProfilePost = new HttpPost(addProfileUrl);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
				ssid = new String(ssid.getBytes("UTF-8"), "ISO-8859-1");
				nameValuePairs.add(new BasicNameValuePair("__SL_P_P.A", ssid));
				nameValuePairs.add(new BasicNameValuePair("__SL_P_P.B", String.valueOf(SecurityType.getIntValue(securityType))));
				nameValuePairs.add(new BasicNameValuePair("__SL_P_P.C", password));

				try {
					int priority = Integer.parseInt(priorityString);
					nameValuePairs.add(new BasicNameValuePair("__SL_P_P.D", String.valueOf(priority)));
				} catch (NumberFormatException e) {
					nameValuePairs.add(new BasicNameValuePair("__SL_P_P.D", String.valueOf(0)));
				}

				addProfilePost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				client.execute(addProfilePost);
				flag = true;
			} catch (Exception e) {
				e.printStackTrace();
				flag = false;
			}
		}

		return flag;
	}

	public static Boolean moveStateMachineAfterProfileAddition(String baseUrl, String ssid, DeviceVersion version) {

		String url = baseUrl;

		switch (version) {
		case R1:
			url += "/add_profile.html";
			break;
		case R2:
			url += "/api/1/wlan/confirm_req";
			break;
		case UNKNOWN:
			break;
		}

		Boolean flag = false;
		HttpClient client = new DefaultHttpClient();
		try {
			String stateMachineUrl = url;
			HttpPost stateMachinePost = new HttpPost(stateMachineUrl);

			switch (version) {
			case R1:
				List<NameValuePair> stateParam = new ArrayList<NameValuePair>(1);
				stateParam.add(new BasicNameValuePair("__SL_P_UAN", ssid));
				stateMachinePost.setEntity(new UrlEncodedFormEntity(stateParam));
				break;
			case R2:
				break;
			case UNKNOWN:
				break;
			}

			client.execute(stateMachinePost);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}

		return flag;
	}


	public static DeviceVersion getSLVersion(String baseUrl) {
		
		String url = baseUrl + "/param_product_version.txt";
		DeviceVersion version = DeviceVersion.UNKNOWN;

		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
//			HttpClient client = new DefaultHttpClient();
			HttpGet slResult = new HttpGet(url);

			HttpResponse response = client.execute(slResult);
			String html = EntityUtils.toString(response.getEntity());

			if ( html.equals("R1.0") ||	html.contains("1.0")) {
				version = DeviceVersion.R1;
			}
			else if (html.equals("R2.0") || html.equals("2.0") ||	html.contains("2.0") ) {
				version = DeviceVersion.R2;
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return version;
	}

	public static String getDeviceName(String baseUrl, DeviceVersion version) {

			String deviceName = "";
			String url = baseUrl;

			switch (version) {
				case R1:
					url += "/param_device_name.txt";
					break;
				case R2:
					url += "/param_device_name.txt";
					break;
		case UNKNOWN:
			break;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
//			HttpClient client = new DefaultHttpClient();
			HttpGet slResult = new HttpGet(url);

			HttpResponse response = client.execute(slResult);
			String name = EntityUtils.toString(response.getEntity());
			deviceName = name;
		} catch (Exception e) {
			Log.e(TAG, "Failed to fetch device name from board");
		}

		return deviceName;
	}

	public static ArrayList<String> getSSIDListFromDevice(String baseUrl, DeviceVersion version) throws ClientProtocolException, IOException, UnknownHostException  {

		switch (version) {
		case R1:
			break;
		case R2:
			break;
		case UNKNOWN:
			break;
		}

		String url = baseUrl + "/netlist.txt";

		ArrayList<String> list = new ArrayList<String>();
		
		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
//			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			String responseString = EntityUtils.toString(response.getEntity());

			String[] names = responseString.split(";");
			for (String name : names) {
				if (!name.equals("X"))
					list.add(name);
			}
		} catch (Exception e) {
			return null;
		}

		return list;
	}

	public static Boolean rescanNetworksOnDevice(String url, DeviceVersion version)   {

		Boolean flag = false;
		HttpClient client = new DefaultHttpClient();
		List<NameValuePair> stateParam = null;
		String rescanUrl = url;

		switch (version) {
		case R1:
			stateParam = new ArrayList<NameValuePair>(1);

			try {
				HttpPost rescanPost = new HttpPost(rescanUrl);
				stateParam.add(new BasicNameValuePair("__SL_P_UFS", "just empty information"));
				rescanPost.setEntity(new UrlEncodedFormEntity(stateParam));
				client.execute(rescanPost);
				flag = true;
			} catch (Exception e) {
				flag = false;
			}

			break;
		case R2:
			stateParam = new ArrayList<NameValuePair>(2);
			rescanUrl += "/api/1/wlan/en_ap_scan";

			try {
				HttpPost rescanPost = new HttpPost(rescanUrl);
				stateParam.add(new BasicNameValuePair("__SL_P_SC1", "10"));
				stateParam.add(new BasicNameValuePair("__SL_P_SC2", "1"));
				rescanPost.setEntity(new UrlEncodedFormEntity(stateParam));
				client.execute(rescanPost);
				flag = true;
			} catch (Exception e) {
				flag = false;
			}

			break;
		case UNKNOWN:
			break;
		}

		return flag;
	}

	public static Boolean setNewDeviceName(String newName, String baseUrl, DeviceVersion version) {

		String url = baseUrl;

		switch (version) {
		case R1:
			url += "/mode_config";
			break;
		case R2:
			url += "/api/1/netapp/set_urn";
			break;
		case UNKNOWN:
			break;
		}

		Boolean flag = false;
		HttpClient client = new DefaultHttpClient();
		try {
			String stateMachineUrl = url;
			HttpPost rescanPost = new HttpPost(stateMachineUrl);
			List<NameValuePair> stateParam = new ArrayList<NameValuePair>(1);

			newName = new String(newName.getBytes("UTF-8"), "ISO-8859-1");
			stateParam.add(new BasicNameValuePair("__SL_P_S.B", newName));

			rescanPost.setEntity(new UrlEncodedFormEntity(stateParam));
			client.execute(rescanPost);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}

		return flag;
	}


	public static Boolean setIotUuid(String newName, String baseUrl) {

		String url = baseUrl;
		url += "/api/1/iotlink/uuid";

		if (newName.equals(""))
			return false;

		Boolean flag = false;
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 1000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 1000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		HttpClient client = new DefaultHttpClient(httpParameters);
		try {
			String stateMachineUrl = url;
			HttpPost rescanPost = new HttpPost(stateMachineUrl);
			List<NameValuePair> stateParam = new ArrayList<NameValuePair>(1);

			newName = new String(newName.getBytes("UTF-8"), "ISO-8859-1");
			stateParam.add(new BasicNameValuePair("uuid", newName));

			rescanPost.setEntity(new UrlEncodedFormEntity(stateParam));
			HttpResponse response = client.execute(rescanPost);
			if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK)
			{
				flag = true;
			}
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();
			flag = false;
		}

		return flag;
	}


	public static String getCGFResultFromDevice(String baseUrl, DeviceVersion version) {

		String url = baseUrl + "/param_cfg_result.txt";
		String result = "Timeout";

		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			HttpGet cfgResult = new HttpGet(url);
			HttpResponse response = client.execute(cfgResult);
			result = EntityUtils.toString(response.getEntity());

			if (result.equals("")) {
				Log.w(TAG, "CFG result returned empty!");
				result = "Timeout";
			}
			else {
				Log.i(TAG, "CFG result returned: " + result);
			}

		} catch (Exception e) {
			Log.e(TAG, "Failed to get CFG result: " + e.getMessage());
			result = "Timeout";
		}

		return result;
	}

	/*
	public static void setMobileDataState(boolean mobileDataEnabled, Context context) {
	    try
	    {
	        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
	        if (null != setMobileDataEnabledMethod) {
	            setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
	        }
	    }
	    catch (Exception ex){
	        Log.e(TAG, "Error setting mobile data state", ex);
	    }
	}

	public static boolean getMobileDataState(Context context) {
	    try {
	        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
	        if (null != getMobileDataEnabledMethod){
	            boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
	            return mobileDataEnabled;
	        }
	    }
	    catch (Exception ex){
	        Log.e(TAG, "Error getting mobile data state", ex);
	    }

	    return false;
	}
	 */
	
	/*
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void registerNetworkCallback(final ConnectivityManager cm) {
		/*NetworkRequest.Builder request = new NetworkRequest.Builder();
		
		Network[] array = cm.getAllNetworks();
		for (Network network : array) {
			NetworkInfo info = cm.getNetworkInfo(network);
			Log.i(TAG, "Network: " + network + "\nInfo: " + info);
		}
		
		request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

		cm.requestNetwork(request.build(), new NetworkCallback() {
			@Override
	        public void onAvailable(Network network) {
	            super.onAvailable(network);
	            NetworkInfo info = cm.getNetworkInfo(network);
	            mLogger.info("onAvailable, network:" + network + "\nInfo:" + info);
	            //ConnectivityManager.setProcessDefaultNetwork(network);
	        }

	        @Override
	        public void onLosing(Network network, int maxMsToLive) {
	            super.onLosing(network, maxMsToLive);
	        }

	        @Override
	        public void onLost(Network network) {
	            super.onLost(network);
	        }

	        @Override
	        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
	            super.onCapabilitiesChanged(network, networkCapabilities);
	        }

	        @Override
	        public void onLinkPropertiesChanged(Network network,
	                LinkProperties linkProperties) {
	            super.onLinkPropertiesChanged(network, linkProperties);
	        }
		});
	}
	*/
}
