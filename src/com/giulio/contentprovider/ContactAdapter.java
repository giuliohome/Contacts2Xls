package com.giulio.contentprovider;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Contact> {
    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Contact contact = getItem(position);    
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
        TextView tvType = (TextView) convertView.findViewById(R.id.tvType);
        TextView tvAccount = (TextView) convertView.findViewById(R.id.tvAccount);
        Button ImportButton = (Button) convertView.findViewById(R.id.importContactBt);
        // Populate the data into the template view using the data object
        tvName.setText(contact.name);
        tvNumber.setText(contact.number);
        tvType.setText(contact.type);
        tvAccount.setText(contact.account);
        if (contact.export) {
        	ImportButton.setText(R.string.exportContactBt);
        } else {
        	ImportButton.setText(R.string.importContactBt);
        }
        // Return the completed view to render on screen
        
        ImportButton.setTag(contact);
     // Attach the click event handler
        ImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Access user from within the tag
            	Contact contact = (Contact) view.getTag();
                // Do what you want here...
            	if (contact.export) {
					SelectContactActivity.saveMe.add(contact);
					NativeContentProvider.showMsg("contact " + contact.name + " added to the export list!");
				} else {
	            	NativeContentProvider.createContact(contact.name, contact.number, contact.type, contact.account);
	            	NativeContentProvider.showMsg("contact " + contact.name + " imported!");
				}
            }
        });
        
        Button removeBt = (Button) convertView.findViewById(R.id.removeContactBt);
        removeBt.setTag(contact);
     // Attach the click event handler
        removeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Access user from within the tag
            	Contact contact = (Contact) view.getTag();
                // Do what you want here...
            	ContactsListActivity.arrayOfContacts.remove(contact);
            	ContactsListActivity.itemsAdapter.notifyDataSetChanged();
            	NativeContentProvider.showMsg("contact " + contact.name + " removed from XLS!");
            }
        });
        
        return convertView;
        }
}
