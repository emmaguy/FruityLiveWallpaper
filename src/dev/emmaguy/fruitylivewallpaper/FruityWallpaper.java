package dev.emmaguy.fruitylivewallpaper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.SparseArray;
import android.view.SurfaceHolder;

public class FruityWallpaper extends WallpaperService {

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

    class FruityEngine extends Engine {

	private boolean isVisible;
	private int maxWidth;
	private int maxHeight;
	private int counter = 0;
	
	private final List<FruitProjectile> fruitProjectiles = new ArrayList<FruitProjectile>();
	private final SparseArray<Bitmap> bitmapCache;
	private final Random random = new Random();

	private final Runnable drawFruitProjectiles = new Runnable() {
	    public void run() {
		drawFrame();
	    }
	};

	private final Runnable updateFruitProjectiles = new Runnable() {
	    public void run() {
		updateFrame();
	    }
	};

	public FruityEngine() {
	    bitmapCache = new SparseArray<Bitmap>(FruitType.values().length);

	    for (FruitType t : FruitType.values()) {
		bitmapCache.put(t.getResourceId(),
			BitmapFactory.decodeResource(getResources(), t.getResourceId(), new Options()));
	    }
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
			    f.draw(c);
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

	    return new FruitProjectile(bitmapCache.get(FruitType.randomFruit().getResourceId()), maxWidth, maxHeight,
		    angle, speed, gravity, rightToLeft);
	}
    }
}