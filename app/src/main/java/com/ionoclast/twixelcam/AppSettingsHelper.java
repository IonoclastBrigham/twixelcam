// AppSettingsFragment.java
// Simple settings UI
// TwixelCam Copyright © 2016 Brigham Toskin


package com.ionoclast.twixelcam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
public class AppSettingsHelper
{
	public static SharedPreferences GetSettings(Context pCtxt)
	{
		return PreferenceManager.getDefaultSharedPreferences(pCtxt);
	}

	public static Fragment CreateFragment()
	{
		return new Fragment();
	}

	public static class Fragment  extends PreferenceFragment
	{
		Fragment(){}

		@Override
		public void onCreate(Bundle pState)
		{
			super.onCreate(pState);

			// TODO: parameterize this behavior below

			addPreferencesFromResource(R.xml.preferences);

			final String KEY_SIZE = "pref_size";
			Preference tSizePref = findPreference(KEY_SIZE);
			String tSize = GetSettings(getActivity()).getString(KEY_SIZE, "40");
			tSizePref.setTitle(getString(R.string.pref_size_title, tSize));
		}
	}
}
