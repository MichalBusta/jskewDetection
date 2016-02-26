package cz.cvut.cmp.skew;

import static org.opencv.core.Core.bitwise_not;

import java.awt.*;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ContourSkewEstimator extends SkewEstimator {


    @Override
    double estimateSkew(Mat img) {

        //invert image
        Mat invImg = new Mat();
        bitwise_not(img, invImg);

        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();
        //nalezneme contury v obrazku (pouze externi - tj. nezajimaji nas diry v blobech)
        Imgproc.findContours(invImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        //podivame se na nalezene kontury:

        Mat draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
        Imgproc.drawContours(draw, contours, -1, new Scalar(255, 255, 255));

        OCVUtils.showImage(draw);

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint cont = contours.get(i);
            draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
            Imgproc.drawContours(draw, contours, i, new Scalar(255, 255, 255));
            OCVUtils.showImage(draw);
            double skewEstimate = estimateContourSkew(cont);

            //TODO voting in histogram ....
        }

        return 0;
    }

    @Override
    double estimateSkew(Mat skew, double i, double f, int max) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    int[] getIterations() {
        // TODO Auto-generated method stub
        return null;
    }

    protected double estimateContourSkew(MatOfPoint contour) {
        MatOfPoint letter = contour;

        org.opencv.core.Point[] points = letter.toArray();
        // points.
        double[] uVector = {1, 0};
        double ux, uy, vx, vy; // u = vector perpendicular to text/line direction, v = the ector of the skew line
        double result;
        ux = 1;
        uy = 0;
        double[] vVector = getSkewVector(points);
        if (vVector == null) {
            System.out.println("Moc maly znak. \n");
            return 0; // ?? Vratit nejakou smysluplnou hodnotu
        }
        vx = vVector[0];
        vy = vVector[1];

        result = Math.acos(((ux * vx) + (uy * vy)) / ((Math.sqrt((ux * ux) + (uy * uy))) * ((Math.sqrt((vx * vx) + (vy * vy))))));
        result = (90 - Math.toDegrees(result));

        System.out.println("Uhel: " + result);
        System.out.println();
        return result;
    }

    public double[] getSkewVector(org.opencv.core.Point[] points) {
        Integer bottomMost, topMost, rightmostTop, rightmostBottom, leftmostTop, leftmostBottom, centerTop, centreBottom;
        int[] center = new int[2];
        double MaxHeight = points[0].y;
        double MaxWidth = points[0].x;
        double MinHeight = points[0].y;
        double MinWidth = points[0].x;
        double h;
        double[] skewVector = new double[2];

        // get the bounding of the character
        for (int i = 1; i < points.length; i++) {
            if (points[i].x > MaxWidth) {
                MaxWidth = points[i].x;
            } else {
                if (points[i].x < MinWidth) {
                    MinWidth = points[i].x;
                }
            }
            if (points[i].y > MinHeight) {
                MinHeight = points[i].y;
            } else {
                if (points[i].y < MaxHeight) {
                    MaxHeight = points[i].y;
                }
            }

        }
        center[0] = (int) (MaxWidth + MinWidth) / 2;
        center[1] = (int) (MaxHeight + MinHeight) / 2;


        /** find the bottomMost and topmost point, perpendicular to the textline direction, passing through the center
         * while y = 0 being the top of the image*/
        bottomMost = null;
        topMost = null;
        for (int j = 0; j < points.length; j++) {
            if (points[j].x == center[0]) {
                if (bottomMost == null) {
                    bottomMost = (int) points[j].y;
                } else {
                    if (points[j].y > bottomMost) {
                        bottomMost = (int) points[j].y;
                    }
                }
                if (topMost == null) {
                    topMost = (int) points[j].y;
                } else {
                    if (points[j].y < topMost) {
                        topMost = (int) points[j].y;
                    }
                }
            }
        }

        // find the right and left-most points in the range of h from the topmost/bottommost points
        leftmostBottom = null;
        leftmostTop = null;
        rightmostBottom = null;
        rightmostTop = null;
        h = (0.08 * (MinHeight - MaxHeight));
        h = Math.round(h);
        if (h < 1) {
            return null;

        } else {
            for (int i = 0; i < points.length; i++) {
                if (points[i].y < topMost + h && points[i].y > topMost - h) {
                    if (leftmostTop == null) {
                        leftmostTop = (int) points[i].x;
                        rightmostTop = (int) points[i].x;
                    } else {
                        if (points[i].x > rightmostTop) {
                            rightmostTop = (int) points[i].x;
                        }
                        if (points[i].x < leftmostTop) {
                            leftmostTop = (int) points[i].x;
                        }
                    }
                }
                if (points[i].y < bottomMost + h && points[i].y > bottomMost - h) {
                    if (leftmostBottom == null) {
                        leftmostBottom = (int) points[i].x;
                        rightmostBottom = (int) points[i].x;
                    } else {
                        if (points[i].x > rightmostBottom) {
                            rightmostBottom = (int) points[i].x;
                        }
                        if (points[i].x < leftmostBottom) {
                            leftmostBottom = (int) points[i].x;
                        }
                    }
                }
            }

        }
        centerTop = (int) (rightmostTop + leftmostTop) / 2;
        centreBottom = (int) (rightmostBottom + leftmostBottom) / 2;
        skewVector[0] = (centerTop - centreBottom);
        skewVector[1] = (topMost - bottomMost);

        System.out.println("Sirka:" + MinWidth + "-" + MaxWidth + "; Vyska: " + MinHeight + "-" + MaxHeight);
        System.out.println("Nejvyssi:" + topMost + "; Nejnizsi: " + bottomMost);
        System.out.println("Horni... Vpravo:" + rightmostTop + " vlevo:" + leftmostTop);
        System.out.println("Dolni... Vpravo:" + rightmostBottom + " dole:" + leftmostBottom);
        System.out.println("Vx a vy: " + skewVector[0] + "; " + skewVector[1]);
        return skewVector;
    }

}
