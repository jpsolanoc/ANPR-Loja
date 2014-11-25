/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deteccion.opencv;

/**
 *
 * @author Diego
 */
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

class DetectFaceDemo {

    CascadeClassifier faceDetector = new CascadeClassifier("C:\\Users\\Diego\\Desktop\\cascade.xml");
    MatOfRect faceDetections = new MatOfRect();
    VideoCapture cap = new VideoCapture(0);
    Mat imagen = new Mat();

    public void run() {
        System.out.println("nDeteccion de rostros con OpenCV y Webcam en java");
        Ventana ventana = new Ventana();

        if (cap.isOpened()) {
            while (true) {
                try {
                    //Thread.sleep(100);
                    cap.read(imagen);
                    if (!imagen.empty()) {
                        faceDetector.detectMultiScale(imagen, faceDetections);
                        for (Rect rect : faceDetections.toArray()) {
                            //Core.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                            Core.rectangle(imagen,new Point(rect.x, rect.y) , new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);
                            
                        }
                        ventana.setImage(convertir(imagen));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DetectFaceDemo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private Image convertir(Mat imagen) {
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", imagen, matOfByte);

        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;

        try {

            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (Image) bufImage;
    }
    
    
    
    
}

public class DeteccionOpenCv {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new DetectFaceDemo().run();
    }

}
