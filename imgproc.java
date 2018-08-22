package peru.proj1;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

import android.widget.Toast;
import android.app.Activity;

/**
 * Created by mustafa on 28/1/18.
 */


public class MessageSender extends AsyncTask<Double,Void,Void>
{

    static String answer="null";
    public Socket s,mysocket;
    ServerSocket ss;
    DataOutputStream dos;
    public PrintWriter pw;

    @Override
    protected Void doInBackground(Double... args) {

        double var = args[0];

        double idm = args[1];
        double mean = args[2];
        double stdx = args[3];
        double stdy = args[4];
        double corr = args[5];
        double contrast = args[6];
        double energy = args[7];
        double entropy = args[8];
        double hom = args[9];
        double shade = args[10];
        double prom = args[11];
        double inertia = args[12];
        double red = args[13];
        double green = args[14];
        double blue = args[15];
        double gbyr = args[16];
        double gbyb = args[17];



        String str=Double.toString(var);
        str=str+"#";
        str=str+Double.toString(idm);
        str=str+"#";
        str=str+Double.toString(mean);
        str=str+"#";
        str=str+Double.toString(stdx);
        str=str+"#";
        str=str+Double.toString(stdy);
        str=str+"#";
        str=str+Double.toString(corr);
        str=str+"#";
        str=str+Double.toString(contrast);
        str=str+"#";
        str=str+Double.toString(energy);
        str=str+"#";
        str=str+Double.toString(entropy);
        str=str+"#";
        str=str+Double.toString(hom);
        str=str+"#";
        str=str+Double.toString(shade);
        str=str+"#";
        str=str+Double.toString(prom);
        str=str+"#";
        str=str+Double.toString(inertia);
        str=str+"#";
        str=str+Double.toString(red);
        str=str+"#";
        str=str+Double.toString(green);
        str=str+"#";
        str=str+Double.toString(blue);
        str=str+"#";
        str=str+Double.toString(gbyr);
        str=str+"#";
        str=str+Double.toString(gbyb);

        try{
            s = new Socket("192.168.43.107",8883);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(str);
            pw.flush();

            DataInputStream dis2 = new DataInputStream(s.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(dis2));
            answer = br.readLine();
            answer = answer.toString();
            dis2.close();
            pw.close();
            s.close();


        }
        catch (IOException e){
             e.printStackTrace();
        }
      /*try{
            ss= new ServerSocket(8880);
            while(true){
                mysocket= ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                answer=br.readLine();


            }
        }
        catch(IOException e){
            e.printStackTrace();
        }*/

    return null;

    }

}

