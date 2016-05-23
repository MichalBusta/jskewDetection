package cz.cvut.cmp.skew;

import org.omg.CORBA.FREE_MEM;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.Core.bitwise_not;

/**
 * Created by J����� on 1. 5. 2016.
 */
public class ConvexHullEstimator extends SkewEstimator {
    @Override
    double estimateSkew(Mat img) {

        //invert image
        Mat invImg = new Mat();
        bitwise_not(img, invImg);

        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();
        //nalezneme contury v obrazku (pouze externi - tj. nezajimaji nas diry v blobech)
        Imgproc.findContours(invImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        MatOfInt[] HullMOI = new MatOfInt[contours.size()];
        Mat draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
        byte[] data = new byte[3];
        data[0] = (byte) 200;
        List<Point> contList;
        List<Point> hullPoints;
        int[] hullArray;


        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint cont = contours.get(i);
            Mat draw2 = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
            Imgproc.drawContours(draw, contours, i, new Scalar(255, 255, 255));
            OCVUtils.showImage(draw);
            contList = cont.toList();
            HullMOI[i] = new MatOfInt();
            Imgproc.convexHull(cont, HullMOI[i]);
            hullArray = HullMOI[i].toArray();
            hullPoints = new ArrayList<>();
            for (int j = 0; j < hullArray.length; j++) {
                hullPoints.add(contList.get(hullArray[j]));
                draw2.put((int) hullPoints.get(j).y, (int) hullPoints.get(j).x, data);
            }
            getHullContourPoints(hullPoints);

            OCVUtils.showImage(draw2);

        }
        return 0;
    }

    // using the Bresenham's alg.
    public void getHullContourPoints(List<Point> points) {
        int x0, y0, x1, y1;
        int ax, ay, bx, by;
        int dx, dy;
        int octant;
        double m;
        List<Point> hullPts = new LinkedList<>();
        List<Point> line = new LinkedList<>();
        for (int i = 0; i < points.size(); i++) {
            // get the line initial and end point
            if (i == 0) {
                x0 = (int) points.get(points.size() - 1).x;
                y0 = (int) points.get(points.size() - 1).y;
                x1 = (int) points.get(i).x;
                y1 = (int) points.get(i).y;
            } else {
                x0 = (int) points.get(i - 1).x;
                y0 = (int) points.get(i - 1).y;
                x1 = (int) points.get(i).x;
                y1 = (int) points.get(i).y;
            }

            //evaluate the slope m and switch to the first octant
            /*
             *  Octants:
             *
             *  +---
                 |0/
                 |/1
                 +---
                 |\2
                 |3\ */

            int pom, pom2;
            if (x0 > x1) {
                pom = x0;
                pom2 = y0;
                x0 = x1;
                x1 = pom;
                y0 = y1;
                y1 = pom2;

            }

            System.out.println("Poc. bod: " + x0 + "; " + y0 + "  Kon. bod: " + x1 + "; " + y1);
            dx = x1 - x0;
            dy = y1 - y0;
            if (dx != 0) {
                m = dy / dx;
            } else {
                // TODO
                System.out.println("Svisla primka");
                continue;
            }


            pom = x0;
            pom2 = x1;
            if (m > 0) {
                if (m > 1) {
                    octant = 3;
                    x0 = y0;
                    y0 = pom;
                    x1 = y1;
                    y1 = pom2;

                } else {
                    octant = 2;
                }
            } else {
                if (Math.abs(m) > 1) {
                    octant = 0;
                    x0 = -y0;
                    y0 = pom;
                    x1 = -y1;
                    y1 = pom2;
                } else {
                    octant = 1;
                    y0 = -y0;
                    y1 = -y1;
                }
            }

            System.out.println("Oktant: " + octant);

            System.out.println("2. Poc. bod : " + x0 + "; " + y0 + "  Kon. bod: " + x1 + "; " + y1);


            /*
            * Desicion paramter p0 = 2dy - dx
            * */
            ax = x0;
            ay = y0;
            bx = x1;
            by = y1;
            line.add(new Point(ax, ay));
            dx = bx - ax;
            dy = by - ay;

            int po = (2 * dy) - dx;

            for (int j = 0; j < Math.abs(dx); j++) {
                if (po < 0) {
                    po = po + 2 * dy;
                    line.add(new Point(ax + 1, ay));
                    ax = ax + 1;
                } else {
                    po = po + 2 * dy - 2 * dx;
                    line.add(new Point(ax + 1, ay + 1));
                    ax = ax + 1;
                    ay = ay + 1;
                }
            }

            // switch the line points back to the initial octant
            int px, py;
            switch (octant) {
                case 0:
                    for (int k = 0; k < line.size(); k++) {
                        px = (int) line.get(k).x;
                        py = (int) line.get(k).y;
                        line.get(k).x = py;
                        line.get(k).y = -px;
                    }
                    break;
                case 1:
                    for (int k = 0; k < line.size(); k++) {
                        px = (int) line.get(k).x;
                        py = (int) line.get(k).y;
                        line.get(k).x = px;
                        line.get(k).y = -py;
                    }
                    break;

                case 2: {

                    break;
                }
                case 3: {
                    for (int k = 0; k < line.size(); k++) {
                        px = (int) line.get(k).x;
                        py = (int) line.get(k).y;
                        line.get(k).x = py;
                        line.get(k).y = px;
                    }
                    break;
                }

                default:
                    break;
            }
            System.out.println(line);
            hullPts.addAll(line);
            line.clear();
        }


    }

    @Override
    double estimateSkew(Mat skew, double i, double f, int max) {
        return 0;
    }

    @Override
    int[] getIterations() {
        return new int[0];
    }
}
