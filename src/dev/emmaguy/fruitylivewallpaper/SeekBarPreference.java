package dev.emmaguy.fruitylivewallpaper;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

    private int maxValue = FruityWallpaper.OPACITY_DEFAULT;
    private int minValue = 0;
    private int interval = 1;
    private int currentValue;
    private SeekBar seekBar;
    private TextView statusTextView;

    public SeekBarPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	
	initPreference(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	
	initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
	seekBar = new SeekBar(context, attrs);
	seekBar.setMax(maxValue - minValue);
	seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

	RelativeLayout layout = null;

	try {
	    LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    layout = (RelativeLayout) mInflater.inflate(R.layout.seekbar_preference, parent, false);
	} catch (Exception e) {
	    Log.e("SeekBarPrefs", "Error creating seek bar preference", e);
	}

	return layout;
    }

    @Override
    public void onBindView(View view) {
	super.onBindView(view);

	try {
	    ViewParent oldContainer = seekBar.getParent();
	    ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

	    if (oldContainer != newContainer) {
		if (oldContainer != null) {
		    ((ViewGroup) oldContainer).removeView(seekBar);
		}

		newContainer.removeAllViews();
		newContainer.addView(seekBar, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    }
	} catch (Exception ex) {
	    Log.e("SeekBarPrefs", "Error binding view: " + ex.toString());
	}

	updateView(view);
    }

    protected void updateView(View view) {

	try {
	    RelativeLayout layout = (RelativeLayout) view;

	    statusTextView = (TextView) layout.findViewById(R.id.seekBarPrefValue);
	    statusTextView.setText(String.valueOf(currentValue));
	    statusTextView.setMinimumWidth(30);

	    seekBar.setProgress(currentValue - minValue);

	} catch (Exception e) {
	    Log.e("SeekBarPrefs", "Error updating seek bar preference", e);
	}
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	int newValue = progress + minValue;

	if (newValue > maxValue) {
	    newValue = maxValue;
	} else if (newValue < minValue) {
	    newValue = minValue;
	} else if (interval != 1 && newValue % interval != 0) {
	    newValue = Math.round(((float) newValue) / interval) * interval;
	}

	if (!callChangeListener(newValue)) {
	    seekBar.setProgress(currentValue - minValue);
	    return;
	}

	currentValue = newValue;
	statusTextView.setText(String.valueOf(newValue));

	getSharedPreferences().edit().putInt(FruityWallpaper.OPACITY_SHARED_PREF_NAME, newValue).commit();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
	notifyChanged();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

	getPreferenceManager().setSharedPreferencesName(FruityWallpaper.SHARED_PREFS_NAME);
	if (restoreValue) {
	    currentValue = getSharedPreferences().getInt(FruityWallpaper.OPACITY_SHARED_PREF_NAME, FruityWallpaper.OPACITY_DEFAULT);
	}
    }
}