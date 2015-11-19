package cz.cvut.cmp.skew;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class TestOpenCVSkew {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        //read image
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png");
        //show image in window
        OCVUtils.showImage(img);

        //create transformation matrix (eye = identity)
        Mat m = Mat.eye(2, 3, CvType.CV_32FC1);
        Mat dst = new Mat();
        Imgproc.warpAffine(img, dst, m, img.size());
        //after transformation, we expect to get same image
        OCVUtils.showImage(dst);

        //now we set some skew value  ....
        m.put(0, 1, Math.tan(Math.PI / 16));
        Imgproc.warpAffine(img, dst, m, img.size(), Imgproc.INTER_LINEAR, Imgproc.BORDER_CONSTANT, new Scalar(255, 255, 255));
        // and display result
        OCVUtils.showImage(dst);

        VerticalDominant vd = new VerticalDominant();
        System.out.println("Uhel sklonu: " + vd.getTheRightSkew(img));


    }
}
