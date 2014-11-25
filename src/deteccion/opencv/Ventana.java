/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deteccion.opencv;

import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
 
/**
*
* @author root
*/
public class Ventana extends JFrame{
    JPanel panel;
    JLabel etiqueta;
 
    public Ventana(){
        setTitle("Reconocimiento de Rostros mediante Webcam con OpenCV y Java");
        setLocation(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600,400);
        setResizable(true);
        setVisible(true);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        etiqueta = new JLabel();
        panel.add(etiqueta);
 
        getContentPane().add(panel);
    }
 
    public void setImage(Image imagen){
        panel.removeAll();
 
        ImageIcon icon = new ImageIcon(imagen.getScaledInstance(etiqueta.getWidth(), etiqueta.getHeight(), Image.SCALE_SMOOTH));
        etiqueta.setIcon(icon);
 
        panel.add(etiqueta);
        panel.updateUI();
    }
}

