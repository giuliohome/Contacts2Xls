package com.giulio.contentprovider;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MergeContactactivity extends Activity implements showMsg {

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
		// "New" adds to the xls list of the Open screen, it has no meaning here.
		findViewById(R.id.newitem_bt).setVisibility(View.GONE);

		ContentResolver cr = getContentResolver();
		SelectContactActivity.saveMe =  Contact.fromXls(this, openedUri, this);
		ArrayList<Contact> mergedList = new ArrayList<Contact>();
		mergedList.addAll(SelectContactActivity.saveMe);
		mergedList.addAll( Contact.fromAndroid(cr));
		// Create the adapter to convert the array to views
		itemsAdapter = new ContactAdapter(this, mergedList);
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
		saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	// Never write an empty sheet over a file.
            	if (SelectContactActivity.saveMe.isEmpty()) {
            		XlsFiles.showResult(MergeContactactivity.this,
            				"Nothing to save yet: tap Export on the contacts you want in the file.");
            		return;
            	}
            	XlsFiles.pickSaveLocation(MergeContactactivity.this, xlsName.getText().toString(), openedUri, REQ_SAVE);
            }
        });
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_SAVE && resultCode == RESULT_OK && data != null && data.getData() != null) {
			XlsFiles.showResult(this, XlsFiles.save(this, data.getData(), SelectContactActivity.saveMe));
		}
	}

}
