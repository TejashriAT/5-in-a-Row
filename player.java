/*
* This interface contains setter and getter methods of Player's name
*/

public interface player {
    private String name;

    public void setPlayerName(String inputName) {
        name = inputName;
    }

    public String getPlayerName() {
    return name;
    }
}
