package com.LMI.activities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.LMI.tools.Protocol;




//import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This class is responsible for authenticating the user on the server. It checks
 * input from the user, sets up a socket if the input is valid, and then attempts
 * to authenticate using the username and password provided. The status of the 
 * authentication is then shown. Upon success, the activity is switched to the
 * FileNameEntryActivity.
 * @author Jessie McGarry
 *
 */
public class HomeActivity extends Activity implements OnClickListener {
	private Button login;
	
	//static so they can be referenced from other activities
	protected static Socket clientSocket;
	
	protected static DataOutputStream outToServer; //messages sent to server
	protected static BufferedReader inFromServer; //messages received from server
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        login = (Button)findViewById(R.id.button1);
        login.setOnClickListener(this); //link login button to listener

    }
    
	public void onClick(View v) {
		switch(v.getId()) {
		
		//user hit the login button
		case R.id.button1:
			String fromServer = "";
			String toServer = "";
			String toHash= "";
			Editable serverIP = ((EditText)findViewById(R.id.editText1)).getText();
			Editable serverPort = ((EditText)findViewById(R.id.editText2)).getText();
			Editable username = ((EditText)findViewById(R.id.editText3)).getText();
			Editable password = ((EditText)findViewById(R.id.editText4)).getText();
			
			// start input checking
			if (serverIP.toString().equals("") || 
				serverPort.toString().equals("") || 
				username.toString().equals("")||
				password.toString().equals("")) {
				Toast.makeText(HomeActivity.this, R.string.empty_field,
						Toast.LENGTH_LONG).show();
				break;
			}
			
			if (username.toString().length() > 20) {
				Toast.makeText(HomeActivity.this, R.string.long_name,
						Toast.LENGTH_LONG).show();
				break;
			}
			
			if (password.toString().length() > 20) {
				Toast.makeText(HomeActivity.this, R.string.long_pass,
						Toast.LENGTH_LONG).show();
				break;
			}
			// end input checking			
			
			/* Try to connect to the server and authenticate. A lot can go wrong
			 * here, hence the massive try block.
			 */
			try {
				InetSocketAddress socketAddr = new InetSocketAddress(serverIP.
						   toString(), Integer.parseInt(serverPort.toString()));
				
				
				clientSocket = new Socket();
				
				//if no connection in 3 seconds, throws exception
				clientSocket.connect(socketAddr, 3000); 
				
				outToServer = new DataOutputStream(
												clientSocket.getOutputStream());
					   
				inFromServer = new BufferedReader(
						  new InputStreamReader(clientSocket.getInputStream()));
				
				//send authentication request to server
				outToServer.writeBytes(Protocol.AUTH_REQ);
				
				//get random 64-char string from server
				fromServer = inFromServer.readLine();
				
				//MD5 hash username + password + string
				MessageDigest md = MessageDigest.getInstance("MD5");
				toHash = username.toString() + password.toString() + fromServer;
				byte[] mdbytes = md.digest(toHash.getBytes());
				
				//get hash string from digest
				StringBuffer sb = new StringBuffer();
			    for (int i = 0; i < mdbytes.length; i++) 
			          sb.append(Integer.toString((mdbytes[i] & 0xff) + 
			        		  						   0x100, 16).substring(1));
				
			    //send username:hash to the server
				toServer = username.toString() + ":" + sb.toString();
				Log.v("test", toServer); 
				outToServer.writeBytes(toServer);
				
				//get the authentication response from the server
				fromServer = inFromServer.readLine(); 
				
			} catch (IOException e) {
				Toast.makeText(HomeActivity.this, R.string.connect_error,
						Toast.LENGTH_LONG).show();
				break;
			
		    //this exception should never be thrown
			} catch (NoSuchAlgorithmException e) {
				break;
			}
			
			catch (IllegalArgumentException e) {
				Toast.makeText(HomeActivity.this, R.string.connect_error,
						Toast.LENGTH_LONG).show();
				break;
			}
		
			
			//if authenticated, go to filename activity
			if(fromServer.equals(Protocol.AUTH_SUCCESS)) {
				Intent intent = new Intent(this, FilenameEntryActivity.class);
				startActivity(intent);
			}
			
			/* otherwise, show the authentication failure message from server.
			the user can try to login as many times as they want (for now) */
			else {
				Toast.makeText(HomeActivity.this, R.string.auth_fail,
						Toast.LENGTH_LONG).show();
			}
		break;	
				

		}
		
	}
}