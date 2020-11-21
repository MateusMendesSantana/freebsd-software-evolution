package atividade;

import java.io.*;
import java.net.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

class Main {
  static String dataset = "https://raw.githubusercontent.com/MateusMendesSantana/freebsd-dataset/master";
  static String filenames[] = {"if", "kern_descrip", "machdep", "pmap", "tcp_input", "vfs_bio", "vfs_subr", "vfs_syscalls", "vm_map", "vm_object", "vm_page"};
  
  static List<ClassCounter> classes = new ArrayList<ClassCounter>();
  static List<MethodCounter> methods = new ArrayList<MethodCounter>();
  static List<ClassCounter> openClasses = new ArrayList<ClassCounter>();
  static List<MethodCounter> openMethods = new ArrayList<MethodCounter>();
  static int lineCount = 0;
  
  public static void main(String[] args)throws Exception  {
    PrintWriter writer = new PrintWriter("SAIDA.CSV", "UTF-8");
    writer.println("MÊS,LOC,CLASSES,MÉTODOS,CLASSE DEUS,MÉTODO DEUS");
    for(int i = 1; i <=12; i++)
    {
      for(String filename : filenames) {
        download(filename, i, writer);
      }
      writer.println(
	      (i + 1) + "," +
	      lineCount + "," +
	      classes.size() + "," +
	      methods.size() + "," +
	      classes.stream().filter(it -> it.isGold()).count() + "," +
	      methods.stream().filter(it -> it.isGold()).count() + ","
		);
      lineCount = 0;
      classes.clear();
      methods.clear();
      openClasses.clear();
      openMethods.clear();
    }
    writer.close();
    System.out.println("Terminou");
  }
  
  private static void download(String filename, int i, PrintWriter writer) {
    try {
      URL url = new URL(dataset + "/" + i + "/" + filename + ".c");
      InputStreamReader stream = new InputStreamReader(url.openStream());
      BufferedReader reader = new BufferedReader(stream);
      Object[] lines = reader.lines().toArray();
      Read(filename, i, lines);
      reader.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static void Read(String filename, int index, Object[] lines) throws Exception {
    boolean multilineComment = false;

    for (int i = 0; i < lines.length; i++) {
      String previusLine = i > 0 ? (String)lines[i-1] : "";
      String line = (String)lines[i];

      if(Pattern.compile("\\/\\*").matcher(line).find()) {
        multilineComment = true;
        continue;
      }
      
      if(Pattern.compile("\\*\\/").matcher(line).find()) {
        multilineComment = false;
        continue;
      }
        
      if(multilineComment)
        continue;
      
      if(Pattern.compile("\\/\\/").matcher(line).find()) {
        continue;
      }

      if(line.replaceAll(" ", "").length() == 0) {
        continue;
      }
      
      lineCount++;

      if(Pattern.compile("(public|private|protected)*\\s*(static|inner|abstract)*\\s*class(<.*>)*(\\[\\])*\\s*\\w+").matcher(line).find()) {
    	ClassCounter currentClass = new ClassCounter();
		classes.add(currentClass);
		openClasses.add(currentClass);
      }
        
      if(
		  !previusLine.contains("@Override") &&
		  Pattern.compile("(public|private|protected)+\\s+(static|inner|abstract)*\\s*(\\w|\\.)+(<.*>)*(\\[\\])*\\s*\\w+\\((\\w*\\s*(\\w|\\.)+\\s\\w+,*\\s*)*\\)\\s*\\{").matcher(line).find()
	  ) {
    	MethodCounter currentMethod = new MethodCounter();
        methods.add(currentMethod);
        openMethods.add(currentMethod);
      }
      
      openMethods.forEach(it -> it.lineCount++);
      openClasses.forEach(it -> it.lineCount++);
      
      if(line.contains("{")) {
    	long occurences = line.chars().filter(it -> it == '{').count();
    	openClasses.forEach(it -> it.deep+= occurences);
    	openMethods.forEach(it -> it.deep+= occurences);
      }

      if(line.contains("}")) {
    	long occurences = line.chars().filter(it -> it == '}').count();
        openClasses.forEach(it -> it.deep-= occurences);
        openClasses.removeIf(it -> it.isEnd());

        openMethods.forEach(it -> it.deep-= occurences);
        openMethods.removeIf(it -> it.isEnd());
        
      }
    }
    
    System.out.println(
      filename + ", " +
      lineCount + ", " +
      classes.size() + ", " +
      methods.size() + ", " +
      classes.stream().filter(it -> it.isGold()).count() + ", " +
      methods.stream().filter(it -> it.isGold()).count() + ", "
    );
  }
}
