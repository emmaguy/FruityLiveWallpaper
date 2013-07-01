package dev.emmaguy.fruitylivewallpaper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class FruityWallpaperSettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    protected Method loadHeaders = null;
    protected Method hasHeaders = null;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * 
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
	if (hasHeaders != null && loadHeaders != null) {
	    try {
		return (Boolean) hasHeaders.invoke(this);
	    } catch (IllegalArgumentException e) {
	    } catch (IllegalAccessException e) {
	    } catch (InvocationTargetException e) {
	    }
	}
	return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle aSavedState) {
	try {
	    loadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
	    hasHeaders = getClass().getMethod("hasHeaders");
	} catch (NoSuchMethodException e) {
	}

	super.onCreate(aSavedState);

	if (!isNewV11Prefs()) {
	    addPreferencesFromResource(R.xml.fruit_opacity);
	    addPreferencesFromResource(R.xml.fruit_selection);

	    addFruitSelectionOptions(this, (PreferenceCategory) findPreference("fruit_selection"), this);
	}
    }

    @SuppressLint("NewApi")
    private static void addFruitSelectionOptions(final Context c, final PreferenceCategory selectFruitsCheckboxes,
	    final Activity a) {

	for (FruitType f : FruitType.values()) {
	    CheckBoxPreference checkBox = new CheckBoxPreference(c);
	    checkBox.setKey(f.name());
	    checkBox.setTitle(f.name());
	    checkBox.setChecked(true);
	    checkBox.setOnPreferenceChangeListener((OnPreferenceChangeListener) a);

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		checkBox.setIcon(f.getResourceId());
	    }

	    selectFruitsCheckboxes.addPreference(checkBox);
	}

    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
	try {
	    loadHeaders.invoke(this, new Object[] { R.xml.preference_headers, aTarget });
	} catch (IllegalArgumentException e) {
	} catch (IllegalAccessException e) {
	} catch (InvocationTargetException e) {
	}
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static public class PrefsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle aSavedState) {
	    super.onCreate(aSavedState);

	    Context context = getActivity().getApplicationContext();
	    Resources resources = context.getResources();
	    String resourcesValue = getArguments().getString("pref-resource");
	    int thePrefRes = resources.getIdentifier(resourcesValue, "xml", context.getPackageName());
	    addPreferencesFromResource(thePrefRes);

	    if (resourcesValue.contains("fruit_selection")) {
		addFruitSelectionOptions(context, (PreferenceCategory) findPreference("fruit_selection"), getActivity());
	    }
	}
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

	boolean includeFruit = Boolean.valueOf(newValue.toString());
	SharedPreferences prefs = getSharedPreferences(FruityWallpaper.SHARED_PREFS_NAME, 0);
	prefs.edit().putBoolean(preference.getKey(), includeFruit).commit();

	return true;
    }
}