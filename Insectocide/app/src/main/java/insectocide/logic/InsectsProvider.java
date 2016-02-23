package insectocide.logic;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Zukis87 on 23/02/2016.
 */
public class InsectsProvider {
    private Insect[][] insects;
    private CopyOnWriteArrayList<Insect> liveInsects;
    private List<InsectType> insectTypes;
    private int rows,cols;
    private Context context;
    private DisplayMetrics metrics;

    public InsectsProvider(int rows, int cols, Context context, DisplayMetrics metrics) {
        this.rows=rows;
        this.cols=cols;
        this.context = context;
        this.metrics = metrics;
        insects = new Insect[rows][cols];
        generateInsects();
        liveInsects = new CopyOnWriteArrayList<>();
        insectTypes = new LinkedList<>();
    }

    private Insect[][] generateInsects(){

        CreateInsectTypes();
        for (int i=0 ; i < cols ; i++){
            for(int j=0 ; j < rows ; j++){
                insects[i][j] = new Insect(pickRandomTypeFromList(),context,metrics);
                insects[i][j].setPositionAndDimensions(i,j);
                insects[i][j].bringToFront();
                liveInsects.add(insects[i][j]);
               // rl.addView(insects[i][j]); need to add in code;
            }
        }
        return insects;
    }
    private void CreateInsectTypes(){
        int numOfRegular = (cols*rows)/2;
        for (int i= 0 ; i<=numOfRegular;i++){
            insectTypes.add(InsectType.Normal);
        }
        int j=1;
        while(insectTypes.size()<=rows*cols){
            insectTypes.add(InsectType.values()[j]);
            j+=1;
            if (j==InsectType.values().length){
                j=1;
            }
        }
    }
    private InsectType pickRandomTypeFromList(){
        Random rand = new Random();
        int n = rand.nextInt(insectTypes.size());
        return insectTypes.remove(n);
    }
    public Insect[][] getInsectMatrix(){
        return insects;
    }
    public CopyOnWriteArrayList<Insect> getLiveInsectsList(){
        return liveInsects;
    }
}
