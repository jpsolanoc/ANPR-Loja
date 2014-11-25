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

import org.opencv.core.Core;
import vista.frmPrincipal;

public class Run {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new frmPrincipal().setVisible(true);
    }

}
