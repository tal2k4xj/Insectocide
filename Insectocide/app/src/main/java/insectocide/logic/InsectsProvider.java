package insectocide.logic;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class InsectsProvider {
    private final String MULTIPLAYER_CONFIGURATION = "0u-0d-0u-0d-0u-0u-0d-0u-0d-0u-0d-2u-4d-3u-1d-1d-3u-4d-2u-0d-0u-2d-4u-3d-1u-1u-3d-4u-2d-0u-0d-0u-0d-0u-0d-0d-0u-0d-0u-0d";
    private Insect[][] insects;
    private CopyOnWriteArrayList<Insect> liveInsects;
    private List<InsectType> insectTypes;
    private int rows,cols;
    private Context context;
    private DisplayMetrics metrics;
    private Random rand;

    public InsectsProvider(int rows, int cols,String mode, Context context, DisplayMetrics metrics) {
        this.rows = rows;
        this.cols = cols;
        this.context = context;
        this.metrics = metrics;
        insects = new Insect[cols][rows];
        liveInsects = new CopyOnWriteArrayList<>();
        insectTypes = new LinkedList<>();
        rand = new Random();
        if (mode.equals("single")) {
            generateInsectsForSingle();
        }else{
            generateInsectsForMulti();
        }
    }

    private Insect[][] generateInsectsForSingle(){
        CreateInsectTypes();
        for (int i=0 ; i < cols ; i++){
            for(int j=0 ; j < rows ; j++){
                insects[i][j] = new Insect(pickRandomTypeFromList(), "down", context, metrics);
                insects[i][j].setPositionAndDimensions(i,j,rows);
                insects[i][j].bringToFront();
                liveInsects.add(insects[i][j]);
            }
        }
        return insects;
    }
    private Insect[][] generateInsectsForMulti(){
        String[] configs = MULTIPLAYER_CONFIGURATION.split("-");
        CreateInsectTypes();
        for (int i=0 ; i < cols ; i++){
            for(int j=0 ; j < rows ; j++){
                insects[i][j] = createNewInsectFromConfiguration(configs[i+j*cols]);
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
    private Insect createNewInsectFromConfiguration(String configuration){
        int type = Integer.parseInt(configuration.substring(0,1));
        char directionChar = configuration.charAt(1);
        String direction = directionChar=='u' ? "up" : "down";
        return new Insect(InsectType.values()[type], direction , context, metrics);
    }
    public Insect[][] getInsectMatrix(){
        return insects;
    }
    public CopyOnWriteArrayList<Insect> getLiveInsectsList(){
        return liveInsects;
    }
}
