package pw.feist.liftcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.TextView;


import com.beardedhen.androidbootstrap.BootstrapEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;


public class LiftCalculator extends Activity {

    private static final int MAX_PLATES = 6;
    private static final int MAX_WEIGHT = 999;

    private static final String EDIT_TAG = "APP_EDIT";

    private static final Float[] platesInLbs = {(float) 45, (float) 35, (float) 25,
            (float) 15, (float) 10, (float) 5, (float) 2.5};
    private static final Float[] platesInKilos = {(float) 25, (float) 20, (float) 15, (float) 10,
            (float) 5, (float) 2.5, (float) 1.25};

    private static final HashMap<Float, Plate> plateMapLbs;

    static {
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

    static {
        plateMapKilos = new HashMap<Float, Plate>();
        plateMapKilos.put((float) 25, new Plate((float) 1.0, "#FF0000"));
        plateMapKilos.put((float) 20, new Plate((float) 0.8, "#FFFF00"));
        plateMapKilos.put((float) 15, new Plate((float) 0.6, "#008000"));
        plateMapKilos.put((float) 10, new Plate((float) 0.5, "#0000FF"));
        plateMapKilos.put((float) 5, new Plate((float) 0.4, "#000000"));
        plateMapKilos.put((float) 2.5, new Plate((float) 0.3, "#888888", (float) 0.6));  //gray
        plateMapKilos.put((float) 1.25, new Plate((float) 0.2, "#888888", (float) 0.4)); //gray

    }

    @InjectViews({
            R.id.weightSelector0,
            R.id.weightSelector1,
            R.id.weightSelector2,
            R.id.weightSelector3,
            R.id.weightSelector4,
            R.id.weightSelector5,
            R.id.weightSelector6,
    })
    LinearLayout[] WeightLayouts;

    private Canvas canvas;
    private int barWeight;
    private List<Float> currentPlates;
    boolean lbsIsRegion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_calculator);

        //plate bitmap
        Bitmap bg = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888); // not sure about the size
        this.canvas = new Canvas(bg);
        ImageView ll = (ImageView) findViewById(R.id.plates);
        ll.setBackground(new BitmapDrawable(getResources(), bg));

        ButterKnife.inject(this);

        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.barWeight = settings.getInt("barWeight", 45);
        this.lbsIsRegion = settings.getBoolean("lbsIsRegion", true);
        if (this.lbsIsRegion)
            this.currentPlates = new ArrayList<Float>(Arrays.asList(this.platesInLbs));
        else
            this.currentPlates = new ArrayList<Float>(Arrays.asList(this.platesInKilos));

        Spinner regionSelect = (Spinner) findViewById(R.id.regionSelect);
        int selection = this.lbsIsRegion ? 0 : 1;
        regionSelect.setSelection(selection);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        createBarSpinner();
        weightButtonsUpdateRegion();


        // add watcher to text field
        TextView textField;
        for(LinearLayout layout: WeightLayouts){
            textField = (TextView) layout.findViewById(R.id.plateCount);
            textField.addTextChangedListener(new PlateWatcher(this, textField));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }

    void saveSettings() {
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


    void  createBarSpinner() {
        List<String> spinnerArray = new ArrayList<String>();
        if (this.lbsIsRegion) {
            spinnerArray.add("45 Lbs");
            spinnerArray.add("35 Lbs");
        } else {
            spinnerArray.add("20 Kilos");
            spinnerArray.add("15 Kilos");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner barSelect = (Spinner) findViewById(R.id.barSelect);
        String selected = barSelect.getSelectedItem().toString().toLowerCase();
        barSelect.setAdapter(adapter);
        if (this.lbsIsRegion) {
            if (selected.equals("15 kilos") || this.barWeight == 35) {
                barSelect.setSelection(1);
            }
        } else if (selected.equals("35 lbs") || this.barWeight == 15) {
            barSelect.setSelection(1);
        }
        barSelect.setTag(EDIT_TAG);
    }

    @OnItemSelected(value = R.id.barSelect)
    void changeBarWeight() {
        Spinner barSelect = (Spinner) findViewById(R.id.barSelect);
        if(barSelect.getTag() == EDIT_TAG){
            barSelect.setTag(null);
            return; //non-user edit
        }
        BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
        int otherPostion = barSelect.getSelectedItemPosition() == 0 ? 1 : 0;
        String selected = barSelect.getSelectedItem().toString();
        String other = barSelect.getItemAtPosition(otherPostion).toString();
        int otherInt = Integer.valueOf(other.substring(0, 2));
        barWeight = Integer.valueOf(selected.substring(0, 2));
        int currentValue;
        try {
            currentValue = Integer.valueOf(weightField.getText().toString());
        } catch (NumberFormatException ex) {
            return;
        }
        if (currentValue == 0)
            return;

        calculateWeight();

    }


    @OnItemSelected(value = R.id.regionSelect)
    void changeRegion() {
        Spinner region = (Spinner) findViewById(R.id.regionSelect);
        String text = region.getSelectedItem().toString();
        BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
        Integer userWeight = 0;
        try {
            userWeight = Integer.valueOf(weightField.getText().toString());
        } catch (NumberFormatException ex) {
            //pass
        }
        weightField.setText("");  // in case it's zero

        if (text.toLowerCase().equals("lbs")) {
            this.lbsIsRegion = true;
            this.currentPlates = new ArrayList<Float>(Arrays.asList(this.platesInLbs));
            weightField.setHint(R.string.weight_lbs);

            if (userWeight > 0) {
                userWeight = (int) (userWeight * 2.20462);
            }

        } else {
            this.lbsIsRegion = false;
            this.currentPlates = new ArrayList<Float>(Arrays.asList(this.platesInKilos));
            weightField.setHint(R.string.weight_kilos);
            if (userWeight > 0) {
                userWeight = (int) (userWeight * 0.453592);
            }
        }
        weightButtonsUpdateRegion();
        if(userWeight > 0)
            weightField.setText(userWeight.toString());
        createBarSpinner();





    }

    @OnTextChanged(value = R.id.weightField)
    void calculateWeight() {
        float userWeight;
        BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
        //someone already did the rest for us
        if (weightField.getTag() == EDIT_TAG) {
            weightField.setTag(null);
            return;
        }

        this.canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        HashMap<Float, Integer> ourPlates = new HashMap<>();
        try {
            userWeight = Float.valueOf(weightField.getText().toString());
        } catch (NumberFormatException ex) {
            setWeightFields(ourPlates); //clear
            return; // probably empty
        }

        if (userWeight <= this.barWeight) {
            setWeightFields(ourPlates); //clear
            return;
        }
        userWeight = (userWeight - this.barWeight) / 2;
        int quotient;

        for (float weight : this.currentPlates) {
            quotient = (int) (userWeight / weight);
            ourPlates.put(weight, quotient);
            userWeight -= (int) (quotient * weight);
            if (userWeight == 0)  //avoid divide by zero errors
                break;

        }
        drawPlates(ourPlates);
        setWeightFields(ourPlates);
    }


    private void drawPlates(HashMap<Float, Integer> drawPlatesMap) {
        //  sort weightings and divide up useable area
        Plate plate; // plate object
        RectF rect;  // rectangle depicting plate
        int right, left = 2;

        //top and bottom for all will match

        int top = 2; //to show stroke
        int bottom = this.canvas.getHeight() - 2;
        int spacing = 3;
        int drawLength = 0;
        for (int count : drawPlatesMap.values())
            drawLength += count;

        int maxPlates = drawLength < MAX_PLATES ? MAX_PLATES : drawLength;
        float maxPlateWidth = (this.canvas.getWidth() - (maxPlates * spacing)) / maxPlates;

        HashMap<Float, Plate> plateMap = this.lbsIsRegion ? plateMapLbs : plateMapKilos;
        Paint outline = new Paint();
        outline.setStyle(Paint.Style.STROKE);
        outline.setColor(Color.BLACK);
        outline.setStrokeWidth(2);

        // this is lame, maybe new object
        List<Float> keys = new ArrayList<>(drawPlatesMap.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);

        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        for (Float weight : keys) {
            plate = plateMap.get(weight);
            int plateSize = (int) (maxPlateWidth * plate.getWeight());

            for (int ii = 0; ii < drawPlatesMap.get(weight); ii++) {
                right = left + plateSize;
                rect = new RectF(left, top - ((1 - plate.getHeight()) * ((top - bottom) / 2)),
                        right, bottom + ((1 - plate.getHeight()) * ((top - bottom) / 2)));
                this.canvas.drawRoundRect(rect, 10, 10, plate.getPaint());
                this.canvas.drawRoundRect(rect, 10, 10, outline);
                left = right + spacing; //for space between plates
            }

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

    public void weightButtonClicked(View view) {
        View parent = (View) view.getParent();
        TextView plateCount = (TextView) parent.findViewById(R.id.plateCount);
        Button button = (Button) view;
        Float plate = Float.valueOf((String) button.getText());
        if (plateCount.isFocusable()) {
            plateCount.setFocusable(false);
            plateCount.setBackgroundColor(Color.GRAY);
            plateCount.setText("0");
            plateCount.setClickable(false);
            this.currentPlates.remove(plate);
        } else {
            setPlateCountDefaultState(plateCount);
            this.currentPlates.add(plate);
            Collections.sort(currentPlates);
            Collections.reverse(currentPlates);
            plateCount.setClickable(true);
        }
    }

    void setPlateCountDefaultState(TextView textView) {
        textView.setFocusable(true);
        textView.setBackgroundResource(R.drawable.edittext_background);
    }

    void setWeightFields(HashMap<Float, Integer> plates) {
        int ii = 0;
        Float[] allPlates = this.lbsIsRegion ? this.platesInLbs : this.platesInKilos;
        for (LinearLayout layout : WeightLayouts) {
            int plateCount = plates.containsKey(allPlates[ii]) ? plates.get(allPlates[ii]) : 0;
            plateCount = plateCount > 9 ? 9 : plateCount;
            TextView platePicker = (TextView) layout.findViewById(R.id.plateCount);
            platePicker.setTag(EDIT_TAG);
            platePicker.setText(String.valueOf(plateCount));
            ii++;
        }
    }

    void createNumberPickerDialog(final TextView textView){
        RelativeLayout linearLayout = new RelativeLayout(this);
        final NumberPicker numberPicker = new NumberPicker(this);

        numberPicker.setMaxValue(9);
        numberPicker.setMinValue(0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(numberPicker, numPicerParams);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Select Plate Count");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                textView.setText(String.valueOf(numberPicker.getValue()));

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void numberSelect(View view) {
        final TextView textView = (TextView) view;
        createNumberPickerDialog(textView);

    }

    void weightButtonsUpdateRegion() {
        int ii = 0;
        String value;
        for (LinearLayout layout : WeightLayouts) {
            Button button = (Button) layout.findViewById(R.id.button);
            TextView plateCount = (TextView) layout.findViewById(R.id.plateCount);
            setPlateCountDefaultState(plateCount);
            int plateInt = (int) ((float) this.currentPlates.get(ii));
            if (this.currentPlates.get(ii) == (float) plateInt)
                value = String.valueOf(plateInt);
            else
                value = String.valueOf(this.currentPlates.get(ii));
            button.setText(value);
            ii++;
        }
    }
    @OnClick(value=R.id.percent)
    public void calculatePercent(){
        RelativeLayout linearLayout = new RelativeLayout(this);
        final BootstrapEditText percentField = new BootstrapEditText(this);

        percentField.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputArray = new InputFilter[1];
        inputArray[0] = new InputFilter.LengthFilter(3);
        percentField.setFilters(inputArray);
        percentField.setHint("95%");
        percentField.setPadding(1, 1, 1, 1);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final BootstrapEditText weightView = (BootstrapEditText) findViewById(R.id.weightField);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(percentField, numPicerParams);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Set Percent");
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                float percent;
                                try {
                                    percent = Float.valueOf(percentField.getText().toString());
                                    percent = percent > 0 ? percent / 100 : 0;
                                }
                                catch (NumberFormatException ex){
                                    percent = 1;
                                }
                                int curVal;
                                try {
                                    curVal = Integer.valueOf(weightView.getText().toString());
                                }
                                catch (NumberFormatException ex){
                                    curVal = 0;
                                }
                                int total = (int)(percent * curVal);
                                total = total > MAX_WEIGHT ? 999 : total;

                                if(total > 0)
                                    weightView.setText(String.valueOf(total));
                                else
                                    weightView.setText("");

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private class PlateWatcher implements TextWatcher {
        private TextView editText;

        public PlateWatcher(LiftCalculator l, TextView e) {
            editText = e;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //pass
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //pass
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(editText.getTag() == EDIT_TAG){
                editText.setTag(null);
                return; //non-user edit
            }
            TextView sibling;
            HashMap<Float, Integer> weights = new HashMap<>();
            int plateCount;
            int ii = 0;
            float sum = 0;
            for(LinearLayout layout: WeightLayouts){
                sibling = (TextView) layout.findViewById(R.id.plateCount);

                if(!sibling.isFocusable()) //disabled
                    continue;

                try {
                    plateCount = Integer.valueOf(sibling.getText().toString());
                }
                catch (NumberFormatException ex){
                    plateCount = 0;
                }
                weights.put(currentPlates.get(ii), plateCount);
                sum += currentPlates.get(ii) * plateCount;
                ii++;
            }
            drawPlates(weights);
            BootstrapEditText weightField = (BootstrapEditText) findViewById(R.id.weightField);
            weightField.setTag(EDIT_TAG);

            sum = (sum * 2) + barWeight;

            if (sum > MAX_WEIGHT){
                weightField.setText(String.valueOf(MAX_WEIGHT));
            }
            else if(sum > barWeight)
                weightField.setText(String.valueOf((int) sum));
            else
                weightField.setText("");
        }

    }

}


