package pw.feist.liftcalculator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PlateWatcher implements TextWatcher {
    private EditText editText;
    private LiftCalculator liftCalc;

    public PlateWatcher(LiftCalculator l, EditText e) {
        liftCalc = l;
        editText = e;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //pass
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //pass
    }

    public void afterTextChanged(Editable s) {
        //liftCalc.createPlates();
    }

}
