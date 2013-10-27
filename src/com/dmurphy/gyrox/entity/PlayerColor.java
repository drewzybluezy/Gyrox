package com.dmurphy.gyrox.entity;

public final class PlayerColor {

	public static final float diffuse[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 1.00f, 0.550f, 0.140f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 0.800f, 0.800f, 0.800f, 1.000f }, // Grey
			{ 0.120f, 0.750f, 0.0f, 1.000f }, // Green
			{ 0.750f, 0.0f, 0.35f, 1.000f } // Purple
	};

	public static final float specular[][] = { { 0.0f, 0.1f, 0.900f, 1.000f }, // Blue
			{ 0.500f, 0.500f, 0.000f, 1.000f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 1.000f }, // Red
			{ 1.00f, 1.00f, 1.00f, 1.000f }, // Grey
			{ 0.050f, 0.500f, 0.00f, 1.00f }, // Green
			{ 0.500f, 0.000f, 0.500f, 1.00f }, // Purple
	};

	public static final float alpha[][] = { { 0.0f, 0.1f, 0.900f, 0.600f }, // Blue
			{ 1.000f, 0.850f, 0.140f, 0.600f }, // Yellow
			{ 0.750f, 0.020f, 0.020f, 0.600f }, // Red
			{ 0.700f, 0.700f, 0.700f, 0.600f }, // Grey
			{ 0.120f, 0.700f, 0.000f, 0.600f }, // Green
			{ 0.720f, 0.000f, 0.300f, 0.600f } // Purple
	};
}
