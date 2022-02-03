package com.giulio.contentprovider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
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
	
	protected static String saveContacts2Xls(String xlsname, ArrayList<Contact> saveMe) {
		
		
    	WritableWorkbook wb;
    	WritableSheet sheet;
    	int row=1;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File(sdCard.getAbsolutePath() + "/Contacts2Xls");
    		dir.mkdirs();
    		File wbfile = new File(dir,xlsname);
			 wb = jxl.Workbook.createWorkbook(wbfile);
			 sheet = wb.createSheet("Contacts",1);
		} catch (Exception e) {
			return e.getMessage();
		}
    	
    	for(Contact contact : saveMe ) {

    		if (contact.name == null) continue;
       	 Label label1 = new Label(1,row,contact.name);
       	 Label label2 = new Label(2,row,contact.number);
       	 Label label3 = new Label(3,row,contact.type);
       	 Label label4 = new Label(4,row,contact.account);
                    	 
                    	 row++;
                    	 try {
 							sheet.addCell(label1);
							sheet.addCell(label2);
							sheet.addCell(label3);
							sheet.addCell(label4);
						} catch (RowsExceededException e) {
							return e.getMessage();
						} catch (WriteException e) {
							return e.getMessage();
						}
                     } 
        try {
        	wb.write();
			wb.close();
			return "Contacts saved to " + xlsname;
		} catch (WriteException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}

		
	}
	
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

		arrayOfContacts = Contact.fromXls("/Contacts2Xls", open_xls_name, this);
		// CrearrayOfContactsate the adapter to convert the array to views
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
            	String msg = saveContacts2Xls(xlsName.getText().toString(), arrayOfContacts);
            	showMsg(msg);
            }


        });
		
		
		
		Button newBt = (Button) this.findViewById(R.id.newitem_bt);
		newBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	
            	Intent newContactIntent = new Intent(getApplicationContext(), NewContactActivity.class);
            	startActivity(newContactIntent);
            	
            }
		});
            	
            }
	}


	
	

