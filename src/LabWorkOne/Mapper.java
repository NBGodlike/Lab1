package LabWorkOne;

//import net.openhft.chronicle.map.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Mapper {
    public static void main (String[] args) throws InterruptedException, IOException {

        TreeMap <String,Integer> Words = new TreeMap <String, Integer>();

        String ResultStr = "";

        String chunk ="";


        char[] abc = new char[1024*1024+7];
        try(FileReader reader = new FileReader("C://Split//" + args[0] + ".txt"))
        {
            while (reader.read(abc)!=-1)
            {
                chunk = new String(abc);
            }
            chunk = chunk.trim();
        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }

        for (String Word :  chunk.split("/"))
        {
            if (Words.containsKey(Word))
            {
                Words.replace(Word,Words.get(Word)+1);
            } else
            {
                Words.put(Word,1);
            }
        }




       // Thread.sleep(20000);
        String filename = "C://TempMap//" + args[0] + ".txt";
        try (FileWriter writer = new FileWriter(filename,false))
        {
            for(Map.Entry< String,Integer> item : Words.entrySet()) {
                ResultStr = item.getKey() + "=" + item.getValue().toString() + "/";
                writer.write(ResultStr);
            }
            //writer.flush();
        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }

        Files.copy(Paths.get("C://TempMap//" + args[0] + ".txt"),Paths.get("C://Map//" + args[0] + ".txt"));

    }
}
