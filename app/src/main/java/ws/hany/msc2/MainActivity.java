package ws.hany.msc2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.ImageView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;



public class MainActivity extends AppCompatActivity implements BeaconConsumer {


    double[] centroid;

    private LeastSquaresOptimizer.Optimum optimum;

    double[][] positions = new double[][] { { 25.0, 25.0 }, { 575.0, 25.0 }, { 575.0, 775.0 } };

    final int mapWidth = 600;
    final int mapHeight = 801;
    final int gridSize = 25;




    private BeaconManager beaconManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {

        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();


        Toast.makeText(MainActivity.this, "connecting", Toast.LENGTH_LONG).show();

        final Region myRegion = new Region("Beaons-UUID", Identifier.parse("426C7565-4368-6172-6D42-6561636F6E73"), null, null);


        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Toast.makeText(MainActivity.this, "In UUID area :)", Toast.LENGTH_LONG).show();
            }

            @Override
            public void didExitRegion(Region region) {
                Toast.makeText(MainActivity.this, "Out UUID area :(", Toast.LENGTH_LONG).show();
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Toast.makeText(MainActivity.this, "switched In/Out", Toast.LENGTH_LONG).show();
            }
        });


        try {
            beaconManager.startMonitoringBeaconsInRegion(myRegion);
            beaconManager.startRangingBeaconsInRegion(myRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        final ImageView drawingImageView;
        drawingImageView = (ImageView) this.findViewById(R.id.DrawingImageView);
        Bitmap bitmap = Bitmap.createBitmap(mapWidth, mapHeight, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        final Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);


        final Paint paintFill = new Paint();
        paintFill.setColor(Color.GRAY);
        paintFill.setStyle(Paint.Style.FILL);


        final Paint paintGrid = new Paint();
        paintGrid.setColor(Color.GRAY);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(1);

        // paint Red
        final Paint paintRed = new Paint();
        paintRed.setColor(Color.RED);
        paintRed.setStyle(Paint.Style.STROKE);
        paintRed.setStrokeWidth(15);


        for (int i = 0; i <= mapWidth; i += gridSize) {
            for (int j = 0; j <= mapHeight; j += gridSize) {
                canvas.drawLine(i, 0, i, mapHeight, paintGrid);
                canvas.drawLine(0, j, mapWidth, j, paintGrid);
            }
        }

        // Blue
        final Paint paintBlueFill = new Paint();
        paintBlueFill.setColor(Color.argb(90, 134, 199, 255));
        paintBlueFill.setStyle(Paint.Style.FILL);

        final Paint paintBlueBorder = new Paint();
        paintBlueBorder.setStyle(Paint.Style.STROKE);
        paintBlueBorder.setStrokeWidth(3);
        paintBlueBorder.setColor(Color.rgb(70, 104, 242));

        // Green
        final Paint paintGreenFill = new Paint();
        paintGreenFill.setColor(Color.argb(50, 55, 165, 62));
        paintGreenFill.setStyle(Paint.Style.FILL);

        final Paint paintGreenBorder = new Paint();
        paintGreenBorder.setStyle(Paint.Style.STROKE);
        paintGreenBorder.setStrokeWidth(3);
        paintGreenBorder.setColor(Color.rgb(53, 144, 59));

        // Red
        final Paint paintRedFill = new Paint();
        paintRedFill.setColor(Color.argb(50, 242, 73, 73));
        paintRedFill.setStyle(Paint.Style.FILL);

        final Paint paintRedBorder = new Paint();
        paintRedBorder.setStyle(Paint.Style.STROKE);
        paintRedBorder.setStrokeWidth(3);
        paintRedBorder.setColor(Color.rgb(230, 33, 33));

        // purple
        final Paint paintPurpleFill = new Paint();
        paintPurpleFill.setColor(Color.argb(50, 177, 156, 217));
        paintPurpleFill.setStyle(Paint.Style.FILL);

        final Paint paintPurpleBorder = new Paint();
        paintPurpleBorder.setStyle(Paint.Style.STROKE);
        paintPurpleBorder.setStrokeWidth(3);
        paintPurpleBorder.setColor(Color.rgb(106, 13, 173));

        // Black Text
        final Paint paintBlackText = new Paint();
        paintBlackText.setColor(Color.BLACK);
        paintBlackText.setStyle(Paint.Style.FILL);
        paintBlackText.setTextSize(28);


        // products
        canvas.drawCircle(75 + 13, 275 + 13, 10, paintFill);
        canvas.drawCircle(75 + 13, 275 + 13, 10, paint);
        canvas.drawText("iPhone 12 (2)", 105, 300, paintBlackText);

        canvas.drawCircle(350 + 13, 525 + 13, 10, paintFill);
        canvas.drawCircle(350 + 13, 525 + 13, 10, paint);
        canvas.drawText("Fire Tablet (3)", 380, 550, paintBlackText);

        canvas.drawCircle(275 + 13, 125 + 13, 10, paintFill);
        canvas.drawCircle(275 + 13, 125 + 13, 10, paint);
        canvas.drawText("IPAD MINI (1)", 305, 150, paintBlackText);


        final Path pathPolygon_1 = new Path();
        pathPolygon_1.reset();
        pathPolygon_1.moveTo(50, 50);
        pathPolygon_1.lineTo(50, 175);
        pathPolygon_1.lineTo(100, 175);
        pathPolygon_1.lineTo(100, 50);
        pathPolygon_1.lineTo(50, 50);
        pathPolygon_1.close();
        canvas.drawPath(pathPolygon_1, paintBlueFill);
        canvas.drawPath(pathPolygon_1, paintBlueBorder);

        final Path pathPolygon_2 = new Path();
        pathPolygon_2.reset();
        pathPolygon_2.moveTo(50, 200);
        pathPolygon_2.lineTo(50, 325);
        pathPolygon_2.lineTo(100, 325);
        pathPolygon_2.lineTo(100, 200);
        pathPolygon_2.lineTo(50, 200);
        pathPolygon_2.close();
        canvas.drawPath(pathPolygon_2, paintBlueFill);
        canvas.drawPath(pathPolygon_2, paintBlueBorder);

        final Path pathPolygon_3 = new Path();
        pathPolygon_3.reset();
        pathPolygon_3.moveTo(50, 350);
        pathPolygon_3.lineTo(50, 475);
        pathPolygon_3.lineTo(100, 475);
        pathPolygon_3.lineTo(100, 350);
        pathPolygon_3.lineTo(50, 350);
        pathPolygon_3.close();
        canvas.drawPath(pathPolygon_3, paintBlueFill);
        canvas.drawPath(pathPolygon_3, paintBlueBorder);

        final Path pathPolygon_4 = new Path();
        pathPolygon_4.reset();
        pathPolygon_4.moveTo(50, 500);
        pathPolygon_4.lineTo(50, 625);
        pathPolygon_4.lineTo(100, 625);
        pathPolygon_4.lineTo(100, 500);
        pathPolygon_4.lineTo(50, 500);
        pathPolygon_4.close();
        canvas.drawPath(pathPolygon_4, paintBlueFill);
        canvas.drawPath(pathPolygon_4, paintBlueBorder);

        final Path pathPolygon_5 = new Path();
        pathPolygon_5.reset();
        pathPolygon_5.moveTo(50, 650);
        pathPolygon_5.lineTo(50, 775);
        pathPolygon_5.lineTo(100, 775);
        pathPolygon_5.lineTo(100, 650);
        pathPolygon_5.lineTo(50, 650);
        pathPolygon_5.close();
        canvas.drawPath(pathPolygon_5, paintBlueFill);
        canvas.drawPath(pathPolygon_5, paintBlueBorder);

        final Path pathPolygon_6 = new Path();
        pathPolygon_6.reset();
        pathPolygon_6.moveTo(125, 50);
        pathPolygon_6.lineTo(125, 175);
        pathPolygon_6.lineTo(175, 175);
        pathPolygon_6.lineTo(175, 50);
        pathPolygon_6.lineTo(125, 50);
        pathPolygon_6.close();
        canvas.drawPath(pathPolygon_6, paintGreenFill);
        canvas.drawPath(pathPolygon_6, paintGreenBorder);

        final Path pathPolygon_7 = new Path();
        pathPolygon_7.reset();
        pathPolygon_7.moveTo(125, 200);
        pathPolygon_7.lineTo(125, 325);
        pathPolygon_7.lineTo(175, 325);
        pathPolygon_7.lineTo(175, 200);
        pathPolygon_7.lineTo(125, 200);
        pathPolygon_7.close();
        canvas.drawPath(pathPolygon_7, paintGreenFill);
        canvas.drawPath(pathPolygon_7, paintGreenBorder);

        final Path pathPolygon_8 = new Path();
        pathPolygon_8.reset();
        pathPolygon_8.moveTo(125, 350);
        pathPolygon_8.lineTo(125, 475);
        pathPolygon_8.lineTo(175, 475);
        pathPolygon_8.lineTo(175, 350);
        pathPolygon_8.lineTo(125, 350);
        pathPolygon_8.close();
        canvas.drawPath(pathPolygon_8, paintGreenFill);
        canvas.drawPath(pathPolygon_8, paintGreenBorder);

        final Path pathPolygon_9 = new Path();
        pathPolygon_9.reset();
        pathPolygon_9.moveTo(125, 500);
        pathPolygon_9.lineTo(125, 625);
        pathPolygon_9.lineTo(175, 625);
        pathPolygon_9.lineTo(175, 500);
        pathPolygon_9.lineTo(125, 500);
        pathPolygon_9.close();
        canvas.drawPath(pathPolygon_9, paintGreenFill);
        canvas.drawPath(pathPolygon_9, paintGreenBorder);

        final Path pathPolygon_10 = new Path();
        pathPolygon_10.reset();
        pathPolygon_10.moveTo(125, 650);
        pathPolygon_10.lineTo(125, 775);
        pathPolygon_10.lineTo(175, 775);
        pathPolygon_10.lineTo(175, 650);
        pathPolygon_10.lineTo(125, 650);
        pathPolygon_10.close();
        canvas.drawPath(pathPolygon_10, paintGreenFill);
        canvas.drawPath(pathPolygon_10, paintGreenBorder);

        final Path pathPolygon_11 = new Path();
        pathPolygon_11.reset();
        pathPolygon_11.moveTo(200, 50);
        pathPolygon_11.lineTo(200, 175);
        pathPolygon_11.lineTo(250, 175);
        pathPolygon_11.lineTo(250, 50);
        pathPolygon_11.lineTo(200, 50);
        pathPolygon_11.close();
        canvas.drawPath(pathPolygon_11, paintRedFill);
        canvas.drawPath(pathPolygon_11, paintRedBorder);

        final Path pathPolygon_12 = new Path();
        pathPolygon_12.reset();
        pathPolygon_12.moveTo(200, 200);
        pathPolygon_12.lineTo(200, 325);
        pathPolygon_12.lineTo(250, 325);
        pathPolygon_12.lineTo(250, 200);
        pathPolygon_12.lineTo(200, 200);
        pathPolygon_12.close();
        canvas.drawPath(pathPolygon_12, paintRedFill);
        canvas.drawPath(pathPolygon_12, paintRedBorder);

        final Path pathPolygon_13 = new Path();
        pathPolygon_13.reset();
        pathPolygon_13.moveTo(200, 350);
        pathPolygon_13.lineTo(200, 475);
        pathPolygon_13.lineTo(250, 475);
        pathPolygon_13.lineTo(250, 350);
        pathPolygon_13.lineTo(200, 350);
        pathPolygon_13.close();
        canvas.drawPath(pathPolygon_13, paintRedFill);
        canvas.drawPath(pathPolygon_13, paintRedBorder);

        final Path pathPolygon_14 = new Path();
        pathPolygon_14.reset();
        pathPolygon_14.moveTo(200, 500);
        pathPolygon_14.lineTo(200, 625);
        pathPolygon_14.lineTo(250, 625);
        pathPolygon_14.lineTo(250, 500);
        pathPolygon_14.lineTo(200, 500);
        pathPolygon_14.close();
        canvas.drawPath(pathPolygon_14, paintRedFill);
        canvas.drawPath(pathPolygon_14, paintRedBorder);

        final Path pathPolygon_15 = new Path();
        pathPolygon_15.reset();
        pathPolygon_15.moveTo(200, 650);
        pathPolygon_15.lineTo(200, 775);
        pathPolygon_15.lineTo(250, 775);
        pathPolygon_15.lineTo(250, 650);
        pathPolygon_15.lineTo(200, 650);
        pathPolygon_15.close();
        canvas.drawPath(pathPolygon_15, paintRedFill);
        canvas.drawPath(pathPolygon_15, paintRedBorder);


        // Purple
        final Path pathPolygon_16 = new Path();
        pathPolygon_16.reset();
        pathPolygon_16.moveTo(275, 50);
        pathPolygon_16.lineTo(275, 175);
        pathPolygon_16.lineTo(325, 175);
        pathPolygon_16.lineTo(325, 50);
        pathPolygon_16.lineTo(275, 50);
        pathPolygon_16.close();
        canvas.drawPath(pathPolygon_16, paintPurpleFill);
        canvas.drawPath(pathPolygon_16, paintPurpleBorder);

        final Path pathPolygon_17 = new Path();
        pathPolygon_17.reset();
        pathPolygon_17.moveTo(275, 200);
        pathPolygon_17.lineTo(275, 325);
        pathPolygon_17.lineTo(325, 325);
        pathPolygon_17.lineTo(325, 200);
        pathPolygon_17.lineTo(275, 200);
        pathPolygon_17.close();
        canvas.drawPath(pathPolygon_17, paintPurpleFill);
        canvas.drawPath(pathPolygon_17, paintPurpleBorder);

        final Path pathPolygon_18 = new Path();
        pathPolygon_18.reset();
        pathPolygon_18.moveTo(275, 350);
        pathPolygon_18.lineTo(275, 475);
        pathPolygon_18.lineTo(325, 475);
        pathPolygon_18.lineTo(325, 350);
        pathPolygon_18.lineTo(275, 350);
        pathPolygon_18.close();
        canvas.drawPath(pathPolygon_18, paintPurpleFill);
        canvas.drawPath(pathPolygon_18, paintPurpleBorder);

        final Path pathPolygon_19 = new Path();
        pathPolygon_19.reset();
        pathPolygon_19.moveTo(275, 500);
        pathPolygon_19.lineTo(275, 625);
        pathPolygon_19.lineTo(325, 625);
        pathPolygon_19.lineTo(325, 500);
        pathPolygon_19.lineTo(275, 500);
        pathPolygon_19.close();
        canvas.drawPath(pathPolygon_19, paintPurpleFill);
        canvas.drawPath(pathPolygon_19, paintPurpleBorder);

        final Path pathPolygon_20 = new Path();
        pathPolygon_20.reset();
        pathPolygon_20.moveTo(275, 650);
        pathPolygon_20.lineTo(275, 775);
        pathPolygon_20.lineTo(325, 775);
        pathPolygon_20.lineTo(325, 650);
        pathPolygon_20.lineTo(275, 650);
        pathPolygon_20.close();
        canvas.drawPath(pathPolygon_20, paintPurpleFill);
        canvas.drawPath(pathPolygon_20, paintPurpleBorder);

        // Blue
        final Path pathPolygon_21 = new Path();
        pathPolygon_21.reset();
        pathPolygon_21.moveTo(350, 50);
        pathPolygon_21.lineTo(350, 175);
        pathPolygon_21.lineTo(400, 175);
        pathPolygon_21.lineTo(400, 50);
        pathPolygon_21.lineTo(350, 50);
        pathPolygon_21.close();
        canvas.drawPath(pathPolygon_21, paintBlueFill);
        canvas.drawPath(pathPolygon_21, paintBlueBorder);

        final Path pathPolygon_22 = new Path();
        pathPolygon_22.reset();
        pathPolygon_22.moveTo(350, 200);
        pathPolygon_22.lineTo(350, 325);
        pathPolygon_22.lineTo(400, 325);
        pathPolygon_22.lineTo(400, 200);
        pathPolygon_22.lineTo(350, 200);
        pathPolygon_22.close();
        canvas.drawPath(pathPolygon_22, paintBlueFill);
        canvas.drawPath(pathPolygon_22, paintBlueBorder);

        final Path pathPolygon_23 = new Path();
        pathPolygon_23.reset();
        pathPolygon_23.moveTo(350, 350);
        pathPolygon_23.lineTo(350, 475);
        pathPolygon_23.lineTo(400, 475);
        pathPolygon_23.lineTo(400, 350);
        pathPolygon_23.lineTo(350, 350);
        pathPolygon_23.close();
        canvas.drawPath(pathPolygon_23, paintBlueFill);
        canvas.drawPath(pathPolygon_23, paintBlueBorder);

        final Path pathPolygon_24 = new Path();
        pathPolygon_24.reset();
        pathPolygon_24.moveTo(350, 500);
        pathPolygon_24.lineTo(350, 625);
        pathPolygon_24.lineTo(400, 625);
        pathPolygon_24.lineTo(400, 500);
        pathPolygon_24.lineTo(350, 500);
        pathPolygon_24.close();
        canvas.drawPath(pathPolygon_24, paintBlueFill);
        canvas.drawPath(pathPolygon_24, paintBlueBorder);

        final Path pathPolygon_25 = new Path();
        pathPolygon_25.reset();
        pathPolygon_25.moveTo(350, 650);
        pathPolygon_25.lineTo(350, 775);
        pathPolygon_25.lineTo(400, 775);
        pathPolygon_25.lineTo(400, 650);
        pathPolygon_25.lineTo(350, 650);
        pathPolygon_25.close();
        canvas.drawPath(pathPolygon_25, paintBlueFill);
        canvas.drawPath(pathPolygon_25, paintBlueBorder);

        // Green
        final Path pathPolygon_26 = new Path();
        pathPolygon_26.reset();
        pathPolygon_26.moveTo(425, 50);
        pathPolygon_26.lineTo(425, 175);
        pathPolygon_26.lineTo(475, 175);
        pathPolygon_26.lineTo(475, 50);
        pathPolygon_26.lineTo(425, 50);
        pathPolygon_26.close();
        canvas.drawPath(pathPolygon_26, paintGreenFill);
        canvas.drawPath(pathPolygon_26, paintGreenBorder);

        final Path pathPolygon_27 = new Path();
        pathPolygon_27.reset();
        pathPolygon_27.moveTo(425, 200);
        pathPolygon_27.lineTo(425, 325);
        pathPolygon_27.lineTo(475, 325);
        pathPolygon_27.lineTo(475, 200);
        pathPolygon_27.lineTo(425, 200);
        pathPolygon_27.close();
        canvas.drawPath(pathPolygon_27, paintGreenFill);
        canvas.drawPath(pathPolygon_27, paintGreenBorder);

        final Path pathPolygon_28 = new Path();
        pathPolygon_28.reset();
        pathPolygon_28.moveTo(425, 350);
        pathPolygon_28.lineTo(425, 475);
        pathPolygon_28.lineTo(475, 475);
        pathPolygon_28.lineTo(475, 350);
        pathPolygon_28.lineTo(425, 350);
        pathPolygon_28.close();
        canvas.drawPath(pathPolygon_28, paintGreenFill);
        canvas.drawPath(pathPolygon_28, paintGreenBorder);

        final Path pathPolygon_29 = new Path();
        pathPolygon_29.reset();
        pathPolygon_29.moveTo(425, 500);
        pathPolygon_29.lineTo(425, 625);
        pathPolygon_29.lineTo(475, 625);
        pathPolygon_29.lineTo(475, 500);
        pathPolygon_29.lineTo(425, 500);
        pathPolygon_29.close();
        canvas.drawPath(pathPolygon_29, paintGreenFill);
        canvas.drawPath(pathPolygon_29, paintGreenBorder);

        final Path pathPolygon_30 = new Path();
        pathPolygon_30.reset();
        pathPolygon_30.moveTo(425, 650);
        pathPolygon_30.lineTo(425, 775);
        pathPolygon_30.lineTo(475, 775);
        pathPolygon_30.lineTo(475, 650);
        pathPolygon_30.lineTo(425, 650);
        pathPolygon_30.close();
        canvas.drawPath(pathPolygon_30, paintGreenFill);
        canvas.drawPath(pathPolygon_30, paintGreenBorder);

        // Red
        final Path pathPolygon_31 = new Path();
        pathPolygon_31.reset();
        pathPolygon_31.moveTo(500, 50);
        pathPolygon_31.lineTo(500, 175);
        pathPolygon_31.lineTo(550, 175);
        pathPolygon_31.lineTo(550, 50);
        pathPolygon_31.lineTo(500, 50);
        pathPolygon_31.close();
        canvas.drawPath(pathPolygon_31, paintRedFill);
        canvas.drawPath(pathPolygon_31, paintRedBorder);

        final Path pathPolygon_32 = new Path();
        pathPolygon_32.reset();
        pathPolygon_32.moveTo(500, 200);
        pathPolygon_32.lineTo(500, 325);
        pathPolygon_32.lineTo(550, 325);
        pathPolygon_32.lineTo(550, 200);
        pathPolygon_32.lineTo(500, 200);
        pathPolygon_32.close();
        canvas.drawPath(pathPolygon_32, paintRedFill);
        canvas.drawPath(pathPolygon_32, paintRedBorder);

        final Path pathPolygon_33 = new Path();
        pathPolygon_33.reset();
        pathPolygon_33.moveTo(500, 350);
        pathPolygon_33.lineTo(500, 475);
        pathPolygon_33.lineTo(550, 475);
        pathPolygon_33.lineTo(550, 350);
        pathPolygon_33.lineTo(500, 350);
        pathPolygon_33.close();
        canvas.drawPath(pathPolygon_33, paintRedFill);
        canvas.drawPath(pathPolygon_33, paintRedBorder);

        final Path pathPolygon_34 = new Path();
        pathPolygon_34.reset();
        pathPolygon_34.moveTo(500, 500);
        pathPolygon_34.lineTo(500, 625);
        pathPolygon_34.lineTo(550, 625);
        pathPolygon_34.lineTo(550, 500);
        pathPolygon_34.lineTo(500, 500);
        pathPolygon_34.close();
        canvas.drawPath(pathPolygon_34, paintRedFill);
        canvas.drawPath(pathPolygon_34, paintRedBorder);

        final Path pathPolygon_35 = new Path();
        pathPolygon_35.reset();
        pathPolygon_35.moveTo(500, 650);
        pathPolygon_35.lineTo(500, 775);
        pathPolygon_35.lineTo(550, 775);
        pathPolygon_35.lineTo(550, 650);
        pathPolygon_35.lineTo(500, 650);
        pathPolygon_35.close();
        canvas.drawPath(pathPolygon_35, paintRedFill);
        canvas.drawPath(pathPolygon_35, paintRedBorder);


        // sides
        final Path pathPolygon_36 = new Path();
        pathPolygon_36.reset();
        pathPolygon_36.moveTo(0, 50);
        pathPolygon_36.lineTo(0, 775);
        pathPolygon_36.lineTo(25, 775);
        pathPolygon_36.lineTo(25, 50);
        pathPolygon_36.lineTo(0, 50);
        pathPolygon_36.close();
        canvas.drawPath(pathPolygon_36, paintPurpleFill);
        canvas.drawPath(pathPolygon_36, paintPurpleBorder);

        final Path pathPolygon_37 = new Path();
        pathPolygon_37.reset();
        pathPolygon_37.moveTo(0, 0);
        pathPolygon_37.lineTo(600, 0);
        pathPolygon_37.lineTo(600, 25);
        pathPolygon_37.lineTo(0, 25);
        pathPolygon_37.lineTo(0, 0);
        pathPolygon_37.close();
        canvas.drawPath(pathPolygon_37, paintPurpleFill);
        canvas.drawPath(pathPolygon_37, paintPurpleBorder);

        final Path pathPolygon_38 = new Path();
        pathPolygon_38.reset();
        pathPolygon_38.moveTo(575, 50);
        pathPolygon_38.lineTo(575, 775);
        pathPolygon_38.lineTo(600, 775);
        pathPolygon_38.lineTo(600, 50);
        pathPolygon_38.lineTo(575, 50);
        pathPolygon_38.close();
        canvas.drawPath(pathPolygon_38, paintPurpleFill);
        canvas.drawPath(pathPolygon_38, paintPurpleBorder);





        beaconManager.addRangeNotifier(new RangeNotifier() {


            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {



                List<Double> list = new ArrayList<Double>();
                list.clear();
                list.add( 100.0);
                list.add( 100.0);
                list.add( 100.0);


                for (final Beacon oneBeacon : beacons) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


//                            Toast.makeText(MainActivity.this, "list: " + list, Toast.LENGTH_LONG).show();


                            if (oneBeacon.getId3().toInt() == 1) {
                                list.set(0, (double) oneBeacon.getDistance());
                                Toast.makeText(MainActivity.this, "B1: " + list.get(0), Toast.LENGTH_LONG).show();
                            }
                            if (oneBeacon.getId3().toInt() == 2) {
                                list.set(1, (double) oneBeacon.getDistance());
                                Toast.makeText(MainActivity.this, "B2: " + list.get(1), Toast.LENGTH_LONG).show();
                            }
                            if (oneBeacon.getId3().toInt() == 3) {
                                list.set(2, (double) oneBeacon.getDistance());
                                Toast.makeText(MainActivity.this, "B3: " + list.get(2), Toast.LENGTH_LONG).show();
                            }


                            if (list.get(0) != 100.0 && list.get(1) != 100.0 && list.get(2) != 100.0) {

                                double[] distances = new double[] { list.get(0), list.get(1),  list.get(2) };
                                NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
                                optimum = solver.solve();

                                centroid = optimum.getPoint().toArray();

                                Toast.makeText(MainActivity.this, "centroid: " + Arrays.toString(centroid), Toast.LENGTH_LONG).show();

                                drawingImageView.invalidate();
                                canvas.drawCircle((float) centroid[0], (float) centroid[1], 4, paintRed);

                            }


                        }
                    });

                }//foreach


            }
        });

    }

}