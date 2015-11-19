package cz.cvut.cmp.skew;

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

    static void showImage(Mat img) {

        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();

        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JDialog dlg = new JDialog(frame, true);
            dlg.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            dlg.pack();
            dlg.setVisible(true);
            frame.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
