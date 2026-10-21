package com.giulio.contentprovider;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsListActivity extends Activity implements showMsg {

	private static final int REQ_SAVE = 1;

	private void showMsg(String msg) {
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void showText(String msg) {
		showMsg(msg);
	}

	protected static ArrayList<Contact> arrayOfContacts = new ArrayList<Contact>();
	protected static ContactAdapter itemsAdapter;
	/** The file this list was opened from, so "Save as" starts in its folder. */
	private Uri openedUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist);
		SystemBars.applyInsets(this);

		// The intent survives rotation and process restarts.
		openedUri = getIntent().getParcelableExtra(XlsFiles.EXTRA_URI);
		String openedName = getIntent().getStringExtra(XlsFiles.EXTRA_NAME);

		arrayOfContacts = Contact.fromXls(this, openedUri, this);
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
		if (openedName != null) {
			xlsName.setText(openedName);
		}
		Button saveBt = (Button) this.findViewById(R.id.saveitems_bt);
		saveBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	// Never write an empty sheet over a file.
            	if (arrayOfContacts.isEmpty()) {
            		XlsFiles.showResult(ContactsListActivity.this, "Nothing to save: the list is empty.");
            		return;
            	}
            	XlsFiles.pickSaveLocation(ContactsListActivity.this, xlsName.getText().toString(), openedUri, REQ_SAVE);
            }
        });

		Button newBt = (Button) this.findViewById(R.id.newitem_bt);
		newBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            	Intent newContactIntent = new Intent(getApplicationContext(), NewContactActivity.class);
            	startActivity(newContactIntent);

            }
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_SAVE && resultCode == RESULT_OK && data != null && data.getData() != null) {
			XlsFiles.showResult(this, XlsFiles.save(this, data.getData(), arrayOfContacts));
		}
	}
}
