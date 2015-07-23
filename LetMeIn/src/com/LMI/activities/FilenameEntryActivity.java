package com.LMI.activities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.LMI.tools.Protocol;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Once a user has authenticated, this class allows them to query the server for
 * a file to display, or logout if they choose. While logged in, the user can
 * read as many files as they wish, one at a time.
 * @author Jessie McGarry
 *
 */
public class FilenameEntryActivity extends Activity implements OnClickListener {
	  private Button getFile;
	  private Button logout;
	  protected static String fileContents;
	  Socket clientSocket;
	  DataOutputStream outToServer;
	  BufferedReader inFromServer;
	  
	  public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.filename_entry);
	        getFile = (Button) findViewById(R.id.button1);
	        getFile.setOnClickListener(this);
	        
	        logout = (Button) findViewById(R.id.button2);
	        logout.setOnClickListener(this);
	        
	        clientSocket = HomeActivity.clientSocket;
			outToServer = HomeActivity.outToServer;
			inFromServer = HomeActivity.inFromServer;
	        
	  }
	  
	  /**
	   * If the user hits the back button, he or she is logged out.
	   */
	  public boolean onKeyDown(int keyCode, KeyEvent event)  {
		    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		    	onClick(logout);
	        return super.onKeyDown(keyCode, event);
	  }
	  
	  /**
	   * Listener for all buttons in this activity
	   */
	  public void onClick(View v) {
			switch(v.getId()) {
			
			//user hit the "view file contents" button
			case R.id.button1: 
				Editable filename = ((EditText)findViewById(R.id.filename)).
																	  getText();
				//empty filename field
				if (filename.toString().equals("")) {
					Toast.makeText(FilenameEntryActivity.this, 
							R.string.empty_field, Toast.LENGTH_LONG).show();
					break;
				}
				
				try {
					//make the file request
					outToServer.writeBytes(Protocol.FILE_REQ + " " + filename.toString());
					
					//read the file contents, EOM means "end of message"
					String line = "";
					fileContents = "";
					while(!line.equals(Protocol.EOM) ) {
						fileContents += line;
						line = inFromServer.readLine();
						Log.v("test", line);
					}
					
					//invalid filename
					if (fileContents.equals(Protocol.FILE_NOT_FOUND)) {
						Toast.makeText(FilenameEntryActivity.this, 
							  R.string.file_not_found,Toast.LENGTH_LONG).show();
						break; 
					}
					
					//file is empty
					else if (fileContents.equals(Protocol.EMPTY_FILE)) {
						Toast.makeText(FilenameEntryActivity.this,
								R.string.empty_file, Toast.LENGTH_LONG).show();
						break;
					}
								
				} catch (IOException e) { 
					e.printStackTrace();
				}
			    
				/* if we have valid file contents, we'll switch to another 
				 activity to show them */
				Intent intent = new Intent(this, FileContentsActivity.class);
				startActivity(intent);
				break;
			
			//user hit the logout button
			case R.id.button2: 
				try {
					//tell the server to stop listening on this stream
					outToServer.writeBytes(Protocol.CLOSE);
					
					//close the socket on our end
					clientSocket.close();
					
					//tell the user they have been logged out
					Toast.makeText(FilenameEntryActivity.this, 
						R.string.logout_msg, Toast.LENGTH_LONG).show();
					
					//close the activity
					finish();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} //end switch
	  } //end OnClick()
}
