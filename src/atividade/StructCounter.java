package atividade;

public class StructCounter {
    public int lineCount = 0;
    public int deep = 0;
    
    public boolean isEnd() {
    	return deep == 0;
    }
    public boolean isGold() {
    	return lineCount > 800;
    }
}
