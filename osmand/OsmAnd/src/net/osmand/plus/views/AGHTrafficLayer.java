package net.osmand.plus.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pl.edu.agh.cache.TrafficDataListener;
import pl.edu.agh.cache.TrafficDataProvider;
import pl.edu.agh.cache.TrafficDataProvider.Status;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.widget.Toast;

public class AGHTrafficLayer implements OsmandMapLayer, TrafficDataListener {
	
	private static final Map<TrafficDataProvider.Status, String> STATUS_MAPPING = new HashMap<TrafficDataProvider.Status, String>() {
		{
			put(Status.FETCHING, "Fetching traffic data...");
			put(Status.ERROR, "Error during fetching traffic data.");
			put(Status.COMPLETED, "Traffic data has been fetched.");
		}
	};
		
	private static final SortedMap<Double, Integer> SPEED_MAPPING = new TreeMap<Double, Integer>() {
		{
			put(15.0, 0x970000);
			put(30.0, Color.RED);
			put(45.0, 0xFF7F27);
			put(60.0, 0xFFEF00);
			put(Double.MAX_VALUE, 0x00DA00);
		}
	};
	
	private OsmandMapTileView view;
	private Handler handler;
	private TrafficDataProvider trafficDataProvider;
	
	private TrafficData trafficData;
	
	private boolean visible = false;
	
	private synchronized TrafficData getTrafficData() {
		return trafficData;
	}

	private synchronized void setTrafficData(TrafficData trafficData) {
		this.trafficData = trafficData;
	}

	protected void makeToastInUIThread(final String description) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(view.getContext(), description, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public boolean isVisible() {
		return visible;
	}
		
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.trafficDataProvider = TrafficDataProvider.getInstance();
		this.view = view;
		this.handler = new Handler();
		trafficDataProvider.registerListener(this);
	}

	@Override
	public void onDraw(Canvas canvas, RectF latlonRect, boolean nightMode) {
		if (!visible) {
			return;
		}
		
		trafficDataProvider.getTrafficDataAsync(new SimpleLocationInfo(19.9245182, 50.0651936), view.getZoom());
		
		TrafficData trafficData = getTrafficData();
		if (trafficData == null) {
			return;
		}
		
		for (TrafficInfo trafficInfo : trafficData.getTrafficInfos()) {
			Path path = createPathFromPoints(trafficInfo.getWay());
			Paint paint = getPaintForSpeed(trafficInfo.getDirectWaySpeed());
			canvas.drawPath(path, paint);
		}
		
	}
	
	private Paint getPaintForSpeed(Double directWaySpeed) {
		Paint paint = new Paint();
		paint.setColor(getColorForSpeed(directWaySpeed));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(calculateStrokeWidth());
		paint.setAntiAlias(true);
		paint.setStrokeCap(Cap.BUTT);
		paint.setStrokeJoin(Join.BEVEL);
		paint.setAlpha(160);
		
		return paint;
	}

	private int calculateStrokeWidth() {
		return Math.max(18 / (int) (Math.pow(1.5, 18 - Math.min(view.getZoom(), 18))), 6);
	}

	private int getColorForSpeed(Double directWaySpeed) {
		Map.Entry<Double, Integer> entry = null;
		Iterator<Map.Entry<Double, Integer>> it = SPEED_MAPPING.entrySet().iterator();
		while (it.hasNext()) {
			entry = (Map.Entry<Double, Integer>) it.next();
			if (entry.getKey() > directWaySpeed)
				break;
		}
		return entry.getValue();
	}

	private Path createPathFromPoints(List<SimpleLocationInfo> way) {
		Path path = new Path();
		
		int x = view.getMapXForPoint(way.get(0).getLongitude());
		int y = view.getMapYForPoint(way.get(0).getLatitude());
		path.moveTo(x, y);
		
		for (int i = 1; i < way.size(); i++) {
			SimpleLocationInfo point = way.get(i);
			x = view.getMapXForPoint(point.getLongitude());
			y = view.getMapYForPoint(point.getLatitude());
			path.lineTo(x, y);
		}
		
		return path;
	}

	@Override
	public void destroyLayer() {}

	@Override
	public boolean onTouchEvent(PointF point) {
		return false;
	}

	@Override
	public boolean onLongPressEvent(PointF point) {
		return false;
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public void onDataProvided(TrafficData trafficData) {
		setTrafficData(trafficData);
		view.refreshMap();
		
	}

	@Override
	public void onStatusChanged(Status status) {
		makeToastInUIThread(STATUS_MAPPING.get(status));
		
	}

}
