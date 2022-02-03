package com.giulio.contentprovider;

import java.util.ArrayList;

import jxl.write.Label;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SelectContactActivity extends Activity {

    private void showMsg(String msg) {
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
	

	protected static ArrayList<Contact> saveMe = new ArrayList<Contact>();
	protected static ArrayList<Contact> arrayOfContacts = new ArrayList<Contact>();
	protected static ContactAdapter itemsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist);
		String open_xls_name;
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras == null) {
		    	open_xls_name= null;
		    } else {
		    	open_xls_name= extras.getString("open_xls_name");
		    }
		} else {
			open_xls_name= (String) savedInstanceState.getSerializable("open_xls_name");
		}
		
		ContentResolver cr = getContentResolver();
		arrayOfContacts = Contact.fromAndroid(cr);
		// Create the adapter to convert the array to views
				itemsAdapter = new ContactAdapter(this, arrayOfContacts);
				// Attach the adapter to a ListView
				ListView listView = (ListView) this.findViewById(R.id.lvContactItems);
				try {
					listView.setAdapter(itemsAdapter);
				} catch (Exception e) {
					showMsg(e.toString());
				}
				//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				final EditText xlsName = (EditText)this.findViewById(R.id.xls_name_save);
				Button saveBt = (Button) this.findViewById(R.id.saveitems_bt);
				saveBt.setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		            	String msg = ContactsListActivity.saveContacts2Xls(xlsName.getText().toString(), saveMe);
		            	showMsg(msg);
		            }


		        });


	}

}
