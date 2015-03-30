package pw.feist.liftcalculator;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;


import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;


public class LiftCalculator extends ActionBarActivity {

    private static final int MAX_PLATES = 6;

    private static final float [] platesInLbs = {45, 35, 25, 15, 10, 5, (float) 2.5};
    private static final float [] platesInKilos = { 25, 20, 15, 10, 5, (float) 2.5};

    private static final HashMap<Float, Plate> plateMapLbs;
    static
    {
        plateMapLbs = new HashMap<Float, Plate>();
        plateMapLbs.put((float) 45, new Plate((float) 1.0, "#FF0000")); //red
        plateMapLbs.put((float) 35, new Plate((float) 0.8, "#FFFF00")); //yellow
        plateMapLbs.put((float) 25, new Plate((float) 0.6, "#008000")); //green
        plateMapLbs.put((float) 15, new Plate((float) 0.5, "#0000FF")); //blue
        plateMapLbs.put((float) 10, new Plate((float) 0.4, "#000000")); //black
        plateMapLbs.put((float) 5, new Plate((float) 0.3, "#888888", (float) 0.6));  //gray
        plateMapLbs.put((float) 2.5, new Plate((float) 0.2, "#888888", (float) 0.4)); //gray
    }
    private static final HashMap<Float, Plate> plateMapKilos;
    static
    {
        plateMapKilos = new HashMap<Float, Plate>();
        plateMapKilos.put((float) 25, new Plate((float) 1.0, "#FF0000"));
        plateMapKilos.put((float) 20, new Plate((float) 0.7, "#FFFF00"));
        plateMapKilos.put((float) 15, new Plate((float) 0.6, "#008000"));
        plateMapKilos.put((float) 10, new Plate((float) 0.5, "#0000FF"));
        plateMapKilos.put((float) 5, new Plate((float) 0.4, "#000000"));
        plateMapKilos.put((float) 2.5, new Plate((float) 0.2, "#888888", (float) 0.6));

    }

    private List<Button> weightButtons;
    private static final int[] BUTTON_IDS = {
            R.id.weightSelector0,
            R.id.weightSelector1,
            R.id.weightSelector2,
            R.id.weightSelector3,
            R.id.weightSelector4,
            R.id.weightSelector5,
            R.id.weightSelector6,
    };

    private Canvas canvas;
    private int barWeight;
    boolean lbsIsRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_calculator);

        //plate bitmap
        Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888); // not sure about the size
        this.canvas = new Canvas(bg);
        ImageView ll = (ImageView) findViewById(R.id.plates);
        ll.setBackground(new BitmapDrawable(getResources(), bg));

        ButterKnife.inject(this);
        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.barWeight = settings.getInt("barWeight", 45);
        this.lbsIsRegion = settings.getBoolean("lbsIsRegion", true);

        Spinner regionSelect = (Spinner) findViewById(R.id.regionSelect);
        int selection = this.lbsIsRegion ? 0 : 1;
        regionSelect.setSelection(selection);
        createBarSpinner();
        createWeightButtons();

    }

    @Override
    protected void onStop(){
        super.onStop();
        saveSettings();
    }

    void saveSettings(){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putInt("barWeight", this.barWeight);
        editor.putBoolean("lbsIsRegion", this.lbsIsRegion);

        // Commit the edits!
        editor.commit();
    }


    void createBarSpinner(){
        List<String> spinnerArray = new ArrayList<String>();
        if (this.lbsIsRegion){
            spinnerArray.add("45 Lbs");
            spinnerArray.add("35 Lbs");
        }
        else{
            spinnerArray.add("20 Kilos");
            spinnerArray.add("15 Kilos");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner barSelect = (Spinner) findViewById(R.id.barSelect);
        String selected = barSelect.getSelectedItem().toString().toLowerCase();
        barSelect.setAdapter(adapter);
        if(this.lbsIsRegion) {
            if (selected.equals("15 kilos") || this.barWeight == 35){
                barSelect.setSelection(1);
            }
        }
        else if(selected.equals("35 lbs") || this.barWeight == 15){
            barSelect.setSelection(1);
        }
    }

    @OnItemSelected(value = R.id.barSelect)
    void changeBarWeight(){
        Spinner barSelect = (Spinner) findViewById(R.id.barSelect);
        String selected = barSelect.getSelectedItem().toString().toLowerCase();
        this.barWeight = Integer.valueOf(selected.substring(0,2));
    }


    @OnItemSelected(value = R.id.regionSelect)
    void changeRegion(){
        Spinner region = (Spinner) findViewById(R.id.regionSelect);
        String text = region.getSelectedItem().toString();
        BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
        Integer userWeight = 0;
        try {
            userWeight = Integer.valueOf(weightField.getText().toString());
        }
        catch (NumberFormatException ex){
            //pass
        }
        weightField.setText("");  // in case it's zero

        if(text.toLowerCase().equals("lbs")){
            this.lbsIsRegion = true;
            weightField.setHint(R.string.weight_lbs);

            if (userWeight > 0){
                userWeight = (int) (userWeight * 2.20462);
                weightField.setText(userWeight.toString());
            }

        }
        else{
            this.lbsIsRegion = false;
            weightField.setHint(R.string.weight_kilos);
            if (userWeight > 0){
                userWeight = (int) (userWeight / 2.20462);
                weightField.setText(userWeight.toString());
            }
        }
        createBarSpinner();
    }

    @OnTextChanged(value = R.id.weightField)
    @OnItemSelected(value = R.id.barSelect)
    void calculateWeight(){
        float userWeight;
        BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
        this.canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        try {
            userWeight = Float.valueOf(weightField.getText().toString());
        }
        catch (NumberFormatException ex){
            return; // probably empty
        }
        if(userWeight <= this.barWeight) {
            return;
        }
        userWeight = (userWeight - this.barWeight)/2;
        int quotient;
        List<Float> ourPlates = new ArrayList<Float>();
        float [] standardPlates = this.lbsIsRegion ? platesInLbs : platesInKilos;
        for (float weight: standardPlates){
            quotient = (int) (userWeight / weight);
            if (quotient > 0){
                for (int ii = 0; ii < quotient; ii ++){
                    ourPlates.add(weight);
                }
            userWeight -= (int) (quotient * weight);
            if(userWeight == 0)  //avoid divide by zero errors
                break;
            }
        }
        createPlates(ourPlates);
    }



    private void createPlates(List<Float> weights){
        //  sort weightings and divide up useable area
        Plate plate; // plate object
        RectF rect;  // rectangle depicting plate
        float right, left = 0;

        //top and bottom for all will match
        int top = 0;
        int bottom = this.canvas.getHeight();
        int spacing = 3;
        int maxPlates = weights.size() < MAX_PLATES ? MAX_PLATES : weights.size();
        float maxPlateWidth = (this.canvas.getWidth() - (maxPlates * spacing))  / maxPlates;

        HashMap<Float, Plate> plateMap = this.lbsIsRegion ? plateMapLbs : plateMapKilos;

        for(Float weight: weights){
            plate = plateMap.get(weight);
            right = left + maxPlateWidth * plate.getWeight();
            rect = new RectF(left, top - ((1 - plate.getHeight()) * ((top - bottom) / 2)),
                    right, bottom + ((1 - plate.getHeight()) * ((top - bottom) / 2)));
            this.canvas.drawRoundRect(rect, 10, 10, plate.getPaint());
            left = right + spacing; //for space between plates
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

    void createWeightButtons(){
        this.weightButtons = new ArrayList<Button>();
        int ii = 0;
        String value;
        for(int id: BUTTON_IDS){
            Button button =  (Button) findViewById(id).findViewById(R.id.button);
            if((int) this.platesInLbs[ii] == this.platesInLbs[ii])
                value = String.valueOf((int) this.platesInLbs[ii]);
            else
                value = String.valueOf(this.platesInLbs[ii]);
            button.setText(value);
            this.weightButtons.add(button);
            ii++;
        }
    }

}
