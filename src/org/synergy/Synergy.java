/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy;

import org.synergy.base.Log;
//import org.synergy.common.screens.PlatformIndependentScreen;
import org.synergy.injection.Injection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Intent;

public class Synergy extends Activity {
	
	private final static String PROP_clientName = "clientName";
	private final static String PROP_serverHost = "serverHost";
	private final static String PROP_deviceName = "deviceName";
	
	private static Boolean DebugMode = false;
	private static EditText DebugText;
	LinearLayout Debug_Parent;
	
	static {
		System.loadLibrary ("synergy-jni");
	}
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String clientName = preferences.getString(PROP_clientName, null);
        if (clientName != null) {
        	((EditText) findViewById (R.id.clientNameEditText)).setText(clientName);
        }
    	String serverHost = preferences.getString(PROP_serverHost, null);
        if (serverHost != null) {
        	((EditText) findViewById (R.id.serverHostEditText)).setText(serverHost);
        }
        
        final Button connectButton = (Button) findViewById (R.id.connectButton);    
        connectButton.setOnClickListener (new View.OnClickListener() {
			public void onClick (View arg) {
				dis_connect();
			}
		});
        
        Debug_Parent = (LinearLayout) findViewById(R.id.Debug_Parent_Control);
        
        if (DebugMode) Debug_Parent.setVisibility(View.VISIBLE);
        else Debug_Parent.setVisibility(View.INVISIBLE);
        
        CheckBox ed_checkbox = (CheckBox) findViewById (R.id.enable_debug_chekbox);
        ed_checkbox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
        	@Override
        	public void onCheckedChanged( CompoundButton arg0, boolean arg1) {
        		if (arg1){
        			DebugMode = true;
        			Debug_Parent.setVisibility(View.VISIBLE);
        		}else{
        			DebugMode = false;
       				Debug_Parent.setVisibility(View.INVISIBLE);
        		}
        	}
        });

        
        
        DebugText = ((EditText) findViewById (R.id.outputEditText));
        
        if(!Servicio.isRunning()) connectButton.setText("Connect");
        else connectButton.setText("Disconnect");
        
        Log.setLogLevel (Log.Level.INFO);
        
        Log.debug ("Client starting....");

        try {
			Injection.setPermissionsForInputDevice();
		} catch (Exception e) {
			// TODO handle exception
		}
    }
    
    private void dis_connect() {
    	
        String clientName = ((EditText) findViewById (R.id.clientNameEditText)).getText().toString();
        String ipAddress = ((EditText) findViewById (R.id.serverHostEditText)).getText().toString();
        String portStr = ((EditText) findViewById(R.id.serverPortEditText)).getText().toString();
        int port = Integer.parseInt(portStr);
        String deviceName = ((EditText) findViewById(R.id.inputDeviceEditText)).getText().toString();
    	
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(PROP_clientName, clientName);
        preferencesEditor.putString(PROP_serverHost, ipAddress);
        preferencesEditor.putString(PROP_deviceName, deviceName);
        preferencesEditor.commit();

        Intent in = new Intent(Synergy.this,Servicio.class);
    	final Button connectButton = (Button) findViewById (R.id.connectButton);
    	
    	if(!Servicio.isRunning()){
    		connectButton.setText("Disconnect");
    		Servicio.clientName=clientName;
            Servicio.ipAddress=ipAddress;
            Servicio.port=port;
            Servicio.deviceName=deviceName;

    		
            Synergy.this.startService(in);
    	} else {
    		// TODO desconectar
    		connectButton.setText("Connect");
    		//show_when_locked
    		

    		
        	Synergy.this.stopService(in);
        	System.exit(0);
		}
    	
    }
    
    public static void debug_text(String Mensaje){
    	
    	if (DebugMode)
    		DebugText.setText(	DebugText.getText().toString() + Mensaje + " \n " );
    	
    }

}
