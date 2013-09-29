package pku.shengbin.ipgateway;

import java.io.UnsupportedEncodingException;

import pku.shengbin.utils.HttpAccessor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
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
    
    /*
    https://its.pku.edu.cn:5428/ipgatewayofpku?uid=1101111141&password=pas&operation=connect&range=2&timeout=2
    operation: connect, disconnect, disconnectall
    range: 1(fee), 2(free)
    */
    public void doLogin() {
	 
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo == null || networkInfo.isConnected() == false) {
	    	Toast.makeText(MainActivity.this, "No Connection!", Toast.LENGTH_SHORT).show();
			return ;
	    }
    	String username = editUser.getText().toString();
    	String password = editPass.getText().toString();
    	
	    if (username.isEmpty() || password.isEmpty()) {
	    	String message = "Input username and password!";
	        messageArea.loadData(message, "text/html; charset=UTF-8", null);

			return ;
	    }
	    
	    String message = "Logging in...";
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
				content = new String(byteArray, "GBK");
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
        	
        	int start = result.indexOf("<table");
        	int end = result.lastIndexOf("</table>") + 8;
        	String table = result.substring(start, end);
        	
        	String message = table;
	        messageArea.loadData(message, "text/html; charset=UTF-8", null);
	        
	        login = true;
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
				content = new String(byteArray, "GBK");
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
        	
        	int start = result.indexOf("<table");
        	int end = result.lastIndexOf("</table>") + 8;
        	String table = result.substring(start, end);
        	
        	String message = table;
	        messageArea.loadData(message, "text/html; charset=UTF-8", null);
            
            login = false;
       }
    } 
    
}
