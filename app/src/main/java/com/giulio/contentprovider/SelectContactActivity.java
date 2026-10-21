package com.giulio.contentprovider;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
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
		SystemBars.applyInsets(this);

		String open_xls_name = getIntent().getStringExtra("open_xls_name");
		// Start each visit with an empty export list, not the leftovers of the previous one.
		saveMe = new ArrayList<Contact>();
		// "New" adds to the xls list of the Open screen, it has no meaning here.
		findViewById(R.id.newitem_bt).setVisibility(View.GONE);

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
		final EditText xlsName = (EditText)this.findViewById(R.id.xls_name_save);
		if (open_xls_name != null) {
			xlsName.setText(open_xls_name);
		}
		Button saveBt = (Button) this.findViewById(R.id.saveitems_bt);
		saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (saveMe.isEmpty()) {
            		ContactsListActivity.showResult(SelectContactActivity.this,
            				"Nothing to save yet: tap Export on the contacts you want in the file.");
            		return;
            	}
            	ContactsListActivity.showResult(SelectContactActivity.this,
            			ContactsListActivity.saveContacts2Xls(SelectContactActivity.this, xlsName.getText().toString(), saveMe));
            }
        });
	}

}
