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

import com.honeywell.iaq.smartlink.model.HostBean;
import com.honeywell.iaq.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpBcastServer extends AsyncTask<Void, HostBean, Boolean> {
    private final String TAG = "UdpBcast";
    public boolean working = false;
    DatagramSocket serverSocket1;
    int portNumber1 = 1501;
    byte[] receiveData = new byte[1024];
    private PingCallback mPingCallback;

    interface PingCallback {
        void pingDeviceFetched(String serial, String token);
    }

    public UdpBcastServer(PingCallback callBack) {
        mPingCallback = callBack;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        working = false;
        serverSocket1.disconnect();
        serverSocket1.close();
        super.onPostExecute(result);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            if( serverSocket1 == null ) {
                serverSocket1 = new DatagramSocket(portNumber1);
            }
        } catch (SocketException e) {
            Log.i(TAG, "socket error" + e.getMessage());
        }

        working = true;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        working = true;
        Log.i(TAG, "doInBackground");
        UdpReceive();
        return true;
    }


    public void UdpReceive() {
            while(working) {

                if(serverSocket1 != null) {
                    DatagramPacket recPacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        serverSocket1.receive(recPacket);
                    }
                    catch(IOException e) {
                        Log.i(TAG, "IO error");
                        working = false;
                    }
                    String dataAsString = new String(recPacket.getData(),0,recPacket.getLength());
                    if ( dataAsString.length() == 0 ) {
                        continue;
                    }

                    // {"age":0,"host":"192.168.43.235","serial":"0010000xx000000000001"}
                    try {
                        JSONObject jsonObject = new JSONObject(dataAsString);
                        String serial = jsonObject.getString("serial");
                        String token = jsonObject.getString("token");
                        mPingCallback.pingDeviceFetched(serial, token);
                        working = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


//                    String deviceName = new String(dataAsString);
//                    String deviceAddress = new String(dataAsString);
//                    if(deviceName != null && !deviceName.equalsIgnoreCase("")) {
//                        deviceName = deviceName.split(",")[1];
//                    }
//                    if(deviceAddress!=null && !deviceAddress.equalsIgnoreCase("") ) {
//                        deviceAddress = deviceAddress.split(",")[0];
//                    }
//                    Log.e(TAG, "Received name: " + deviceName + " Received address: " + deviceAddress );
//                    InetAddress IPAddress = recPacket.getAddress();
//                    Log.i(TAG, "Received from " + IPAddress.toString().split("/")[1]);

//                    if (!deviceName.equalsIgnoreCase("") && !deviceAddress.equalsIgnoreCase("") ) {
//                        JSONObject deviceJSON = new JSONObject();
//                        try {
//                            deviceJSON.put("name", deviceName);
//                            deviceJSON.put("host", deviceAddress);
//                            deviceJSON.put("age", 0);
//                            Log.e(TAG, "Bcast publishing device found to application,  name: " + deviceName);
//                            mPingCallback.pingDeviceFetched(deviceJSON);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }

                }

            }
    }



}
