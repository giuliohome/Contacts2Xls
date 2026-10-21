package com.giulio.contentprovider;

import java.io.File;
import java.util.ArrayList;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsListActivity extends Activity implements showMsg {

	private void showMsg(String msg) {
    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void showText(String msg) {
		showMsg(msg);
	}

	/** Folder where every .xls is saved and read: the app's own folder, no storage permission needed. */
	protected static File xlsDir(Context ctx) {
		return ctx.getExternalFilesDir(null);
	}

	/** Shows the outcome of a save in a dialog, so the full file path is readable. */
	protected static void showResult(Activity activity, String msg) {
		new AlertDialog.Builder(activity)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, null)
				.show();
	}

	private static String errorText(Exception e) {
		return e.getMessage() != null ? e.getMessage() : e.toString();
	}

	protected static String saveContacts2Xls(Context ctx, String xlsname, ArrayList<Contact> saveMe) {
		xlsname = xlsname == null ? "" : xlsname.trim();
		if (xlsname.length() == 0) {
			return "Please enter a file name.";
		}
		// Never overwrite an existing file with an empty sheet.
		if (saveMe == null || saveMe.isEmpty()) {
			return "Nothing to save: the list is empty.";
		}
		File dir = xlsDir(ctx);
		if (dir == null) {
			return "Save failed: storage is not available.";
		}

		File wbfile = new File(dir, xlsname);
		int saved = 0;
		try {
			wbfile.getParentFile().mkdirs();
			WritableWorkbook wb = jxl.Workbook.createWorkbook(wbfile);
			WritableSheet sheet = wb.createSheet("Contacts", 1);
			int row = 1;
			for (Contact contact : saveMe) {
				if (contact.name == null) continue;
				sheet.addCell(new Label(1, row, contact.name));
				sheet.addCell(new Label(2, row, contact.number));
				sheet.addCell(new Label(3, row, contact.type));
				sheet.addCell(new Label(4, row, contact.account));
				row++;
				saved++;
			}
			wb.write();
			wb.close();
		} catch (Exception e) {
			return "Save failed: " + errorText(e);
		}
		return "Saved " + saved + " contacts to:\n" + wbfile.getAbsolutePath();
	}

	protected static ArrayList<Contact> arrayOfContacts = new ArrayList<Contact>();
	protected static ContactAdapter itemsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist);
		SystemBars.applyInsets(this);

		// The intent survives rotation and process restarts, the saved state does not carry the name.
		String open_xls_name = getIntent().getStringExtra("open_xls_name");

		arrayOfContacts = Contact.fromXls(this, open_xls_name, this);
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
		saveBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	showResult(ContactsListActivity.this,
            			saveContacts2Xls(ContactsListActivity.this, xlsName.getText().toString(), arrayOfContacts));
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
}
