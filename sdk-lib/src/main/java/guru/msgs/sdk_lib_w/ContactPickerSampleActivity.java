/*
* Copyright (C) 2011 by Ngewi Fet <ngewif@gmail.com>
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
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;

import guru.msgs.sdk_lib.BuildConfig;
import guru.msgs.sdk_lib.utils.config.Configurations;
import guru.msgs.sdk_lib.utils.contactpicker.OnContactSelectedListener;


/**
 * Extends the ContactsPickerActivity class from the library for demonstration purposes.
 * Instead of delivering an intent with the selected contact info, a toast is displayed
 */
public class ContactPickerSampleActivity extends ContactsPickerActivity implements OnContactSelectedListener {
	public static final int ACTIVITY_START_CODE = CONTEXT_INCLUDE_CODE +1;
	public static final String ACTIVITY_CODE = "INTENT_RES_NAME";
	public static final String INTENT_RES_NAME = "INTENT_RES_NAME";
	public static final String INTENT_RES_ID = "INTENT_RES_ID";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mNamesOnly = true;
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(false);
		}

	}


	@Override
	public void onContactNameSelected(long contactId, String contactName) {
		if (BuildConfig.DEBUG) {
			Log.d(Configurations.TAG, String.format("onContactNameSelected :\n %d: %s\nAn intent would be delivered to your app", contactId, contactName));
		}

		Intent i = new Intent();
		i.putExtra(INTENT_RES_ID, contactId);
		i.putExtra(INTENT_RES_NAME, contactName);
		setResult(RESULT_OK, i);
		finish();
	}
}