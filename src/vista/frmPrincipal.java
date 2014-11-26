/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista;

import deteccion.opencv.Extra;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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

/**
 *
 * @author fabricio
 */
public class frmPrincipal extends javax.swing.JFrame {

    CascadeClassifier faceDetector = new CascadeClassifier("cascade.xml");
    MatOfRect faceDetections = new MatOfRect();
    VideoCapture cap = new VideoCapture(0);
    Mat imagen = new Mat();
    Thread hilo;

    private void initCamara() {
        hilo = new Thread() {

            public void run() {
                if (cap.isOpened()) {
                    while (true) {
                        try {
                            //Thread.sleep(100);
                            cap.read(imagen);
                            if (!imagen.empty()) {
                                Core.putText(imagen, Extra.retornaFecha(), new Point(10, 25), 1, 2, new Scalar(0, 255, 0), 3);
                                Core.putText(imagen, Extra.retornaHora(), new Point(10, 55), 1, 2, new Scalar(0, 255, 0), 3);
                                faceDetector.detectMultiScale(imagen, faceDetections);
                                for (Rect rect : faceDetections.toArray()) {
                                    //Core.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                                    Core.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);
                                    Core.line(imagen, new Point(rect.x + rect.width / 2, rect.y + rect.height), new Point(imagen.width() / 2, imagen.height()), new Scalar(0, 255, 0), 3);
                                    setPlacaImage(convertir(Sub_Image(imagen, rect)));
                                    setPlacaFiltradaImage(filtrar(convertirBufferedImage(Sub_Image(imagen, rect))));
                                }
                                setImage(convertir(imagen));
                            }
                        } catch (Exception ex) {
                            //     Logger.getLogger(frmPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };
        hilo.start();
    }

    public Mat Sub_Image(Mat image, Rect roi) {
        Mat result = image.submat(roi);
        return result;
    }

    public BufferedImage filtrar(BufferedImage bi) {
        BufferedImage biDestino = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(bi.getWidth(), bi.getHeight(), Transparency.OPAQUE);
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int r = 0;
                int g = 0;
                int b = 0;
                Color c1 = new Color(bi.getRGB(x, y));
                r = c1.getRed();
                g = c1.getGreen();
                b = c1.getBlue();
                int m=(r+g+b)/3;
                if(m>150)m=255;
             else
                 if(m<110)m=0;

                
                biDestino.setRGB(x, y, new Color(m, m, m).getRGB());
            }
        }
        return biDestino;
    }

    public void setImage(Image imagen) {

        ImageIcon icon = new ImageIcon(imagen.getScaledInstance(jl_display.getWidth(), jl_display.getHeight(), Image.SCALE_SMOOTH));
        jl_display.setIcon(icon);

        //  panel.add(etiqueta);
        // panel.updateUI();
    }

    public void setPlacaImage(Image imagen) {
        //  panel.removeAll();

        ImageIcon icon = new ImageIcon(imagen.getScaledInstance(jl_placa.getWidth(), jl_placa.getHeight(), Image.SCALE_SMOOTH));
        jl_placa.setIcon(icon);

        //  panel.add(etiqueta);
        // panel.updateUI();
    }

    public void setPlacaFiltradaImage(BufferedImage img) {
        //  panel.removeAll();
        ImageIcon fot = new ImageIcon(img);
        Icon icono = new ImageIcon(fot.getImage().getScaledInstance(jl_placa_filtrada.getWidth(), jl_placa_filtrada.getHeight(), Image.SCALE_DEFAULT));
        jl_placa_filtrada.setIcon(icono);

        //  panel.add(etiqueta);
        // panel.updateUI();
    }

    private BufferedImage convertirBufferedImage(Mat imagen) {
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
        return bufImage;
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

    /**
     * Creates new form frmPrincipal
     */
    public frmPrincipal() {
        initComponents();
        this.initCamara();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jl_display = new javax.swing.JLabel();
        jl_placa = new javax.swing.JLabel();
        jl_placa_filtrada = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jl_display.setText("jLabel1");

        jl_placa.setText("jLabel1");

        jl_placa_filtrada.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jl_display, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jl_placa, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jl_placa_filtrada, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(69, 69, 69))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jl_display, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jl_placa, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(jl_placa_filtrada, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 223, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jl_display;
    private javax.swing.JLabel jl_placa;
    private javax.swing.JLabel jl_placa_filtrada;
    // End of variables declaration//GEN-END:variables
}
