package net.osmand.plus.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.osmand.plus.OsmandSettings;
import pl.edu.agh.logic.TrafficDataListener;
import pl.edu.agh.logic.TrafficDataProvider;
import pl.edu.agh.logic.TrafficDataProvider.Status;
import pl.edu.agh.model.RectD;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;
import pl.edu.agh.model.Vector;
import pl.edu.agh.utils.GeometryUtils;
import pl.edu.agh.utils.GeometryUtils.Direction;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.widget.Toast;

public class AGHTrafficLayer implements OsmandMapLayer, TrafficDataListener {

	private static final int MIN_STROKE_WIDTH = 6;
	private static final int MAX_STROKE_WIDTH = 18;

	private static final int ZOOM_FOR_MAX_STROKE_WIDTH = 18;
	private static final double DRAWING_AREA_BOUND_SIZE = 0.02;

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

	private void makeToastInUIThread(final String description) {
		if (!OsmandSettings.getMapActivityEnabled(OsmandSettings.getPrefs(view.getContext()))) {
			return;
		}

		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(view.getContext(), description, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.trafficDataProvider = view.getApplication().getTrafficDataProvider();
		this.view = view;
		this.handler = new Handler();
		trafficDataProvider.registerListener(this);
	}

	@Override
	public void onDraw(Canvas canvas, RectF latlonRect, boolean nightMode) {
		if (view.getZoom() < TrafficDataProvider.MIN_ZOOM) {
			return;
		}

		TrafficData trafficData = trafficDataProvider.getTrafficData(
				new SimpleLocationInfo(view.getLongitude(), view.getLatitude()), view.getZoom(), latlonRect);

		if (trafficData == null) {
			return;
		}

		RectD extendedBound = new RectD(latlonRect).expandByDistance(DRAWING_AREA_BOUND_SIZE);
		for (TrafficInfo trafficInfo : trafficData.getTrafficInfos()) {
			if (GeometryUtils.existsPointInsideRect(trafficInfo.getWay(), extendedBound)) {
				drawTrafficInfo(trafficInfo, canvas);
			}
		}

	}

	private void drawTrafficInfo(TrafficInfo trafficInfo, Canvas canvas) {
		if (trafficInfo.isOneWay()) {
			drawOneWayStreet(trafficInfo, canvas);
		} else {
			drawTwoWayStreet(trafficInfo, canvas);
		}
	}

	private void drawTwoWayStreet(TrafficInfo trafficInfo, Canvas canvas) {
		List<SimpleLocationInfo> way = trafficInfo.getWay();

		for (int i = 1; i < way.size(); i++) {
			drawSection(way.get(i - 1), way.get(i), trafficInfo, canvas);
		}

	}

	private void drawSection(SimpleLocationInfo begin, SimpleLocationInfo end, TrafficInfo trafficInfo, Canvas canvas) {
		int strokeWidth = calculateStrokeWidth();

		Point canvasBegin = new Point(view.getMapXForPoint(begin.getLongitude()), view.getMapYForPoint(begin
				.getLatitude()));
		Point canvasEnd = new Point(view.getMapXForPoint(end.getLongitude()), view.getMapYForPoint(end.getLatitude()));

		Vector directWay = GeometryUtils.translate(new Vector(canvasBegin, canvasEnd), strokeWidth / 2.0,
				Direction.RIGHT);
		drawSectionWithSpeed(directWay, trafficInfo.getDirectWaySpeed(), canvas);

		Vector reverseWay = GeometryUtils.translate(new Vector(canvasBegin, canvasEnd), strokeWidth / 2.0,
				Direction.LEFT);
		drawSectionWithSpeed(reverseWay, trafficInfo.getReverseWaySpeed(), canvas);
	}

	private void drawSectionWithSpeed(Vector section, double speed, Canvas canvas) {
		canvas.drawLine(section.getBegin().x, section.getBegin().y, section.getEnd().x, section.getEnd().y,
				getPaintForSpeed(speed));
	}

	private void drawOneWayStreet(TrafficInfo trafficInfo, Canvas canvas) {
		Path path = createPathFromPoints(trafficInfo.getWay());
		Paint paint = getPaintForSpeed(trafficInfo.getDirectWaySpeed());
		canvas.drawPath(path, paint);
	}

	private Paint getPaintForSpeed(Double speed) {
		Paint paint = new Paint();
		paint.setColor(getColorForSpeed(speed));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(calculateStrokeWidth());
		paint.setAntiAlias(true);
		paint.setStrokeCap(Cap.BUTT);
		paint.setStrokeJoin(Join.BEVEL);
		paint.setAlpha(160);

		return paint;
	}

	private int calculateStrokeWidth() {
		return Math.max(
				(int) Math.round(MAX_STROKE_WIDTH
						/ (Math.pow(1.5,
								ZOOM_FOR_MAX_STROKE_WIDTH - Math.min(view.getZoom(), ZOOM_FOR_MAX_STROKE_WIDTH)))),
				MIN_STROKE_WIDTH);
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
	public void destroyLayer() {
		trafficDataProvider.unregisterListener(this);
	}

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
		view.refreshMap();
	}

	@Override
	public void onStatusChanged(Status status) {
		if (status == Status.ERROR)
			makeToastInUIThread(STATUS_MAPPING.get(status));

	}

}
