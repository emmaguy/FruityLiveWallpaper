package dev.emmaguy.fruitylivewallpaper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.SparseArray;
import android.view.SurfaceHolder;

public class FruityWallpaper extends WallpaperService {

    public static final String SHARED_PREFS_NAME = "fruitywallpapersettings";
    public static final String OPACITY_SHARED_PREF_NAME = "fruitOpacity";
    public static final int OPACITY_DEFAULT = 255;
    
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
	super.onCreate();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
	return new FruityEngine();
    }

    class FruityEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

	private boolean isVisible;
	private int maxWidth;
	private int maxHeight;
	private int counter = 0;

	private final List<FruitProjectile> fruitProjectiles = new ArrayList<FruitProjectile>();
	private final SparseArray<Bitmap> bitmapCache;
	private final Random random = new Random();
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private final Runnable updateFruitProjectiles = new Runnable() {
	    public void run() {
		updateFrame();
	    }
	};

	private final Runnable drawFruitProjectiles = new Runnable() {
	    public void run() {
		drawFrame();
	    }
	};

	private List<FruitType> permittedFruits = new ArrayList<FruitType>();

	public FruityEngine() {
	    bitmapCache = new SparseArray<Bitmap>(FruitType.values().length);

	    for (FruitType t : FruitType.values()) {
		bitmapCache.put(t.getResourceId(),
			BitmapFactory.decodeResource(getResources(), t.getResourceId(), new Options()));
	    }

	    SharedPreferences prefs = FruityWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
	    prefs.registerOnSharedPreferenceChangeListener(this);
	    setPermittedFruits(prefs);

	    if (permittedFruits.size() <= 0) {
		for (FruitType f : FruitType.values()) {
		    permittedFruits.add(f);
		    prefs.edit().putBoolean(f.name(), true).commit();
		}
	    }
	    
	    onSharedPreferenceChanged(prefs, OPACITY_SHARED_PREF_NAME);
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();

	    mHandler.removeCallbacks(updateFruitProjectiles);
	    mHandler.removeCallbacks(drawFruitProjectiles);
	}

	@Override
	public void onVisibilityChanged(boolean visible) {
	    isVisible = visible;
	    if (visible) {
		updateFrame();
		drawFrame();
	    } else {
		mHandler.removeCallbacks(updateFruitProjectiles);
		mHandler.removeCallbacks(drawFruitProjectiles);
	    }
	}

	@Override
	public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    super.onSurfaceChanged(holder, format, width, height);

	    updateFrame();
	    drawFrame();

	    this.maxWidth = width;
	    this.maxHeight = height;
	}

	@Override
	public void onSurfaceCreated(SurfaceHolder holder) {
	    super.onSurfaceCreated(holder);
	}

	@Override
	public void onSurfaceDestroyed(SurfaceHolder holder) {
	    super.onSurfaceDestroyed(holder);
	    isVisible = false;

	    mHandler.removeCallbacks(drawFruitProjectiles);
	    mHandler.removeCallbacks(updateFruitProjectiles);
	}

	void updateFrame() {

	    if (counter++ % 2 == 0) {
		FruitProjectile createNewFruitProjectile = createNewFruitProjectile();
		synchronized (fruitProjectiles) {
		    fruitProjectiles.add(createNewFruitProjectile);
		}
	    }

	    synchronized (fruitProjectiles) {
		for (Iterator<FruitProjectile> iter = fruitProjectiles.iterator(); iter.hasNext();) {

		    FruitProjectile f = iter.next();
		    f.move();

		    if (f.hasMovedOffScreen()) {
			iter.remove();
		    }
		}
	    }

	    mHandler.removeCallbacks(updateFruitProjectiles);
	    if (isVisible) {
		mHandler.postDelayed(updateFruitProjectiles, 50);
	    }
	}

	void drawFrame() {
	    final SurfaceHolder holder = getSurfaceHolder();

	    Canvas c = null;
	    try {
		c = holder.lockCanvas();
		if (c != null) {
		    c.drawARGB(255, 0, 0, 0);
		    synchronized (fruitProjectiles) {
			for (FruitProjectile f : fruitProjectiles) {
			    f.draw(c, paint);
			}
		    }
		}
	    } finally {
		if (c != null)
		    holder.unlockCanvasAndPost(c);
	    }

	    mHandler.removeCallbacks(drawFruitProjectiles);
	    if (isVisible) {
		mHandler.postDelayed(drawFruitProjectiles, 30);
	    }
	}

	private FruitProjectile createNewFruitProjectile() {
	    int angle = random.nextInt(20) + 70;
	    int speed = random.nextInt(30) + (maxHeight / 6);
	    boolean rightToLeft = random.nextBoolean();
	    float gravity = random.nextInt(6) + 14.0f;

	    return new FruitProjectile(bitmapCache.get(getRandomFruit().getResourceId()), maxWidth, maxHeight, angle,
		    speed, gravity, rightToLeft);
	}

	private FruitType getRandomFruit() {
	    return permittedFruits.get(random.nextInt(permittedFruits.size()));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    if (key.equals(OPACITY_SHARED_PREF_NAME)) {
		int opacity = sharedPreferences.getInt(key, OPACITY_DEFAULT);
		paint.setAlpha(opacity);
	    } else {
		setPermittedFruits(sharedPreferences);
	    }
	}

	private void setPermittedFruits(SharedPreferences sharedPreferences) {
	    permittedFruits.clear();

	    for (FruitType f : FruitType.values()) {
		if (sharedPreferences.getBoolean(f.toString(), false)) {
		    permittedFruits.add(f);
		}
	    }
	}
    }
}