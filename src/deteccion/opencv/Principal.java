package deteccion.opencv;
import vista.frmPlaca;
import java.awt.image.BufferedImage;
import java.io.File;
import rna_analisis_imagen.CarSnapshot;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import com.googlecode.javacv.cpp.opencv_objdetect;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;
import java.awt.Image;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import rna_ocr.Intelligence;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.opencv.core.CvType;
import vista.frmConfigBD;
import vista.frmOcrManual;
import vista.frmConfigRTSP;
import vista.frmReporteBD;

public class Principal extends javax.swing.JFrame {
 public static final String XML_FILE ="/home/fabricio/NetBeansProjects/DeteccionPlacas/data/cascade.xml";
    ArrayList<String> lista_placas = new ArrayList<String>();
    ArrayList<String> lista_validas_parte_letra = new ArrayList<String>();
    ArrayList<String> lista_validas_parte_numero = new ArrayList<String>();
    DefaultListModel listModel;
    DefaultListModel listModel_nombres;
    GregorianCalendar gcHora1;
    IplImage grabbedImage;
    Thread hilo_initCamara;
    Thread hilo_initVisualizacion;
    boolean guardar, pause, local;
    //CvFont font;
    CarSnapshot car;
    BufferedImage panelCarContent;

    public Principal() {
        initComponents();
        initRecursos();
    }

    public IplImage detect(IplImage src){
 		opencv_objdetect.CvHaarClassifierCascade cascade = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(XML_FILE));
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(src,cascade,storage,1.5,3,CV_HAAR_DO_CANNY_PRUNING);
 		cvClearMemStorage(storage);
 		int total_Faces = sign.total();		
 		for(int i = 0; i < total_Faces; i++){
			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			cvRectangle (src,cvPoint(r.x(), r.y()),cvPoint(r.width() + r.x(), r.height() + r.y()),CvScalar.RED,2,CV_AA,0);
                            new frmPlaca(Sub_Image(src, r).getBufferedImage()).setVisible(true);
                }
		return src;
	}		
   public IplImage Sub_Image(IplImage image, CvRect roi){
    IplImage result;
    // set ROI, you may use following two funs:
    //cvSetImageROI( image, cvRect( 0, 0, image->width, image->height ));

    cvSetImageROI(image,roi);
    // sub-image
    result = cvCreateImage( cvSize(roi.width(), roi.height()), image.depth(), image.nChannels() );
    cvCopy(image,result);
    cvResetImageROI(image); // release image ROI
    return result;
}
    private void initRecursos() {
        this.setLocationRelativeTo(null);
        this.listModel = new DefaultListModel();
        this.listModel_nombres = new DefaultListModel();
        this.gcHora1 = new GregorianCalendar();
        this.grabbedImage = null;
        this.guardar = false;
        this.local = true;
        this.font = null;
        this.jtxttiempo.setText("" + 2);
        try {
            Operaciones.systemLogic = new Intelligence(false);
        } catch (Exception ex) {
        }
    }

    private void initCamara_ip() {
        hilo_initCamara = new Thread() {
            public void run() {
                CvCapture capture = opencv_highgui.cvCreateCameraCapture(Operaciones.camara_divece);
                grabbedImage = opencv_highgui.cvQueryFrame(capture);
                while ((grabbedImage = opencv_highgui.cvQueryFrame(capture)) != null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
        hilo_initCamara.start();
    }

    private void initCamara() {
        hilo_initCamara = new Thread() {
            FrameGrabber grabber = new OpenCVFrameGrabber(Operaciones.path_archivo_video);

            public void run() {
                try {
                    grabber.start();
                    grabbedImage = grabber.grab();
                    while (((grabbedImage = grabber.grab()) != null) && (pause == false)) {
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                        }
                    }
                } catch (FrameGrabber.Exception ex) {
                }
            }
        };
        hilo_initCamara.start();
    }

    private void iniciarVisualizacion() {
        hilo_initVisualizacion = new Thread() {
            public void run() {
                int tiempo = 0;
                while (grabbedImage != null) {
                     grabbedImage=detect(grabbedImage);
                    opencv_core.cvRectangle(grabbedImage, cvPoint(100, 145), cvPoint(100, 145), CvScalar.BLUE, 1, 2, 2);
                    IplImage img_temporal = grabbedImage;
                    CvFont font1 = new CvFont(8);
                    cvInitFont(font1, 2, 6.9, 1.0, 1.3, 5, 5);
                 //   cvPutText(img_temporal, Operaciones.retornaFecha(), cvPoint(100, 145), font1, CvScalar.RED);
                    ImageIcon fot = new ImageIcon(img_temporal.getBufferedImage());
                    Icon icono = new ImageIcon(fot.getImage().getScaledInstance(jlimg.getWidth(), jlimg.getHeight(), Image.SCALE_DEFAULT));
                    jlimg.setIcon(icono);
                    try {
                        String nombre_img;
                        if (guardar == true) {
                            int tiempo_ingresado = (Integer.parseInt(jtxttiempo.getText())) * 1000;
                            if (tiempo < tiempo_ingresado) {
                                nombre_img = Operaciones.asignaNombreImg();
                                //   Operaciones.guardarImagen(path + nombre_img, img_temporal);
                                listModel.addElement(nombre_img);
                                jlistanombres.setModel(listModel);
                                String recognizedText = "";
                                try {
                                    CarSnapshot crs = new CarSnapshot(grabbedImage.getBufferedImage());
                                    recognizedText = Operaciones.systemLogic.recognize(crs);
                                    System.out.println("posible placa:" + recognizedText);
                                 //   new frmPlaca(Operaciones.subimage).setVisible(true);
                                } catch (Exception ex) {
                                }
                                lista_placas.add(recognizedText);
                                tiempo = tiempo + 100;
                            } else {
                                tiempo = 0;
                                guardar = false;
                                jbtncapturar.setEnabled(true);
                                validar_placa();

                            }
                        }

                        Thread.sleep(20);
                    } catch (Exception e) {
                    }
                }
            }
        };
        hilo_initVisualizacion.start();
    }

    public void validar_placa() {
        String placa = Operaciones.validar_placa(lista_placas, lista_validas_parte_letra, lista_validas_parte_numero);
        if (!placa.equals("")) {
            //Operaciones.lista_detecciones.add(placa);
            listModel_nombres.addElement(placa);
            jlista_placas.setModel(listModel_nombres);
            Operaciones.exportar(placa);
        }
    }

    public void capturar_placa() {
        listModel.removeAllElements();
        jlistanombres.setModel(listModel);
        jlista_placas.setModel(listModel_nombres);
        lista_placas = new ArrayList<String>();
 //       this.namefolder = Operaciones.asignaNombreImg();
//        if (System.getProperty("os.name").equals("Linux")) {
//            this.path = "/home/fabricio/Imágenes/PLACAS_CAPTURAS/" + namefolder + "/";
//        } else {
//            this.path = "c:\\" + namefolder + "\\";
//        }
//        File dir = new File(path);
//        dir.mkdirs();
        if (this.guardar) {
            this.guardar = false;
            jbtncapturar.setText("CAPTURAR");
        } else {
            this.guardar = true;
            jbtncapturar.setEnabled(false);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        jbtncapturar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jlistanombres = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jtxttiempo = new javax.swing.JTextField();
        jlnumero_capturas = new javax.swing.JLabel();
        jlplaca = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jlimg = new javax.swing.JLabel();
        jl_placa_validada = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jlista_placas = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jbtn_pause = new javax.swing.JButton();
        jbtn_play = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        imageMenu = new javax.swing.JMenu();
        btn_reporte_bd = new javax.swing.JMenuItem();
        btn_abrir_video = new javax.swing.JMenuItem();
        btn_ocr_manual = new javax.swing.JMenuItem();
        btn_salir = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        btn_archivo_video = new javax.swing.JMenuItem();
        btn_camara_ip = new javax.swing.JMenuItem();
        btn_configurar_bd = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Deteccion placas autumatico");
        setResizable(false);

        jbtncapturar.setText("CAPTURAR");
        jbtncapturar.setEnabled(false);
        jbtncapturar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtncapturarActionPerformed(evt);
            }
        });

        jlistanombres.setBackground(new java.awt.Color(217, 217, 217));
        jlistanombres.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlistanombresMouseClicked(evt);
            }
        });
        jlistanombres.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jlistanombresValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jlistanombres);

        jLabel1.setText("tiempo de captura:");

        jtxttiempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxttiempoActionPerformed(evt);
            }
        });

        jlplaca.setText("Capturas:");

        jPanel1.setBackground(new java.awt.Color(217, 217, 217));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Video tiempo real"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlimg, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlimg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jl_placa_validada.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N

        jlista_placas.setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        jScrollPane3.setViewportView(jlista_placas);

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 24)); // NOI18N
        jLabel2.setText("SISTEMA DETECCION PLACAS VEHICULARES");

        jbtn_pause.setText("PAUSA");
        jbtn_pause.setEnabled(false);
        jbtn_pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtn_pauseActionPerformed(evt);
            }
        });

        jbtn_play.setText("Reproducir");
        jbtn_play.setEnabled(false);
        jbtn_play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtn_playActionPerformed(evt);
            }
        });

        jButton1.setText("INICIAR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        menuBar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        imageMenu.setText("Opciones");
        imageMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        btn_reporte_bd.setText("Reporte base datos");
        btn_reporte_bd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_reporte_bdActionPerformed(evt);
            }
        });
        imageMenu.add(btn_reporte_bd);

        btn_abrir_video.setText("abrir video");
        btn_abrir_video.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_abrir_videoActionPerformed(evt);
            }
        });
        imageMenu.add(btn_abrir_video);

        btn_ocr_manual.setText("Reonocimiento manual");
        btn_ocr_manual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ocr_manualActionPerformed(evt);
            }
        });
        imageMenu.add(btn_ocr_manual);

        btn_salir.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btn_salir.setText("Salir");
        btn_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_salirActionPerformed(evt);
            }
        });
        imageMenu.add(btn_salir);

        menuBar.add(imageMenu);

        jMenu2.setText("fuente de video");

        btn_archivo_video.setText("Archivo de video");
        btn_archivo_video.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_archivo_videoActionPerformed(evt);
            }
        });
        jMenu2.add(btn_archivo_video);

        btn_camara_ip.setText("Camara Ip");
        btn_camara_ip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_camara_ipActionPerformed(evt);
            }
        });
        jMenu2.add(btn_camara_ip);

        menuBar.add(jMenu2);

        btn_configurar_bd.setText("Base de datos");

        jMenuItem6.setText("Configurar");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        btn_configurar_bd.add(jMenuItem6);

        menuBar.add(btn_configurar_bd);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jtxttiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1)
                    .addComponent(jbtncapturar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jlplaca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlnumero_capturas, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jbtn_pause)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbtn_play)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jl_placa_validada, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(312, 312, 312)
                .addComponent(jLabel2)
                .addContainerGap(262, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbtncapturar)
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jtxttiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlplaca, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlnumero_capturas, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 62, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jl_placa_validada, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jbtn_pause)
                                .addComponent(jbtn_play)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jbtncapturarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtncapturarActionPerformed
        this.capturar_placa();
    }//GEN-LAST:event_jbtncapturarActionPerformed
    private void jtxttiempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxttiempoActionPerformed
    }//GEN-LAST:event_jtxttiempoActionPerformed
    private void jlistanombresMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlistanombresMouseClicked
    }//GEN-LAST:event_jlistanombresMouseClicked
    private void btn_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_salirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btn_salirActionPerformed
    private void jlistanombresValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jlistanombresValueChanged
    }//GEN-LAST:event_jlistanombresValueChanged

    private void btn_reporte_bdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_reporte_bdActionPerformed
        new frmReporteBD().setVisible(true);
    }//GEN-LAST:event_btn_reporte_bdActionPerformed

    private void jbtn_pauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtn_pauseActionPerformed
        hilo_initCamara.suspend();
        jbtn_play.setEnabled(true);
        jbtn_pause.setEnabled(false);
    }//GEN-LAST:event_jbtn_pauseActionPerformed

    private void jbtn_playActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtn_playActionPerformed
        hilo_initCamara.resume();
        jbtn_play.setEnabled(false);
        jbtn_pause.setEnabled(true);
    }//GEN-LAST:event_jbtn_playActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       // if (local) {
         //   initCamara();
        //} else {
            initCamara_ip();
        //}

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.iniciarVisualizacion();
        jbtncapturar.setEnabled(true);
        jbtn_pause.setEnabled(true);

    }//GEN-LAST:event_jButton1ActionPerformed
    private void abrirVideo() {
        JFileChooser JFC = new JFileChooser();
        int abrir = JFC.showDialog(null, "Abrir");
        if (abrir == JFileChooser.APPROVE_OPTION) {
            File archivo = JFC.getSelectedFile();
            Operaciones.path_archivo_video = archivo.getAbsolutePath();
        }
    }
    private void btn_ocr_manualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ocr_manualActionPerformed
        new frmOcrManual().setVisible(true);
    }//GEN-LAST:event_btn_ocr_manualActionPerformed

    private void btn_abrir_videoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_abrir_videoActionPerformed
        abrirVideo();
    }//GEN-LAST:event_btn_abrir_videoActionPerformed

    private void btn_archivo_videoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_archivo_videoActionPerformed
        this.local = true;
        abrirVideo();
    }//GEN-LAST:event_btn_archivo_videoActionPerformed

    private void btn_camara_ipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_camara_ipActionPerformed
        new frmConfigRTSP().setVisible(true);
        this.local = false;
    }//GEN-LAST:event_btn_camara_ipActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        new frmConfigBD().setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem btn_abrir_video;
    private javax.swing.JMenuItem btn_archivo_video;
    private javax.swing.JMenuItem btn_camara_ip;
    private javax.swing.JMenu btn_configurar_bd;
    private javax.swing.JMenuItem btn_ocr_manual;
    private javax.swing.JMenuItem btn_reporte_bd;
    private javax.swing.JMenuItem btn_salir;
    private javax.swing.JMenu imageMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton jbtn_pause;
    private javax.swing.JButton jbtn_play;
    private javax.swing.JButton jbtncapturar;
    private javax.swing.JLabel jl_placa_validada;
    private javax.swing.JLabel jlimg;
    private javax.swing.JList jlista_placas;
    private javax.swing.JList jlistanombres;
    private javax.swing.JLabel jlnumero_capturas;
    private javax.swing.JLabel jlplaca;
    private javax.swing.JTextField jtxttiempo;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
}
