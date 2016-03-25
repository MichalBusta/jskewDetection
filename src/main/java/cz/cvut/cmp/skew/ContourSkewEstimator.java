package cz.cvut.cmp.skew;

import static org.opencv.core.Core.bitwise_not;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ContourSkewEstimator extends SkewEstimator {

    private boolean debug = true;

    double[] axisVector; // the axisVector line vector
    int[] a; // coordinates of a point that lies on the axisVector line
    double minX, maxX; // the constraints of the contour


    @Override
    double estimateSkew(Mat img) {

        //invert image
        Mat invImg = new Mat();
        bitwise_not(img, invImg);
        Mat inv2 = new Mat();
        bitwise_not(img, inv2);
        OCVUtils.showImage(inv2);

        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();
        //nalezneme contury v obrazku (pouze externi - tj. nezajimaji nas diry v blobech)
        Imgproc.findContours(invImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        //podivame se na nalezene kontury:

        double symmetry, correlation;


        Mat draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
        Imgproc.drawContours(draw, contours, -1, new Scalar(255, 255, 255));

        OCVUtils.showImage(draw);

        double histogram[] = new double[181];

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint cont = contours.get(i);
            System.out.println();
            draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
            Imgproc.drawContours(draw, contours, i, new Scalar(255, 255, 255));
            OCVUtils.showImage(draw);
            double skewEstimate = estimateContourSkew(cont);
            symmetry = estimateSymmetry(cont.toArray(), inv2);
            correlation = calculateCorrelation(img, cont.toArray());
            System.out.println("Korelace: " + correlation);
            System.out.println("Symetrie: " + symmetry + " %");

            //voting in histogram ....
            double angleDeg = skewEstimate + 90;
            angleDeg = Math.max(0, angleDeg);
            angleDeg = Math.min(180, angleDeg);

            histogram[(int) Math.round(angleDeg)] += 1;

        }

        //smooth the histogram (copy of cpp impl)
        double histogramSmooth[] = new double[180];
        double delta = 3;
        int range = (int) (delta * 3);
        for (int k = 0; k < 180; k++) {
            for (int i = k - range; i <= k + range; i++) {
                int j = i;
                if (j < 0) j += 180;
                if (j >= 180) j -= 180;
                histogramSmooth[k] += histogram[j] / (delta * Math.sqrt(2 * Math.PI)) * Math.pow(Math.E, -((i - k) * (i - k)) / (2 * delta * delta));
            }
        }

        int ignoreAngle = 30;
        int maxI = 0;
        double totalLen = 0;
        double maxVal = 0;
        for (int i = 0; i < 180; i++) {
            if (i > ignoreAngle && i < (180 - ignoreAngle)) {
                if (histogramSmooth[i] > histogramSmooth[maxI]) maxI = i;
                totalLen += histogramSmooth[i];
                maxVal = Math.max(maxVal, histogramSmooth[i]);
            }
        }

        int sigma = 3;
        range = 3;
        double resLen = 0;
        for (int i = maxI - sigma * range; i <= maxI + sigma * range; i++) {
            int j = i;
            if (j < 0) j = j + 180;
            if (j >= 180) j = j - 180;
            if (j > ignoreAngle && j < (180 - ignoreAngle)) {
                resLen += histogramSmooth[j];
            }
        }

        if (debug) {
            int histWidth = 180;
            int histHeight = 100;
            int colWidth = histWidth / 180;

            Mat histogramImg = Mat.zeros(histHeight, histWidth, org.opencv.core.CvType.CV_8UC3);
            //histogramIm = new Scalar(255, 255, 255);

            Core.line(histogramImg, new Point(0, 0), new Point(0, histogramImg.rows()), new Scalar(0, 0, 0));
            Core.line(histogramImg, new Point(90, 0), new Point(90, histogramImg.rows()), new Scalar(0, 0, 0));
            Core.line(histogramImg, new Point(90 + 45, 0), new Point(90 + 45, histogramImg.rows()), new Scalar(100, 100, 100));
            Core.line(histogramImg, new Point(45, 0), new Point(45, histogramImg.rows()), new Scalar(100, 100, 100));
            if (maxVal < 1)
                maxVal = 1;
            double norm = histHeight / maxVal;
            for (int i = 0; i < 180; i++) {
                int colHeight = (int) (histogramSmooth[i] * norm);
                Core.rectangle(histogramImg, new Point(i * colWidth, histHeight), new Point(colWidth * i + colWidth, histHeight - colHeight), new Scalar(255, 0, 0), Core.FILLED);
            }
            Core.line(histogramImg, new Point(0, histogramImg.rows() - 1), new Point(180, histogramImg.rows() - 1), new Scalar(100, 100, 100));

            OCVUtils.showImage(histogramImg);
        }


        double angle = maxI * Math.PI / 180 - Math.PI / 2;
        double probability = (resLen / totalLen);

        System.out.println("Uhel final: " + angle);


        return angle;
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
        double ux, uy, vx, vy; // u = vector perpendicular to text/line direction, v = the vector of the skew line
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
        this.a = new int[2];
        double MaxHeight = points[0].y;
        double MaxWidth = points[0].x;
        double MinHeight = points[0].y;
        double MinWidth = points[0].x;
        double h;
        double[] skewVector = new double[2];
        axisVector = new double[2];

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


        /** find the bottomMost and topmost point, perpendicular to the textline direction, passing through the center,
         *  y = 0 being the top of the image*/
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
        a[0] = centerTop;
        a[1] = topMost;
        centreBottom = (int) (rightmostBottom + leftmostBottom) / 2;
        skewVector[0] = (centerTop - centreBottom);
        skewVector[1] = (topMost - bottomMost);

        this.minX = MinWidth;
        this.maxX = MaxWidth;

        System.out.println("Sirka:" + MinWidth + "-" + MaxWidth + "; Vyska: " + MinHeight + "-" + MaxHeight);
        System.out.println("Nejvyssi:" + topMost + "; Nejnizsi: " + bottomMost);
        System.out.println("Horni... Vpravo:" + rightmostTop + " vlevo:" + leftmostTop);
        System.out.println("Dolni... Vpravo:" + rightmostBottom + " dole:" + leftmostBottom);
        System.out.println("Vx a vy: " + skewVector[0] + "; " + skewVector[1]);
        axisVector = skewVector;
        return skewVector;
    }

    public Double estimateSymmetry(Point[] points, Mat img) {
        Point[] reflections = findReflections(points);
        drawReflection(reflections, points, img);
        double symmetry;
        int symmetric;
        symmetric = 0;
        for (int i = 0; i < points.length; i++) {
            if ((int) reflections[i].x < img.cols() && (int) reflections[i].y < img.rows()) {
                if (img.get((int) reflections[i].y, (int) reflections[i].x)[0] > (255 / 2)) {
                    symmetric++;
                } else {
                    if ((int) reflections[i].x + 1 < img.cols() && (int) reflections[i].y < img.rows()) {
                        if (img.get((int) reflections[i].y, (int) reflections[i].x + 1)[0] > (255 / 2)) {
                            symmetric++;
                        }
                    }
                }

            } else {

                if ((int) reflections[i].x - 1 < img.cols() && (int) reflections[i].y < img.rows() && (int) reflections[i].x - 1 > 0) {
                    if (img.get((int) reflections[i].y, (int) reflections[i].x - 1)[0] > (255 / 2)) {
                        symmetric++;
                    }
                }
            }


        }


        symmetry = 0;
        if (reflections.length != 0) {
            System.out.println("symmetric: " + symmetric);
            symmetry = ((double) (symmetric) / (reflections.length));

        }


        return symmetry;
    }

    public Double calculateCorrelation(Mat img, Point[] points) {
        int px, py, ax, ay; // p is a point on the axis, a is a point of the contour
        double sumX, sumY, meanX, meanY; //x is the
        double a, b, c, correlation;
        double[] xValues = new double[points.length]; // the distance between the point of the contour and the axis
        double[] yValues = new double[points.length]; // distance between the point of the contour closest to the reflection of another point of the contour
        px = this.a[0];
        py = this.a[1];
        Point closest;


        // axis: a*x +b*y +c = 0
        a = this.axisVector[1];
        b = (-this.axisVector[0]);
        c = -((a * px) + (b * py));


        Point[] reflections;
        reflections = findReflections(points);

        // get the y values by calculating the closest point's distance from the axis
        meanX = 0;
        meanY = 0;
        for (int i = 0; i < reflections.length; i++) {
            ax = (int) points[i].x;
            ay = (int) points[i].y;
            xValues[i] = (Math.abs((a * ax) + (b * ay) + c)) / (Math.sqrt((a * a) + (b * b))); // distance between point a and the axis
            meanX += xValues[i];
            closest = findClosestPointDist(reflections[i], img);
            // System.out.println("Bod:" + ax + "; " + ay + " ... Odraz: " + reflections[i].x + "; " + reflections[i].y + " ... Nejblizsi k odrazu: " + closest.x + "; " + closest.y);
            yValues[i] = (Math.abs((a * closest.x) + (b * closest.y) + c)) / (Math.sqrt((a * a) + (b * b))); // distance between the point closest to a and the axis
            meanY += yValues[i];
        }
        if (xValues.length > 0) {
            meanX = meanX / xValues.length;
            meanY = meanY / yValues.length;
        }
        double nom = 0;
        double denom;
        double denomA = 0;
        double denomB = 0;

        for (int i = 0; i < xValues.length; i++) {
            nom += (xValues[i] - meanX) * (yValues[i] - meanY);
            denomA += (xValues[i] - meanX) * (xValues[i] - meanX);
            denomB += (yValues[i] - meanY) * (yValues[i] - meanY);
        }
        denom = Math.sqrt(denomA * denomB);

        correlation = 0;
        if (denom != 0)
            correlation = nom / denom;

        return correlation;
    }


    // finds the closest point within the bounds of the letter and on the same side fo the skew axis as point p
    public Point findClosestPointDist(Point p, Mat img) {
        int count = 1;
        Point closest = null;
        boolean found = false;
        Point n, s, e, w, ne, nw, se, sw;
        int ax; // the value of x on skew axis line in the height of the point
        int sign; // + if the point is on the right side of the axis line, - if on the left
        if (img.get((int) p.y, (int) p.x)[0] > (255 / 2)) {
            return p;
        } else {
            // axis: a*x +b*y +c = 0
            double a = this.axisVector[1];
            double b = (-this.axisVector[0]);
            double c = -((a * this.a[0]) + (b * this.a[1]));
            ax = (int) ((-b * p.y - c) / a);
            sign = (int) (p.x - ax);
            while (found == false) {
                n = new Point(p.x, p.y + count);
                s = new Point(p.x, p.y - count);
                e = new Point(p.x + count, p.y);
                w = new Point(p.x - count, p.y);
                ne = new Point(p.x + count, p.y + count);
                nw = new Point(p.x - count, p.y + count);
                se = new Point(p.x + count, p.y - count);
                sw = new Point(p.x - count, p.y - count);
                if ((int) n.y > 0) {
                    if (img.get((int) n.y, (int) n.x)[0] > (255 / 2)) {

                        return n;
                    }
                }
                if ((int) ne.x < this.maxX && (int) ne.y > 0) {
                    if (img.get((int) ne.y, (int) ne.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * ne.y - c) / a);
                            if ((ne.x - ax) > 0 == sign > 0) {
                                return ne;
                            }
                        } else {
                            ax = (int) ((-b * ne.y - c));
                            if ((ne.x - ax) > 0 == sign > 0) {
                                return ne;
                            }
                        }
                    }
                }
                if ((int) nw.x > this.minX && (int) nw.y > 0) {
                    if (img.get((int) nw.y, (int) nw.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * nw.y - c) / a);
                            if ((nw.x - ax) > 0 == sign > 0) {
                                return nw;
                            }
                        } else {
                            ax = (int) ((-b * nw.y - c));
                            if ((nw.x - ax) > 0 == sign > 0) {
                                return nw;
                            }
                        }
                    }
                }
                if ((int) s.y < img.rows()) {
                    if (img.get((int) s.y, (int) s.x)[0] > (255 / 2)) {
                        return s;
                    }
                }
                if ((int) se.x < this.maxX && (int) se.y < img.rows()) {
                    if (img.get((int) se.y, (int) se.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * se.y - c) / a);
                            if ((se.x - ax) > 0 == sign > 0) {
                                return se;
                            }
                        } else {
                            ax = (int) ((-b * se.y - c));
                            if ((se.x - ax) > 0 == sign > 0) {
                                return se;
                            }
                        }
                    }
                }
                if ((int) sw.x > this.minX && (int) sw.y < img.rows()) {
                    if (img.get((int) sw.y, (int) sw.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * sw.y - c) / a);
                            if ((sw.x - ax) > 0 == sign > 0) {
                                return sw;
                            }
                        } else {
                            ax = (int) ((-b * sw.y - c));
                            if ((sw.x - ax) > 0 == sign > 0) {
                                return sw;
                            }
                        }
                    }
                }
                if ((int) e.x < this.maxX) {
                    if (img.get((int) e.y, (int) e.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * e.y - c) / a);
                            if ((e.x - ax) > 0 == sign > 0) {
                                return e;
                            }
                        } else {
                            ax = (int) ((-b * e.y - c));
                            if ((e.x - ax) > 0 == sign > 0) {
                                return e;
                            }
                        }
                    }
                }
                if ((int) w.x > this.minX) {
                    if (img.get((int) w.y, (int) w.x)[0] > (255 / 2)) {
                        if (a != 0) {
                            ax = (int) ((-b * w.y - c) / a);
                            if ((w.x - ax) > 0 == sign > 0) {
                                return w;
                            }
                        } else {
                            ax = (int) ((-b * w.y - c));
                            if ((w.x - ax) > 0 == sign > 0) {
                                return w;
                            }
                        }
                    }
                }
                count++;
            }
        }

        return closest;
    }


    public void drawReflection(Point[] points, Point[] original, Mat img) {
        Mat draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
        byte[] data = new byte[3];
        data[0] = (byte) 150;
        for (int a = 0; a < points.length; a++) {
            draw.put((int) points[a].y, (int) points[a].x, data);
            draw.put((int) original[a].y, (int) original[a].x, data);
        }
        OCVUtils.showImage(draw);
    }

    public Point[] findReflections(Point[] points) {
        int ax, ay, bx, by, px, py; // point b is the reflection of point a
        double a, b, c, dist, x1, x2;
        px = this.a[0];
        py = this.a[1];
        Point[] reflections = new Point[points.length];

        // axis: a*x +b*y +c = 0
        a = this.axisVector[1];
        b = (-this.axisVector[0]);
        c = -((a * px) + (b * py));

        for (int i = 0; i < points.length; i++) {
            ax = (int) points[i].x;
            ay = (int) points[i].y;
            dist = (Math.abs((a * ax) + (b * ay) + c)) / (Math.sqrt((a * a) + (b * b))); // distance between point a and the axis
            by = ay; // the y coordinate stays the same
            //

            // we get two bx values, we need to compare the distance from the axis to get the right one
            x1 = ax + (2 * dist);
            x2 = ax - (2 * dist);

            double dist1 = +Math.abs((a * x1) + (b * by) + c) / (Math.sqrt((a * a) + (b * b)));
            double dist2 = +Math.abs((a * x2) + (b * by) + c) / (Math.sqrt((a * a) + (b * b)));
            /**
             System.out.println("x1: "+x1);
             System.out.println("x2: "+x2);
             System.out.println("Dx1: "+dist1);
             System.out.println("Dx2: "+dist2);
             */

            if (dist1 - dist < dist2 - dist) {
                bx = Math.round((float) x1);
            } else {
                bx = Math.round((float) x2);
            }

            /**
             System.out.println("[px, py] = " +px+"; "+py);
             System.out.println("[ax, ay]" +ax+"; "+ay);
             System.out.println("[bx, by]" +bx+"; "+by);
             System.out.println("Vzdalenost: " +dist);
             */
            reflections[i] = new Point(bx, by);
        }

        return reflections;
    }


}
