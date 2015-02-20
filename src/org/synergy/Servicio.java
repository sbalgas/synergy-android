/*
	Servicio
	Creado por Leandro G. Ortega

*/

package org.synergy;

import org.synergy.base.Event;
import org.synergy.base.EventQueue;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.client.Client;
import org.synergy.common.screens.BasicScreen;
import org.synergy.injection.Injection;
import org.synergy.net.NetworkAddress;
import org.synergy.net.SocketFactoryInterface;
import org.synergy.net.TCPSocketFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.view.Display;
import android.widget.Toast;


public class Servicio extends Service {

	public static String clientName;
    public static String ipAddress;
    public static int port;
    public static String deviceName;

    private static Client client;
    
	private static Servicio instance  = null;
	
	private Thread mainLoopThread = null;
	
	public static boolean isRunning() { 
	      return instance != null; 
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	

	@Override
	public void onCreate() {
		Toast.makeText(getApplicationContext(), "Servicio Synergy iniciado.", Toast.LENGTH_LONG).show();
		System.out.println( "Servicio Synergy iniciado");
		instance=this;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "Servicio Synergy destruido", Toast.LENGTH_LONG).show();
		System.out.println( "Servicio Synergy destruido");
		disconnect();
		instance = null;
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(getApplicationContext(), "Servicio Synergy iniciado!!", Toast.LENGTH_LONG).show();
		System.out.println( "Servicio Synergy iniciado");

		conectar();
		ScreenOn();
		lanzarNotificacion();

	}
	
	private class MainLoopThread extends Thread {
		
		public void run () {
			try {
		        Event event = new Event ();
		        event = EventQueue.getInstance ().getEvent (event, -1.0);
		        Log.note ("Event grabbed");
		        while (event.getType () != EventType.QUIT && mainLoopThread == Thread.currentThread()) {
		            EventQueue.getInstance ().dispatchEvent (event);
		            // TODO event.deleteData ();
		            event = EventQueue.getInstance ().getEvent (event, -1.0);
		            Log.note ("Event grabbed");
		        } 
				mainLoopThread = null;
			} catch (Exception e) {
				e.printStackTrace ();
			} finally {
				Injection.stop();
			}
		}
	}

	private void disconnect(){
		mainLoopThread.stop();
		mainLoopThread = null;
		client.disconnect(null);
	}
	
	private void conectar() {

		try {
        	SocketFactoryInterface socketFactory = new TCPSocketFactory();
       	   	NetworkAddress serverAddress = new NetworkAddress (ipAddress, port);
        	serverAddress.resolve ();

        	Injection.startInjection(deviceName);

        	BasicScreen basicScreen = new BasicScreen();
        	
        	
        	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    		
   		 	display.getWidth(); // to get width of the screen
   		 	display.getHeight(); // to get height of the Screen
   		 
   		  	basicScreen.setShape (display.getWidth (), display.getHeight ());
        	
        	
        	//PlatformIndependentScreen screen = new PlatformIndependentScreen(basicScreen);
            
            Log.debug ("Hostname: " + clientName);
            
			client = new Client (getApplicationContext(), clientName, serverAddress, socketFactory, null, basicScreen);
			client.connect ();

			if (mainLoopThread == null) {
				mainLoopThread = new MainLoopThread();
				mainLoopThread.start();
			}
			
        } catch (Exception e) {
        	e.printStackTrace();
        	//((EditText) findViewById (R.id.outputEditText)).setText("Connection Failed.");
        }
	}

	void ScreenOn(){
		
        
	}
	
	void lanzarNotificacion(){
		 String ns = Context.NOTIFICATION_SERVICE;
		 NotificationManager notManager = (NotificationManager) getSystemService(ns);
		
		 //Configuramos la notificacion
		 Notification notif = new Notification(android.R.drawable.presence_online, "Synergy", System.currentTimeMillis());
		
		 //Configuramos el Intent
		 Context contexto = Servicio.this;
		 CharSequence titulo = "Servicio de Synergy";
		 CharSequence descripcion = "Synergy esta corriendo en este momento.";
		
		 //Intent que se abrira al clickear la notificacion
		 Intent resultIntent =new Intent(this, Synergy.class);;
		 
		 PendingIntent contIntent = PendingIntent.getActivity(contexto, 0, resultIntent, 0);
		 notif.setLatestEventInfo(contexto, titulo, descripcion, contIntent);
		 notif.flags |= Notification.FLAG_AUTO_CANCEL;
		 notif.defaults |= Notification.DEFAULT_VIBRATE;
		 notManager.notify(1, notif);
	 }

}
