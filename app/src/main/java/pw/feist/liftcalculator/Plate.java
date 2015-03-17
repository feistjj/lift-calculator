package pw.feist.liftcalculator;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by James on 3/17/2015.
 */
public class Plate{
    private Float weight;
    private Float height;
    private Paint paint = new Paint();

    public Plate(Float weight, String hexColor){
        this(weight, hexColor, (float) 1.0);
    }

    public Plate(Float weight, String hexColor, Float height){
        this.weight = weight;
        this.height = height;
        this.paint.setColor(Color.parseColor(hexColor));
    }

    public Paint getPaint(){
        return this.paint;
    }

    public Float getWeight(){
        return this.weight;
    }
    public Float getHeight(){
        return this.height;
    }

}
