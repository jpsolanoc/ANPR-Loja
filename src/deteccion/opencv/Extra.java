/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deteccion.opencv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author fabricio
 */
public class Extra {

    //   public static String path_archivo_video="/home/fabricio/Vídeos/placas_ec.mp4";
   // public static String path_archivo_video = "placas_ec.mp4";
    public static String camara_ip_divice = "rtsp://admin:12345@192.168.10.150:554//Streaming/Channels/1";
    public static int camara_divece = 1;
    
     public static String posible_placa(String texto) {
        texto=texto.replace(" ", "");
         System.out.println(texto);
        return texto;
    }
    public static String conversionNumero(String texto) {
        String numero = "";
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isDigit(texto.charAt(i))) {
                numero.concat(texto.charAt(i) + "");
            } else {
                return null;
            }
        }
        return numero;
    }

    public static String conversionLetra(String texto) {
        String letras = "";
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                letras.concat(texto.charAt(i) + "");
            } else {
                return null;
            }
        }
        return letras;
    }

    public static String moda(ArrayList<String> valor) {

        int frecuenciaTemp, frecuenciaModa = 0;
        String moda = "";
       System.out.println("catirdad de placas detectadas:"+valor.size());
        for (int i = 0; i < valor.size() - 1; i++) {
            frecuenciaTemp = 1;
            for (int j = i + 1; j < valor.size(); j++) {
                if (valor.get(i).equals(valor.get(j))) {
                   // System.out.println("igual:"+valor.get(i));
                    frecuenciaTemp++;
                }
            }
            if (frecuenciaTemp > frecuenciaModa) {
                frecuenciaModa = frecuenciaTemp;
                moda = valor.get(i);
            }
        }
        System.out.println("la moda es: " + moda + "nsu frecuencia: " + frecuenciaModa);

        return moda;
    }
    public static String retornaHora() {
        System.out.println("");
        String hora;
        GregorianCalendar gcHora1 = new GregorianCalendar();
        hora = gcHora1.get(Calendar.HOUR) + ":" + gcHora1.get(Calendar.MINUTE) + ":" + gcHora1.get(Calendar.SECOND);
        if (gcHora1.get(Calendar.AM_PM) == 0) {                                                                                      //retorno de calendar.AM_PM
            hora = hora + " am";
        } else {
            hora = hora + " pm";
        }
        return hora;
    }

    public static String retornaFecha() {
        String fecha;
        GregorianCalendar gcHora1 = new GregorianCalendar();
        fecha = gcHora1.get(Calendar.YEAR) + "/" + gcHora1.get(Calendar.MONTH) + "/" + gcHora1.get(Calendar.DATE);
        return fecha;
    }


   

}
