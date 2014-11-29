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
import java.util.ArrayList;
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
public class frmPrincipalConteoCars extends javax.swing.JFrame {

    CascadeClassifier faceDetector = new CascadeClassifier("cascade.xml");
    MatOfRect faceDetections = new MatOfRect();
    VideoCapture cap = new VideoCapture(0);
    public Mat imagen = new Mat();
    Thread hilo;
    public Thread haz;
    public ArrayList<Point> haz_puntos = new ArrayList<Point>();
    int contador_sube = 0;
    int contador_baja = 0;

    public void hiloHaz() {
        haz = new Thread() {
            public void run() {

                while (true) {
                    System.out.println("holaa");
                    System.out.println(haz_puntos.size());
                    if (!haz_puntos.isEmpty()) {
                        for (int i = 0; haz_puntos.size() < 10; i++) {
                  //  for (int j = 0; j < 100; j++) {

                            Core.circle(imagen, haz_puntos.get(i), 5, new Scalar(0, 0, 255), 6);
                          //  Thread.sleep(10);

                   // }
                        }
                    }

                }
            }
        };
        haz.start();
    }

    private void initCamara() {

        hilo = new Thread() {

            public void run() {
                Point p1 = new Point(50, 400);
                Point p2 = new Point(600, 400);

                Point pfs1 = new Point(30, 150);
                Point pfs2 = new Point(30, 300);

                Point pfb1 = new Point(620, 150);
                Point pfb2 = new Point(620, 300);

                Point pc = new Point();

                boolean baja = false;
                if (cap.isOpened()) {
                    while (true) {
                        try {
                            //Thread.sleep(100);
                            cap.read(imagen);
                            if (!imagen.empty()) {

                                Core.putText(imagen, Extra.retornaFecha(), new Point(10, 25), 1, 2, new Scalar(0, 255, 0), 3);
                                Core.putText(imagen, Extra.retornaHora(), new Point(10, 55), 1, 2, new Scalar(0, 255, 0), 3);
                                Core.putText(imagen, "# Suben: " + contador_sube, new Point(250, 25), 1, 2, new Scalar(0, 255, 0), 3);
                                Core.putText(imagen, "# Bajan: " + contador_baja, new Point(250, 55), 1, 2, new Scalar(0, 255, 0), 3);
                                faceDetector.detectMultiScale(imagen, faceDetections);
                                Core.line(imagen, p1, p2, new Scalar(0, 255, 0), 3);
                                int n_face = 0;
                                Point anterior = new Point(0,0);
                                for (Rect rect : faceDetections.toArray()) {
                                    n_face++;
                                    //Core.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                                    pc.x = rect.x + rect.width / 2;
                                    pc.y = rect.y + rect.height / 2;
                                    haz_puntos.add(pc);
                                    Core.putText(imagen, "#: " + n_face, new Point(rect.x - 20, rect.y - 20), 1, 2, new Scalar(0, 255, 0), 3);
                                    Core.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);
                                    Core.circle(imagen, pc, 5, new Scalar(0, 0, 255), 6);
                                    Core.line(imagen, new Point(rect.x + rect.width / 2, rect.y + rect.height), new Point(imagen.width() / 2, imagen.height()), new Scalar(0, 255, 0), 3);
                                  
                                    
                                    if (pc.y < p1.y) {
                                        baja = true;
                                       
                                    }
                                    
                                    if (pc.y > p1.y) {
                                        baja = false;
                                    }
                                    
                                    
                                    if ((baja == true) && (pc.y == p1.y)) {
                                        
                                        Core.line(imagen, anterior, pc, new Scalar(0, 0, 255), 3);   
                                        Core.line(imagen, p1, p2, new Scalar(0, 0, 255), 3);
                                        Core.line(imagen, pfs1, pfs2, new Scalar(255, 0, 0), 3);
                                        Core.line(imagen, pfb1, pfb2, new Scalar(255, 0, 0), 3);
                                        Core.line(imagen, pfs2, new Point(pfs2.x + 10, pfs2.y - 10), new Scalar(255, 0, 0), 3);
                                        Core.line(imagen, pfb2, new Point(pfb2.x + 10, pfb2.y - 10), new Scalar(255, 0, 0), 3);
                                        Core.line(imagen, pfs2, new Point(pfs2.x - 10, pfs2.y - 10), new Scalar(255, 0, 0), 3);
                                        Core.line(imagen, pfb2, new Point(pfb2.x - 10, pfb2.y - 10), new Scalar(255, 0, 0), 3);
                                        contador_baja++;
                                    }
                                    
                                    if ((pc.y == p1.y) && (baja == false)) {
                                        Core.line(imagen, p1, p2, new Scalar(0, 0, 255), 5);
                                        Core.line(imagen, pfs1, pfs2, new Scalar(0, 0, 255), 5);
                                        Core.line(imagen, pfb1, pfb2, new Scalar(0, 0, 255), 5);
                                        Core.line(imagen, pfb1, new Point(pfb1.x + 10, pfb1.y + 10), new Scalar(0, 0, 255), 3);
                                        Core.line(imagen, pfs1, new Point(pfs1.x + 10, pfs1.y + 10), new Scalar(0, 0, 255), 3);
                                        Core.line(imagen, pfb1, new Point(pfb1.x - 10, pfb1.y + 10), new Scalar(0, 0, 255), 3);
                                        Core.line(imagen, pfs1, new Point(pfs1.x - 10, pfs1.y + 10), new Scalar(0, 0, 255), 3);
                                        contador_sube++;
                                    }
                                    anterior=pc;

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
            int d = 0;
            for (int y = 0; y < bi.getHeight(); y++) {
                int r = 0;
                int g = 0;
                int b = 0;
                Color c1 = new Color(bi.getRGB(x, y));
                r = c1.getRed();
                g = c1.getGreen();
                b = c1.getBlue();
                int m = (r + g + b) / 3;
                d = d + m;
                if (m > 150) {
                    m = 255;
                } else if (m < 110) {
                    m = 0;
                }

                biDestino.setRGB(x, y, new Color(r, g, b).getRGB());
                if ((d / bi.getHeight()) > 125) {
                    for (int i = 0; i < bi.getHeight(); i++) {
                        biDestino.setRGB(x, i, new Color(255, 0, 0).getRGB());
                    }
                }
            }
        }
        return biDestino;
    }

    public void setImage(Image imagen) {

        ImageIcon icon = new ImageIcon(imagen.getScaledInstance(jl_display.getWidth(), jl_display.getHeight(), Image.SCALE_SMOOTH));
        jl_display.setIcon(icon);

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
    public frmPrincipalConteoCars() {
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
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jl_display.setText("jLabel1");

        jButton1.setText("Reiniciar contador");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jl_display, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jl_display, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.contador_baja = 0;
        this.contador_sube = 0;
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jl_display;
    // End of variables declaration//GEN-END:variables
}
