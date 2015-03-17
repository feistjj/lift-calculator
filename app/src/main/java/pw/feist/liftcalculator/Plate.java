package pw.feist.liftcalculator;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by James on 3/17/2015.
 */
public class Plate{
    private Float weight;
    private Paint paint = new Paint();

    public Plate(Float weight, String hexColor){
        this.weight = weight;
        this.paint.setColor(Color.parseColor(hexColor));
    }

    public Paint getPaint(){
        return this.paint;
    }

    public Float getWeight(){
        return this.weight;
    }

}
