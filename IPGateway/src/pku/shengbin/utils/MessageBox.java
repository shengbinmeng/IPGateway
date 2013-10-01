package pku.shengbin.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageBox {

	public static void show(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setMessage(msg).setTitle(title)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, null)
				.show();
	}
	
	public static void show(Context context, String title, String msg,
			DialogInterface.OnClickListener ok_listener) {
		new AlertDialog.Builder(context).setMessage(msg).setTitle(title)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, ok_listener)
				.show();
	}
	
	public static void show(Context context, String title, String msg,
			DialogInterface.OnClickListener ok_listener, DialogInterface.OnClickListener cancel_listener) {
		new AlertDialog.Builder(context).setMessage(msg).setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, ok_listener)
				.setNegativeButton(android.R.string.cancel, cancel_listener)
				.show();
				
	}
}
