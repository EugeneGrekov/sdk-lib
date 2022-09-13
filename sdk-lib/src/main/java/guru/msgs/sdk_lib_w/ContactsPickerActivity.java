/*
* Copyright (C) 2011 - 2015 by Ngewi Fet <ngewif@gmail.com>
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
 */

package guru.msgs.sdk_lib_w;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.guru.managed.notifications.R;

import guru.msgs.sdk_lib.utils.contactpicker.ContactsListFragment;
import guru.msgs.sdk_lib.utils.contactpicker.OnContactSelectedListener;

public class ContactsPickerActivity extends AppCompatActivity implements OnContactSelectedListener {
    public static final String SELECTED_CONTACT_ID 	= "contact_id";
	public static final String KEY_PHONE_NUMBER 	= "phone_number";
	public static final String KEY_CONTACT_NAME 	= "contact_name";
	private static final int ACTIVITY_START_CODE = 203432;

	public boolean mNamesOnly;
	/**
	 * Starting point
	 * Loads the {@link ContactsListFragment} 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		checkPermission("android.permission.READ_CONTACTS", ACTIVITY_START_CODE);
	}

	private void BuildIt() {
		FragmentManager fragmentManager 	= this.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ContactsListFragment fragment = new ContactsListFragment();
		fragment.mNamesOnly = this.mNamesOnly;
		
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.commit();

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.contact_label);
		}
	}

	/**
	 * Callback when the contact is selected from the list of contacts.
	 * Loads the {@link ContactDetailsFragment} 
	 */
	@Override
	public void onContactNameSelected(long contactId, String contactName) {
		/* Now that we know which Contact was selected we can go to the details fragment */
		
		Fragment detailsFragment = new ContactDetailsFragment();
		Bundle 		args 			= new Bundle();
		args.putLong(ContactsPickerActivity.SELECTED_CONTACT_ID, contactId);
		detailsFragment.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.fragment_container, detailsFragment);
		
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

	/** 
	 * Callback when the contact number is selected from the contact details view 
	 * Sets the activity result with the contact information and finishes
	 */
	@Override
	public void onContactNumberSelected(String contactNumber, String contactName) {
		Intent intent = new Intent();
		intent.putExtra(KEY_PHONE_NUMBER, contactNumber);
		intent.putExtra(KEY_CONTACT_NAME, contactName);
		
        setResult(RESULT_OK, intent);
        finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == ACTIVITY_START_CODE) {

			// Checking whether user granted the permission or not.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				BuildIt();
			}
			else {
				Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
			}
		}
	}


	public void checkPermission(String permission, int requestCode)
	{
		// Checking if permission is not granted
		if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
		}
		else {
			BuildIt();
		}
	}


}