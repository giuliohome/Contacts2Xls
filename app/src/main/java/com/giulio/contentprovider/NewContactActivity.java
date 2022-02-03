package com.giulio.contentprovider;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NewContactActivity extends Activity implements showMsg {

	private void showMsg(String msg) {
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void showText(String msg) {
		showMsg(msg);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_contact);
		
		final TextView tvName = (TextView) findViewById(R.id.tvName);
		final TextView tvNumber = (TextView) findViewById(R.id.tvNumber);
		final TextView tvType = (TextView) findViewById(R.id.tvType);
		final TextView tvAccount = (TextView) findViewById(R.id.tvAccount);
		
		Button btAddNew = (Button) findViewById(R.id.addnewitem_bt);
		btAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Contact newContact = new Contact(tvName.getText().toString(), tvNumber.getText().toString(), 
                		tvType.getText().toString(), tvAccount.getText().toString(), false);
                
                ContactsListActivity.arrayOfContacts.add(newContact);
            	ContactsListActivity.itemsAdapter.notifyDataSetChanged();
            }
		});
            }
		
        
    	
	}

