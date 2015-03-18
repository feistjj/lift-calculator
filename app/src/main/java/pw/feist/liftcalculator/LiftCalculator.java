package pw.feist.liftcalculator;

import android.graphics.Canvas;
import android.util.Log;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;


import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;


import butterknife.ButterKnife;
import butterknife.OnClick;



public class LiftCalculator extends ActionBarActivity {
    private static final int MAX_PLATES = 10;

    private static final HashMap<Float, Plate> plateMap;
    static
    {
        plateMap = new HashMap<Float, Plate>();
        plateMap.put((float) 45, new Plate((float) 1.0, "#FF0000"));
        plateMap.put((float) 35, new Plate((float) 0.8, "#FFFF00"));
        plateMap.put((float) 25, new Plate((float) 0.6, "#008000"));
        plateMap.put((float) 15, new Plate((float) 0.4, "#0000FF"));
        plateMap.put((float) 10, new Plate((float) 0.3, "#000000"));
        plateMap.put((float) 5, new Plate((float) 0.2, "#888888", (float) 0.6));
        plateMap.put((float) 2.5, new Plate((float) 0.05, "#888888", (float) 0.3));
    }


    Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_calculator);

        Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bg);


        Float [] fakePlates = {(float) 45,( float) 45,( float) 25, (float) 10, (float) 5};
        createPlates(fakePlates);

        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        ll.setBackground(new BitmapDrawable(getResources(), bg));

        ButterKnife.inject(this);
    }

    @OnClick(R.id.calculate)
    void someBroClickedCalaculate(){
        TextView titleBar = (TextView) findViewById(R.id.textfield1);
        titleBar.setText("hello governor");
    }

    private void createPlates(Float [] weights){
        //  sort weightings and divide up useable area
        Plate plate; // plate object
        RectF rect;  // rectangle depicting plate
        float right, left = 0;

        //top and bottom for all will match
        int top = this.canvas.getHeight() / 10;
        int bottom = top + this.canvas.getHeight() / 4; // 25%
        float maxPlateWidth = (float) 0.75 * this.canvas.getWidth() / MAX_PLATES;

        Arrays.sort(weights, Collections.reverseOrder());

        for(Float weight: weights){
            plate = plateMap.get(weight);
            right = left + maxPlateWidth * plate.getWeight();
            rect = new RectF(left, top - ((1 - plate.getHeight()) * ((top - bottom) / 2)),
                    right, bottom + ((1 - plate.getHeight()) * ((top - bottom) / 2)));
            this.canvas.drawRoundRect(rect, 10, 10, plate.getPaint());
            left = right + 3; //for space between plates
            Log.w("WARNING", weight.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lift_calculator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
