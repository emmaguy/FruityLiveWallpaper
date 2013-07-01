package dev.emmaguy.fruitylivewallpaper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class FruitProjectile {

    private final Paint paint = new Paint();
    private final Bitmap bitmap;

    private final int maxWidth;
    private final int maxHeight;
    private final float gravity;
    private final int bitmapWidth;
    
    private int xLocation;
    private int yLocation;
    private int absYLocation;
    private float time = 0.0f;
    private boolean rightToLeft;
    private double v0x;
    private double v0y;

    public FruitProjectile(Bitmap b, int maxWidth, int maxHeight, int angle, int initialSpeed, float gravity, boolean rightToLeft) {
	this.bitmap = b;
	this.maxHeight = maxHeight;
	this.gravity = gravity;
	this.maxWidth = maxWidth;
	this.rightToLeft = rightToLeft;
	
	this.v0x = initialSpeed * Math.cos(angle * Math.PI / 180);
	this.v0y = initialSpeed * Math.sin(angle * Math.PI / 180);
	
	this.paint.setAntiAlias(true);
	this.bitmapWidth = b.getWidth();
    }

    public boolean hasMovedOffScreen() {
	return yLocation < 0 || xLocation + bitmap.getWidth() < 0 || xLocation > maxWidth;
    }

    public void move() {
	
	xLocation = (int) (v0x * time);
	yLocation = (int) (v0y * time - 0.5 * gravity * time * time);

	if (rightToLeft) {
	    xLocation = maxWidth - bitmapWidth - xLocation;
	}

	// 0,0 is top left, we want the parabola to go the other way up
	absYLocation = (yLocation * -1) + maxHeight;

	time += 0.1f;
    }

    public void draw(Canvas canvas) {
	canvas.drawBitmap(bitmap, xLocation, absYLocation, paint);
    }
}