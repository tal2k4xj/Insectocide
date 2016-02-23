package insectocide.logic;

import android.os.Parcel;

public class Player implements Comparable<Player> ,android.os.Parcelable{
    private String name;
    private int score;
    private SpaceShip ship;

    public Player(String name, int score) {
        this.name = name;
        this.score = score;
        this.ship = ship;
    }

    protected Player(Parcel in) {
        name = in.readString();
        score = in.readInt();
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public SpaceShip getShip() {
        return ship;
    }

    public void setShip(SpaceShip ship) {
        this.ship = ship;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Player another) {
        if (this.getScore()>another.getScore())
            return -1;
        else if (this.getScore()<another.getScore())
            return 1;
        else
            return 0;
    }
    public String toSaveString() {
        return name + "-@-" + score + "!@#@!";
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(score);
    }
}
