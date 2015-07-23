package com.LMI.tools;

/**
 * This class is the protocol that the client and server use to communicate.
 * These string represent the base commands, additional parameters are passed in
 * the form "command<sp>param<sp>param ..."
 * @author Jessie
 *
 */
public class Protocol {
		public static final String 
			AUTH_REQ = "requesting_authentication", 				 
			AUTH_SUCCESS = "successful_login",				   
			AUTH_BAD_USR = "user_not_found",
			AUTH_BAD_PASS = "incorrect_password",
			FILE_REQ = "requesting_file_contents", 
			EMPTY_FILE ="empty_file", 
			FILE_NOT_FOUND ="file_not_found",          
			CLOSE = "close",	                 
			EOM = "end_of_message";
}


