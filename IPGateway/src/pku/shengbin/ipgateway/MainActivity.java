package pku.shengbin.ipgateway;

import java.io.UnsupportedEncodingException;

import pku.shengbin.utils.HttpAccessor;
import pku.shengbin.utils.MessageBox;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private WebView messageArea = null;
	private EditText editUser, editPass;
	private boolean login = false;
	private boolean disconnectAll = false;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        messageArea = (WebView) this.findViewById(R.id.webView_message);
        String message = this.getString(R.string.welcome);
        messageArea.loadData(message, "text/html; charset=UTF-8", null);
      
    	editUser = (EditText) this.findViewById(R.id.editText_user);
    	editPass = (EditText) this.findViewById(R.id.editText_pass);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String user = settings.getString("username", "");
        String pass = settings.getString("password", "");
    	editUser.setText(user);
    	editPass.setText(pass);
    	
    	// set the check box if there exists remembered data
    	if (user.isEmpty() || pass.isEmpty()) {
    		((CheckBox) MainActivity.this.findViewById(R.id.checkBox_remember)).setChecked(false);
    	} else {
    		((CheckBox) MainActivity.this.findViewById(R.id.checkBox_remember)).setChecked(true);
    	}

        // add the button listener
        Button loginButton = (Button) this.findViewById(R.id.button_login);
        loginButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				doLogin();
			}
        });
        Button logoutButton = (Button) this.findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				doLogout();
			}
        });
    }

    

    @Override
	protected void onResume() {
		super.onResume();
		boolean autoLogin = true;
		if (autoLogin) {
			doLogin();
		}
	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
    	if (item.getItemId() == R.id.action_about) {
    		MessageBox.show(this, this.getString(R.string.about_ipgateway), this.getString(R.string.about_message));
    	}
		return super.onContextItemSelected(item);
	}



	/*
    https://its.pku.edu.cn:5428/ipgatewayofpku?uid=1101111141&password=pas&operation=connect&range=2&timeout=2
    operation: connect, disconnect, disconnectall
    range: 1(fee), 2(free)
    */
    public void doLogin() {
	 
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo == null || networkInfo.isConnected() == false) {
	    	Toast.makeText(MainActivity.this, this.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			return ;
	    }
    	String username = editUser.getText().toString();
    	String password = editPass.getText().toString();
    	
	    if (username.isEmpty() || password.isEmpty()) {
	    	String message = this.getString(R.string.please_input);
	        messageArea.loadData(message, "text/html; charset=UTF-8", null);

			return ;
	    }
	    
	    String message = this.getString(R.string.logging_in);
        messageArea.loadData(message, "text/html; charset=UTF-8", null);
	    
	    int range = 2;
	    if (((CheckBox) MainActivity.this.findViewById(R.id.checkBox_global)).isChecked()) {
	    	range = 1;
	    }
    	String loginUrl = "https://its.pku.edu.cn:5428/ipgatewayofpku?uid=" + username + "&password=" + password + "&operation=connect&range=" + range + "&timeout=2";
	    new IPGatewayLoginTask().execute(loginUrl);
	    
    }
    
    private class IPGatewayLoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {   
            // params comes from the execute() call: params[0] is the url.
            byte[] byteArray = HttpAccessor.getContentBytesFromUrl(urls[0]);
            String content = "";
            try {
            	if (byteArray != null) {
    				content = new String(byteArray, "GBK");
            	}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return content;

        }
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	
        	boolean remember = ((CheckBox) MainActivity.this.findViewById(R.id.checkBox_remember)).isChecked();
        	if (remember) {
	        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.putString("username", editUser.getText().toString());
	        	editor.putString("password", editPass.getText().toString());
	        	editor.commit();
        	}
        	
        	String message = "Something wrong! Sorry.";
        	if (result.isEmpty()) {
        		message = "No data returned!";
        	} else {
        		int start = result.indexOf("<!--IPGWCLIENT_START ") + "<!--IPGWCLIENT_START ".length();
        		int end = result.indexOf("<IPGWCLIENT_END-->");
        		if (end != -1) {
            		String information = result.substring(start, end);
        			String success = information.substring(0, 10);
        			if (success.equals("SUCCESS=YES")) {
        				login = true;
        			} else if (information.substring(information.indexOf("REASON=")).contains("连接数超过")){
        				DialogInterface.OnClickListener ok_listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								disconnectAll = true;
							}
        				};
        				DialogInterface.OnClickListener cancel_listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								disconnectAll = false;
							}
        				};
        				MessageBox.show(MainActivity.this, "提示", "你已经达到最大连接数。是否断开其他连接并在此终端上重新连接？", ok_listener, cancel_listener);
        			}
        			
        			start = result.indexOf("<table");
                	end = result.lastIndexOf("</table>") + "</table>".length();
                	if (start != -1) {
                		String table = result.substring(start, end);
                		message = table;
                	}
        			
        		} else {
        			message = "No information returned!";
        		}
        	}     
        	
        	if (disconnectAll) {
				String username = editUser.getText().toString();
		    	String password = editPass.getText().toString();
		    	String disconnectAllUrl = "https://its.pku.edu.cn:5428/ipgatewayofpku?uid=" + username + "&password=" + password + "&operation=disconnectall&range=2&timeout=2";
		    	byte[] byteArray = HttpAccessor.getContentBytesFromUrl(disconnectAllUrl);
	            result = "";
	            try {
	            	if (byteArray != null) {
	    				result = new String(byteArray, "GBK");
	            	}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	            
	            message = "Something wrong! Sorry.";
	        	if (result.isEmpty()) {
	        		message = "No data returned!";
	        	} else {
	        		int start = result.indexOf("<!--IPGWCLIENT_START ");
	        		if (start != -1) {
	        			String success = result.substring(start, start + 32);
	        			if (success.equals("SUCCESS=YES")) {
	        				doLogin();
	        				disconnectAll = false;
	        				return;
	        			}
	        			start = result.indexOf("<table");
	                	int end = result.lastIndexOf("</table>") + 8;
	                	if (start != -1) {
	                		String table = result.substring(start, end);
	                		message = table;
	                	}
	        		} else {
	        			message = "No information returned!";
	        		}
	        	}
		        messageArea.loadData(message, "text/html; charset=UTF-8", null);
				disconnectAll = false;
			} else {
				messageArea.loadData(message, "text/html; charset=UTF-8", null);
			}
       }
    }
    
    public void doLogout() {
    	
    	if (login == false) {
    	    String message = "No Successful Login!";
            messageArea.loadData(message, "text/html; charset=UTF-8", null);
			return ;
    	}
    	
    	String message = "Logging out...";
        messageArea.loadData(message, "text/html; charset=UTF-8", null);

    	String username = editUser.getText().toString();
    	String password = editPass.getText().toString();
    	
    	int range = 2;
	    if (((CheckBox) MainActivity.this.findViewById(R.id.checkBox_global)).isChecked()) {
	    	range = 1;
	    }
    	String logoutUrl = "https://its.pku.edu.cn:5428/ipgatewayofpku?uid=" + username + "&password=" + password + "&operation=disconnect&range=" + range + "&timeout=2";
    	new IPGatewayLogoutTask().execute(logoutUrl);
    }
    
    private class IPGatewayLogoutTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {   
            byte[] byteArray = HttpAccessor.getContentBytesFromUrl(urls[0]);
            String content = "";
            try {
            	if (byteArray != null) {
    				content = new String(byteArray, "GBK");
            	}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
            return content;
        }
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	
        	boolean remember = ((CheckBox) MainActivity.this.findViewById(R.id.checkBox_remember)).isChecked();
        	if (!remember) {
	        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.remove("username");
	        	editor.remove("password");
	        	editor.commit();
	            editUser.setText("");
	            editPass.setText("");
        	}
        	
        	String message = "Something wrong! Sorry.";
        	if (result.isEmpty()) {
        		message = "No data returned!";
        	} else {
        		int start = result.indexOf("<!--IPGWCLIENT_START ");
        		if (start != -1) {
        			String success = result.substring(start, start + 32);
        			if (success.equals("SUCCESS=YES")) {
        				login = false;
        			}
        			
        			start = result.indexOf("<table");
                	int end = result.lastIndexOf("</table>") + 8;
                	if (start != -1) {
                		String table = result.substring(start, end);
                		message = table;
                	}
        			
        		} else {
        			message = "No information returned!";
        		}
        	}     
        	
	        messageArea.loadData(message, "text/html; charset=UTF-8", null);
       }
    } 
    
}
