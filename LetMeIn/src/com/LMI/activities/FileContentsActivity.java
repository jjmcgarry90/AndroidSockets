package com.LMI.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class simply displays file contents retrieved from the server during the
 * FilenameEntryActivity
 * @author Jessie McGarry
 */
public class FileContentsActivity extends Activity {
	
	  /**
	   * Called when the activity is first created. Shows the contents in a 
	   * scroll-able form.
	   */
	  public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.file_content);
	        TextView contents = ((TextView)findViewById(R.id.textView1));
	        contents.setText(FilenameEntryActivity.fileContents);
	    }
}
