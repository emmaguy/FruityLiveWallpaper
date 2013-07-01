package dev.emmaguy.fruitylivewallpaper;

public enum FruitType {
    WATERMELON(R.drawable.watermelon), STRAWBERRY(R.drawable.strawberry), 
    PINEAPPLE(R.drawable.pineapple), GRAPES(R.drawable.grape), 
    APPLE(R.drawable.apple), BANANA(R.drawable.banana), 
    ORANGE(R.drawable.orange);

    private final int resourceId;

    private FruitType(int resourceId) {
	this.resourceId = resourceId;
    }

    public int getResourceId() {
	return resourceId;
    }
}