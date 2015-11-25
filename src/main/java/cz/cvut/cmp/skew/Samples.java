package cz.cvut.cmp.skew;

import org.math.plot.Plot2DPanel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;

import java.awt.*;

import static org.opencv.core.Core.bitwise_not;

/**
 * Created by Jссссс on 21. 11. 2015.
 */
public class Samples {
    static {
        nu.pattern.OpenCV.loadShared();
    }

    public void getRelationChart(Mat img) {
        double[] x = new double[89]; // angle values
        double[] y = new double[89]; // entropy values

        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);

        //extend image with border on left and right side of the image
        Imgproc.copyMakeBorder(invImg, invImg, 0, 0, invImg.rows(), invImg.rows(), Imgproc.BORDER_CONSTANT);

        // get the histogram matrix
        Mat rowSumImg = new Mat();
        Core.reduce(invImg, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

        // find the skew
        double entropy = VerticalDominant.calculateEntropy(rowSumImg);
        double angle = 0;
        y[44] = entropy;
        x[44] = angle;

        // try all angles in the range of +-45d from the initial one, compare the entropy
        for (int a = 1; a < 45; a++) {
            Mat edited = new Mat();

            //get the entropy and angle values for positive angles
            SkewEstimator.skewImage(invImg, edited, Math.toRadians(a));
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = VerticalDominant.calculateEntropy(rowSumImg);
            y[44 + a] = entropy;
            x[44 + a] = a;


            //get the entropy and angle values for negative angles
            SkewEstimator.skewImage(invImg, edited, Math.toRadians(-a));
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = VerticalDominant.calculateEntropy(rowSumImg);
            y[44 - a] = entropy;
            x[44 - a] = -a;
        }

        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("Entropy and angle relation chart", x, y);
        plot.setName("The relation between skew angle and entropy of an image"); //not sure what this is supposed to do


        //customize the x axis
        plot.setFixedBounds(1, -45, 45);
        plot.setAxisLabel(0, "ANGLE");


        // cusotmize the y axis
        plot.setFixedBounds(1, 4.2, 4.8);
        plot.setAxisLabel(1, "ENTROPY");

        JFrame frame = new JFrame("a plot panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JDialog dlg = new JDialog(frame, true);
        dlg.setContentPane(plot);
        dlg.pack();
        dlg.setVisible(true);

    }

    public void getVerticalProjectionImage(Mat img) {
        double[] y; // vert. projection values
        double[] x; // hist. columns

        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);
        // get the histogram matrix
        Mat hist = new Mat();
        Core.reduce(invImg, hist, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

        // put the histogram values into an array
        System.out.println(hist.cols());
        y = new double[hist.cols()];
        x = new double[hist.cols()];
        float[] data = new float[3];
        for (int i = 0; i < hist.cols(); i++) {
            hist.get(0, i, data);
            y[i] = data[0];
            x[i] = i;
        }
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i] + " " + y[i]);
        }


        // create the histogram plot
        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("Vertical projection profiles", x, y);

        //customize the x axis
        plot.setFixedBounds(0, 0, 105);

        // cusotmize the y axis (intervals)


        JFrame frame = new JFrame("a plot panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JDialog dlg = new JDialog(frame, true);
        dlg.setContentPane(plot);
        dlg.pack();
        dlg.setVisible(true);
    }

    public void getSampleImages(Mat img) {
        //original
        OCVUtils.showImage(img);
        //extend image with border on left and right side of the image
        Scalar value = new Scalar(255);
        Imgproc.copyMakeBorder(img, img, 0, 0, 2 * img.rows(), 2 * img.rows(), Imgproc.BORDER_CONSTANT, value);

        Mat edited = new Mat();
        // correct skew
        SkewEstimator.skewImage(img, edited, Math.tan(Math.toRadians(14)));
        OCVUtils.showImage(edited);

        //max. angle
        SkewEstimator.skewImage(img, edited, Math.tan(Math.toRadians(45)));
        OCVUtils.showImage(edited);

        // min. angle
        SkewEstimator.skewImage(img, edited, Math.tan(Math.toRadians(-45)));
        OCVUtils.showImage(edited);
    }

    public static void main(String[] args) {

        Samples samples = new Samples();

        // read image
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);


        // get vertical projection profile of the initial skew
        samples.getVerticalProjectionImage(img);

        // get vertical projection profile of the correct skew
        Mat edited = new Mat();
        SkewEstimator.skewImage(img, edited, Math.toRadians(14));
        samples.getVerticalProjectionImage(edited);

        // get the realtion between entropy and skew angle chart
        samples.getRelationChart(img);

        //get sample images for skew
        samples.getSampleImages(img);
    }
}
