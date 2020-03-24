package LabWorkOne;


import net.openhft.chronicle.map.ChronicleMap;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LabOne {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        String dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder AllWords = new StringBuilder();
        String Chunk;
        String NewChunk;
        String ChunkEnd = "";

        ChronicleMap<String, Integer> Words = ChronicleMap
                .of(String.class, Integer.class)
                .name("codes-map")
                .averageKey("Aaaaaaaaa")
                .entries(320000000)
                .create();


        AtomicReference<String> Dict = new AtomicReference<>("");

        //TreeMap <String, Integer> Words = new TreeMap<>();

        int ChunkSize = 1024 * 1024;
        int reCalibration;


        int ProcessCount =0;
        int FileNumber = 0; //Added Map
        AtomicInteger FileCount = new AtomicInteger(1);  //Has read

        double MaxSize = 8 * 1024;
        double FileSize = 0;
        double Size = 0;


        char[] ChunkChar = new char[ChunkSize+7];

        MysqlConnect mysqlConnect = new MysqlConnect();

        //Generator
        try(FileWriter writer = new FileWriter("C://File//200.txt",false))
        {
            while (FileSize < 100L * 1024  * 1024) {
                while (Size < MaxSize) {
                    StringBuilder NewWord = new StringBuilder();
                    for (int i = 0; i < 6; i++) {
                        int pos = (int) (Math.random() * dict.length());
                        if (i < 3) {
                            NewWord.append(dict.charAt(pos));
                        } else {
                            if ((int) (Math.random() * 2) == 1) {
                                break;
                            } else {
                                NewWord.append(dict.charAt(pos));
                            }
                        }

                    }
                    Size += NewWord.length() + 1;
                    if (Size < MaxSize)
                        AllWords.append(NewWord).append("/");
                }
                writer.write(AllWords.toString());
                FileSize = FileSize + MaxSize;
                Size = 0;
                AllWords = new StringBuilder();
            }
        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        System.out.println("The file has been written");

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 3);

        try(FileReader reader = new FileReader("C://File//8000.txt"))
        {

            while((reader.read(ChunkChar, 0, ChunkSize))!=-1)
            {
                //Split Chunks
                FileNumber ++;
                Chunk = new String(ChunkChar);
                reCalibration = Chunk.lastIndexOf('/');
                NewChunk = Chunk.substring(0,reCalibration+1);
                NewChunk = ChunkEnd.trim() + NewChunk;
                ChunkEnd = Chunk.substring(reCalibration+1);
                try (FileWriter writer = new FileWriter("C://Split//" + FileNumber + ".txt", false))
                {
                    writer.write(NewChunk);
                }
                catch(IOException ex)
                {
                    System.out.println(ex.getMessage());
                }


                ProcessCount++;
                int finalFileNumber = FileNumber;
                Runnable r = () ->
                {
                    try {
                        Process process = Runtime.getRuntime().exec("java -cp src LabWorkOne.Mapper " + finalFileNumber);
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                };
                threadPool.execute(r);


                int finalFileCount = FileCount.get();
                if (ProcessCount  > FileCount.get())
                {
                    Callable task = () -> {
                        File file = new File("C://Map//" + finalFileCount + ".txt");

                        if ((file.exists()) && (file.length() > 0))
                        {
                            int Value;
                            char[] TempStr = new char[(int)file.length()];
                            String ReducerStr = "";
                            String Key;
                            try (FileReader refile = new FileReader("C://Map//" + finalFileCount + ".txt"))
                            {

                                while ((refile.read(TempStr))!=-1)
                                {
                                    ReducerStr = new String(TempStr);
                                }
                                ReducerStr = ReducerStr.trim();
                            }
                            catch (IOException ex)
                            {
                                System.out.println("Mistake Reducer. File name: " + finalFileCount);
                            }
                            System.out.println("FileReaded1: " + finalFileCount);
                            /*Date stdate = new Date();
                            System.out.println("Start: " + stdate.getTime());
                            mysqlConnect.connect().setAutoCommit(false);
                            String InsertSql = "INSERT INTO dictionary (word, count) VALUES (?,?)";
                            PreparedStatement InsertStatement = mysqlConnect.connect().prepareStatement(InsertSql);
                            int ki = 0;*/

                            for (String Word :  ReducerStr.split("/"))
                            {
                                Key = Word.substring(0,Word.indexOf("="));
                                Value = Integer.parseInt(Word.substring(Word.indexOf("=")+1));
                                /*InsertStatement.setString(1, Key);
                                InsertStatement.setInt(2,Value);
                                InsertStatement.addBatch();
                                System.out.println("Statement: " + ki++);
                                //String SelectSQL = "SELECT * FROM `dictionary` AS dict  WHERE dict.word = ? ";
                                try {
                                    PreparedStatement statement = mysqlConnect.connect().prepareStatement(SelectSQL);
                                    statement.setString(1, Key);
                                    ResultSet resultSet = statement.executeQuery();
                                    if(resultSet.next())
                                    {
                                        String UpdateSQL = "UPDATE dictionary SET count = count + ? WHERE word = ?";
                                        try {
                                            PreparedStatement UpdateStatement = mysqlConnect.connect().prepareStatement(UpdateSQL);
                                            UpdateStatement.setInt(1, Value);
                                            UpdateStatement.setString(2, Key);
                                            UpdateStatement.executeUpdate();
                                            //System.out.println(ResultUpdate);
                                        } catch (SQLException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                    else

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }*/
                                if (Words.containsKey(Key))
                                {
                                    Words.replace(Key, (Words.get(Key) + Value));
                                } else
                                {
                                    Words.put(Key,  Value);
                                }
                            }
                            /*InsertStatement.executeBatch();
                            mysqlConnect.connect().commit();
                            Date stdate1 = new Date();
                            System.out.println("Start: " + stdate1.getTime());*/
                            FileCount.getAndIncrement();
                            return 1;
                        }
                        else
                        {
                            return 0;
                        }
                    };
                    FutureTask<Integer> future = new FutureTask<Integer>(task);
                    new Thread(future).start();
                    future.get();
                }

            }

        }
        catch(IOException  ex){
            System.out.println(ex.getMessage());
        }

        while (ProcessCount >= FileCount.get())
        {
            int finalFileCount = FileCount.get();
            Callable task = () -> {
                File file = new File("C://Map//" + finalFileCount + ".txt");
                if ((file.exists()) && (file.length() > 0))
                {
                    int Value;
                    char[] TempStr = new char[(int)file.length()];
                    String ReducerStr = "";
                    String Key;

                    try (FileReader refile = new FileReader("C://Map//" + finalFileCount + ".txt"))
                    {
                        while (refile.read(TempStr)!=-1)
                        {
                            ReducerStr = new String(TempStr);
                        }
                        ReducerStr = ReducerStr.trim();
                    }
                    catch (IOException ex)
                    {
                        System.out.println("Mistake Reducer part 2. File name: " + finalFileCount);
                    }
                    System.out.println("FileReaded2: " + finalFileCount);
                   /* mysqlConnect.connect().setAutoCommit(false);
                    String InsertSql = "INSERT INTO dictionary (word, count) VALUES (?,?)";
                    PreparedStatement InsertStatement = mysqlConnect.connect().prepareStatement(InsertSql);*/



                    for (String Word :  ReducerStr.split("/"))
                    {

                        Key = Word.substring(0,Word.indexOf("="));
                        Value = Integer.parseInt(Word.substring(Word.indexOf("=")+1));
                        /*InsertStatement.setString(1, Key);
                        InsertStatement.setInt(2,Value);
                        InsertStatement.addBatch();
                        //Database Writer
                        //String SelectSQL = "SELECT * FROM `dictionary` AS dict  WHERE dict.word = ? ";
                        try {
                            PreparedStatement statement = mysqlConnect.connect().prepareStatement(SelectSQL);
                            statement.setString(1, Key);
                            ResultSet resultSet = statement.executeQuery();
                            if(resultSet.next())
                            {
                                String UpdateSQL = "UPDATE dictionary SET count = count + ? WHERE word = ?";
                                try {
                                    PreparedStatement UpdateStatement = mysqlConnect.connect().prepareStatement(UpdateSQL);
                                    UpdateStatement.setInt(1, Value);
                                    UpdateStatement.setString(2, Key);
                                    UpdateStatement.executeUpdate();
                                    //System.out.println(ResultUpdate);
                                } catch (SQLException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                            String InsertSql = "INSERT INTO dictionary (word, count) VALUES (?,?)";
                            try{PreparedStatement InsertStatement = mysqlConnect.connect().prepareStatement(InsertSql);
                            InsertStatement.setString(1, Key);
                            InsertStatement.setInt(2,Value);
                            InsertStatement.executeUpdate();}
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }*/

                        if (Words.containsKey(Key))
                        {
                            Words.replace(Key, (Words.get(Key) + Value));
                        } else
                        {
                            Words.put(Key, Value);
                        }
                    }
                    /*InsertStatement.executeBatch();
                    mysqlConnect.connect().commit();*/
                    FileCount.getAndIncrement();
                    return 1;
                }
                else
                {
                    return 99;
                }

            };
            FutureTask<Integer> future = new FutureTask<Integer>(task);
            new Thread(future).start();
            future.get();

        }
        threadPool.shutdown();
        mysqlConnect.disconnect();
        System.out.println("Все файлы прочитаны");

        try(FileWriter writer = new FileWriter("C://Reduce//Total.txt",false))
        {
            for(Map.Entry< String, Integer> item : Words.entrySet()) {
                Dict.set(item.getKey() + "=" + item.getValue().toString() + "\n");
                writer.write(Dict.get());
            }
            writer.flush();
        }
        catch(IOException ex)
        {
            System.out.println(ex.getMessage());
        }


    }
}