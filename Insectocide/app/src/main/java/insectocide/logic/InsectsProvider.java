package insectocide.logic;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class InsectsProvider {
    private Insect[][] insects;
    private CopyOnWriteArrayList<Insect> liveInsects;
    private List<InsectType> insectTypes;
    private int rows,cols;
    private Context context;
    private DisplayMetrics metrics;
    private String mode;
    private Random rand;

    public InsectsProvider(int rows, int cols,String mode, Context context, DisplayMetrics metrics) {
        this.rows = rows;
        this.cols = cols;
        this.mode = mode;
        this.context = context;
        this.metrics = metrics;
        insects = new Insect[cols][rows];
        liveInsects = new CopyOnWriteArrayList<>();
        insectTypes = new LinkedList<>();
        rand = new Random();
        generateInsects();
    }

    private Insect[][] generateInsects(){
        CreateInsectTypes();
        for (int i=0 ; i < cols ; i++){
            for(int j=0 ; j < rows ; j++){
                if(mode.equals("single")) {
                    insects[i][j] = new Insect(pickRandomTypeFromList(), "down", context, metrics);
                }else{
                    if((i%2 == 0 && i%2 == 0) || (i%2 == 1 && j%2 == 1)) {
                        insects[i][j] = new Insect(pickRandomTypeFromList(), "down", context, metrics);
                    }else {
                        insects[i][j] = new Insect(pickRandomTypeFromList(), "up", context, metrics);
                    }
                }
                insects[i][j].setPositionAndDimensions(i,j,rows);
                insects[i][j].bringToFront();
                liveInsects.add(insects[i][j]);
            }
        }
        return insects;
    }
    private void CreateInsectTypes(){
        int numOfRegular = (cols*rows)/2;
        for (int i= 0 ; i<numOfRegular;i++){
            insectTypes.add(InsectType.Normal);
        }
        int j=1;
        while(insectTypes.size()<rows*cols){
            insectTypes.add(InsectType.values()[j]);
            j+=1;
            if (j==InsectType.values().length){
                j=1;
            }
        }
    }
    private InsectType pickRandomTypeFromList(){
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
