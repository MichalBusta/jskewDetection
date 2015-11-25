package cz.cvut.cmp.skew;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

public class OCVUtils {

	static void showImage(Mat img){
		
		MatOfByte matOfByte = new MatOfByte();
	    Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		
		BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        final JFrame frame = new JFrame();
	        
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        final JDialog dlg = new JDialog(frame, true);
	        dlg.addKeyListener(new KeyListener(){

				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					dlg.dispatchEvent(new WindowEvent(dlg, WindowEvent.WINDOW_CLOSING));
					
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
	        	
	        });
	        dlg.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        dlg.pack();
	        dlg.setVisible(true);
	        frame.dispose();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
		
	}

}
