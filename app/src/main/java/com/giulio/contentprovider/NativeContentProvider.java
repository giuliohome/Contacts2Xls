package com.giulio.contentprovider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class NativeContentProvider extends Activity implements showMsg {
	
    private static NativeContentProvider thisClass;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nativecontentprovider);
        thisClass = this;

            Button view = (Button)findViewById(R.id.viewButton);
            Button select = (Button)findViewById(R.id.selectButton);
            Button add = (Button)findViewById(R.id.createButton);
            Button openXls = (Button)findViewById(R.id.openXlsButton);
            Button merge = (Button)findViewById(R.id.mergeButton);
            final EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            
            view.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
             		if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS,
             						Manifest.permission.WRITE_EXTERNAL_STORAGE},
             	                MY_PERMISSIONS_REQUEST_ExportContacts);
             		} else {
                		exportContacts(getApplicationContext(),xlsName.getText().toString());
                		showMsg("Completed Exporting Contact numbers");
             		}
            	}
            });
            
            select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
             		    ContextCompat.checkSelfPermission(thisClass,
                    		 Manifest.permission.WRITE_CONTACTS)
                    		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_SelectXlsContacts);
             		} else {
						ContactsListActivity.arrayOfContacts.clear();
             			selectXlsContacts(xlsName.getText().toString());
             		}
					
				}
			});

            add.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){
					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED)  {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_CreateContacts);
             		} else {
                		createContacts("/Contacts2Xls",xlsName.getText().toString());
                		//toast already inside above call
                		//showMsg("Completed Importing Contact numbers");
             		}
             		
            	}
            });

            openXls.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){

					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_OpenXlsContacts);
             		} else {
                		openXlsContacts(xlsName.getText().toString());

                		showMsg("Contact numbers .xls opened");
             		}
            	}
            });

            merge.setOnClickListener(new OnClickListener() {
             	public void onClick(View v){

					if (ContextCompat.checkSelfPermission(thisClass,
             		        Manifest.permission.READ_CONTACTS)
             		        != PackageManager.PERMISSION_GRANTED ||
                 		    ContextCompat.checkSelfPermission(thisClass,
                           		 Manifest.permission.WRITE_CONTACTS)
                           		 != PackageManager.PERMISSION_GRANTED ) {
             			ActivityCompat.requestPermissions(thisClass,
             	                new String[]{Manifest.permission.READ_CONTACTS,
             						Manifest.permission.WRITE_CONTACTS},
             	                MY_PERMISSIONS_REQUEST_MergeContacts);
             		} else {
                		mergeContacts(xlsName.getText().toString());

                		showMsg("Contact numbers .xls opened");
             		}
            	}
            });
    }
    
    private static final int MY_PERMISSIONS_REQUEST_ExportContacts = 1;
    private static final int MY_PERMISSIONS_REQUEST_SelectXlsContacts = 2;
    private static final int MY_PERMISSIONS_REQUEST_CreateContacts = 3;
    private static final int MY_PERMISSIONS_REQUEST_OpenXlsContacts = 4;
    private static final int MY_PERMISSIONS_REQUEST_MergeContacts = 5;
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ExportContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		exportContacts(getApplicationContext(),xlsName.getText().toString());
            		showMsg("Completed Exporting Contact numbers");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
            
            case MY_PERMISSIONS_REQUEST_SelectXlsContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
                	selectXlsContacts(xlsName.getText().toString());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }

            
            case MY_PERMISSIONS_REQUEST_CreateContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		createContacts("/Contacts2Xls",xlsName.getText().toString());
            		//toast already inside above call
            		//showMsg("Completed Importing Contact numbers");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }
            
            case MY_PERMISSIONS_REQUEST_OpenXlsContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		openXlsContacts(xlsName.getText().toString());

            		showMsg("Contact numbers .xls opened");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }   
            
            case MY_PERMISSIONS_REQUEST_MergeContacts: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                	EditText xlsName = (EditText)findViewById(R.id.xls_name_open);
            		mergeContacts(xlsName.getText().toString());

            		showMsg("Contact numbers .xls opened");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
            		showMsg("You didn't authorize the app");
                }
                return;
            }   
                        
        }
    }
    
    protected static void showMsg(String msg) {
    	Toast.makeText(thisClass.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void showText(String msg) {
		showMsg(msg);
	}
    private void exportContacts(Context context, String xlsname) {
    	WritableWorkbook wb;
    	WritableSheet sheet;
    	int row=1;
    	try {
    		// File sdCard = Environment.getExternalStorageDirectory();
    		File dir = context.getExternalFilesDir(null);
					// new File(sdCard.getAbsolutePath() + path);
    		dir.mkdirs();
    		File wbfile = new File(dir,xlsname);
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
                    	 String type = pCur.getString(pCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    	 String account = pCur.getString(pCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    	 
                    	 if (name == null) continue;
                    	 Label label1 = new Label(1,row,name);
                    	 Label label2 = new Label(2,row,phoneNo);
                    	 Label label3 = new Label(3,row,type);
                    	 Label label4 = new Label(4,row,account);
                    	 
                    	 
                    	 row++;
                    	 try {
							sheet.addCell(label1);
							sheet.addCell(label2);
							sheet.addCell(label3);
							sheet.addCell(label4);
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
    
    protected static void createContact(String name, String phone, String type, String account) {
    	ContentResolver cr = thisClass.getContentResolver();
    	
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
    	
    	if (cur.getCount() > 0) {
        	while (cur.moveToNext()) {
        		String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		if (existName !=  null && existName.equalsIgnoreCase(name)) {
                	Toast.makeText(thisClass,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
                	return;        			
        		}
        	}
    	}
    	
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, type)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account)
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

    }
    
    private void openXlsContacts(String xlsname) {
    	try {
        	Intent contactsListIntent = new Intent(thisClass, ContactsListActivity.class);
    		contactsListIntent.putExtra("open_xls_name", xlsname);
        	startActivity(contactsListIntent);
    		
    	} catch(Exception e) {
    		showMsg(e.toString());
    	}
    }
    
    private void mergeContacts(String xlsname) {
    	try {
        	Intent mergeIntent = new Intent(thisClass, MergeContactactivity.class);
        	mergeIntent.putExtra("open_xls_name", xlsname);
        	startActivity(mergeIntent);
    		
    	} catch(Exception e) {
    		showMsg(e.toString());
    	}
    }
    
    private void selectXlsContacts(String xlsname) {
    	try {
			Intent selectContactIntent = new Intent(thisClass, SelectContactActivity.class);
			selectContactIntent.putExtra("open_xls_name", xlsname);
			startActivity(selectContactIntent);
		} catch (Exception e) {
    		showMsg(e.toString());
		}
    }
    
    private void createContacts(String path, String xlsname) {

        
    	Workbook wb;
    	Sheet sheet;
    	int row=1;
    	try {
    		File sdCard = Environment.getExternalStorageDirectory();
    		File dir = new File(sdCard.getAbsolutePath() + path);
    		dir.mkdirs();
    		File wbfile = new File(dir,xlsname);
			 wb = jxl.Workbook.getWorkbook(wbfile);
			 sheet = wb.getSheet("Contacts");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(NativeContentProvider.this,e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
    	
    	
    	row = 1;
    	String name=sheet.getCell(1, row).getContents();
    	String phone=sheet.getCell(2, row).getContents();
    	String type=sheet.getCell(3, row).getContents();
    	String account=sheet.getCell(4, row).getContents();
    	int rows = sheet.getRows();
    	while (name != null && name.length()>0)
    	{
    		createContact(name,phone, type, account);
    		row++;
    		if (row == rows) break;
    		name=sheet.getCell(1, row).getContents();
    		phone=sheet.getCell(2, row).getContents();
    		type=sheet.getCell(3, row).getContents();
    		account=sheet.getCell(4, row).getContents();
    	}
    	
    	
    	Toast.makeText(NativeContentProvider.this, "Imported contacts from folder: " + path, Toast.LENGTH_SHORT).show();
    	
    }

    

}