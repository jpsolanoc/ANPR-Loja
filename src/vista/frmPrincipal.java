/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista;

import deteccion.opencv.Extra;
import deteccion.opencv.Luminance;
import deteccion.opencv.Picture;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 *
 * @author fabricio
 */
public class frmPrincipal extends javax.swing.JFrame {

    ArrayList<String> lista_placas = new ArrayList<String>();
    ArrayList<String> lista_validas_parte_letra = new ArrayList<String>();
    ArrayList<String> lista_validas_parte_numero = new ArrayList<String>();
    CascadeClassifier faceDetector = new CascadeClassifier("cascade.xml");
    DefaultListModel listModel = new DefaultListModel();
    ;
    DefaultListModel listModel_nombres = new DefaultListModel();
    MatOfRect faceDetections = new MatOfRect();
    // VideoCapture cap = new VideoCapture("rtsp://admin:12345@192.168.10.150:554//Streaming/Channels/1");
    VideoCapture cap = new VideoCapture(0);

    Mat imagen = new Mat();
    Thread hilo;
    Tesseract instance;

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

                               //    Imgproc.adaptiveThreshold(imagen, imagen, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
                                    //      Imgproc.Canny(imagen,imagen, 15, 34);
                                    //     Imgproc.threshold(imagen, imagen, -1, 255, 1);
                                    System.out.println("valor?" );
                                 //   Imgproc.threshold(imagen, imagen, -1, 255, Imgproc.THRESH_OTSU);

                                    setPlacaImage(convertir(Sub_Image(imagen, rect)));

                                    setPlacaFiltradaImage(threshold(convertirBufferedImage(Sub_Image(imagen, rect))));
                                    try {
                                        String result = "ABC-0123";
                                       //  result = instance.doOCR(convertirBufferedImage(Sub_Image(imagen, rect)));
                                        //result="XBU-445   ";
                                        System.out.println(result);
                                        result = Extra.posible_placa(result);
                                        //    result=Extra.posible_placa(result);
                                        lista_placas.add(result);
                                        // System.out.println(result);
                                    } catch (Exception e) {
                                        //   System.err.println(e.getMessage());
                                    }
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
    public BufferedImage threshold(BufferedImage image){
       
         int THRESHOLD = 130;
        Picture pic = new Picture(image);
      
        for (int i = 0; i < pic.width(); i++) {
            for (int j = 0; j < pic.height(); j++) {
                Color color = pic.get(i, j);
                double lum = Luminance.lum(color);
                if (lum >= THRESHOLD) pic.set(i, j, Color.WHITE);
                else                  pic.set(i, j, Color.BLACK);
            }
        }
        pic.show();
        return pic.getImage();
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
        instance = Tesseract.getInstance();  // JNA Interface Mapping
        this.initCamara();
    }

    public void validar_placa() {
        String parte_numero = "";
        String parte_letra = "";
        String placa = "";
        lista_validas_parte_letra.clear();
        lista_validas_parte_numero.clear();
        for (int i = 0; i < lista_placas.size(); i++) {
            if (lista_placas.get(i) != null) {
                if ((lista_placas.get(i).length() == 7) || (lista_placas.get(i).length() == 8)) {
                    System.out.println("TAMAÑO CORRECTO");
                    parte_letra = lista_placas.get(i).substring(0, 3);
                    parte_numero = lista_placas.get(i).substring(4, lista_placas.get(i).length());
                    if ((Extra.conversionLetra(parte_letra) != null) && (Extra.conversionNumero(parte_numero) != null)) {
                        lista_validas_parte_letra.add(parte_letra);
                        lista_validas_parte_numero.add(parte_numero);
                        System.out.println("placa: " + parte_letra + "-" + parte_numero);
                    }
                }
            }
        }
        parte_letra = Extra.moda(lista_validas_parte_letra);
        parte_numero = Extra.moda(lista_validas_parte_numero);
        //  System.out.println(parte_letra);
        System.out.println(parte_numero);
        if (!parte_letra.equals("") && !parte_numero.equals("")) {
            placa = parte_letra + "-" + parte_numero;
            listModel_nombres.addElement(placa);
            //    jlista_placas.setModel(listModel);
            jlista_placas.setModel(listModel_nombres);
        }

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
        jScrollPane3 = new javax.swing.JScrollPane();
        jlista_placas = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jlista_placas.setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        jScrollPane3.setViewportView(jlista_placas);

        jButton1.setText("Detectar placa");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Placa detectada:");

        jLabel2.setText("Placa procesada:");

        jLabel3.setText("Video:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jl_display, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jl_placa, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jl_placa_filtrada, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)))
                .addGap(69, 69, 69))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jl_placa, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jl_placa_filtrada, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addContainerGap(52, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jl_display, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(29, 29, 29))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.out.println("VALIDANDO PLACA");
        validar_placa();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel jl_display;
    private javax.swing.JLabel jl_placa;
    private javax.swing.JLabel jl_placa_filtrada;
    private javax.swing.JList jlista_placas;
    // End of variables declaration//GEN-END:variables
}
