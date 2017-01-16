package com.sai.contentprovider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;



public class NativeContentProvider extends Activity {
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nativecontentprovider);

            Button view = (Button)findViewById(R.id.viewButton);
            Button add = (Button)findViewById(R.id.createButton);
            Button modify = (Button)findViewById(R.id.updateButton);
            Button delete = (Button)findViewById(R.id.deleteButton);
            
            
            view.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
            		displayContacts();
            		Log.i("NativeContentProvider", "Completed Displaying Contact list");
            	}
            });

            add.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
            		createContact("Sample Name", "123456789");
            		Log.i("NativeContentProvider", "Created a new contact, of course hard-coded");
            	}
            });
            
            modify.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		updateContact("Sample Name", "987654321");
            		Log.i("NativeContentProvider", "Completed updating the email id, if applicable");
            	}
            });
            
            delete.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		deleteContact("Sample Name");
            		Log.i("NativeContentProvider", "Deleted the just created contact");
            	}
            });
    }
    
    private void displayContacts() {
    	WritableWorkbook wb;
    	WritableSheet sheet;
    	int row=1;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File(sdCard.getAbsolutePath() + "/MyOpenTweet");
    		dir.mkdirs();
    		File wbfile = new File(dir,"MyExportedContacts.xls");
			 wb = jxl.Workbook.createWorkbook(wbfile);
			 sheet = wb.createSheet("Contacts",1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    	ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
        		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                     Cursor pCur = cr.query(
                    		 ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    		 null, 
                    		 ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                    		 new String[]{id}, null);
                     while (pCur.moveToNext()) {
                    	 String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    	 //Toast.makeText(NativeContentProvider.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                    	 if (name == null) continue;
                    	 Label label1 = new Label(1,row,name);
                    	 Label label2 = new Label(2,row,phoneNo);
                    	 row++;
                    	 try {
							sheet.addCell(label1);
							sheet.addCell(label2);
						} catch (RowsExceededException e) {
							Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
							return;
						} catch (WriteException e) {
							Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
							return;
						}
                     } 
                     
      	        pCur.close();
      	    }
        	}
        }
        try {
        	wb.write();
			wb.close();
		} catch (WriteException e) {
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		} catch (IOException e) {
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    }
    
    private void createContact(String name, String phone) {
    	ContentResolver cr = getContentResolver();
    	
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        
    	if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (existName !=  null && existName.contains(name)) {
                	Toast.makeText(NativeContentProvider.this,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
                	return;        			
        		}
        	}
    	}
    	
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.google")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "accountname@gmail.com")
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());

        
        try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	Toast.makeText(NativeContentProvider.this, "Created a new contact with name: " + name + " and Phone No: " + phone, Toast.LENGTH_SHORT).show();
    	
    }
    
    private void updateContact(String name, String phone) {
    	ContentResolver cr = getContentResolver();
 
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " + 
        			ContactsContract.Data.MIMETYPE + " = ? AND " +
        			String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";
        String[] params = new String[] {name,
        		ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        		String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};

        Cursor phoneCur = managedQuery(ContactsContract.Data.CONTENT_URI, null, where, params, null);
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        
        if ( (null == phoneCur)  ) {
        	createContact(name, phone);
        } else {
        	ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        	        .withSelection(where, params)
        	        .withValue(ContactsContract.CommonDataKinds.Phone.DATA, phone)
        	        .build());
        }
        
        phoneCur.close();
        
        try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(NativeContentProvider.this, "Updated the phone number of 'Sample Name' to: " + phone, Toast.LENGTH_SHORT).show();
    }
    
    private void deleteContact(String name) {

    	ContentResolver cr = getContentResolver();
    	String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
    	String[] params = new String[] {name};
    
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
    	        .withSelection(where, params)
    	        .build());
        try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(NativeContentProvider.this, "Deleted the contact with name '" + name +"'", Toast.LENGTH_SHORT).show();
  
    }
}